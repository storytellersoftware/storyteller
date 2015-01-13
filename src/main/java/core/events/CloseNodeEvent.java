package core.events;

import java.util.Date;


public class CloseNodeEvent extends StorytellerEvent {
    public static final String CLOSE_NODE_EVENT_TYPE = "CLOSE-NODE";

    /**
     * Used for making a brand new CloseNodeEvent
     *
     * @param timestamp
     * @param createdUnderNodeId
     * @param devGroupId
     * @param nodeSequenceNum
     * @param sequentiallyBeforeEventId
     */
    public CloseNodeEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId) {
        super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId);
    }

    public CloseNodeEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId) {
        super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId);
    }

    @Override
    public String getEventType() {
        return CLOSE_NODE_EVENT_TYPE;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
