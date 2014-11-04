package ide;

import static core.Constants.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import playback.PlaybackSession;

import core.Constants;
import core.StorytellerCore;
import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.entities.Developer;
import core.entities.DeveloperGroup;
import core.entities.Directory;
import core.entities.ExtendedDirectory;
import core.entities.Node;
import core.entities.Project;
import core.events.CreateDirectoryEvent;
import core.events.CreateDocumentEvent;
import core.events.DeleteDirectoryEvent;
import core.events.DeleteDocumentEvent;
import core.events.DeleteEvent;
import core.events.InsertEvent;
import core.events.MoveDirectoryEvent;
import core.events.MoveDocumentEvent;
import core.events.OpenNodeEvent;
import core.events.RenameDirectoryEvent;
import core.events.RenameDocumentEvent;
import core.services.json.JSONiffy;

/**
 * This class is used to create a server that communicates with an IDE. Unlike 
 * other servers, this one keeps a socket open with the IDE at all times. This 
 * saves us from the overhead of bringing up and tearing down a socket for each 
 * and every keystroke. This class will be created and run in a new thread.
 * 
 * The main responsibility of this class is to sit and listen for commands from 
 * the IDE and create events in the database that represent these commands. These 
 * commands come in the form of JSON objects. The IDEServer parses the request to 
 * figure out what the IDE is asking to be done. Here are the main responsibilities:
 * - initial handshake 
 * - new project creation and existing project opening
 * - text related events (record inserts and deletes)
 * - document and directory operations (create, rename, move, delete)
 * - new node creation and moving to another node in the history (sends the new 
 *   state back to the IDE and updates its own state)
 * - developer/developer group creation and retrieval
 * 
 * One additional thing this server does is to keep track of the state of the IDE. 
 * The server needs this state in order to generate some events. For example, the 
 * server needs to know the ids of the previous neighbors for new text events and 
 * the ids of events that are being deleted. The server uses a map of IDESession 
 * objects to track this state. An IDESession keeps track of the current set of 
 * documents and directories in the IDE at a point in time. The state of the 
 * documents, stored in DocumentBuffers, hold the text in a document along with 
 * the storyteller ids of those events. When the IDE requests that some text is 
 * added or removed the document state is updated to reflect that.
 */
public class IDEServer implements Runnable
{	
	//the port that the server proxy listens in on
	public static final int IDE_PROXY_PORT = 5555;
		
	//reference to the storyteller core handle inter-server communication
	private StorytellerCore core;

	//dictates if this server should be listening to the IDE
	private boolean shouldAttemptToConnectToAnIDE = true;
	
	//indicates whether the server is expecting heart beat messages from the IDE
	private boolean isHeartBeat = false;

	//used to wait for an IDE to connect to the server
	private ServerSocket welcomeSocket = null;

	//one IDE session object for each project keyed by the ide session id. Most 
	//requests from the IDE come with an IDE session id. 
	private final Map<String, IDESession> currentIDESessions = new HashMap<String, IDESession>();
	
	public IDEServer(StorytellerCore c)
	{
		//store a reference to the core object for use by this server
		setCore(c);
	}
	
