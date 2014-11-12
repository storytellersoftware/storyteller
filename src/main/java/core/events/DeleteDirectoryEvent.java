package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public class DeleteDirectoryEvent extends DirectoryEvent
{
	public static final String DELETE_DIRECTORY_EVENT_TYPE = "DELETE-DIRECTORY"; 

	//this is the old name of a directory
	private String directoryOldName;

	/**
	 * Constructor used when creating a new DeleteDirectoryEvent. Will generate a unique ID.
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param nodeSequenceNum
	 * @param sequentiallyBeforeEventId
	 * @param directoryId
	 */
	public DeleteDirectoryEvent(Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String directoryId, String parentDirectoryId, String dirOldName) 
	{
		super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId, directoryId, parentDirectoryId);
		//store the old name
		setDirectoryOldName(dirOldName);
	}

	public DeleteDirectoryEvent(String id, Date timestamp, String createdUnderNodeId, String devGroupId, int nodeSequenceNum, String sequentiallyBeforeEventId, String directoryId, String parentDirectoryId, String dirOldName) 
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum, sequentiallyBeforeEventId, directoryId, parentDirectoryId);
		//store the old name
		setDirectoryOldName(dirOldName);
	}
	
	@Override
	public String getEventType() 
	{
		return DELETE_DIRECTORY_EVENT_TYPE;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (directoryOldName != null)
		{
			builder.append(", ");
			builder.append("directoryBeingDeleted=");
			builder.append(directoryOldName);
		}
		builder.append("]");
		return builder.toString();
	}

//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException 
//	{
//		JSONObject tempJsonObject = super.toJSON();
//		tempJsonObject.put(Constants.OLD_NAME, getDirectoryOldName());
//		
//		return tempJsonObject;
//	}
	public String getDirectoryOldName()
	{
		return directoryOldName;

	}

	public void setDirectoryOldName(String directoryOldName)
	{
		this.directoryOldName = directoryOldName;
	}

}
