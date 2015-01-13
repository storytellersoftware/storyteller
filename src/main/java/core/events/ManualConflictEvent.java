package core.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public class ManualConflictEvent extends CombinatorialEvent {

    public static final String MANUAL_CONFLICT_EVENT_TYPE = "MANUAL-CONFLICT";
    private List<String> idsOfEventsInFirstBlock;
    private List<String> idsOfEventsInSecondBlock;

    /**
     * Used to restore a manualConflict from the database.  Note that the two lists passed in are the actual events. This
     * is for convenience because when a manual conflict will be created, the program will have a list of events, not ids.
     *
     * @param timestamp
     * @param createdUnderNodeId
     * @param devGroupId
     * @param NodeSequenceNum
     * @param sequentiallyBeforeEventId
     * @param eventsInFirstBlock
     * @param eventsInSecondBlock
     */
    public ManualConflictEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, List<? extends Object> eventsInFirstBlock, List<? extends Object> eventsInSecondBlock, String documentId) {
        super(timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId, documentId);
        idsOfEventsInFirstBlock = new ArrayList<String>(eventsInFirstBlock.size());
        for (Object event : eventsInFirstBlock) {
            if (event instanceof StorytellerEvent) {
                idsOfEventsInFirstBlock.add(((StorytellerEvent) event).getId());
            } else {
                idsOfEventsInFirstBlock.add(event.toString());
            }
        }

        idsOfEventsInSecondBlock = new ArrayList<String>(eventsInSecondBlock.size());
        for (Object event : eventsInSecondBlock) {
            if (event instanceof StorytellerEvent) {
                idsOfEventsInSecondBlock.add(((StorytellerEvent) event).getId());
            } else {
                idsOfEventsInSecondBlock.add(event.toString());
            }
        }
    }

    public ManualConflictEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, List<? extends Object> eventsInFirstBlock, List<? extends Object> eventsInSecondBlock, String documentId) {
        super(id, timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId, documentId);
        idsOfEventsInFirstBlock = new ArrayList<String>(eventsInFirstBlock.size());
        for (Object event : eventsInFirstBlock) {
            if (event instanceof StorytellerEvent) {
                idsOfEventsInFirstBlock.add(((StorytellerEvent) event).getId());
            } else {
                idsOfEventsInFirstBlock.add(event.toString());
            }
        }

        idsOfEventsInSecondBlock = new ArrayList<String>(eventsInSecondBlock.size());
        for (Object event : eventsInSecondBlock) {
            if (event instanceof StorytellerEvent) {
                idsOfEventsInSecondBlock.add(((StorytellerEvent) event).getId());
            } else {
                idsOfEventsInSecondBlock.add(event.toString());
            }
        }
    }

//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException {
//		JSONObject tempJSONObject = super.toJSON();
//		tempJSONObject.put(Constants.IDS_OF_EVENTS_IN_FIRST_BLOCK, getIdsOfEventsInFirstBlock());
//		tempJSONObject.put(Constants.IDS_OF_EVENTS_IN_SECOND_BLOCK, getIdsOfEventsInSecondBlock());
//		return tempJSONObject;
//	}

    @Override
    public String getEventType() {
        return MANUAL_CONFLICT_EVENT_TYPE;
    }

    public List<String> getIdsOfEventsInFirstBlock() {
        return idsOfEventsInFirstBlock;
    }

    public List<String> getIdsOfEventsInSecondBlock() {
        return idsOfEventsInSecondBlock;
    }

}
