package playback;

import core.Constants;
import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.entities.Developer;
import core.entities.DeveloperGroup;
import core.entities.ExtendedDirectory;
import core.entities.ExtendedDocument;
import core.events.*;
import core.services.json.JSONiffy;
import merge.MergeNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * This class is used to hold a collection of playback nodes that represent
 * something that could be played back. The playback nodes are needed because
 * developers can create new nodes from the middle of existing ones. This class
 * provides access to the data about a collection of these playback nodes.
 * <p>
 * Every playback session has a playback filter object associated with it to
 * determine which events are worthy of being played back. A playback session
 * can be filtered using the filter object and a group of events will be collected
 * for playback. The events will be marked as relevant or not to be used during
 * an animated playback.
 */
public class PlaybackSession {
    //id of this playback session
    private String playbackSessionId;

    //reference to the database for this playback session
    private DBAbstraction database;

    //the logged in dev group manipulating this session
    private String loggedInDeveloperGroupId;

    //list of playback nodes in this session
    private LinkedList<PlaybackNode> playbackNodes;

    //filter used to decide which events should be marked relevant or irrelevant
    //if a playback is requested
    private PlaybackFilter filter;

    //this holds events that will get played back
    private LinkedList<StorytellerEvent> eventsToPlayback;

    //this is the state of filtering in this playback session
    private String stateOfEventFiltering;
    private final String NOT_FILTERED = "not filtered";
    private final String FILTERING = "filtering";
    private final String DONE_FILTERING = "done filtering";

    /**
     * Creates an empty playback session.
     */
    public PlaybackSession(DBAbstraction db, String developerGroupId) {
        //generate a random id for the session
        setId(UUID.randomUUID().toString());
        setDatabase(db);
        setLoggedInDeveloperGroupId(developerGroupId);
        setStateOfEventFiltering(NOT_FILTERED);
        setEventsToPlayback(new LinkedList<StorytellerEvent>());
        setPlaybackNodes(new LinkedList<PlaybackNode>());
        setFilter(new PlaybackFilter());
    }

    /**
     * Creates a playback session based on an existing node id and node sequence
     * number. The playback session will be updated with playback nodes from the
     * node id/sequence num up to the root node.
     */
    public PlaybackSession(DBAbstraction db, String developerGroupId, String nodeId, int seqNumber) throws DBAbstractionException {
        //build the data
        this(db, developerGroupId);

        //create the playback nodes
        updatePlaybackSessionToNode(nodeId, seqNumber);
    }

    /**
     * This method builds up a list of playback nodes using the passed in node id
     * as a starting node and moves up towards the root. It only gathers 'relevant'
     * events while it is climbing up the tree (a node created in the middle of
     * another does not want all the events in the node). The node list is ordered
     * from root node (at pos 0) onward.
     * <p>
     * The user passes in a node id and a sequence number within that node. If the
     * passed in sequence number is -1 then the last event in the node will be used,
     * otherwise the last node will stop at the passed in sequence number within the
     * node.
     */
    public void updatePlaybackSessionToNode(String nodeId, int nodeSequenceNumber) throws DBAbstractionException {
        //indicate that the new session has not been filtered
        setStateOfEventFiltering(NOT_FILTERED);

        //create a new list of events to playback
        setEventsToPlayback(new LinkedList<StorytellerEvent>());

        //new list of all the playback nodes from the passed in node up to the root node
        playbackNodes = new LinkedList<PlaybackNode>();

        //holds the first and last events in each of the nodes
        StorytellerEvent firstEventInNode = null;
        StorytellerEvent lastEventInNode = null;

        //if there was no node sequence number specified
        if (nodeSequenceNumber <= -1) {
            //get the last event in the passed in node
            lastEventInNode = getDatabase().getLastEventInNode(nodeId);
        } else //the user did specify a sequence number in the node to get
        {
            //get specified event in the node
            lastEventInNode = getDatabase().getEvent(nodeId, nodeSequenceNumber);
        }

        //holds the id of the last relevant event in the node
        String idOfLastEventInNode = null;

        //get the id of the last event in the passed in node
        idOfLastEventInNode = lastEventInNode.getId();

        //reference the latest node as we move up to the root
        PlaybackNode latestNode;

        //while we have not tried to go above the root node
        while (idOfLastEventInNode != null) {
            //get the last event in the node based on its id
            lastEventInNode = getDatabase().getEvent(idOfLastEventInNode);

            //get the node id that this event was created under
            String currentNodeId = lastEventInNode.getCreatedUnderNodeId();

            //get first event in the current node
            firstEventInNode = getDatabase().getFirstEventInNode(currentNodeId);

            //if the first event was a merge event
            if (firstEventInNode instanceof MergeEvent) {
                //create a merge node
                latestNode = new MergeNode(getDatabase(), currentNodeId);
            } else //first event in the node was not a merge event
            {
                //create a plain old playback node
                latestNode = new PlaybackNode(getDatabase(), currentNodeId, firstEventInNode, lastEventInNode);
            }

            //add the playback node to the beginning of the list
            playbackNodes.addFirst(latestNode);

            //in order to move backwards toward the previous playback node we get
            //the id of the event that immediately precedes the first event in the
            //node. This will be null only when we are at the root
            idOfLastEventInNode = firstEventInNode.getSequentiallyBeforeEventId();
        }

        //update the filter information based on the node data
        getFilter().setNodeID(getLastNode().getNodeId());
        getFilter().setNodeSequenceNumber(getLastNode().getLastEvent().getNodeSequenceNum());
        getFilter().setStartTime(getRootNode().getFirstEvent().getTimestamp().getTime());
        getFilter().setEndTime(getLastNode().getLastEvent().getTimestamp().getTime());
    }

