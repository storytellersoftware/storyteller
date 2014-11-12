package merge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import core.Constants;
import core.events.DirectoryEvent;
import core.events.DocumentEvent;
import core.events.InsertEvent;
import core.events.StorytellerEvent;
import core.events.TextEvent;


/**
 * A class that keeps track of a block of code during the merging process.  This class keeps hold of the events, the MergeBlocks that
 * this block is in conflict with and the status as far as this block being resolved goes.
 * 
 * Not much processing goes on except for in resolveConflictWith
 *
 */
public class MergeBlock
{
	private List<StorytellerEvent> eventsInThisBlock = null;
	
	private boolean isManualConflict = false;
	private boolean isRedundant = false; //Redundant blocks can occur when we have two deletes of the same thing in different nodes.
	private List<MergeBlock> manualConflictPartners = new ArrayList<MergeBlock>();
	
	public static final int UNRESOLVED = 0;		//This block has not been "played" or resolved if there is a manual conflict
	public static final int RESOLVED = 1;		//This block was resolved
	public static final int UNNEEDED = 2;		//This block does not need to be resolved or played.  This happens when one of the manualConflictPartners "wins" and is selected over this block
	
	private int fromList;		//This int represents 1 or 2, the first or the second developer.  This allows us to know from which head the block belongs.
	private String id; 
	private boolean isLast = false;
	
	private int resolveStatus = UNRESOLVED;
	
	public MergeBlock(StorytellerEvent firstEvent)
	{
		id = UUID.randomUUID().toString();
		eventsInThisBlock = new ArrayList<StorytellerEvent>();
		eventsInThisBlock.add(firstEvent);
	}
	
	public boolean isManualConflict()
	{
		return isManualConflict;
	}
	
	public boolean isConflictable()
	{
		if (!(getFirstEvent() instanceof InsertEvent))		//the user may have deleted the text
		{
			for(StorytellerEvent event: eventsInThisBlock)
			{
				if (event instanceof InsertEvent)
				{
					if (((InsertEvent) event).getDeletedAtTimestamp()==null)
					{
						return true;
					}
				}
			}
			return false;
		}
		return true;		
	}
	public boolean isRedundant()
	{
		return isRedundant;
	}
	public void setToRedundant()
	{
		this.isRedundant = true;
	}

	
	public void setManualConflict(boolean isManualConflict)
	{
		this.isManualConflict = isManualConflict;
	}

	public List<MergeBlock> getManualConflictPartners()
	{
		return manualConflictPartners;
	}
	
	public void addManualConflictPartner(MergeBlock partner)
	{
		manualConflictPartners.add(partner);
		isManualConflict = true;
	}
	
	/**
	 * Priority is a classification of blocks.  One can have Text Blocks, Document Blocks and Directory Blocks, in order from 
	 * low to high as far as classification goes. Most times, events will have just one partner and priority is irrelevant.
	 * Occasionally, events will have a few partners, but they will all be the same type, so priority is irrelevant. (This could
	 * happen if developer 1 deleted a document that developer 2 was typing in.  The DeleteDocumentBlock would be in conflict with all
	 * of those Text Blocks).  
	 * 
	 * Very rarely, a MergeBlock will have many partners of different types (this could happen if developer 1 deleted a document that 
	 * developer 2 was typing in and renamed).  Priority matters here because if the user resolves one of the text events, then all
	 * of the text events should be resolved, but not necessarily the RenameDocumentBlock that is also in conflict. If the user were to resolve
	 * the RenameDocumentBlock with the DeleteDocumentBlock, then either way, the TextBlocks will be out of conflict (either ready to render or
	 * to be marked unneeded).  Thus, priority.  When a mergeblock resolves a conflict with another mergeblock, it should also resolve
	 * any partners that have the same or lower priority than the partner that got resolved.
	 * 
	 * 
	 * @return true if the first block is equal or lower priority than the second block
	 */
	private static boolean firstBlockIsEqualOrLowerPriorityThanSecond(MergeBlock firstBlock, MergeBlock secondBlock)
	{
		if (secondBlock.getFirstEvent() instanceof DirectoryEvent)
		{
			return true;		//Directory Events are as high "priority" as you can get
		}
		if (firstBlock.getFirstEvent() instanceof DirectoryEvent)
		{
			return false;		//Directory Events are as high "priority" as you can get, so if the second one isn't that and the first one is , first one wins
		}
		if (firstBlock.getFirstEvent() instanceof TextEvent)
		{
			return true;		//TextEvents are lowest priority, so if the firstBlock is a textEvent, there's no way it can "win"
		}
		if (secondBlock.getFirstEvent() instanceof TextEvent)
		{
			return false;		//TextEvents are lowest priority, so if the secondBlock is a textEvent, but firstBlock isn't there's no way it can "win"
		}
		return true;		//otherwise, a tie at DocumentEvents
		
	}
	
