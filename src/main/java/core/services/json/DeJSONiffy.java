package core.services.json;

import core.Constants;
import core.entities.*;
import ide.IDEServerException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import playback.PlaybackFilter;

import java.util.Date;


public class DeJSONiffy {
    /**
     * Create a clip from a JSON object
     *
     * @param json A JSON representation of a Clip
     * @return A Clip version of the above JSON representation.
     * @throws JSONException When the passed in JSONObject doesn't have the
     *                       keys needed to make a Clip.
     */
    public static Clip clip(JSONObject json) throws JSONException {
        PlaybackFilter filter = playbackFilter(json.getJSONObject(Constants.FILTERS));
        return clip(json, filter);
    }

    /**
     * Create a clip from json and a PlaybackFilter
     *
     * @param json   A JSON representation of a Clip
     * @param filter The PlaybackFilter for the above Clip
     * @throws JSONException
     * @return A Clip version of the json and filter.
     */
    public static Clip clip(JSONObject json, PlaybackFilter filter) throws JSONException {
        json.put(Constants.NODE_ID, filter.getNodeID());

        String developerGroupID = json.getString(Constants.DEVELOPER_GROUP_ID);
        String name = json.getString(Constants.NAME);
        String description = json.getString(Constants.DESCRIPTION);
        String nodeID = json.getString(Constants.NODE_ID);

        Clip c;
        if (json.has(Constants.ID)) {
            String id = json.getString(Constants.ID);
            c = new Clip(id, nodeID, developerGroupID, name, description, filter);
        } else {
            c = new Clip(nodeID, developerGroupID, name, description, filter);
        }

        return c;
    }


    /**
     * Create a PlaybackFilter from a JSON object
     *
     * @param json A JSON representation of a PlaybackFilter
     * @return A PlaybackFilter created from the above JSON object
     * @throws JSONException When the passed in JSONObject doesn't have the
     *                       keys needed to make a PlaybackFilter.
     */
    public static PlaybackFilter playbackFilter(JSONObject json) throws JSONException {
        String nodeID = json.getString(Constants.NODE_ID);
        int nodeSeqNum = json.getInt(Constants.NODE_SEQUENCE_NUMBER);
        long startTime = json.getLong(Constants.START_TIME);
        long endTime = json.getLong(Constants.END_TIME);

        int hideDeleteLimit = json.getInt(Constants.HIDE_DELETE_LIMIT);
        boolean showOnlyEndResult = json.getBoolean(Constants.SHOW_ONLY_END_RESULT);
        boolean showPasteOrigin = json.getBoolean(Constants.SHOW_PASTE_ORIGIN);

        String relevantBlockType = json.getString(Constants.RELEVANT_BLOCK_TYPE);
        PlaybackFilter filter = new PlaybackFilter(nodeID, nodeSeqNum, startTime, endTime,
                hideDeleteLimit, showPasteOrigin, showOnlyEndResult,
                relevantBlockType);

        JSONArray docIDsInJSON = json.getJSONArray(Constants.DOCUMENT_IDS);
        for (int i = 0; i < docIDsInJSON.length(); i++) {
            filter.getAcceptableDocumentIDs().add(docIDsInJSON.getString(i));
        }

        JSONArray devIDsInJSON = json.getJSONArray(Constants.DEVELOPER_GROUP_IDS);
        for (int i = 0; i < devIDsInJSON.length(); i++) {
            filter.getAcceptableDeveloperGroupIDs().add(devIDsInJSON.getString(i));
        }

        //if this is a selected text playback there will be some relevant events
        if (json.has(Constants.RELEVANT_EVENTS)) {
            JSONArray relevantIDsInJSON = json.getJSONArray(Constants.RELEVANT_EVENTS);
            for (int i = 0; i < relevantIDsInJSON.length(); i++) {
                filter.getSelectedAndRelevantEventIds().add(relevantIDsInJSON.getString(i));
            }
        }
        //else- this was a filtered playback and there will not be a relevant events field

        return filter;
    }

    public static PlaybackFilter playbackFilter(String json) throws JSONException {
        return playbackFilter(new JSONObject(json));
    }


    public static ClipComment clipComment(String json) throws JSONException, IDEServerException {
        return clipComment(new JSONObject(json));
    }

