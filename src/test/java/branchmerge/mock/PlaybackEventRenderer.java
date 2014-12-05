package branchmerge.mock;
//package Playback;
//
//import static StorytellerServer.json.JSONConstants.*;
//
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.DefaultTreeModel;
//import javax.swing.tree.MutableTreeNode;
//
//import org.apache.log4j.Logger;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import StorytellerEntities.Events.AutomaticConflictEvent;
//import StorytellerEntities.Events.CloseNodeEvent;
//import StorytellerEntities.Events.ConflictResolutionEvent;
//import StorytellerEntities.Events.CreateDirectoryEvent;
//import StorytellerEntities.Events.CreateDocumentEvent;
//import StorytellerEntities.Events.DeleteDirectoryEvent;
//import StorytellerEntities.Events.DeleteDocumentEvent;
//import StorytellerEntities.Events.DeleteEvent;
//import StorytellerEntities.Events.InsertEvent;
//import StorytellerEntities.Events.ManualConflictEvent;
//import StorytellerEntities.Events.MergeEvent;
//import StorytellerEntities.Events.MoveDirectoryEvent;
//import StorytellerEntities.Events.MoveDocumentEvent;
//import StorytellerEntities.Events.OpenNodeEvent;
//import StorytellerEntities.Events.RenameDirectoryEvent;
//import StorytellerEntities.Events.RenameDocumentEvent;
//import StorytellerServer.ide.DocumentBuffer;
//import StorytellerServer.playback.PlaybackProxy;
//
//
///**
// * A superclass of anything that needs to render the JSON output by the server, like the mock browser.
// * 
// * subclasses will be in charge of getting the events once a connection has been established
// * @author Kevin
// *
// */
//public abstract class PlaybackEventRenderer
//{
//	//list of all events in the playback
//	private List<JSONObject> eventsToRender;
//
//	//position of the current event being handled in the playback
//	private int currentEventIndex = 0;
//
//	//A tree structure (see lower in this class) for modeling the nodes
//	private DefaultTreeModel treeOfNodes = null;
//	//The id of the "current" node
//	private String currentNode = null;
//
//	private PlaybackDocumentRenderer documentSetToRender = null;
//
//	//for communicating with the playback proxy
//	private int playbackProxyPort = PlaybackProxy.PLAYBACK_PROXY_PORT;
//	private Socket socketToPlaybackProxy;	
//	protected DataOutputStream outToPlaybackProxy;
//	protected BufferedReader inFromPlaybackProxy;
//
//	protected String pathToServer;
//	
//	private String sessionId = null;
//	
//	private Logger logger = Logger.getLogger(getClass().getName());
//	private Logger timer = Logger.getLogger("timing."+getClass().getName());
//
//	public PlaybackEventRenderer(String pathToServer)
//	{
//		this.pathToServer = pathToServer;
//
//
//	}
//	//==========================================Rendering Related Start==========================================================
//	/**
//	 * Allows subclasses to filter out certain events from the playback if needed
//	 * @param event
//	 * @return
//	 */
//	public boolean shouldRenderEvent(JSONObject event) throws JSONException
//	{
//		if (event!=null)
//			return true;
//		return false;
//	}
//
//	/**
//	 * Renders a given amount of events.  Any documents and directories that were affected by the events will
//	 * be updated with properly rendered objects.
//	 * @param step a positive number of events to render
//	 * @return
//	 */
//	public boolean playForward(int step)
//	{
//		//feed the events into the playback
//		for(int i = 0;(i < step) && (currentEventIndex < eventsToRender.size());i++)
//		{
//			//grab the current event from the list
//			JSONObject event = eventsToRender.get(currentEventIndex);	
//			try
//			{
//				if (!(shouldRenderEvent(event)))
//				{
//					logger.debug("Skipping "+event.toString());
//					i--;
//					currentEventIndex++;
//					continue;		//skip this event rendering, but render another event in its place
//				}
//			}
//			catch (Exception e) 
//			{
//				logger.fatal("There must have been a problem with parsing. "+event.toString(),e);
//				throw new RuntimeException(e);
//			}
//			timer.trace("Starting rendering of "+event.toString());
//
//			String type;
//			try 
//			{
//				type = event.getString(TYPE);
//			} 
//			catch (JSONException e) 
//			{
//				logger.fatal("Hey!  There was a problem with "+event.toString()+" - it doesn't have a type!",e);
//				throw new RuntimeException(e);				//If we get a problem here, throw it.  StoryTellerEventRenderer is used only in unit tests,
//				//and we want any bad JSON problems here to fail and end the test.
//			}
//
//			try 
//			{				
//				if(type.equals(CreateDirectoryEvent.CREATE_DIRECTORY_EVENT_TYPE))
//				{
//					addCreateDirectoryEvent(event);
//				}
//				else if(type.equals(CreateDocumentEvent.CREATE_DOCUMENT_EVENT_TYPE))
//				{
//					addCreateDocumentEvent(event);
//				}
//				else if(type.equals(DeleteDirectoryEvent.DELETE_DIRECTORY_EVENT_TYPE))
//				{
//					addDeleteDirectoryEvent(event);
//				}
//				else if(type.equals(DeleteDocumentEvent.DELETE_DOCUMENT_EVENT_TYPE))
//				{
//					addDeleteDocumentEvent(event);
//				}
//				else if(type.equals(DeleteEvent.DELETE_EVENT_TYPE))
//				{
//					addDeleteEvent(event);
//				}
//				else if(type.equals(InsertEvent.INSERT_EVENT_TYPE))
//				{
//					addInsertEvent(event);
//				}
//				else if(type.equals(MoveDirectoryEvent.MOVE_DIRECTORY_EVENT_TYPE))
//				{
//					addMoveDirectoryEvent(event);
//				}
//				else if(type.equals(MoveDocumentEvent.MOVE_DOCUMENT_EVENT_TYPE))
//				{
//					addMoveDocumentEvent(event);
//				}
//				else if(type.equals(RenameDirectoryEvent.RENAME_DIRECTORY_EVENT_TYPE))
//				{
//					addRenameDirectoryEvent(event);
//				}
//				else if(type.equals(RenameDocumentEvent.RENAME_DOCUMENT_EVENT_TYPE))
//				{
//					addRenameDocumentEvent(event);
//				}
//				else if (type.equals(OpenNodeEvent.OPEN_NODE_EVENT_TYPE))
//				{	
//					addOpenNodeEvent(event);
//				}
//				else if (type.equals(CloseNodeEvent.CLOSE_NODE_EVENT_TYPE))
//				{
//					//do nothing
//				}
//				else if (type.equals(MergeEvent.MERGE_EVENT_TYPE)) 
//				{
//					addMergeNodeEvent(event);
//				}
//				else if (type.equals(AutomaticConflictEvent.AUTOMATIC_CONFLICT_EVENT_TYPE))
//				{
//					//do nothing special and let them render
//				}
//				else if (type.equals(ManualConflictEvent.MANUAL_CONFLICT_EVENT_TYPE))
//				{
//					currentEventIndex+=addManualConflictEvent(event);			//skip forward over the blocks to see the two windows.  There's nothing to see here
//				}
//				else if (type.equals(ConflictResolutionEvent.CONFLICT_RESOLUTION_EVENT_TYPE))
//				{
//					//do nothing special and let the following events render
//				}
//			} 
//			catch (JSONException e) 
//			{
//				logger.fatal("Hey!  There was a problem with "+event.toString(),e);
//				throw new RuntimeException(e);				//If we get a problem here, throw it.  StoryTellerEventRenderer is used only in unit tests,
//				//and we want any bad JSON problems here to fail and end the test.
//			}
//			//move the current index forward
//			currentEventIndex++;
//			timer.trace("Finished rendering of "+event.toString());
//		}
//
//		//if there are more events that haven't been added to the playback
//		return currentEventIndex < (eventsToRender.size());
//	}
//
//	/**
//	 * Adds a manual conflict, which basically just skips over a certain number of events
//	 * @param event
//	 * @return
//	 */
//	@SuppressWarnings("static-method")
//	private int addManualConflictEvent(JSONObject event) throws JSONException
//	{
//		JSONArray idsOfFirstBlock = event.getJSONArray(IDS_OF_EVENTS_IN_FIRST_BLOCK);
//		JSONArray idsOfSecondBlockArray = event.getJSONArray(IDS_OF_EVENTS_IN_SECOND_BLOCK);
//
//		return idsOfFirstBlock.length()+idsOfSecondBlockArray.length();
//	}
//	
//	private void addMergeNodeEvent(JSONObject event) throws JSONException
//	{
//		addOpenNodeEvent(event);			//a mergeNodeEvent is exactly the same as an open Node
//		
//
//	}
//
//	/**
//	 * Parses the JSON for a CreateDirectoryEvent and renders it
//	 * @param event
//	 * @throws JSONException
//	 */
//	public void addCreateDirectoryEvent(JSONObject event) throws JSONException
//	{
//		//get the data from the create directory event
//		//id of the dir being created
//		String dirId = event.getString(DIRECTORY_ID);
//		//name of the new dir
//		String dirName = event.getString(NEW_NAME);
//		//id of the parent of the new dir
//		String parentDirId = event.getString(PARENT_DIRECTORY_ID);
//
//		//store the new dir info 
//		getDocumentSetToRender().getDirIdToDirNameMap().put(dirId, dirName);
//		getDocumentSetToRender().getDirNameToDirIdMap().put(dirName, dirId);
//
//		getDocumentSetToRender().getDirIdToParentDirIdMap().put(dirId, parentDirId);
//	}
//
//	/**
//	 * Parses the JSON for a CreateDocumentEvent and renders it
//	 * @param event
//	 * @throws JSONException
//	 */
//	public void addCreateDocumentEvent(JSONObject event) throws JSONException
//	{
//		//get the data from the create document event
//		//id of the doc being created
//		String docId = event.getString(DOCUMENT_ID);
//		//name of the new doc
//		String docName = event.getString(NEW_NAME);
//		//id of the parent of the new doc
//		String parentDirId = event.getString(PARENT_DIRECTORY_ID);
//
//		//store the new doc info 
//		getDocumentSetToRender().getDocumentBuffers().put(docId, new DocumentBuffer());
//
//		getDocumentSetToRender().getDocIdToDocNameMap().put(docId, docName);
//		getDocumentSetToRender().getDocNameToDocIdMap().put(docName, docId);
//
//		getDocumentSetToRender().getDocIdToParentDirIdMap().put(docId, parentDirId);		
//	}
//
//	/**
//	 * A helper method that recursively deletes everything in a directory
//	 * @param idOfDirectoryToDelete
//	 */
//	private void deleteDirectoryHelper(String idOfDirectoryToDelete)
//	{
//		//get the directory name (to be used to remove from the name map)
//		String dirName = getDocumentSetToRender().getDirIdToDirNameMap().get(idOfDirectoryToDelete);
//
//		//remove the deleted dir info 
//		getDocumentSetToRender().getDirIdToDirNameMap().remove(idOfDirectoryToDelete);
//		getDocumentSetToRender().getDirNameToDirIdMap().remove(dirName);
//
//		getDocumentSetToRender().getDirIdToParentDirIdMap().remove(idOfDirectoryToDelete);
//
//		//need to clone the keyset because docIDToParentDirIDMap will be changing as documents get removed.  The iterator does not like that
//		Set<String> cloneOfKeyset = new HashSet<String>(getDocumentSetToRender().getDocIdToParentDirIdMap().keySet());
//
//		for (String potentialDocumentIDs : cloneOfKeyset) 	//go through all documents created
//		{
//			//If a document's parentDirID is the one being deleted, we should delete it
//			if (getDocumentSetToRender().getDocIdToParentDirIdMap().get(potentialDocumentIDs).equals(idOfDirectoryToDelete))
//			{
//				deleteDocumentHelper(potentialDocumentIDs);
//			}
//		}
//
//		//now we will go through and recursively delete all subdirectories
//
//		//again, cloning the keyset because dirIdToParentDirIdMap will get modified
//		cloneOfKeyset = new HashSet<String>(getDocumentSetToRender().getDirIdToParentDirIdMap().keySet());		//go through all directories
//
//		for (String potentialDirectoryIDs : cloneOfKeyset) 
//		{
//			//If a directory's parentDirID is the one being deleted, we should delete it
//			if (getDocumentSetToRender().getDirIdToParentDirIdMap().get(potentialDirectoryIDs).equals(idOfDirectoryToDelete))
//			{
//				deleteDirectoryHelper(potentialDirectoryIDs);		//recursively delete all sub directories
//			}
//		}
//
//	}
//
//
//	/**
//	 * Parses the JSON for a DeleteDirectoryEvent and renders it, using a recursive helper
//	 * @param event
//	 * @throws JSONException
//	 */
//	public void addDeleteDirectoryEvent(JSONObject event) throws JSONException
//	{
//		//get the data from the delete directory event
//		//id of the dir being deleted
//		String dirId = event.getString(DIRECTORY_ID);
//
//
//		deleteDirectoryHelper(dirId);
//
//	}
//
//	/**
//	 * Removes all traces of this document
//	 * @param idOfDocumentToDelete
//	 */
//	private void deleteDocumentHelper(String idOfDocumentToDelete)
//	{
//
//		//name of the document (to be used to remove from the name map)
//		String docName = getDocumentSetToRender().getDocIdToDocNameMap().get(idOfDocumentToDelete);
//
//		//remove the deleted doc info 
//		getDocumentSetToRender().getDocumentBuffers().remove(idOfDocumentToDelete);
//
//		getDocumentSetToRender().getDocIdToDocNameMap().remove(idOfDocumentToDelete);
//		getDocumentSetToRender().getDocNameToDocIdMap().remove(docName);
//
//		getDocumentSetToRender().getDocIdToParentDirIdMap().remove(idOfDocumentToDelete);
//	}
//
//	/**
//	 * Parses the JSON for a DeleteDocumentEvent and renders it using a helper
//	 * @param event
//	 * @throws JSONException
//	 */
//	public void addDeleteDocumentEvent(JSONObject event) throws JSONException
//	{
//		//get the data from the delete document event
//		//id of the doc being deleted
//		String docId = event.getString(DOCUMENT_ID);
//
//		deleteDocumentHelper(docId);
//
//	}
//
//	/**
//	 * Parses the JSON for an AddDeleteEvent and renders it, deleting the appropriate TextEvent
//	 * @param event
//	 * @throws JSONException
//	 */
//	public void addDeleteEvent(JSONObject event) throws JSONException
//	{
//		//get the data from the delete event
//		//id of the doc to add to
//		String docId = event.getString(DOCUMENT_ID);
//
//		//get the id of the textEvent to delete
//		String idOfTextEventToDelete = event.getString(PREVIOUS_NEIGHBOR_ID);
//
//		//get the buffer referred to by docID
//		DocumentBuffer buffer = getDocumentSetToRender().getDocumentBuffers().get(docId);
//
//		buffer.addDeleteEvent(idOfTextEventToDelete);
//
//	}
//
//	/**
//	 * Parses the JSON for an AddInsertEvent and renders it, putting the new TextEvent where it should be
//	 * @param event
//	 * @throws JSONException
//	 */
//	public void addInsertEvent(JSONObject event) throws JSONException
//	{
//		//get the data from the insert event
//		//id of the doc to add to
//		String docId = event.getString(DOCUMENT_ID);
//
//		//data to add to the document
//		String textToAdd = event.getString(EVENT_DATA);
//
//		//get the id of this text event
//		String thisID = event.getString(ID);
//
//		//get the id of the previousNeighbor
//		String idOfPreviousNeighbor = event.getString(PREVIOUS_NEIGHBOR_ID);
//		if (idOfPreviousNeighbor.equals("null")||idOfPreviousNeighbor.equals(""))			//since JSON can't actually be null, we need to change from fake null to actual null
//		{
//			idOfPreviousNeighbor=null;
//		}
//
//		//get the buffer referred to by docID
//		DocumentBuffer buffer = getDocumentSetToRender().getDocumentBuffers().get(docId);
//
//		//add the text event to the buffer
//		buffer.addTextEvent(thisID, idOfPreviousNeighbor, textToAdd);
//
//	}
//
//	/**
//	 * Parses the JSON for a MoveDirectoryEvent and renders it
//	 * @param event
//	 * @throws JSONException
//	 */
//	public void addMoveDirectoryEvent(JSONObject event) throws JSONException
//	{
//		//get the data from the move directory event
//		//id of the dir being moved
//		String dirId = event.getString(DIRECTORY_ID);
//		//id of the new parent dir
//		String newParentDirId = event.getString(DESTINATION_DIRECTORY_ID);
//
//		//store the new dir info 
//		//remove the old association
//		getDocumentSetToRender().getDirIdToParentDirIdMap().remove(dirId);
//		//add the new association
//		getDocumentSetToRender().getDirIdToParentDirIdMap().put(dirId, newParentDirId);		
//	}
//
//	/**
//	 * Parses the JSON for a MoveDocumentEvent and renders it
//	 * @param event
//	 * @throws JSONException
//	 */
//	public void addMoveDocumentEvent(JSONObject event) throws JSONException
//	{
//		//get the data from the move doc event
//		//id of the doc being moved
//		String docId = event.getString(DOCUMENT_ID);
//		//id of the new parent dir
//		String newParentDirId = event.getString(DESTINATION_DIRECTORY_ID);
//
//		//store the new doc info 
//		//remove the old association
//		getDocumentSetToRender().getDocIdToParentDirIdMap().remove(docId);
//		//add the new association
//		getDocumentSetToRender().getDocIdToParentDirIdMap().put(docId, newParentDirId);				
//	}
//
//	/**
//	 * 
//	 * @param event
//	 * @throws JSONException
//	 */
//	private void addOpenNodeEvent(JSONObject event) throws JSONException
//	{
//		logger.trace(event);
//		//If no nodes have been made thus far, start the tree with this freshly opened node as the root
//		if (treeOfNodes==null)
//		{
//			setCurrentNode(event.getString(CREATED_UNDER_NODE_ID));
//			treeOfNodes=new DefaultTreeModel(new DefaultMutableTreeNode(getCurrentNode()));
//		}
//		else
//		{
//			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(event.getString(CREATED_UNDER_NODE_ID));
//
//			//Go find the node that this new node branched off of
//			MutableTreeNode parent = getChildWithValue((MutableTreeNode) treeOfNodes.getRoot(), event.getString(SEQUENTIALLY_BEFORE_NODE_ID));
//			parent.insert(newNode, 0);
//
//			//update current Node
//			setCurrentNode(event.getString(CREATED_UNDER_NODE_ID));
//
//		}
//
//	}
//
//	/**
//	 * Parses the JSON for a RenameDirectoryEvent and renders it
//	 * @param event
//	 * @throws JSONException
//	 */
//	public void addRenameDirectoryEvent(JSONObject event) throws JSONException
//	{
//		//get the data from the rename directory event
//		//id of the dir being renamed
//		String dirId = event.getString(DIRECTORY_ID);
//		//new name of the dir
//		String newDirName = event.getString(NEW_NAME);
//		//old name of dir
//		String oldDirName = getDocumentSetToRender().getDirIdToDirNameMap().get(dirId);
//
//		//store the new dir info 
//		getDocumentSetToRender().getDirIdToDirNameMap().remove(dirId);
//		getDocumentSetToRender().getDirIdToDirNameMap().put(dirId, newDirName);
//
//		getDocumentSetToRender().getDirNameToDirIdMap().remove(oldDirName);		
//		getDocumentSetToRender().getDirNameToDirIdMap().put(newDirName, dirId);		
//	}
//
//	/**
//	 * Parses the JSON for a RenameDocumentEvent and renders it
//	 * @param event
//	 * @throws JSONException
//	 */
//	public void addRenameDocumentEvent(JSONObject event) throws JSONException
//	{
//		//get the data from the rename doc event
//		//id of the doc being renamed
//		String docId = event.getString(DOCUMENT_ID);
//		//new name of the doc
//		String newDocName = event.getString(NEW_NAME);
//		//old name of the document
//		String oldDocName = getDocumentSetToRender().getDocIdToDocNameMap().get(docId);
//
//		//store the new doc info 
//		getDocumentSetToRender().getDocIdToDocNameMap().remove(docId);
//		getDocumentSetToRender().getDocIdToDocNameMap().put(docId, newDocName);
//
//		getDocumentSetToRender().getDocNameToDocIdMap().remove(oldDocName);		
//		getDocumentSetToRender().getDocNameToDocIdMap().put(newDocName, docId);				
//	}
//
//	//================================================Rendering Related End==============================================================
//
//	//================================================Get Status Related Start===========================================================
//	
//	/**
//	 * Returns if a document by a given documentID exists
//	 * @param docID
//	 * @return
//	 */
//	public boolean hasDocument(String docID)
//	{
//		return getDocumentSetToRender().getDocumentBuffers().containsKey(docID);
//	}
//	/**
//	 * Returns the text of a given Document, based on the DocumentID
//	 * @param docId
//	 * @return
//	 */
//	public String getDocumentText(String docId)
//	{
//		//return the text associated with a document id
//		if (getDocumentSetToRender().getDocumentBuffers().get(docId)!=null)
//			return getDocumentSetToRender().getDocumentBuffers().get(docId).toString();
//		return null;
//	}
//
//	/**
//	 * Returns all the names of subdirectories in a given parent directory
//	 * @param parentDirId
//	 * @return
//	 */
//	public List < String > getAllSubdirectoryNames(String parentDirId)
//	{
//		//create a container to hold the names of subdirectory ids 
//		List < String > subDirIds = new ArrayList < String >();
//		//create a container to hold the names of subdirectory names
//		List < String > subDirNames = new ArrayList < String >();
//
//		//for all the dir ids in the dir id to parent dir map
//		for(String potentialDirId : getDocumentSetToRender().getDirIdToParentDirIdMap().keySet())
//		{
//			//if the dir's parent is the same as the passed in one
//			if(getDocumentSetToRender().getDirIdToParentDirIdMap().get(potentialDirId).equals(parentDirId))
//			{
//				//this dir is in the passed in dir
//				subDirIds.add(potentialDirId);
//			}
//		}
//
//		//for all the dir ids we know are in the passed in dir
//		for(String subDirId : subDirIds)
//		{
//			//add the name of the dir to the list
//			subDirNames.add(getDocumentSetToRender().getDirIdToDirNameMap().get(subDirId));
//		}
//
//		return subDirNames;
//	}
//	
//	public String getSessionId()
//	{
//		return sessionId;
//	}
//
//	public void setSessionId(String sessionId)
//	{
//		this.sessionId = sessionId;
//	}
//	
//	public String getIdOfEvent(String docIDToLookIn, int index)
//	{
//		return getDocumentSetToRender().getDocumentBuffers().get(docIDToLookIn).idOfEventAt(index);
//	}
//
//	public JSONObject getJSONObjectByEventID(String id) throws JSONException
//	{
//		for(JSONObject jobj: eventsToRender)
//		{
//			if (jobj.getString(ID).equals(id))
//			{
//				return jobj;
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * Returns the names of all documents in a given directory
//	 * @param dirId
//	 * @return
//	 */
//	public List < String > getAllDocumentNames(String dirId)
//	{
//		//create a container to hold the ids of documents in a dir
//		ArrayList < String > docIds = new ArrayList < String >();
//
//		//create a container to hold the names of documents in a dir
//		ArrayList < String > docNames = new ArrayList < String >();
//
//		//for all the doc ids in the doc id to parent dir map
//		for(String docId : getDocumentSetToRender().getDocIdToParentDirIdMap().keySet())
//		{
//			//if the doc's parent is the same as the passed in one
//			if(getDocumentSetToRender().getDocIdToParentDirIdMap().get(docId).equals(dirId))
//			{
//				//this doc is in the passed in dir
//				docIds.add(docId);
//			}
//		}
//
//		//for all the doc ids we know are in the passed in dir
//		for(String docId : docIds)
//		{
//			//add the name of the doc to the list
//			docNames.add(getDocumentSetToRender().getDocIdToDocNameMap().get(docId));
//		}
//
//		return docNames;
//	}
//
//	public String getDirectoryId(String dirName)
//	{
//		return getDocumentSetToRender().getDirNameToDirIdMap().get(dirName);
//	}
//
//	public boolean directoryExists(String dirId)
//	{
//		return getDocumentSetToRender().getDirIdToDirNameMap().containsKey(dirId);
//	}
//
//	public boolean documentExists(String docId)
//	{
//		return getDocumentSetToRender().getDocIdToDocNameMap().containsKey(docId);
//	}
//
//	public String getDocumentName(String docId)
//	{
//		return getDocumentSetToRender().getDocIdToDocNameMap().get(docId);
//	}
//
//	public String getDocumentId(String docName)
//	{
//		return getDocumentSetToRender().getDocNameToDocIdMap().get(docName);
//	}
//
//	public PlaybackDocumentRenderer getDocumentSetToRender()
//	{
//		return documentSetToRender;
//	}
//	public void setDocumentSetToRender(PlaybackDocumentRenderer documentSetToRender)
//	{
//		this.documentSetToRender = documentSetToRender;
//	}
//	public String getDirectoryName(String dirId)
//	{
//		return getDocumentSetToRender().getDirIdToDirNameMap().get(dirId);
//	}
//
//	/**
//	 * return the tree of nodes that is created by playback
//	 * @return
//	 */
//	public DefaultTreeModel getRenderedTreeOfNodes()
//	{
//		return treeOfNodes;
//	}
//
//	/**
//	 * Pings the playback server for the tree of nodes and renders it
//	 * @return
//	 */
//	public abstract DefaultTreeModel getExpectedTreeOfNodes();
//
//
//	public String getCurrentNode()
//	{
//		return currentNode;
//	}
//	public void setCurrentNode(String currentNode)
//	{
//		this.currentNode = currentNode;
//	}
//	/**
//	 * A helper for walking through the tree of a MutableTreeNode looking for a value
//	 * @param root
//	 * @param value
//	 * @return
//	 */
//	public MutableTreeNode getChildWithValue(MutableTreeNode root, String value)
//	{
//		if (root.toString().equals(value))
//		{
//			return root;				//it's this node
//		}
//		if (root.getChildCount()==0)
//		{
//			return null;				//it is not this node
//		}
//		for(int i=0;i<root.getChildCount();i++)
//		{
//			MutableTreeNode childWithValue = getChildWithValue((MutableTreeNode) root.getChildAt(i), value);
//			if (childWithValue!=null)
//			{
//				return childWithValue;		//there it is!
//			}
//		}
//		return null;			//I couldn't find it
//	}
//
//	public void setEventsToRender(List<JSONObject> events) 
//	{
//		this.eventsToRender = events;
//	}
//	
//	public List<JSONObject> getEventsToRender()
//	{
//		return eventsToRender;
//	}
//
//
//
//	//========================================Status Related End =============================================================
//
//	public int getCurrentEventIndex()
//	{
//		return currentEventIndex;
//	}
//	public void setCurrentEventIndex(int currentEventIndex)
//	{
//		this.currentEventIndex = currentEventIndex;
//	}
//	//========================================Utility Functions Start=========================================================
//	/**
//	 * A helping method that gets a user readable implementation of this tree
//	 * @param tree
//	 * @return
//	 */
//	public static String treeModelToString(DefaultTreeModel tree)
//	{
//		StringBuilder builder = new StringBuilder();
//		builder.append("Root: ");
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//		builder.append(root.toString());
//		builder.append("\n");
//		for(int i=0;i<root.getChildCount();i++)
//		{
//			MutableTreeNode child =(MutableTreeNode) root.getChildAt(i);
//			builder.append(printTreeHelper(child, 1));
//		}
//		return builder.toString();
//	}
//
//	private static String printTreeHelper(MutableTreeNode node, int numTabs)
//	{
//		StringBuilder builder = new StringBuilder();
//		for(int i = 0;i<numTabs;i++)
//		{
//			builder.append("\t");	
//		}
//		builder.append("Node: ");
//		builder.append(node.toString());
//		builder.append("   Parent: ");
//		builder.append(node.getParent().toString());
//		builder.append("\n");
//		for(int i=0;i<node.getChildCount();i++)
//		{
//			MutableTreeNode child =(MutableTreeNode) node.getChildAt(i);
//			builder.append(printTreeHelper(child, numTabs+1));
//		}
//		return builder.toString();
//	}
//	
//
//
//	//==========================================Network Related===========================================================
//	
//	/**
//	 * Makes a connection to the PlaybackProxy on the set Port and path
//	 */
//	protected void createSocketToPlaybackProxy()
//	{
//		try
//		{
//	        //create a socket connection with a playback server on another machine
//			socketToPlaybackProxy = new Socket(pathToServer, playbackProxyPort);
//	
//	        //we can use outToPlaybackProxy to write to the socket
//	        outToPlaybackProxy = new DataOutputStream(socketToPlaybackProxy.getOutputStream());
//	
//	        //we can use inFromPlayback to read in to the socket
//	        inFromPlaybackProxy = new BufferedReader(new InputStreamReader(socketToPlaybackProxy.getInputStream()));
//		}
//		catch(IOException ex)
//		{
//			logger.fatal("",ex);
//		}
//	}
//	/**
//	 * Closes the socket (Freeing it up for other classes to use) 
//	 */
//	protected void closeSocketToPlaybackProxy()
//	{
//		try
//		{
//	        //close the socket after we are done with it
//			socketToPlaybackProxy.close();
//		}
//		catch(IOException ex)
//		{
//			logger.fatal("",ex);
//		}
//	}
//	
//	
//
//
//
//
//}
