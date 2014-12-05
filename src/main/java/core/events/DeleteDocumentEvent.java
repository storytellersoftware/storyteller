package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public class DeleteDocumentEvent extends DocumentEvent 
{
	public static final String DELETE_DOCUMENT_EVENT_TYPE = "DELETE-DOCUMENT"; 

	//this is the old name of a document
	private String documentOldName;

	/**
	 * Constructor used when creating a new DeleteDocumentEvent. Will generate a unique Id.
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param nodeSequenceNum
	 * @param sequentiallyBeforeEventId
	 * @param docID
	 */
	public DeleteDocumentEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String docId,  String parentDirectoryId, String docOldName) 
	{
		super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId, docId, parentDirectoryId);
		
		//store the doc's name
		setDocumentOldName(docOldName);
	}

	public DeleteDocumentEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String docId,  String parentDirectoryId, String docOldName) 
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId, docId, parentDirectoryId);
		
		//store the doc's name
		setDocumentOldName(docOldName);
	}
	
	@Override
	public String getEventType() 
	{
		return DELETE_DOCUMENT_EVENT_TYPE;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (documentOldName != null)
		{
			builder.append(", ");
			builder.append("documentBeingDeleted=");
			builder.append(documentOldName);
		}
		builder.append("]");
		return builder.toString();
	}


//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException 
//	{
//		JSONObject tempJsonObject = super.toJSON();
//		tempJsonObject.put(Constants.OLD_NAME, getDocumentOldName());
//		
//		return tempJsonObject;
//	}
	public String getDocumentOldName()
	{
		return documentOldName;

	}

	public void setDocumentOldName(String documentOldName)
	{
		this.documentOldName = documentOldName;
	}

}
