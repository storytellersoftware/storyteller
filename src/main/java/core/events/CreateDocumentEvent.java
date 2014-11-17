package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public class CreateDocumentEvent extends DocumentEvent 
{
	public static final String CREATE_DOCUMENT_EVENT_TYPE = "CREATE-DOCUMENT"; 
	
	//name of the newly created document
	private String documentNewName;
	
	/**
	 * Constructor used when creating a new CreateDocumentEvent. Will generate a unique Id.
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param nodeSequenceNum
	 * @param sequentiallyBeforeEventId
	 * @param docId
	 * @param documentNewName
	 * @param parentDirectoryId
	 */
	public CreateDocumentEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String docId, String documentNewName, String parentDirectoryId) 
	{
		super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId, docId, parentDirectoryId);
		setDocumentNewName(documentNewName);
	}

	public CreateDocumentEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String docId, String documentNewName, String parentDirectoryId) 
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId, docId, parentDirectoryId);
		setDocumentNewName(documentNewName);
	}
	
	@Override
	public String getEventType() 
	{
		return CREATE_DOCUMENT_EVENT_TYPE;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (documentNewName != null)
		{
			builder.append(", ");
			builder.append("documentNewName=");
			builder.append(documentNewName);
		}
		builder.append("]");
		return builder.toString();
	}
	
	//Getters and Setters
	public String getDocumentNewName() 
	{
		return documentNewName;
	}
	public void setDocumentNewName(String documentNewName) 
	{
		this.documentNewName = documentNewName;
	}
	
//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException 
//	{
//		JSONObject tempJsonObject = super.toJSON();
//		tempJsonObject.put(Constants.NEW_NAME, getDocumentNewName());
//
//		return tempJsonObject;
//	}
}
