package core.events;


import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;

public class InsertEvent extends TextEvent
{
	public static final String INSERT_EVENT_TYPE = "INSERT";
	
	//text from the insert event
	private String eventData;

	//if the insert was pasted from another insert this is the origin
	private String pasteParentId;

	//this is an alphanumeric string indicating the order of the insert event on the screen
	//every insert event's sortOrder will come after the sortOrder of the insert
	//event before it and before the sortOrder of the insert event after it
	private String sortOrder;
	
	//information about this insert event's deletion (not all inserts will be
	//deleted but if they are we can use this info in a playback). This data
	//is not stored in the db
	//TODO are we using it????
	private Date deletedAtTimestamp;
	private String deletedByDevGroupId;
	private String deleteEventId;
	
	/**
	 * Used for creating a brand new insert event.
	 */
	public InsertEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String docId, String previousNeighborEventId, String eventData, String pasteParentId, String sortOrder)
	{
		super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, 
				sequentiallyBeforeEventId, docId, previousNeighborEventId);
		setEventData(eventData);
		setPasteParentId(pasteParentId);	
		setDeletedAtTimestamp(null);
		setDeletedByDevGroupId("");
		setDeleteEventId("");
		setSortOrder(sortOrder);
	}

	/**
	 * Used for creating an existing insert event coming from the database.
	 */
	public InsertEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String docId, String previousNeighborEventId, String eventData, String pasteParentId, String sortOrder)
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, 
				sequentiallyBeforeEventId, docId, previousNeighborEventId);
		setEventData(eventData);
		setPasteParentId(pasteParentId);	
		setDeletedAtTimestamp(null);
		setDeletedByDevGroupId("");
		setDeleteEventId("");
		setSortOrder(sortOrder);
	}
	
	@Override
	public String getEventType()
	{
		return INSERT_EVENT_TYPE;
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (eventData != null)
		{
			builder.append(", ");
			builder.append("eventData=");
			builder.append(eventData);
		}
		if (pasteParentId != null)
		{
			builder.append(", ");
			builder.append("pasteParentId=");
			builder.append(pasteParentId);
		}
		if (deletedAtTimestamp != null)
		{
			builder.append(", ");
			builder.append("deletedAtTimestamp=");
			builder.append(deletedAtTimestamp);
			builder.append(", ");
			builder.append("deletedByDevGroupId=");
			builder.append(deletedByDevGroupId);
			builder.append(", ");
			builder.append("deleteEventId=");
			builder.append(deleteEventId);
		}
		builder.append("]");
		return builder.toString();
	}

//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException 
//	{
//		JSONObject tempJsonObject = super.toJSON();
//		tempJsonObject.put(Constants.EVENT_DATA, getEventData());
//		tempJsonObject.put(Constants.PASTE_PARENT_ID, getPasteParentId());	
//		if (getDeletedAtTimestamp() == null)
//		{
//			tempJsonObject.put(Constants.DELETED_AT_TIMESTAMP,0);
//		}
//		else
//		{
//			tempJsonObject.put(Constants.DELETED_AT_TIMESTAMP, getDeletedAtTimestamp().getTime());
//		}
//		
//		tempJsonObject.put(Constants.DELETED_BY_DEV_GROUP_ID, getDeletedByDevGroupId());
//		tempJsonObject.put(Constants.DELETE_EVENT_ID, getDeleteEventId());
//		
//		return tempJsonObject;
//	}

	//Getters and Setters
	public String getEventData() 
	{
		return eventData;
	}
	public void setEventData(String eventData) 
	{
		this.eventData = eventData;
	}

	public String getPasteParentId() 
	{
		return pasteParentId;
	}
	public void setPasteParentId(String pasteParentId) 
	{
		this.pasteParentId = pasteParentId;
	}
	
	public Date getDeletedAtTimestamp()
	{
		return deletedAtTimestamp;
	}
	public void setDeletedAtTimestamp(Date deletedAtTimestamp)
	{
		this.deletedAtTimestamp = deletedAtTimestamp;
	}

	public String getDeletedByDevGroupId()
	{
		return deletedByDevGroupId;
	}
	public void setDeletedByDevGroupId(String deletedByDevGroupId)
	{
		this.deletedByDevGroupId = deletedByDevGroupId;
	}

	public String getDeleteEventId()
	{
		return deleteEventId;
	}
	public void setDeleteEventId(String deleteEventId)
	{
		this.deleteEventId = deleteEventId;
	}

	public String getSortOrder()
	{
		return sortOrder;
	}
	public void setSortOrder(String sortOrder)
	{
		this.sortOrder = sortOrder;
	}
}
