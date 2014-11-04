package ide;

import static core.Constants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import playback.PlaybackSession;

import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.entities.Developer;
import core.entities.DeveloperGroup;
import core.entities.Directory;
import core.entities.Document;
import core.entities.ExtendedDirectory;
import core.entities.ExtendedDocument;
import core.entities.Node;
import core.entities.Project;
import core.events.CloseNodeEvent;
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
import core.events.StorytellerEvent;

/**
 * An IDE Session represents the state of a project at any given point in time.
 * It knows what database to use to create/retrieve/update/delete data, which
 * developer group to attribute changes to, the current node (all new data tracks
 * the created under node id) and node sequence number (to keep track of the 
 * number of events created in a node), the id of the event that came immediately
 * before a new event (the wonderfully named sequentiallyBeforeIdOfLastEvent), 
 * the project, and the state of the files/folders in the project (this includes
 * the actual text inside the documents and the file/folder parent ids). 
 */
public class IDESession
{
	//each ide session has its own id
	private String sessionID;

	//reference to the database for this session
	private DBAbstraction database;
	
	//holds the dev group id for the logged in group of devs
	private String loggedInDevGroupId;
	
	//a reference to the current node. All new events will use this as their 
	//'created under node' 
	private Node currentNode = null;

	//this is a sequence number used to record the order of events within a node 	
	//this should be set back to zero every time the createdUnderNode is reset
	private int sequenceNumber = 0;

	//this is the id of the last event successfully added 
	private String sequentiallyBeforeIdOfLastEvent = null;

	//reference to the project??? do we do anything with projects now???
	private Project project;

	//map that holds the path to a file/folder to their storyteller ids
	//"path/to/file" -> "1234"
	private Map < String, String > docPathToDocIdMap;
	
	//"path/to/folder" -> "3456"
	private Map < String, String > dirPathToDirIdMap;

	//this map uses a doc id as a key and the contents of a doc as the value
	//"1234" -> "The quick brown fox"
	//"1235" -> "This is a doc."
	private Map < String, DocumentBuffer > documentBuffers;
	
	//this map uses a doc id to retrieve a document name
	//"1234" -> "fox.txt"
	//"1235" -> "doc.txt"
	private Map < String, String > docIdToDocNameMap;
	
	//this map uses a dir id to retrieve a dir name
	//"3456" -> "rootDir"
	//"3457" -> "subDir1"
	private Map < String, String > dirIdToDirNameMap;

	
	//this map uses a doc id to retrieve the parent dir id that contains the doc
	//"1234" -> "3457"
	//"1235" -> "3456"
	private Map < String, String > docDirIdToParentDirIdMap;

	//this map uses a dir id to retrieve the parent dir id that contains the dir
	//"3457" -> "3456"
	//private Map < String, String > dirIdToParentDirIdMap;

	public IDESession(DBAbstraction db, Map < String, Map < String, String > > allDevInfo) throws DBAbstractionException
	{
		//TODO perhaps the id should be the full path to the project folder
		//generate a unique id
		sessionID = UUID.randomUUID().toString();

		//store this session's database
		setDatabase(db);
		
		//find or create the new logged in dev group id		
		logInDevelopers(allDevInfo);
		
		//create the dir/document structures
		initializeSession();
	}

	public IDESession(DBAbstraction db, Map < String, Map < String, String > > allDevInfo, String projectName) throws DBAbstractionException
	{
		this(db, allDevInfo);
		
		//TODO remove the project entity from the system- with separate db's it is no longer required
		//get the project from the database
		Project project = getDatabase().getProjectByName(projectName);
		
		//store the project
		setProject(project);
		
		//get the node the user wants to update to
		Node openNode = getDatabase().getOpenNode();

		//get the last event in the passed in node
		StorytellerEvent lastEvent = getDatabase().getLastEventInNode(openNode.getId());
		
		//create a playback session to recreate the state of the files
		PlaybackSession session = new PlaybackSession(getDatabase(), getLoggedInDevGroupId(), openNode.getId(), lastEvent.getNodeSequenceNum());

		//get the state of the file system 
		ExtendedDirectory rootDir = session.getTheStateOfTheFileSystem(true, false);

		//clear out the passed in session so that it holds the new state of the file system
		initializeSession(rootDir);

		//update the session to handle new events
		setSequentiallyBeforeIdOfLastEvent(lastEvent.getId());
		setSequenceNumber(lastEvent.getNodeSequenceNum() + 1);
		setCurrentNode(openNode);
	}

	/**
	 * Used by the constructors to init some data. Can also be used to clear 
	 * out an existing session 
	 */
	public void initializeSession()
	{
		//init the private maps to keep track of the file system
		documentBuffers = new HashMap < String, DocumentBuffer >();

		docIdToDocNameMap = new HashMap < String, String >();

		dirIdToDirNameMap = new HashMap < String, String >();

		docDirIdToParentDirIdMap = new HashMap < String, String >();
		 
		docPathToDocIdMap = new HashMap < String, String >(); 
		dirPathToDirIdMap = new HashMap < String, String >();
	}

	/**
	 * Used by the constructors to init some data. This one recreates the state
	 * of the file system based on an extended directory structure 
	 */
	public void initializeSession(ExtendedDirectory rootDir)
	{
		//init the data
		initializeSession();
		
		//fill the lists with the directories and documents
		initAllDirectories(rootDir);
		initAllDocuments(rootDir);		
	}

	/**
	 * Helper to get all the directories and their info in the maps
	 */
	private void initAllDirectories(ExtendedDirectory dir)
	{	
		//add the passed in directory's info to the maps
		String dirId = dir.getId();
		String dirPath = dir.getPath();
		String dirName = dir.getName();
		String parentDirId = dir.getParentDirectoryId();

		//map the dir id to the dir name
		dirIdToDirNameMap.put(dirId, dirName);
		//map the dir id to its parent dir id
		docDirIdToParentDirIdMap.put(dirId, parentDirId);
		//map the current dir path to the dir id
		dirPathToDirIdMap.put(dirPath, dirId);
		
		//go through this directory's sub-directories 
		for(ExtendedDirectory subDir : dir.getSubdirectories())
		{
			//recurse
			initAllDirectories(subDir);
		}
	}
	
