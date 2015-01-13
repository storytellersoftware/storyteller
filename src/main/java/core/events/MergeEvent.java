package core.events;

import java.util.Date;


public class MergeEvent extends OpenNodeEvent {
    public static final String MERGE_EVENT_TYPE = "MERGE";
    private String firstNodeToMergeId;
    private String secondNodeToMergeId;

    public MergeEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, String firstNodeToMergeId, String secondNodeToMergeId, String sequentiallyBeforeNodeId) {
        super(timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId, sequentiallyBeforeNodeId);
        this.firstNodeToMergeId = firstNodeToMergeId;
        this.secondNodeToMergeId = secondNodeToMergeId;
    }

    public MergeEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, String firstNodeToMergeId, String secondNodeToMergeId, String sequentiallyBeforeNodeId) {
        super(id, timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId, sequentiallyBeforeNodeId);
        this.firstNodeToMergeId = firstNodeToMergeId;
        this.secondNodeToMergeId = secondNodeToMergeId;
    }

    @Override
    public String getEventType() {
        return MERGE_EVENT_TYPE;
    }

    //Getters
    public String getFirstNodeToMergeID() {
        return firstNodeToMergeId;
    }

    public String getSecondNodeToMergeID() {
        return secondNodeToMergeId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        if (firstNodeToMergeId != null) {
            builder.append(", ");
            builder.append("firstNodeToMergeId=");
            builder.append(firstNodeToMergeId);

        }
        if (secondNodeToMergeId != null) {
            builder.append(", ");
            builder.append("secondNodeToMergeId=");
            builder.append(secondNodeToMergeId);
        }
        builder.append("]");
        return builder.toString();
    }

}
