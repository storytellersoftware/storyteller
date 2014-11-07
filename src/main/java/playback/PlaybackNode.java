package playback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.StorytellerCore;
import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.entities.DeveloperGroup;
import core.entities.ExtendedDirectory;
import core.entities.ExtendedDocument;
import core.events.CreateDirectoryEvent;
import core.events.CreateDocumentEvent;
import core.events.DeleteDirectoryEvent;
import core.events.DeleteDocumentEvent;
import core.events.DeleteEvent;
import core.events.InsertEvent;
import core.events.MoveDirectoryEvent;
import core.events.MoveDocumentEvent;
import core.events.RenameDirectoryEvent;
import core.events.RenameDocumentEvent;
import core.events.StorytellerEvent;

/**
 * A playback node is a loose representation of a (possibly partial) node. It 
 * exists to help answer questions about events in a node during a playback. 
 * PlaybackSessions hold a collection of these playback nodes that represent 
 * something that can be played back. 
 * 
 * During a playback a developer can stop at any point and decide to branch from 
 * the middle of a node. Let's say node 1 had 200 events. However, in the middle 
 * of a playback a developer can chose to create a new node directly after event 
 * 100. Now, when playing back node 2 we would like to see all the events in node 
 * 2 but only the first 100 events in node 1.
 * 
 * Node 1
 *  ev 1
 *  ev 2
 *  ...
 *  ev 100 
 *  ..     \
 *  ev 199  \
 *  ev 200   \
 *            Node 2
 *             ev 1
 *             ev 2
 *             ...
 *             
 * Node 1: ev 1, ev 2, ... ev 100, Node 2: ev 1, ev2, ...            
 * 
 * This class allows us to reconstruct only the relevant parts of a node for a 
 * playback. We need a way to be able to tell which part of a lineage of nodes 
 * should be used (events 101-200 from node 1, for example, should not be used 
 * in a playback of node 2). 
 */
public class PlaybackNode
{
	//holds the id of the node
	protected String nodeId;
	
	//holds the first event in the node (used to find the parent node and last event 
	//in it by looking at the sequentially before id)
	private StorytellerEvent firstEvent;
	
	//holds the last event in the node (not necessarily the very last event added to 
	//a node if someone created a new node in the middle of a playback)
	private StorytellerEvent lastEvent;
	
	//database to use for node
	private DBAbstraction database;
	
	/**
	 * Creates a single playback node. Pass in a database object for access to data, 
	 * a node id that this playback node is representing, the first event in the node, 
	 * and the last relevant event in the node (this is not necessarily the last event 
	 * in the node but it is the last one we are interested in for playback purposes).
	 */
	public PlaybackNode(DBAbstraction db, String nodeId, StorytellerEvent first, StorytellerEvent last)
	{
		//store the node id
		setNodeId(nodeId);

		//first and last relevant events in this playback node
		setFirstEvent(first);
		setLastEvent(last);

		//store a reference to the database
		setDatabase(db);
	}

	/**
	 * Creates a single playback node. Pass in a database object for access to data, 
	 * a node id that this playback node is representing, and the last relevant event 
	 * in the node (this is not necessarily the last event in the node but it is the 
	 * last one we are interested in for playback purposes). 
	 * 
	 * The first event in the node will be retrieved from the database
	 */
	public PlaybackNode(DBAbstraction db, String nodeId, StorytellerEvent last) throws DBAbstractionException
	{
		//get the first event in the passed in node		
		this(db, nodeId, db.getFirstEventInNode(nodeId), last);
	}
	
	/**
	 * This method will get all the events from the passed in sequence number 
	 * until the end of the node. 
	 * 
	 * A list of acceptable document ids is passed in. If there are doc ids on the 
	 * list then only events in these documents will be retrieved.
	 */
	public List<StorytellerEvent> getAllEventsFromAnEventUntilTheEndOfTheNode(int startEventSeqNum, List<String> acceptableDocIds) throws DBAbstractionException
	{
		//return all the events in this node from the sequence number to the end the list of all events
		return getDatabase().getEventsInNodeFromOneEventToAnother(getNodeId(), startEventSeqNum, getLastEvent().getNodeSequenceNum(), acceptableDocIds);		
	}