    public static ClipComment clipComment(JSONObject json) throws JSONException, IDEServerException {
        //(TODO make sure this is the) clip id where this clip comment belongs
        String clipId = json.getString(Constants.CLIP_ID);
        //TODO !!!
        //TODO make sure the JSON object has these data members!!!!!! I'm not sure it does now!!!
        String createdUnderNodeId = json.getString(Constants.CREATED_UNDER_NODE_ID);
        Date timestamp = new Date(Long.valueOf(json.getString(Constants.TIMESTAMP)));
        //TODO !!!
        String developerGroupID = json.getString(Constants.DEVELOPER_GROUP_ID);
        String commentText = json.getString(Constants.COMMENT_TEXT);
        String eventID = json.getString(Constants.EVENT_ID);
        String startHighlightedEventID = json.getString(Constants.START_HIGHLIGHTED_EVENT_ID);
        String endHighlightedEventID = json.getString(Constants.END_HIGHLIGHTED_EVENT_ID);

        ClipComment comment = new ClipComment(timestamp, createdUnderNodeId, developerGroupID, commentText, eventID, clipId, startHighlightedEventID, endHighlightedEventID);
        return comment;
    }

    public static ClipComment clipComment(Clip parent, JSONObject json) throws JSONException {
        String developerGroupID = json.getString(Constants.DEVELOPER_GROUP_ID);
        String commentText = json.getString(Constants.COMMENT_TEXT);
        String eventID = json.getString(Constants.EVENT_ID);
        String startHighlightedEventID = json.getString(Constants.START_HIGHLIGHTED_EVENT_ID);
        String endHighlightedEventID = json.getString(Constants.END_HIGHLIGHTED_EVENT_ID);

        ClipComment comment = new ClipComment(parent, developerGroupID, commentText,
                eventID, startHighlightedEventID, endHighlightedEventID);
        return comment;
    }


    public static Developer developer(JSONObject object) throws JSONException {
        Date timestamp = new Date();
        timestamp.setTime(object.getLong(Constants.TIMESTAMP));

        String createdUnderNodeId = object.getString(Constants.CREATED_UNDER_NODE_ID);
        //Should this be DEVELOPER_GROUP_ID?
        String createdByDevGroupId = object.getString(Constants.DEVELOPER_GROUP_ID);

        String email = object.getString(Constants.EMAIL);

        String firstName = object.getString(Constants.FIRST_NAME);

        String lastName = object.getString(Constants.LAST_NAME);

        Developer developer;
        if (object.has(Constants.ID)) {
            String id = object.getString(Constants.ID);
            developer = new Developer(id, timestamp, createdUnderNodeId, createdByDevGroupId, email, firstName, lastName);
        } else {
            developer = new Developer(timestamp, createdUnderNodeId, createdByDevGroupId, email, firstName, lastName);
        }

        return developer;
    }


    public static DeveloperGroup developerGroup(JSONObject object) throws JSONException {
        Date timestamp = new Date();
        timestamp.setTime(object.getLong(Constants.TIMESTAMP));

        String createdUnderNodeId = object.getString(Constants.CREATED_UNDER_NODE_ID);

        String createdDevGroupId = object.getString(Constants.DEVELOPER_GROUP_ID);

        DeveloperGroup devGroup = null;

        if (object.has(Constants.ID)) {
            String ID = object.getString(Constants.ID);
            devGroup = new DeveloperGroup(ID, timestamp, createdUnderNodeId, createdDevGroupId);
        }
        //TODO do we ever want a random dev group id?? it should be an MD5 hash of the dev emails in the group
//		else
//		{
//			devGroup = new DeveloperGroup(timestamp, createdUnderNodeId, createdDevGroupId);
//		}

        return devGroup;
    }

    public static Directory directory(JSONObject object) throws JSONException {
        Date timestamp = new Date();
        timestamp.setTime(object.getLong(Constants.TIMESTAMP));

        String createdUnderNodeId = object.getString(Constants.CREATED_UNDER_NODE_ID);

        String devGroupId = object.getString(Constants.DEVELOPER_GROUP_ID);

        String parentDirectoryId = object.getString(Constants.PARENT_DIRECTORY_ID);

        Directory directory;
        if (object.has(Constants.ID)) {
            String id = object.getString(Constants.ID);
            directory = new Directory(id, timestamp, createdUnderNodeId, devGroupId, parentDirectoryId);
        } else {
            directory = new Directory(timestamp, createdUnderNodeId, devGroupId, parentDirectoryId);
        }
        return directory;
    }