    /**
     * This method takes in a list of ids of events that were selected by the
     * user. The algorithm we use looks for groups of events that have a prev
     * neighbor equal to the relevant events AND were deleted. The thinking is
     * if a user highlights some text to see it playback we only want to see:
     * - the selected events themselves (because the user specified these were
     * of interest)
     * - any events that were touching the selected events and were deleted
     * (events not deleted are on the screen and the user can choose to see
     * them if they widen their search)
     * <p>
     * The events that are prev neighbors and have been deleted are then marked
     * as relevant and we repeat the process looking for events that touched
     * these and were deleted.
     * <p>
     * The selected events are all relevant so they are added to the return array.
     * <p>
     * Next, the ids of the selected events are added to a list. We search the
     * database for any events that have a prev neighbor id (backlink) to an id
     * on the list (in the node lineage). We collect these insert events.
     * <p>
     * Then we search the node lineage for delete events that delete one of the
     * newly found backlink insert events. If we find a delete event we add the
     * delete event and the backlink event to the list of relevant events.
     * <p>
     * Lastly, for all of the backlink events that were deleted we add their ids
     * to the list and repeat this process.
     * <p>
     * TODO it probably wouldn't be hard to include a 'next neighbor' at this
     * point with the id of the event that comes after an event. Is this valuable?
     * Would it make this selected text query work better??
     * <p>
     * TODO I am still not convinced there isn't a better algo to find relevant
     * neighbors keep thinking about this!!
     */
    public Set<String> findEventIdsRelevantToTheSelectedEvents(List<String> selectedEventIds) throws DBAbstractionException {
        //map of all the relevant events (selected and those that at one time
        //touched the relevant ones)
        Set<String> relevantEventIds = new HashSet<String>();

        //get all of the selected insert events from the db
        List<StorytellerEvent> selectedEvents = getDatabase().getAllEventsByIds(selectedEventIds);

        //holds the ids of the events that we will do a prev neighbor search for. We are
        //looking for events whose prev neighbor attr is equal to an id on this list.
        Set<String> idsOfEventsToLookForBackLinksTo = new HashSet<String>();

        //go through the selected events
        for (StorytellerEvent event : selectedEvents) {
            //the selected events are all relevant, add their ids
            relevantEventIds.add(event.getId());

            //add the id of the selected event to look for relevant previous neighbors
            idsOfEventsToLookForBackLinksTo.add(event.getId());

            //get the selected event's previous neighbor
            String prevNeighborId = ((InsertEvent) event).getPreviousNeighborEventId();

            //add the id of the selected event's prev neighbor (if the prev neighbor is
            //already in the set it will not be added a second time)
            idsOfEventsToLookForBackLinksTo.add(prevNeighborId);
        }

        //while there are more events to do a prev neighbor search for
        while (idsOfEventsToLookForBackLinksTo.size() > 0) {
            //holds the events that have a prev neighbor attr equal to an id on idsOfEventsToLookForBackLinksTo
            List<InsertEvent> insertsThatBackUpToRelevant = new ArrayList<InsertEvent>();

            //look through all the nodes and accumulate events that have a prev neighbor attr
            //equal to one of the ids on idsOfEventsToLookForBackLinksTo
            for (PlaybackNode node : getPlaybackNodes()) {
                //get all the insert events that back up to the selected events (events already
                //marked as relevant can be ignored)
                insertsThatBackUpToRelevant.addAll(node.getInsertEventsThatBackUpToEvents(new ArrayList<String>(idsOfEventsToLookForBackLinksTo), new ArrayList<String>(relevantEventIds)));
            }

            //now clear out the ids of the last batch of events so we can add the next batch
            //newly relevant events and their previous neighbors will be added
            idsOfEventsToLookForBackLinksTo.clear();

            //now collect any of these previous neighbor events that have been deleted
            //in the node lineage
            for (PlaybackNode node : getPlaybackNodes()) {
                //go through each of the events with a prev neighbor in the list
                Iterator<InsertEvent> iter = insertsThatBackUpToRelevant.iterator();
                while (iter.hasNext()) {
                    //grab the event that backs up to a relevant one
                    InsertEvent insertEvent = iter.next();

                    //attempt to get the delete event for this event in this node
                    DeleteEvent deleteEvent = node.getDeleteEventForAnInsertEvent(insertEvent);

                    //if the insert event has been deleted in this node
                    if (deleteEvent != null) {
                        //if this event is already relevant it will not be added
                        if (!relevantEventIds.contains(insertEvent.getId())) {
                            //add the event id of the new event so we can do a prev neighbor
                            //search on it too
                            idsOfEventsToLookForBackLinksTo.add(insertEvent.getId());
                        }

                        //get the relevant event's prev neighbor id
                        String prevNeighborId = insertEvent.getPreviousNeighborEventId();

                        //if the prev neighbor is already relevant it will not be added
                        if (!relevantEventIds.contains(prevNeighborId)) {
                            //add the id of the selected event's prev neighbor
                            idsOfEventsToLookForBackLinksTo.add(prevNeighborId);
                        }

                        //add the insert event as relevant
                        relevantEventIds.add(insertEvent.getId());

                        //add the delete event as relevant
                        relevantEventIds.add(deleteEvent.getId());

                        //remove the insert event since it has been deleted in this node
                        iter.remove();
                    }
                    //else- the event backs up to a relevant one but has not been
                    //deleted, it is still on the screen
                }
            }
        }

        //return the set of relevant event ids
        return relevantEventIds;
    }

    /**
     * This method gets all the developers who contributed to the playback nodes
     * in this session.
     */
    public Set<DeveloperGroup> getDeveloperGroupsWhoContributed() throws DBAbstractionException {
        //a list of all developer groups who participated in the nodes
        Set<DeveloperGroup> devGroups = new HashSet<DeveloperGroup>();

        //go through each node
        for (PlaybackNode node : getPlaybackNodes()) {
            //collect the dev groups who made some contribution
            node.getDeveloperGroupsWhoContributed(devGroups);
        }

        return devGroups;
    }