	/**
	 * This method will get all the events from the beginning of the node until 
	 * the the passed in sequence number. 
	 * 
	 * A list of acceptable document ids is passed in. If there are doc ids on the 
	 * list then only events in these documents will be retrieved.
	 */
	public List<StorytellerEvent> getAllEventsUpToAnEvent(int lastEventSeqNumber, List<String> acceptableDocIds) throws DBAbstractionException
	{
		//return all the events in this node up to the sequence number to the list of all events
		return getDatabase().getEventsByNodeAndSequenceNumber(getNodeId(), lastEventSeqNumber, acceptableDocIds);
	}

	/**
	 * This method will get all the events between the two passed in sequence numbers 
	 * 
	 * A list of acceptable document ids is passed in. If there are doc ids on the 
	 * list then only events in these documents will be retrieved.
	 */
	public List<StorytellerEvent> getAllEventsFromOneEventToAnother(int startEventSeqNum, int endEventSeqNum, List<String> acceptableDocIds) throws DBAbstractionException
	{
		//return all the events in this node from the start sequence number to the end sequence number
		return getDatabase().getEventsInNodeFromOneEventToAnother(getNodeId(), startEventSeqNum, endEventSeqNum, acceptableDocIds);		
	}

	/**
	 * This method will get all the events in the node.
	 */
	public List<StorytellerEvent> getAllEvents() throws DBAbstractionException
	{
		//return all the events in this node up to the sequence number to the list of all events
		return getDatabase().getEventsByNodeAndSequenceNumber(getNodeId(), getLastEvent().getNodeSequenceNum(), null);
	}

	/**
	 * This method will get all the events in the node.
	 * 
	 * A list of acceptable document ids is passed in. If there are doc ids on the 
	 * list then only events in these documents will be retrieved.
	 */
	public List<StorytellerEvent> getAllEvents(List<String> acceptableDocIds) throws DBAbstractionException
	{
		//return all the events in this node up to the sequence number to the list of all events
		return getDatabase().getEventsByNodeAndSequenceNumber(getNodeId(), getLastEvent().getNodeSequenceNum(), acceptableDocIds);
	}
	
	/**
	 * Gets the first relevant event in this node based on some filter information.
	 * 
	 * A list of acceptable document and dev group ids are passed in. If there are 
	 * doc/dev group ids on the list then only events in these documents will be 
	 * retrieved.
	 */
	public StorytellerEvent getFirstRelevantEvent(long startTime, long endTime, List<String> acceptableDocIds, List<String> developerGroupIDs) throws DBAbstractionException
	{
		//get the first relevant event in the node based on start time, end time, developer group ids, and doc ids
		return getDatabase().getFirstRelevantEventInNode(getNodeId(), getLastEvent().getNodeSequenceNum(), startTime, endTime, acceptableDocIds, developerGroupIDs);
	}

	/**
	 * Gets the first relevant event in this node from among the passed in selected
	 * and relevant event ids. The passed in event ids are strings so we have to 
	 * retrieve the events from the database.
	 */
	public StorytellerEvent getFirstRelevantEvent(List<String> selectedAndRelevantEventIds) throws DBAbstractionException
	{
		//get the first relevant event in the node based on the passed in ids
		return getDatabase().getFirstRelevantEventInNode(getNodeId(), getLastEvent().getNodeSequenceNum(), selectedAndRelevantEventIds);
	}