	/**
	 * Handles all communication with an IDE. 
	 * 
	 * The thread will wait for an IDE to connect with it. Then it will perform 
	 * a handshake to make sure that the IDE/server can communicate with each 
	 * other and whether we are creating a new project or opening an existing 
	 * one. After the handshake, the server sits and listens for requests from 
	 * the IDE and handles them. 
	 */
	@Override
	public void run() 
	{
		//used for communicating through a socket to the IDE
		BufferedReader inFromIDE  = null;
		DataOutputStream outToIDE = null;

		//the non-default JSON ack return object
		JSONObject ackObject = null;

		try
		{			
			//holds a string of text from the IDE
			String jsonRequestString = null;
			//object that holds the key/value pairs from the IDE
			JSONObject jsonRequestObject = null;	
			
			//default ack and nack json objects
			JSONObject defaultAckObject = null;
			defaultAckObject = new JSONObject();
			defaultAckObject.put(ACKNOWLEDGMENT_VALUE, DEFAULT_ACK_MESSAGE);

			JSONObject defaultNackObject = null;
			defaultNackObject = new JSONObject();
			defaultNackObject.put(ACKNOWLEDGMENT_VALUE, DEFAULT_NACK_MESSAGE);
					
			//while the IDEServer should be listening and is available to run
			while(shouldAttemptToConnectToAnIDE)
			{
				//this is used to communicate with the IDE
				Socket ideSocket = null;
				
				//create a server socket to listen for IDE requests
				welcomeSocket = new ServerSocket();
				
				//This is synchronized so that quitting does not happen between creating a 
				//server socket and binding
				synchronized (welcomeSocket)
				{
					//allows us to bind, close, rebind to this port faster than the 2 minute timeout allows
					welcomeSocket.setReuseAddress(true);	
					
					//TODO look into this
					//Occasionally, there are errors on close due to the synchronized thing not quite working
					if(!welcomeSocket.isClosed())
					{
						welcomeSocket.bind(new InetSocketAddress(IDE_PROXY_PORT));
					}
				}

				//there is a chance that quit() was called, if so we will kill the server
				if(!shouldAttemptToConnectToAnIDE)
				{
					break;			
				}
				
				//Wait for an IDE to make a connection with the server- BLOCKING CALL!!
				ideSocket = welcomeSocket.accept();

				//get the socket ready for two-way communication
				outToIDE = new DataOutputStream(ideSocket.getOutputStream());
				inFromIDE = new BufferedReader(new InputStreamReader(ideSocket.getInputStream()));
				
				//now real requests can flow
				while(shouldAttemptToConnectToAnIDE)
				{
					//Attempt to read in a command from the IDE
					try
					{
						jsonRequestString = inFromIDE.readLine();
					}
					//If we haven't heard from them in the allotted time or there
					//is an exception otherwise, assume the plugin is dead
					catch(IOException e)
					{				
						//TODO tell the core?? do something else??
						//Kill the server
						closeProject();
						outToIDE.close();
						welcomeSocket.close();
						e.printStackTrace();
						break;
					}
					
					//set the default ack object to a bad response (some responses will require a 
					//custom ACK message and this will be overwritten)
					ackObject = defaultNackObject;
					
					//if the json is null something bad happened
					if(jsonRequestString == null)
					{
						throw new IDEServerException("received a null string when expecting JSON for a command from the IDE");
					}

					//create a JSON object from the text
					jsonRequestObject = new JSONObject(jsonRequestString);

					//debug- print out the JSON string
					//System.out.println("RX:\n" + jsonRequestString.replaceAll(",", ",\n"));
					System.out.println("RX: " + jsonRequestString);

					//get the data members that are a part of every IDE request
					String type = jsonRequestObject.getString(TYPE);
					String timestamp = jsonRequestObject.getString(TIMESTAMP);
					
					//reference to the ideSession that we will be sending this 
					//request to 
					IDESession ideSession = null;

					//all requests that have a session id are handled here (some 
					//requests do not have a session id like create project, open project, etc.)
					if(jsonRequestObject.has(SESSION_ID))
					{
						//get the IDE Session object associated with the IDE 
						//that made the request 
						ideSession = currentIDESessions.get(jsonRequestObject.getString(SESSION_ID));
						
						//if there was a session id in the request that we are tracking
						if(ideSession != null)
						{
							//else if the event is an insert event
							if(type.equals(InsertEvent.INSERT_EVENT_TYPE))
							{
								//get the text of the insert event
								//We want to use optString because if an integer is passed in 
								//it will automatically convert it to its string value instead 
								//of throwing an exception.
								String eventData = jsonRequestObject.optString(EVENT_DATA);

								//get the index of the new text event
								int index = jsonRequestObject.getInt(INDEX);
														
								//get the document id where this new insert is being added
								String documentPath = jsonRequestObject.getString(DOCUMENT_PATH);

								//assume there is no paste parent id
								String pasteParentId = "";
								
								//if the insert is a pasted event
								if(jsonRequestObject.has(PASTE_PARENT_ID))
								{
									//get the parent event info
									pasteParentId = jsonRequestObject.getString(PASTE_PARENT_ID);
								}

								//insert the text event into the system
								ideSession.receiveInsertEvent(index, eventData, timestamp, documentPath, pasteParentId);
								
								//indicate success
								ackObject = defaultAckObject;						
							}
							else if(type.equals(DeleteEvent.DELETE_EVENT_TYPE))
							{
								//get the index of the text event that is being deleted
								int index = jsonRequestObject.getInt(INDEX);
								
								//get the document id from which we will be deleting the event
								String documentPath = jsonRequestObject.getString(DOCUMENT_PATH);
								
								//handle deleting the event in the system
								ideSession.receiveDeleteEvent(index, timestamp, documentPath);
								
								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(CreateDocumentEvent.CREATE_DOCUMENT_EVENT_TYPE))
							{
								//get the create doc info
								String documentNewName = jsonRequestObject.getString(NEW_NAME);
								String documentPath = jsonRequestObject.getString(DOCUMENT_PATH);
								String parentDirectoryPath = jsonRequestObject.getString(PARENT_DIRECTORY_PATH);
								
								//create the new document in the system
								ideSession.receiveCreateDocumentEvent(documentNewName, timestamp, documentPath, parentDirectoryPath);

								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(RenameDocumentEvent.RENAME_DOCUMENT_EVENT_TYPE))
							{
								//get the rename doc info 
								String documentOldName = jsonRequestObject.getString(OLD_NAME);
								String documentNewName = jsonRequestObject.getString(NEW_NAME);
								String documentOldPath = jsonRequestObject.getString(OLD_DOCUMENT_PATH);
								String documentNewPath = jsonRequestObject.getString(NEW_DOCUMENT_PATH);
								String documentParentOldPath = jsonRequestObject.getString(OLD_PARENT_DIRECTORY_PATH);
								String documentParentNewPath = jsonRequestObject.getString(NEW_PARENT_DIRECTORY_PATH);
								
								//handle the rename in the system
								ideSession.receiveRenameDocumentEvent(documentOldName, documentNewName, timestamp, documentOldPath, documentNewPath, documentParentOldPath, documentParentNewPath);
								
								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(MoveDocumentEvent.MOVE_DOCUMENT_EVENT_TYPE))
							{
								//get the move doc info
								String documentOldPath = jsonRequestObject.getString(OLD_DOCUMENT_PATH);
								String documentNewPath = jsonRequestObject.getString(NEW_DOCUMENT_PATH);
								String documentParentOldPath = jsonRequestObject.getString(OLD_PARENT_DIRECTORY_PATH);
								String documentParentNewPath = jsonRequestObject.getString(NEW_PARENT_DIRECTORY_PATH);

								//handle the move event in the system
								ideSession.receiveMoveDocumentEvent(timestamp, documentOldPath, documentNewPath, documentParentOldPath, documentParentNewPath);
								
								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(DeleteDocumentEvent.DELETE_DOCUMENT_EVENT_TYPE))
							{
								//get the delete doc info
								String thisDocumentPath = jsonRequestObject.getString(DOCUMENT_PATH);
								String parentDirectoryPath = jsonRequestObject.getString(PARENT_DIRECTORY_PATH);
								String documentOldName = jsonRequestObject.getString(OLD_NAME);
								
								//handle the delete doc event in the system
								ideSession.receiveDeleteDocumentEvent(timestamp, thisDocumentPath, parentDirectoryPath, documentOldName);

								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(CreateDirectoryEvent.CREATE_DIRECTORY_EVENT_TYPE))
							{
								//get the create dir info
								String directoryNewName = jsonRequestObject.getString(NEW_NAME);
								String thisDirectoryPath = jsonRequestObject.getString(DIRECTORY_PATH);
								String parentDirectoryPath = jsonRequestObject.getString(PARENT_DIRECTORY_PATH);
								
								//handle the create dir event in the system
								ideSession.receiveCreateDirectoryEvent(directoryNewName, timestamp, thisDirectoryPath, parentDirectoryPath);
								
								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(RenameDirectoryEvent.RENAME_DIRECTORY_EVENT_TYPE))
							{
								//get the rename dir info
								String directoryOldName = jsonRequestObject.getString(OLD_NAME);
								String directoryNewName = jsonRequestObject.getString(NEW_NAME);
								String directoryOldPath = jsonRequestObject.getString(OLD_DIRECTORY_PATH);
								String directoryNewPath = jsonRequestObject.getString(NEW_DIRECTORY_PATH);
								String directoryParentOldPath = jsonRequestObject.getString(OLD_PARENT_DIRECTORY_PATH);
								String directoryParentNewPath = jsonRequestObject.getString(NEW_PARENT_DIRECTORY_PATH);
								
								//handle renaming a dir in the system
								ideSession.receiveRenameDirectoryEvent(directoryOldName, directoryNewName, timestamp, directoryOldPath, directoryNewPath, directoryParentOldPath, directoryParentNewPath);
								
								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(MoveDirectoryEvent.MOVE_DIRECTORY_EVENT_TYPE))
							{
								//get the move dir info
								String directoryOldPath = jsonRequestObject.getString(OLD_DIRECTORY_PATH);
								String directoryNewPath = jsonRequestObject.getString(NEW_DIRECTORY_PATH);
								String directoryParentOldPath = jsonRequestObject.getString(OLD_PARENT_DIRECTORY_PATH);
								String directoryParentNewPath = jsonRequestObject.getString(NEW_PARENT_DIRECTORY_PATH);

								//handle moving a directory in the system
								ideSession.receiveMoveDirectoryEvent(timestamp, directoryOldPath, directoryNewPath, directoryParentOldPath, directoryParentNewPath);

								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(DeleteDirectoryEvent.DELETE_DIRECTORY_EVENT_TYPE))
							{
								//get the delete dir info
								String thisDirectoryPath = jsonRequestObject.getString(DIRECTORY_PATH);
								String parentDirectoryPath = jsonRequestObject.getString(PARENT_DIRECTORY_PATH);
								String directoryOldName = jsonRequestObject.getString(OLD_NAME);
								
								//handle the delete dir in the system
								ideSession.receiveDeleteDirectoryEvent(timestamp, thisDirectoryPath, parentDirectoryPath, directoryOldName);

								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(IS_PROJECT_BEING_TRACKED))
							{		
								//if we get here the project is being tracked 
								//because there is a session associated with the 
								//passed in session id
								
								//create a JSON object with the value true
								ackObject = new JSONObject();
								ackObject.put(Constants.IS_PROJECT_BEING_TRACKED, "true");
							}
							else if(type.equals(STOP_RECORDING_AND_DELETE_DATABASE))
							{		
								//close the db before removing
								getCore().closeAndDeleteDatabase(ideSession.getDatabase());
														
								//remove the ide session since we won't be using it anymore
								currentIDESessions.remove(jsonRequestObject.getString(SESSION_ID));
								
								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(GET_STATE_OF_FILE_SYSTEM))
							{ 
								//get the node id that we'd like to move to
								String nodeId = jsonRequestObject.getString(NODE_ID);
								
								//get the sequence number in the node if there is one
								int nodeSeqNum = -1;
								if(jsonRequestObject.has(NODE_SEQUENCE_NUMBER))
								{
									nodeSeqNum = jsonRequestObject.getInt(NODE_SEQUENCE_NUMBER);
								}
								
								//create a playback session
								PlaybackSession playbackSession = new PlaybackSession(ideSession.getDatabase(), ideSession.getLoggedInDevGroupId(), nodeId, nodeSeqNum);
								
								//get a directory view of the playback session
								ExtendedDirectory rootDir = playbackSession.getTheStateOfTheFileSystem(true, false);

								//turn the root dir into a JSON object
								ackObject = JSONiffy.toJSON(rootDir);
							}
							else if(type.equals(COMMIT_NOTIFICATION))
							{				
								//get the name and description for the new node that will be created 
								String nameOfNewNode = jsonRequestObject.getString(NEW_NAME);
								String descriptionOfNewNode = jsonRequestObject.getString(NODE_DESCRIPTION);
								
								//commit to the server and update the state of the IDE (the null indicates
								//we would like to close the current open node and create a new node directly
								//underneath it)
								ideSession.closeCurrentOpenNodeAndCreateANewOpenNode(null, timestamp, nameOfNewNode, descriptionOfNewNode);
								
								//no need to send data back to the IDE
								ackObject = defaultAckObject;
							}
							else if(type.equals(MOVE_TO_ANOTHER_NODE)) 
							{
								//get the node id to move to and info about the new node
								String destinationNodeId = jsonRequestObject.getString(NODE_ID);
								String nameOfNewNode = jsonRequestObject.getString(NEW_NAME);
								String descriptionOfNewNode = jsonRequestObject.getString(NODE_DESCRIPTION);
								
								//ask the server to create a new node under an existing one
								JSONObject stateOfFiles = ideSession.closeCurrentOpenNodeAndCreateANewOpenNode(destinationNodeId, timestamp, nameOfNewNode, descriptionOfNewNode);
								
								//return the state of the file system
								ackObject = new JSONObject();
								
								//this happens if the user accidentally asks to move to the current open node
								if(stateOfFiles == null)
								{
									ackObject = defaultAckObject;
								}
								else //the state of the IDE has changed, let it know about the new state of the files
								{
									ackObject.put(Constants.NEW_STATE_OF_FILES, stateOfFiles);
								}
							}
							else if(type.equals(GET_FILE_DATA_FOR_IDE_SESSION))
							{
				                //returns the state of the directories/documents in an IDE session
								ackObject = ideSession.getFileDataFromIDESession();
							}
							else if(type.equals(SELECTED_TEXT_FOR_PLAYBACK_TYPE))
							{
								//get the info about the selected text in the IDE
								String thisDocumentPath = jsonRequestObject.getString(DOCUMENT_PATH);					
								int index = jsonRequestObject.getInt(INDEX);
								int length = jsonRequestObject.getInt(LENGTH);
								
								//send the relevant events to a new playback session and we will return the id
								//of that session back to a new browser
								String playbackSessionId = receiveSelectedTextFromIDEForPlayback(ideSession, timestamp, ideSession.getLoggedInDevGroupId(), thisDocumentPath, index, length);
								
								//create a JSON object with the id of the new playback session and send it to the IDE
								ackObject = new JSONObject();
								ackObject.put(Constants.SESSION_ID, playbackSessionId);
							}					
							else if(type.equals(REQUEST_FOR_PLAYBACK_TYPE))
							{						
								String playbackSessionId = receiveRequestForPlayback(ideSession);
								
								//create a JSON object with the id of the new playback session and send it to the IDE
								ackObject = new JSONObject();
								ackObject.put(Constants.SESSION_ID, playbackSessionId);
							}
							else if(type.equals(FIND_STORYBOARDS_FROM_SELECTED_TEXT_TYPE))
							{
								//get the info about the selected text in the IDE
								String thisDocumentPath = jsonRequestObject.getString(DOCUMENT_PATH);	 
								int index = jsonRequestObject.getInt(INDEX);
								int length = jsonRequestObject.getInt(LENGTH);

								//send a message to the server to find the storyboards with one or more 
								//of the selected events in them. These storyboards will be contained
								//in the playback session object on the server. The browser will extract
								//these storyboard titles from the session and give the user the option to 
								//choose one to playback
								String playbackSessionId = receiveSelectedTextFromIDEForFindingStoryboards(ideSession, timestamp, ideSession.getLoggedInDevGroupId(), thisDocumentPath, index, length);
								
								//create a JSON object with the id of the new playback session and send it to the IDE
								ackObject = new JSONObject();
								ackObject.put(Constants.SESSION_ID, playbackSessionId);
							}
							else if(type.equals(GET_SELECTED_TEXT_FOR_CUT_OR_COPY))
							{
								//get the info about the selected text in the IDE
								String thisDocumentPath = jsonRequestObject.getString(DOCUMENT_PATH);	 
								int index = jsonRequestObject.getInt(INDEX);
								int length = jsonRequestObject.getInt(LENGTH);
								
								//get the ids of the selected text events
								List  < String > idsOfSelectedTextCutOrCopied = ideSession.getIdsOfSelectedText(thisDocumentPath, index, length);
								
								//array that holds the ids of selected events
								JSONArray selectedIds = new JSONArray();
								
								//get each selected text id
								for(String selectedEventId : idsOfSelectedTextCutOrCopied)
								{
									//create a json object to represent a pasted event
									JSONObject selectedEvent = new JSONObject();				
									
									//set the id
									selectedEvent.put(EVENT_ID, selectedEventId);
									
									//add id to the array
									selectedIds.put(selectedEvent);
								}

								//get the selected text
								String selectedText = ideSession.getSelectedText(thisDocumentPath, index, length);
								
								//create a JSON object with the ids of the selected text and the text itself
								ackObject = new JSONObject();
								ackObject.put(Constants.SELECTED_TEXT_IDS, selectedIds);
								ackObject.put(Constants.SELECTED_TEXT, selectedText);
							}
							else if(type.equals(LOGIN_AS_DEVELOPER_GROUP))
							{	
								//response object
								JSONObject retObject = new JSONObject();

								//if the user has passed in a group of email addresses
								if(jsonRequestObject.has(DEVELOPERS))
								{
									//a list of email addresses for the developers we want to work together
									JSONArray devsJson = jsonRequestObject.getJSONArray(DEVELOPERS);
									
									//all dev info:
									// email => (FIRST_NAME => "Mark", LAST_NAME => "Mahoney", EMAIL => "mmahoney@carthage.edu")
									Map < String, Map < String, String > > allDevInfo = new HashMap < String, Map < String, String > >();

									//for each dev we want to log in
									for(int i = 0;i < devsJson.length();i++)
									{
										//get a single json object
										JSONObject jsonDev = devsJson.getJSONObject(i);
										
										//create a map of dev info keyed on fields
										Map < String, String > devInfo = new HashMap < String, String >();
											
										//create the dev map
										devInfo.put(FIRST_NAME, jsonDev.getString(FIRST_NAME));
										devInfo.put(LAST_NAME, jsonDev.getString(LAST_NAME));
										devInfo.put(EMAIL, jsonDev.getString(EMAIL));
										
										//add the dev map to the map of all devs keyed on email address
										allDevInfo.put(jsonDev.getString(EMAIL), devInfo);
									}
									
									String newDevGroupId = null;
									
									//for each of the ide sessions
									for(IDESession sessionToAddDevTo : currentIDESessions.values())
									{
										//login the developers, this will either:
										//- find a developer group with all the dev's passed in
										//- create a new developer group with all the dev's passed in
										sessionToAddDevTo.logInDevelopers(allDevInfo);
										
										//all dev groups with the same devs in them have the same dev group id
										newDevGroupId = sessionToAddDevTo.getLoggedInDevGroupId();
									}
									
									//put the dev group id in the response object
									ackObject = retObject.put(DEVELOPER_GROUP_ID, newDevGroupId);							
								}
							}
							else if(type.equals(UPDATE_DEVELOPER_INFO)) 
							{
								//get the developer info from the JSON object
								String lastName = jsonRequestObject.getString(LAST_NAME);
								String firstName = jsonRequestObject.getString(FIRST_NAME);
								String email = jsonRequestObject.getString(EMAIL);
								
								//for each of the ide sessions
								for(IDESession sessionToUpdate : currentIDESessions.values())
								{
									//update the dev (no session info necessary because this is an update)
									sessionToUpdate.getDatabase().updateDeveloper(firstName, lastName, email);
								}
								
								//indicate success
								ackObject = defaultAckObject;
							}
							else if(type.equals(ADD_NEW_DEVELOPER)) 
							{
								//data about the new dev
								String lastName = jsonRequestObject.getString(LAST_NAME);
								String firstName = jsonRequestObject.getString(FIRST_NAME);
								String email = jsonRequestObject.getString(EMAIL);
								
								//get the creation date
								Date creationDate = new Date(Long.valueOf(timestamp));

								DeveloperGroup devGroup = null;
														
								//go through all the IDE sessions and add the developer to each db
								for(IDESession sessionToAddDevTo : currentIDESessions.values())
								{
									//add the new developer to the database and create an initial developer group
									devGroup = sessionToAddDevTo.getDatabase().addADeveloperAndCreateANewDeveloperGroup(creationDate, firstName, lastName, email, sessionToAddDevTo.getCurrentNode().getId(), sessionToAddDevTo.getLoggedInDevGroupId());
								}
								
								JSONObject response = new JSONObject();
								
								//the dev group id is the md5 hash of all the dev's email addresses
								response.put(Constants.DEVELOPER_GROUP_ID, devGroup.getId());

								//indicate success
								ackObject = response;
							}
							else if(type.equals(GET_ALL_DEVELOPERS)) 
							{
								//a set of unique developers
								Set < Developer > allDevsList = new HashSet < Developer >();

								//go through all the IDE sessions and add the developer to each db
								for(IDESession sessionToGetDevInfo : currentIDESessions.values())
								{
									//ask the server for a json array of all devs 
									allDevsList.addAll(sessionToGetDevInfo.getDatabase().getAllDevelopers());
								}
								
								JSONArray allDevs = new JSONArray();
								
								//convert the dev info to json
								for (Developer dev : allDevsList)
								{
									//create a json object for a developer
									JSONObject devJSON = new JSONObject();
									
									//set its data members
									devJSON.put(Constants.EMAIL, dev.getEmail());
									devJSON.put(Constants.FIRST_NAME, dev.getFirstName());
									devJSON.put(Constants.LAST_NAME, dev.getLastName());							
									
									allDevs.put(devJSON);
								}
								
								//wrap up the contents into a JSON object
								ackObject = new JSONObject();
								ackObject.put(LIST_OF_DEVELOPERS, allDevs);
							}
							else if(type.equals(GET_DEVELOPERS_IN_DEV_GROUP)) 
							{
								//get the dev group id that the user is interested in
								String devGroupId = jsonRequestObject.getString(DEVELOPER_GROUP_ID);

								List <Developer> devInGroup = null;
								
								//get the first session (since all dev info should be the same)
								for(IDESession sessionToSearch : currentIDESessions.values())
								{
									//ask the server for a json array of all devs in the group 
									devInGroup = sessionToSearch.getDatabase().getDevelopersInADeveloperGroup(devGroupId);
									
									//all the dev info should be the same so we should be able 
									//to stop after the first one
									break;
								}
								
								JSONArray allDevs = new JSONArray();
								
								//for each dev in the group
								for (Developer dev : devInGroup)
								{
									//create a json object for the developer
									JSONObject devJSON = new JSONObject();
									
									//set the data members for the developer in the group
									devJSON.put(Constants.EMAIL, dev.getEmail());
									devJSON.put(Constants.FIRST_NAME, dev.getFirstName());
									devJSON.put(Constants.LAST_NAME, dev.getLastName());
									
									allDevs.put(devJSON);
								}
								
								//wrap up the contents into a JSON object
								ackObject = new JSONObject();
								ackObject.put(LIST_OF_DEVELOPERS, allDevs);
							}
							else if(type.equals(GET_ALL_NODES)) 
							{
								//get all the nodes in the database
								List<Node> nodes = ideSession.getDatabase().getAllNodes();
								
								//add them to a json array
								JSONArray allNodes = JSONiffy.toJSON(nodes);
								
								//wrap up the contents into a JSON object
								ackObject = new JSONObject();
								ackObject.put(NODE_TREE, allNodes);
							}
							else if(type.equals(Constants.HEARTBEAT))
							{
								//if heart beat monitoring is turned on then every once in a while the 
								//IDE server will receive a heart beat message from the IDE. There is 
								//nothing to do in this case since the socket will die if it doesn't
								//receive anything from the IDE every once in a while 
								ackObject = defaultAckObject;
							}
							else if(type.equals(Constants.START_HEARTBEAT))
							{
								//start the heart beat monitoring
								toggleHeartbeat(true, ideSocket);
								
								ackObject = defaultAckObject;
							}
							else if(type.equals(Constants.STOP_HEARTBEAT))
							{
								//stop the heart beat monitoring
								toggleHeartbeat(false, ideSocket);
								
								ackObject = defaultAckObject;
							}
							else //an unacceptable command
							{
								throw new IDEServerException("IDE Server did not recognize the command: " + type);
							}		
						}
						else //there was a session id but we are not tracking that session
						{
							//if the request was to find out if a specific project was being tracked
							if(type.equals(IS_PROJECT_BEING_TRACKED))
							{		
								//there was no session with the passed in session id
								
								//create a JSON object with the value false
								ackObject = new JSONObject();
								ackObject.put(Constants.IS_PROJECT_BEING_TRACKED, "false");
							}
							else //just a plain old project we are not tracking
							{
								System.out.println("We are not tracking the session with id: " + jsonRequestObject.getString(SESSION_ID));
								ackObject = defaultAckObject;
							}
						}
					}
					else //there was no session id in the request- this happens 
						 //for requests outside the context of an existing session
					{
						//the IDE is creating a brand new project
						if(type.equals(CREATE_NEW_PROJECT))
						{
							//path to where the db file will live
							String pathToProject = jsonRequestObject.getString(PATH_TO_PROJECT);
							
							//The project name that should be opened or created
							String projectName = jsonRequestObject.getString(PROJECT_NAME);

							//get the logged in dev info
							JSONArray devs = jsonRequestObject.getJSONArray(DEVELOPERS);
							Map<String, Map<String, String>> allDevInfo = getDevelopersInfo(devs);
										
							//create a new project 
							ackObject = createProject(pathToProject, projectName, allDevInfo, timestamp);						
						}
						//the IDE is asking to open an existing project
						else if(type.equals(OPEN_PROJECT)) 
						{
							//path to the db file
							String pathToProject = jsonRequestObject.getString(PATH_TO_PROJECT);
	
							//The project name that should be opened or created
							String projectName = jsonRequestObject.getString(PROJECT_NAME);
	
							//get the logged in dev info
							JSONArray devs = jsonRequestObject.getJSONArray(DEVELOPERS);
							Map<String, Map<String, String>> allDevInfo = getDevelopersInfo(devs);
							
							//information to send to the IDE about the opening of this project
							JSONObject jsonResponseToIde = new JSONObject();
	
							//open an existing project and get a session id
							String sessionId = openProject(pathToProject, projectName, allDevInfo);
							
							//store the IDE session id
							jsonResponseToIde.put(SESSION_ID, sessionId);
							
							ackObject = jsonResponseToIde;
						}
						else if(type.equals(CLOSE_IDE))			
						{
							//close the project and disconnect from IDE.   
							closeProject();
							
							try 
							{
								//the ack here is useful for the tests, real IDE's can ignore the return value
								ackObject = new JSONObject();
								ackObject.put(CLOSE_IDE, DEFAULT_ACK_MESSAGE);	
								
								//we break out of the loop below so we need to send the ack message here
								outToIDE.writeBytes(ackObject.toString() + "\n");
								
								//Close the old proxy
								outToIDE.close();
								
								//shut down the socket so we can create a new one in the future.
								welcomeSocket.close();
							} 
							catch (IOException e) 
							{
								e.printStackTrace();
							}
							
							break;
						}					

					}
					
					//write an ack message back to the client
					outToIDE.writeBytes(ackObject.toString() + "\n");					
				}
			}
		}
		catch(Exception ex)
		{
			try
			{
				//TODO clean this up a bit
				if(shouldAttemptToConnectToAnIDE && ackObject != null && !ackObject.toString().isEmpty())
				{
					//the loop above is "infinite", so if we ever get here and we should be listening, its 
					//because an exception happened in the loop and brought us out here
					outToIDE.writeBytes(ackObject.toString() + "\n");
				}		
				
				//free up resources on error
				outToIDE.close();
				welcomeSocket.close();
			}
			catch(IOException ex2)
			{
				ex2.printStackTrace();
			}
			ex.printStackTrace();
		}
	}