	/**
	 * Resolves any conflict with exPartner and related blocks.  StatusToGivePartners will be the status 
	 * that any resolved partners will get if they are resolved by this call.
	 * @param exPartner
	 * @param statusToGivePartners
	 * @return
	 */
	public boolean resolveConflictWith(MergeBlock exPartner, int statusToGivePartners)
	{
		Iterator<MergeBlock> iterator = manualConflictPartners.iterator();
		while (iterator.hasNext())
		{
			MergeBlock block = iterator.next();
			//When a mergeblock resolves a conflict with another mergeblock, it should also resolve
			//any partners that have the same or lower priority than the partner that got resolved in the same way
			if (firstBlockIsEqualOrLowerPriorityThanSecond(block, exPartner))
			{
				block.setResolveStatus(statusToGivePartners);
				block.manualConflictPartners.remove(this);
				block.recomputeIsManualConflict();		//The block should check if it is still a manual conflict
				iterator.remove();
			}
		}
		recomputeIsManualConflict();
		return isManualConflict;
	}

	private void recomputeIsManualConflict()
	{
		isManualConflict = manualConflictPartners.size()!=0;
		
	}

	public StorytellerEvent getFirstEvent()
	{
		return eventsInThisBlock.get(0);
	}

	public List<StorytellerEvent> getEventsInThisBlock()
	{
		return eventsInThisBlock;
	}
	
	public String getPreviousNeighborOfBlock()
	{
		if (getFirstEvent() instanceof TextEvent)
			return ((TextEvent) getFirstEvent()).getPreviousNeighborEventId();
		return null;
	}
	
	public String getDocumentIdOfBlock()
	{
		if (getFirstEvent() instanceof TextEvent)
			return ((TextEvent) getFirstEvent()).getDocumentId();
		if (getFirstEvent() instanceof DocumentEvent)
			return ((DocumentEvent) getFirstEvent()).getDocumentId();
		return null;	
	}
	
	public boolean contains(Class<? extends StorytellerEvent> classOfObject)
	{
		for(StorytellerEvent event: eventsInThisBlock)
		{
			if (event.getClass().equals(classOfObject))
			{
				return true;
			}
		}
		return false;
	}
	
	public StorytellerEvent getLastEventOfClass(Class<? extends StorytellerEvent> someClass)
	{
		for(int i = eventsInThisBlock.size()-1;i>=0;i--)
		{
			if (someClass.isInstance(eventsInThisBlock.get(i)))		//may not work
			{
				return eventsInThisBlock.get(i);
			}
		}
		return null;
	}

	
	public String getTypeOfFirstEventInBlock()
	{
		return getFirstEvent().getEventType();
	}
	public String getDirectoryIdOfBlock()
	{
		if (getFirstEvent() instanceof DirectoryEvent)
		{
			return ((DirectoryEvent)getFirstEvent()).getDirectoryId();
		}
		return null;
	}
	public void addEvent(StorytellerEvent e)
	{
		eventsInThisBlock.add(e);
	}
	
	public int getResolveStatus()
	{
		return resolveStatus;
	}

	public void setResolveStatus(int resolveStatus)
	{
		this.resolveStatus = resolveStatus;
	}
	
	public int getFromList()
	{
		return fromList;
	}

	public void setFromList(int fromList)
	{
		this.fromList = fromList;
	}
	
	public String getId()
	{
		return id;
	}
	
	public boolean isLast() {
		return isLast;
	}

