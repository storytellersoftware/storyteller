package core.entities;

import java.util.Date;




public class Developer extends StorytellerEntity
{
	//every developer has a name and email address
	private String email;
	private String firstName;
	private String lastName;

	/**
	 * Constructor for making a brand new Developer
	 * @param timestamp
	 * @param createdUnderNodeId
	 * @param groupId
	 */
	public Developer(Date timestamp, String createdUnderNodeId, String createdByDevGroupId, String email, String firstName, String lastName)
	{
		//if no id is specified use the developer's email address as the id
		super(email, timestamp, createdUnderNodeId, createdByDevGroupId);
		setEmail(email);
		setFirstName(firstName);
		setLastName(lastName);
	}

	/**
	 * Constructor for use when resurrecting an object from the database
	 * @param id
	 * @param timestamp
	 * @param createdUnderNodeIdId
	 * @param devGroupId
	 */
	public Developer(String id, Date timestamp, String createdUnderNodeId, String devGroupId, String email, String firstName, String lastName)
	{
		super(id, timestamp, createdUnderNodeId, devGroupId);
		setEmail(email);
		setFirstName(firstName);
		setLastName(lastName);
	}

	/*********************
	  GETTERS and SETTERS
	 *********************/
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Developer [email=");
		builder.append(email);
		builder.append(", firstName=");
		builder.append(firstName);
		builder.append(", lastName=");
		builder.append(lastName);
		builder.append(", getCreatedUnderNodeId()=");
		builder.append(getCreatedUnderNodeId());
		builder.append(", getCreatedByDevGroupId()=");
		builder.append(getCreatedByDevGroupId());
		builder.append("]");
		return builder.toString();
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}
	
	public void update(Developer updateDeveloper)
	{
		this.update((StorytellerEntity)updateDeveloper);
		
		this.setEmail(updateDeveloper.getEmail());
		this.setFirstName(updateDeveloper.getFirstName());
		this.setLastName(updateDeveloper.getLastName());
	}
}
