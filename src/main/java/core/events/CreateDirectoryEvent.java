package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public class CreateDirectoryEvent extends DirectoryEvent 
{
	public static final String CREATE_DIRECTORY_EVENT_TYPE = "CREATE-DIRECTORY"; 
	
	//name of the newly created directory
	private String directoryNewName;
	
	/**
	 * Constructor for creating a new CreateDirectoryEvent. Will generate a unique ID.
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param nodeSequenceNum
	 * @param sequentiallyBeforeEventId
	 * @param directoryId
	 * @param directoryNewName
	 * @param parentDirectoryId
	 */
	public CreateDirectoryEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String directoryId, String directoryNewName, String parentDirectoryId)
	{
		super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId, directoryId, parentDirectoryId);
		setDirectoryNewName(directoryNewName);
	}

	public CreateDirectoryEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String directoryId, String directoryNewName, String parentDirectoryId)
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId, directoryId, parentDirectoryId);
		setDirectoryNewName(directoryNewName);
	}
	
	@Override
	public String getEventType() 
	{
		return CREATE_DIRECTORY_EVENT_TYPE;
	}
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (directoryNewName != null)
		{
			builder.append(", ");
			builder.append("directoryNewName=");
			builder.append(directoryNewName);
		}
		builder.append("]");
		return builder.toString();
	}

	//Getters and Setters
	public String getDirectoryNewName()
	{
		return directoryNewName;
	}
	public void setDirectoryNewName(String directoryNewName)
	{
		this.directoryNewName = directoryNewName;
	}

//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException
//	{
//		JSONObject tempJsonObject = super.toJSON();
//		tempJsonObject.put(Constants.NEW_NAME, getDirectoryNewName());
//
//		return tempJsonObject;
//	}
}