	/**
	 * Helper to get all the documents and their info in the maps
	 */
	private void initAllDocuments(ExtendedDirectory dir)
	{
		//gather all of this directory's documents
		for(ExtendedDocument doc : dir.getDocuments())
		{
			//get the doc info
			String docId = doc.getId();
			String docPath = doc.getPath();
			String docName = doc.getName();
			String parentDirId = doc.getParentDirectoryId();

			//create a new doc buffer with the insert events 
			DocumentBuffer newDocument = new DocumentBuffer(doc.getInsertEvents());
		
			//map the doc id to the document buffer that holds its info
			documentBuffers.put(docId, newDocument);
			
			//map the doc id to its name
			docIdToDocNameMap.put(docId, docName);
			//map the doc id to its parent dir id
			docDirIdToParentDirIdMap.put(docId, parentDirId);
			//map the current doc path to the doc id 
			docPathToDocIdMap.put(docPath, docId);  
		}
		
		//go through this directory's sub-directories 
		for(ExtendedDirectory subDir : dir.getSubdirectories())
		{
			//recurse
			initAllDocuments(subDir);
		}
	}

	/**
	 * Create a logged in developer group id for this IDE Session. A map of dev
	 * info is passed in (first name, last name, email for each dev in the map)
	 * and the developer is created if it is not already in the database. Then 
	 * we look in the db to see if a dev group exists with all of those devs.
	 * If so, we store the dev group id as the logged in dev group id. If not, 
	 * we create a new dev group id and make it the logged in dev group id. 
	 */
	public void logInDevelopers(Map<String, Map<String, String>> allDevInfo) throws DBAbstractionException
	{
		//holds the devs we want to log in
		List < Developer > loggedInDevs = new ArrayList < Developer >();
		
		//go through each of the developers email addresses that we'd like to 
		//make the logged in dev group
		for(String emailAddress : allDevInfo.keySet())
		{
			//check to see if the developer exists in the db yet
			Developer dev = getDatabase().getDeveloperByEmailAddress(emailAddress);
			
			//if the dev is not in the db
			if(dev == null)
			{
				//create the dev (and their sole dev group)
				getDatabase().addADeveloperAndCreateANewDeveloperGroup(
						new Date(), 
						allDevInfo.get(emailAddress).get(FIRST_NAME), 
						allDevInfo.get(emailAddress).get(LAST_NAME), 
						allDevInfo.get(emailAddress).get(EMAIL), 
						null, null);
				
				//now get the new dev
				dev = getDatabase().getDeveloperByEmailAddress(emailAddress);
			}
			//else- there is a dev with that email in the passed in db
			
			//add the dev to the collection we want to make the logged in group
			loggedInDevs.add(dev);
		}
		
		//get all the ids of the devs
		List < String > ids = new ArrayList < String >();
		for(Developer dev : loggedInDevs)
		{
			ids.add(dev.getId());
		}
		
		//attempt to get the dev group that has all the devs in it
		DeveloperGroup devGroup = getDatabase().getDeveloperGroupByDeveloperIds(ids);
		
		//if there is no such group that has all of the passed in devs
		if(devGroup == null)
		{
			//create a new dev group
			devGroup = new DeveloperGroup(loggedInDevs, new Date(), null, null);
			//insert it in the db
			getDatabase().insertDeveloperGroup(devGroup);
			
			//for each of the devs
			for(Developer dev: loggedInDevs)
			{
				//join the dev and the new dev group
				getDatabase().joinDeveloperAndDeveloperGroup(dev, devGroup, null, null);
			}
		}
		//else- there was a developer group in the db with the passed in devs
		
		//now set the logged in dev group id
		setLoggedInDevGroupId(devGroup.getId());
	}
	
	/**
	 * Receive a text event that was not pasted
	 */
	public void receiveInsertEvent(int index, String value, String timestamp, String thisDocumentPath, String pasteParentId) throws DBAbstractionException
	{
		//get the current node id
		String createdUnderNodeId = getCurrentNode().getId();
		
		//get the current sequence number
		int currentSequenceNumber = getSequenceNumber();
		
		//get the last event id
		String sequentiallyBeforeId = getSequentiallyBeforeIdOfLastEvent();
		
		//get the time
		Date dateTimestamp = new Date(Long.parseLong(timestamp));

		//get the document id from the doc path
		String docId = getDocPathToDocIdMap().get(thisDocumentPath);
		
		//start these out as empty strings to create the event
		String idOfPreviousNeighbor = null;
		String sortOrder = "";

		//create an insert event (in memory)
		InsertEvent newInsertEvent = new InsertEvent(
										dateTimestamp, createdUnderNodeId,
										getLoggedInDevGroupId(), currentSequenceNumber,
										sequentiallyBeforeId, docId,
										idOfPreviousNeighbor, value,
										pasteParentId, sortOrder);

		//render this event in the proper document buffer
		//insert the text in the document buffer and get the 'after' event id 
		//(returns the ID of the event after the new one)
		String idOfEventAfterNewOne = renderInsertEventByLocation(newInsertEvent, index);
		
		//get the id of the event before the new one
		String idOfEventBeforeNewOne = newInsertEvent.getPreviousNeighborEventId();
		
		//holds the 'sortOrder' field for the events around the new event
		String beforeSortOrder = null;
		String afterSortOrder = null;

		//TODO possibly store the sort order along with the id and text in the doc buffer
		//and then get it from the buffer rather than the db
		
		//if there was an 'after' event id (the new text event is not the last one in a file)
		if(idOfEventAfterNewOne != null)
		{
			//get the insert event after the new one
			InsertEvent afterEvent = (InsertEvent)getDatabase().getEvent(idOfEventAfterNewOne);

			//get the after event's sort order value
			afterSortOrder = afterEvent.getSortOrder();
		}
		
		//if there was a 'before' event id (the new text event is not the first one in a file)
		if(idOfEventBeforeNewOne != null)
		{
			//get the insert event before the new one
			InsertEvent beforeEvent = (InsertEvent)getDatabase().getEvent(idOfEventBeforeNewOne);

			//get the before event's sort order value
			beforeSortOrder = beforeEvent.getSortOrder();
		}
		
		//TODO debug only to see prettier sortOrders- remove later
		//InBetweenHelper.setUseBase10Digits(true);
		
		//get a value in between the sort orders of the surrounding text events
		//every text event's sort order is between its neighbors. This allows us to sort events by 
		//the 'sort order' to get the events back in a comprehensible order
		newInsertEvent.setSortOrder(InBetweenHelper.inbetween(beforeSortOrder, afterSortOrder));

		//store the insert event in the db
		getDatabase().insertEvent(newInsertEvent);

		//store the id of the latest event added
		setSequentiallyBeforeIdOfLastEvent(newInsertEvent.getId());

		//successfully added a new event in the node, increase the sequence number
		currentSequenceNumber++;
		setSequenceNumber(currentSequenceNumber);
	}

