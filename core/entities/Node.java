package core.entities;

import java.util.Date;



public class Node extends StorytellerEntity 
{
	//TODO move these to constants file
	public static final String OPEN_NODE = "OPEN_NODE";
	public static final String CLOSED_NODE = "CLOSED_NODE";
	
	//TODO should we store the node's parent id??? At one time we did but then it got removed- I have no idea why :(
	
	//user selected name and description
	private String name;	
	private String description;
	//node type- open or closed
	private String nodeType;
	//for sorting nodes by ancestor. This number is always greater than it's
	//parent's nodeLineageNumber but is not necessarily unique among nodes in a 
	//tree
	private Integer nodeLineageNumber;
	
	private String projectId;
	
	/**
	 * Constructor for making a brand new Node.  
	 * 
	 * @param timestamp
	 * @param createdUnderNodeId the id of the parent node
	 * @param devGroupId
	 * @param name	if null, defaults ""
	 * @param description  if null, defaults ""
	 * @param nodeLineageNumber
	 * @param nodeType	e.g. open/closed
	 */
	public Node(Date timestamp, String createdUnderNodeId, String devGroupId, String name, String description, String projectId, Integer nodeLineageNumber, String nodeType) 
	{
		super(timestamp, createdUnderNodeId, devGroupId);
		if (name == null)
			name = "";
		if (description == null)
			description = "";
		setName(name);
		setDescription(description);
		this.nodeType = nodeType;
		this.nodeLineageNumber = nodeLineageNumber;
		setProjectId(projectId);
	}
	
	/**
	 * Constructor for use when resurrecting a Node from the database
	 * 
	 * @param id
	 * @param timestamp
	 * @param createdUnderNodeId  the id of the parent node
	 * @param devGroupId
	 * @param name	if null, defaults ""
	 * @param description  if null, defaults ""
	 * @param nodeLineageNumber
	 * @param nodeType	e.g. open/closed
	 */
	public Node(String id, Date timestamp, String createdUnderNodeId, String devGroupId, String name, String description, String projectId, Integer nodeLineageNumber, String nodeType) 
	{
		super(id, timestamp, createdUnderNodeId, devGroupId);
		if (name == null)
			name="";
		if (description == null)
			description="";
		setName(name);
		setDescription(description);
		this.nodeType=nodeType;
		this.nodeLineageNumber=nodeLineageNumber;
		setProjectId(projectId);
	}
	
	/**
	 * Set this node to be closed.
	 */
	public void close()
	{
		nodeType = "CLOSED_NODE";
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Node name: ");
		builder.append(getName());
		builder.append(" ");
		builder.append("descriptiom: ");
		builder.append(getDescription());
		builder.append(" ");
		builder.append("parent node id: ");
		builder.append(getCreatedUnderNodeId());
		builder.append(" ");
		builder.append("Node type: ");
		builder.append(getNodeType());
		builder.append(" ");
		builder.append("Node lineage number: ");
		builder.append(getNodeLineageNumber());
		builder.append(" ");
		builder.append(super.toString());
		
		return builder.toString();
	}
	
	//Getters and Setters
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
	public String getNodeType() 
	{
		return nodeType;
	}
	public Integer getNodeLineageNumber() 
	{
		return nodeLineageNumber;
	}

	public String getProjectId()
	{
		return projectId;
	}

	private void setProjectId(String projectId)	//ProjectIds should be read only
	{
		this.projectId = projectId;
	}
	
	public void update(Node updateNode)
	{
		this.update((StorytellerEntity)updateNode);
		
		this.setDescription(updateNode.getDescription());
		this.setName(updateNode.getName());
	}

}
