package core.entities;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import core.Constants;




public class Storyboard extends StorytellerEntity
{
	private String name;
	private String description;
	
	public Storyboard(Date timestamp, String createdUnderNodeIdId, String devGroupId, String name, String description) 
	{
		super(timestamp, createdUnderNodeIdId, devGroupId);
		setName(name);
		setDescription(description);
	}

	public Storyboard(String id, Date timestamp, String createdUnderNodeIdId, String devGroupId, String name, String description) 
	{
		super(id, timestamp, createdUnderNodeIdId, devGroupId);
		setName(name);
		setDescription(description);
	}

	@Override
	public String toString()
	{
		return null;
	}

	@Override
	@Deprecated
	public JSONObject toJSON() throws JSONException
	{
		JSONObject tempJson = super.toJSON();
		tempJson.put(Constants.NAME, getName());
		tempJson.put(Constants.DESCRIPTION, getDescription());
		return tempJson;
		
	}

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public void update(Storyboard updateStoryboard)
	{
		this.update((StorytellerEntity)updateStoryboard);
		
		this.setName(updateStoryboard.getName());
		this.setDescription(updateStoryboard.getDescription());
	}
}