	/**
	 * Handle a delete event from the IDE
	 */
	public void receiveDeleteEvent(int index, String timestamp, String thisDocumentPath) throws DBAbstractionException
	{
		//get the node/sequence info
		String createdUnderNodeId = getCurrentNode().getId();
		int currentSequenceNumber = getSequenceNumber();
		String sequentiallyBeforeId = getSequentiallyBeforeIdOfLastEvent();
		Date dateTimestamp = new Date(Long.parseLong(timestamp));

		//start this out as an empty string to create the event
		String idOfPreviousNeighbor = null;

		//get the document id from the doc path
		String docId = getDocPathToDocIdMap().get(thisDocumentPath);

		//create a delete event (in memory)
		DeleteEvent newDeleteEvent = new DeleteEvent(
										dateTimestamp, createdUnderNodeId,
										getLoggedInDevGroupId(), currentSequenceNumber,
										sequentiallyBeforeId, docId,
										idOfPreviousNeighbor);

		//update the state of the IDE to reflect that a text event was deleted 
		//(returns the id of the event that was deleted)
		idOfPreviousNeighbor = renderDeleteEventByLocation(newDeleteEvent, index);

		//set the previous neighbor id before we add to the db
		newDeleteEvent.setPreviousNeighborEventId(idOfPreviousNeighbor);

		//store the delete event in the db
		getDatabase().insertEvent(newDeleteEvent);

		//store the id of the latest event added
		setSequentiallyBeforeIdOfLastEvent(newDeleteEvent.getId());

		//successfully added a new event in the node, increase the sequence number
		currentSequenceNumber++;
		setSequenceNumber(currentSequenceNumber);
	}

	/**
	 * Handle a create document event 
	 */
	public void receiveCreateDocumentEvent(String documentNewName, String timestamp, String docPath, String parentDirPath) throws DBAbstractionException
	{
		//get the node/sequence info
		String createdUnderNodeId = getCurrentNode().getId();
		int currentSequenceNumber = getSequenceNumber();
		String sequentiallyBeforeId = getSequentiallyBeforeIdOfLastEvent();
		Date dateTimestamp = new Date(Long.parseLong(timestamp));

		//get the id of the parent dir from its path
		String parentDirId = getDirPathToDirIdMap().get(parentDirPath);
		
		//create the document in the db
		Document newDocument = new Document(dateTimestamp, 
									createdUnderNodeId, getLoggedInDevGroupId(), 
									parentDirId);
		
		//insert the document in the db
		getDatabase().insertDocument(newDocument);

		//create a create document event (in memory)
		CreateDocumentEvent createDocEvent = new CreateDocumentEvent(
										dateTimestamp, createdUnderNodeId,
										getLoggedInDevGroupId(), currentSequenceNumber,
										sequentiallyBeforeId, newDocument.getId(),
										documentNewName, parentDirId);

		//store the create document event in the db
		getDatabase().insertEvent(createDocEvent);

		//update the state of the IDE to represent the new document
		//add a new empty document buffer 
		getDocumentBuffers().put(newDocument.getId(), new DocumentBuffer()); 
		
		//map the doc id to the doc name
		getDocIdToDocNameMap().put(newDocument.getId(), documentNewName);
		
		//map the doc id to the parent dir id
		getDocDirIdToParentDirIdMap().put(newDocument.getId(), parentDirId);
		
		//map the doc path to the doc id 
		getDocPathToDocIdMap().put(docPath, newDocument.getId());  

		//store the id of the latest event added
		setSequentiallyBeforeIdOfLastEvent(createDocEvent.getId());

		//successfully added a new event in the node, increase the sequence number
		currentSequenceNumber++;
		setSequenceNumber(currentSequenceNumber);
	}

	/**
	 * Handle a rename document event 
	 */
	public void receiveRenameDocumentEvent(String documentOldName, String documentNewName, String timestamp, String documentOldPath, String documentNewPath, String documentParentOldPath, String documentParentNewPath) throws DBAbstractionException
	{
		//get the node/sequence info
		String createdUnderNodeId = getCurrentNode().getId();
		int currentSequenceNumber = getSequenceNumber();
		String sequentiallyBeforeId = getSequentiallyBeforeIdOfLastEvent();
		Date dateTimestamp = new Date(Long.parseLong(timestamp));

		//get the doc id from the doc path
		String docId = getDocPathToDocIdMap().get(documentOldPath);

		//get the dir id of the parent from its path 
		String parentDirId = getDirPathToDirIdMap().get(documentParentOldPath);

		//create a rename document event (in memory)
		RenameDocumentEvent newRenameDocEvent = new RenameDocumentEvent(
										dateTimestamp, createdUnderNodeId,
										getLoggedInDevGroupId(), currentSequenceNumber,
										sequentiallyBeforeId, docId,
										documentNewName, documentOldName, parentDirId);

		//store the rename document event in the db
		getDatabase().insertEvent(newRenameDocEvent);

		//update the IDE Session to reflect the new name of the document
		//add a new mapping from the doc id to the new name 
		getDocIdToDocNameMap().put(docId, documentNewName);
		
		//update the path to doc id map
		getDocPathToDocIdMap().remove(documentOldPath);
		getDocPathToDocIdMap().put(documentNewPath, docId);
		
		//store the id of the latest event added
		setSequentiallyBeforeIdOfLastEvent(newRenameDocEvent.getId());

		//successfully added a new event in the node, increase the sequence number
		currentSequenceNumber++;
		setSequenceNumber(currentSequenceNumber);
	}

