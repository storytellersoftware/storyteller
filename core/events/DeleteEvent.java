package core.events;

import java.util.Date;

public class DeleteEvent extends TextEvent
{
	public static final String DELETE_EVENT_TYPE = "DELETE"; 

	public DeleteEvent(String id, Date timestamp, String createdUnderNodeId,String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String docId, String previousNeighborEventId)
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId, docId,	previousNeighborEventId);
		
	}
	
	public DeleteEvent(Date timestamp, String createdUnderNodeId,String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String docId, String previousNeighborEventId)
	{
		super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId, docId,	previousNeighborEventId);
		
	}
	
	@Override
	public String getEventType()
	{
		return DELETE_EVENT_TYPE;
	}
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("]");
		return builder.toString().replaceFirst("previousNeighborEventId", "idOfDeletedInsertEvent");
	}
}
