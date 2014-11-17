package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;



public class OpenNodeEvent extends StorytellerEvent
{
	public static final String OPEN_NODE_EVENT_TYPE = "OPEN-NODE";
	
	private String sequentiallyBeforeNodeID = null;

	/**
	 * Used for making a brand new OpenNodeEvent
	 * @param timestamp
	 * @param createdUnderNodeId   this should be the ID of the new, freshly opened node (NOT OF THIS OpenNodeEvent)
	 * @param devGroupId
	 * @param NodeSequenceNum
	 * @param sequentiallyBeforeEventId
	 * @param sequentiallyBeforeNodeId  this is the node from which this path branches
	 */
	public OpenNodeEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, String sequentiallyBeforeNodeId)
	{
		super(timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId);
		setSequentiallyBeforeNodeID(sequentiallyBeforeNodeId);
	}

	public OpenNodeEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int NodeSequenceNum, String sequentiallyBeforeEventId, String sequentiallyBeforeNodeId)
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, NodeSequenceNum, sequentiallyBeforeEventId);
		setSequentiallyBeforeNodeID(sequentiallyBeforeNodeId);
	}
	
	@Override
	public String getEventType()
	{
		return OPEN_NODE_EVENT_TYPE;
	}

	public String getSequentiallyBeforeNodeID()
	{
		return sequentiallyBeforeNodeID;
	}

	public void setSequentiallyBeforeNodeID(String sequentiallyBeforeNodeID)
	{
		this.sequentiallyBeforeNodeID = sequentiallyBeforeNodeID;
	}
	
//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException
//	{
//		JSONObject tempJsonObject = super.toJSON();
//		tempJsonObject.put(Constants.PREVIOUS_NODE_ID, getSequentiallyBeforeNodeID());
//		return tempJsonObject;
//	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (sequentiallyBeforeNodeID != null)
		{
			builder.append(", ");
			builder.append("sequentiallyBeforeNodeID=");
			builder.append(sequentiallyBeforeNodeID);
		}
		if (getClass().equals(OpenNodeEvent.class))		//if this guy isn't being subclassed
		{
			builder.append("]");
		}
		return builder.toString();
	}
	
	
}
