package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;



public abstract class TextEvent extends StorytellerEvent
{
	//id of the document that this text event is in
	private String documentId;
	//backlink to the event behind it in the document
	private String previousNeighborEventId;

	public TextEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, 
			int nodeSequenceNum, String sequentiallyBeforeEventId, String docId, 
			String previousNeighborEventId)
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId);
		setDocumentId(docId);
		setPreviousNeighborEventId(previousNeighborEventId);
	}

	public TextEvent(Date timestamp, String createdUnderNodeId, String devGroupId, 
			int nodeSequenceNum, String sequentiallyBeforeEventId, String docId, 
			String previousNeighborEventId)
	{
		super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId);
		setDocumentId(docId);
		setPreviousNeighborEventId(previousNeighborEventId);
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
		if (previousNeighborEventId != null)
		{
			builder.append(", ");
			builder.append("previousNeighborEventId=");
			builder.append(previousNeighborEventId);
		}
		return builder.toString();
	}
	

	//Getters and Setters
	public String getDocumentId() 
	{
		return documentId;
	}
	public void setDocumentId(String documentId) 
	{
		this.documentId = documentId;
	}

	public String getPreviousNeighborEventId() 
	{
		return previousNeighborEventId;
	}
	public void setPreviousNeighborEventId(String previousNeighborEventId) 
	{
		if (previousNeighborEventId==null||previousNeighborEventId.equals("null"))
		{
			this.previousNeighborEventId = null;
		}
		else
		{
			this.previousNeighborEventId = previousNeighborEventId;
		}
	}
}
