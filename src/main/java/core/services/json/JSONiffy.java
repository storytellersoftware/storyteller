package core.services.json;

import core.Constants;
import core.entities.*;
import core.events.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import playback.PlaybackFilter;

import java.util.List;

import static core.Constants.*;


// TODO write some javadocs
public class JSONiffy {

    public static JSONObject toJSON(StorytellerEntity entity, List<? extends StorytellerEntity> relatedEntities) throws JSONException {
        JSONObject object = new JSONObject();

        // Every StorytellerEntity has this
        object.put(Constants.ID, entity.getId());
        object.put(Constants.TIMESTAMP, entity.getTimestamp().getTime());
        object.put(Constants.CREATED_UNDER_NODE_ID, entity.getCreatedUnderNodeId());
        object.put(Constants.DEVELOPER_GROUP_ID, entity.getCreatedByDevGroupId());

        if (entity instanceof DeveloperGroup) {
            developerGroup(entity, relatedEntities, object);
        } else if (entity instanceof Clip) {
            clip(entity, relatedEntities, object);
        } else {
            throw new JSONException("The passed in Storyteller entity cannot be converted into JSON");
        }

        return object;
    }

    public static JSONObject toJSON(StorytellerEntity entity) throws JSONException {
        JSONObject object = new JSONObject();
        // Every StorytellerEntity has this
        object.put(Constants.ID, entity.getId());
        object.put(Constants.TIMESTAMP, entity.getTimestamp().getTime());
        object.put(Constants.CREATED_UNDER_NODE_ID, entity.getCreatedUnderNodeId());
        object.put(Constants.DEVELOPER_GROUP_ID, entity.getCreatedByDevGroupId());

        // Now do specific things for each object
        if (entity instanceof ClipComment) {
            clipComment(entity, object);
        } else if (entity instanceof Developer) {
            developer(entity, object);
        } else if (entity instanceof ExtendedDirectory) {
            extendedDirectory(entity, object);
        } else if (entity instanceof ExtendedDocument) {
            extendedDocument(entity, object);
        } else if (entity instanceof Directory) {
            directory(entity, object);
        } else if (entity instanceof Document) {
            document(entity, object);
        } else if (entity instanceof Node) {
            node(entity, object);
        } else if (entity instanceof Project) {
            project(entity, object);
        } else if (entity instanceof Storyboard) {
            storyboard(entity, object);
        } else if (entity instanceof StorytellerEvent) {
            storytellerEvent(entity, object);
        } else {
            throw new JSONException("The passed in Storyteller entity cannot be converted into JSON");
        }

        return object;
    }

    /**
     * Turn a List of StorytellerEntities into a JSONArray of JSON
     * representations of those entities.
     *
     * @param entityList List of StorytellerEntities to be turned into
     *                   a JSONArray.
     * @throws JSONException When something bad happens while trying
     *                       to create the JSON representations.
     * @return JSONArray of JSONObjects representing the elements
     * of the passed in entityList. If the entityList is
     * empty, an empty JSONarray is returned.
     */
    public static JSONArray toJSON(List<? extends StorytellerEntity> entityList) throws JSONException {
        // they passed in an empty list, they receive an empty JSONArray.
        // it's only fair...
        if (entityList.isEmpty()) {
            return new JSONArray();
        }


        JSONArray object = new JSONArray();
        for (StorytellerEntity entity : entityList) {
            JSONObject json = toJSON(entity);
            object.put(json);
        }

        return object;
    }

    public static JSONArray toJSON(List<? extends StorytellerEntity> entityList, List<? extends List<? extends StorytellerEntity>> relatedEntityLists) throws JSONException {
        // they passed in an empty list, they receive an empty JSONArray.
        // it's only fair...
        if (entityList.isEmpty()) {
            return new JSONArray();
        }


        JSONArray object = new JSONArray();
        for (int i = 0; i < entityList.size(); i++)
        //for(StorytellerEntity entity: entityList)
        {
            JSONObject json = toJSON(entityList.get(i), relatedEntityLists.get(i));
            object.put(json);
        }

        return object;
    }

