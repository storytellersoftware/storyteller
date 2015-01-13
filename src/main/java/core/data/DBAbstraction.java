package core.data;

import core.entities.*;
import core.events.*;
import util.Pair;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This interface describes what a Storyteller database can do.
 * Subclasses will implement this interface to do things in a
 * db specific way (sqlite, for example).
 */
public interface DBAbstraction {
    public static final DBAbstractionException NOT_IMPLEMENTED_EXCEPTION = new DBAbstractionException("Method not implemented");

    //preparing the database for use

    /**
     * This method prepares a database for use.
     *
     * @param pathToFile The path to the sqlite file that contains all the data.
     */
    public void open(String pathToFile) throws DBAbstractionException;

    /**
     * This closes the db and frees up any resources.
     */
    public void close() throws DBAbstractionException;

    //========================================================================================================================
    //inserting events

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(InsertEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(DeleteEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(CreateDocumentEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(RenameDocumentEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(MoveDocumentEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(DeleteDocumentEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(CreateDirectoryEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(RenameDirectoryEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(MoveDirectoryEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(DeleteDirectoryEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(MergeEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(AutomaticConflictEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(ManualConflictEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate event and stuffing it in the database.
     *
     * @param event
     */
    public void insertEvent(ConflictResolutionEvent event) throws DBAbstractionException;

    //========================================================================================================================
    //Inserting anything other than an event

    /**
     * Adds project to the database, taking all relevant info needed to recreate node and stuffing it in the database.
     *
     * @param newProject
     * @throws DBAbstractionException
     */
    public void insertProject(Project newProject) throws DBAbstractionException;

    /**
     * Adds node to the database, taking all relevant info needed to recreate node and stuffing it in the database.
     *
     * @param node
     */
    public void insertNode(Node node) throws DBAbstractionException;

    /**
     * Adds developer to the database, taking all relevant info needed to recreate developer and stuffing it in the database.
     *
     * @param developer
     */
    public void insertDeveloper(Developer developer) throws DBAbstractionException;

    /**
     * Adds developerGroup to the database, taking all relevant info needed to recreate developerGroup and stuffing it in the database.
     *
     * @param developerGroup
     */
    public void insertDeveloperGroup(DeveloperGroup developerGroup) throws DBAbstractionException;

    /**
     * Adds document to the database, taking all relevant info needed to recreate document and stuffing it in the database.
     * Be sure to complement this with an insertEvent(CreateDocumentEvent event)
     *
     * @param document
     */
    public void insertDocument(Document document) throws DBAbstractionException;

    /**
     * Helper to insert a new developer and create that new developer's initial developer group
     *
     * @param firstName
     * @param lastName
     * @param email
     * @param createdUnderNodeId
     * @param createdByDeveloperId
     * @throws DBAbstractionException
     */
    public Pair<DeveloperGroup, Developer> addADeveloperAndCreateANewDeveloperGroup(Date creationDate, String firstName, String lastName,
                                                                                    String email, String createdUnderNodeId,
                                                                                    String createdByDeveloperId) throws DBAbstractionException;

    public void insertDirectory(Directory directory) throws DBAbstractionException;

    /**
     * Adds clip to the database, taking all relevant info needed to recreate clip and stuffing it in the database.
     *
     * @param clip
     */
    public void insertClip(Clip clip) throws DBAbstractionException;

    /**
     * Adds storyboard to the database, taking all relevant info needed to recreate storyboard and stuffing it in the database.
     *
     * @param storyboard
     */
    public void insertStoryboard(Storyboard storyboard) throws DBAbstractionException;

    /**
     * Adds clipComment to the database, taking all relevant info needed to recreate clipComment and stuffing it in the database.
     *
     * @param clipComment
     */
    public void insertClipComment(ClipComment clipComment) throws DBAbstractionException;

    //=========================================================================================================================
    //Joining tables

    /**
     * Joins clip into storyboard at a given position
     *
     * @param clip
     * @param storyboard
     * @param pos
     */
    public void joinClipAndStoryboard(Clip clip, Storyboard storyboard, int pos) throws DBAbstractionException;

    /**
     * Removes clip from storyboard
     *
     * @param clip
     * @param storyboard
     */
    public void unjoinClipAndStoryboard(Clip clip, Storyboard storyboard) throws DBAbstractionException;

    /**
     * Adds developerWhoIsJoiningGroup to developerGroupToJoin and stores when developerWhoIsJoiningGroup joined and
     * who "allowed" them into the group.
     *
     * @param developerWhoIsJoiningGroup
     * @param developerGroupToJoin
     * @param joinedUnderNodeId
     * @param createdByDeveloperGroupId
     */
    public void joinDeveloperAndDeveloperGroup(Developer developerWhoIsJoiningGroup, DeveloperGroup developerGroupToJoin,
                                               String joinedUnderNodeId, String createdByDeveloperGroupId) throws DBAbstractionException;

    //=========================================================================================================================
    //methods for getting stuff out of the database in a usable form

    /**
     * Gets a single StorytellerEvent from the database based on the passed in id.
     *
     * @param id
     */
    public StorytellerEvent getEvent(String id) throws DBAbstractionException;

    /**
     * This method will retrieve an event based on the node it is in and the sequence number
     * within that node.
     *
     * @param nodeId         node id to find the event
     * @param sequenceNumber sequence number within the node
     * @return
     */
    public StorytellerEvent getEvent(String nodeId, int sequenceNumber) throws DBAbstractionException;

    public DeleteEvent getDeleteEventForAnInsertEvent(String nodeId, int seqNumber, InsertEvent event) throws DBAbstractionException;

    /**
     * Gets a single Node from the database based on the passed in id.
     *
     * @param id
     */
    public Node getNode(String id) throws DBAbstractionException;

    /**
     * Gets a single Developer from the database based on the passed in id.
     *
     * @param id
     */
    public Developer getDeveloper(String id) throws DBAbstractionException;

    /**
     * Gets a single Project from the database based on the project id
     *
     * @param id
     * @return
     */
    public Project getProject(String id) throws DBAbstractionException;

    /**
     * Gets a single Project from the database based on the project name
     *
     * @param projectName
     * @return
     * @throws DBAbstractionException
     */
    public Project getProjectByName(String projectName) throws DBAbstractionException;

    /**
     * Gets a single Developer from the database based on the email address
     *
     * @param emailAddress
     * @return
     */
    public Developer getDeveloperByEmailAddress(String emailAddress) throws DBAbstractionException;

    /**
     * Gets a single DeveloperGroup from the database based on the passed in id.
     *
     * @param id
     */
    public DeveloperGroup getDeveloperGroup(String id) throws DBAbstractionException;

    /**
     * Gets the DeveloperGroup with just a single developer in it from the database based on the
     * passed in developer id.
     *
     * @param developerId
     */
    public DeveloperGroup getDevelopersSoleDeveloperGroup(String developerId) throws DBAbstractionException;

    /**
     * Gets a single DeveloperGroup from the database of which all of the developers passed in (by ids in devIds) belong to
     *
     * @param devIds
     */
    public DeveloperGroup getDeveloperGroupByDeveloperIds(List<String> devIds) throws DBAbstractionException;

    /**
     * Gets all the developers who are a part of the developerGroup with the id groupId.  This very well could be just one developer.
     *
     * @param groupId
     * @return
     */
    public List<Developer> getDevelopersInADeveloperGroup(String groupId) throws DBAbstractionException;

    /**
     * Gets all the developers who are a part of the developerGroups passed in.  This very well could be just one developer.
     *
     * @param devGroupIds
     * @return
     */
    public List<Developer> getDevelopersInDeveloperGroups(List<String> devGroupIds) throws DBAbstractionException;

    /**
     * Gets a single Document from the database based on the passed in id.
     * Recall that Documents do not have their name stored with them.  If you want the name of the document
     * you'll need to talk to StorytellerServer and ask politely.
     *
     * @param id
     * @return
     */
    public Document getDocument(String id) throws DBAbstractionException;

    /**
     * Gets a single Directory from the database based on the passed in id.
     * Recall that Directories do not have their name stored with them.  If you want the name of the directory
     * you'll need to talk to StorytellerServer and ask politely.
     *
     * @param id
     * @return
     */
    public Directory getDirectory(String id) throws DBAbstractionException;

    /**
     * Gets the last event that has the createdUnderNodeId of nodeID.  Does not do any sort of node familial computation.
     * That is/should be done in StorytellerServer.
     *
     * @param nodeID
     * @return
     */
    public StorytellerEvent getLastEventInNode(String nodeID) throws DBAbstractionException;

    /**
     * Gets the first event that has the createdUnderNodeId of nodeID.  Does not do any sort of node familial computation.
     * That is/should be done in StorytellerServer.
     *
     * @param nodeID
     * @return
     */
    public StorytellerEvent getFirstEventInNode(String nodeID) throws DBAbstractionException;

    /**
     * Gets all events that have the createdUnderNodeId of nodeID.  Does not do any sort of node familial computation.
     * That is/should be done in StorytellerServer.
     *
     * @param id
     * @return
     */
    public List<StorytellerEvent> getEventsByNode(String id) throws DBAbstractionException;

    /**
     * Gets a group of events in a playback node. The user passes in a node id and the max sequence number
     * to identify the node. The user passes in a string with the event type (which can be gotten from
     * any event class's static name member) and a list of document ids to look in.
     */
    public List<StorytellerEvent> getEventsByType(String nodeId, int numEventsInNode, String typeOfEvent, List<String> documentIds) throws DBAbstractionException;

    /**
     * Gets a group of events in a playback node. The user passes in a node id and the max sequence number
     * to identify the node. The user passes in a string with the event type (which can be gotten from
     * any event class's static name member).
     */
    public List<StorytellerEvent> getEventsByType(String nodeId, int numEventsInNode, String typeOfEvent) throws DBAbstractionException;

    public List<StorytellerEvent> getEventsInNodeFromOneEventToAnother(String nodeId, int startNodeSequenceNum,
                                                                       int endNodeSequenceNum, List<String> acceptableDocIds) throws DBAbstractionException;

    public List<StorytellerEvent> getEventsByNodeAndSequenceNumber(String nodeId, int sequenceNumber,
                                                                   List<String> acceptableDocIds) throws DBAbstractionException;

    public StorytellerEvent getFirstRelevantEventInNode(String nodeId, int sequenceNumber, long startTime, long endTime,
                                                        List<String> documentIDs, List<String> developerGroupIDs) throws DBAbstractionException;

    public StorytellerEvent getFirstRelevantEventInNode(String nodeId, int nodeSequenceNum, List<String> selectedAndRelevantEventIds) throws DBAbstractionException;

    public StorytellerEvent getLastRelevantEventInNode(String nodeId, int sequenceNumber, long startTime, long endTime,
                                                       List<String> documentIDs, List<String> developerGroupIDs) throws DBAbstractionException;

    public StorytellerEvent getCreateDirectoryEvent(String dirId) throws DBAbstractionException;

    public StorytellerEvent getCreateDocumentEvent(String docId) throws DBAbstractionException;

    /**
     * Converts a list of event ids to a list of event objects
     *
     * @param eventIds
     * @return
     * @throws DBAbstractionException
     */
    public List<StorytellerEvent> getAllEventsByIds(List<String> eventIds) throws DBAbstractionException;

    /**
     * Converts a list of event ids to a list of insert event objects
     *
     * @param eventIds
     * @return
     * @throws DBAbstractionException
     */
    public List<InsertEvent> getAllInsertEventsByIds(List<String> eventIds) throws DBAbstractionException;


    /**
     * Here are the rules about determining what events are relevant when there
     * are selected events in a playback:
     * 1. All selected events are relevant
     * 2. Each selected event's previous neighbor is relevant with the following
     * exceptions:
     * - previous neighbors who are already selected (and therefore relevant)
     * are not added a second time
     * - the first selected event (the leftmost event) will not have its
     * previous neighbor marked as relevant since this is where the user
     * decided to stop selecting
     * 3. Any insert event that has a previous neighbor of one of the selected
     * events is relevant with the following exception:
     * - events already marked as relevant are not added a second time
     * - the last selected event (the rightmost event) will not participate
     * in the algorithm
     * <p>
     * Events selected event in this group fall into one of two categories-
     * they have either been deleted or have not.
     * <p>
     * If the event that backs up to a selected event has been deleted:
     * - we will look for event that backs to the back link that was also
     * deleted. If there is one we will mark it as relevant and repeat
     * this process until there are no more. This will show blocks that
     * were at one time on the screen but then later deleted that would
     * not show up in the playback but are part of the history
     * If the event that backs up to a selected event has not been deleted:
     * - the events selected in this step will be searched for
     * 'consecutive insert' events. A consecutive insert is any event that
     * has a previous neighbor id and sequentially before id equal to the
     * event. This cannot be any event from the originally selected events list.
     * These represent events that were directly related to the event.
     * *Optionally- only look for consecutive inserts until a white space is
     * reached. This will find groups of related blocks up to the next word.
     */
    //public List<StorytellerEvent> getRelevantEventsFromSelectedEventsByNode(List < PlaybackNode > playbackNodes, List < String > selectedEventIds) throws DBAbstractionException;
    public List<InsertEvent> getInsertEventsThatBackUpToEventsByNode(String nodeId, int nodeSequenceNum,
                                                                     List<String> idsToLookFor, List<String> idsToIgnore) throws DBAbstractionException;

    /**
     * Gets a single Clip from the database based on the passed in id.
     *
     * @param id
     * @return
     */
    public Clip getClip(String id) throws DBAbstractionException;

    /**
     * Get all clips that are in the database
     *
     * @return
     */
    public List<Clip> getAllClips() throws DBAbstractionException;

    /**
     * Get all clips associated with the storyboard that has the id storyboardId
     *
     * @param storyboardId
     * @return
     */
    public List<Clip> getClipsAssociatedWithStoryboard(String storyboardId) throws DBAbstractionException;

    /**
     * Get all clips made by the passed in devGroups (passed in by ids)
     *
     * @param ids
     * @return
     */
    public List<Clip> getAllClipsByDevGroups(List<String> ids) throws DBAbstractionException;

    /**
     * Gets all clips that exist between startTime and endTime
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Clip> getAllClipsByTime(Date startTime, Date endTime) throws DBAbstractionException;

    /**
     * Gets all developers in the db
     *
     * @return A list of developer objects
     */
    public List<Developer> getAllDevelopers() throws DBAbstractionException;

    public List<DeveloperGroup> getAllDeveloperGroupsInANode(String nodeId, int numEventsInNode) throws DBAbstractionException;

    /**
     * Gets the storyboard that has the id of id
     *
     * @param id
     * @return
     */
    public Storyboard getStoryboard(String id) throws DBAbstractionException;

    /**
     * Gets all storyboards from the database
     *
     * @return
     * @throws DBAbstractionException
     */
    public List<Storyboard> getAllStoryboards() throws DBAbstractionException;

    /**
     * Get all Storyboards made by the passed in devGroups (passed in by ids)
     *
     * @param ids
     * @return
     */
    public List<Storyboard> getAllStoryboardsByDevGroups(List<String> ids) throws DBAbstractionException;

    /**
     * Gets all Storyboards that exist between startTime and endTime
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Storyboard> getAllStoryboardsByTime(Date startTime, Date endTime) throws DBAbstractionException;

    /**
     * Gets all storyboards that include the clip referenced by clipId
     *
     * @param clipId
     * @return
     */
    public List<Storyboard> getAllStoryboardsAssociatedWithAClip(String clipId) throws DBAbstractionException;

    /**
     * Gets the clipComment that has the given id
     *
     * @param id
     * @return
     */
    public ClipComment getClipComment(String id) throws DBAbstractionException;

    /**
     * Gets all the clip comments that are in the clip referenced by clipId
     *
     * @param clipId
     * @return
     * @throws DBAbstractionException
     */
    public List<ClipComment> getAllClipCommentsAssociatedWithAClip(String clipId) throws DBAbstractionException;

    //========================================================================================================================
    //updates

    /**
     * Updates all data affiliated with node (usually closing it)
     *
     * @param node
     */
    public void updateNode(Node node) throws DBAbstractionException;

    /**
     * Updates all data affiliated with developer
     *
     * @param developer
     */
    public void updateDeveloper(Developer developer) throws DBAbstractionException;

    /**
     * Updates all data affiliated with document
     *
     * @param document
     */
    public void updateDocument(Document document) throws DBAbstractionException;

    /**
     * Updates all data affiliated with directory
     *
     * @param directory
     */
    public void updateDirectory(Directory directory) throws DBAbstractionException;


    /**
     * Given a list of nodeIds to look in, update the insertEvents list with whether or not they have been deleted
     * <p>
     * DOES NOT and SHOULD NOT modify the database
     *
     * @param nodeIds
     * @param insertEvents
     */
    public void updateInsertEventsWithDeletedTimestamps(List<String> nodeIds, Map<String, InsertEvent> insertEvents) throws DBAbstractionException;

    /**
     * Updates all data affiliated with clip
     *
     * @param clip
     */
    public void updateClip(Clip clip) throws DBAbstractionException;

    /**
     * Updates all data affiliated with storyboard
     *
     * @param storyboard
     */
    public void updateStoryboard(Storyboard storyboard) throws DBAbstractionException;

    /**
     * Updates all data affiliated with clipComment
     *
     * @param clipComment
     */
    public void updateClipComment(ClipComment clipComment) throws DBAbstractionException;

    /**
     * Updates the position of clipId within storyboardId
     *
     * @param clipId
     * @param storyboardId
     * @param newPosition
     */
    public void updateClipPositionInStoryboard(String clipId, String storyboardId, int newPosition) throws DBAbstractionException;

    //========================================================================================================================
    //deletes

    /**
     * Removes event from the database.  This is nonrecoverable.  Really shouldn't ever be called. If you want to delete text,
     * use insertEvent(DeleteEvent event).
     *
     * @param event
     */
    public void deleteEvent(StorytellerEvent event) throws DBAbstractionException;

    /**
     * Removes node from the database.  This is nonrecoverable.  Really shouldn't ever be called.
     *
     * @param node
     */
    public void deleteNode(Node node) throws DBAbstractionException;

    /**
     * Removes developer from the database.  This is nonrecoverable.
     *
     * @param developer
     */
    public void deleteDeveloper(Developer developer) throws DBAbstractionException;

    /**
     * Removes developerGroup from the database.  This is nonrecoverable.
     *
     * @param developerGroup
     */
    public void deleteDeveloperGroup(DeveloperGroup developerGroup) throws DBAbstractionException;

    /**
     * Removes developerWhoIsLeavingGroup from developerGroupToQuit.  This is nonrecoverable.
     *
     * @param developerWhoIsLeavingGroup
     * @param developerGroupToQuit
     */
    public void removeDeveloperFromDeveloperGroup(Developer developerWhoIsLeavingGroup, DeveloperGroup developerGroupToQuit) throws DBAbstractionException;

    /**
     * Removes document from the database.  This is nonrecoverable. Really shouldn't ever be called.
     * Use insertEvent(DeleteDocumentEvent event) for when the user deletes the Document.
     *
     * @param document
     */
    public void deleteDocument(Document document) throws DBAbstractionException;

    /**
     * Removes directory from the database.  This is nonrecoverable. Really shouldn't ever be called.
     * Use insertEvent(DeleteDirectoryEvent event) for when the user deletes the Directory.
     *
     * @param directory
     */
    public void deleteDirectory(Directory directory) throws DBAbstractionException;

    /**
     * Removes clip from the database.  This is nonrecoverable.
     *
     * @param clip
     */
    public void deleteClip(Clip clip) throws DBAbstractionException;

    /**
     * Removes storyboard from the database.  This is nonrecoverable.
     *
     * @param storyboard
     * @throws DBAbstractionException
     */
    public void deleteStoryboard(Storyboard storyboard) throws DBAbstractionException;

    /**
     * Removes clip comment from the database.  This is nonrecoverable.
     *
     * @param clipComment
     */
    public void deleteClipComment(ClipComment clipComment) throws DBAbstractionException;

    //========================================================================================================================
    //Node related things

    /**
     * Adds event to the database, taking all relevant info needed to recreate an OpenNodeEvent and stuffing it in the database.
     * Should probably be used in conjunction with insertNode(Node)
     *
     * @param event
     */
    public void insertEvent(OpenNodeEvent event) throws DBAbstractionException;

    /**
     * Adds event to the database, taking all relevant info needed to recreate an CloseNodeEvent and stuffing it in the database.
     * Should probably be used in conjuction with updateNode(Node) (when it gets closed)
     *
     * @param event
     */
    public void insertEvent(CloseNodeEvent event) throws DBAbstractionException;

    /**
     * @return
     */
    public List<Node> getAllNodes() throws DBAbstractionException;

    /**
     * Gets all the projects from the database
     *
     * @return
     * @throws DBAbstractionException
     */
    public List<Project> getAllProjects() throws DBAbstractionException;

    /**
     * @param childNodeId
     * @return
     */
    public List<Node> getAllNodesInALineage(String childNodeId, boolean includePassedInNode) throws DBAbstractionException;

    /**
     * Gets all events  relevant to the future that occur in a given merge node
     *
     * @param nodeId
     * @return
     */
    public List<StorytellerEvent> getAllEventsRelevantToFutureInMergeNode(String mergeNodeId) throws DBAbstractionException;

    /**
     * Returns the documents that occur in just this node.  Should only be called if the node is not a merge node.
     *
     * @param nodeId
     * @return
     */
    public List<Document> getAllDocumentsCreatedInANode(String nodeId) throws DBAbstractionException;

    /**
     * Returns all the documents in the db
     *
     * @return
     * @throws DBAbstractionException
     */
    public List<Document> getAllDocuments() throws DBAbstractionException;

    /**
     * Get all devgroups that exist in the list of nodeIds
     *
     * @param nodeIDs
     * @return
     */
    public List<DeveloperGroup> getAllDevGroupsInNodes(List<String> nodeIDs) throws DBAbstractionException;

    /**
     * Get all the dev groups in the database
     *
     * @return
     * @throws DBAbstractionException
     */
    public List<DeveloperGroup> getAllDeveloperGroups() throws DBAbstractionException;

    //TODO - put this somewhere better
    public void associateEventsWithClip(List<String> listOfEventIds, String clipId, String devGroupId) throws DBAbstractionException;

    public List<Storyboard> getAllStoryboardsAssociatedWithEvents(List<String> listOfEventIds) throws DBAbstractionException;

    public String getAllClipIdsAssociatedWithEvents(List<String> listOfEventIds) throws DBAbstractionException;

    /**
     * updates the developer whose email matches with the firstName and lastName.
     *
     * @param firstName
     * @param lastName
     * @param email
     */
    public void updateDeveloper(String firstName, String lastName, String email) throws DBAbstractionException;

    /**
     * Gets all events in the list of eventIds
     *
     * @param eventIds
     * @return
     * @throws DBAbstractionException
     */
    public Node getOpenNode() throws DBAbstractionException;

    public Node getRootNode() throws DBAbstractionException;


    //================================================================
    //clean up data
    /**
     * This method will go through all the documents in the database and
     * shorten the sort orders to be as small as possible.
     */
    //public void shortenAllSortOrders() throws DBAbstractionException;

    /**
     * This method will go through all the events in a document and make the
     * sort orders as short as possible. Sort orders can get quite long if
     * they are not cleaned up periodically.
     *
     * @param docId The id of the document to shorten the sort order field
     */
    //public void shortenSortOrders(String docId) throws DBAbstractionException;

}
