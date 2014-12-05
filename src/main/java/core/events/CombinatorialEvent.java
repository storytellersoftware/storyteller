package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;



public abstract class CombinatorialEvent extends StorytellerEvent
{
	private String documentId;
	
	public CombinatorialEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, String documentId)
	{
		super(timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId);
		this.documentId = documentId;
		
	}
	
	public CombinatorialEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, String documentId)
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId);
		this.documentId = documentId;
		
	}
	
	public String getDocumentId() {
		return documentId;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (documentId != null)
		{
			builder.append(", ");
			builder.append("documentId=");
			builder.append(documentId);
		}
		return builder.toString();
	}

//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException {
//		JSONObject tempJSONObject =  super.toJSON();
//		tempJSONObject.put(Constants.DOCUMENT_ID, getDocumentId());
//		return tempJSONObject;
//	}

	


}