    public static JSONObject toJSON(PlaybackFilter filter) throws JSONException {

        JSONObject object = new JSONObject();

        object.put(Constants.NODE_ID, filter.getNodeID());
        object.put(Constants.NODE_SEQUENCE_NUMBER, filter.getNodeSequenceNumber());
        object.put(Constants.START_TIME, filter.getStartTime());
        object.put(Constants.END_TIME, filter.getEndTime());

        object.put(Constants.HIDE_DELETE_LIMIT, filter.getHideDeleteLimit());
        object.put(Constants.SHOW_ONLY_END_RESULT, filter.isShowOnlyEndResult());
        object.put(Constants.SHOW_PASTE_ORIGIN, filter.isShowPasteOrigin());

        object.put(Constants.RELEVANT_BLOCK_TYPE, filter.getRelevantBlockType());

        object.put(Constants.DOCUMENT_IDS, new JSONArray(filter.getAcceptableDocumentIDs().toString()));
        object.put(Constants.DEVELOPER_GROUP_IDS, new JSONArray(filter.getAcceptableDeveloperGroupIDs().toString()));
        if (filter.getSelectedAndRelevantEventIds() != null &&
                !filter.getSelectedAndRelevantEventIds().isEmpty()) {
            object.put(Constants.RELEVANT_EVENTS, new JSONArray(filter.getSelectedAndRelevantEventIds().toString()));
        }

        return object;
    }

    public static JSONObject toJSONDevGroup(DeveloperGroup devGroup, List<Developer> devs) throws JSONException {
        //holds a dev group
        JSONObject devGroupJSON = new JSONObject();

        //store the dev group id
        devGroupJSON.put(Constants.DEVELOPER_GROUP_ID, devGroup.getId());

        //holds all the devs in the group
        JSONArray devsJSON = new JSONArray();

        //add the dev info
        for (Developer dev : devs) {
            //add the dev id to the array of devs
            devsJSON.put(dev.getId());
        }

        //add the array to the dev group object
        devGroupJSON.put(Constants.DEVELOPERS, devsJSON);

        return devGroupJSON;

        //[ {'devgroupid':'123',
        //   'developers':
        //                  [
        //                      {
        //                       'devId':'123',
        //                       'firstname':'Mark',
        //                       'lastname':'Mahoney',
        //                       'email':'mmahoney@carthage.edu'
        //                      },
        //
        //                      {
        //                       'devId':'124',
        //                       'firstname':'Laura',
        //                       'lastname':'Mahoney',
        //                       'email':'lauracmahoney@gmail.com'
        //                      }
        //                  ]
        //  }
        //  {'devgroupid':'124',
        //   'developers':
        //                  [
        //                      {
        //                       'devId':'125',
        //                       'firstname':'Buddy',
        //                       'lastname':'Mahoney',
        //                       'email':'buddymahoney04@gmail.com'
        //                      },
        //
        //                      {
        //                       'devId':'126',
        //                       'firstname':'Patrick',
        //                       'lastname':'Mahoney',
        //                       'email':'patrickmahoney07@gmail.com'
        //                      }
        //                  ]
        //  }
        //]
    }

    public static JSONObject toJSONDeveloper(List<Developer> devs) throws JSONException {
        //holds all the devs
        JSONObject devsJSON = new JSONObject();

        //add the dev info
        for (Developer dev : devs) {
            //holds a single dev
            JSONObject devJSON = new JSONObject();

            //store the dev info
            devJSON.put(Constants.DEVELOPER_ID, dev.getId());
            devJSON.put(Constants.FIRST_NAME, dev.getFirstName());
            devJSON.put(Constants.LAST_NAME, dev.getLastName());
            devJSON.put(Constants.EMAIL, dev.getEmail());

            //add the dev to the array of devs
            devsJSON.put(dev.getId(), devJSON);
        }

        return devsJSON;
    }