	/**
	 * Converts a JSON array of developer info into a map of developer info.
	 * 
	 * JSON: [{FIRST_NAME:"mark", LAST_NAME:"mahoney", EMAIL:"mmahoney@carthage.edu"},
	 *        {FIRST_NAME:"laura", LAST_NAME:"mahoney", EMAIL:"laura@mail.com"},
	 *        {FIRST_NAME:"buddy", LAST_NAME:"mahoney", EMAIL:"buddy@mail.com"},
	 *        {FIRST_NAME:"patrick", LAST_NAME:"mahoney", EMAIL:"patrick@mail.com"}]
	 *        
	 * Map: mmahoney@carthage.edu => {FIRST_NAME => "mark", LAST_NAME => "mahoney", EMAIL => "mmahoney@carthage.edu"},
	 *      laura@mail.com => {FIRST_NAME => "laura", LAST_NAME => "mahoney", EMAIL => "laura@mail.com"},
	 *      buddy@mail.com => {FIRST_NAME => "buddy", LAST_NAME => "mahoney", EMAIL => "buddy@mail.com"},
	 *      patrick@mail.com => {FIRST_NAME => "patrick", LAST_NAME => "mahoney", EMAIL => "patrick@mail.com"}      
	 */
	private Map<String, Map<String, String>> getDevelopersInfo(JSONArray devs) throws JSONException
	{
		//a map of all the requested devs' info
		Map < String, Map < String, String > > allDevInfo = new HashMap < String, Map < String, String > >();

		//go through all the logged in devs
		for(int i = 0;i < devs.length();i++)
		{
			//get the json object
			JSONObject dev = devs.getJSONObject(i);
			
			//holds a map of dev info
			Map < String, String > devInfo = new HashMap < String, String >();
			
			//add the dev's info to a map
			devInfo.put(FIRST_NAME, dev.getString(FIRST_NAME));
			devInfo.put(LAST_NAME, dev.getString(LAST_NAME));
			devInfo.put(EMAIL, dev.getString(EMAIL));
			
			//collect all the devs' info
			allDevInfo.put(dev.getString(EMAIL), devInfo);
		}
		return allDevInfo;
	}