	/**
	 * Handle a move document event 
	 */
	public void receiveMoveDocumentEvent(String timestamp, String documentOldPath, String documentNewPath, String documentParentOldPath, String documentParentNewPath) throws DBAbstractionException
	{
		//if the destination is the same as the current directory then it is 
		//not really a move
		if (!documentOldPath.equals(documentNewPath))
		{
			//get the node/sequence info
			String createdUnderNodeId = getCurrentNode().getId();
			int currentSequenceNumber = getSequenceNumber();
			String sequentiallyBeforeId = getSequentiallyBeforeIdOfLastEvent();
			Date dateTimestamp = new Date(Long.parseLong(timestamp));
	
			//get the doc id from the old path
			String docId = getDocPathToDocIdMap().get(documentOldPath);
			//get the old dir id from the old path
			String currentParentDirId = getDocDirIdToParentDirIdMap().get(docId);
			//get the new dir id from the new path
			String destinationDirId = getDirPathToDirIdMap().get(documentParentNewPath);

			
			
			//TODO do we need to update the par dir id????
			//get the document from the database
			Document docToMove = getDatabase().getDocument(docId);
	
			//update its parent to be the destination directory
			docToMove.setParentDirectoryId(destinationDirId);
	
			//update the document in the db
			getDatabase().updateDocument(docToMove);
	
			
			
			//create a move document event (in memory)
			MoveDocumentEvent newMoveDocEvent = new MoveDocumentEvent(
										dateTimestamp, createdUnderNodeId,
										getLoggedInDevGroupId(), currentSequenceNumber,
										sequentiallyBeforeId, docId,
										currentParentDirId, destinationDirId);
	
			//store the move document event in the db
			getDatabase().insertEvent(newMoveDocEvent);
	
			//update the IDE state to reflect the moved document
			//add a new mapping from the doc id to a new parent dir id
			getDocDirIdToParentDirIdMap().put(docId, destinationDirId);
			
			//update the path to doc id map
			getDocPathToDocIdMap().remove(documentOldPath);
			getDocPathToDocIdMap().put(documentNewPath, docId);
			
			//store the id of the latest event added
			setSequentiallyBeforeIdOfLastEvent(newMoveDocEvent.getId());
	
			//successfully added a new event in the node, increase the sequence number
			currentSequenceNumber++;
			setSequenceNumber(currentSequenceNumber);
		}
		//else- current dir id and destination dir id are the same- nothing has moved
	}

	/**
	 * Handle a delete document event 
	 */
	public void receiveDeleteDocumentEvent(String timestamp, String docPath, String parentDirectoryPath, String docName) throws DBAbstractionException
	{
		//get the node/sequence info
		String createdUnderNodeId = getCurrentNode().getId();
		int currentSequenceNumber = getSequenceNumber();
		String sequentiallyBeforeId = getSequentiallyBeforeIdOfLastEvent();
		Date dateTimestamp = new Date(Long.parseLong(timestamp));

		//get the id from the path
		String docId = getDocPathToDocIdMap().get(docPath);
		String parentDirId = getDirPathToDirIdMap().get(parentDirectoryPath);

		//create a delete document event (in memory)
		DeleteDocumentEvent newDeleteDocEvent = new DeleteDocumentEvent(
										dateTimestamp, createdUnderNodeId,
										getLoggedInDevGroupId(), currentSequenceNumber,
										sequentiallyBeforeId, docId,
										parentDirId, docName);

		//store the delete document event in the db
		getDatabase().insertEvent(newDeleteDocEvent);

		//update the IDE state to reflect the delete doc
		//this happens in a couple of different places, so call the function
		deleteDocumentHelper(docId);
				
		//store the id of the latest event added
		setSequentiallyBeforeIdOfLastEvent(newDeleteDocEvent.getId());

		//successfully added a new event in the node, increase the sequence number
		currentSequenceNumber++;
		setSequenceNumber(currentSequenceNumber);
	}

	/**
	 * Handle a create directory event 
	 */
	public void receiveCreateDirectoryEvent(String directoryNewName, String timestamp, String dirPath, String parentDirectoryPath) throws DBAbstractionException
	{
		//get the node/sequence info 
		String createdUnderNodeId = getCurrentNode().getId();
		int currentSequenceNumber = getSequenceNumber();
		String sequentiallyBeforeId = getSequentiallyBeforeIdOfLastEvent();
		Date dateTimestamp = new Date(Long.parseLong(timestamp));

		//get the id from the path
		String parentDirId = getDirPathToDirIdMap().get(parentDirectoryPath);

		//create a new directory and store it in the db
		Directory newDirectory = new Directory(dateTimestamp, 
										createdUnderNodeId, getLoggedInDevGroupId(), 
										parentDirId);
		
		getDatabase().insertDirectory(newDirectory);

		//create a create directory event (in memory)
		CreateDirectoryEvent newCreateDirectoryEvent = 
								new CreateDirectoryEvent(dateTimestamp, 
									createdUnderNodeId, getLoggedInDevGroupId(), 
									currentSequenceNumber, sequentiallyBeforeId, 
									newDirectory.getId(), directoryNewName, 
									parentDirId);

		//store the create directory event in the db
		getDatabase().insertEvent(newCreateDirectoryEvent);

		//update the state of the IDE to reflect the newly created dir
		//map the new directory id to the name of the dir
		getDirIdToDirNameMap().put(newDirectory.getId(), directoryNewName);

		//map the new directory id to the parent dir id
		getDocDirIdToParentDirIdMap().put(newDirectory.getId(), parentDirId);
		
		//map the path to the dir id
		getDirPathToDirIdMap().put(dirPath, newDirectory.getId());
		
		//store the id of the latest event added
		setSequentiallyBeforeIdOfLastEvent(newCreateDirectoryEvent.getId());

		//successfully added a new event in the node, increase the sequence number
		currentSequenceNumber++;
		setSequenceNumber(currentSequenceNumber);
	}