    public static JSONArray toJSONDirectoryArray(List<ExtendedDirectory> allDirs) throws JSONException {
        //the array of all the directories
        JSONArray allDirsJSON = new JSONArray();

        //for each dir
        for (ExtendedDirectory dir : allDirs) {
            //get the directory info
            String dirId = dir.getId();
            String dirName = dir.getName();
            String parentDirId = dir.getParentDirectoryId();

            //create a json object to represent a directory
            JSONObject aDir = new JSONObject();

            //set the dir members
            aDir.put(DIRECTORY_ID, dirId);
            aDir.put(DIRECTORY_NAME, dirName);
            aDir.put(PARENT_DIRECTORY_ID, parentDirId);

            //if this was a deleted directory
            if (dir.getWasDeleted()) {
                aDir.put(WAS_DELETED, "true");
            } else //directory was not deleted
            {
                aDir.put(WAS_DELETED, "false");
            }

            //add the directory object to the directory array
            allDirsJSON.put(aDir);
        }

        return allDirsJSON;
    }

    public static JSONArray toJSONDocumentArray(List<ExtendedDocument> allDocs) throws JSONException {
        //the array of all the documents
        JSONArray allDocsJSON = new JSONArray();

        //for each of the documents
        for (ExtendedDocument doc : allDocs) {
            String docId = doc.getId();
            String docName = doc.getName();
            String parentDirId = doc.getParentDirectoryId();

            //create a json object to represent a document
            JSONObject aDoc = new JSONObject();

            //set the doc members
            aDoc.put(DOCUMENT_ID, docId);
            aDoc.put(DOCUMENT_NAME, docName);
            aDoc.put(PARENT_DIRECTORY_ID, parentDirId);

            //if the doc was deleted
            if (doc.getWasDeleted()) {
                aDoc.put(WAS_DELETED, "true");
            } else {
                aDoc.put(WAS_DELETED, "false");
            }

            //if there are insert events associated with document
            if (doc.getInsertEvents() != null && doc.getInsertEvents().size() > 0) {
                //an array of insert events
                JSONArray insertEventsJSON = new JSONArray();

                //go through all the inserts
                for (InsertEvent insertEvent : doc.getInsertEvents()) {
                    //create a json object for the insert
                    JSONObject insertEventJSON = JSONiffy.toJSON(insertEvent);

                    //add it to the array
                    insertEventsJSON.put(insertEventJSON);
                }

                //add the events to the doc
                aDoc.put(EVENT_IDS, insertEventsJSON);
            }

            //add the document object to the document array
            allDocsJSON.put(aDoc);
        }

        return allDocsJSON;
    }

    private static void clipComment(StorytellerEntity entity, JSONObject object) throws JSONException {
        ClipComment comment = (ClipComment) entity;

        object.put(Constants.COMMENT_TEXT, comment.getText());
        object.put(Constants.EVENT_ID, comment.getDisplayCommentEventId());
        object.put(Constants.CLIP_ID, comment.getClipId());
        object.put(Constants.START_HIGHLIGHTED_EVENT_ID, comment.getStartHighlightEventId());
        object.put(Constants.END_HIGHLIGHTED_EVENT_ID, comment.getEndHighlightEventId());
    }

    private static void developer(StorytellerEntity entity, JSONObject object) throws JSONException {
        Developer developer = (Developer) entity;

        object.put(Constants.FIRST_NAME, developer.getFirstName());
        object.put(Constants.LAST_NAME, developer.getLastName());
        object.put(Constants.EMAIL, developer.getEmail());
    }