	//project related methods
	/**
	 * Create a new project with the passed in file name. 
	 */
	private JSONObject createProject(String pathToProject, String projectName, Map < String, Map < String, String > > allDevInfo, String timestamp) throws DBAbstractionException, IDEServerException, JSONException
	{
		//create the path to the db file
		String pathToDbFile = pathToProject + File.separator + projectName + DATABASE_FILE_EXTENSION;
		
		//get a handle to the file that we would like to create
		File dbFile = new File(pathToDbFile);

		//if a file with that path already exists then we can't create a new database
		if(dbFile.exists())
		{
			//indicate the error
			throw new IDEServerException("A database file with that name already exists.");
		}

		//ask the core to create a new database abstraction (this should create 
		//a brand new file) and store it in the core object
		DBAbstraction db = getCore().createDatabaseAbstraction(pathToDbFile);

		//create an IDE session object to track the state of the ide
		IDESession ideSession = new IDESession(db, allDevInfo);
		
		//set the session id to be the path of the project
		ideSession.setSessionId(dbFile.getParentFile().getAbsolutePath());
		
		//add it to the map of IDE sessions
		currentIDESessions.put(ideSession.getSessionId(), ideSession);
		
		//TODO
		//TODO move most of this into a IDESession constructor
		//get the creation date
		Date creationDate = new Date(Long.parseLong(timestamp));

		//create a project which holds the name
		Project newProject = new Project(creationDate, ideSession.getLoggedInDevGroupId(), projectName);

		//create the first node in the project (in memory right now).
		Node startingNode = new Node(creationDate, null, ideSession.getLoggedInDevGroupId(), "root", "The beginning node in " + projectName, newProject.getId(), 1, Node.OPEN_NODE);

		//insert the project and the node into the db
		ideSession.getDatabase().insertProject(newProject);
		ideSession.getDatabase().insertNode(startingNode);

		//update the IDESession information.
		ideSession.setCurrentNode(startingNode);
		ideSession.setProject(newProject);
		ideSession.setSequenceNumber(0); //set the initial sequence number for this new node to 0
		ideSession.setSequentiallyBeforeIdOfLastEvent(null);

		//add the first events manually. All other events explicitly come from the IDE
		OpenNodeEvent newOpenNodeEvent = new OpenNodeEvent(creationDate, ideSession.getCurrentNode().getId(), ideSession.getLoggedInDevGroupId(), ideSession.getSequenceNumber(), ideSession.getSequentiallyBeforeIdOfLastEvent(), null);
		ideSession.getDatabase().insertEvent(newOpenNodeEvent);
		ideSession.setSequenceNumber(1);
		ideSession.setSequentiallyBeforeIdOfLastEvent(newOpenNodeEvent.getId());

		//create the root directory and add it to the db (all root dir's have the same name- ROOT_DIRECTORY)
		Directory rootDirectory = new Directory(ROOT_DIRECTORY, creationDate, startingNode.getId(), ideSession.getLoggedInDevGroupId(), null);
		ideSession.getDatabase().insertDirectory(rootDirectory);

		//TODO
		//receiveCreateDirectoryEvent(String directoryNewName, String timestamp, String dirPath, String parentDirectoryPath)
		
		//add the directory's creation event to the database
		CreateDirectoryEvent createRootDirEvent = new CreateDirectoryEvent(creationDate, ideSession.getCurrentNode().getId(), ideSession.getLoggedInDevGroupId(), ideSession.getSequenceNumber(), ideSession.getSequentiallyBeforeIdOfLastEvent(), rootDirectory.getId(), projectName, "");
		ideSession.getDatabase().insertEvent(createRootDirEvent);
		//handle the new directory event
		
		//get the relative path to the project
		String relativePathToProject = pathToProject.substring(pathToProject.lastIndexOf(File.separator));
		
		//update the session to handle the new root directory
		ideSession.renderCreateDirectoryEvent(relativePathToProject, createRootDirEvent);

		//these are the only events not created in response from the IDE so we manually increment
		//the sequence number for each event added
		ideSession.setSequenceNumber(2);

		//store the create event's id as the last event created
		ideSession.setSequentiallyBeforeIdOfLastEvent(createRootDirEvent.getId());

		//send back the dev group id, the session id???, the node id (current node), and the sequence number
		JSONObject responseToIDE = new JSONObject();
		responseToIDE.put(Constants.DEVELOPER_GROUP_ID, ideSession.getLoggedInDevGroupId());
		responseToIDE.put(Constants.SESSION_ID, ideSession.getSessionId());
		responseToIDE.put(Constants.NODE_ID, startingNode.getId());
		responseToIDE.put(Constants.NODE_SEQUENCE_NUMBER, ideSession.getSequenceNumber());
		
		return responseToIDE;
	}