	/**
	 * Gets the last relevant event in this node based on some filter information.
	 * 
	 * A list of acceptable document and dev group ids are passed in. If there are 
	 * doc/dev group ids on the list then only events in these documents will be 
	 * retrieved.
	 */
	public StorytellerEvent getLastRelevantEvent(long startTime, long endTime, List<String> acceptableDocIds, List<String> developerGroupIDs) throws DBAbstractionException
	{
		//get the last relevant event in the node based on start time, end time, devloper group ids, and doc ids
		return getDatabase().getLastRelevantEventInNode(getNodeId(), getLastEvent().getNodeSequenceNum(), startTime, endTime, acceptableDocIds, developerGroupIDs);
	}

	/**
	 * Gets any insert events in this node that back up to an event id on the list 
	 * idsOfEventsToLookForBackLinksTo. The event ids on the list idsOfEventsToIgnore 
	 * can be ignored (they are most likely already accounted for and can be). 
	 */
	public List< InsertEvent > getInsertEventsThatBackUpToEvents(ArrayList<String> idsOfEventsToLookForBackLinksTo, ArrayList<String> idsOfEventsToIgnore) throws DBAbstractionException
	{
		//get the inserts that back up to the event ids passed in
		return getDatabase().getInsertEventsThatBackUpToEventsByNode(getNodeId(), getLastEvent().getNodeSequenceNum(), idsOfEventsToLookForBackLinksTo, idsOfEventsToIgnore);
	}

	/**
	 * Looks in this node to see if there is a delete event that deleted the 
	 * passed in insert event.
	 */
	public DeleteEvent getDeleteEventForAnInsertEvent(InsertEvent insertEvent) throws DBAbstractionException
	{
		//look for a delete of the insert event
		return getDatabase().getDeleteEventForAnInsertEvent(getNodeId(), getLastEvent().getNodeSequenceNum(), insertEvent);
	}

	/**
	 * Takes in a set of dev groups and adds any distinct dev groups that have 
	 * contributed in this playback node that are not already in the set. The 
	 * passed in set is used to collect dev groups in a lineage of playback nodes. 
	 */
	public void getDeveloperGroupsWhoContributed(Set<DeveloperGroup> devGroups) throws DBAbstractionException
	{
		//get all the developer groups who contributed at least one event in this playback node
		List < DeveloperGroup > devGroupsInANode = getDatabase().getAllDeveloperGroupsInANode(getNodeId(), getLastEvent().getNodeSequenceNum());
		
		//go through and add unique dev groups to the passed in set
		for(DeveloperGroup devGroup : devGroupsInANode)
		{
			devGroups.add(devGroup);
		}
	}

	/**
	 * Gets all the ids of deleted events in this node. All the delete events in 
	 * this node are examined and their previous neighbor id is added to the set 
	 * (for quick search later on). The passed in set collects these ids for use 
	 * in a lineage of playback nodes.
	 */
	public void getDeletedEventsIds(Set<String> allDeletedInsertEventsIds) throws DBAbstractionException
	{
		//get all the delete events in this node
		List < StorytellerEvent > deleteEvents = getDatabase().getEventsByType(getNodeId(), getLastEvent().getNodeSequenceNum(), DeleteEvent.DELETE_EVENT_TYPE);

		//go through the delete events in this node
		for(StorytellerEvent event : deleteEvents)
		{
			//convert into a real delete event
			DeleteEvent deleteEvent = (DeleteEvent)event;
			
			//add the event's prev neighbor (the id of the insert being deleted) to the set
			allDeletedInsertEventsIds.add(deleteEvent.getPreviousNeighborEventId());
		}
	}
	