    private static void clip(StorytellerEntity entity, List<? extends StorytellerEntity> clipCommentEntities, JSONObject object) throws JSONException {
        Clip clip = (Clip) entity;

        object.put(Constants.FILTERS, toJSON(clip.getFilter()));
        object.put(Constants.NAME, clip.getName());
        object.put(Constants.PLAYBACK_NODE_ID, clip.getPlaybackNodeId());
        object.put(Constants.DESCRIPTION, clip.getDescription());

        object.put(Constants.COMMENTS, toJSON(clipCommentEntities));
    }

    private static void developerGroup(StorytellerEntity entity, List<? extends StorytellerEntity> developerEntities, JSONObject object) throws JSONException {
        DeveloperGroup devGroup = (DeveloperGroup) entity;

        object.put(Constants.DEVELOPERS, toJSON(developerEntities));
    }

    private static void extendedDocument(StorytellerEntity entity, JSONObject object) throws JSONException {
        ExtendedDocument document = (ExtendedDocument) entity;

        object.put(Constants.PARENT_DIRECTORY_ID, document.getParentDirectoryId());
        object.put(Constants.DIRECTORY_NAME, document.getName());
        object.put(Constants.DOCUMENT_TEXT, document.getText());

        if (document.getInsertEvents() != null && document.getInsertEvents().size() > 0) {
            object.put(Constants.EVENTS, toJSON(document.getInsertEvents()));
        }
    }

    private static void extendedDirectory(StorytellerEntity entity, JSONObject object) throws JSONException {
        ExtendedDirectory directory = (ExtendedDirectory) entity;

        object.put(Constants.PARENT_DIRECTORY_ID, directory.getParentDirectoryId());
        object.put(Constants.DIRECTORY_NAME, directory.getName());
        object.put(Constants.DIRECTORY_ARRAY, toJSON(directory.getSubdirectories()));
        object.put(Constants.DOCUMENT_ARRAY, toJSON(directory.getDocuments()));
    }

    private static void directory(StorytellerEntity entity, JSONObject object) throws JSONException {
        Directory directory = (Directory) entity;

        object.put(Constants.PARENT_DIRECTORY_ID, directory.getParentDirectoryId());
    }

    private static void document(StorytellerEntity entity, JSONObject object) throws JSONException {
        Document document = (Document) entity;

        object.put(Constants.PARENT_DIRECTORY_ID, document.getCreatedUnderNodeId());
    }

    private static void node(StorytellerEntity entity, JSONObject object) throws JSONException {
        Node node = (Node) entity;

        object.put(Constants.ID, node.getId());
        object.put(Constants.PARENT_NODE_ID, node.getCreatedUnderNodeId());
        object.put(Constants.LINEAGE_NUMBER, node.getNodeLineageNumber().intValue());
        object.put(Constants.NAME, node.getName());
        object.put(Constants.DESCRIPTION, node.getDescription());
        object.put(Constants.TYPE, node.getNodeType());
    }

    private static void project(StorytellerEntity entity, JSONObject object) throws JSONException {
        Project project = (Project) entity;

        object.put(Constants.NAME, project.getProjectName());
    }

    private static void storyboard(StorytellerEntity entity, JSONObject object) throws JSONException {
        Storyboard storyboard = (Storyboard) entity;

        object.put(Constants.NAME, storyboard.getName());
        object.put(Constants.DESCRIPTION, storyboard.getDescription());
    }