	/**
	 * Opens an existing project.
	 */
	private String openProject(String pathToProject, String projectName, Map < String, Map < String, String > > allDevInfo) throws DBAbstractionException, IDEServerException
	{
		//create the path to the db file
		String pathToDbFile = pathToProject + File.separator + projectName + DATABASE_FILE_EXTENSION;

		//get a handle to the db file that we would like to open
		File dbFile = new File(pathToDbFile);

		//if the file doesn't exist, it can't be opened
		if(!dbFile.exists())
		{
			throw new IDEServerException("Cannot find the database file to open.");
		}

		//create a database abstraction to the existing db file and store it in the core object
		DBAbstraction db = getCore().createDatabaseAbstraction(pathToDbFile);

		//prepare a new IDE Session for the IDE's use
		IDESession ideSession = new IDESession(db, allDevInfo, projectName);

		//set the session id to be the path of the project
		ideSession.setSessionId(dbFile.getParentFile().getAbsolutePath());

		//add the IDE session to the map
		currentIDESessions.put(ideSession.getSessionId(), ideSession);

		//return the IDE session id
		return ideSession.getSessionId();
	}

	/**
	 * Closes an open project.
	 * 
	 * @throws DBAbstractionException
	 */
	private void closeProject() throws DBAbstractionException
	{
		//close each session's database
		for(IDESession ideSession : currentIDESessions.values())
		{
			ideSession.getDatabase().close();
		}
	}