    /**
     * This method begins the filtering process. It grabs all of the events in
     * the playback nodes associated with this playback session and adds them to
     * a collection. One can get the events by calling getEventsToPlayback().
     * <p>
     * Note: this is one of two implementations. I was trying to make this a
     * smarter version of filtering. The other version uses the db less and
     * does more of the filtering manually.
     * TODO test which is better for large databases
     */
    public void startFilteringEventsv2() throws DBAbstractionException {
        //indicate that filtering has started
        setStateOfEventFiltering(FILTERING);

        //holds the first relevant event
        StorytellerEvent firstRelevantEvent = getFirstRelevantEvent();

        //if there were no relevant events
        if (firstRelevantEvent == null) {
            //recreate the state of the system up until the end and get all of the events up
            //to the last event (they will all be marked as irrelevant)
            getEventsToPlayback().addAll(getAllEventsWithoutDeletes());
        } else //there is at least one relevant event
        {
            //if the first relevant event comes somewhere in the middle of the events we
            //recreate the state of the system and collect the events that makes that state
            //(only the first event in the node lineage has a seq before id of null)
            if (firstRelevantEvent.getSequentiallyBeforeEventId() != null) {
                //get all of the events to just before the first relevant event
                getEventsToPlayback().addAll(getAllEventsUpToAnEvent(firstRelevantEvent, false));
            }

            //holds the last relevant event
            StorytellerEvent lastRelevantEvent = getLastRelevantEvent();

            //get all the events from the first relevant one to the last relevant one
            List<StorytellerEvent> remainingEvents = getEventsFromEventToEvent(firstRelevantEvent, lastRelevantEvent);

            //add all the remaining events to the list of playback events
            getEventsToPlayback().addAll(remainingEvents);

            //deleted insert events need to be informed when they were deleted and deletes
            //under a certain time limit need to be removed from the event list
            handleDeletes(getEventsToPlayback());

            //mark the events as either relevant or irrelevant
            markEventsWithRelevancy(getEventsToPlayback());
        }

        //indicate that filtering has ended
        setStateOfEventFiltering(DONE_FILTERING);
    }

    /**
     * Returns a list of all the events in a playback session (without deletes)
     * and marks them as irrelevant. It returns events in the order of create
     * directory, create document, and finally insert events.
     */
    private LinkedList<StorytellerEvent> getAllEventsWithoutDeletes() throws DBAbstractionException {
        //TODO consider doing this without the call to getTheStateOfTheFileSystem()
        //recreate the state of the system in this session with insert events
        //and no deleted docs/dirs
        ExtendedDirectory root = getTheStateOfTheFileSystem(true, false);

        //holds all of the events
        LinkedList<StorytellerEvent> events = new LinkedList<StorytellerEvent>();

        //add all the create directory events to the list of playback events
        getAllCreateDirectoryEventsInAnExtendedDirectory(root, events);
        //add all the create document events to the list of playback events
        getAllCreateDocumentEventsInAnExtendedDirectory(root, events);
        //add all the insert events to the list of playback events
        getAllInsertEventsInAnExtendedDirectory(root, events);

        //mark them all as irrelevant and set their seq before ids
        //set the first event to not relevant (we'll do the rest down below)
        events.get(0).setIsRelevantForPlayback(false);
        //set the first event's seq before event to the null string
        events.get(0).setSequentiallyBeforeEventId("null");

        //mark them all as irrelevant
        //set their previous neighbor and sequentially before to each other
        for (int i = 1; i < events.size(); i++) {
            //get consecutive events
            StorytellerEvent event = events.get(i);
            StorytellerEvent previousEvent = events.get(i - 1);

            //set them to be irrelevant
            event.setIsRelevantForPlayback(false);
            //set the sequentially before id
            event.setSequentiallyBeforeEventId(previousEvent.getId());
        }

        return events;
    }

    /**
     * Gets all the events in a node lineage up to the passed in event and returns
     * a list of those events that can be played back in order to recreate an
     * initial state of a playback.
     */
    private LinkedList<StorytellerEvent> getAllEventsUpToAnEvent(StorytellerEvent upToEvent, boolean includePassedInEvent) throws DBAbstractionException {
        //holds all of the events up to (and possibly including) the passed in event
        LinkedList<StorytellerEvent> events = new LinkedList<StorytellerEvent>();

        //if we do not want to include the 'up to' event
        if (!includePassedInEvent) {
            //change the 'up to' event to the event just before it
            upToEvent = getDatabase().getEvent(upToEvent.getSequentiallyBeforeEventId());
        }

        //create a throw away session up to the state before the first relevant event
        PlaybackSession beforeFirstRelevant = new PlaybackSession(getDatabase(), getLoggedInDeveloperGroupId(), upToEvent.getCreatedUnderNodeId(), upToEvent.getNodeSequenceNum());
        //copy this session's filter into the new one
        beforeFirstRelevant.setFilter(getFilter());

        //recreate the state of the system just prior to that first relevant event (with
        //insert events and no deleted docs/dirs)
        ExtendedDirectory root = beforeFirstRelevant.getTheStateOfTheFileSystem(true, false);

        //add all the create directory events to the list of playback events
        getAllCreateDirectoryEventsInAnExtendedDirectory(root, events);
        //add all the create document events to the list of playback events
        getAllCreateDocumentEventsInAnExtendedDirectory(root, events);
        //add all the insert events to the list of playback events
        getAllInsertEventsInAnExtendedDirectory(root, events);

        //mark them all as irrelevant
        //set the first event to not relevant (we'll do the rest down below)
        events.get(0).setIsRelevantForPlayback(false);

        //mark them all as irrelevant
        //set their previous neighbor and sequentially before to each other
        for (int i = 1; i < events.size(); i++) {
            //get consecutive events
            StorytellerEvent event = events.get(i);
            StorytellerEvent previousEvent = events.get(i - 1);

            //set them to be irrelevant
            event.setIsRelevantForPlayback(false);
            //set the sequentially before id
            event.setSequentiallyBeforeEventId(previousEvent.getId());
        }

        return events;
    }

