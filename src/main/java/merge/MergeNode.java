package merge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import playback.PlaybackNode;



import core.StorytellerCore;
import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.entities.Document;
import core.events.AutomaticConflictEvent;
import core.events.CloseNodeEvent;
import core.events.ConflictResolutionEvent;
import core.events.CreateDocumentEvent;
import core.events.DeleteEvent;
import core.events.InsertEvent;
import core.events.ManualConflictEvent;
import core.events.MergeEvent;
import core.events.StorytellerEvent;



/**
 * PlaybackMerges abstract events involving merging in a node in a nice, easy to use package.  
 * 
 * Doing this stuff from only the database would be very hard and potentially not very modular.
 * @author Kevin
 * @author Wojciech
 */
public class MergeNode extends PlaybackNode
{
	//The list of all conflicts that are a part of this merging.  
	private List <ConflictStruct> listOfConflicts;
	
	//protected static StorytellerCore core = null;

	//The id of the MergeEvent that starts off this mergeNode
	private String mergeEventId;
	
	//The id of the CloseNodeEvent that ends this mergeNode
	private String closeNodeId;
	
	public MergeNode(/*StorytellerCore core, */DBAbstraction db, String nodeId) throws DBAbstractionException
	{
		super(/*core, */db, nodeId, null, null);
		List <StorytellerEvent> allEventsInNode = db.getEventsByNode(nodeId);
		listOfConflicts = parseEventsForConflicts(allEventsInNode);
	}
	
	/**
	 * Takes the events that will be in the database for this merge node (many AutomaticConflictEvents and ManualConflictEvents)
	 * and builds ConflictStructs based on those events.
	 * @param allEventsInNode
	 * @return
	 */
	private List<ConflictStruct> parseEventsForConflicts(List<StorytellerEvent> allEventsInNode) throws DBAbstractionException
	{
		List <ConflictStruct> conflictBuilder = new ArrayList<ConflictStruct>();
		ConflictStruct currentConflictStruct = new ConflictStruct();
		for(StorytellerEvent e: allEventsInNode)
		{
			if (e instanceof CloseNodeEvent)
			{
				closeNodeId = e.getId();
			}
			else if (e instanceof MergeEvent)
			{
				mergeEventId = e.getId();
			}
			else
			{
				if (e instanceof AutomaticConflictEvent)
				{
					currentConflictStruct.idOfConflictEvent = e.getId();
					currentConflictStruct.isManualConflict = false;
					currentConflictStruct.idsOfFirstBlock = ((AutomaticConflictEvent) e).getIdsOfEventsInThisBlock();
					conflictBuilder.add(currentConflictStruct);	//add this conflictStruct to the list, because we have all of the info we need
					currentConflictStruct = new ConflictStruct();
				}
				else if( e instanceof ManualConflictEvent)
				{
					currentConflictStruct.idOfConflictEvent = e.getId();
					currentConflictStruct.isManualConflict = true;
					currentConflictStruct.idsOfFirstBlock = ((ManualConflictEvent) e).getIdsOfEventsInFirstBlock();
					currentConflictStruct.idsOfSecondBlock = ((ManualConflictEvent) e).getIdsOfEventsInSecondBlock();
					//We don't add the manualConflict yet, because it is missing the conflict resolution part
				}
				else if( e instanceof ConflictResolutionEvent)
				{
					currentConflictStruct.idOfResolutionEvent = e.getId();
					currentConflictStruct.idsOfResolutionBlock= ((ConflictResolutionEvent) e).getIdsOfEventsInThisBlock();
					conflictBuilder.add(currentConflictStruct);	//This wraps up the (manual) ConflictStruct we started earlier, so add it to the list.
					currentConflictStruct = new ConflictStruct();
				}
			}
		}
		return conflictBuilder;

	}
	/** 
	 * Returns the "net" events of this node, i.e. all events that actually occur, if one 
	 * were to open a new node at this node's close.  Deleted Text events ARE still included.
	 * 
	 * Things not included include events from the not selected branch(s) in a merge
	 * 
	 * @return
	 */
	//@Override
	public List<StorytellerEvent> getAllEventsReleventToFuture() throws DBAbstractionException
	{
		//return server.getLocalDatabase().getAllEventsRelevantToFutureInMergeNode(getNodeId());
		ArrayList<StorytellerEvent> events = new ArrayList<StorytellerEvent>(getNumEventsInNode()/2);
		
		events.add(getDatabase().getEvent(mergeEventId));
		
		for(ConflictStruct conflictStruct: listOfConflicts)
		{
			if (conflictStruct.isManualConflict)
			{
				for(String id: conflictStruct.idsOfResolutionBlock)
				{
					events.add(getDatabase().getEvent(id));
				}						
			}
			else
			{
				events.addAll(conflictStruct.getEventsByIndex(0, conflictStruct.getTotalLength()));
			}
		}
		events.add(getDatabase().getEvent(closeNodeId));

		return events;
	}
	
	
	/**
	 * Gets the events from this node
	 * @param index
	 * @param blockSize CANNOT exceed how many elements are in this node
	 * @return
	 */
	//@Override
	public List<StorytellerEvent> getEventsFromNode(int startIndex, int blockSize) throws DBAbstractionException
	{
		if ((startIndex+blockSize)>getNumEventsInNode())
		{
			throw new IndexOutOfBoundsException("Request for "+blockSize+" elements, starting at index "+startIndex+" is invalid because there are only "+getNumEventsInNode()+" elements in this node");
		}
		List <StorytellerEvent> listToReturn = new ArrayList<StorytellerEvent>(blockSize);
		int adjustedBlockSize = blockSize;
		if(startIndex == 0)
		{
			listToReturn.add(getDatabase().getEvent(mergeEventId));
			adjustedBlockSize--;
		}
		if (startIndex + blockSize >= getNumEventsInNode())		//make some space for the CloseNodeEvent
		{
			adjustedBlockSize--;
		}
		while(adjustedBlockSize > 0)
		{
				ConflictStruct relevantConflictStruct = findConflictStructWithIndex(startIndex + listToReturn.size() - 1);//The subtraction of one is to compensate for the merge event.

				int offset = getOffsetForConflictStruct(relevantConflictStruct);
				//ask the conflict struct to return the relevant events in its block, but only enough to get listToReturn up to the size of blockSize, and not one more or less

				List<StorytellerEvent> returnedEvents = relevantConflictStruct.getEventsByIndex(startIndex - offset + listToReturn.size() - 1, adjustedBlockSize);
				listToReturn.addAll(returnedEvents);
				adjustedBlockSize-=returnedEvents.size();

		}
		if (startIndex + blockSize >= getNumEventsInNode())
		{
			listToReturn.add(getDatabase().getEvent(closeNodeId));
		}
		return listToReturn;
	}

