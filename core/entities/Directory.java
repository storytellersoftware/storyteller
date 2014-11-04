package core.entities;

import java.util.Date;



public class Directory extends StorytellerEntity 
{
	//TODO get rid of this! just like a directory doesn't have a permanent name it doesn't have a permanent parent dir id???
	//this is the parent directory
	private String parentDirectoryId;

	/**
	 * Constructor for making a brand new Directory
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param parentDirectoryId The directory that this directory exists in.  If null, this is the head directory.
	 */
	public Directory(Date timestamp, String createdUnderNodeId, String devGroupId, String parentDirectoryId)
	{
		super(timestamp, createdUnderNodeId, devGroupId);
		this.parentDirectoryId=parentDirectoryId;
	}

	/**
	 * Constructor for use when resurrecting a Directory from the database
	 * @param ID
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param parentDirectoryId The directory that this directory exists in.  If null, this is the head directory.
	 */
	public Directory(String ID, Date timestamp, String createdUnderNodeId, String devGroupId, String parentDirectoryId)
	{
		super(ID, timestamp, createdUnderNodeId, devGroupId);
		this.parentDirectoryId=parentDirectoryId;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Directory parent id: ");
		builder.append(getParentDirectoryId());
		builder.append(" ");
		builder.append(super.toString());
		
		return builder.toString();
	}
	
	//Getters and Setters
	public String getParentDirectoryId() 
	{
		return parentDirectoryId;
	}
	public void setParentDirectoryId(String parentDirectoryId) 
	{
		this.parentDirectoryId = parentDirectoryId;
	}
	
	public void update(Directory updateDirectory)
	{
		this.update((StorytellerEntity)updateDirectory);
		this.setParentDirectoryId(updateDirectory.getParentDirectoryId());
	}
}