    /**
     * Gets all the events between two passed in events in a node lineage.
     */
    private List<StorytellerEvent> getEventsFromEventToEvent(StorytellerEvent firstEvent, StorytellerEvent lastEvent) throws DBAbstractionException {
        //holds all the events between the first and the last
        List<StorytellerEvent> events = new LinkedList<StorytellerEvent>();

        //if the first and last events are the same
        if (firstEvent.equals(lastEvent)) {
            //add the one event
            events.add(firstEvent);
        }
        //else if the first and last event are in the same node
        else if (firstEvent.getCreatedUnderNodeId().equals(lastEvent.getCreatedUnderNodeId())) {
            //find the node
            for (PlaybackNode node : getPlaybackNodes()) {
                if (node.getNodeId().equals(firstEvent.getCreatedUnderNodeId())) {
                    //get all the events in between the two events
                    events.addAll(node.getAllEventsFromOneEventToAnother(firstEvent.getNodeSequenceNum(), lastEvent.getNodeSequenceNum(), getFilter().getAcceptableDocumentIDs()));
                    break;
                }
            }
        } else //the first and last are in different nodes
        {
            //this keeps track of the node we are in
            int nodeIndex = 0;

            //go through all the nodes
            for (int i = 0; i < getPlaybackNodes().size(); i++) {
                //get the next node
                PlaybackNode node = getPlaybackNodes().get(i);

                //if this is the first event's node
                if (node.getNodeId().equals(firstEvent.getCreatedUnderNodeId())) {
                    //get all the events from the first event until the end of the node
                    events.addAll(node.getAllEventsFromAnEventUntilTheEndOfTheNode(firstEvent.getNodeSequenceNum(), getFilter().getAcceptableDocumentIDs()));

                    //keep track of the node number
                    nodeIndex = i;

                    //stop looking for the first
                    break;
                }
            }

            //go through the remaining nodes
            for (int i = nodeIndex + 1; i < getPlaybackNodes().size(); i++) {
                //get the next node
                PlaybackNode node = getPlaybackNodes().get(i);

                //if this is the last event's node
                if (node.getNodeId().equals(lastEvent.getCreatedUnderNodeId())) {
                    //get all the events up until the last event
                    events.addAll(node.getAllEventsUpToAnEvent(lastEvent.getNodeSequenceNum(), getFilter().getAcceptableDocumentIDs()));

                    //no need to look at any more nodes
                    break;
                } else //this is in between the first event's node and the last event's node
                {
                    //get all the events in the node (except in the docs we don't care about)
                    events.addAll(node.getAllEvents(getFilter().getAcceptableDocumentIDs()));
                }
            }
        }

        return events;
    }

    /**
     * Gets all the insert events in all the documents in an extended directory. The
     * insert events are manipulated so that all of their previous neighbor id's are
     * set to the events immediately previous to them in the document. If a perfect
     * programmer had written the code it would be in this order. This is used to
     * recreate the state of all the documents.
     */
    private void getAllInsertEventsInAnExtendedDirectory(ExtendedDirectory dir, List<StorytellerEvent> events) {
        //for each of the docs in the directory
        for (ExtendedDocument doc : dir.getDocuments()) {
            //update all of the insert events' previous neighbor id so that they
            //appear as if they were written in the order they appear in the file
            //this is needed because their real prev neighbor might have been deleted
            for (int i = 1; i < doc.getInsertEvents().size(); i++) {
                //get a pair of consecutive events
                InsertEvent previousInsertEvent = doc.getInsertEvents().get(i - 1);
                InsertEvent currentInsertEvent = doc.getInsertEvents().get(i);

                //set the current event's prev neighbor to the prev insert event
                currentInsertEvent.setPreviousNeighborEventId(previousInsertEvent.getId());
            }

            //add all of the document's insert events
            events.addAll(doc.getInsertEvents());
        }

        //for each of the subdirectories
        for (ExtendedDirectory subDir : dir.getSubdirectories()) {
            //recurse through the sub dirs
            getAllInsertEventsInAnExtendedDirectory(subDir, events);
        }
    }

    /**
     * Gets all the create document events in an extended directory
     */
    private void getAllCreateDocumentEventsInAnExtendedDirectory(ExtendedDirectory dir, List<StorytellerEvent> events) throws DBAbstractionException {
        //for each of the docs in the directory
        for (ExtendedDocument doc : dir.getDocuments()) {
            //get the original create document event
            CreateDocumentEvent createDocEvent = (CreateDocumentEvent) getDatabase().getCreateDocumentEvent(doc.getId());

            //the name and parent directory may have changed because of a rename or a move but the
            //extended doc has the correct ones so write them just in case
            createDocEvent.setDocumentNewName(doc.getName());
            createDocEvent.setParentDirectoryId(doc.getParentDirectoryId());

            //add the create doc event to the list of all events
            events.add(createDocEvent);
        }

        //for each of the subdirectories
        for (ExtendedDirectory subDir : dir.getSubdirectories()) {
            //recurse through the sub dirs
            getAllCreateDocumentEventsInAnExtendedDirectory(subDir, events);
        }
    }

    /**
     * Gets all the create directory events from an extended directory
     */
    private void getAllCreateDirectoryEventsInAnExtendedDirectory(ExtendedDirectory dir, List<StorytellerEvent> events) throws DBAbstractionException {
        //get the original create directory event
        CreateDirectoryEvent createDirEvent = (CreateDirectoryEvent) getDatabase().getCreateDirectoryEvent(dir.getId());

        //the name and parent directory may have changed because of a rename or a move but the
        //extended dir has the correct ones so write them just in case
        createDirEvent.setDirectoryNewName(dir.getName());
        createDirEvent.setParentDirectoryId(dir.getParentDirectoryId());

        //add the create dir event to the list of all events
        events.add(createDirEvent);

        //for each of the dubdirectories
        for (ExtendedDirectory subDir : dir.getSubdirectories()) {
            //recurse through the sub dirs
            getAllCreateDirectoryEventsInAnExtendedDirectory(subDir, events);
        }
    }

    /**
     * Gets the first relevant event in the node lineage. If there is a selected
     * text filter it will find the first relevant event in the group. If it is a
     * normal filter then the first event that satisfies all of the filter info
     * will be returned.
     */
    public StorytellerEvent getFirstRelevantEvent() throws DBAbstractionException {
        StorytellerEvent firstRelevantEvent = null;

        //if this session's filter was created from some selected text
        if (getFilter().isASelectedTextFilter()) {
            //find the earliest event in the selected and relevant ones
            //grab all the events in the nodes
            for (PlaybackNode node : getPlaybackNodes()) {
                //collect all the events in this node that are in the user selected docs
                firstRelevantEvent = node.getFirstRelevantEvent(getFilter().getSelectedAndRelevantEventIds());

                //if this node found a relevant event
                if (firstRelevantEvent != null) {
                    //stop looking through the rest of the nodes
                    break;
                }
            }
        } else //filter was created using filter params including acceptable docs and dev groups
        {
            //find first relevant event in the node lineage
            //grab all the events in the nodes
            for (PlaybackNode node : getPlaybackNodes()) {
                //find the first event based on the filter params
                firstRelevantEvent = node.getFirstRelevantEvent(getFilter().getStartTime(), getFilter().getEndTime(), getFilter().getAcceptableDocumentIDs(), getFilter().getAcceptableDeveloperGroupIDs());

                //if this node found a relevant event
                if (firstRelevantEvent != null) {
                    //stop looking through the rest of the nodes
                    break;
                }
            }
        }
        return firstRelevantEvent;
    }

