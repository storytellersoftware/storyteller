package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public abstract class DirectoryEvent extends StorytellerEvent {
    //this is the id of the directory that is being created, moved, renamed, or deleted
    private String directoryId;

    //this is the id of the parent directory that holds this directory
    private String parentDirectoryId;

    /**
     * Constructor For Creating a brand new Directory Event
     *
     * @param timestamp
     * @param createdUnderNodeId
     * @param devGroupId
     * @param nodeSequenceNum
     * @param sequentiallyBeforeEventId
     * @param directoryId
     */
    public DirectoryEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String directoryId, String parentDirectoryId) {
        super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId);
        setDirectoryId(directoryId);
        setParentDirectoryId(parentDirectoryId);
    }

    public DirectoryEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String directoryId, String parentDirectoryId) {
        super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId);
        setDirectoryId(directoryId);
        setParentDirectoryId(parentDirectoryId);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        if (directoryId != null) {
            builder.append(", ");
            builder.append("directoryId=");
            builder.append(directoryId);

        }
        if (parentDirectoryId != null) {
            builder.append(", ");
            builder.append("parentDirectoryId=");
            builder.append(parentDirectoryId);
        }
        return builder.toString();
    }

//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException
//	{
//		JSONObject tempJsonObject = super.toJSON();
//		tempJsonObject.put(Constants.PARENT_DIRECTORY_ID, getParentDirectoryId());
//		tempJsonObject.put(Constants.DIRECTORY_ID, getDirectoryId());
//		return tempJsonObject;
//	}

    //Getters and Setters
    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public String getParentDirectoryId() {
        return parentDirectoryId;
    }

    public void setParentDirectoryId(String parentDirectoryId) {
        this.parentDirectoryId = parentDirectoryId;
    }

}