	/**
	 * Handle a rename directory event 
	 */
	public void receiveRenameDirectoryEvent(String directoryOldName, String directoryNewName, String timestamp, String directoryOldPath, String directoryNewPath, String directoryParentOldPath, String directoryParentNewPath) throws DBAbstractionException
	{
		//get the node/sequence info
		String createdUnderNodeId = getCurrentNode().getId();
		int currentSequenceNumber = getSequenceNumber();
		String sequentiallyBeforeId = getSequentiallyBeforeIdOfLastEvent();
		Date dateTimestamp = new Date(Long.parseLong(timestamp));

		//get the id of the dir from its old path
		String dirId = getDirPathToDirIdMap().get(directoryOldPath);
		//get the id of the dir's parent dir from its path
		String parentDirId = getDirPathToDirIdMap().get(directoryParentOldPath);

		//create a rename directory event (in memory)
		RenameDirectoryEvent newRenameDirectoryEvent = 
					new RenameDirectoryEvent(dateTimestamp, createdUnderNodeId,
						getLoggedInDevGroupId(), currentSequenceNumber,
						sequentiallyBeforeId, dirId,
						directoryNewName,directoryOldName, parentDirId);

		//store the rename directory event in the db
		getDatabase().insertEvent(newRenameDirectoryEvent);

		//update the state of the IDE to reflect the dir rename
		//add a new mapping from the doc id to the new dir name
		getDirIdToDirNameMap().put(dirId, directoryNewName);
		
		//update the path to dir id map
		getDirPathToDirIdMap().remove(directoryOldPath);
		getDirPathToDirIdMap().put(directoryNewPath, dirId);
		
		//store the id of the latest event added
		setSequentiallyBeforeIdOfLastEvent(newRenameDirectoryEvent.getId());

		//successfully added a new event in the node, increase the sequence number
		currentSequenceNumber++;
		setSequenceNumber(currentSequenceNumber);
	}

	/**
	 * Handle a move directory event 
	 */
	public void receiveMoveDirectoryEvent(String timestamp, String directoryOldPath, String directoryNewPath, String directoryParentOldPath, String directoryParentNewPath) throws DBAbstractionException
	{
		//if the current directory id is the same as the destination directory id then it is not really a move
		if(!directoryOldPath.equals(directoryNewPath))
		{
			//get the node/sequence info
			String createdUnderNodeId = getCurrentNode().getId();
			int currentSequenceNumber = getSequenceNumber();
			String sequentiallyBeforeId = getSequentiallyBeforeIdOfLastEvent();
			Date dateTimestamp = new Date(Long.parseLong(timestamp));

			//get the id from the path
			String dirId = getDirPathToDirIdMap().get(directoryOldPath);
			String currentParentDirId = getDocDirIdToParentDirIdMap().get(dirId);
			String destinationDirId = getDirPathToDirIdMap().get(directoryParentNewPath);
			
			//TODO do we need to store the new parent id????
			//get the directory from the database
			Directory dirToMove = getDatabase().getDirectory(dirId);
	
			//update its parent to be the destination directory
			dirToMove.setParentDirectoryId(destinationDirId);
	
			//update the directory in the db
			getDatabase().updateDirectory(dirToMove);
	
			
			
			
			//create a move directory event (in memory)
			MoveDirectoryEvent newMoveDirectoryEvent = 
					new MoveDirectoryEvent(dateTimestamp, createdUnderNodeId,
							getLoggedInDevGroupId(), currentSequenceNumber,
							sequentiallyBeforeId, dirId,
							currentParentDirId, destinationDirId);
	
			//store the move directory event in the db
			getDatabase().insertEvent(newMoveDirectoryEvent);
	
			//update the state of the IDE to reflect the moved dir
			//add a new mapping from the dir id to its parent		
			getDocDirIdToParentDirIdMap().put(dirId, destinationDirId);
			
			//update the path to dir id map
			getDirPathToDirIdMap().remove(directoryOldPath);
			getDirPathToDirIdMap().put(directoryNewPath, dirId);
			
			//store the id of the latest event added
			setSequentiallyBeforeIdOfLastEvent(newMoveDirectoryEvent.getId());
	
			//successfully added a new event in the node, increase the sequence number
			currentSequenceNumber++;
			setSequenceNumber(currentSequenceNumber);
		}
		//else- curr dir id and destination dir id are the same- do nothing
	}

	/**
	 * Handle a delete directory event 
	 */
	public void receiveDeleteDirectoryEvent(String timestamp, String dirPath, String parentDirectoryPath, String dirName) throws DBAbstractionException
	{
		//get the node/sequence info
		String createdUnderNodeId = getCurrentNode().getId();
		int currentSequenceNumber = getSequenceNumber();
		String sequentiallyBeforeId = getSequentiallyBeforeIdOfLastEvent();
		Date dateTimestamp = new Date(Long.parseLong(timestamp));

		//get the id from the path
		String dirId = getDirPathToDirIdMap().get(dirPath);
		String parentDirId = getDirPathToDirIdMap().get(parentDirectoryPath);

		//create a delete document event (in memory)
		DeleteDirectoryEvent newDeleteDirectoryEvent = 
							new DeleteDirectoryEvent(
									dateTimestamp, createdUnderNodeId,
									getLoggedInDevGroupId(), currentSequenceNumber,
									sequentiallyBeforeId, dirId,
									parentDirId, dirName);

		//store the delete directory event in the db
		getDatabase().insertEvent(newDeleteDirectoryEvent);

		//update the state of the IDE to reflect the deleted directory
		//this happens in a couple of places... so, call the function
		deleteDirectoryHelper(dirId);
		
		//store the id of the latest event added
		setSequentiallyBeforeIdOfLastEvent(newDeleteDirectoryEvent.getId());

		//successfully added a new event in the node, increase the sequence number
		currentSequenceNumber++;
		setSequenceNumber(currentSequenceNumber);
	}
	/**
	 * A helper method that recursively deletes everything in a directory
	 */
	private void deleteDirectoryHelper(String deleteDirId)
	{
		//remove the deleted dir info
		getDirIdToDirNameMap().remove(deleteDirId);			
		getDocDirIdToParentDirIdMap().remove(deleteDirId);

		//go through all of the paths of directories
		for(String dirPath : getDirPathToDirIdMap().keySet())
		{
			//if a path yields the dir id we have found the path to the dir to remove
			if(getDirPathToDirIdMap().get(dirPath).equals(deleteDirId))
			{
				//remove the mapping to the deleted directory
				getDirPathToDirIdMap().remove(dirPath);

				//stop looking 
				break;				
			}
		}

		//get all of the document ids
		Set<String> allDocIds = new HashSet<String>(getDocIdToDocNameMap().keySet());
		
		//go through all documents created
		for (String potentialDocumentId : allDocIds) 	
		{
			//if a document's parentDirID is the one being deleted, we should delete it
			if (getDocDirIdToParentDirIdMap().get(potentialDocumentId).equals(deleteDirId))
			{
				//remove the document
				deleteDocumentHelper(potentialDocumentId);
			}
		}
		
		//now we will go through and recursively delete all subdirectories
		
		//get all the directory ids
		Set<String> allDirIds = new HashSet<String>(getDirIdToDirNameMap().keySet());
		
		//go through all directories
		for (String potentialDirectoryId : allDirIds) 
		{
			//if a directory's parentDirID is the one being deleted, we should delete it
			if (getDocDirIdToParentDirIdMap().get(potentialDirectoryId).equals(deleteDirId))
			{
				//recursively delete all sub directories
				deleteDirectoryHelper(potentialDirectoryId);		
			}
		}		
	}
			