	/**
	 * Given a conflict Struct, what index is the first element in that conflict struct relative to all events in this PlaybackMerge
	 * @param conflictStruct
	 * @return
	 */
	private int getOffsetForConflictStruct(ConflictStruct conflictStruct)
	{
		int offset = 0;
		for(ConflictStruct c: listOfConflicts)
		{
			if(c.equals(conflictStruct))
			{
				return offset;
			}
			offset+= c.getTotalLength();
		}
		return offset;
	}
	@Override
	public int getNumEventsInNode()
	{
		int number = 2;			//add on 2 for the merge event and the close event
		for(ConflictStruct c: listOfConflicts)
		{
			number+=c.getTotalLength();
		}
		return number;
	}
	
	@Override
	public String toString()
	{
		return "PlaybackMerge in node "+getNodeId()+" with "+ getNumEventsInNode()+" events here and "+this.listOfConflicts.size()+" conflict structs";
	}
	
	//@Override
	public List<Document> getDocumentsCreatedInNode() throws DBAbstractionException
	{
		List<StorytellerEvent> events = getAllEventsReleventToFuture();
		LinkedList<Document> listOfDocuments = new LinkedList<Document>();
		for(StorytellerEvent event: events)
		{
			if (event instanceof CreateDocumentEvent)
			{
				listOfDocuments.add(getDatabase().getDocument(((CreateDocumentEvent) event).getDocumentId()));
			}
		}
		
		return listOfDocuments;
	}


