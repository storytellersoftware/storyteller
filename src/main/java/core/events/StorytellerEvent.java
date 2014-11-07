package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;
import core.entities.StorytellerEntity;



public abstract class StorytellerEvent extends StorytellerEntity
{
	//this is the sequence number in the node
	private int nodeSequenceNum;
	//this is the id of the event submitted before this one (for ordering by time)
	private String sequentiallyBeforeEventId;

	//used to indicate if the event is relevant in a particular playback. Relevant events
	//are animated in a playback with some delay in between each relevant event
	//this data member is NOT stored in the database 
	private boolean isRelevantForPlayback;

	//used to indicate if an event should be displayed as relevant (even if it is not)
	//this happens when people want to see just the end results of a playback or a 
	//'perfect programmer' playback  
	//this data member is NOT stored in the database 
	private boolean displayAsRelevant;
	/**
	 * Constructor for use when creating a brand new StoryTellerEvent
	 * 
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param nodeSequenceId
	 * @param sequentiallyBeforeEventId
	 */
	public StorytellerEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId)
	{
		super(id, timestamp, createdUnderNodeId, devGroupId);
		setNodeSequenceNum(nodeSequenceNum);
		setSequentiallyBeforeEventId(sequentiallyBeforeEventId);
		setIsRelevantForPlayback(false);
	}
	
	/**
	 * Constructor for use when creating a brand new StoryTellerEvent
	 * 
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param nodeSequenceId
	 * @param sequentiallyBeforeEventId
	 */
	public StorytellerEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId)
	{
		super(timestamp, createdUnderNodeId, devGroupId);
		setNodeSequenceNum(nodeSequenceNum);
		setSequentiallyBeforeEventId(sequentiallyBeforeEventId);
		setIsRelevantForPlayback(false);
	}
	
	/**
	 * This returns the event type
	 * 
	 * @return A string to store in the db with the event type
	 */
	public abstract String getEventType();
	
	/**
	 * Returns an id based on the createdUnderNodeId and the nodeSequenceNum
	 * @param createdUnderNodeId
	 * @param nodeSequenceNum
	 * @return
	 */
	/*public final static String createEventId(String createdUnderNodeId, int nodeSequenceNum)
	{
		return createdUnderNodeId+"_"+nodeSequenceNum;
	}*/
	
	/*public static int getSequenceNumFromId(String id)
	{
		return Integer.valueOf(id.substring(id.indexOf('_')+1));
	}
	public static String getNodeIdFromId(String id)
	{
		return id.substring(0,id.indexOf('_'));
	}*/
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getEventType());
		builder.append(" [nodeSequenceNum=");
		builder.append(nodeSequenceNum);
		builder.append(", ");
		if (sequentiallyBeforeEventId != null)
		{
			builder.append("sequentiallyBeforeEventId=");
			builder.append(sequentiallyBeforeEventId);
			builder.append(", ");
		}
		if (getId() != null)
		{
			builder.append("id=");
			builder.append(getId());
			builder.append(", ");
		}
		if (getTimestamp() != null)
		{
			builder.append("timestamp=");
			builder.append(getTimestamp());
			builder.append(", ");
		}
		if (getCreatedUnderNodeId() != null)
		{
			builder.append("createdUnderNodeId=");
			builder.append(getCreatedUnderNodeId());
			builder.append(", ");
		}
		if (getCreatedByDevGroupId() != null)
		{
			builder.append("devGroupId=");
			builder.append(getCreatedByDevGroupId());
		}
		//builder.append("]");
		return builder.toString();
	}

	//Getters and Setters
	public int getNodeSequenceNum() 
	{
		return nodeSequenceNum;
	}
	public void setNodeSequenceNum(int nodeSequenceNum) 
	{
		this.nodeSequenceNum = nodeSequenceNum;
	}

	public String getSequentiallyBeforeEventId()
	{
		return sequentiallyBeforeEventId;
	}
	public void setSequentiallyBeforeEventId(String sequentiallyBeforeEventId) 
	{
		if (sequentiallyBeforeEventId==null || sequentiallyBeforeEventId.equals("null"))
		{
			this.sequentiallyBeforeEventId= null;
		}
		else 
		{
			this.sequentiallyBeforeEventId = sequentiallyBeforeEventId;
		}
	}

	public boolean getIsRelevantForPlayback()
	{
		return isRelevantForPlayback;
	}

	public void setIsRelevantForPlayback(boolean isRelevantForPlayback)
	{
		this.isRelevantForPlayback = isRelevantForPlayback;
	}

	public boolean getDisplayAsRelevantButDoNotAnimate()
	{
		return displayAsRelevant;
	}

	public void setDisplayAsRelevantButDoNotAnimate(boolean displayAsRelevant)
	{
		this.displayAsRelevant = displayAsRelevant;
	}
}