    /**
     * Gets the last relevant event in a node lineage. If there is a selected text
     * filter then the last event in the last node is returned. If it is a normal
     * filter then the last event that satisfies all of the filter conditions is
     * returned.
     */
    public StorytellerEvent getLastRelevantEvent() throws DBAbstractionException {
        StorytellerEvent lastRelevantEvent = null;

        //if the filter was created from some selected text
        if (getFilter().isASelectedTextFilter()) {
            //get the last event in the last node if it is a selected text playback
            lastRelevantEvent = getLastNode().getLastEvent();
        } else //filter was created using filter params including acceptable docs and dev groups
        {
            //find last relevant event in the node lineage
            //go in reverse order through the nodes
            for (int i = getPlaybackNodes().size() - 1; i >= 0; i--) {
                //get the next playback node
                PlaybackNode node = getPlaybackNodes().get(i);

                //collect all the events in this node that are in the user selected docs
                lastRelevantEvent = node.getLastRelevantEvent(getFilter().getStartTime(), getFilter().getEndTime(), getFilter().getAcceptableDocumentIDs(), getFilter().getAcceptableDeveloperGroupIDs());

                //if this node found a relevant event
                if (lastRelevantEvent != null) {
                    //stop looking through the rest of the nodes
                    break;
                }
            }
        }
        return lastRelevantEvent;
    }

    /**
     * This method filters all events and creates a JSON array of the important event info that is
     * ready to be sent back to the client.
     * <p>
     * This version does not make extensive use of the database. A lot of the work is manual.
     * TODO this may end up being just as fast or faster than v2, if so this one is simpler!
     */
    public void startFilteringEventsv1() throws DBAbstractionException, JSONException {
        //indicate that filtering has started
        setStateOfEventFiltering(FILTERING);

        //grab all the events in the nodes
        for (PlaybackNode node : getPlaybackNodes()) {
            //collect all the events in this node that are in the user selected docs
            getEventsToPlayback().addAll(node.getAllEvents(getFilter().getAcceptableDocumentIDs()));
        }

        //deleted insert events need to be informed when they were deleted and deletes under a
        //certain time limit need to be removed from the event list
        handleDeletes(getEventsToPlayback());

        //mark the remaining events as either relevant or irrelevant
        markEventsWithRelevancy(getEventsToPlayback());

        //if the user wants to see only the end result 'perfect programmer' playback
        if (getFilter().isShowOnlyEndResult()) {
            //now remove events from the end of the list
            for (int i = getEventsToPlayback().size() - 1; i >= 0; i--) {
                //if the event is going to be highlighted
                if (getEventsToPlayback().get(i).getDisplayAsRelevantButDoNotAnimate()) {
                    //stop
                    break;
                } else //the event should not show up in the results
                {
                    //remove the unneeded event
                    getEventsToPlayback().remove(i);
                }
            }
        }

        //indicate that filtering has ended
        setStateOfEventFiltering(DONE_FILTERING);
    }

    /**
     * This method goes through the events in reverse order looking for deletes.
     * When it finds one it stores the delete event in a map and the id of the
     * insert that it is deleting in a set.
     * <p>
     * The method also looks for insert events that have been deleted (using the
     * set from above). When it finds an insert that has been deleted it either:
     * - deletes both the insert and delete events if they happen within a user
     * specified time difference
     * - updates the insert with information about when and how it was deleted
     * <p>
     * TODO optimization: go node by node in reverse building up a list of delete events and
     * then go through that node's inserts and handle them. If
     */
    private void handleDeletes(List<StorytellerEvent> allEvents) throws DBAbstractionException {
        //a map of all delete events keyed by the insert's id
        Map<String, DeleteEvent> deleteEvents = new HashMap<String, DeleteEvent>();

        //holds all the inserts that were created and then deleted under the user specified time limit
        //and all the delete events that got rid of them
        List<StorytellerEvent> eventsToRemove = new ArrayList<StorytellerEvent>();

        //go through all the events in reverse order
        for (int i = allEvents.size() - 1; i >= 0; i--) {
            //grab the next event
            StorytellerEvent event = allEvents.get(i);

            //if this is a delete event
            if (event.getEventType().equals(DeleteEvent.DELETE_EVENT_TYPE)) {
                //get the delete event
                DeleteEvent deleteEvent = (DeleteEvent) event;

                //add the delete event to the map of all deletes
                deleteEvents.put(deleteEvent.getPreviousNeighborEventId(), deleteEvent);
            }
            //if this is an insert event that was deleted
            //(only inserts can be deleted and deletes come after inserts)
            else if (event.getEventType().equals(InsertEvent.INSERT_EVENT_TYPE) &&
                    deleteEvents.get(event.getId()) != null) {
                //turn the event into an insert
                InsertEvent insertEvent = (InsertEvent) event;

                //get the delete event that deleted it
                DeleteEvent deleteEvent = deleteEvents.get(insertEvent.getId());

                //if the user wants to hide deletes under a certain threshold
                if (getFilter().getHideDeleteLimit() > 0) {
                    //get the difference from the time of the delete to the time of the insert
                    long timeInBetweenInsertAndDelete = deleteEvent.getTimestamp().getTime() - insertEvent.getTimestamp().getTime();

                    //if the diff is less than or equal to the hide delete under x seconds time
                    if (timeInBetweenInsertAndDelete <= (getFilter().getHideDeleteLimit() * 1000)) {
                        //mark the insert to be deleted later
                        eventsToRemove.add(insertEvent);
                        eventsToRemove.add(deleteEvent);
                    }
                }

                //store info about the delete in the insert event
                insertEvent.setDeletedAtTimestamp(deleteEvent.getTimestamp());
                insertEvent.setDeletedByDevGroupId(deleteEvent.getCreatedByDevGroupId());
                insertEvent.setDeleteEventId(deleteEvent.getId());

                //we don't need the delete event anymore
                deleteEvents.remove(deleteEvent);
            }
        }

        //if the user is even interested in hiding deletes under the limit AND
        //there are some to remove
        if (getFilter().getHideDeleteLimit() > 0 && !eventsToRemove.isEmpty()) {
            //remove the inserts
            allEvents.removeAll(eventsToRemove);
        }
    }

