package core.entities;

import java.util.Date;



public class Document extends StorytellerEntity 
{
	//TODO get rid of this! just like a document doesn't have a permanent name it doesn't have a permanent parent dir id???
	//this is the owning parent directory
	private String parentDirectoryId;

	/**
	 * Constructor for making a brand new Document
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param parentDirectoryId The directory that this document exists in
	 */
	public Document(Date timestamp, String createdUnderNodeId, String devGroupId, String parentDirectoryId)
	{
		super(timestamp, createdUnderNodeId, devGroupId);
		this.parentDirectoryId=parentDirectoryId;
	}

	/**
	 * Constructor for use when resurrecting a Document from the database
	 * @param ID
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param devGroupId
	 * @param parentDirectoryId The directory that this document exists in
	 */
	public Document(String ID, Date timestamp, String createdUnderNodeId, String devGroupId, String parentDirectoryId)
	{
		super(ID, timestamp, createdUnderNodeId, devGroupId);
		this.parentDirectoryId=parentDirectoryId;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Document parent id: ");
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
	
	public void update(Document updateDocument)
	{
		this.update((StorytellerEntity)updateDocument);
		
		this.setParentDirectoryId(updateDocument.getParentDirectoryId());
	}
}