	/**
	 * Gets all the insert events that were not deleted in this (or previous) nodes. 
	 */
	public void getInsertsThatWereNotDeleted(Map < String, List < InsertEvent > > allInserts , Set<String> allDeletedInsertEventsIds) throws DBAbstractionException
	{
		//get all the insert events in this node
		List < StorytellerEvent > insertEvents = getDatabase().getEventsByType(getNodeId(), getLastEvent().getNodeSequenceNum(), InsertEvent.INSERT_EVENT_TYPE);

		//go through the insert events in this node
		for(StorytellerEvent event : insertEvents)
		{
			//convert into a real insert event
			InsertEvent insertEvent = (InsertEvent)event;
			
			//if this insert event was not deleted
			if(!allDeletedInsertEventsIds.contains(insertEvent.getId()))
			{
				//if there is not a place on the map for this event's document
				if(!allInserts.containsKey(insertEvent.getDocumentId()))
				{
					//add a new list for this doc 
					allInserts.put(insertEvent.getDocumentId(), new ArrayList < InsertEvent >());
				}
				
				//this event was never deleted in the node lineage, add it to the doc's list of events
				allInserts.get(insertEvent.getDocumentId()).add(insertEvent);
			}
			else //we have found an insert event that was deleted
			{
				//we don't need the insert event id around anymore
				allDeletedInsertEventsIds.remove(insertEvent.getId());
			}
		}		
	}

	/**
	 * Gets all the directories in this node. It handles creates, renames, moves, 
	 * and delete directory events.  
	 */
	public void getAllDirectories(Map<String, ExtendedDirectory> allDirs, boolean withDeletedDocsAndDirs) throws DBAbstractionException
	{
		//get all the create directory events in this node
		List < StorytellerEvent > createDirectoryEvents = getDatabase().getEventsByType(getNodeId(), getLastEvent().getNodeSequenceNum(), CreateDirectoryEvent.CREATE_DIRECTORY_EVENT_TYPE);
		//create new extended directory objects for each event
		handleCreateDirectoryEvents(allDirs, createDirectoryEvents);

		//get all the rename directory events in this node
		List < StorytellerEvent > renameDirectoryEvents = getDatabase().getEventsByType(getNodeId(), getLastEvent().getNodeSequenceNum(), RenameDirectoryEvent.RENAME_DIRECTORY_EVENT_TYPE);
		//renames extended directory objects when the name changes
		handleRenameDirectoryEvents(allDirs, renameDirectoryEvents);
		
		//get all the move directory events in this node
		List < StorytellerEvent > moveDirectoryEvents = getDatabase().getEventsByType(getNodeId(), getLastEvent().getNodeSequenceNum(), MoveDirectoryEvent.MOVE_DIRECTORY_EVENT_TYPE);
		//adjusts the parent/child relationships in the extended directories when a move occurs
		handleMoveDirectoryEvents(allDirs, moveDirectoryEvents);

		//get all the delete directory events in this node
		List < StorytellerEvent > deleteDirectoryEvents = getDatabase().getEventsByType(getNodeId(), getLastEvent().getNodeSequenceNum(), DeleteDirectoryEvent.DELETE_DIRECTORY_EVENT_TYPE);
		//removes extended directories or marks them as being deleted
		handleDeleteDirectoryEvents(allDirs, withDeletedDocsAndDirs, deleteDirectoryEvents);
	}

	/**
	 * This method takes in a list of create directory events and creates extended 
	 * directories in the map of all directories 
	 */
	private void handleCreateDirectoryEvents(Map<String, ExtendedDirectory> allDirs, List<StorytellerEvent> createDirectoryEvents)
	{
		//for each create directory event add an extended directory to the directory map
		for(StorytellerEvent event : createDirectoryEvents)
		{
			//turn the event into a real create directory event
			CreateDirectoryEvent createDirEvent = (CreateDirectoryEvent)event;
			
			//create a new extended directory
			ExtendedDirectory newDir = new ExtendedDirectory(createDirEvent.getDirectoryId(), createDirEvent.getTimestamp(), createDirEvent.getCreatedUnderNodeId(), createDirEvent.getCreatedByDevGroupId(), createDirEvent.getParentDirectoryId(), createDirEvent.getDirectoryNewName());
			
			//add it to the map of all directories created in this node
			allDirs.put(newDir.getId(), newDir);
		}
		
		//now that all the dir's are in the map set up the parent/child links
		for(StorytellerEvent event : createDirectoryEvents)
		{
			//turn the event into a real create directory event
			CreateDirectoryEvent createDirEvent = (CreateDirectoryEvent)event;

			//get the new dir
			ExtendedDirectory newDir = allDirs.get(createDirEvent.getDirectoryId());
			
			//now add the parent/child relationships to the new dirs
			//if there is a parent (the root dir does not have a parent)
			if(newDir.getParentDirectoryId() != null && !newDir.getParentDirectoryId().isEmpty())
			{
				//get the parent directory of dir
				ExtendedDirectory parent = allDirs.get(newDir.getParentDirectoryId());
				
				//add the dir to its parent
				parent.addDirectory(newDir);
				
				//set the parent directory
				newDir.setParentDirectory(parent);
			}
		}
	}