    /**
     * This method takes a list of events and marks them as relevant or irrelevant. If the
     * user has chosen to see paste origins it finds the parent (copied event) and marks it
     * as relevant too.
     */
    private void markEventsWithRelevancy(List<StorytellerEvent> allEvents) {
        //holds each of the events in the list
        StorytellerEvent currentEvent = null;

        //holds the ids of all of the relevant events who have a paste parent
        Set<String> relevantPastedIds = new HashSet<String>();

        //an iterator starting at the end
        ListIterator<StorytellerEvent> iter = allEvents.listIterator(allEvents.size());

        //go through all of the events in reverse order (that way pasted events
        //will show up first and we will see the source of the pastes after)
        while (iter.hasPrevious()) {
            //get the latest event
            currentEvent = iter.previous();

            //is the current event relevant based on time and developers
            if (isRelevant(currentEvent)) {
                //if the user wants to see the end result only (perfect programmer)
                if (getFilter().isShowOnlyEndResult()) {
                    //highlight the event but do NOT animate it
                    currentEvent.setDisplayAsRelevantButDoNotAnimate(true);
                }
                //if the user wants to see whole words only
                else if (getFilter().getRelevantBlockType().equals(PlaybackFilter.WORD_BLOCK_TYPE)) {
                    //if this is an insert event
                    if (currentEvent.getEventType().equals(InsertEvent.INSERT_EVENT_TYPE)) {
                        //turn it into an insert
                        InsertEvent insertEvent = (InsertEvent) currentEvent;

                        //if this is a blank or newline
                        if (insertEvent.getEventData().equals(" ") ||
                                insertEvent.getEventData().equals("\n")) {
                            //mark it as relevant
                            currentEvent.setIsRelevantForPlayback(true);
                        } else //not a blank or newline
                        {
                            //highlight the event but do NOT make it relevant
                            currentEvent.setDisplayAsRelevantButDoNotAnimate(true);
                        }
                    }
                }
                //if the user wants to see whole lines only
                else if (getFilter().getRelevantBlockType().equals(PlaybackFilter.LINE_BLOCK_TYPE)) {
                    //if this is an insert event
                    if (currentEvent.getEventType().equals(InsertEvent.INSERT_EVENT_TYPE)) {
                        //turn it into an insert
                        InsertEvent insertEvent = (InsertEvent) currentEvent;

                        //if this is a newline
                        if (insertEvent.getEventData().equals("\n")) {
                            //mark it as relevant
                            currentEvent.setIsRelevantForPlayback(true);
                        } else //not a newline
                        {
                            //highlight the event but do NOT make it relevant
                            currentEvent.setDisplayAsRelevantButDoNotAnimate(true);
                        }
                    }
                } else //the user wants to see an animated playback
                {
                    //mark the event as relevant
                    currentEvent.setIsRelevantForPlayback(true);
                }

                //if the relevant event is an insert
                if (currentEvent.getEventType().equals(InsertEvent.INSERT_EVENT_TYPE)) {
                    //convert to an insert event
                    InsertEvent insertEvent = (InsertEvent) currentEvent;

                    //if the relevant event's paste parent id is not empty
                    if (!insertEvent.getPasteParentId().isEmpty()) {
                        //record the source of this event, store the event's
                        //paste parent id
                        relevantPastedIds.add(insertEvent.getPasteParentId());
                    }
                }
            } else //the current event is not a relevant event
            {
                //if the current, non-relevant event was the source of a relevant
                //pasted event
                if (relevantPastedIds.contains(currentEvent.getId())) {
                    //if the user is interested in seeing paste parents
                    if (getFilter().isShowPasteOrigin()) {
                        //if the user wants to see the end result only (perfect programmer)
                        if (getFilter().isShowOnlyEndResult()) {
                            //highlight the event but do NOT make it relevant
                            currentEvent.setDisplayAsRelevantButDoNotAnimate(true);
                        } else //the user wants to see the pasted events play out
                        {
                            //make this event relevant too
                            currentEvent.setIsRelevantForPlayback(true);
                        }
                    }
                    //if this is a selected text playback
                    else if (getFilter().isASelectedTextFilter()) {
                        //highlight the event but do NOT make it relevant
                        currentEvent.setDisplayAsRelevantButDoNotAnimate(true);
                    }

                    //we no longer need to keep track of this paste parent id so remove it
                    relevantPastedIds.remove(currentEvent.getId());

                    //if the current event was pasted from somewhere else too
                    if (currentEvent.getEventType().equals(InsertEvent.INSERT_EVENT_TYPE)) {
                        //convert to an insert event
                        InsertEvent insertEvent = (InsertEvent) currentEvent;

                        //if the paste parent id is not empty
                        if (!insertEvent.getPasteParentId().isEmpty()) {
                            //store the event's paste parent id
                            relevantPastedIds.add(insertEvent.getPasteParentId());
                        }
                    }
                }
            }
        }
    }

