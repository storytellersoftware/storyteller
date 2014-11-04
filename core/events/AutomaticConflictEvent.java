package core.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class AutomaticConflictEvent extends CombinatorialEvent
{
	public static final String AUTOMATIC_CONFLICT_EVENT_TYPE = "AUTOMATIC-CONFLICT";
	private List<String> idsOfEventsInThisBlock;
	
	/**
	 * Creates an AutomaticConflictEvent.  Allows just StoryTellerEvents to be passed in (for convenience)
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param NodeSequenceNum
	 * @param sequentiallyBeforeEventId
	 * @param eventsInBlock			A list of StorytellerEvents that are in the conflict block
	 */
	public AutomaticConflictEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, List<? extends Object> eventsInBlock, String documentId)
	{
		super(timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId, documentId);
		idsOfEventsInThisBlock = new ArrayList<String>(eventsInBlock.size());
		for(Object event: eventsInBlock)
		{
			if (event instanceof StorytellerEvent)
			{
				idsOfEventsInThisBlock.add(((StorytellerEvent) event).getId());
			}
			else
			{
				idsOfEventsInThisBlock.add(event.toString());
			}
		}
	}

	public AutomaticConflictEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, List<? extends Object> eventsInBlock, String documentId)
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId, documentId);
		idsOfEventsInThisBlock = new ArrayList<String>(eventsInBlock.size());
		for(Object event: eventsInBlock)
		{
			if (event instanceof StorytellerEvent)
			{
				idsOfEventsInThisBlock.add(((StorytellerEvent) event).getId());
			}
			else
			{
				idsOfEventsInThisBlock.add(event.toString());
			}
		}
	}
	
	@Override
	public String getEventType()
	{
		return AUTOMATIC_CONFLICT_EVENT_TYPE;
	}


	public List<String> getIdsOfEventsInThisBlock()
	{
		return idsOfEventsInThisBlock;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (idsOfEventsInThisBlock != null)
		{
			builder.append(", ");
			builder.append("idsOfEventsInThisBlock=");
			builder.append(idsOfEventsInThisBlock);
		}
		builder.append("]");
		return builder.toString();
	}


}
