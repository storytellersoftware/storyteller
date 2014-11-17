package core.events;

import java.util.Date;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import core.Constants;


public class RenameDirectoryEvent extends DirectoryEvent
{
	public static final String RENAME_DIRECTORY_EVENT_TYPE = "RENAME-DIRECTORY";

	//this is the old name of a directory
	private String directoryOldName;

	//this is the new name of a directory
	private String directoryNewName;

	/**
	 * Constructor used when creating a new RenameDirectoryEvent. Will generate a unique ID.
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param nodeSequenceNum
	 * @param sequentiallyBeforeEventId
	 * @param directoryId
	 * @param directoryNewName
	 */
	public RenameDirectoryEvent(Date timestamp, String createdUnderNodeId,
			String devGroupId, int nodeSequenceNum,
			String sequentiallyBeforeEventId, String directoryId, 
			String directoryNewName, String directoryOldName, 
			String parentDirectoryId) 
	{
		super(timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum,
				sequentiallyBeforeEventId, directoryId, parentDirectoryId);
		setDirectoryOldName(directoryOldName);
		setDirectoryNewName(directoryNewName);

	}

	public RenameDirectoryEvent(String id, Date timestamp, String createdUnderNodeId,
			String devGroupId, int nodeSequenceNum,
			String sequentiallyBeforeEventId, String directoryId, 
			String directoryNewName, String directoryOldName, 
			String parentDirectoryId) 
	{
		super(id, timestamp, createdUnderNodeId, devGroupId, nodeSequenceNum,
				sequentiallyBeforeEventId, directoryId, parentDirectoryId);
		setDirectoryOldName(directoryOldName);
		setDirectoryNewName(directoryNewName);

	}
	
	@Override
	public String getEventType() 
	{
		return RENAME_DIRECTORY_EVENT_TYPE;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (directoryOldName != null)
		{
			builder.append(", ");
			builder.append("directoryOldName=");
			builder.append(directoryOldName);
		}
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

	public String getDirectoryOldName()
	{
		return directoryOldName;
	}

//	@Override
//	@Deprecated
//	public JSONObject toJSON() throws JSONException 
//	{
//		JSONObject tempJsonObject =super.toJSON();
//		tempJsonObject.put(Constants.OLD_NAME, getDirectoryOldName());
//		tempJsonObject.put(Constants.NEW_NAME, getDirectoryNewName());
//
//		return tempJsonObject;
//	}

	public void setDirectoryOldName(String directoryOldName)
	{
		this.directoryOldName = directoryOldName;
	}
}
