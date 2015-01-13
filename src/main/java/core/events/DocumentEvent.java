package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public abstract class DocumentEvent extends StorytellerEvent {
    //this is the id of the document that is being created, moved, renamed, or deleted
    private String documentId;

    //this is the id of the parent directory where the document event occurs
    private String parentDirectoryId;

    /**
     * Constructor for making a brand new DocumentEvent
     *
     * @param timestamp
     * @param createdUnderNodeId
     * @param devGroupId
     * @param nodeSequenceNum
     * @param sequentiallyBeforeEventId
     * @param documentId
     */
    public DocumentEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String documentId, String parentDirectoryId) {
        super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId);
        this.documentId = documentId;
        this.parentDirectoryId = parentDirectoryId;
    }

    public DocumentEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String documentId, String parentDirectoryId) {
        super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId);
        this.documentId = documentId;
        this.parentDirectoryId = parentDirectoryId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        if (documentId != null) {
            builder.append(", ");
            builder.append("documentId=");
            builder.append(documentId);

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
//		tempJsonObject.put(Constants.DOCUMENT_ID, getDocumentId());
//		tempJsonObject.put(Constants.PARENT_DIRECTORY_ID, getParentDirectoryId());
//		
//		return tempJsonObject;
//	}

    //Getters
    public String getDocumentId() {
        return documentId;
    }

    public String getParentDirectoryId() {
        return parentDirectoryId;
    }

    public void setParentDirectoryId(String parentDirectoryId) {
        this.parentDirectoryId = parentDirectoryId;
    }

}
