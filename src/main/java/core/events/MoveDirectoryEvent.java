package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public class MoveDirectoryEvent extends DirectoryEvent {
    public static final String MOVE_DIRECTORY_EVENT_TYPE = "MOVE-DIRECTORY";

    //the id of the directory where the directory is moved to
    private String newParentDirectoryId;

    /**
     * Constructor used when creating a new MoveDirectoryEvent. Will generate a unique ID.
     *
     * @param timestamp
     * @param createdUnderNodeId
     * @param devGroupId
     * @param nodeSequenceNum
     * @param sequentiallyBeforeEventId
     * @param directoryId
     * @param parentDirectoryId
     * @param newParentDirectoryId
     */
    public MoveDirectoryEvent(Date timestamp, String createdUnderNodeId,
                              String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId,
                              String directoryId, String parentDirectoryId, String newParentDirectoryId) {
        super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum,
                sequentiallyBeforeEventId, directoryId, parentDirectoryId);
        setNewParentDirectoryId(newParentDirectoryId);
    }

    public MoveDirectoryEvent(String id, Date timestamp, String createdUnderNodeId,
                              String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId,
                              String directoryId, String parentDirectoryId, String newParentDirectoryId) {
        super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum,
                sequentiallyBeforeEventId, directoryId, parentDirectoryId);
        setNewParentDirectoryId(newParentDirectoryId);
    }

    @Override
    public String getEventType() {
        return MOVE_DIRECTORY_EVENT_TYPE;
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