    /*	Now we're in to the Events section...
     */
    private static void storytellerEvent(StorytellerEntity entity, JSONObject object) throws JSONException {
        StorytellerEvent event = (StorytellerEvent) entity;
        //TODO the object already has an ID now it has an event id- can we get rid of one??
        //object.remove(Constants.ID);
        //or change the front end to use only ID not eventID

        object.put(Constants.EVENT_ID, event.getId());
        object.put(Constants.NODE_SEQUENCE_NUMBER, event.getNodeSequenceNum());
        object.put(Constants.PREVIOUS_EVENT_ID, event.getSequentiallyBeforeEventId());
        object.put(Constants.TYPE, event.getEventType());
        //TODO change this to a true false and add it to every event???
        //if the event is relevant add a relevant attribute to the event
        if (event.getIsRelevantForPlayback()) {
            object.put(Constants.RELEVANT, "true");
        }

        if (event.getDisplayAsRelevantButDoNotAnimate()) {
            object.put(Constants.DISPLAY_RELEVANT_BUT_DO_NOT_ANIMATE, "true");
        }
        //create a JSON representation of the event to send back to the client

        // grab specific event data
        if (event instanceof CombinatorialEvent) {
            combinatorialEvent(event, object);
        } else if (event instanceof DirectoryEvent) {
            directoryEvent(event, object);
        } else if (event instanceof DocumentEvent) {
            documentEvent(event, object);
        } else if (event instanceof TextEvent) {
            textEvent(event, object);
        } else if (event instanceof OpenNodeEvent) {
            openNodeEvent(event, object);
        }
    }

    private static void openNodeEvent(StorytellerEvent event, JSONObject object) throws JSONException {
        OpenNodeEvent one = (OpenNodeEvent) event;
        object.put(Constants.PREVIOUS_NODE_ID, one.getSequentiallyBeforeNodeID());
    }


    // combinatorial events
    private static void combinatorialEvent(StorytellerEvent event, JSONObject object) throws JSONException {
        CombinatorialEvent ce = (CombinatorialEvent) event;
        object.put(Constants.DOCUMENT_ID, ce.getDocumentId());

        if (ce instanceof ConflictResolutionEvent) {
            conflictResolutionEvent(ce, object);
        } else if (ce instanceof ManualConflictEvent) {
            manualConflictEvent(ce, object);
        }
    }

    private static void conflictResolutionEvent(CombinatorialEvent ce, JSONObject object) throws JSONException {
        ConflictResolutionEvent cre = (ConflictResolutionEvent) ce;

        object.put(Constants.BASE_RESOLUTION_BLOCK_ID, cre.getBaseResolutionBlockId());
        object.put(Constants.IDS_OF_EVENTS_IN_FIRST_BLOCK, cre.getIdsOfEventsInThisBlock());
    }

    private static void manualConflictEvent(CombinatorialEvent ce, JSONObject object) throws JSONException {
        ManualConflictEvent mce = (ManualConflictEvent) ce;
        object.put(Constants.IDS_OF_EVENTS_IN_FIRST_BLOCK, mce.getIdsOfEventsInFirstBlock());
        object.put(Constants.IDS_OF_EVENTS_IN_SECOND_BLOCK, mce.getIdsOfEventsInSecondBlock());
    }


    // directory events
    private static void directoryEvent(StorytellerEvent event, JSONObject object) throws JSONException {
        DirectoryEvent dirEvent = (DirectoryEvent) event;

        object.put(Constants.PARENT_DIRECTORY_ID, dirEvent.getParentDirectoryId());
        object.put(Constants.DIRECTORY_ID, dirEvent.getDirectoryId());

        if (dirEvent instanceof CreateDirectoryEvent) {
            createDirectoryEvent(dirEvent, object);
        } else if (dirEvent instanceof DeleteDirectoryEvent) {
            deleteDirectoryEvent(dirEvent, object);
        } else if (dirEvent instanceof MoveDirectoryEvent) {
            moveDirectoryEvent(dirEvent, object);
        } else if (dirEvent instanceof RenameDirectoryEvent) {
            renameDirectoryEvent(dirEvent, object);
        }
    }

    private static void createDirectoryEvent(DirectoryEvent dirEvent, JSONObject object) throws JSONException {
        CreateDirectoryEvent cde = (CreateDirectoryEvent) dirEvent;
        object.put(Constants.NEW_NAME, cde.getDirectoryNewName());
    }

    private static void deleteDirectoryEvent(DirectoryEvent dirEvent, JSONObject object) throws JSONException {
        DeleteDirectoryEvent dde = (DeleteDirectoryEvent) dirEvent;
        object.put(Constants.OLD_NAME, dde.getDirectoryOldName());
    }

