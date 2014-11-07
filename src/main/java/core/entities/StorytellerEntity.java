package core.entities;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import core.Constants;
import core.data.DBAbstraction;
import core.data.DBAbstractionException;



public abstract class StorytellerEntity implements Cloneable 
{
	private String id;
	private Date timestamp; 
	private String createdUnderNodeId;
	private String createdByDevGroupId;
	
	public StorytellerEntity(Date timestamp, String createdUnderNodeId, String devGroupId)
	{
		this();
		setTimestamp(timestamp);
		setCreatedUnderNodeId(createdUnderNodeId);
		setCreatedByDevGroupId(devGroupId);
	}

	public StorytellerEntity(String id, Date timestamp, String createdUnderNodeId, String devGroupId)
	{
		//this();
		setId(id);
		setTimestamp(timestamp);
		setCreatedUnderNodeId(createdUnderNodeId);
		setCreatedByDevGroupId(devGroupId);
	}
	
	public StorytellerEntity()
	{		
		// give it a random UUID
		setId(UUID.randomUUID().toString());
		
		// do nothing, have user add things to it...
	}

	
	@Override
	public boolean equals(Object o)
	{
/*		//assume the objects are not equal
		boolean retVal = false;
		
		//cast to a storyteller entity
		StorytellerEntity other = (StorytellerEntity)o;
		
		//compare ids- two are the same if they have the same id
		if(getId().equals(other.getId()))
		{
			retVal = true;
		}
		
		return retVal;*/
		
		//Make sure o is a StorytellerEntity
		if(!(o instanceof StorytellerEntity))
			return false;
		
		//Return if the ids are equal
		return getId().equals(((StorytellerEntity) o).getId());
	}
	
	@Override
	public int hashCode()
	{
		//use the id's hash code to hash storyteller entities
		return getId().hashCode();
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("id: ");
		builder.append(getId());
		builder.append(" ");
		
		builder.append("timestamp: ");
		builder.append(getTimestamp().toString());
		builder.append(" ");

		builder.append("created under node id: ");
		builder.append(getCreatedUnderNodeId());
		builder.append(" ");

		builder.append("dev group id: ");
		builder.append(getCreatedByDevGroupId());
		builder.append(" ");
		
		return builder.toString();
	}
	
	/**
	 * Should fill a JSONObject with enough information to be rendered at playback.
	 * 
	 * @deprecated
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSON() throws JSONException
	{
		JSONObject tempJsonObject = new JSONObject();
		tempJsonObject.put(Constants.ID, getId());
		tempJsonObject.put(Constants.TIMESTAMP, getTimestamp().getTime());
		tempJsonObject.put(Constants.CREATED_UNDER_NODE_ID, getCreatedUnderNodeId());
		tempJsonObject.put(Constants.DEVELOPER_GROUP_ID, getCreatedByDevGroupId());

		return tempJsonObject;
	}
		
	//Getters and Setters
	public String getId() 
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}
	public void setTimestamp(Date timestamp) 
	{
		this.timestamp = timestamp;
	}

	public String getCreatedUnderNodeId() 
	{
		return createdUnderNodeId;
	}
	public void setCreatedUnderNodeId(String createdUnderNodeId) 
	{
		this.createdUnderNodeId = createdUnderNodeId;
	}

	public String getCreatedByDevGroupId() 
	{
		return createdByDevGroupId;
	}

	public void setCreatedByDevGroupId(String createdByDevGroupId)
	{
		this.createdByDevGroupId = createdByDevGroupId;
	}
	
	public void update(StorytellerEntity newEntity)
	{
		this.setCreatedByDevGroupId(newEntity.getCreatedByDevGroupId());
		this.setCreatedUnderNodeId(newEntity.getCreatedUnderNodeId());
		this.setId(newEntity.getId());
		this.setTimestamp(newEntity.getTimestamp());
	}
}