	public void setLast(boolean isLast) {
		this.isLast = isLast;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		switch(resolveStatus)
		{
		case UNNEEDED:
			builder.append("Unneeded ");
			break;
		case RESOLVED:
			builder.append("Resolved ");
			break;
		case UNRESOLVED:
			builder.append("Unresolved ");
			break;
		}
		builder.append("MergeBlock [eventsInThisBlock=");
		builder.append(eventsInThisBlock);
		builder.append(", isManualConflict=");
		builder.append(isManualConflict?"Yes":"No");
		builder.append(", manualConflictPartners=");
		builder.append(toSimpleString(manualConflictPartners));
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Turns a list of Merge blocks into a string of mergeblocks without causing infinite recurision.  This is especially
	 * needed because "joined" merged blocks refer to each other and would call each other's toString indefinately.
	 * @param listOfMergeBlocks
	 * @return
	 */
	private static String toSimpleString(List<MergeBlock> listOfMergeBlocks)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<{");
		for(int i = 0;i<listOfMergeBlocks.size();i++)
		{
			MergeBlock mergeBlock = listOfMergeBlocks.get(i);
			if (i!=0)
			{
				builder.append(" ; ");
			}
			
			builder.append("MergeBlock [eventsInThisBlock=");
			builder.append(mergeBlock.eventsInThisBlock);
			
		}
		builder.append("}>");
		return builder.toString();
	}

	/**
	 * Creates a JSON object with enough data to render this MergeBlock.
	 * Pairings:
	 * "ID" = The id of this mergeblock
	 * "EventsInFirstConflictBlock" = For automatic conflicts, these are the events in the block.
	 * 								  For manual conflicts, these are the events in the first block.
	 * "IsManualConflict" = is this a manual conflict?
	 * "FromList" = is this block from the first developer or the second developer in the merge?
	 * "IsLast" = is this the last mergeblock in this merge?
	 * [For Manual Conflicts]
	 * "EventsInSecondConflictBlock" = the events in the second block
	 * "PartnerId" = the id of the merge block whose conflict is currently being resolved.
	 * @return
	 */
	public JSONObject toJSON() throws JSONException
	{
		JSONObject retval = new JSONObject();
		JSONArray eventsInConflict = new JSONArray();
		retval.put(Constants.ID, id);
		for(StorytellerEvent e: eventsInThisBlock)
		{
			eventsInConflict.put(e.toJSON());
		}
		retval.put(Constants.EVENTS_IN_FIRST_CONFLICT_BLOCK, eventsInConflict);
		retval.put(Constants.IS_MANUAL_CONFLICT, isManualConflict);
		retval.put(Constants.FROM_LIST, fromList);
		if(isLast)
		{
			retval.put(Constants.IS_LAST, isLast);
		}
		
		if(isManualConflict)
		{
			//Find conflicts in order of priority  DirectoryEvents first, DocumentEvents second and then TextEvents
			//Since we can only resolve the conflict with one of the potentially many partners at a time,
			//We pick the one with the highest priority in what ever order it is in the list.  
			for(MergeBlock m: manualConflictPartners)
			{
				if(m.contains(DirectoryEvent.class) && m.getResolveStatus() == MergeBlock.UNRESOLVED)
				{
					JSONArray eventsInSecondConflict = new JSONArray();
					for(StorytellerEvent e: m.eventsInThisBlock)
					{
						eventsInSecondConflict.put(e.toJSON());
					}
					retval.put(Constants.EVENTS_IN_SECOND_CONFLICT_BLOCK, eventsInSecondConflict);
					retval.put(Constants.PARTNER_ID, m.id);
					return retval;
				}
			}
			for(MergeBlock m: manualConflictPartners)
			{
				if(m.contains(DocumentEvent.class) && m.getResolveStatus() == MergeBlock.UNRESOLVED)
				{
					JSONArray eventsInSecondConflict = new JSONArray();
					for(StorytellerEvent e: m.eventsInThisBlock)
					{
						eventsInSecondConflict.put(e.toJSON());
					}
					retval.put(Constants.EVENTS_IN_SECOND_CONFLICT_BLOCK, eventsInSecondConflict);
					retval.put(Constants.PARTNER_ID, m.id);
					return retval;
				}
			}
			for(MergeBlock m: manualConflictPartners)
			{
				if(m.getResolveStatus() == MergeBlock.UNRESOLVED)
				{
					JSONArray eventsInSecondConflict = new JSONArray();
					for(StorytellerEvent e: m.eventsInThisBlock)
					{
						eventsInSecondConflict.put(e.toJSON());
					}
					retval.put(Constants.EVENTS_IN_SECOND_CONFLICT_BLOCK, eventsInSecondConflict);
					retval.put(Constants.PARTNER_ID, m.id);
					return retval;
				}
			}	
		}
		return retval;
	}




	

}
