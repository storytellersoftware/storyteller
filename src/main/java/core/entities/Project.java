package core.entities;

import java.util.Date;



public class Project extends StorytellerEntity
{

	private String projectName;
	
	public Project(Date timestamp, String devGroupId, String projectName)
	{
		super(timestamp, null, devGroupId);
		this.projectName=projectName;
	}

	public Project(String id, Date timestamp, String devGroupId, String projectName)
	{
		super(id, timestamp, null, devGroupId);
		this.projectName=projectName;
	}

	//Project should be read only
	public String getProjectName()
	{
		return projectName;
	}
	
	public void update(Project updateProject)
	{
		this.update((StorytellerEntity) updateProject);
	}
	
}