	/**
	 * This method takes in a list of rename directory events and updates the map
	 * of all directories 
	 */
	private void handleRenameDirectoryEvents(Map<String, ExtendedDirectory> allDirs, List<StorytellerEvent> renameDirectoryEvents)
	{
		//for each rename directory event update the name in the directory map 
		for(StorytellerEvent event : renameDirectoryEvents)
		{
			//create a real rename directory event
			RenameDirectoryEvent renameDirEvent = (RenameDirectoryEvent)event;
			
			//get the directory id that this event refers to
			String dirId = renameDirEvent.getDirectoryId();
			
			//get the renamed dir
			ExtendedDirectory renameDir = allDirs.get(dirId); 

			//change the name of the directory
			renameDir.setName(renameDirEvent.getDirectoryNewName());
		}
	}

	/**
	 * This method takes in a list of move directory events and updates the map
	 * of all directories 
	 */
	private void handleMoveDirectoryEvents(Map<String, ExtendedDirectory> allDirs, List<StorytellerEvent> moveDirectoryEvents)
	{
		//for each move directory event update the parent id in the directory map 
		for(StorytellerEvent event : moveDirectoryEvents)
		{
			//create a real move directory event
			MoveDirectoryEvent moveDirEvent = (MoveDirectoryEvent)event;
			
			//get the directory id that this event refers to
			String dirId = moveDirEvent.getDirectoryId();

			//get the moved directory
			ExtendedDirectory movedDir = allDirs.get(dirId);
			
			//remove the moved directory from its old parent's list of children
			movedDir.getParentDirectory().getSubdirectories().remove(movedDir);

			//go to the directory and change its parent id
			movedDir.setParentDirectoryId(moveDirEvent.getNewParentDirectoryId());
						
			//get the new parent directory of dir
			ExtendedDirectory parent = allDirs.get(movedDir.getParentDirectoryId());
			
			//add the dir to its parent
			parent.addDirectory(movedDir);
			
			//set the parent directory
			movedDir.setParentDirectory(parent);
		}
	}

	/**
	 * This method takes in a list of delete directory events and deletes them from the map
	 * of all directories OR marks them as deleted
	 */
	private void handleDeleteDirectoryEvents(Map<String, ExtendedDirectory> allDirs, boolean withDeletedDocsAndDirs, List<StorytellerEvent> deleteDirectoryEvents)
	{
		//for each delete directory event update the wasDeleted in the directory map 
		for(StorytellerEvent event: deleteDirectoryEvents)
		{
			//create a real delete directory event
			DeleteDirectoryEvent deleteDirEvent = (DeleteDirectoryEvent)event;
			
			//get the directory id that this event refers to
			String dirId = deleteDirEvent.getDirectoryId();

			//get the deleted directory
			ExtendedDirectory delDir = allDirs.get(dirId);
			
			//if the user wants deleted directories
			if(withDeletedDocsAndDirs)
			{
				//indicate that this directory was deleted (but keep it in the map)
				delDir.setWasDeleted(true);
			}
			else //the user does not want deleted directories
			{
				//remove the directory from its parent
				delDir.getParentDirectory().getSubdirectories().remove(delDir);
				
				//remove the directory from the map
				allDirs.remove(dirId);
			}
		}
	}