    public static Document document(JSONObject object) throws JSONException {
        Date timestamp = new Date();
        timestamp.setTime(object.getLong(Constants.TIMESTAMP));

        String createdUnderNodeId = object.getString(Constants.CREATED_UNDER_NODE_ID);

        String devGroupId = object.getString(Constants.DEVELOPER_GROUP_ID);

        String parentDirectoryId = object.getString(Constants.PARENT_DIRECTORY_ID);

        Document document;
        if (object.has(Constants.ID)) {
            String id = object.getString(Constants.ID);
            document = new Document(id, timestamp, createdUnderNodeId, devGroupId, parentDirectoryId);
        } else {
            document = new Document(timestamp, createdUnderNodeId, devGroupId, parentDirectoryId);
        }
        return document;
    }

    public static Node node(JSONObject object) throws JSONException {
        Date timestamp = new Date();
        timestamp.setTime(object.getLong(Constants.TIMESTAMP));

        String createdUnderNodeId = object.getString(Constants.CREATED_UNDER_NODE_ID);

        String devGroupId = object.getString(Constants.DEVELOPER_GROUP_ID);

        String name = object.getString(Constants.NAME);

        String description = object.getString(Constants.DESCRIPTION);

        String projectId = object.getString(Constants.ID);

        int nodeLineageNumber = object.getInt(Constants.LINEAGE_NUMBER);

        String nodeType = object.getString(Constants.TYPE);

        Node node;

        if (object.has(Constants.ID)) {
            String ID = object.getString(Constants.ID);
            node = new Node(ID, timestamp, createdUnderNodeId, devGroupId, name, description, projectId, nodeLineageNumber, nodeType);
        } else {
            node = new Node(timestamp, createdUnderNodeId, devGroupId, name, description, projectId, nodeLineageNumber, nodeType);
        }

        return node;
    }

    public static Project project(JSONObject object) throws JSONException {
        Date timestamp = new Date();
        timestamp.setTime(object.getLong(Constants.TIMESTAMP));

        String devGroupId = object.getString(Constants.DEVELOPER_GROUP_ID);

        String projectName = object.getString(Constants.NAME);

        Project project;

        if (object.has(Constants.ID)) {
            String ID = object.getString(Constants.ID);
            project = new Project(ID, timestamp, devGroupId, projectName);
        } else {
            project = new Project(timestamp, devGroupId, projectName);
        }

        return project;
    }

    public static Storyboard storyboard(JSONObject object) throws JSONException {
        Date timestamp = new Date();
        timestamp.setTime(object.getLong(Constants.TIMESTAMP));

        String createdUnderNodeId = object.getString(Constants.CREATED_UNDER_NODE_ID);

        String devGroupId = object.getString(Constants.DEVELOPER_GROUP_ID);

        String name = object.getString(Constants.NAME);

        String description = object.getString(Constants.DESCRIPTION);

        Storyboard storyboard;

        if (object.has(Constants.ID)) {
            String ID = object.getString(Constants.ID);
            storyboard = new Storyboard(ID, timestamp, createdUnderNodeId, devGroupId, name, description);
        } else {
            storyboard = new Storyboard(timestamp, createdUnderNodeId, devGroupId, name, description);
        }

        return storyboard;
    }


    //*******EVENTS

    //Under Construction!!
    /*
	public static AutomaticConflictEvent automaticConflictEvent(JSONObject object)
	{
		Date timestamp = new Date(object.getLong(JSONConstants.TIMESTAMP));

		String createdUnderNodeId = object.getString(JSONConstants.CREATED_UNDER_NODE_ID);

		String devGroupId = object.getString(JSONConstants.DEVELOPER_GROUP_ID);

		int NodeSequenceNum = object.getInt(JSONConstants.NODE_SEQUENCE_NUMBER);

		String sequentiallyBeforeEventId = object.getString(JSONConstants.PREVIOUS_EVENT_ID);

		//Go through array getting the objects and put them in a list
		List <StorytellerEvent> eventsInBlock = object.getJSONArray(JSONConstants.EVENT_IDS).
		AutomaticConflictEvent event = new AutomaticConflictEvent(timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId, eventsInBlock, documentId);
	}*/
}
