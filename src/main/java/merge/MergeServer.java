package merge;

import java.util.HashMap;
import java.util.Map;
import core.StorytellerCore;

public class MergeServer implements Runnable
{
	private final Map<String, MergeSession> currentMergeSessions = new HashMap<String, MergeSession>();


	public MergeServer(StorytellerCore core)
	{
		
	}
	
	@Override
	public void run()
	{
		
	}
//    /**
//     * MERGE section
//     * 
//     * The section for all of the merge requests and events.
//     * 
//     * CONTENTS:
//     * 		getMergeSession
//     * 			The first part of the handshake. It sends the session data
//     * 
//     * 		startMergeSession
//     * 			The second part of the handshake.
//     * 
//     * 		getMergeConflict
//     * 
//     * 		postConflisctResolution
//     */
//
//    /**
//     * Merge Handshake part 1.  Sends Merge Session Data (sessionID and nodeTree) in exchange for a developer email address
//     * 
//     * @param parsedArguments contains a first name and a last name for a developer
//     * 
//     * TODO come up with a better name for this method - get implies HTTP GET
//     */
//    private void getMergeSession(JSONObject parsedArguments) throws JSONException, DBAbstractionException, IOException
//    {
//	JSONArray developerEmails = parsedArguments.getJSONArray(Constants.EMAIL);
//
//	JSONObject devGroupPackage = getDevGroupIdFromEmailAddresses(developerEmails);
//
//	if (devGroupPackage.has(DEVELOPER_GROUP_ID))
//	{
//	    JSONObject mergeSessionData = generateMergeSession(devGroupPackage.getString(DEVELOPER_GROUP_ID));
//
//	    outToClientProxy.writeBytes("HTTP/1.1 200 OK\n");
//	    //Content-Type is application/json because this is json
//	    outToClientProxy.writeBytes("Content-Type: application/json\n");
//	    outToClientProxy.writeBytes("\n");
//	    outToClientProxy.writeBytes(mergeSessionData.toString()+"\n");
//	}
//	else
//	{
//	    outToClientProxy.writeBytes("HTTP/1.1 200 OK\n");
//	    //Content-Type is application/json because this is json
//	    outToClientProxy.writeBytes("Content-Type: application/json\n");
//	    outToClientProxy.writeBytes("\n");
//	    outToClientProxy.writeBytes("That developer team doesn't exist.  You must exist to merge.\n");
//	}
//    }
//
//
//    /**
//     * Merge Handshake part 2.  Takes in the sessionId, and the first node and second node to merge.
//     * 
//     * Sends back the state of all documents in the commonAncestor at that point
//     * 
//     * @param parsedArguments
//     */
//    private void startMergeSession(JSONObject parsedArguments) throws JSONException, DBAbstractionException, IOException
//    {
//	String firstNodeId = parsedArguments.getJSONArray(Constants.FIRST_NODE).getString(0);
//	String secondNodeId = parsedArguments.getJSONArray(Constants.SECOND_NODE).getString(0);
//	String sessionId = parsedArguments.getJSONArray(Constants.SESSION_ID).getString(0);
//
//	JSONObject commonAncestorDocumentState = receiveMergeNotification(firstNodeId, secondNodeId, sessionId);
//
//	outToClientProxy.writeBytes("HTTP/1.1 200 OK\n");
//	//Content-Type is application/json because this is json
//	outToClientProxy.writeBytes("Content-Type: application/json\n");
//	outToClientProxy.writeBytes("\n");
//
//	if (commonAncestorDocumentState==null)
//	{
//	    outToClientProxy.writeBytes("That was an invalid session id"+"\n");
//	}
//	else
//	{
//	    outToClientProxy.writeBytes(commonAncestorDocumentState.toString()+"\n");
//	}
//
//
//
//    }
//
//    /**
//     * Merge Handshake part 3 takes in a sessionID and returns the next mergeConflict to be played or resolved
//     * 
//     * @param parseArguments
//     * 
//     * TODO come up with a better name for this method, get implies HTTP GET
//     */
//    private void getMergeConflict(JSONObject parseArguments) throws JSONException, DBAbstractionException, IOException
//    {
//	String sessionId = parseArguments.getJSONArray(Constants.SESSION_ID).getString(0);
//
//	JSONObject retval = getMergeConflict(sessionId);
//
//	outToClientProxy.writeBytes("HTTP/1.1 200 OK\n");
//	//Content-Type is application/json because this is json
//	outToClientProxy.writeBytes("Content-Type: application/json\n");
//	outToClientProxy.writeBytes("\n");
//
//	if (retval ==null)
//	{
//	    outToClientProxy.writeBytes("That was an invalid session id"+"\n");
//	}
//	else
//	{
//	    outToClientProxy.writeBytes(retval.toString()+"\n");
//	}
//
//
//
//
//    }
//
//    /**
//     * Merge Handshake part 4
//     * 
//     * Called when a manual conflict has been resolved.  Takes in the id of the first conflict and the id of the
//     * second conflict (To tell what's what) and then the id of the winner.
//     * 
//     * @param parsedArguments
//     * @deprecated
//     */
//    private void conflictResolutionWasSent(JSONObject parsedArguments) throws JSONException, DBAbstractionException, IOException
//    {
//
//	String sessionId = parsedArguments.getJSONArray(Constants.SESSION_ID).getString(0);
//	String idOfFirstConflict = parsedArguments.getJSONArray(Constants.ID).getString(0);
//	String idOfSecondConflict = parsedArguments.getJSONArray(Constants.PARTNER_ID).getString(0);
//	String idOfWinner = parsedArguments.getJSONArray(Constants.WINNER_ID).getString(0);
//
//	JSONArray customResolutionEvents = new JSONArray();
//	if (parsedArguments.has(Constants.CUSTOM_RESOLUTION_EVENTS))
//	{
//	    customResolutionEvents = new JSONArray(parsedArguments.getJSONArray(Constants.CUSTOM_RESOLUTION_EVENTS).getString(0).replace("%22", ""));
//	}
//
//
//	JSONObject retval = updateConflict(sessionId, idOfFirstConflict, idOfSecondConflict, idOfWinner, customResolutionEvents);
//
//	outToClientProxy.writeBytes("HTTP/1.1 200 OK\n");
//	//Content-Type is application/json because this is json
//	outToClientProxy.writeBytes("Content-Type: application/json\n");
//	outToClientProxy.writeBytes("\n");
//	if (retval ==null)
//	{
//	    outToClientProxy.writeBytes("That was an invalid session id"+"\n");
//	}
//	else
//	{
//	    outToClientProxy.writeBytes(retval.toString()+"\n");
//	}
//
//
//    }

//	/**
//	 * Called when a user wants to merge two nodes together.
//	 * 
//	 * Finds all the events in both nodes that are after the point of divergence, blocks them together and finds
//	 * the conflicts.
//	 * 
//	 * @param firstNodeId
//	 * @param secondNodeId
//	 * @param sessionId
//	 * @return
//	 */
//	public JSONObject receiveMergeNotification(String firstNodeId, String secondNodeId, String mergeSessionId) throws DBAbstractionException, JSONException
//	{
//		MergeSession currentMergeSession = currentMergeSessions.get(mergeSessionId);
//
//		//If the mergeSession doesn't exist, short circuit.
//		if (currentMergeSession==null)
//		{
//			return null;
//		}
//
//		//Get the merge proxy ready because the merge is starting for real.
//		currentMergeSession.setFirstNodeId(firstNodeId);
//		currentMergeSession.setSecondNodeId(secondNodeId);
//		List<RealizedNode> firstNodeLineage = getPlaybackNodesThroughNode(firstNodeId);
//		List<RealizedNode> secondNodeLineage = getPlaybackNodesThroughNode(secondNodeId);
//		String devGroupId = currentMergeSession.getDeveloperGroupId();
//
//		Node firstNode = getLocalDatabase().getNode(firstNodeId);
//		Node secondNode = getLocalDatabase().getNode(secondNodeId);
//
//		if(firstNode.getNodeType().equals(Node.OPEN_NODE))			//if this guy is open, we need to close him
//		{
//			firstNode.close();
//			getLocalDatabase().updateNode(firstNode);
//
//			StorytellerEvent lastEvent = getLocalDatabase().getLastEventInNode(firstNodeId);
//
//			//put a CloseNodeEvent right after the current last event in the node
//			CloseNodeEvent closeNodeEvent = new CloseNodeEvent(new Date(), lastEvent.getCreatedUnderNodeId(), devGroupId, lastEvent.getNodeSequenceNum()+1, lastEvent.getId());
//
//			getLocalDatabase().insertEvent(closeNodeEvent);
//		}
//
//		if(secondNode.getNodeType().equals(Node.OPEN_NODE))		//if this guy is open, we need to close him
//		{
//			secondNode.close();
//			getLocalDatabase().updateNode(secondNode);
//
//			StorytellerEvent lastEvent = getLocalDatabase().getLastEventInNode(secondNodeId);
//
//			//put a CloseNodeEvent right after the current last event in the node
//			CloseNodeEvent closeNodeEvent = new CloseNodeEvent(new Date(), lastEvent.getCreatedUnderNodeId(), devGroupId, lastEvent.getNodeSequenceNum()+1, lastEvent.getId());
//
//			getLocalDatabase().insertEvent(closeNodeEvent);
//		}
//
//		String pointOfDivergence = getPointOfDivergence(firstNodeLineage, secondNodeLineage);
//
//
//
//		List<StorytellerEvent> firstNodeEventList = getEventsAfterPointOfDivergence(pointOfDivergence, firstNodeLineage);
//		List<StorytellerEvent> secondNodeEventList = getEventsAfterPointOfDivergence(pointOfDivergence, secondNodeLineage);
//
//		List<MergeBlock> firstNodeBlocks = blockify(firstNodeEventList);
//		List<MergeBlock> secondNodeBlocks = blockify(secondNodeEventList);
//
//		findManualConflicts(firstNodeBlocks, secondNodeBlocks, firstNodeId, secondNodeId);
//
//		currentMergeSession.setListOfFirstMergeBlocks(firstNodeBlocks);
//		currentMergeSession.setListOfSecondMergeBlocks(secondNodeBlocks);
//
//		//The merge event will point back to the pointOfDivergence now
//		Node youngestCommonAncestorNode = getLocalDatabase().getNode(getLocalDatabase().getEvent(pointOfDivergence).getCreatedUnderNodeId());
//
//
//		IDESession ideSession = new IDESession();
//		ideSession.setProject(getLocalDatabase().getProject(youngestCommonAncestorNode.getProjectId()));
//		currentMergeSession.setIdeSessionThatManagesThisMerge(ideSession);
//
//
//		Node mergeNode = new Node(new Date(), youngestCommonAncestorNode.getId(), devGroupId, "mergeNode", "this is a merge node", youngestCommonAncestorNode.getProjectId(), youngestCommonAncestorNode.getNodeLineageNumber() + 1, Node.OPEN_NODE);
//
//		getLocalDatabase().insertNode(mergeNode);
//
//		//set our ideSession to be inside that mergeNode
//		ideSession.setCurrentNode(mergeNode);
//		ideSession.setSequentiallyBeforeIdOfLastEvent(pointOfDivergence);		//The merge event happens after the point of divergence
//
//		ideSession.setSequenceNumber(0);
//
//		MergeEvent mergeEvent = new MergeEvent(new Date(), ideSession.getCurrentNode().getId(), devGroupId, ideSession.getSequenceNumber(), ideSession.getSequentiallyBeforeIdOfLastEvent(), firstNodeId, secondNodeId, youngestCommonAncestorNode.getId());
//		getLocalDatabase().insertEvent(mergeEvent);
//
//		ideSession.setSequenceNumber(ideSession.getSequenceNumber()+1);
//		ideSession.setSequentiallyBeforeIdOfLastEvent(mergeEvent.getId());
//
//
//		//The mergeSesion's ideSession (used to follow along for the custom resolutions) needs both a playbackDocumentRenderer and
//		//a copy helper
//		ideSession.setIDEState(new IDEState());
//		ideSession.setCopyHelper(new CopyHelper(this));
//
//		//Emulate through to the nodeId and the point of divergence
//		updateIDESessionToBeAtTheEndOfNode(ideSession, youngestCommonAncestorNode.getId(), pointOfDivergence);
//
//		//Now, RESET our session to be back in the mergeNode, because updateIDESessionToBeAtTheEndOfNode will automatically
//		//move the stuff to be in the ANCESTOR NODE.
//		ideSession.setCurrentNode(mergeNode);
//		ideSession.setSequentiallyBeforeIdOfLastEvent(pointOfDivergence);		//The merge event happens after the point of divergence
//
//		ideSession.setSequenceNumber(1);
//
//		//get that state
//		return getFileDataWithIdsForRendering(ideSession.getIDEState());
//
//	}
//
//	/**
//	 * Finds the event at which there is a divergence in the two given lineages.
//	 * 
//	 * Returns the id of that event.
//	 * 
//	 * @param firstNodeLineage
//	 * @param secondNodeLineage
//	 * @return
//	 * @throws DBAbstractionException
//	 */
//	private String getPointOfDivergence(List<RealizedNode> firstNodeLineage, List<RealizedNode> secondNodeLineage) throws DBAbstractionException
//	{
//		int youngestCommonAncestorNodeIndex = -1;
//
//		//Go through the lineages and find the index of the last common node
//		for(int i = 0;i<firstNodeLineage.size()&&i<secondNodeLineage.size();i++)
//		{
//			if (firstNodeLineage.get(i).getNodeId().equals(secondNodeLineage.get(i).getNodeId()))
//			{
//				youngestCommonAncestorNodeIndex = i;
//			}
//			else //once we get into unequal nodes, break.
//			{
//				break;
//			}
//		}
//		if (youngestCommonAncestorNodeIndex==-1)
//		{
//			//No common ancestor node
//			return null;
//		}
//
//		String pointOfDivergence = null;
//
//		int numEventsInFirstNodeLineagesYoungestCommonAncestorNode = firstNodeLineage.get(youngestCommonAncestorNodeIndex).getAllEventsReleventToFuture().size(); //I know, it's not saving any typing.  But it'll be more clear what's going on.  READABILITY IS #1!
//		int numEventsInSecondNodeLineagesYoungestCommonAncestorNode = secondNodeLineage.get(youngestCommonAncestorNodeIndex).getAllEventsReleventToFuture().size();
//
//		if (numEventsInFirstNodeLineagesYoungestCommonAncestorNode == numEventsInSecondNodeLineagesYoungestCommonAncestorNode)
//		{
//			//both new nodes point to the end (or at least the same event) of the youngestCommonAncestorNode.
//			pointOfDivergence = firstNodeLineage.get(youngestCommonAncestorNodeIndex).getAllEventsReleventToFuture().get(numEventsInFirstNodeLineagesYoungestCommonAncestorNode-1).getId();
//		}
//		else if (numEventsInFirstNodeLineagesYoungestCommonAncestorNode< numEventsInSecondNodeLineagesYoungestCommonAncestorNode)
//		{
//			//The first branch branched before the second branch did in the youngestCommonAncestorNode.
//			//return the last event in the first node's youngestCommonAncestor
//			pointOfDivergence = firstNodeLineage.get(youngestCommonAncestorNodeIndex).getAllEventsReleventToFuture().get(numEventsInFirstNodeLineagesYoungestCommonAncestorNode-1).getId();
//		}
//		else
//		{
//			//The second branch branched before the first branch did in the youngestCommonAncestorNode.
//			//return the last event in the second node's youngestCommonAncestor
//			pointOfDivergence = secondNodeLineage.get(youngestCommonAncestorNodeIndex).getAllEventsReleventToFuture().get(numEventsInSecondNodeLineagesYoungestCommonAncestorNode-1).getId();
//		}
//		return pointOfDivergence;
//
//	}
//
//	//	/**
//	//	 * Returns a JSON object with the state of all documents at the end of the nodeId given, stopping at the eventId given
//	//	 *
//	//	 * @param nodeId
//	//	 * @param eventIdToStopAt
//	//	 * @return
//	//	 *
//	//	 * Maybe unneeded because the one place that used to call it [receiveMergeNotification()] no longer does so
//	//	 */
//	//	private JSONObject getStateWithIdsThroughNode(String nodeId, String eventIdToStopAt) throws DBAbstractionException, JSONException
//	//	{
//	//
//	//		PlaybackDocumentRenderer rendering = new PlaybackDocumentRenderer();
//	//		//Emulate through to the nodeId
//	//
//	//		updateRendererThroughNode(rendering, nodeId, eventIdToStopAt);
//	//
//	//		//get that state
//	//		JSONObject state = getFileDataWithIdsForRendering(rendering);
//	//
//	//
//	//		return state;
//	//	}
//	//
//	//	/**
//	//	 * Returns a JSON object with the state of all documents at the end of the nodeId given.
//	//	 *
//	//	 * Most likely unneeded because the one place that used to call it [receiveMergeNotification()] no longer does so
//	//	 * @param nodeId
//	//	 * @return
//	//	 */
//	//	private JSONObject getStateWithIdsThroughNode(String nodeId) throws DBAbstractionException, JSONException
//	//	{
//	//
//	//		PlaybackDocumentRenderer rendering = new PlaybackDocumentRenderer();
//	//		//Emulate through to the nodeId
//	//
//	//		updateRendererThroughNode(rendering, nodeId);
//	//
//	//		//get that state
//	//		JSONObject state = getFileDataWithIdsForRendering(rendering);
//	//
//	//
//	//		return state;
//	//	}
//
//	/**
//	 * Generates a merge session for a given set of developers.
//	 * 
//	 * The next step in the handshake calls receiveMergeNotification()
//	 * 
//	 * @param developers names of developers who generate the merge session
//	 * @return a JSONObject with a sessionId and a node tree
//	 */
//	public JSONObject generateMergeSession(String devGroupId) throws JSONException, DBAbstractionException
//	{
//		JSONObject retval = new JSONObject();
//		String mergeSessionId = UUID.randomUUID().toString();
//
//		JSONArray nodeTreeData = getNodeTreeData();
//		retval.put(Constants.SESSION_ID, mergeSessionId);
//		retval.put(Constants.NODE_TREE, nodeTreeData);
//		MergeSession newMergeSession = new MergeSession(mergeSessionId, devGroupId);
//		currentMergeSessions.put(mergeSessionId, newMergeSession);
//
//		return retval;
//	}
//
//	/**
//	 * Gets the next merge conflict as a JSONObject.
//	 * 
//	 * The object looks like:
//	 * "ID" = The id of this mergeblock
//	 * "EventsInFirstConflictBlock" = For automatic conflicts, these are the events in the block.
//	 * 								  For manual conflicts, these are the events in the first block.
//	 * "IsManualConflict" = is this a manual conflict?
//	 * "FromList" = is this block from the first developer or the second developer in the merge?
//	 * "IsLast" = is this the last mergeblock in this merge?
//	 * [For Manual Conflicts]
//	 * "EventsInSecondConflictBlock" = the events in the second block
//	 * "PartnerId" = the id of the merge block whose conflict is currently being resolved.
//	 * 
//	 * @param sessionId
//	 * @return
//	 * @throws JSONException
//	 * @throws DBAbstractionException
//	 */
//	public JSONObject getMergeConflict(String mergeSessionId) throws JSONException, DBAbstractionException
//	{
//		MergeSession mergeSession = currentMergeSessions.get(mergeSessionId);
//
//		//if the merge session doesn't exist, short circuit it.
//		if (mergeSession==null)
//		{
//			return null;
//		}
//
//		IDESession ideSessionThatIsManagingMerge = mergeSession.getIdeSessionThatManagesThisMerge();
//
//
//		String devGroupId = mergeSession.getDeveloperGroupId();
//		MergeBlock conflictBlock = mergeSession.getNextMergeConflict();
//		//For automatic conflicts, write them to the database as they have been "resolved" and are ready for playback
//		if(!conflictBlock.isManualConflict())
//		{
//			AutomaticConflictEvent autoConflictEvent = new AutomaticConflictEvent(new Date(), ideSessionThatIsManagingMerge.getCurrentNode().getId(), devGroupId, ideSessionThatIsManagingMerge.getSequenceNumber(), ideSessionThatIsManagingMerge.getSequentiallyBeforeIdOfLastEvent(), conflictBlock.getEventsInThisBlock(), conflictBlock.getDocumentIdOfBlock());
//			//if we have a redundant delete we do not want to write it to the database. However, we do want to display it in the merge.
//			if(!conflictBlock.isRedundant())
//			{
//				getLocalDatabase().insertEvent(autoConflictEvent);
//				ideSessionThatIsManagingMerge.setSequenceNumber(ideSessionThatIsManagingMerge.getSequenceNumber() + 1);
//				ideSessionThatIsManagingMerge.setSequentiallyBeforeIdOfLastEvent(autoConflictEvent.getId());
//			}
//
//			conflictBlock.setResolveStatus(MergeBlock.RESOLVED);
//
//			updatePlaybackRendererWithEvents(ideSessionThatIsManagingMerge.getIDEState(), conflictBlock.getEventsInThisBlock());
//
//			if(mergeSession.isLastConflict())
//			{
//				closeCurrentOpenNodeAndCreateANewOpenNode(ideSessionThatIsManagingMerge, null, String.valueOf(new Date().getTime()), devGroupId, "merge", "merged");
//				conflictBlock.setLast(true);
//			}
//
//
//		}
//
//		return conflictBlock.toJSON();
//	}
//
//	/**
//	 * Helper for updating a MergeSession's playbackDocumentRenderer with the events from an AutomaticConflictEvent or ConflictResolution
//	 * @param playbackDocumentRenderer
//	 * @param eventsInThisBlock
//	 */
//	private void updatePlaybackRendererWithEvents(IDEState playbackDocumentRenderer, List<StorytellerEvent> eventsInThisBlock)
//	{
//		for(StorytellerEvent event: eventsInThisBlock)
//		{
//			StoryTellerEventRenderer.playEvent(playbackDocumentRenderer, event);
//		}
//
//	}
//
//	/**
//	 * Tells the merge session (referenced by sessionId) which block won after a manualConflict
//	 * 
//	 * @param sessionId
//	 * @param idOfFirstConflict
//	 * @param idOfSecondConflict
//	 * @param idOfWinner
//	 * @param customResolutionEvents
//	 * @return	A JSONObject that has JSONConstants.MESSAGE (the value is "ACK") and maybe JSONConstants.IS_LAST if this was the last merge needed to be taken care of
//	 */
//	public JSONObject updateConflict(String mergeSessionId, String idOfFirstConflict, String idOfSecondConflict, String idOfWinner, JSONArray customResolutionEvents) throws DBAbstractionException, JSONException
//	{
//		MergeSession mergeSession = currentMergeSessions.get(mergeSessionId);
//		//if the mergeSession doesn't exist, short circuit.
//		if (mergeSession==null)
//		{
//			return null;
//		}
//
//		//This is the ideSession that will be managing this merge.  Recall that the IDESession holds the documentBuffers
//		//and all of that fun stuff that the Server needs to play along
//		IDESession ideSession = mergeSession.getIdeSessionThatManagesThisMerge();
//
//		MergeBlock firstMergeBlock = mergeSession.getMergeBlockById(idOfFirstConflict);
//		MergeBlock secondMergeBlock = mergeSession.getMergeBlockById(idOfSecondConflict);
//		String devGroupId = mergeSession.getDeveloperGroupId();
//
//		boolean commitBeforeReturn = false;		//Is this the last thing that needs to be merged?
//		JSONObject acknowledgment = new JSONObject();
//
//
//		//Now we can add the manualConflictEvent to the database
//		ManualConflictEvent manualConflictEvent = new ManualConflictEvent(new Date(), ideSession.getCurrentNode().getId(), devGroupId, ideSession.getSequenceNumber(), ideSession.getSequentiallyBeforeIdOfLastEvent(), firstMergeBlock.getEventsInThisBlock(), secondMergeBlock.getEventsInThisBlock(), firstMergeBlock.getDocumentIdOfBlock());
//		getLocalDatabase().insertEvent(manualConflictEvent);
//
//		ideSession.setSequenceNumber(ideSession.getSequenceNumber() + 1);
//		ideSession.setSequentiallyBeforeIdOfLastEvent(manualConflictEvent.getId());
//
//		ConflictResolutionEvent conflictResolutionEvent = null;
//
//		//Now we update the mergeBlocks with how the resolution played out and add a conflict resolution
//		if(firstMergeBlock.getId().equals(idOfWinner))
//		{
//			//Tell firstMergeBlock to find all buddies in the secondMergeBlock and mark them as unneeded
//			firstMergeBlock.resolveConflictWith(secondMergeBlock, MergeBlock.UNNEEDED);
//			//Tell secondMergeBlock to find all buddies in the firstMergeBlock and let them go
//			secondMergeBlock.resolveConflictWith(firstMergeBlock, MergeBlock.UNRESOLVED);
//
//			if(!firstMergeBlock.isManualConflict())
//			{
//				firstMergeBlock.setResolveStatus(MergeBlock.RESOLVED);
//				List<StorytellerEvent> eventsInThisConflictResolution = firstMergeBlock.getEventsInThisBlock();
//				//first update the renderer with the events in the selected conflict resolution
//				updatePlaybackRendererWithEvents(ideSession.getIDEState(), eventsInThisConflictResolution);
//				//Then parse the custom resolution
//				//THIS IS WHERE WERE LOSING OUR EVENTS.
//				List<StorytellerEvent> eventsInCustomResolution = parseEventsInJSONArrayAndRenderThemToADocumentRenderer(customResolutionEvents, devGroupId, ideSession);
//				eventsInThisConflictResolution.addAll(eventsInCustomResolution);
//
//				//The acknowledgment needs to include how to render those events that sent here as a part of the manual conflict resolution
//				//because the browser doesn't know how to render output from the mockIDE, nor is it a trivial job to teach it.
//				//TODO REDO THIS acknowledgment.put(PLAYBACK_EVENTS_OF_CUSTOM_RESOLUTION, PlaybackProxy.convertStoryTellerEventsIntoPlaybackEvents(eventsInCustomResolution).toString());
//
//				conflictResolutionEvent = new ConflictResolutionEvent(new Date(), ideSession.getCurrentNode().getId(), devGroupId, ideSession.getSequenceNumber(),
//						ideSession.getSequentiallyBeforeIdOfLastEvent(), firstMergeBlock.getFirstEvent().getId(), eventsInThisConflictResolution, firstMergeBlock.getDocumentIdOfBlock());
//				if(mergeSession.isLastConflict())
//				{
//					commitBeforeReturn = true;
//				}
//			}
//			else
//			{
//				//This allows the winning side to be "flashed" but since there are more conflicts yet to resolve, we need to have a blank resolution until later
//				conflictResolutionEvent = new ConflictResolutionEvent(new Date(), ideSession.getCurrentNode().getId(), devGroupId, ideSession.getSequenceNumber(), ideSession.getSequentiallyBeforeIdOfLastEvent(), firstMergeBlock.getFirstEvent().getId(), new ArrayList<StorytellerEvent>(), firstMergeBlock.getDocumentIdOfBlock());
//			}
//		}
//		else if(secondMergeBlock.getId().equals(idOfWinner))
//		{
//			//Tell secondMergeBlock to find all buddies in the firstMergeBlock and mark them as unneeded
//			secondMergeBlock.resolveConflictWith(firstMergeBlock, MergeBlock.UNNEEDED);
//			//Tell firstMergeBlock to find all buddies in the secondMergeBlock and let them go
//			firstMergeBlock.resolveConflictWith(secondMergeBlock, MergeBlock.UNRESOLVED);
//			if(!secondMergeBlock.isManualConflict())
//			{
//				secondMergeBlock.setResolveStatus(MergeBlock.RESOLVED);
//
//				List<StorytellerEvent> eventsInThisConflictResolution = secondMergeBlock.getEventsInThisBlock();
//				//first update the renderer with the events in the selected conflict resolution
//				updatePlaybackRendererWithEvents(ideSession.getIDEState(), eventsInThisConflictResolution);
//				//Then parse the custom resolution
//				List<StorytellerEvent> eventsInCustomResolution = parseEventsInJSONArrayAndRenderThemToADocumentRenderer(customResolutionEvents, devGroupId, ideSession);
//				eventsInThisConflictResolution.addAll(eventsInCustomResolution);
//
//				//The acknowledgment needs to include how to render those events that sent here as a part of the manual conflict resolution
//				//because the browser doesn't know how to render output from the mockIDE, nor is it a trivial job to teach it.
//				//TODO REDO THIS acknowledgment.put(PLAYBACK_EVENTS_OF_CUSTOM_RESOLUTION, PlaybackProxy.convertStoryTellerEventsIntoPlaybackEvents(eventsInCustomResolution).toString());
//
//				conflictResolutionEvent = new ConflictResolutionEvent(new Date(), ideSession.getCurrentNode().getId(), devGroupId, ideSession.getSequenceNumber(),
//						ideSession.getSequentiallyBeforeIdOfLastEvent(), secondMergeBlock.getFirstEvent().getId(), eventsInThisConflictResolution, firstMergeBlock.getDocumentIdOfBlock());
//				if(mergeSession.isLastConflict())
//				{
//					commitBeforeReturn = true;
//				}
//
//			}
//			else
//			{	//This allows the winning side to be "flashed" but since there are more conflicts yet to resolve, we need to have a blank resolution until later
//				conflictResolutionEvent = new ConflictResolutionEvent(new Date(), ideSession.getCurrentNode().getId(), devGroupId, ideSession.getSequenceNumber(), ideSession.getSequentiallyBeforeIdOfLastEvent(), secondMergeBlock.getFirstEvent().getId(), new ArrayList<StorytellerEvent>(), firstMergeBlock.getDocumentIdOfBlock());
//			}
//		}
//		else
//		{
//			//Nobody is needed, either in first or second mergeblock
//			firstMergeBlock.resolveConflictWith(secondMergeBlock, MergeBlock.UNNEEDED);
//			firstMergeBlock.setResolveStatus(MergeBlock.UNNEEDED);
//			secondMergeBlock.resolveConflictWith(firstMergeBlock, MergeBlock.UNNEEDED);
//			secondMergeBlock.setResolveStatus(MergeBlock.UNNEEDED);
//
//			List<StorytellerEvent> eventsInThisConflictResolution = parseEventsInJSONArrayAndRenderThemToADocumentRenderer(customResolutionEvents, devGroupId, ideSession);
//
//			conflictResolutionEvent = new ConflictResolutionEvent(new Date(), ideSession.getCurrentNode().getId(), devGroupId, ideSession.getSequenceNumber(), ideSession.getSequentiallyBeforeIdOfLastEvent(), null, eventsInThisConflictResolution, firstMergeBlock.getDocumentIdOfBlock());
//			if(mergeSession.isLastConflict())
//			{
//				commitBeforeReturn = true;
//			}
//		}
//		getLocalDatabase().insertEvent(conflictResolutionEvent);
//		ideSession.setSequenceNumber(ideSession.getSequenceNumber() + 1);
//		ideSession.setSequentiallyBeforeIdOfLastEvent(conflictResolutionEvent.getId());
//
//		acknowledgment.put(Constants.MESSAGE, "ACK");
//		if (commitBeforeReturn)
//		{
//			closeCurrentOpenNodeAndCreateANewOpenNode(ideSession, null, String.valueOf(new Date().getTime()), devGroupId, "merge", "merged");
//			acknowledgment.put(Constants.IS_LAST, true);
//		}
//
//		return acknowledgment;
//	}
//
//	/**
//	 * Takes a list of events (wrapped up in JSONObjects as if sent from the IDE during a custom merge sequence) in a JSONArray
//	 * and renders them to the playbackDocumentRenderer passed in.  This also puts the events in the database.
//	 * @param jsonArrayOfEvents
//	 * @param playbackDocumentRenderer
//	 * @return
//	 */
//
//	private List<StorytellerEvent> parseEventsInJSONArrayAndRenderThemToADocumentRenderer(JSONArray jsonArrayOfEvents, String developerGroupId, IDESession ideSession) throws DBAbstractionException
//	{
//		int startingSequenceNumber = ideSession.getSequenceNumber();
//		for(int i = 0;i<jsonArrayOfEvents.length();i++)
//		{
//			try
//			{
//				JSONObject currentJsonObject = jsonArrayOfEvents.getJSONObject(i);
//				String jsonObjectType = currentJsonObject.getString(TYPE);
//
//				String timestamp = String.valueOf(currentJsonObject.getLong(TIMESTAMP));
//				//String developerGroupId = currentJsonObject.getString(DEVELOPER_GROUP_ID);
//				tryToHandleEvent(ideSession, jsonObjectType, timestamp, developerGroupId, currentJsonObject);
//			}
//			catch (Exception e)
//			{
//			}
//
//		}
//		int endingSequenceNumber = ideSession.getSequenceNumber();
//		return getLocalDatabase().getEventsByNodeAndSequenceNumber(ideSession.getCurrentNode().getId(), startingSequenceNumber, endingSequenceNumber-startingSequenceNumber);
//	}
//
//	/**
//	 * This is where the rubber meets the road.  The most vital part of merging - finding conflicts.  This assumes that the blocking
//	 * requirements have been met (all events that can conflict together are blocked together and that no blocks in the same node conflict
//	 * with each other.  This is why we had to do all that business in blockify() and blockifyHelper() with blocksPointingToOutsideEvents).
//	 * 
//	 * ==How things conflict==
//	 * Blocks can conflict in several ways, depending on what the blocks contain.  Blocks are categorized based on the
//	 * first (the keystone) event in the block.  This means that we can have Text Blocks (only InsertEvents and DeleteEvents),
//	 * Document Blocks (Contains one Document event and some Text Events if the DocumentEvent is a CreateDocumentEvent) and
//	 * Directory Blocks (Contains one Directory Event and maybe some Document and Text events if the DirectoryEvent is a CreateDirectoryEvent).
//	 * 
//	 * TextBlocks can conflict with other TextBlocks if the previousNeighborEvent of the keystone events are the same (or if the documents
//	 * are the same and the previousNeighborEvents are null).
//	 * 
//	 * http://i.1dl.us/9Ut.png		A table of conflicts.  If the link doesn't work, look in the Google doc titled Storyteller Design Document under Merging
//	 * 
//	 * The table above shows how Document and Directory Blocks can conflict with each other.
//	 * For example, the table shows that a CreateDocument and a RenameDocument can conflict with each other based on name,
//	 * that is if one block creates a document foo.txt and the other block renames bar.txt to foo.txt then we have a manual conflict.
//	 * (Name implies that the parent directories are the same).  MoveDocument Blocks can conflict with other MoveDocument Blocks if both nodes
//	 *  try to move the same document, so the ID indicates this (as in both events have the same documentId).
//	 * 
//	 * Text Blocks can conflict with DeleteDocument Blocks if the document that the Text Block occurs in was deleted by the other node.
//	 * In the same way, DocumentBlocks can conflict with DeleteDirectoryBlocks.
//	 * 
//	 * ==How we can find those conflicts==
//	 * 
//	 * Just as in blockify, Maps are going to be our friends.  We'll go through firstNodeBlocks and set up the maps
//	 * and then go through secondNodeBlocks and check those blocks against the maps see if there are conflicts.
//	 * 
//	 * Map<TextConflictStruct, MergeBlock> textBlocks - this map will map a TextConflictStruct with a MergeBlock.  A TextConflictStruct (see lower in StorytellerServer)
//	 * is specially designed to be equal to another TextConflictStruct under the conditions that make TextEvents go into conflict with each
//	 * other.  For each Text Block, we create a TextConflictStruct based on the keystone event and either add it to the map (paired with the MergeBlock
//	 * it represents) or check to see if the map already contains the struct.
//	 * 
//	 * Map<NameConflictStruct, MergeBlock> documentNameBlocks - this map will map a NameConflictStruct with a MergeBlock.  A NameConflictStruct (see lower in StorytellerServer)
//	 * is specially designed to be equal to another NameConflictStruct under the conditions that make any Blocks that can conflict by name (see
//	 * the chart above).  For each Create, Move and Rename DocumentEvent, we create a NameConflictStruct based on the keystone event's parentDirectoryId
//	 * and the resulting name from each event.  Then, just like with the TextConflictStructs, we either add it to the map or check to see if the map
//	 * already contains the struct.
//	 * 
//	 * Map<NameConflictStruct, MergeBlock> directoryNameBlocks - this map will map a NameConflictStruct with a MergeBlock.  A NameConflictStruct (see lower in StorytellerServer)
//	 * is specially designed to be equal to another NameConflictStruct under the conditions that make any Blocks that can conflict by name (see
//	 * the chart above).  For each Create, Move and Rename DirectoryEvent, we create a NameConflictStruct based on the keystone event's parentDirectoryId
//	 * and the resulting name from each event.  Then, just like with the TextConflictStructs, we either add it to the map or check to see if the map
//	 * already contains the struct.
//	 * 
//	 * Map<String, Set<MergeBlock>> docIdToTextBlockMap - this map maps documentIds to a set of TextBlocks that occur in the given document. This map is used
//	 * for one purpose only, if a document is deleted, any textBlocks that were in that document are automatically in conflict with the deleteDocument block.
//	 * 
//	 * Map<String, MergeBlock> docIdBlocks - This map maps documentIds to Document Blocks that modify the document. This allows us to find any events that
//	 * can conflict by id (see the chart above) because all we have to do is map the documentId to the mergeBlock of the rename/move/delete Document.
//	 * 
//	 * Map<String, MergeBlock> dirIdBlocks - This map maps directoryIds to Directory Blocks that modify the document. This allows us to find any events that
//	 * can conflict by id (see the chart above) because all we have to do is map the directoryId to the mergeBlock of the rename/move/delete Directory.
//	 * 
//	 * @param firstNodeBlocks
//	 * @param secondNodeBlocks
//	 * @param firstNodeId
//	 * @param secondNodeId
//	 */
//	private void findManualConflicts(List<MergeBlock> firstNodeBlocks, List<MergeBlock> secondNodeBlocks, String firstNodeId, String secondNodeId) throws DBAbstractionException
//	{
//		//See the blue commentary above for what the purpose of these maps is
//		Map<TextConflictStruct, MergeBlock> textBlocks = new HashMap<StorytellerCore.TextConflictStruct, MergeBlock>();
//		Map<NameConflictStruct, MergeBlock> documentNameBlocks = new HashMap<StorytellerCore.NameConflictStruct, MergeBlock>();
//		Map<NameConflictStruct, MergeBlock> directoryNameBlocks = new HashMap<StorytellerCore.NameConflictStruct, MergeBlock>();
//
//		Map<String, Set<MergeBlock>> docIdToTextBlockMap = new HashMap<String, Set<MergeBlock>>();
//		Map<String, MergeBlock> docIdBlocks = new HashMap<String, MergeBlock>();
//		Map<String, MergeBlock> dirIdBlocks = new HashMap<String, MergeBlock>();
//
//		//Set up merge blocks into respective containers for conflict checking
//		for(MergeBlock mergeBlock: firstNodeBlocks)
//		{
//			if(mergeBlock.getFirstEvent() instanceof TextEvent)		//.getFirstEvent() returns the keystone event
//			{
//				//Create a TextConflictStruct for the TextBlock
//				TextConflictStruct newTextBlock = new TextConflictStruct(mergeBlock.getDocumentIdOfBlock(), mergeBlock.getPreviousNeighborOfBlock());
//				//put it in the map
//				textBlocks.put(newTextBlock, mergeBlock);
//
//				//Try to add this TextBlock to the set of MergeBlocks that are in the same document as this one is
//				Set<MergeBlock> blocksInSameDocumentBlocks = docIdToTextBlockMap.get(mergeBlock.getDocumentIdOfBlock());
//				if (blocksInSameDocumentBlocks == null)
//				{
//					blocksInSameDocumentBlocks = new HashSet<MergeBlock>();
//					docIdToTextBlockMap.put(mergeBlock.getDocumentIdOfBlock(), blocksInSameDocumentBlocks);
//				}
//				blocksInSameDocumentBlocks.add(mergeBlock);
//
//			}
//			else if(mergeBlock.getFirstEvent() instanceof DocumentEvent)
//			{
//				//Get the keystone Event
//				DocumentEvent documentEvent = ((DocumentEvent) mergeBlock.getLastEventOfClass(DocumentEvent.class)); //FIXME this should probably get the first Event (keystone) instead.  not that it matters too much...
//				if(!(documentEvent instanceof DeleteDocumentEvent))
//				{
//					//This block can conflict by name
//					String name = getLatestNameOfDocument(documentEvent.getDocumentId(), firstNodeId);
//					NameConflictStruct newDocumentBlock;
//					//Create a NameConflictStruct based on the name and parentDirectoryId
//					if (documentEvent instanceof MoveDocumentEvent)
//					{	//for moveDocumentEvents, we want the final location, not where it started moving
//						newDocumentBlock = new NameConflictStruct(((MoveDocumentEvent) documentEvent).getNewParentDirectoryId(), name);
//					}
//					else
//					{
//						newDocumentBlock = new NameConflictStruct(documentEvent.getParentDirectoryId(), name);
//					}
//					documentNameBlocks.put(newDocumentBlock, mergeBlock);
//				}
//				if (!(documentEvent instanceof CreateDocumentEvent))
//				{
//					//The block can conflict by id, so add it to the map
//					docIdBlocks.put(documentEvent.getDocumentId(), mergeBlock);
//				}
//			}
//			else if(mergeBlock.getFirstEvent() instanceof DirectoryEvent)
//			{
//				//Get the keystone Event
//				DirectoryEvent directoryEvent = ((DirectoryEvent) mergeBlock.getLastEventOfClass(DirectoryEvent.class));  //FIXME this should probably get the first Event (keystone) instead.  not that it matters too much...
//				if(!(directoryEvent instanceof DeleteDirectoryEvent))
//				{
//					//This block can conflict by name
//					String name = getLatestNameOfDirectory(directoryEvent.getDirectoryId(), firstNodeId);
//					NameConflictStruct newDirectoryBlock;
//					//Create a NameConflictStruct based on the name and parentDirectoryId
//					if (directoryEvent instanceof MoveDirectoryEvent)
//					{	//We want to try a conflict based on where it will land, not where it is now
//						newDirectoryBlock = new NameConflictStruct(((MoveDirectoryEvent) directoryEvent).getNewParentDirectoryId(), name);
//					}
//					else
//					{
//						newDirectoryBlock = new NameConflictStruct(directoryEvent.getParentDirectoryId(), name);
//					}
//					//put it to the map
//					directoryNameBlocks.put(newDirectoryBlock, mergeBlock);
//				}
//				if (!(directoryEvent instanceof CreateDirectoryEvent))
//				{
//					//The block can conflict by id, so add it to the map
//					dirIdBlocks.put(directoryEvent.getDirectoryId(), mergeBlock);
//				}
//			}
//		}
//		//============================================================================================================================
//		//Actually check for conflicts based on the maps that we set up in part 1
//		for(MergeBlock mergeBlock: secondNodeBlocks)
//		{
//			if(mergeBlock.getFirstEvent() instanceof TextEvent)
//			{
//				TextEvent textEvent = (TextEvent)mergeBlock.getFirstEvent();
//				//Create TextConflictStruct in the same way that we did in the first list.
//				TextConflictStruct conflictStruct = new TextConflictStruct(mergeBlock.getDocumentIdOfBlock(), mergeBlock.getPreviousNeighborOfBlock());
//
//				//Here we check if two blocks are inserting text after the same character, which we know if two MergeBlock's
//				//TextConflictStruct are the same
//				if (textBlocks.containsKey(conflictStruct))
//				{
//					//as long as they are not both deletes they are in conflict.
//					if(!(mergeBlock.getTypeOfFirstEventInBlock() == "DELETE" && textBlocks.get(conflictStruct).getTypeOfFirstEventInBlock() == "DELETE"))
//					{
//						MergeBlock conflictingMergeBlock = textBlocks.get(conflictStruct);
//						mergeBlock.addManualConflictPartner(conflictingMergeBlock);
//						conflictingMergeBlock.addManualConflictPartner(mergeBlock);
//					}
//					else //otherwise though they are not conflicting one of them is redundant.
//					{
//						mergeBlock.setToRedundant();
//					}
//				}
//
//				//Here we are checking if a text event has been written to a document that has been deleted.
//				if(docIdBlocks.containsKey(textEvent.getDocumentId()))
//				{
//					MergeBlock conflictMergeBlock = docIdBlocks.get(textEvent.getDocumentId());
//					if(conflictMergeBlock.contains(DeleteDocumentEvent.class))		//Only if there is a deleteDocument Event in here, did the document actually get deleted
//					{
//						mergeBlock.addManualConflictPartner(conflictMergeBlock);
//						conflictMergeBlock.addManualConflictPartner(mergeBlock);
//					}
//				}
//			}
//			else if(mergeBlock.getFirstEvent() instanceof DocumentEvent)
//			{
//				DocumentEvent documentEvent = (DocumentEvent)mergeBlock.getLastEventOfClass(DocumentEvent.class);
//
//				if(!(documentEvent instanceof DeleteDocumentEvent))
//				{
//					//This block could conflict by name so we check if a create, rename, or move
//					//ended up making a document of the same name in the same directory.
//					String name = getLatestNameOfDocument(documentEvent.getDocumentId(), secondNodeId);
//					//If two mergeblocks share the same nameConflictStruct, then they are in conflict
//					NameConflictStruct nameConflictStruct;
//					if (documentEvent instanceof MoveDocumentEvent)
//					{	//Again, for moving, we need to check where the document will end up, not where it starts from
//						nameConflictStruct = new NameConflictStruct(((MoveDocumentEvent) documentEvent).getNewParentDirectoryId(),name);
//					}
//					else
//					{
//						nameConflictStruct = new NameConflictStruct(documentEvent.getParentDirectoryId(),name);
//					}
//					if(documentNameBlocks.containsKey(nameConflictStruct))
//					{
//						MergeBlock conflictMergeBlock = documentNameBlocks.get(nameConflictStruct);
//						mergeBlock.addManualConflictPartner(conflictMergeBlock);
//						conflictMergeBlock.addManualConflictPartner(mergeBlock);
//					}
//				}
//				else
//				{
//					//Deletes can confict with text blocks in the documents that they delete, so we best check for that here
//					Set<MergeBlock> textBlocksInSameDocumentBlocks = docIdToTextBlockMap.get(mergeBlock.getDocumentIdOfBlock());
//					if (textBlocksInSameDocumentBlocks!=null)
//					{
//						for(MergeBlock conflictMergeBlock : textBlocksInSameDocumentBlocks)
//						{
//							mergeBlock.addManualConflictPartner(conflictMergeBlock);
//							conflictMergeBlock.addManualConflictPartner(mergeBlock);
//						}
//					}
//				}
//
//				if(!(documentEvent instanceof CreateDocumentEvent))
//				{
//					//The mergeblock can conflict by id so we check to see if a delete, move, or rename tried to touch the same document.
//					if(docIdBlocks.containsKey(documentEvent.getDocumentId()))
//					{
//						MergeBlock conflictMergeBlock = docIdBlocks.get(documentEvent.getDocumentId());
//						mergeBlock.addManualConflictPartner(conflictMergeBlock);
//						conflictMergeBlock.addManualConflictPartner(mergeBlock);
//					}
//				}
//				//We have to check if we've created or moved into a directory thats been changed.
//				if(mergeBlock.contains(CreateDocumentEvent.class) || mergeBlock.contains(MoveDocumentEvent.class))
//				{
//					MoveDocumentEvent moveDocumentEvent = (MoveDocumentEvent)mergeBlock.getLastEventOfClass(MoveDocumentEvent.class);
//					if(moveDocumentEvent == null)
//					{
//						CreateDocumentEvent createDocumentEvent = (CreateDocumentEvent)mergeBlock.getLastEventOfClass(CreateDocumentEvent.class);
//						if(dirIdBlocks.containsKey(createDocumentEvent.getParentDirectoryId()))	//if the parentDirectoryId
//						{
//							//TODO Maybe change this to create conflicts only at deleteDirectory?
//							MergeBlock conflictMergeBlock = dirIdBlocks.get(createDocumentEvent.getParentDirectoryId());
//							mergeBlock.addManualConflictPartner(conflictMergeBlock);
//							conflictMergeBlock.addManualConflictPartner(mergeBlock);
//						}
//					}
//					else
//					{
//						if(dirIdBlocks.containsKey(moveDocumentEvent.getNewParentDirectoryId()))  //remember to use the moveDirectoryEvent's final destination
//						{
//							//TODO Maybe change this to create conflicts only at deleteDirectory?
//							MergeBlock conflictMergeBlock = dirIdBlocks.get(moveDocumentEvent.getNewParentDirectoryId());
//							mergeBlock.addManualConflictPartner(conflictMergeBlock);
//							conflictMergeBlock.addManualConflictPartner(mergeBlock);
//						}
//					}
//				}
//
//			}
//			else if(mergeBlock.getFirstEvent() instanceof DirectoryEvent)
//			{
//				DirectoryEvent directoryEvent = (DirectoryEvent)mergeBlock.getLastEventOfClass(DirectoryEvent.class);
//				//Here we check if a create, rename, or move, ended up making a directory of the same name in the same directory.
//				if(!(directoryEvent instanceof DeleteDirectoryEvent))
//				{
//					String name = getLatestNameOfDirectory(directoryEvent.getDirectoryId(), secondNodeId);
//					NameConflictStruct nameConflictStruct;
//					if (directoryEvent instanceof MoveDirectoryEvent)
//					{
//						nameConflictStruct = new NameConflictStruct(((MoveDirectoryEvent) directoryEvent).getNewParentDirectoryId(),name);
//					}
//					else
//					{
//						nameConflictStruct = new NameConflictStruct(directoryEvent.getParentDirectoryId(),name);
//					}
//					if(directoryNameBlocks.containsKey(nameConflictStruct))
//					{
//						MergeBlock conflictMergeBlock = directoryNameBlocks.get(nameConflictStruct);
//						mergeBlock.addManualConflictPartner(conflictMergeBlock);
//						conflictMergeBlock.addManualConflictPartner(mergeBlock);
//					}
//				}
//				//Here we check to see a delete, move, or rename tried to touch the same directory.
//				if(!(directoryEvent instanceof CreateDirectoryEvent))
//				{
//					if(dirIdBlocks.containsKey(directoryEvent.getDirectoryId()))
//					{
//						MergeBlock conflictMergeBlock = dirIdBlocks.get(directoryEvent.getDirectoryId());
//						mergeBlock.addManualConflictPartner(conflictMergeBlock);
//						conflictMergeBlock.addManualConflictPartner(mergeBlock);
//					}
//				}
//				//We have to check if weve created or moved into a directory thats been changed.
//				if(mergeBlock.contains(CreateDirectoryEvent.class) || mergeBlock.contains(MoveDirectoryEvent.class))
//				{
//					MoveDirectoryEvent moveDirectoryEvent = (MoveDirectoryEvent)mergeBlock.getLastEventOfClass(MoveDirectoryEvent.class);
//					if(moveDirectoryEvent == null)
//					{
//						CreateDirectoryEvent createDirectoryEvent = (CreateDirectoryEvent)mergeBlock.getLastEventOfClass(CreateDirectoryEvent.class);
//						if(dirIdBlocks.containsKey(createDirectoryEvent.getParentDirectoryId()))
//						{
//							//TODO Maybe change this to create conflicts only at deleteDirectory?
//							MergeBlock conflictMergeBlock = dirIdBlocks.get(createDirectoryEvent.getParentDirectoryId());
//							mergeBlock.addManualConflictPartner(conflictMergeBlock);
//							conflictMergeBlock.addManualConflictPartner(mergeBlock);
//						}
//					}
//					else
//					{
//						if(dirIdBlocks.containsKey(moveDirectoryEvent.getNewParentDirectoryId()))	//remember to use the moveDirectoryEvent's final destination
//						{
//							//TODO Maybe change this to create conflicts only at deleteDirectory?
//							MergeBlock conflictMergeBlock = dirIdBlocks.get(moveDirectoryEvent.getNewParentDirectoryId());
//							mergeBlock.addManualConflictPartner(conflictMergeBlock);
//							conflictMergeBlock.addManualConflictPartner(mergeBlock);
//						}
//					}
//				}	//Look at all the closing curly braces!
//			}
//		}
//	}
//	@Deprecated		//This was used in preliminary testing.  It's no longer needed
//	public List<MergeBlock> testBlockify(String nodeId) throws DBAbstractionException
//	{
//		List<RealizedNode> nodeLineage = getPlaybackNodesThroughNode(nodeId);
//
//		List<StorytellerEvent> nodeEventList = getEventsAfterPointOfDivergence(null, nodeLineage);
//
//		return blockify(nodeEventList);
//	}
//	/**
//	 * Groups events into blocks.  A block is defined as a group of events that is related and that depends only on one of those events (the first event
//	 * or "keystone" event), i.e. if the keystone event conflicts with the keystone event of another block, then really all the events in the block
//	 * are in conflict with all of the events from the other block.
//	 * 
//	 * ==TextEvents==
//	 * Text events should be blocked together if they form a "chain", that is "ABCDEFG" should be blocked together with a keystone of A because A
//	 * is the insertion point, B is following A, C following B and so on. This is done by having a Map of textEventIds to the MergeBlock they are in.
//	 * If a TextEvent's previousNeighborId is in this Map, then the TextEvent (both InsertEvents and DeleteEvents) is stuck to the end of the MergeBlock.
//	 * If not, then a new MergeBlock is created with that event as the keystone event.  This means that if I delete some text that I haven't typed, each individual
//	 * deleteEvent will have its own block.
//	 * 
//	 * Text events can also be blocked with a CreateDocumentEvent.  If a document is newly created, then any text typed in it cannot possibly be
//	 * at conflict with any other text.  They only way the text would be in conflict with anything is if the new document conflicts with another document or
//	 * directory event.
//	 * 
//	 * ==DocumentEvents==
//	 * As mentioned above, CreateDocumentEvents can be blocked with TextEvents.  ALL other DocumentEvents are blocked separately (because a DocumentEvent
//	 * always has the potential to conflict).  There is one exception - if a newly created document is deleted, that DeleteDocumentEvent can be added to the
//	 * block because it cannot be in conflict with anything else.
//	 * 
//	 * Blocks with a CreateDocumentEvent as a keystone can also be blocked with CreateDirectoryEvents because if I create a new directory and then put
//	 * some new files in it, then those cannot conflict with anything from other nodes, unless the newly created Directory had a conflict.
//	 * 
//	 * ==DirectoryEvents==
//	 * Work very similarly to DocumentEvents.  CreateDirectoryEvents can be blocked with blocks that have a CreateDocumentEvent as the keystone if that newly
//	 * created Document is in the newly created Directory. ALL other DirectoryEvents are blocked separately (because a DirectoryEvent
//	 * always has the potential to conflict).  There is one exception - if a newly created directory is deleted, that DeleteDirectoryEvent can be added to the
//	 * block because it cannot be in conflict with anything else. (which has not been implemented yet)
//	 * 
//	 * 
//	 * @param eventList
//	 * @return
//	 */
//	private List<MergeBlock> blockify(List<StorytellerEvent> eventList)
//	{
//		Map<String, MergeBlock> eventIdToMergeBlock = new HashMap<String, MergeBlock>();	//textEventIds to MergeBlocks  (EVERY textEvent!)  says which text event is in which merge block.  Used to add links to a chain
//		//blocks to stuff that we made in this node
//		Map<String, MergeBlock> newlyCreatedDocumentIdToMergeBlock = new HashMap<String, MergeBlock>(); 	// maps an id of a newly created document to the MergeBlock that it is a part of
//		Map<String, MergeBlock> newlyCreatedDirectoryIdToMergeBlock = new HashMap<String, MergeBlock>();	// maps an id of a newly created directory to the MergeBlock that it is a part of
//
//		//blocks to stuff that was made in ancestral nodes
//		Map<String, MergeBlock> blocksPointingToBeginningOfDoc = new HashMap<String, MergeBlock>();
//		Map<String, MergeBlock> blocksPointingToOutsideEvents = new HashMap<String, MergeBlock>();	//maps previousNeighborIds to a mergeBlock because if two text events have the same previousNeighbor, they should be a part of the same block
//
//		List<MergeBlock> allMergeBlocks = new ArrayList<MergeBlock>();
//
//		blockifyHelper(eventList, eventIdToMergeBlock, newlyCreatedDocumentIdToMergeBlock, newlyCreatedDirectoryIdToMergeBlock, blocksPointingToBeginningOfDoc, blocksPointingToOutsideEvents, allMergeBlocks);
//		return allMergeBlocks;
//
//	}
//
//	/**
//	 * A helper for blocking the events that allows for recursive calls.
//	 * 
//	 * @param eventList	the list of events that should be blocked into chunks
//	 * @param eventIdToMergeBlock	a list between every TextEvent and which block it has been put in
//	 * @param newlyCreatedDocumentIdToMergeBlock	If a document was created, this maps to which block it is in
//	 * @param newlyCreatedDirectoryIdToMergeBlock	If a directory was created, this maps to which block it is in
//	 * @param blocksPointingToOutsideEvents			A map of previous neighbors to MergeBlocks.  Allows us to combine two shorter chains into a long chain, if their previous neighbors are the same.
//	 * @param blocksPointingToBeginningOfDoc		Since a block that edits the beginning of document would have a previous neighborId of null, we use this map to map those blocks
//	 * @param allMergeBlocks	All created mergeBlocks MUST be added to this list.  This is the compilation data structure for the mergeBlocks.
//	 */
//	private void blockifyHelper(List<StorytellerEvent> eventList, Map<String, MergeBlock> eventIdToMergeBlock, Map<String, MergeBlock> newlyCreatedDocumentIdToMergeBlock, Map<String, MergeBlock> newlyCreatedDirectoryIdToMergeBlock, Map<String, MergeBlock> blocksPointingToBeginningOfDoc, Map<String, MergeBlock> blocksPointingToOutsideEvents, /*Map<String, MergeBlock> documentIdToMutatingBlocks, Map<String, MergeBlock> directoryIdToMutatingBlocks,*/ List<MergeBlock> allMergeBlocks)
//	{
//		Iterator<StorytellerEvent> iterator = eventList.iterator();
//		//Using an iterator, walk through each element in eventList.  An iterator is used due the developer having better control than either a for or a foreach loop
//		while (iterator.hasNext())
//		{
//			StorytellerEvent event = iterator.next();
//
//			if (event instanceof OpenNodeEvent || event instanceof CloseNodeEvent || event instanceof MergeEvent)
//			{
//				continue;			//No Conflict.  These can never conflict and shouldn't be a part of any block
//			}
//			else if (event instanceof CreateDirectoryEvent)
//			{
//				CreateDirectoryEvent createEvent = (CreateDirectoryEvent)event;
//				MergeBlock newBlock = null;
//
//				//If a new directory is created in a newly created directory, it should go as a part of that new directories block because the
//				//new directory can't conflict with anything unless the new directory would.
//				if(newlyCreatedDirectoryIdToMergeBlock.containsKey(createEvent.getParentDirectoryId()))
//				{
//					newBlock = newlyCreatedDirectoryIdToMergeBlock.get((createEvent.getParentDirectoryId()));
//					newBlock.addEvent(createEvent);
//				}
//				else
//				{
//					//Create a new block with the CreateDirectoryEvent as the keystone
//					//and add it to the allMergeBlock list
//					newBlock = new MergeBlock(event);
//					allMergeBlocks.add(newBlock);
//				}
//				//Put the id of the newly created directory into the newlyCreatedDirectoryIdToMergeBlock map so that if any other directories or documents
//				//are created in this directory, we can block them together
//				newlyCreatedDirectoryIdToMergeBlock.put(createEvent.getDirectoryId(), newBlock);
//			}
//			else if (event instanceof CreateDocumentEvent)
//			{
//				CreateDocumentEvent createEvent = (CreateDocumentEvent)event;
//				MergeBlock newBlock = null;
//
//				//If a new document is created in a newly created directory, it should go as a part of that new directories block because the
//				//new document can't conflict with anything unless the new directory would.
//				if(newlyCreatedDirectoryIdToMergeBlock.containsKey(createEvent.getParentDirectoryId()))
//				{
//					newBlock = newlyCreatedDirectoryIdToMergeBlock.get((createEvent.getParentDirectoryId()));	//add this event on to the newlyCreatedDirctory of of the parentId
//					newBlock.addEvent(createEvent);
//				}
//				else
//				{
//					//If not, then we can make a brand new block with the createDocumentEvent as the keystone
//					//and add it to the allMergeBlock list
//					newBlock = new MergeBlock(event);
//					allMergeBlocks.add(newBlock);
//				}
//				//Put the id of the newly created document into the newlyCreatedDocumentIdToMergeBlock map so that if any text
//				//events are put into this document, we can block them with this one.
//				newlyCreatedDocumentIdToMergeBlock.put(createEvent.getDocumentId(), newBlock);
//			}
//			else if (event instanceof TextEvent)
//			{
//				TextEvent textEvent = (TextEvent) event;
//				String prevNeighbor = textEvent.getPreviousNeighborEventId();		//text events depend on their previous neighbor for blocking
//				MergeBlock block = null;
//				if (prevNeighbor == null)	//this means the text event was added to the beginning of a document
//				{
//					String documentId = textEvent.getDocumentId();
//
//					//If this text event was created in a newly created document, stick it on the end of that block.
//					//This means that any text with a previous neighbor to this text event will be added on to this block as well.
//					if (newlyCreatedDocumentIdToMergeBlock.containsKey(documentId))
//					{
//						block = newlyCreatedDocumentIdToMergeBlock.get(documentId);
//						block.addEvent(event);
//					}
//					//If there already is a block pointing to the beginning of a document, this text event should be included
//					//with that one.  Imagine someone types "apple" at the beginning of the document and then moves
//					//their cursor to the beginning again and types "zebra".  Those events should be blocked together,
//					//with the 'a' in apple being the keystone event because that 'a' is the potential point of conflict.
//					else if (blocksPointingToBeginningOfDoc.containsKey(documentId))
//					{
//						block = blocksPointingToBeginningOfDoc.get(documentId);
//						block.addEvent(event);
//					}
//					//Else, create a new block and put it in the blocksPointingToBeginningOfDoc.
//					//We need a seperate map for these than because the previous neighbor of InsertEvents
//					//referring to the beginning of a document will always be null, so we use the documentId of
//					//the event instead
//					else
//					{
//						block = new MergeBlock(event);
//						allMergeBlocks.add(block);		//be sure to add the block to the allMergeBlocks list
//						blocksPointingToBeginningOfDoc.put(documentId, block);
//					}
//				}
//				else
//				{
//					//If a TextEvent's previous neighbor is already in a block, find that block and add this event on to the
//					//end of that block (think adding a link to a chain)
//					if (eventIdToMergeBlock.containsKey(prevNeighbor))
//					{
//						block = eventIdToMergeBlock.get(prevNeighbor);
//						block.addEvent(event);
//					}
//					//If a TextEvent's previousNeighbor is the the same as the keystone of another block, we should combine this
//					//event with that block.  let's imagine a document that contains the text "horse".  If I add "apple" to the end
//					//of horse ("horseapple") and then type zebra after horse ("horsezebraapple"), the z (and subsequent letters) should
//					//be added to the block that the 'a' in apple is the keystone of because if the 'a' is in conflict, the 'z' (and all other
//					//subsequent letters in zebra) will be in conflict as well.
//					else if (blocksPointingToOutsideEvents.containsKey(prevNeighbor))
//					{
//						block = blocksPointingToOutsideEvents.get(prevNeighbor);
//						block.addEvent(event);
//					}
//					//Otherwise make a new mergeblock, add it to the allMergeBlocks and to the map of previousNeighborIds to MergeBlocks.
//					else
//					{
//						block = new MergeBlock(event);
//						allMergeBlocks.add(block);
//						blocksPointingToOutsideEvents.put(prevNeighbor,block);
//					}
//				}
//				//Always add the block to the eventIdToMergeBlock map.  This will allow us to easily add links to our chains of text.
//				eventIdToMergeBlock.put(event.getId(), block);
//			}
//			else if (event instanceof DocumentEvent)
//			{
//				DocumentEvent documentEvent = (DocumentEvent)event;
//
//				//DocumentEvents that are not CreateDocumentEvents (we strained those out earlier, remember?) should be their own
//				//separate block unless the user deleted a document that they had created in that node because since they were
//				//the only ones to deal with that document, deleting it will not and cannot be a conflict.
//				if (newlyCreatedDocumentIdToMergeBlock.containsKey(documentEvent.getDocumentId()) && documentEvent instanceof DeleteDocumentEvent)
//				{
//					MergeBlock block = newlyCreatedDocumentIdToMergeBlock.get(documentEvent.getDocumentId());
//					block.addEvent(event);
//				}
//				else
//				{
//					//Create a new Mergeblock with this event as the keystone (and only event)
//					//Then, add it to allMergeBlocks
//					MergeBlock newBlock = new MergeBlock(event);
//					allMergeBlocks.add(newBlock);
//				}
//			}
//			else if (event instanceof DirectoryEvent)
//			{
//				DirectoryEvent directoryEvent = (DirectoryEvent)event;
//
//				//DirectoryEvents that are not CreateDirectoryEvents should be their own separate block unless the user
//				//deleted a directory that they had created in that node because since they were
//				//the only ones to deal with that directory, deleting it will not and cannot be a conflict.
//				if (newlyCreatedDirectoryIdToMergeBlock.containsKey(directoryEvent.getDirectoryId()) && directoryEvent instanceof DeleteDirectoryEvent)
//				{
//					MergeBlock block = newlyCreatedDirectoryIdToMergeBlock.get(directoryEvent.getDirectoryId());
//					block.addEvent(event);
//				}
//				else
//				{
//					//Create a new Mergeblock with this event as the keystone (and only event)
//					//Then, add it to allMergeBlocks
//					MergeBlock newBlock = new MergeBlock(event);
//					allMergeBlocks.add(newBlock);
//				}
//			}
//			else if (event instanceof AutomaticConflictEvent)
//			{
//				//The strategy for AutomaticConflictEvents is to put the events in this Conflict into a list
//				//and then recursively call blockifyHelper to blockify those events
//				int numEventsInBlock = ((AutomaticConflictEvent) event).getIdsOfEventsInThisBlock().size();		//find out how many events are in this block
//				List<StorytellerEvent> events = new ArrayList<StorytellerEvent>();
//
//				//Iterate through to get all the events in this block
//				while (iterator.hasNext() && (events.size()<numEventsInBlock))
//				{
//					event = iterator.next();
//					events.add(event);
//				}
//				//Did you mean recursion?  (seriously, if you haven't done this before,do a google seearch for recursion)
//				blockifyHelper(events, eventIdToMergeBlock, newlyCreatedDocumentIdToMergeBlock, newlyCreatedDirectoryIdToMergeBlock, blocksPointingToBeginningOfDoc, blocksPointingToOutsideEvents, allMergeBlocks);
//				//this will update any documents..extend any blocks..cure cancer..
//			}
//			else if (event instanceof ManualConflictEvent)
//			{
//				//The strategy here is to march through the first part of the ManualConflict and the second part and then
//				//basically ignore those events.  The only events relevent to the future, and therefore conflictable
//				//are those that come in the conflictResolution
//				List<StorytellerEvent> firstBranchEvents = new ArrayList<StorytellerEvent>();
//				List<StorytellerEvent> secondBranchEvents = new ArrayList<StorytellerEvent>();
//				List<StorytellerEvent> conflictResolutionEvents = new ArrayList<StorytellerEvent>();//These are the ids of the resolution that we have to recursively send to our helper
//
//				int numEventsInFirstBlock = ((ManualConflictEvent) event).getIdsOfEventsInFirstBlock().size();
//				int numEventsInSecondBlock = ((ManualConflictEvent) event).getIdsOfEventsInFirstBlock().size();
//
//				while (iterator.hasNext() && (firstBranchEvents.size()<numEventsInFirstBlock))		//skip through "first branch"
//				{
//					event = iterator.next();
//					firstBranchEvents.add(event);
//				}
//
//				while (iterator.hasNext() && (secondBranchEvents.size()<numEventsInSecondBlock))	//skip through the "second branch"
//				{
//					event = iterator.next();
//					secondBranchEvents.add(event);
//				}
//
//				//Now  we save all of the events in the conflictResolution portion of the manual conflict and blockify those.
//				event = iterator.next();
//				ConflictResolutionEvent conflictResolve= (ConflictResolutionEvent)event;
//				int numEventsInResolutionBlock = conflictResolve.getIdsOfEventsInThisBlock().size();
//
//				while(iterator.hasNext() && (conflictResolutionEvents.size()<numEventsInResolutionBlock))
//				{
//					event = iterator.next();
//					conflictResolutionEvents.add(event);
//				}
//
//				blockifyHelper(conflictResolutionEvents, eventIdToMergeBlock, newlyCreatedDocumentIdToMergeBlock, newlyCreatedDirectoryIdToMergeBlock, blocksPointingToBeginningOfDoc, blocksPointingToOutsideEvents, allMergeBlocks);
//
//			}
//
//		}
//	}
//
//	/**
//	 * Gets a list of events after the point of divergence, given a node lineage to follow.
//	 * @param youngestCommonAncestor
//	 * @param nodeLineage
//	 * @return
//	 * @throws DBAbstractionException
//	 */
//	private static List<StorytellerEvent> getEventsAfterPointOfDivergence(String idOfPointOfDivergence, List<RealizedNode> nodeLineage) throws DBAbstractionException
//	{
//		List<StorytellerEvent> events = new ArrayList<StorytellerEvent>();
//		Iterator<RealizedNode> iterator = nodeLineage.iterator();
//		boolean keepLookingForPointOfDivergence = true && idOfPointOfDivergence!=null;		// if idOfPointOfDivergence is null, just start from the beginning
//		while (keepLookingForPointOfDivergence)
//		{
//			//get the next node
//			RealizedNode node = iterator.next();
//			List<StorytellerEvent> eventsInNode = node.getAllEventsReleventToFuture();
//
//			//look in this node for the point of divergence
//			for (StorytellerEvent event : eventsInNode)
//			{
//				if(!keepLookingForPointOfDivergence)
//				{
//					//After we've found the point of divergence (which could be in the middel of a node), need to add the rest
//					//of that node to the list
//					events.add(event);
//				}
//				else if (event.getId().equals(idOfPointOfDivergence))		//maybe this is the p.o.d.?
//				{
//					keepLookingForPointOfDivergence=false;
//				}
//			}
//		}
//		//iterator.next() returns the first playback node after the node that contains the point of divergence
//		while(iterator.hasNext())
//		{
//			RealizedNode node = iterator.next();
//			events.addAll(node.getEventsFromNode(0, node.getNumEventsInNode()));// get all events in this node
//		}
//		return events;
//	}
//	
//	/**
//	 * A class specially designed to be equal to another TextConflictStruct under the same conditions that would cause
//	 * two TextEvent blocks to conflict (by previousNeighborId, unless it's null {indicating that the TextEvent was added
//	 * to the beginning of a document}, in which case check the documentId).
//	 */
//	private class TextConflictStruct
//	{
//		private final String docId;
//		private final String prevNeighborId;
//
//		public TextConflictStruct(String docId, String prevNeighborId)
//		{
//			this.docId = docId;
//			this.prevNeighborId = prevNeighborId;
//		}
//
//		@Override
//		public int hashCode()
//		{
//			if(prevNeighborId == null)
//			{
//				return docId.hashCode();
//			}
//			return prevNeighborId.hashCode();
//		}
//
//		@Override
//		public boolean equals(Object obj)
//		{
//			if (obj instanceof TextConflictStruct)
//			{
//				return obj.hashCode()==hashCode();
//			}
//			return false;
//		}
//	}
//	/**
//	 * A class specially designed to be equal to another NameConflictStruct under the conditions that make any Blocks that can conflict by name
//	 * which is by having the same parentDirectoryId and the same name.
//	 */
//	private class NameConflictStruct
//	{
//		private final String parentDirId;
//		private final String name;
//		public NameConflictStruct(String parentDirId, String name)
//		{
//			this.parentDirId = parentDirId;
//			this.name = name;
//		}
//
//		@Override
//		public int hashCode()
//		{
//			return (parentDirId + name).hashCode();
//		}
//
//		@Override
//		public boolean equals(Object obj)
//		{
//			if (obj instanceof NameConflictStruct)
//			{
//				return obj.hashCode()==hashCode();
//			}
//			return false;
//		}
//	}
}