	/**
	 * Gets all the documents in this node. It handles creates, renames, moves, and 
	 * delete document events.
	 * 
	 * This method gets passed in a list of document ids that need to be considered. 
	 * We ignore any doc id that is not on the list. 
	 */
	public void getAllDocuments(Map<String, ExtendedDocument> allDocs, Map<String, ExtendedDirectory> allDirs, List<String> documentIds, boolean withDeletedDocsAndDirs) throws DBAbstractionException
	{
		//get all the create document events in the list of acceptable doc ids
		List < StorytellerEvent > createDocumentEvents = getDatabase().getEventsByType(getNodeId(), getLastEvent().getNodeSequenceNum(), CreateDocumentEvent.CREATE_DOCUMENT_EVENT_TYPE, documentIds);
		//creates new documents for create document events
		handleCreateDocumentEvents(allDocs, allDirs, createDocumentEvents);

		//get all the rename document events in the list of acceptable doc ids
		List < StorytellerEvent > renameDocumentEvents = getDatabase().getEventsByType(getNodeId(), getLastEvent().getNodeSequenceNum(), RenameDocumentEvent.RENAME_DOCUMENT_EVENT_TYPE, documentIds);
		//renames documents in the collection of all docs
		handleRenameDocumentEvents(allDocs, renameDocumentEvents);

		//get all the move document events in the list of acceptable doc ids
		List < StorytellerEvent > moveDocumentEvents = getDatabase().getEventsByType(getNodeId(), getLastEvent().getNodeSequenceNum(), MoveDocumentEvent.MOVE_DOCUMENT_EVENT_TYPE, documentIds);
		//adjusts the parent ids for move document events
		handleMoveDocumentEvents(allDocs, allDirs, moveDocumentEvents);

		//get all the delete document events in the list of acceptable doc ids
		List < StorytellerEvent > deleteDocumentEvents = getDatabase().getEventsByType(getNodeId(), getLastEvent().getNodeSequenceNum(), DeleteDocumentEvent.DELETE_DOCUMENT_EVENT_TYPE, documentIds);
		//removes documents or marks them as being deleted
		handleDeleteDocumentEvents(allDocs, allDirs, withDeletedDocsAndDirs, deleteDocumentEvents);
	}

	/**
	 * This method takes in a list of create document events and created extended 
	 * documents from them and adds them to the map of all directories 
	 */
	private void handleCreateDocumentEvents(Map<String, ExtendedDocument> allDocs, Map<String, ExtendedDirectory> allDirs, List<StorytellerEvent> createDocumentEvents)
	{
		//for each create document event add an extended document to the document map
		for(StorytellerEvent event : createDocumentEvents)
		{
			CreateDocumentEvent createDocEvent = (CreateDocumentEvent)event;
			
			//create a new extended document
			ExtendedDocument newDoc = new ExtendedDocument(createDocEvent.getDocumentId(), createDocEvent.getTimestamp(), createDocEvent.getCreatedUnderNodeId(), createDocEvent.getCreatedByDevGroupId(), createDocEvent.getParentDirectoryId(), createDocEvent.getDocumentNewName());
			
			//add it to the map of all docs
			allDocs.put(newDoc.getId(), newDoc);
			
			//get the docs parent dir
			ExtendedDirectory parent = allDirs.get(newDoc.getParentDirectoryId());
			
			//add the document to the parent
			parent.addDocument(newDoc);
			
			//set the document's parent
			newDoc.setParentDirectory(parent);			
		}
	}