    /**
     * An event is relevant if all of these characteristics are true:
     * - it comes on or after the filter's start time
     * - it comes on or before the filter's end time time
     * - it was created by a user selected developer group in the filter
     * - it is in a user selected file. In fact, if the event is not in a user selected
     * file then it is irrelevant and not shown in the playback at all (this file
     * filtering happens when we ask the db for events- this is an optimization so
     * that the db can do some of the filtering so we don't have to)
     * <p>
     * Alternatively, it is also relevant if:
     * - if the event is in the collection of selected and relevant events
     * in a selected text playback
     */
    private boolean isRelevant(StorytellerEvent event) {
        //assume the event is not relevant
        boolean retVal = false;

        //if this playback session's filter is a selected text filter
        if (getFilter().isASelectedTextFilter()) {
            //see if the event is in the collection of selected and relevant ids
            if (getFilter().getSelectedAndRelevantEventIds().contains(event.getId())) {
                retVal = true;
            }
        } else //this is a filter specified by user supplied filter parameters
        {
            //if the event was created in the right time frame
            if (event.getTimestamp().getTime() >= getFilter().getStartTime() &&
                    event.getTimestamp().getTime() <= getFilter().getEndTime()) {
                //if the event was created by one of the selected developer groups
                if (getFilter().getAcceptableDeveloperGroupIDs().contains(event.getCreatedByDevGroupId())) {
                    //it is relevant
                    retVal = true;
                }
            }
        }
        return retVal;
    }

    /**
     * This method will get the state of the file system at a single point in time.
     * The point in time is represented by the playback nodes in the PlaybackSession.
     * <p>
     * The user can choose to get the state of the system with the insert event ids
     * added to the documents or without.
     * <p>
     * The user can also choose to include docs and dirs that have been deleted.
     */
    public ExtendedDirectory getTheStateOfTheFileSystem(boolean withInsertEventIds, boolean withDeletedDocsAndDirs) throws DBAbstractionException {
        //this holds the root of the file system
        ExtendedDirectory root = null;

        //holds all the directories in the node lineage based on the directory id
        Map<String, ExtendedDirectory> allDirs = new HashMap<String, ExtendedDirectory>();

        //holds all of the documents in the node lineage based on the doc id
        Map<String, ExtendedDocument> allDocs = new HashMap<String, ExtendedDocument>();

        //start with the directories in this playback session
        for (PlaybackNode node : getPlaybackNodes()) {
            //collect all the directories in the node (accounts for creates, renames, moves, deletes)
            node.getAllDirectories(allDirs, withDeletedDocsAndDirs);
        }

        //now for documents in the nodes
        for (PlaybackNode node : getPlaybackNodes()) {
            //get all the documents at a point in time and ignore documents not on the filter
            //list (accounts for creates, renames, moves, deletes)
            node.getAllDocuments(allDocs, allDirs, getFilter().getAcceptableDocumentIDs(), withDeletedDocsAndDirs);
        }

        //if we are to add the insert events to the documents
        if (withInsertEventIds) {
            //holds all the deleted events' ids in the nodes
            Set<String> allDeletedInsertEventsIds = new HashSet<String>();

            //holds all of the insert events in the nodes (that were not deleted) by document id
            Map<String, List<InsertEvent>> allInserts = new HashMap<String, List<InsertEvent>>();

            //collect all the delete events in the nodes first
            for (int i = getPlaybackNodes().size() - 1; i >= 0; i--) {
                //get the playback node
                PlaybackNode node = getPlaybackNodes().get(i);

                //collect all the ids of inserts that were deleted in the node lineage
                node.getDeletedEventsIds(allDeletedInsertEventsIds);

                //now get the insert events (that were not deleted)
                node.getInsertsThatWereNotDeleted(allInserts, allDeletedInsertEventsIds);
            }

            //insert the insert events into the extended documents
            insertEventsIntoExtendedDocuments(allDocs, allInserts);
        }

        //find the root node to return
        for (ExtendedDirectory dir : allDirs.values()) {
            //if this is the root of the file system (the root dir does not have a parent)
            if (dir.getParentDirectoryId() == null || dir.getParentDirectoryId().isEmpty()) {
                //store the root
                root = dir;
                break;
            }
        }

        return root;
    }

    /**
     * This method takes a map of all the extended documents in the node lineage
     * and a map of lists of all inserts events in the documents and combines
     * them together so that the extended documents have their insert events in
     * the correct order as they appear in the documents
     */
    private void insertEventsIntoExtendedDocuments(Map<String, ExtendedDocument> allDocs, Map<String, List<InsertEvent>> allInserts) {
        //sort the inserts for a document by 'sort order'
        //go through each document's list of events
        for (List<InsertEvent> listOfEvents : allInserts.values()) {
            //sort the events based on the sort order of the insert events
            Collections.sort(listOfEvents, new Comparator<InsertEvent>() {
                @Override
                public int compare(InsertEvent left, InsertEvent right) {
                    //compare based on the string sort order
                    return left.getSortOrder().compareTo(right.getSortOrder());
                }
            });
        }

        //go through each extended doc
        for (ExtendedDocument doc : allDocs.values()) {
            //if there is a list of events for that doc
            if (allInserts.containsKey(doc.getId())) {
                //get the doc's sorted list of events
                doc.addInsertEvents(allInserts.get(doc.getId()));
            }
        }
    }