	/**
	 * Helps delete a document in a renderer based on the id
	 */
	private void deleteDocumentHelper(String docId)
	{
		//remove the deleted doc info 
		getDocumentBuffers().remove(docId);
		getDocIdToDocNameMap().remove(docId);
		getDocDirIdToParentDirIdMap().remove(docId);

		//for all the paths in the doc path to doc id map
		for(String docPath : getDocPathToDocIdMap().keySet())
		{
			//if a path yields the passed in id we have found the correct path
			if(getDocPathToDocIdMap().get(docPath).equals(docId))
			{
				//remove the mapping from doc path to doc id
				getDocPathToDocIdMap().remove(docPath);
				
				//stop looking
				break;
			}
		}
	}

	/**
	 * This method is used to close the current node and open up a new one. If 
	 * no destination node id is passed in then the open node will be directly 
	 * underneath the ide session's current open node (there will always be at 
	 * most one open node). If the destination node id is valid then a new node
	 * will be created underneath the node with that id.
	 * 
	 * The method will close the current open node (and record that in the 
	 * database) and insert a closed node event. If the new open node will be 
	 * directly underneath the newly closed node then a new open node will be 
	 * created and an open node event will be inserted. If the new open node is 
	 * somewhere other than directly underneath the closed node then the state 
	 * of the ide session had to be changed to the state of the passed in 
	 * destination node id. After this happens the new open node can be created 
	 * with this state.
	 * 
	 * The state of the file system will be returned to the IDE so it can update 
	 * the editors with the selected node as the correct starting point.
	 * 
	 * @param ideSession The session on the server to change the nodes
	 * @param destinationNodeId The destination node where to create a new node 
	 * under. If a null value is passed in it means that the new node will be 
	 * directly underneath the current open node
	 * @param timestamp Time of change
	 * @param developerGroupId Dev group making the change
	 * @param nameOfNewNode Name of the newly created node
	 * @param descriptionOfNewNode Description of the new node
	 * @return A JSON object with the state of the file system where the new node 
	 * will start OR null if we are creating a new node directly under the current 
	 * node (since the IDE will have the correct state at that time)
	 * @throws DBAbstractionException
	 * @throws JSONException
	 */
	public JSONObject closeCurrentOpenNodeAndCreateANewOpenNode(String destinationNodeId, String timestamp, String nameOfNewNode, String descriptionOfNewNode) throws DBAbstractionException, JSONException
	{
		JSONObject structureOfFileData = null;

		//get the current open node from the IDE session
		Node currentOpenNode = getCurrentNode();

		//close it and write the changes to the db
		currentOpenNode.close();
		getDatabase().updateNode(currentOpenNode);

		//get the time of the request
		Date dateTimestamp = new Date(Long.parseLong(timestamp));
		
		//create a closed node event and insert it
		CloseNodeEvent closeNodeEvent = new CloseNodeEvent(dateTimestamp, currentOpenNode.getId(), getLoggedInDevGroupId(), getSequenceNumber(), getSequentiallyBeforeIdOfLastEvent());
		getDatabase().insertEvent(closeNodeEvent);
		
		//store the closed node event as the last one inserted
		setSequentiallyBeforeIdOfLastEvent(closeNodeEvent.getId());

		//if there is a desired destination node other than under the current node
		if(destinationNodeId != null && !destinationNodeId.isEmpty() &&
		   !destinationNodeId.equals(currentOpenNode.getId()))
		{
			//clear out the current state of the IDE session
			initializeSession();

			//update the state of the IDE session to the passed in node id
			updateIDESessionToBeAtTheEndOfNode(destinationNodeId, null);

			//update to the new parent- this is the node with the passed in id
			currentOpenNode = getCurrentNode();

			//get all the file data at the new parent to update the editors
			structureOfFileData = getFileDataFromIDESession();
		}

		//update the session's sequence number to go back to 0, new node back to zero
		setSequenceNumber(0);

		//get the existing project from the current session
		Project existingProject = getProject();

		//create a new node, following the parent node
		Node newNode = new Node(dateTimestamp, currentOpenNode.getId(), 
						getLoggedInDevGroupId(), nameOfNewNode, descriptionOfNewNode, 
						existingProject.getId(), currentOpenNode.getNodeLineageNumber() + 1,
						Node.OPEN_NODE);
		getDatabase().insertNode(newNode);
		
		//make the new node the current one
		setCurrentNode(newNode);

		//create an event for playback
		OpenNodeEvent openNodeEvent = new OpenNodeEvent(dateTimestamp, newNode.getId(),
				getLoggedInDevGroupId(), getSequenceNumber(),
				getSequentiallyBeforeIdOfLastEvent(), currentOpenNode.getId());

		getDatabase().insertEvent(openNodeEvent);

		//set the sequence num and last event
		setSequenceNumber(1);
		setSequentiallyBeforeIdOfLastEvent(openNodeEvent.getId());

		//return the file data
		return structureOfFileData;
	}