	/**
	 * Delete a project.
	 * 
	 * TODO this be bad
	 * 
	 * @param pathToFile The name of the db file to delete
	 * @throws StorytellerServerException
	 */
	private void deleteProject(String pathToFile) throws IDEServerException
	{
		//get a handle to the db file
		File dbFile = new File(pathToFile);

		//make sure the file exists
		if(dbFile.exists())
		{
			//get rid of the db file
			dbFile.delete();
		}
		else //the file cannot be found
		{
			//indicate file not found with an exception
			throw new IDEServerException("Cannot find the database file to delete.");
		}
	}

	/**
	 * Tells the Server proxy to stop listening.
	 * 
	 * Any additional cleanup should go here
	 */
	private void quit() 
	{
		synchronized (welcomeSocket)
		{
			shouldAttemptToConnectToAnIDE = false;

			try 
			{
				//most likely, ideProxy is waiting for an accept(), so we need to interrupt it
				welcomeSocket.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	/** 
	 * toggles whether or not the heartbeat should exists
	 * 
	 * @param turnOn true to turn on, false to turn off
	 * @throws SocketException 
	 */
	private void toggleHeartbeat(boolean turnOn, Socket socket) throws SocketException
	{
		//if we are turning on heart beat monitoring
		if(turnOn)
		{
			//if it is off
			if(!isHeartBeat)
			{
				//turn it on
				isHeartBeat = true;
				
				//the socket will die if it doesn't receive anything within a timeout period
				socket.setSoTimeout(10000);
			}
		}
		else //we are turning off the heart beat
		{
			//if it is on
			if(isHeartBeat)
			{
				//turn it off
				isHeartBeat = false;
				
				//the socket will stay open indefinitely without receiving any messages
				socket.setSoTimeout(0);
			}
		}
	}
	
	private boolean getIsHeartbeat()
	{
		return isHeartBeat;
	}
	
	
	/**
	 * Finds all the storyboards that have some highlighted text in a storyboard 
	 */
	private String receiveSelectedTextFromIDEForFindingStoryboards(IDESession ideSession, String timestamp, String developerGroupId, String thisDocumentId, int index, int length)
	{
		//do something similar to receiveSelectedTextFromIDEForPlayback but instead of setting
		//up a playback, do a search for storyboards that have the selected text highlighted in 
		//a storyboard. Then display the list of storyboards
		
		return "FIX THIS!!!";
	}

	/**
	 * Asks the storyteller core to create a playback where the selected events 
	 * are marked as relevant 
	 */
	private String receiveSelectedTextFromIDEForPlayback(IDESession ideSession, String timestamp, String developerGroupId, String thisDocumentPath, int index, int length) throws DBAbstractionException
	{
		//get the ids of the selected events
		List < String > selectedEventIds = ideSession.getIdsOfSelectedText(thisDocumentPath, index, length);

		//have the core create a new playback session with the selected events marked as relevant
		return getCore().sendSelectedTextToPlaybackServer(ideSession.getDatabase(), selectedEventIds, ideSession.getCurrentNode().getId(), developerGroupId);
	}
	
	/**
	 * Asks the storyteller core to create a mostly empty playback 
	 */
	private String receiveRequestForPlayback(IDESession ideSession) throws DBAbstractionException
	{
		//have the core create a mostly empty playback
		return getCore().sendRequestForPlaybackToPlaybackServer(ideSession.getDatabase(), ideSession.getLoggedInDevGroupId());
	}
	
	private StorytellerCore getCore() 
	{
		return core;
	}
	private void setCore(StorytellerCore core) 
	{
		this.core = core;
	}
}