    /**
     * Initially, before the user chooses the actual filter, the client will ask for
     * the 'filter parameters'. These are the set of all start/end times, docs, and
     * devs in a node lineage. This information is sent back to the client to populate
     * the filter menu. The creator of the playback can then edit/unselect these items in
     * order to filter items and it will send a playback filter back to the server.
     * <p>
     * This method is called in order for the GUI to present the filter information
     * to the user. It does not represent the choices a user has already made.
     */
    public JSONObject createFilterInfo(boolean returnJSON) throws DBAbstractionException, JSONException {
        //create a JSON object with the filter info (if it is requested)
        JSONObject returnFilter = null;

        //get the last node's id and sequence number of the latest relevant event
        String lastNodesId = getLastNode().getNodeId();
        int lastNodesSequenceNumber = getLastNode().getLastEvent().getNodeSequenceNum();

        //get the first and last event in the node sequence
        StorytellerEvent firstEvent = getRootNode().getFirstEvent();
        StorytellerEvent lastEvent = getLastNode().getLastEvent();

        //get the earliest event and the latest events' timestamp
        long startTime = firstEvent.getTimestamp().getTime();
        long endTime = lastEvent.getTimestamp().getTime();

        //recreate the state of the file system without the text events and with deleted docs/dirs
        ExtendedDirectory root = getTheStateOfTheFileSystem(false, true);

        //holds all of the directories in the node lineage
        List<ExtendedDirectory> allDirs = new ArrayList<ExtendedDirectory>();
        root.getAllDirectories(allDirs);

        //get only the directory ids
        List<String> allDirIds = new ArrayList<String>();
        for (ExtendedDirectory dir : allDirs) {
            allDirIds.add(dir.getId());
        }

        //holds all of the documents in the node lineage
        List<ExtendedDocument> allDocs = new ArrayList<ExtendedDocument>();
        root.getAllDocuments(allDocs);

        //get only the document ids
        List<String> allDocIds = new ArrayList<String>();
        for (ExtendedDocument doc : allDocs) {
            allDocIds.add(doc.getId());
        }

        //get all the developer groups who contributed to the playback session
        Set<DeveloperGroup> devGroups = getDeveloperGroupsWhoContributed();

        //get only the dev group ids
        List<String> allDevgroupIds = new ArrayList<String>();
        for (DeveloperGroup devGroup : devGroups) {
            allDevgroupIds.add(devGroup.getId());
        }

        //now set the filter info
        getFilter().setNodeID(lastNodesId);
        getFilter().setNodeSequenceNumber(lastNodesSequenceNumber);
        getFilter().setStartTime(startTime);
        getFilter().setEndTime(endTime);
        getFilter().setAcceptableDocumentIDs(allDocIds);
        getFilter().setAcceptableDeveloperGroupIDs(allDevgroupIds);

        //TODO make this a JSONiffy method
        //if the user wants a JSON object with filter info in it
        if (returnJSON) {
            //create a JSON object with the filter info
            returnFilter = new JSONObject();

            //store node id and sequence number
            returnFilter.put(Constants.NODE_ID, lastNodesId);
            returnFilter.put(Constants.NODE_SEQUENCE_NUMBER, lastNodesSequenceNumber);

            //start and end time
            returnFilter.put(Constants.START_TIME, startTime);
            returnFilter.put(Constants.END_TIME, endTime);

            //store the docs and dirs in the node lineage
            returnFilter.put(Constants.DOCUMENT_ARRAY, JSONiffy.toJSONDocumentArray(allDocs));
            returnFilter.put(Constants.DIRECTORY_ARRAY, JSONiffy.toJSONDirectoryArray(allDirs));

            //create a JSON array of dev groups
            JSONArray devGroupsJSON = new JSONArray();

            //go through all of the dev groups
            for (DeveloperGroup devGroup : devGroups) {
                //get all the devs in the dev group
                List<Developer> devsInAGroup = getDatabase().getDevelopersInADeveloperGroup(devGroup.getId());

                //create a json object for the dev group (and the devs in it) and add it to the JSON array
                devGroupsJSON.put(JSONiffy.toJSONDevGroup(devGroup, devsInAGroup));
            }

            //add the array of dev groups to the JSON object
            returnFilter.put(Constants.DEVELOPER_GROUPS, devGroupsJSON);

            //get a list of all the distinct developers
            List<Developer> allDevs = getDatabase().getDevelopersInDeveloperGroups(allDevgroupIds);

            //add the array of dev groups to the JSON object
            returnFilter.put(Constants.DEVELOPERS, JSONiffy.toJSONDeveloper(allDevs));
        }

        return returnFilter;
    }

    /**
     * Returns the last node (farthest from the root) in the list of playback nodes
     */
    public PlaybackNode getLastNode() {
        return playbackNodes.get(playbackNodes.size() - 1);
    }

    /**
     * Returns the root node
     */
    public PlaybackNode getRootNode() {
        return playbackNodes.get(0);
    }

    private void setId(String id) {
        this.playbackSessionId = id;
    }

    public String getId() {
        return playbackSessionId;
    }

    public String getLoggedInDeveloperGroupId() {
        return loggedInDeveloperGroupId;
    }

    public void setLoggedInDeveloperGroupId(String loggedInDeveloperGroupId) {
        this.loggedInDeveloperGroupId = loggedInDeveloperGroupId;
    }

    public void setPlaybackNodes(LinkedList<PlaybackNode> playbackNodes) {
        this.playbackNodes = playbackNodes;
    }

    public List<PlaybackNode> getPlaybackNodes() {
        return playbackNodes;
    }

    public PlaybackFilter getFilter() {
        return filter;
    }

    public void setFilter(PlaybackFilter filter) {
        this.filter = filter;
    }

    //filter getters
    public long getStartTime() {
        return filter.getStartTime();
    }

    public long getEndTime() {
        return filter.getEndTime();
    }

    public List<String> getDocIds() {
        return filter.getAcceptableDocumentIDs();
    }

    public List<String> getDeveloperGroupIDs() {
        return filter.getAcceptableDeveloperGroupIDs();
    }

    public boolean isShowPasteOrigin() {
        return filter.isShowPasteOrigin();
    }

    public int getHideDeletes() {
        return filter.getHideDeleteLimit();
    }

    public boolean isShowOnlyEndResult() {
        return filter.isShowOnlyEndResult();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PlaybackSession [id=");
        builder.append(playbackSessionId);
        builder.append(", developerGroupId=");
        builder.append(loggedInDeveloperGroupId);
        builder.append(", playbackNodes=");
        builder.append(playbackNodes);
        builder.append(", filter=");
        builder.append(filter);
        builder.append("]");
        return builder.toString();
    }

    public String getStateOfEventFiltering() {
        return stateOfEventFiltering;
    }

    public void setStateOfEventFiltering(String stateOfEventFiltering) {
        this.stateOfEventFiltering = stateOfEventFiltering;

        //simple logging of the time it takes to filter
        if (stateOfEventFiltering.equals(FILTERING)) {
            System.out.println("Start filtering at: " + System.currentTimeMillis());
        } else if (stateOfEventFiltering.equals(DONE_FILTERING)) {
            System.out.println("Stop filtering at:  " + System.currentTimeMillis());
        }
    }

    public LinkedList<StorytellerEvent> getEventsToPlayback() {
        return eventsToPlayback;
    }

    public void setEventsToPlayback(LinkedList<StorytellerEvent> eventsToPlayback) {
        this.eventsToPlayback = eventsToPlayback;
    }

    public DBAbstraction getDatabase() {
        return database;
    }

    public void setDatabase(DBAbstraction database) {
        this.database = database;
    }
}
