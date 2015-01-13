package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public class MoveDocumentEvent extends DocumentEvent {
    public static final String MOVE_DOCUMENT_EVENT_TYPE = "MOVE-DOCUMENT";

    //the id of the directory where the document is moved to
    private String newParentDirectoryId;

    /**
     * Constructor used when creating a new MoveDocumentEvent. Will generate a unique ID.
     *
     * @param timestamp
     * @param createdUnderNodeId
     * @param devGroupId
     * @param nodeSequenceNum
     * @param sequentiallyBeforeEventId
     * @param docId
     * @param oldParentDirectoryId
     * @param newParentDirectoryId
     */
    public MoveDocumentEvent(Date timestamp, String createdUnderNodeId,
                             String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId,
                             String docId, String oldParentDirectoryId, String newParentDirectoryId) {
        super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum,
                sequentiallyBeforeEventId, docId, oldParentDirectoryId);
        setNewParentDirectoryId(newParentDirectoryId);
    }

    public MoveDocumentEvent(String id, Date timestamp, String createdUnderNodeId,
                             String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId,
                             String docId, String oldParentDirectoryId, String newParentDirectoryId) {
        super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum,
                sequentiallyBeforeEventId, docId, oldParentDirectoryId);
        setNewParentDirectoryId(newParentDirectoryId);
    }

    @Override
    public String getEventType() {
        return MOVE_DOCUMENT_EVENT_TYPE;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        if (newParentDirectoryId != null) {
            builder.append(", ");
            builder.append("newParentDirectoryId=");
            builder.append(newParentDirectoryId);
        }
        builder.append("]");
        return builder.toString();
    }


    //Getters and Setters
    public String getNewParentDirectoryId() {
        return newParentDirectoryId;
    }

    public void setNewParentDirectoryId(String directoryNewID) {
        this.newParentDirectoryId = directoryNewID;
    }

//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException 
//	{
//		JSONObject tempJsonObject = super.toJSON();
//		tempJsonObject.put(Constants.DESTINATION_DIRECTORY_ID, getNewParentDirectoryId());
//
//		return tempJsonObject;
//	}
}
