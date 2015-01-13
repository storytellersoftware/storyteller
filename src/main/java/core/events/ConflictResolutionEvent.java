package core.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public class ConflictResolutionEvent extends CombinatorialEvent {
    public static final String CONFLICT_RESOLUTION_EVENT_TYPE = "CONFLICT-RESOLUTION";

    //tells playback which resolution option was chosen
    private String baseResolutionBlockId; //can be the first event of either node being merged, or null if the resolution does not rely on them.

    private List<String> idsOfEventsInThisBlock;    //This (if one of the nodes was selected) will be a copy of that respective list from the ManualConflictEvent
    //plus the ids of any additional events needed to resolve this conflict

    /**
     * Used to restore a ConflictResolutionEvent from the database.  Note that the list passed in is the actual events. This
     * is for convenience because when a manual conflict will be created, the program will have a list of events, not ids.
     * A brand new List will be created and contain the ids of the events passed in.
     *
     * @param timestamp
     * @param createdUnderNodeId
     * @param devGroupId
     * @param NodeSequenceNum
     * @param sequentiallyBeforeEventId
     * @param baseResolutionBlockId
     * @param eventsInBlock
     */
    public ConflictResolutionEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, String baseResolutionBlockId, List<? extends Object> eventsInBlock, String documentId) {
        super(timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId, documentId);
        setBaseResolutionBlockId(baseResolutionBlockId);

        idsOfEventsInThisBlock = new ArrayList<String>(eventsInBlock.size());
        for (Object event : eventsInBlock) {
            if (event instanceof StorytellerEvent) {
                idsOfEventsInThisBlock.add(((StorytellerEvent) event).getId());
            } else {
                idsOfEventsInThisBlock.add(event.toString());
            }
        }
    }

    public ConflictResolutionEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, String baseResolutionBlockId, List<? extends Object> eventsInBlock, String documentId) {
        super(id, timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId, documentId);
        setBaseResolutionBlockId(baseResolutionBlockId);

        idsOfEventsInThisBlock = new ArrayList<String>(eventsInBlock.size());
        for (Object event : eventsInBlock) {
            if (event instanceof StorytellerEvent) {
                idsOfEventsInThisBlock.add(((StorytellerEvent) event).getId());
            } else {
                idsOfEventsInThisBlock.add(event.toString());
            }
        }
    }

    @Override
    public String getEventType() {
        return CONFLICT_RESOLUTION_EVENT_TYPE;
    }

    public String getBaseResolutionBlockId() {
        return baseResolutionBlockId;
    }

    public void setBaseResolutionBlockId(String resolutionBlockId) {
        if (resolutionBlockId == null || resolutionBlockId.equals("") || resolutionBlockId.equals("null")) {
            baseResolutionBlockId = null;
        } else {
            baseResolutionBlockId = resolutionBlockId;
        }

    }


    //	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException 
//	{
//		JSONObject tempJSON =  super.toJSON();
//		tempJSON.put(Constants.BASE_RESOLUTION_BLOCK_ID, getBaseResolutionBlockId());
//		tempJSON.put(Constants.IDS_OF_EVENTS_IN_FIRST_BLOCK, getIdsOfEventsInThisBlock());
//		return tempJSON;
//	}
//	
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        if (baseResolutionBlockId != null) {
            builder.append(", ");
            builder.append("baseResolutionBlockId=");
            builder.append(baseResolutionBlockId);
        }
        if (idsOfEventsInThisBlock != null) {
            builder.append(", ");
            builder.append("idsOfEventsInThisBlock=");
            builder.append(idsOfEventsInThisBlock);
        }
        builder.append("]");
        return builder.toString();
    }

    public List<String> getIdsOfEventsInThisBlock() {
        return idsOfEventsInThisBlock;
    }

}
