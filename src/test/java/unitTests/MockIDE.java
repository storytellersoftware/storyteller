//package unitTests;
//
//import static StorytellerServer.json.JSONConstants.*;
//
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.Socket;
//import java.security.InvalidParameterException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import StorytellerEntities.Events.CreateDirectoryEvent;
//import StorytellerEntities.Events.CreateDocumentEvent;
//import StorytellerEntities.Events.DeleteDirectoryEvent;
//import StorytellerEntities.Events.DeleteDocumentEvent;
//import StorytellerEntities.Events.DeleteEvent;
//import StorytellerEntities.Events.InsertEvent;
//import StorytellerEntities.Events.MoveDirectoryEvent;
//import StorytellerEntities.Events.MoveDocumentEvent;
//import StorytellerEntities.Events.RenameDirectoryEvent;
//import StorytellerEntities.Events.RenameDocumentEvent;
//import StorytellerServer.Utilities;
//import StorytellerServer.ide.IDEProxy;
//import StorytellerServer.json.JSONConstants;
//
///**
// * This class is used in unit tests to easily send many events to a listening
// * server. The key points of this class are the convenience methods available to
// * send vast amounts of text and then easily delete text from anywhere in any
// * created document
// * 
// * @author Kevin
// * 
// */
//public class MockIDE
//{
//	//for connecting to a socket
//
//	//ip address of server
//	protected String pathToServer;
//	private int ideProxyPort = IDEProxy.IDE_PROXY_PORT;
//
//	//for communicating with the server proxy
//	private Socket socketToIDEProxy;
//	private DataOutputStream outToIDEProxy;
//	private BufferedReader inFromIDEProxy;
//
//	//refers to the current directory and document ids
//	private String currentDirectoryId;
//	private String currentDocumentId;
//	
//	//the current project name
//	private String currentProjectName = null;
//
//	//the id of the current developer group that generates events
//	private String currentDevGroupID = null;
//
//	//this holds all the documents being worked on- key: doc id, val: group of text
//	protected Map<String, StringBuilder> documentBuffers;
//
//	//maps doc ids to the directories the docs are in
//	private Map<String, String> toParentDirectoryMap;
//
//	//list of all directory IDs in the system
//	private ArrayList<String> directoryIDs;
//
//
//	protected String sessionId = null;
//
//	private static final char unusedChar = Character.MAX_VALUE;
//
//	//for time faking
//	private boolean holdTime;
//	private Date heldTime;
//	private boolean humanDelay;
//	private boolean dateFaking;
//	private final SimpleDateFormat dateTimeParser = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
//
//	private Date lastDateCheckPoint;
//	private long checkPointStartMillis;
//
//	private Logger logger = Logger.getLogger(getClass().getName());
//	private Logger timer = Logger.getLogger("timing."+getClass().getName());
//
//	public MockIDE(String pathToServer)
//	{
//		documentBuffers = new HashMap<String, StringBuilder>();
//		toParentDirectoryMap = new HashMap<String, String>();
//
//		directoryIDs = new ArrayList<String>();
//
//
//		this.pathToServer = pathToServer;
//
//		humanDelay = false;
//		holdTime = false;
//		disableDateFaking();
//	}
//
//	public String getCurrentDevGroupID()
//	{
//		return currentDevGroupID;
//	}
//
//	public void setCurrentDevGroupID(String currentDevGroupID)
//	{
//		this.currentDevGroupID = currentDevGroupID;
//	}
//
//	public String getSessionId()
//	{
//		return sessionId;
//	}
//
//
//	public void setHumanTyping(boolean humanTyping)
//	{
//		humanDelay=humanTyping;
//	}
//
//	public void disableDateFaking()
//	{
//		dateFaking=false;
//		lastDateCheckPoint=null;
//		checkPointStartMillis=0;
//	}
//	/**
//	 * Basically sets the mockIDE's clock to whatever time you want.  All events after occur in "real" time
//	 * @param parsableDateToBeNow
//	 * @throws ParseException
//	 */
//	public void enableDateFaking(String parsableDateToBeNow) throws ParseException
//	{
//		dateFaking=true;
//		lastDateCheckPoint=this.dateTimeParser.parse(parsableDateToBeNow);
//		logger.debug("The parsable date: "+parsableDateToBeNow+" was parsed into "+lastDateCheckPoint);
//		checkPointStartMillis=System.currentTimeMillis();
//	}
//
//	public void enableDateFaking()
//	{
//		dateFaking=true;
//		lastDateCheckPoint = new Date();
//		checkPointStartMillis=System.currentTimeMillis();
//	}
//
//	/**
//	 * Freezes the clock at whatever time it currently is
//	 * @param b
//	 */
//	private void setHoldTime(boolean b)
//	{
//		if (!dateFaking)
//		{
//			throw new RuntimeException("You must enable date faking in order to use setHoldTime(boolean b).  Remember that cutAndPasteToServer uses setHoldTime(boolean b)");
//		}
//		heldTime = getCurrentTime();
//		holdTime = b;
//	}
//
//	public Date getCurrentTime()
//	{
//		if (!dateFaking)
//		{
//			return new Date();
//		}
//		if (holdTime)
//		{
//			return heldTime;
//		}
//		return new Date(lastDateCheckPoint.getTime()+(System.currentTimeMillis()-checkPointStartMillis));
//
//	}
//
//	/**
//	 * Artificially delays for some amount of milliseconds
//	 * @param millis
//	 */
//	public void delayTime(long millis)
//	{
//		if (!dateFaking)
//		{
//			throw new RuntimeException("You must enable date faking in order to use delayTime(long millis).  Remember that copyAndPasteToServer uses delayTime(long millis)");
//		}
//		checkPointStartMillis-=millis;
//	}
//
//	/**
//	 * Tells the ideProxy that we want to create a new project, using the developer's name and email and this ID for a rootDir
//	 * @param filePath
//	 * @param devFirstName
//	 * @param devLastName
//	 * @param devEmail
//	 * @param rootDirId
//	 * @throws JSONException
//	 * @throws IOException
//	 */
//	public void createNewProject(String filePath, String devFirstName, String devLastName, String devEmail, String rootDirId, String projectName) throws JSONException, IOException
//	{
//		createSocketToIDEProxy();
//		JSONObject commandObject = new JSONObject();
//		commandObject.put(TYPE, CREATE_NEW_PROJECT);
//
//		commandObject.put(FIRST_NAME, devFirstName);
//		commandObject.put(LAST_NAME, devLastName);
//		commandObject.put(EMAIL , devEmail);
//		commandObject.put(DIRECTORY_ID, rootDirId);
//		commandObject.put(PATH_TO_FILE, filePath);
//		commandObject.put(PROJECT_NAME, projectName);
//		commandObject.put(TIMESTAMP, String.valueOf(getCurrentTime().getTime()));
//
//		logger.debug("Creating project "+filePath);
//
//		outToIDEProxy.writeBytes(commandObject.toString()+"\n");
//
//		String jsonString = inFromIDEProxy.readLine();
//		JSONObject welcomePacket = new JSONObject(jsonString);
//
//		setCurrentDevGroupID(welcomePacket.getString(DEVELOPER_GROUP_ID)); //read in the devGroupID
//		sessionId = welcomePacket.getString(SESSION_ID);
//
//		directoryIDs.add(rootDirId); //the root has already been created in the creation of the project
//	}
//	
//	/**
//	 * A convenient way to use openProject with just one Developer to the selected node
//	 * @param filePath
//	 * @param useNodeIdAsOpen if null, assumes that there is just one node open, and that node will be opened
//	 * @param devFirstName
//	 * @param devLastName
//	 * @param devEmail
//	 */
//	public void openProject(String filePath, String projectName, String useNodeIdAsOpen, String developerFirstName, String developerLastName, String developerEmailAddress) throws JSONException, IOException
//	{
//		openProject(filePath, projectName, useNodeIdAsOpen, new String[]{developerFirstName}, new String[]{developerLastName}, new String[]{developerEmailAddress});
//
//	}
//	
//	/**
//	 * Opens a project from filePath with an JSONArray of developers (the JSONarray should be a list of the emails of each developer)
//	 * @param filePath
//	 * @param useNodeIdAsOpen  if null, assumes that there is just one node open, and that node will be opened
//	 * @param multipleDevelopers
//	 * @param developerFirstNames 
//	 * @param developerLastNames 
//	 * @param developerEmailAddresses 
//	 */
//	public void openProject(String filePath, String projectName, String useNodeIdAsOpen, String[] developerFirstNames, String[] developerLastNames, String[] developerEmailAddresses) throws IOException, JSONException
//	{		
//		createSocketToIDEProxy();
//		
//		JSONObject commandObject = new JSONObject();
//		commandObject.put(TYPE, OPEN_PROJECT);
//		commandObject.put(PATH_TO_FILE, filePath);
//		commandObject.put(PROJECT_NAME, projectName);
//		commandObject.put(TIMESTAMP, String.valueOf(getCurrentTime().getTime()));
//		commandObject.put(NODE_ID, useNodeIdAsOpen);
//		commandObject.put(DEVELOPER_GROUP_ID, currentDevGroupID);
//
//		outToIDEProxy.writeBytes(commandObject.toString()+"\n");
//
//		String jsonString = inFromIDEProxy.readLine();
//
//		logger.debug("Open project recieved: "+jsonString);
//		
//		JSONObject welcomePacket = new JSONObject(jsonString);		
//		sessionId = welcomePacket.getString(SESSION_ID);
//
//		//now ask for the state of the file system based on the current IDE session
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put(TYPE, GET_FILE_DATA_FOR_IDE_SESSION);
//		jsonObject.put(TIMESTAMP, String.valueOf((getCurrentTime()).getTime()));
//		jsonObject.put(DEVELOPER_GROUP_ID, getCurrentDevGroupID());
//		jsonObject.put(SESSION_ID, getSessionId());
//		
//		outToIDEProxy.writeBytes(jsonObject.toString()+"\n");
//		
//		//get the response
//		jsonString = inFromIDEProxy.readLine();
//		JSONObject responseWithFileData = new JSONObject(jsonString);
//		unpackageProjectStateFromServer(responseWithFileData);
//		
//		//Note: changed this part of the open project protocol
//		/*
//		if (useNodeIdAsOpen==null)		//means that there is only one open node
//		{
//			String input = inFromIDEProxy.readLine();
//			if (!input.equals("ACK"))
//			{
//				throw new RuntimeException("No ACK recieved, got a "+input);
//			}
//			//TODO manually recreate buffers
//		}
//		else
//		{
//
//			String input = inFromIDEProxy.readLine();			//this is the node tree (which we can psuedo ignore)
//			logger.debug("in openProject, recieved the following nodeTree:"+input);
//			if (input.equals("ACK"))
//			{
//				throw new RuntimeException("ACK recieved instead of node tree");
//			}
//			//give the selected nodeID to the proxy
//			outToIDEProxy.writeBytes(useNodeIdAsOpen+"\n");
//
//			input = inFromIDEProxy.readLine();
//			if (!input.equals("ACK"))
//			{
//				throw new RuntimeException("No ACK recieved, got a "+input);
//			}
//			String fileData = inFromIDEProxy.readLine();
//			if (logger.isEnabledFor(Level.TRACE)) logger.trace("Recreating files using the data: "+fileData);
//			JSONObject jobj = new JSONObject(fileData);
//			unpackageProjectStateFromServer(jobj);
//			if (logger.isEnabledFor(Level.TRACE)) logger.trace("Done recreating, MockIDE now looks like "+toString());
//		}
//		*/
//	}
//
//	private void unpackageProjectStateFromServer(JSONObject jobj) throws JSONException
//	{
//		documentBuffers = new HashMap<String, StringBuilder>();
//		toParentDirectoryMap = new HashMap<String, String>();
//		directoryIDs = new ArrayList<String>();
//		currentDirectoryId=null;
//		currentDocumentId=null;
//
//		JSONArray arrayOfDirectories = jobj.getJSONArray(DIRECTORY_ARRAY);
//		JSONArray arrayOfDocuments = jobj.getJSONArray(DOCUMENT_ARRAY);
//
//		for(int i =0;i<arrayOfDirectories.length();i++)
//		{
//			unpackageDirectory(arrayOfDirectories.getJSONObject(i));
//		}
//
//		for(int i = 0;i<arrayOfDocuments.length();i++)
//		{
//			unpackageDocument(arrayOfDocuments.getJSONObject(i));
//		}
//
//	}
//
//	private void unpackageDocument(JSONObject jsonObject) throws JSONException
//	{
//		String docId = jsonObject.getString(DOCUMENT_ID);
//		String docText = jsonObject.getString(DOCUMENT_TEXT);
//		String parentDirectoryId = jsonObject.getString(PARENT_DIRECTORY_ID);
//
//		//add the document buffer to the hash table using the doc id as the key
//		documentBuffers.put(docId, new StringBuilder(docText));
//
//		//associate the new document to the parent directory
//		toParentDirectoryMap.put(docId, parentDirectoryId);
//
//	}
//
//	private void unpackageDirectory(JSONObject jsonObject) throws JSONException
//	{
//		directoryIDs.add(jsonObject.getString(DIRECTORY_ID));
//
//	}
//
//	public void closeProject() throws IOException, JSONException
//	{
//		if (logger.isEnabledFor(Level.TRACE)) logger.trace("mockIDE.closeProject() stacktrace "+Utilities.getStackTrace(new Throwable()));
//		
//		JSONObject closeCommand = prebuildJSON(CLOSE_PROJECT, getCurrentTime(), getCurrentDevGroupID());
//		
//		logger.debug("Attempting to close project"); 
//		outToIDEProxy.writeBytes(closeCommand.toString()+"\n");
//		//This is an ack
//		inFromIDEProxy.readLine();
//		closeSocketToIDEProxy();
//		logger.debug("Shut down of the project successful");
//	}
//
//	public void clear()
//	{
//		documentBuffers = new HashMap<String, StringBuilder>();
//		toParentDirectoryMap = new HashMap<String, String>();
//
//		directoryIDs = new ArrayList<String>();
//	}
//
//	/**
//	 * Emulates as if the user ran a commit
//	 */
//	public void commit(String name, String description)
//	{
//		try
//		{
//			JSONObject jsonObject = prebuildJSON(COMMIT_NOTIFICATION, getCurrentTime(), getCurrentDevGroupID());
//			jsonObject.put(NEW_NAME, name);
//			jsonObject.put(NODE_DESCRIPTION, description);
//
//			sendJSONObject(jsonObject);
//		}
//		catch (JSONException e)
//		{
//			logger.error("",e);
//		}
//		catch (IOException e)
//		{
//			logger.error("",e);
//		}
//
//	}
//
//	/**
//	 * Creates a JSON object for a CreateNewDocumentEvent and sends it to the currently configured server
//	 * @param newDocId
//	 * @param newDocumentName
//	 * @param parentDirectoryId
//	 */
//	public void sendCreateNewDocumentEventToServer(String newDocId, String newDocumentName, String parentDirectoryId)
//	{
//		if (!directoryIDs.contains(parentDirectoryId))
//		{
//			throw new RuntimeException("There is no parent directory id  " + parentDirectoryId);
//		}
//		//add the document buffer to the hash table using the doc id as the key
//		documentBuffers.put(newDocId, new StringBuilder());
//
//		//associate the new document to the parent directory
//		toParentDirectoryMap.put(newDocId, parentDirectoryId);
//
//		//Create a JSON object with the relevant details and send it to the server
//		JSONObject jsonObject = null;
//
//		try
//		{
//			jsonObject = prebuildJSON(CreateDocumentEvent.CREATE_DOCUMENT_EVENT_TYPE, getCurrentTime(), getCurrentDevGroupID());
//			jsonObject.put(NEW_NAME, newDocumentName);
//			jsonObject.put(DOCUMENT_ID, newDocId);
//			jsonObject.put(PARENT_DIRECTORY_ID, parentDirectoryId);
//
//			sendJSONObject(jsonObject);
//
//		}
//		catch (JSONException e)
//		{
//			logger.error("",e);
//		}
//		catch (IOException e)
//		{
//			logger.error("",e);
//		}
//
//		//save the new doc as the most current document
//		currentDocumentId = newDocId;
//	}
//
//	/**
//	 * Creates a JSON object for a MoveDocumentEvent and sends it to the currently configured server
//	 * @param documentId
//	 * @param parentDirectoryId
//	 * @param destinationDirectoryId
//	 */
//	public void sendMoveDocumentEventToServer(String documentId, String parentDirectoryId, String destinationDirectoryId)
//	{
//		//check to see if the source and destination directories exist
//		if (!directoryIDs.contains(parentDirectoryId))
//		{
//			throw new RuntimeException("There is no source directory id  " + parentDirectoryId);
//		}
//		if (!directoryIDs.contains(destinationDirectoryId))
//		{
//			throw new RuntimeException("There is no destination directory id " + destinationDirectoryId);
//		}
//
//		//if the document to move exists
//		if (documentBuffers.containsKey(documentId))
//		{
//			//Create a JSON object with the relevant details and send it to the server
//			JSONObject jsonObject = null;
//
//			try
//			{
//				jsonObject = prebuildJSON(MoveDocumentEvent.MOVE_DOCUMENT_EVENT_TYPE, getCurrentTime(), getCurrentDevGroupID());
//				jsonObject.put(DOCUMENT_ID, documentId);
//				jsonObject.put(PARENT_DIRECTORY_ID, parentDirectoryId);
//				jsonObject.put(DESTINATION_DIRECTORY_ID, destinationDirectoryId);
//
//				sendJSONObject(jsonObject);
//
//			}
//			catch (JSONException e)
//			{
//				logger.error("",e);
//			}
//			catch (IOException e)
//			{
//				logger.error("",e);
//			}
//			//remove the old association between the doc and dir
//			toParentDirectoryMap.remove(documentId);
//
//			//associate the doc id to a new directory
//			toParentDirectoryMap.put(documentId, destinationDirectoryId);
//		}
//		else
//			//bad dirs or doc 
//		{
//			throw new RuntimeException("Can't move!");
//		}
//	}
//	/**
//	 * Creates a JSON object for a RenameDocumentEvent and sends it to the currently configured server
//	 * @param documentId
//	 * @param oldDocumentName
//	 * @param newDocumentName
//	 * @param parentDirectoryId
//	 */
//	public void sendRenameDocumentEventToServer(String documentId, String oldDocumentName, String newDocumentName, String parentDirectoryId)
//	{
//		//make sure the document exists
//		if (!documentBuffers.containsKey(documentId))
//		{
//			throw new RuntimeException("There is no document");
//		}
//		if (!directoryIDs.contains(parentDirectoryId))
//		{
//			throw new RuntimeException("There is no parent directory id  " + parentDirectoryId);
//		}
//
//		//Create a JSON object with the relevant details and send it to the server
//		JSONObject jsonObject = null;
//
//		try
//		{
//			jsonObject = prebuildJSON(RenameDocumentEvent.RENAME_DOCUMENT_EVENT_TYPE, getCurrentTime(), getCurrentDevGroupID());
//			jsonObject.put(NEW_NAME, newDocumentName);
//			jsonObject.put(OLD_NAME, oldDocumentName);
//			jsonObject.put(DOCUMENT_ID, documentId);
//			jsonObject.put(PARENT_DIRECTORY_ID, parentDirectoryId);
//
//			sendJSONObject(jsonObject);
//
//		}
//		catch (JSONException e)
//		{
//			logger.error("",e);
//		}
//		catch (IOException e)
//		{
//			logger.error("",e);
//		}
//	}
//	/**
//	 * Creates a JSON object for a DeleteDocumentEvent and sends it to the currently configured server
//	 * @param documentId
//	 * @param documentOldName
//	 * @param parentDirectoryId
//	 */
//	public void sendDeleteDocumentEventToServer(String documentId, String documentOldName, String parentDirectoryId)
//	{
//		//make sure the document exists
//		if (!documentBuffers.containsKey(documentId))
//		{
//			throw new RuntimeException("There is no document");
//		}
//		if (!directoryIDs.contains(parentDirectoryId))
//		{
//			throw new RuntimeException("There is no parent directory id  " + parentDirectoryId);
//		}
//
//		//remove the document buffer
//		documentBuffers.remove(documentId);
//
//		//remove the association between the doc and a dir
//		toParentDirectoryMap.remove(documentId);
//
//		//Create a JSON object with the relevant details and send it to the server
//		JSONObject jsonObject = null;
//
//		try
//		{
//			jsonObject = prebuildJSON(DeleteDocumentEvent.DELETE_DOCUMENT_EVENT_TYPE, getCurrentTime(), getCurrentDevGroupID());
//			jsonObject.put(DOCUMENT_ID, documentId);
//			jsonObject.put(PARENT_DIRECTORY_ID, parentDirectoryId);
//			jsonObject.put(OLD_NAME, documentOldName);
//			sendJSONObject(jsonObject);
//
//		}
//		catch (JSONException e)
//		{
//			logger.error("",e);
//		}
//		catch (IOException e)
//		{
//			logger.error("",e);
//		}
//	}
//	/**
//	 * Creates a JSON object for a CreateNewDirectoryEvent and sends it to the currently configured server
//	 * @param newDirId
//	 * @param dirName
//	 * @param parentDirectoryId
//	 */
//	public void sendCreateNewDirectoryEventToServer(String newDirId, String dirName, String parentDirectoryId)
//	{
//		if (!directoryIDs.contains(parentDirectoryId))
//		{
//			throw new RuntimeException("There is no parent directory id  " + parentDirectoryId);
//		}
//		//store the id of the newly created directory
//		directoryIDs.add(newDirId.toString());
//
//		//Create a JSON object with the relevant details and send it to the server
//		JSONObject jsonObject = null;
//
//		try
//		{
//			jsonObject = prebuildJSON(CreateDirectoryEvent.CREATE_DIRECTORY_EVENT_TYPE, getCurrentTime(), getCurrentDevGroupID());
//			jsonObject.put(NEW_NAME, dirName);
//			jsonObject.put(DIRECTORY_ID, newDirId);
//			jsonObject.put(PARENT_DIRECTORY_ID, parentDirectoryId);
//
//			sendJSONObject(jsonObject);
//
//		}
//		catch (JSONException e)
//		{
//			logger.error("",e);
//		}
//		catch (IOException e)
//		{
//			logger.error("",e);
//		}
//
//		//remember the latest directory
//		currentDirectoryId = newDirId;
//	}
//	/**
//	 * Creates a JSON object for a MoveDirectoryEvent and sends it to the currently configured server
//	 * @param directoryId
//	 * @param parentDirectoryId
//	 * @param destinationDirectoryId
//	 */
//	public void sendMoveDirectoryEventToServer(String directoryId, String parentDirectoryId, String destinationDirectoryId)
//	{
//		//check to see if the source and destination directories exist
//		if (!directoryIDs.contains(parentDirectoryId))
//		{
//			throw new RuntimeException("There is no source directory id  " + parentDirectoryId);
//		}
//		if (!directoryIDs.contains(destinationDirectoryId))
//		{
//			throw new RuntimeException("There is no destination directory id " + destinationDirectoryId);
//		}
//
//		//if the directory to move exists
//		if (directoryIDs.contains(directoryId))
//		{
//			//Create a JSON object with the relevant details and send it to the server
//			JSONObject jsonObject = null;
//
//			try
//			{
//				jsonObject = prebuildJSON(MoveDirectoryEvent.MOVE_DIRECTORY_EVENT_TYPE, getCurrentTime(), getCurrentDevGroupID());
//				jsonObject.put(DIRECTORY_ID, directoryId);
//				jsonObject.put(PARENT_DIRECTORY_ID, parentDirectoryId);
//				jsonObject.put(DESTINATION_DIRECTORY_ID, destinationDirectoryId);
//
//				sendJSONObject(jsonObject);
//
//			}
//			catch (JSONException e)
//			{
//				logger.error("",e);
//			}
//			catch (IOException e)
//			{
//				logger.error("",e);
//			}
//		}
//		else
//		{
//			throw new RuntimeException("Can't move! Directory doesn't exist");
//		}
//	}
//	/**
//	 * Creates a JSON object for a RenameDirectoryEvent and sends it to the currently configured server
//	 * @param directoryId
//	 * @param oldDirectoryName
//	 * @param newDirectoryName
//	 * @param parentDirectoryId
//	 */
//	public void sendRenameDirectoryEventToServer(String directoryId, String oldDirectoryName, String newDirectoryName, String parentDirectoryId)
//	{
//		//make sure the directory exists
//		if (!directoryIDs.contains(directoryId))
//		{
//			throw new RuntimeException("There is no directory");
//		}
//		if (!directoryIDs.contains(parentDirectoryId))
//		{
//			throw new RuntimeException("There is no parent directory id  " + parentDirectoryId);
//		}
//
//		//Create a JSON object with the relevant details and send it to the server
//		JSONObject jsonObject = null;
//		try
//		{
//			jsonObject = prebuildJSON(RenameDirectoryEvent.RENAME_DIRECTORY_EVENT_TYPE, getCurrentTime(), getCurrentDevGroupID());
//			jsonObject.put(NEW_NAME, newDirectoryName);
//			jsonObject.put(OLD_NAME, oldDirectoryName);
//			jsonObject.put(DIRECTORY_ID, directoryId);
//			jsonObject.put(PARENT_DIRECTORY_ID, parentDirectoryId);
//
//			sendJSONObject(jsonObject);
//
//		}
//		catch (JSONException e)
//		{
//			logger.error("",e);
//		}
//		catch (IOException e)
//		{
//			logger.error("",e);
//		}
//	}
//	/**
//	 * Creates a JSON object for a DeleteDirectoryEvent and sends it to the currently configured server
//	 * @param directoryId
//	 * @param directoryOldName
//	 * @param parentDirectoryId
//	 */
//	public void sendDeleteDirectoryEventToServer(String directoryId, String directoryOldName, String parentDirectoryId)
//	{
//		//make sure the directory exists
//		if (!directoryIDs.contains(directoryId))
//		{
//			throw new RuntimeException("There is no directory");
//		}
//
//		if (!directoryIDs.contains(parentDirectoryId))
//		{
//			throw new RuntimeException("There is no parent directory id  " + parentDirectoryId);
//		}
//		//Create a JSON object with the relevant details and send it to the server
//		JSONObject jsonObject = null;
//		try
//		{
//			jsonObject = prebuildJSON(DeleteDirectoryEvent.DELETE_DIRECTORY_EVENT_TYPE, getCurrentTime(), getCurrentDevGroupID());
//			jsonObject.put(DIRECTORY_ID, directoryId);
//			jsonObject.put(PARENT_DIRECTORY_ID, parentDirectoryId);
//			jsonObject.put(OLD_NAME, directoryOldName);
//			sendJSONObject(jsonObject);
//
//		}
//		catch (JSONException e)
//		{
//			logger.error("",e);
//		}
//		catch (IOException e)
//		{
//			logger.error("",e);
//		}
//	}
//
//	/**
//	 * A convenience method for sending a long series of text events to the
//	 * server
//	 * 
//	 * @param message
//	 *            any message that you want broken up
//	 * @param index
//	 *            what index to start sending the messages. 0 is the beginning
//	 *            of the "document viewer"
//	 */
//	public void sendStringToServer(String message, int index)
//	{
//		//for all the characters in the passed in string
//		for (int i = 0; i < message.length(); i++)
//		{
//			//get a key
//			String value = message.substring(i, i + 1);
//
//			//send it to the server as an insert
//			sendInsertEventToServer(value, i + index, null, -1, null);
//		}
//	}
//	/**
//	 * used to sanitize "find" strings to valid regular expressions
//	 * 
//	 * @param matchString
//	 * @return
//	 */
//	private static String sanitizeMatchString(String matchString)
//	{
//		return matchString.replaceAll("\\(", "\\\\(") //Replace ( with \(   because the replaceALL is expecting a regex (which uses ()[]
//				.replaceAll("\\)", "\\\\)") //Replace ) with \)   and we need to escape all of those)
//				.replaceAll("\\[", "\\\\[") //Replace [ with \[ 
//				.replaceAll("\\]", "\\\\]") //Replace ] with \]	
//				.replaceAll("\\+", "\\\\+") //Replace + with \+
//				.replaceAll("\\?", "\\\\?") //Replace ? with \?
//				.replaceAll("\\.", "\\\\.") //Replace . with \.
//				.replaceAll("\\{", "\\\\{") //Replace { with \{
//				.replaceAll("\\}", "\\\\}");//Replace } with\}
//	}
//
//	/**
//	 * A helper used to get the index for right after or right before the nth occurrence of a given string
//	 * @param matchString
//	 * @param occurrence
//	 * @param beforeHuh true if the index should be before the nth occurrence, or false if it should be after
//	 * @return
//	 */
//	public int findStringInCurrentBuffer(String matchString, int occurrence, boolean beforeHuh)
//	{
//		if (occurrence <= 0)
//		{
//			throw new InvalidParameterException("occurrence must be a positive integer");
//		}
//		String buffer = getCurrentBuffer();
//		String sanitizedMatchString = sanitizeMatchString(matchString); //clean up the find string so that it's a regex
//
//		StringBuilder replaceStringBuilder = new StringBuilder(matchString.length());
//		for(int i = 0;i<matchString.length();i++)
//		{
//			replaceStringBuilder.append(unusedChar);
//		}
//		String replaceString = replaceStringBuilder.toString();
//
//		String replacedBuffer = buffer.replaceAll(sanitizedMatchString, replaceString);
//
//		//replace the search string with many and then start at the beginning, iterating through the string
//		//until matchString.length()*(occurrence-1) or matchString.length()*occurrence of them are found.  When the next one is found,
//		//that's where the insertion should start
//		int hitChars = 0;
//		int numberToHit;
//		if (beforeHuh)
//		{
//			numberToHit = matchString.length()*(occurrence-1);
//		}
//		else 
//		{
//			numberToHit = matchString.length()*occurrence;
//		}
//		int index;
//
//		for(index =0;index<replacedBuffer.length();index++)
//		{
//			if ((replacedBuffer.charAt(index)==unusedChar)||((!beforeHuh)&&(hitChars==numberToHit)))		// added on extra break out for when we have met the criteria.
//			{
//
//				if (hitChars==numberToHit)
//				{
//					break;
//				}
//				hitChars++;
//			}
//		}
//		if (hitChars!=numberToHit)
//		{
//			throw new RuntimeException("There are not " + occurrence + " occurrences of " + matchString);
//		}
//
//		return index;
//
//	}
//
//	public String createSelectedPlayback(String selectedText, int occurrence) throws JSONException, IOException 
//	{
//		int index = findStringInCurrentBuffer(selectedText, occurrence, true);
//
//		String sessionId = sendSelectedTextToServer(new Date(), index, selectedText.length());
//
//		return sessionId;
//	}
//
//
//
//	/**
//	 * A convenience method for inserting text immediately before some matched
//	 * text.
//	 * 
//	 * @param insertMessage
//	 *            The text to insert
//	 * @param matchString
//	 *            A string that the text should be inserted before
//	 * @param occurrence
//	 *            If there is more than one match, which one should be chosen?
//	 * @return returns the index at which the "cursor" is after the typing happened
//	 */
//	public int insertStringBeforeToServer(String insertMessage, String matchString, int occurrence)
//	{
//		int index = findStringInCurrentBuffer(matchString, occurrence, true);
//
//		//pass off this info to another convenience method.
//		sendStringToServer(insertMessage, index);
//		return index+insertMessage.length();
//	}
//
//	/**
//	 * A convenience method for inserting text immediately after some matched
//	 * text.
//	 * 
//	 * @param insertMessage
//	 *            The text to insert
//	 * @param matchString
//	 *            A string that the text should be inserted before
//	 * @param occurrence
//	 *            If there is more than one match, which one should be chosen?
//	 *            
//	 * @return returns where the "cursor" would be after inserting
//	 */
//	public int insertStringAfterToServer(String insertMessage, String matchString, int occurrence)
//	{
//
//		int index = findStringInCurrentBuffer(matchString, occurrence, false);
//
//		//pass off this info to another convenience method.
//		sendStringToServer(insertMessage, index);
//		return index+insertMessage.length();
//	}
//
//
//
//	/**
//	 * A convenience method for easily inserting text.
//	 * 
//	 * @param message
//	 *            Some message where the skipChar means to ignore/not insert the
//	 *            char and anything else will be inserted
//	 * @param skipChar
//	 *            what char to use for skipping. Recommended chars are '_' or
//	 *            '-'
//	 */
//	public void insertStringToServer(String insertMessage, String skipChar, int startingIndex)
//	{
//		//for all the characters in the passed in message
//		for (int i = 0; i < insertMessage.length(); i++)
//		{
//			//grab the text
//			String value = insertMessage.substring(i, i + 1);
//
//			//if we are not supposed to skip it
//			if (!value.equals(skipChar))
//			{
//				//get the starting index
//				int offsetIndex = i + startingIndex;
//
//				//make sure its within bounds
//				if (offsetIndex < 0 || offsetIndex > documentBuffers.get(currentDocumentId).length())
//				{
//					throw new RuntimeException("Illegal use of insertStringToServer.  " + 
//							"You tried to insert text starting at a location outside what has been written.  " +
//							"Current length of text is " + documentBuffers.get(currentDocumentId).length() + 
//							" and you tried inserting a " + value + " at " + offsetIndex);
//				} 
//
//				//send the event to the server
//				sendInsertEventToServer(value, offsetIndex, null, -1, null);
//			}
//		}
//	}
//
//	/**
//	 * A convenience method for sending some deletes to the server. It parses
//	 * the deletes from right to left, as if the user was backspacing
//	 * 
//	 * @param deleteMessage
//	 *            Some message where '_' means to ignore/not delete the char and
//	 *            'X' means to delete
//	 * @param index
//	 *            an index to start deleting at. This can be less than zero,
//	 *            provided that no delete chars ('X') exist outside the actual
//	 *            text area
//	 */
//	public void sendDeletesToServer(String deleteMessage, int index)
//	{
//
//		for (int i = deleteMessage.length() - 1; i >= 0; i--) //parse through each char in the delete message backwards
//		{
//
//			if (deleteMessage.charAt(i) == 'X') //if the parsed character is 'X', send a delete if and only if the offset index is pointing to a char in the buffer
//			{
//				int offsetIndex = i + index;
//				if (offsetIndex < 0 || offsetIndex > documentBuffers.get(currentDocumentId).length())
//				{
//					throw new RuntimeException("Illegal use of sendDeletesToServer.  " + "You tried to send a delete command to a location outside what has been written.  "
//							+ "Current length of text is " + documentBuffers.get(currentDocumentId).length() + " and you tried deleting at " + offsetIndex);
//				}
//				sendDeleteEventToServer(offsetIndex);
//			}
//		}
//	}
//
//	/**
//	 * Goes and finds the deleteString and then sends proper delete commands to the Server to perform the desired action
//	 * 
//	 * @param string
//	 * @param ocurrance
//	 * @return the index of the last delete
//	 */
//	public int findAndDeleteToServer(String deleteString, int occurrence)
//	{
//		int index = findStringInCurrentBuffer(deleteString, occurrence, true);
//
//		//now turn the replaceString into all X's
//		StringBuilder replaceStringBuilder = new StringBuilder(deleteString.length());
//		for(int i = 0;i<deleteString.length();i++)
//		{
//			replaceStringBuilder.append('X');			//make a "delete string" with a perfect amount of 'X's to get rid of the requested string
//		}
//		sendDeletesToServer(replaceStringBuilder.toString(), index);
//		return index;
//
//	}
//
//	/**
//	 * Cuts a given string (from the given occurrence of that string in the current document) and pastes it at indexToPasteAt
//	 * @param stringToCut
//	 * @param occurrence
//	 * @param indexToPasteAt
//	 */
//	public void cutAndPaste(String stringToCut, int occurrence, int indexToPasteAt)
//	{
//		cutAndPaste(stringToCut, occurrence, currentDocumentId, currentDocumentId, indexToPasteAt);
//	}
//
//	/**
//	 * Cuts a given string (from the given occurrence of that string in the given document) and pastes it at indexToPasteAt in the other given document
//	 * @param stringToCut
//	 * @param occurrence
//	 * @param docIdOfOrigin
//	 * @param docIdOfDestination
//	 * @param indexToPasteAt
//	 */
//	public void cutAndPaste(String stringToCut, int occurrence,String docIdOfOrigin,String docIdOfDestination, int indexToPasteAt)
//	{
//
//		Date copiedAtDate = getCurrentTime();
//		setCurrentDocumentId(docIdOfOrigin);		//go to the document where the cut happened
//
//		int startingIndexOfCut = findStringInCurrentBuffer(stringToCut, occurrence, true);
//
//		setHoldTime(true);		//freeze the time to make all the deletes occur at the same time
//
//		findAndDeleteToServer(stringToCut, occurrence); 
//		setHoldTime(false);
//
//		if (docIdOfDestination.equals(docIdOfOrigin)&&indexToPasteAt>startingIndexOfCut)	//we are going to have adjust the index to paste at because of the deletes
//		{
//			indexToPasteAt-=stringToCut.length();
//		}
//
//		delayTime(1000);
//
//		pasteToServer(stringToCut, startingIndexOfCut, docIdOfOrigin, docIdOfDestination, indexToPasteAt, copiedAtDate);
//	}
//
//
//	/**
//	 * Copies a given string (from the given occurrence of that string in the current buffer) to the index given.
//	 * 
//	 * @param stringToCopy
//	 * @param occurrence
//	 * @param indexToPasteAt
//	 */
//	public void copyAndPasteToServer(String stringToCopy, int occurrence, int indexToPasteAt)
//	{
//		copyAndPasteToServer(stringToCopy, currentDocumentId, occurrence, currentDocumentId, indexToPasteAt);
//	}
//
//	/**
//	 * Copies a given string (from the given occurence of that string in the indicated document) to the index given in the specified document
//	 * @param stringToCopy
//	 * @param docIdOfOrigin
//	 * @param occurence
//	 * @param docIdOfDestination
//	 * @param indexToPasteAt
//	 */
//	public void copyAndPasteToServer(String stringToCopy, String docIdOfOrigin, int occurrenceOfStringToCopy, String docIdOfDestination, int indexToPasteAt)
//	{
//		setCurrentDocumentId(docIdOfOrigin);
//		int startingIndexOfCopy = findStringInCurrentBuffer(stringToCopy, occurrenceOfStringToCopy, true);
//		Date copyTime = getCurrentTime();
//		delayTime(1000);
//		pasteToServer(stringToCopy, startingIndexOfCopy, docIdOfOrigin, docIdOfDestination, indexToPasteAt, copyTime);
//	}
//
//	/**
//	 * Splits a string up into many characters and sends them as a paste
//	 * @param stringToPaste
//	 * @param startingIndexOfCopy
//	 * @param docIdOfOrigin
//	 * @param docIdOfDestination
//	 * @param indexToPasteAt
//	 * @param copyTime
//	 */
//	private void pasteToServer(String stringToPaste, int startingIndexOfCopy, String docIdOfOrigin, String docIdOfDestination, int indexToPasteAt, Date copyTime)
//	{
//
//		setCurrentDocumentId(docIdOfDestination);
//		//for all the characters in the passed in string
//		for (int i = 0; i < stringToPaste.length(); i++)
//		{
//			//get a key
//			String value = stringToPaste.substring(i, i + 1);
//
//			//send it to the server as an insert, using the offset of where the string was "copied" from
//			sendInsertEventToServer(value, indexToPasteAt + i ,docIdOfOrigin, startingIndexOfCopy+i, copyTime);
//		}
//	}
//
//	/**
//	 * Sends a character to a certain index
//	 * 
//	 * @param value
//	 *            the character to insert
//	 * @param index
//	 *            where to put this in the buffer
//	 * @param pasteParentId
//	 * 			  where the paste was from
//	 * @param pasteParentIndex
//	 * 			  the index in the pasteParent
//	 * @param copyTime 
//	 */
//	private void sendInsertEventToServer(String value, int index, String pasteParentId, int pasteParentIndex, Date copyTime)
//	{
//
//		//Create a JSON object with the relevant details and send it to the server
//		JSONObject jsonObject = null;
//
//		try
//		{
//			jsonObject = prebuildJSON(InsertEvent.INSERT_EVENT_TYPE, getCurrentTime(), getCurrentDevGroupID());
//			jsonObject.put(INDEX, index);
//			jsonObject.put(EVENT_DATA, value);
//			jsonObject.put(DOCUMENT_ID, getCurrentDocumentId());
//			if (pasteParentId!=null)
//			{
//				jsonObject.put(PASTE_PARENT_ID, pasteParentId);
//				jsonObject.put(PASTE_PARENT_INDEX, pasteParentIndex);
//				jsonObject.put(COPIED_TIMESTAMP, String.valueOf(copyTime.getTime()));
//			}
//			sendJSONObject(jsonObject);
//
//		}
//		catch (JSONException e)
//		{
//			logger.error("",e);
//		}
//		catch (IOException e)
//		{
//			logger.error("",e);
//		}
//
//		//Now update the current document's buffer
//		documentBuffers.get(currentDocumentId).insert(index, value);
//		if (humanDelay)
//		{
//			delayTime(80+Math.round(Math.random()*80));		//delay after this keypress for an average human amount of time, which at 80wpm, or 8.3 char/second)
//		}
//	}
//
//	/**
//	 * Sends a DeleteEvent to the Configured Server at the given index
//	 * 
//	 * @param index
//	 */
//	private void sendDeleteEventToServer(int index)
//	{
//		logger.trace("Deleting char '"+documentBuffers.get(currentDocumentId).charAt(index)+"' from index "+index);
//		//Create a JSON object with the relevant details and send it to the server
//		JSONObject jsonObject = null;
//
//		try
//		{
//			jsonObject = prebuildJSON(DeleteEvent.DELETE_EVENT_TYPE, getCurrentTime(), getCurrentDevGroupID());
//			jsonObject.put(INDEX, index);
//			jsonObject.put(DOCUMENT_ID, getCurrentDocumentId());
//
//			sendJSONObject(jsonObject);
//
//		}
//		catch (JSONException e)
//		{
//			logger.error("",e);
//		}
//		catch (IOException e)
//		{
//			logger.error("",e);
//		}
//		//remove the text from the doc buffer
//		documentBuffers.get(currentDocumentId).deleteCharAt(index);
//		if (humanDelay)
//		{
//			delayTime(10+Math.round(Math.random()*80));		//delay after this keypress for an average human amount of time (it's usually quicker to delete)
//		}
//	}
//
//	/**
//	 * Sets up a continuously connected socket to the server proxy at the pathToServer and the ideProxyPort
//	 */
//	private void createSocketToIDEProxy()
//	{
//		try
//		{
//			//create a socket connection with a server on another machine
//			socketToIDEProxy = new Socket(pathToServer, ideProxyPort);
//
//			//we can use outToServer to write to the socket
//			outToIDEProxy = new DataOutputStream(socketToIDEProxy.getOutputStream());
//
//			//we can use inFromServer to read in to the socket
//			inFromIDEProxy = new BufferedReader(new InputStreamReader(socketToIDEProxy.getInputStream()));
//		}
//		catch (IOException ex)
//		{
//			logger.error("",ex);
//		}
//	}
//
//	/**
//	 * Called once the MockIDE is needed no more to relinquish the socket
//	 */
//	public void closeSocketToIDEProxy()
//	{
//		try
//		{
//			//close the socket after we are done with it
//			socketToIDEProxy.close();
//		}
//		catch (IOException ex)
//		{
//			logger.error("",ex);
//		}
//	}
//
//	public String getCurrentBuffer()
//	{
//		return documentBuffers.get(currentDocumentId).toString();
//	}
//
//	@Override
//	public String toString()
//	{
//		StringBuilder builder = new StringBuilder();
//		builder.append("MockIDE [currentDirectoryId=");
//		builder.append(currentDirectoryId);
//		builder.append(", currentDocumentId=");
//		builder.append(currentDocumentId);
//		builder.append(", currentDevGroupID=");
//		builder.append(currentDevGroupID);
//		builder.append(", documentBuffers=");
//		builder.append(documentBuffers);
//		builder.append("]");
//		return builder.toString();
//	}
//
//	/**
//	 * Prints out the current buffer so that all escaped characters are visible
//	 */
//	public void export()
//	{
//		StringBuilder builder = new StringBuilder();
//		for (char c : documentBuffers.get(currentDocumentId).toString().toCharArray())
//		{
//			if (c == '\n')
//			{
//				builder.append("\\n");
//			}
//			else if (c == '\t')
//			{
//				builder.append("\\t");
//			}
//			else if (c == '\\')
//			{
//				builder.append("\\\\");
//			}
//			else if (c == '"')
//			{
//				builder.append("\\\"");
//			}
//			else
//			{
//				builder.append(c);
//			}
//		}
//		logger.info("The exported buffer is: "+builder.toString());
//	}
//
//	public int getCurrentLength()
//	{
//		return documentBuffers.get(currentDocumentId).toString().length();
//	}
//
//	public String getCurrentProjectName()
//	{
//		return currentProjectName;
//	}
//
//	/**
//	 * All JSON Objects are going to have certain traits in common. This method
//	 * takes care of those.
//	 * 
//	 * @param type
//	 * @param timestamp
//	 * @param devGroupId
//	 * @return
//	 * @throws JSONException
//	 */
//	private JSONObject prebuildJSON(String type, Date timestamp, String devGroupId) throws JSONException
//	{
//		JSONObject jsonObject = new JSONObject();
//
//		//string representation of the timestamp
//		String timestampString = String.valueOf(timestamp.getTime());
//
//		//these get set for all event types
//		jsonObject.put(TYPE, type);
//		jsonObject.put(TIMESTAMP, timestampString);
//		jsonObject.put(DEVELOPER_GROUP_ID, devGroupId);
//		jsonObject.put(SESSION_ID, sessionId);
//
//		return jsonObject;
//	}
//
//	/**
//	 * Follows the protocol to set up a selectedTextPlayback from the server.
//	 * @param date
//	 * @param index
//	 * @param length
//	 * @return
//	 */
//	private String sendSelectedTextToServer(Date date, int index, int length) throws JSONException, IOException 
//	{
//		//create a selected text JSON message
//		JSONObject jobj = prebuildJSON(JSONConstants.SELECTED_TEXT_FOR_PLAYBACK_TYPE, date, currentDevGroupID);
//		jobj.put(JSONConstants.SESSION_ID, sessionId);
//		jobj.put(JSONConstants.DOCUMENT_ID, currentDocumentId);
//		jobj.put(JSONConstants.LENGTH, length);
//		jobj.put(JSONConstants.INDEX, index);
//
//		//send it through the socket.  We don't call sendJSONObject() because we need to hold onto the returned sessionId
//		String jsonString = jobj.toString() + "\n";
//		outToIDEProxy.writeBytes(jsonString);
//
//		//wait for a session id from the server proxy
//		String sessionId = inFromIDEProxy.readLine();
//		//create a JSON object from the response
//		JSONObject sessionObject = new JSONObject(sessionId);
//		//return the session id
//		return sessionObject.getString(SESSION_ID);
//	}
//
//	/**
//	 * Follows proper protocol to send a JSON object containing the vital details for any Event
//	 * @param jsonObject
//	 * @throws IOException
//	 */
//	protected void sendJSONObject(JSONObject jsonObject) throws IOException
//	{
//		timer.trace("Sending "+jsonObject.toString());
//		String jsonString = jsonObject.toString() + "\n";
//		outToIDEProxy.writeBytes(jsonString);
//
//		//wait for an acknowledgment from the server proxy
//		String ack = inFromIDEProxy.readLine();
//
//		try 
//		{
//			JSONObject ackObject = new JSONObject(ack);
//		
//			//make sure it is a positive ack
//			if (!ackObject.has(ACKNOWLEDGMENT_VALUE) || 
//				!ackObject.getString(ACKNOWLEDGMENT_VALUE).equals(JSONConstants.DEFAULT_ACK_MESSAGE))
//			{
//				//the server did not acknowledge the sending
//				throw new RuntimeException("Did not get an ack back");
//			}
//			timer.trace("Sent "+jsonObject.toString());
//		} 
//		catch (JSONException e) 
//		{
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * Follows proper protocol to send a JSON object containing the vital details for any Event
//	 * @param jsonObject
//	 * @return 
//	 * @throws IOException
//	 */
//	protected String sendJSONObjectExpectingResponse(JSONObject jsonObject) throws IOException
//	{
//		timer.trace("Sending "+jsonObject.toString());
//		String jsonString = jsonObject.toString() + "\n";
//		outToIDEProxy.writeBytes(jsonString);
//
//		//wait for an acknowledgment from the server proxy
//		String ack = inFromIDEProxy.readLine();
//
//		timer.trace("Sent "+jsonObject.toString());
//		
//		return ack;
//	}
//
//	public String getCurrentDirectoryId()
//	{
//		return currentDirectoryId;
//	}
//
//	public void setCurrentDirectoryId(String currentDirectoryId)
//	{
//		this.currentDirectoryId = currentDirectoryId;
//	}
//
//	public String getCurrentDocumentId()
//	{
//		return currentDocumentId;
//	}
//
//	public void setCurrentDocumentId(String currentDocumentId)
//	{
//		this.currentDocumentId = currentDocumentId;
//	}
//
//	/**
//	 * Communicates with the server to log this developer in.  This should be called the 
//	 * first time a developer has ever been on the server (it's also okay to use the second, third or nth time as well)
//	 * 
//	 * 
//	 * @param developerFirstName
//	 * @param developerLastName
//	 * @param developerEmailAddress
//	 */
//	public void changeDeveloper(String developerFirstName, String developerLastName, String developerEmailAddress) throws JSONException, IOException
//	{
//		changeDevelopers(new String[]{developerFirstName}, new String[]{developerLastName}, new String[]{developerEmailAddress});
//	}
//	
//	/**
//	 * This should only be called after 
//	 * @param developerEmailAddress
//	 * @throws IOException 
//	 * @throws JSONException 
//	 */
//	public void changeDeveloper(String developerEmailAddress) throws JSONException, IOException
//	{
//		changeDevelopers(null, null, new String[]{developerEmailAddress});
//	}
//	
//	/**
//	 * 
//	 * @param developerFirstNames
//	 * @param developerLastNames
//	 * @param developerEmailAddress
//	 * @throws JSONException
//	 * @throws IOException
//	 */
//	public void changeDevelopers(String[] developerFirstNames, String[] developerLastNames, String[] developerEmailAddresses) throws JSONException, IOException
//	{
//		JSONObject jsonObject = prebuildJSON(LOGIN_AS_DEVELOPER_GROUP, getCurrentTime() , "");
//		
//		JSONArray jarrDeveloperEmailAddresses = new JSONArray();
//		for(int i = 0;i<developerEmailAddresses.length;i++)
//		{
//			jarrDeveloperEmailAddresses.put(developerEmailAddresses[i]);
//		}
//		
//		jsonObject.put(DEVELOPER_EMAIL_ADDRESSES, jarrDeveloperEmailAddresses);
//		
//		logger.debug("in changeDeveloper(), sending "+jsonObject);
//		
//		String reply = sendJSONObjectExpectingResponse(jsonObject);
//		
//		logger.debug("Recieved: "+reply);
//		
//		JSONObject replyJSON = new JSONObject(reply);
//		
//		this.currentDevGroupID = replyJSON.getString(DEVELOPER_GROUP_ID);
//		
//		if (replyJSON.has(DEVELOPERS_WHO_NEED_MORE_INFO))
//		{
//			sendExtraDeveloperInformation(replyJSON, developerFirstNames,developerLastNames,developerEmailAddresses);
//		}
//	}
//	/**
//	 * 
//	 * @param developerEmailAddress
//	 * @throws JSONException
//	 * @throws IOException
//	 */
//	public void changeDevelopers(String[] developerEmailAddresses) throws JSONException, IOException
//	{
//		changeDevelopers(null, null, developerEmailAddresses);
//	}
//	
//	public void createNewDeveloper(String devFirstName, String devLastName, String devEmail) throws JSONException, IOException
//	{
//		JSONObject commandObject = new JSONObject();
//		commandObject.put(TYPE, ADD_NEW_DEVELOPER);
//
//		commandObject.put(FIRST_NAME, devFirstName);
//		commandObject.put(LAST_NAME, devLastName);
//		commandObject.put(EMAIL , devEmail);
//		commandObject.put(TIMESTAMP, getCurrentTime());
//		commandObject.put(DEVELOPER_GROUP_ID, getCurrentDevGroupID());
//		commandObject.put(SESSION_ID, getSessionId());
//		
//		logger.debug("Creating developer");
//
//		outToIDEProxy.writeBytes(commandObject.toString()+"\n");
//		//This is an ack
//		String jsonString = inFromIDEProxy.readLine();
//		logger.trace("mockIDE.createNewDeveloper() had the following jsonString: "+jsonString);
//	}
//
//	
//	private void sendExtraDeveloperInformation(String developerFirstName, String developerLastName, String developerEmailAddress) throws JSONException, IOException
//	{
//		JSONObject jsonObject = prebuildJSON(UPDATE_DEVELOPER_INFO,getCurrentTime(), getCurrentDevGroupID());
//
//		jsonObject.put(FIRST_NAME, developerFirstName);
//		jsonObject.put(LAST_NAME, developerLastName);
//		jsonObject.put(EMAIL, developerEmailAddress);
//		logger.debug("MockIDE updating "+developerEmailAddress+" to have a first name of "+developerFirstName+" and a last name of "+developerLastName);
//		sendJSONObject(jsonObject);
//	}
//	
//	private void sendExtraDeveloperInformation(JSONObject replyJSON, String[] developerFirstNames, String[] developerLastNames,String[] developerEmailAddresses) throws JSONException, IOException
//	{
//		JSONArray devsWhoNeedMoreInfo = replyJSON.getJSONArray(DEVELOPERS_WHO_NEED_MORE_INFO);
//		//Yes, this is a O(n^2) search, but really shouldn't matter for the 3 or 4 (tops) developers that we have to handle
//		for(int i = 0;i<devsWhoNeedMoreInfo.length();i++)
//		{
//			for(int j =0;j<developerEmailAddresses.length;j++)
//			{
//				if (devsWhoNeedMoreInfo.getString(i).equals(developerEmailAddresses[j]))
//				{
//					sendExtraDeveloperInformation(developerFirstNames[j], developerLastNames[j], developerEmailAddresses[j]);
//				}
//			}
//		}
//	}
//
//}