	/**
	 * Sets a given IDE session to be updated to a given nodeID, stopping at the passed in event
	 */
	public void updateIDESessionToBeAtTheEndOfNode(String nodeId, String eventIdToStopAtInLastNode) throws DBAbstractionException
	{
		//get the node the user wants to update to
		Node openNode = getDatabase().getNode(nodeId);

		//recreate the state of the ide through that node
		StorytellerEvent lastEvent = updateIDEStateThroughNode(nodeId, eventIdToStopAtInLastNode);

		//update the session to handle new events
		setSequentiallyBeforeIdOfLastEvent(lastEvent.getId());

		setSequenceNumber(lastEvent.getNodeSequenceNum()+1);
		setCurrentNode(openNode);
	}

	/**
	 * Updates renderer with all of the events in node, stopping at eventIdToStopAtInLastNode.
	 */
	public StorytellerEvent updateIDEStateThroughNode(String nodeId, String eventIdToStopAtInLastNode) throws DBAbstractionException
	{
		//the last event in the node to update to 
		StorytellerEvent lastEvent;

		//if there is no passed in event id
		if(eventIdToStopAtInLastNode == null)
		{
			//get the last event in the passed in node
			lastEvent = getDatabase().getLastEventInNode(nodeId);
		}
		else //there is an id of an event to stop at
		{
			//get the event to stop at
			lastEvent = getDatabase().getEvent(eventIdToStopAtInLastNode);
		}
		
		//create a playback session to the point being requested
		PlaybackSession session = new PlaybackSession(getDatabase(), getLoggedInDevGroupId(), nodeId, lastEvent.getNodeSequenceNum());

		//get the state of the file system at the passed in time (node/event id) with event list
		ExtendedDirectory rootDir = session.getTheStateOfTheFileSystem(true, false);

		//clear out the passed in session so that it holds the new state of the file system
		initializeSession(rootDir);

		return lastEvent;
	}

	/**
	 * Gets all of the ids of the selected events in a buffer
	 */
	public List<String> getIdsOfSelectedText(String thisDocumentPath, int index, int length)
	{
		//get the document id from the path to the file
		String docId = getDocPathToDocIdMap().get(thisDocumentPath);
		
		//get the document buffer for the passed in document
		DocumentBuffer documentBuffer = getDocumentBuffers().get(docId);

		//list of event ids of the selected text in the editor
		List < String > selectedEventIds = new ArrayList < String >();

		//go from the start index to the last selected event
		for(int i = 0;i < length;i++)
		{
			//grab the id of the selected event
			selectedEventIds.add(documentBuffer.getIdAtIndex(index + i));
		}
		
		return selectedEventIds;
	}

	/*
	 * Gets all the text of some selected events based on their position in 
	 * the document buffer
	 */
	public String getSelectedText(String thisDocumentPath, int index, int length)
	{
		//get the document id from the path to the file
		String docId = getDocPathToDocIdMap().get(thisDocumentPath);

		//get the document buffer for the passed in session and document
		DocumentBuffer documentBuffer = getDocumentBuffers().get(docId);

		//list of event ids of the selected text in the editor
		StringBuilder builder = new StringBuilder();

		//go from the start index to the last selected event
		for(int i = 0;i < length;i++)
		{
			//get the text at the position
			builder.append(documentBuffer.getTextAtIndex(index + i));
		}
		
		return builder.toString();
	}

	/**
	 * Gets the state of the file system for a particular IDE session and wraps
	 * it up in JSON.
	 */
	public JSONObject getFileDataFromIDESession() throws JSONException
	{
		//the json object that holds the entire directory structure
		JSONObject fileDataObject = new JSONObject();
		
		//array that holds the directory info
		JSONArray directoryArray = new JSONArray();
		
		//get each dir id from the IDE session
		for(String dirID : getDirIdToDirNameMap().keySet())
		{
			//create a json object to represent a directory
			JSONObject aDir = new JSONObject();				
			
			//set the dir members
			aDir.put(DIRECTORY_ID, dirID);
			aDir.put(DIRECTORY_NAME, getDirIdToDirNameMap().get(dirID));
			aDir.put(PARENT_DIRECTORY_ID, getDocDirIdToParentDirIdMap().get(dirID));
			
			//add the directory object to the directory array
			directoryArray.put(aDir);
		}

		//create an array to hold json objects with the document info
		JSONArray documentArray = new JSONArray();
		
		//go through each document in the IDE session
		for(String docID : getDocIdToDocNameMap().keySet())
		{
			//create a json to represent a document
			JSONObject aDoc = new JSONObject();	
			
			//set the doc info
			aDoc.put(DOCUMENT_ID, docID);
			aDoc.put(DOCUMENT_NAME, getDocIdToDocNameMap().get(docID));
			aDoc.put(PARENT_DIRECTORY_ID, getDocDirIdToParentDirIdMap().get(docID));
			aDoc.put(DOCUMENT_TEXT, getDocumentBuffers().get(docID).toString());
			
			//add the doc object to the array of doc objects
			documentArray.put(aDoc);
		}
		
		//add the directory and document arrays to the json object that holds all of the dir info
		fileDataObject.put(DIRECTORY_ARRAY, directoryArray);
		fileDataObject.put(DOCUMENT_ARRAY, documentArray);

		return fileDataObject;
	}
	
	/**
	 * Renders a CreateDirectoryEvent, updating renderer to mirror the change
	 */
	public void renderCreateDirectoryEvent(String pathToDir, CreateDirectoryEvent createDirectoryEvent)
	{
		//map the new directory id to the name of the dir
		getDirIdToDirNameMap().put(createDirectoryEvent.getDirectoryId(), createDirectoryEvent.getDirectoryNewName());

		//map the new directory id to the parent dir id
		getDocDirIdToParentDirIdMap().put(createDirectoryEvent.getDirectoryId(), createDirectoryEvent.getParentDirectoryId());
		
		//map the path to the dir id
		getDirPathToDirIdMap().put(pathToDir, createDirectoryEvent.getDirectoryId());
	}
	