    private static void moveDirectoryEvent(DirectoryEvent dirEvent, JSONObject object) throws JSONException {
        MoveDirectoryEvent mde = (MoveDirectoryEvent) dirEvent;
        object.put(Constants.DESTINATION_DIRECTORY_ID, mde.getNewParentDirectoryId());
    }

    private static void renameDirectoryEvent(DirectoryEvent dirEvent, JSONObject object) throws JSONException {
        RenameDirectoryEvent rde = (RenameDirectoryEvent) dirEvent;
        object.put(Constants.OLD_NAME, rde.getDirectoryOldName());
        object.put(Constants.NEW_NAME, rde.getDirectoryNewName());
    }


    // document events
    private static void documentEvent(StorytellerEvent event, JSONObject object) throws JSONException {
        DocumentEvent de = (DocumentEvent) event;

        object.put(Constants.DOCUMENT_ID, de.getDocumentId());
        object.put(Constants.PARENT_DIRECTORY_ID, de.getParentDirectoryId());

        if (de instanceof CreateDocumentEvent) {
            createDocumentEvent(de, object);
        } else if (de instanceof DeleteDocumentEvent) {
            deleteDocumentEvent(de, object);
        } else if (de instanceof MoveDocumentEvent) {
            moveDocumentEvent(de, object);
        } else if (de instanceof RenameDocumentEvent) {
            renameDocumentEvent(de, object);
        }
    }

    private static void createDocumentEvent(DocumentEvent de, JSONObject object) throws JSONException {
        CreateDocumentEvent cde = (CreateDocumentEvent) de;
        object.put(Constants.NEW_NAME, cde.getDocumentNewName());
    }

    private static void deleteDocumentEvent(DocumentEvent de, JSONObject object) throws JSONException {
        DeleteDocumentEvent dde = (DeleteDocumentEvent) de;
        object.put(Constants.OLD_NAME, dde.getDocumentOldName());
    }

    private static void moveDocumentEvent(DocumentEvent de, JSONObject object) throws JSONException {
        MoveDocumentEvent mde = (MoveDocumentEvent) de;
        object.put(Constants.DESTINATION_DIRECTORY_ID, mde.getNewParentDirectoryId());
    }

    private static void renameDocumentEvent(DocumentEvent de, JSONObject object) throws JSONException {
        RenameDocumentEvent rde = (RenameDocumentEvent) de;
        object.put(Constants.OLD_NAME, rde.getDocumentOldName());
        object.put(Constants.NEW_NAME, rde.getDocumentNewName());
    }


    // text events
    private static void textEvent(StorytellerEvent event, JSONObject object) throws JSONException {
        TextEvent te = (TextEvent) event;
        object.put(Constants.DOCUMENT_ID, te.getDocumentId());
        object.put(Constants.PREVIOUS_NEIGHBOR_ID, te.getPreviousNeighborEventId());

        if (te instanceof InsertEvent) {
            insertEvent(te, object);
        }
    }

    private static void insertEvent(TextEvent te, JSONObject object) throws JSONException {
        InsertEvent ie = (InsertEvent) te;
        object.put(Constants.EVENT_DATA, ie.getEventData());
        object.put(Constants.PASTE_PARENT_ID, ie.getPasteParentId());
        object.put(Constants.DELETED_AT_TIMESTAMP, ie.getDeletedAtTimestamp());
        object.put(Constants.DELETED_BY_DEV_GROUP_ID, ie.getDeletedByDevGroupId());
        object.put(Constants.DELETE_EVENT_ID, ie.getDeleteEventId());

        long deleteTimestamp = 0;
        if (ie.getDeletedAtTimestamp() != null) {
            deleteTimestamp = ie.getDeletedAtTimestamp().getTime();
        }

        object.put(Constants.DELETED_AT_TIMESTAMP, deleteTimestamp);
    }
}
