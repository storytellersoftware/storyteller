package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public class RenameDocumentEvent extends DocumentEvent
{
	public static final String RENAME_DOCUMENT_EVENT_TYPE = "RENAME-DOCUMENT";

	// the document's old name
	private String documentOldName;

	// the document's new name
	private String documentNewName;

	/**
	 * Constructor used when creating a new RenameDocumentEvent. Will generate a
	 * unique ID.
	 * 
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param nodeSequenceNum
	 * @param sequentiallyBeforeEventId
	 * @param docId
	 * @param documentNewName
	 */
	public RenameDocumentEvent(Date timestamp, String createdUnderNodeId,
			String devGroupId, int nodeSequenceNum,
			String sequentiallyBeforeEventId, String docId,
			String documentNewName, String documentOldName,
			String parentDirectoryId)
	{
		super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum,
				sequentiallyBeforeEventId, docId, parentDirectoryId);
		setDocumentOldName(documentOldName);
		setDocumentNewName(documentNewName);
	}

	public RenameDocumentEvent(String id, Date timestamp, String createdUnderNodeId,
			String devGroupId, int nodeSequenceNum,
			String sequentiallyBeforeEventId, String docId,
			String documentNewName, String documentOldName,
			String parentDirectoryId)
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum,
				sequentiallyBeforeEventId, docId, parentDirectoryId);
		setDocumentOldName(documentOldName);
		setDocumentNewName(documentNewName);
	}
	
	@Override
	public String getEventType()
	{
		return RENAME_DOCUMENT_EVENT_TYPE;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (documentOldName != null)
		{
			builder.append(", ");
			builder.append("documentOldName=");
			builder.append(documentOldName);
		}
		if (documentNewName != null)
		{
			builder.append(", ");
			builder.append("documentNewName=");
			builder.append(documentNewName);
		}
		builder.append("]");
		return builder.toString();
	}

	// Getters and Setters
	public String getDocumentNewName()
	{
		return documentNewName;
	}

	public void setDocumentNewName(String documentNewName)
	{
		this.documentNewName = documentNewName;
	}

	public String getDocumentOldName()
	{
		return documentOldName;
	}

//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException
//	{
//		JSONObject tempJsonObject = super.toJSON();
//		tempJsonObject.put(Constants.OLD_NAME, getDocumentOldName());
//		tempJsonObject.put(Constants.NEW_NAME, getDocumentNewName());
//
//		return tempJsonObject;
//	}

	public void setDocumentOldName(String documentOldName)
	{
		this.documentOldName = documentOldName;
	}

}