	/**
	 * Renders an InsertEvent using the previousNeighborId for location, updating renderer to mirror the change
	 */
	public void renderInsertEvent(InsertEvent insertEvent)
	{
		//insert a new event in an already existing document
		getDocumentBuffers().get(insertEvent.getDocumentId()).addTextEvent(insertEvent.getId(), insertEvent.getPreviousNeighborEventId(), insertEvent.getEventData());
	}
	
	/**
	 * Should only be called when the InsertEvent is being created, that is, 
	 * when the previousNeighborId is null, but the location is known 
	 */
	public String renderInsertEventByLocation(InsertEvent newInsertEvent, int locationOfInsert)
	{
		//if there is no prev neighbor, we have a problem
		if (!(newInsertEvent.getPreviousNeighborEventId() == null ||
			  newInsertEvent.getPreviousNeighborEventId().equals("null") ||
			  newInsertEvent.getPreviousNeighborEventId().isEmpty()))
		{
			throw new RuntimeException("Improper Usage of renderInsertEventByLocation!  It should only be called when creating a new InsertEvent, not if the previousNeighborEventId is known");
		}

		//get the document buffer based on the doc id
		DocumentBuffer docBuffer = getDocumentBuffers().get(newInsertEvent.getDocumentId());
		
		//get the event id after where the new event will go
		String afterId = docBuffer.getIdAtIndex(locationOfInsert);

		//insert at the correct position (and get the id of the prev neighbor)
		String idOfPreviousNeighbor = docBuffer.addTextEvent(newInsertEvent.getId(), locationOfInsert, newInsertEvent.getEventData());
		
		//set the previous neighbor
		newInsertEvent.setPreviousNeighborEventId(idOfPreviousNeighbor);
		
		return afterId;
	}

	/**
	 * Renders a DeleteEvent, updating renderer to mirror the change
	 */
	public void renderDeleteEvent(DeleteEvent deleteEvent)
	{	
		//go to the map of all doc buffers and select one based on doc id
		//then use the doc buffer to find out the previous neighbor's id
		getDocumentBuffers().get(deleteEvent.getDocumentId()).addDeleteEvent(deleteEvent.getPreviousNeighborEventId());
	}
	
	/**
	 * Should only be called when the DeleteEvent is being created, that is, when the previousNeighborId is null, but the location is known
	 */
	public String renderDeleteEventByLocation(DeleteEvent deleteEvent, int locationOfDelete)
	{	
		//if there is not previous neighbor, we have a problem
		if (!(deleteEvent.getPreviousNeighborEventId()==null ||
			  deleteEvent.getPreviousNeighborEventId().equals("null") ||
			  deleteEvent.getPreviousNeighborEventId().isEmpty()))
		{
			throw new RuntimeException("Improper Usage of renderDeleteEventByLocation!  It should only be called when creating a new DeleteEvent, not if the previousNeighborEventId is known");
		}
		
		//get the document buffer where the delete occured
		DocumentBuffer docBuffer = getDocumentBuffers().get(deleteEvent.getDocumentId());
		
		//handle the delete in the doc buffer (and return the id of the event 
		//that was deleted)
		return docBuffer.addDeleteEvent(locationOfDelete);
	}
	
	private Map<String, DocumentBuffer> getDocumentBuffers()
	{
		return documentBuffers;
	}

	private Map<String, String> getDocIdToDocNameMap()
	{
		return docIdToDocNameMap;
	}

	private Map<String, String> getDirIdToDirNameMap()
	{
		return dirIdToDirNameMap;
	}

	private Map<String, String> getDocDirIdToParentDirIdMap()
	{
		return docDirIdToParentDirIdMap;
	}

	public Map<String, String> getDocPathToDocIdMap()
	{
		return docPathToDocIdMap;
	}

	public Map<String, String> getDirPathToDirIdMap()
	{
		return dirPathToDirIdMap;
	}
	
	public String getSessionId()
	{
		return sessionID;
	}
	
	public void setSessionId(String id)
	{
		sessionID = id;
	}

	/**
	 * Get the current node in this IDE session 
	 */
	public Node getCurrentNode()
	{
		return currentNode;
	}
	public void setCurrentNode(Node node)
	{
		this.currentNode = node;
	}

	/**
	 * Get the latest sequence number in this IDE session 
	 */
	public int getSequenceNumber()
	{
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber)
	{
		this.sequenceNumber=sequenceNumber;
	}

	/**
	 * Get the latest sequentially before id in this IDE session 
	 */
	public String getSequentiallyBeforeIdOfLastEvent()
	{
		return sequentiallyBeforeIdOfLastEvent;
	}
	public void setSequentiallyBeforeIdOfLastEvent(String id)
	{
		this.sequentiallyBeforeIdOfLastEvent=id;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("IDESession [sessionID=");
		builder.append(sessionID);
		builder.append(", currentNode=");
		if (currentNode==null)
		{
			builder.append("null");
		}
		else
		{
			builder.append(currentNode.getId());
		}
		builder.append(", sequenceNumber=");
		builder.append(sequenceNumber);
		builder.append(", sequentiallyBeforeIdOfLastEvent=");
		builder.append(sequentiallyBeforeIdOfLastEvent);
		builder.append(", copyHelper=");
		//builder.append(copyHelper);
		builder.append("]");
		return builder.toString();
	}

	public void setProject(Project project)
	{
		this.project=project;
	}	
	public Project getProject()
	{
		return this.project;
	}

	public DBAbstraction getDatabase()
	{
		return database;
	}

	private void setDatabase(DBAbstraction database)
	{
		this.database = database;
	}

	public String getLoggedInDevGroupId()
	{
		return loggedInDevGroupId;
	}

	public void setLoggedInDevGroupId(String loggedInDevGroupId)
	{
		this.loggedInDevGroupId = loggedInDevGroupId;
	}
}