	/**
	 * Given an index of an event, this returns which ConflictStruct contains that conflictStruct
	 * @param index
	 * @return
	 */
	private ConflictStruct findConflictStructWithIndex(int index)
	{
		for(ConflictStruct c: listOfConflicts)
		{
			index -= c.getTotalLength();
			if(index < 0)
			{
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Takes a Map of InsertEvents and modifies them if they were deleted in this playbackMerge
	 * @param remainingInsertEvents
	 */
	public void updateInsertEventsWithDeletedTimestamps(Map<String, InsertEvent> remainingInsertEvents) throws DBAbstractionException
	{
		List<StorytellerEvent> events = getAllEventsReleventToFuture();
		for(StorytellerEvent event : events)
		{
			if (event instanceof DeleteEvent)
			{
				String deletedInsertEventId = ((DeleteEvent) event).getPreviousNeighborEventId();
				if (remainingInsertEvents.containsKey(deletedInsertEventId))
				{
					//Set the relevant InsertEvent's deleted data
					InsertEvent deletedInsertEvent = remainingInsertEvents.get(deletedInsertEventId);
					deletedInsertEvent.setDeletedAtTimestamp(event.getTimestamp());
					deletedInsertEvent.setDeletedByDevGroupId(event.getCreatedByDevGroupId());
					deletedInsertEvent.setDeleteEventId(event.getId());
					//remove it from the mapping because we don't need to keep checking it, the insertEvent can only be deleted once
					remainingInsertEvents.remove(deletedInsertEventId);
					
					if (remainingInsertEvents.size()==0)		//short circuit if we can
						return;
				}
			}
		}
		
	}
	
	
	/**
	 * A small class that holds onto info related to automatic conflicts and manual conflicts
	 * @author Kevin
	 * @author Wojciech
	 *
	 */
	private class ConflictStruct
	{
		String idOfConflictEvent;				//this is the id of the AutomaticConflictEvent or the ManualConflictEvent
		List<String> idsOfFirstBlock = null;
		boolean isManualConflict;


		//All that is needed for an automatic merge is above this comment.
		List<String> idsOfSecondBlock = null;
		String idOfResolutionEvent;			//in the case of the ManualConflictEvent, this is the id of that event

		List<String> idsOfResolutionBlock = null;

		public int getTotalLength()
		{
			if (idsOfFirstBlock==null)
				return 0;
			int totalLength = 1 + idsOfFirstBlock.size(); // +1 is for the start we need to send the server
			if (this.isManualConflict)
			{
				totalLength += (idsOfSecondBlock.size() + idsOfResolutionBlock.size() + 1); // +1 is for the additional resolution event we need to send.	

			}
			return totalLength;
		}
		

		
		/**
		 * Returns a list of events based on index and the number of them wanted
		 * @param index
		 * @param block size
		 * @return
		 */
		public List<StorytellerEvent> getEventsByIndex(int index, int blockSize) throws DBAbstractionException
		{
			List<StorytellerEvent> eventsToReturn;
			if (isManualConflict)
			{
				eventsToReturn = getEventsFromManualConflict(index, blockSize);
			}
			else
			{
				eventsToReturn = getEventsFromAutomaticConflict(index, blockSize);
			}

			return eventsToReturn;
		}
		
		/**
		 * Does all the tricky offset handling and returns blockSize events starting at index, under the assumption
		 * that this is a properly set up automatic ConflictStruct
		 * @param index
		 * @param blockSize
		 * @return
		 */
		private List<StorytellerEvent> getEventsFromAutomaticConflict(int index, int blockSize) throws DBAbstractionException
		{
			List<StorytellerEvent> eventsToReturn = new ArrayList<StorytellerEvent>(blockSize);
			while (eventsToReturn.size()<blockSize && index<getTotalLength())
			{
				if (index==0)
				{
					eventsToReturn.add(getDatabase().getEvent(idOfConflictEvent));
				}
				else 
				{
					eventsToReturn.add(getDatabase().getEvent(idsOfFirstBlock.get(index-1)));
				}
				index++;
			}
			
			return eventsToReturn;
//			List<StorytellerEvent> eventsToReturn = new ArrayList<StorytellerEvent>(blockSize);		To be used if we can guarentee getAllEvents returns them in order
//			if (index==0)
//			{
//				eventsToReturn.add(server.getLocalDatabase().getEvent(idOfConflictEvent));
//				blockSize--;
//				index++;
//			}
//			
//			if (blockSize==0)
//			{
//				return eventsToReturn;
//			}
//			if (index-1+blockSize>idsOfFirstBlock.size())
//			{
//				blockSize=idsOfFirstBlock.size()+1-index;
//			}
//			eventsToReturn.addAll(server.getLocalDatabase().getAllEventsByIds(idsOfFirstBlock.subList(index-1, index-1+blockSize)));
//			
//			return eventsToReturn;

		}
		
		/**
		 * Does all the tricky offset handling and returns blockSize events starting at index, under the assumption
		 * that this is a properly set up manual ConflictStruct
		 * 
		 * @param index
		 * @param blockSize
		 * @return
		 */
		private List<StorytellerEvent> getEventsFromManualConflict(int index, int blockSize) throws DBAbstractionException
		{
//			//build up a list of ids to request from the database and then make one big request.  To be used if we can guarentee getAllEvents returns them in order
//			List<String> idsToRequest = new ArrayList<String>();
//			while (idsToRequest.size()<blockSize  && index<getTotalLength())
//			{
//				if (index == 0)
//				{
//					idsToRequest.add(idOfConflictEvent);	//always need the startEvent
//					index++;	//starting at 1 instead of zero
//				}
//				else if (index < 1 + idsOfFirstBlock.size())		//from 1 - lengthOfFirstBlock, return stuff in first block
//				{
//					idsToRequest.add(idsOfFirstBlock.get(index-1));
//					index++;
//				}
//				else if (index < 1 + idsOfFirstBlock.size() + idsOfSecondBlock.size())		//we are in the second block
//				{
//					idsToRequest.add(idsOfSecondBlock.get(index-1-idsOfFirstBlock.size()));
//					index++;
//				}
//				else if (index == 1 + idsOfFirstBlock.size() + idsOfSecondBlock.size()) // the start event for the resolution block
//				{
//					idsToRequest.add(idOfResolutionEvent);
//					index++;
//				}
//				else if (index <  getTotalLength())  //all of the resolutions
//				{
//					idsToRequest.add(idsOfResolutionBlock.get(idsOfResolutionBlock.size()-(getTotalLength()-index)));
//					index++;
//				}
//				else		
//				{
//					break;		//just to make sure we leave the loop
//				}
//			}
//			return server.getLocalDatabase().getAllEventsByIds(idsToRequest);
			
			 List<StorytellerEvent> eventsToReturn = new ArrayList<StorytellerEvent>(blockSize);
             while (eventsToReturn.size()<blockSize  && index<getTotalLength())
             {
                     if (index == 0)
                     {
                             eventsToReturn.add(getDatabase().getEvent(idOfConflictEvent));      //always need the startEvent
                             index++;        //starting at 1 instead of zero
                     }
                     else if (index < 1 + idsOfFirstBlock.size())            //from 1 - lengthOfFirstBlock, return stuff in first block
                     {
                             eventsToReturn.add(getDatabase().getEvent(idsOfFirstBlock.get(index-1)));
                             index++;
                     }
                     else if (index < 1 + idsOfFirstBlock.size() + idsOfSecondBlock.size())          //we are in the second block
                     {
                             eventsToReturn.add(getDatabase().getEvent(idsOfSecondBlock.get(index-1-idsOfFirstBlock.size())));
                             index++;
                     }
                     else if (index == 1 + idsOfFirstBlock.size() + idsOfSecondBlock.size()) // the start event for the resolution block
                     {
                             eventsToReturn.add(getDatabase().getEvent(idOfResolutionEvent));
                             index++;
                     }
                     else if (index <  getTotalLength())  //all of the resolutions
                     {
                             eventsToReturn.add(getDatabase().getEvent(idsOfResolutionBlock.get(idsOfResolutionBlock.size()-(getTotalLength()-index))));
                             index++;
                     }
                     else            
                     {
                             break;          //just to make sure we leave the loop
                     }
             }
             return eventsToReturn;
		}



		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("ConflictStruct [idOfStartEvent=");
			builder.append(idOfConflictEvent);
			builder.append(", idsOfFirstBlock=");
			builder.append(idsOfFirstBlock);
			builder.append(", isManualConflict=");
			builder.append(isManualConflict);
			builder.append(", idsOfSecondBlock=");
			builder.append(idsOfSecondBlock);
			builder.append(", idsOfResolutionBlock=");
			builder.append(idsOfResolutionBlock);
			builder.append("]");
			return builder.toString();
		}
	}
}
