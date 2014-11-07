package merge;

import ide.IDESession;

import java.util.List;


/**
 * Has the information needed for a merging between two nodes.  It doesn't do a 
 * whole lot.  Just contains info, like the list of merge blocks from the first node
 * and the list of the merge blocks in the second node.
 * @author Kevin
 */
public class MergeSession
{
	private String sessionId;
	private String firstNodeId = null;
	private String secondNodeId = null;
	private List <MergeBlock> listOfFirstMergeBlocks = null;
	private List <MergeBlock> listOfSecondMergeBlocks = null;
	private String developerGroupId;
	
	private int firstListCounter = 0;
	private int secondListCounter = 0;
	
	private boolean firstListBlocked = false;
	private boolean secondListBlocked = false;
	
	//So, while merging, we need a tool to help handle any custom events from the IDE
	//Since we'll be dealing with IDE input, it seems fair that we'll need to use 
	//an IDESession to do this.
	private IDESession ideSessionThatManagesThisMerge = null;
	

	public MergeSession(String id, String developerGroupId)
	{
		this.sessionId = id;
		this.setDeveloperGroupId(developerGroupId);
		
	}
	
	public MergeBlock getMergeBlockById(String id)
	{
		for(MergeBlock m: listOfFirstMergeBlocks)
		{
			if (m.getId().equals(id))
			{
				return m;
			}
		}
		for(MergeBlock m: listOfSecondMergeBlocks)
		{
			if (m.getId().equals(id))
			{
				return m;
			}
		}
		return null;
	}
	public String getFirstNodeId()
	{
		return firstNodeId;
	}
	public void setFirstNodeId(String firstNodeId)
	{
		this.firstNodeId = firstNodeId;
	}
	public String getSecondNodeId()
	{
		return secondNodeId;
	}
	public void setSecondNodeId(String secondNodeId)
	{
		this.secondNodeId = secondNodeId;
	}
	public List<MergeBlock> getListOfFirstMergeBlocks()
	{
		return listOfFirstMergeBlocks;
	}
	public void setListOfFirstMergeBlocks(List<MergeBlock> listOfFirstMergeBlocks)
	{
		this.listOfFirstMergeBlocks = listOfFirstMergeBlocks;
	}
	public List<MergeBlock> getListOfSecondMergeBlocks()
	{
		return listOfSecondMergeBlocks;
	}
	public void setListOfSecondMergeBlocks(List<MergeBlock> listOfSecondMergeBlocks)
	{
		this.listOfSecondMergeBlocks = listOfSecondMergeBlocks;
	}
	public String getId()
	{
		return sessionId;
	}
	public String getDeveloperGroupId() 
	{
		return developerGroupId;
	}
	public void setDeveloperGroupId(String developerGroupId) 
	{
		this.developerGroupId = developerGroupId;
	}

	public boolean isLastConflict() 
	{
		for(MergeBlock m:listOfFirstMergeBlocks)
		{
			if (m.getResolveStatus()==MergeBlock.UNRESOLVED)
				return false;
		}
		for(MergeBlock m:listOfSecondMergeBlocks)
		{
			if (m.getResolveStatus()==MergeBlock.UNRESOLVED)
				return false;
		}
		
		
		return true;
	}
	
	/**
	 * The strategy here is to go down the first list, returning unresolved conflicts, until we hit a manual conflict that is unresolved.
	 * We then want to go over to the second list and do the same, until a manual conflict is hit in the second list.
	 * Then return the manual conflict from the first list.  Continue pattern.  This allows all events in the playback to appear
	 * in as close to order as possible in both lists.
	 * @return
	 */
	public MergeBlock getNextMergeConflict()
	{	
		MergeBlock retval = null;
		
		if(!firstListBlocked && firstListCounter < listOfFirstMergeBlocks.size())
		{
			retval = listOfFirstMergeBlocks.get(firstListCounter);
			
			while (retval.getResolveStatus()!=MergeBlock.UNRESOLVED)
			{
				firstListCounter++;
				if (firstListCounter==listOfFirstMergeBlocks.size())
				{
					retval=null; 
					break;
				}
				else
				{
					retval = listOfFirstMergeBlocks.get(firstListCounter);
				}
			}
			
			if (retval!=null)
			{
				if (retval.isManualConflict())
				{
					firstListBlocked = true;
				}
				else
				{
					retval.setFromList(1);
					firstListCounter++;
					return retval;			//it's an automatic conflict, so increment to prepare for next time
				}
			}
		}
		if (!secondListBlocked && secondListCounter < listOfSecondMergeBlocks.size())
		{
			retval = listOfSecondMergeBlocks.get(secondListCounter);
			while (retval.getResolveStatus()!=MergeBlock.UNRESOLVED)
			{
				secondListCounter++;
				if (secondListCounter==listOfSecondMergeBlocks.size())
				{
					retval=null; 
					break;
				}
				else
				{
					retval = listOfSecondMergeBlocks.get(secondListCounter);
				}
			}
			
			if (retval!=null)
			{
				if (retval.isManualConflict())
				{
					secondListBlocked = true;
				}
				else
				{
					retval.setFromList(2);
					secondListCounter++;
					return retval;			//it's an automatic conflict, so increment to prepare for next time
				}
			}	
		}
		
		if (secondListBlocked && firstListBlocked)
		{
			retval = listOfFirstMergeBlocks.get(firstListCounter);
			firstListBlocked = false;
			secondListBlocked = false;	
			retval.setFromList(1);
		}
		
		return retval;
	}

	public IDESession getIdeSessionThatManagesThisMerge()
	{
		return ideSessionThatManagesThisMerge;
	}

	public void setIdeSessionThatManagesThisMerge(IDESession ideSessionThatManagesThisMerge)
	{
		this.ideSessionThatManagesThisMerge = ideSessionThatManagesThisMerge;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("MergeSession [sessionId=");
		builder.append(sessionId);
		builder.append(", firstNodeId=");
		builder.append(firstNodeId);
		builder.append(", secondNodeId=");
		builder.append(secondNodeId);
		builder.append(", ideSessionThatManagesThisMerge=");
		builder.append(ideSessionThatManagesThisMerge);
		builder.append(", listOfFirstMergeBlocks=");
		builder.append(listOfFirstMergeBlocks);
		builder.append(", listOfSecondMergeBlocks=");
		builder.append(listOfSecondMergeBlocks);
		builder.append(", developerGroupId=");
		builder.append(developerGroupId);
		builder.append(", firstListCounter=");
		builder.append(firstListCounter);
		builder.append(", secondListCounter=");
		builder.append(secondListCounter);
		builder.append(", firstListBlocked=");
		builder.append(firstListBlocked);
		builder.append(", secondListBlocked=");
		builder.append(secondListBlocked);
		builder.append("]");
		return builder.toString();
	}

}