	/**
	 * This method takes in a list of rename document events and renames the extended 
	 * documents in the collection of all extended documents.
	 */
	private void handleRenameDocumentEvents(Map<String, ExtendedDocument> allDocs, List<StorytellerEvent> renameDocumentEvents)
	{
		//for each rename document event update the name in the document map 
		for(StorytellerEvent event : renameDocumentEvents)
		{
			RenameDocumentEvent renameDocEvent = (RenameDocumentEvent)event;
			
			//get the document id that this event refers to
			String docId = renameDocEvent.getDocumentId();
			
			//go to the document and change its name
			allDocs.get(docId).setName(renameDocEvent.getDocumentNewName());
		}
	}

	/**
	 * This method takes in a list of move document events and handles adjusting parent/child 
	 * relationships in the map of all documents 
	 */
	private void handleMoveDocumentEvents(Map<String, ExtendedDocument> allDocs, Map<String, ExtendedDirectory> allDirs, List<StorytellerEvent> moveDocumentEvents)
	{
		//for each move document event update the parent id in the document map 
		for(StorytellerEvent event : moveDocumentEvents)
		{
			MoveDocumentEvent moveDocEvent = (MoveDocumentEvent)event;
			
			//get the document id that this event refers to
			String docId = moveDocEvent.getDocumentId();
			
			//get the moved document
			ExtendedDocument movedDoc = allDocs.get(docId);
			
			//remove the moved document from its old parent's list of children
			movedDoc.getParentDirectory().getDocuments().remove(movedDoc);

			//set the new parent dir id
			movedDoc.setParentDirectoryId(moveDocEvent.getNewParentDirectoryId());			
						
			//get the new parent directory of dir
			ExtendedDirectory parent = allDirs.get(movedDoc.getParentDirectoryId());
			
			//add the doc to its parent
			parent.addDocument(movedDoc);
			
			//set the parent directory
			movedDoc.setParentDirectory(parent);
		}
	}

	/**
	 * This method takes in a list of delete document events and deletes them from the map
	 * of all documents OR marks them as deleted
	 */
	private void handleDeleteDocumentEvents(Map<String, ExtendedDocument> allDocs, Map<String, ExtendedDirectory> allDirs, boolean withDeletedDocsAndDirs, List<StorytellerEvent> deleteDocumentEvents)
	{
		//for each delete document event update the wasDeleted in the document map 
		for(StorytellerEvent event : deleteDocumentEvents)
		{
			DeleteDocumentEvent deleteDocEvent = (DeleteDocumentEvent)event;
			
			//get the document id that this event refers to
			String docId = deleteDocEvent.getDocumentId();
			
			//get the deleted document
			ExtendedDocument delDoc = allDocs.get(docId);
			
			//if the user wants deleted documents
			if(withDeletedDocsAndDirs)
			{
				//indicate that this document was deleted (but keep it in the map)
				delDoc.setWasDeleted(true);
			}
			else //the user does not want deleted documents
			{
				//remove the document from its parent
				delDoc.getParentDirectory().getDocuments().remove(delDoc);

				//remove the document from the map
				allDirs.remove(docId);				
			}
		}
	}
		
	public String getNodeId()
	{
		return nodeId;
	}
	protected void setNodeId(String nodeId)
	{
		this.nodeId = nodeId;
	}
	
	//TODO we need this for merge nodes right now, get rid of it if/when that changes
	public int getNumEventsInNode()
	{
		return getLastEvent().getNodeSequenceNum() + 1;
	}
	
	public StorytellerEvent getFirstEvent()
	{
		return firstEvent;
	}
	public void setFirstEvent(StorytellerEvent firstEvent)
	{
		this.firstEvent = firstEvent;
	}
	
	public StorytellerEvent getLastEvent()
	{
		return lastEvent;
	}
	public void setLastEvent(StorytellerEvent lastEvent)
	{
		this.lastEvent = lastEvent;
	}
	
	@Override
	public String toString()
	{
		return "PlaybackNode in node " + getNodeId();
	}

	public DBAbstraction getDatabase()
	{
		return database;
	}

	public void setDatabase(DBAbstraction database)
	{
		this.database = database;
	}
}