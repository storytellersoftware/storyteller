package branchmerge.mock;
//package Playback;
//
//import static StorytellerServer.json.JSONConstants.*;
//import static StorytellerServer.playback.PlaybackProxy.ARGUMENT_ASSIGNER_CHAR;
//import static StorytellerServer.playback.PlaybackProxy.ARGUMENT_SPACER_CHAR;
//import static StorytellerServer.playback.PlaybackProxy.END_ARGUMENT_BLOCK_CHAR;
//import static StorytellerServer.playback.PlaybackProxy.GET_BLOCK_OF_EVENTS;
//import static StorytellerServer.playback.PlaybackProxy.START_ARGUMENT_BLOCK_CHAR;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.DefaultTreeModel;
//import javax.swing.tree.MutableTreeNode;
//
//import org.apache.log4j.Logger;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import StorytellerServer.json.JSONConstants;
//import StorytellerServer.playback.PlaybackFilter;
//import StorytellerServer.playback.PlaybackProxy;
//
///**
// * A playback manager that emulates what would go on in a real viewer (i.e. web browser).  
// * 
// * This class knows how to ask the playback proxy for the events (which have been transcoded to JSONObjects) and then render them.
// * 
// * @author Mark and Kevin
// *
// */
//public class MockPlaybackViewer extends PlaybackEventRenderer
//{
//	private static String devFirstName = null;
//
//	private static String devLastName = null;
//
//	private static String devEmailAddress = null;	
//	
//	public static final String PLAYBACK_RELEVANCE = "PlaybackRelevance";
//	
//	
//	//if an event was pasted, the event's previous neighbor id maps to the ID of the paste event
//	private Map <String, String> pastedInsertEventsPreviousNeighborToIDMap; 
//	
//	//this map maps the id of a pasted event to the JSONObject with all the info
//	private Map <String, JSONObject> pastedInsertEventsIDtoJSONMap;
//	
//
//	private PlaybackFilter filter = null;			//this is the filter that will control playback
//
//	private Logger logger = Logger.getLogger(getClass().getName());
//	private Logger timer = Logger.getLogger("timing."+getClass().getName());
//
//	public MockPlaybackViewer(String pathToServer)
//	{		
//		super(pathToServer);
//		setDocumentSetToRender(new PlaybackDocumentRenderer());
//		
//		pastedInsertEventsIDtoJSONMap = new HashMap < String, JSONObject>();
//		pastedInsertEventsPreviousNeighborToIDMap = new HashMap < String, String >();
//	}
//	
//	public static String getDevFirstName()
//	{
//		return devFirstName;
//	}
//	
//	public static void setDevFirstName(String devFirstName)
//	{
//		MockPlaybackViewer.devFirstName = devFirstName;
//	}
//	
//	public static String getDevLastName()
//	{
//		return devLastName;
//	}
//	
//	public static void setDevLastName(String devLastName)
//	{
//		MockPlaybackViewer.devLastName = devLastName;
//	}
//	
//	public static void setDevEmailAddress(String devEmailAddress)
//	{
//		MockPlaybackViewer.devEmailAddress = devEmailAddress;
//	}
//	
//	public static String getDevEmail()
//	{
//		return MockPlaybackViewer.devEmailAddress;
//	}
//	
//	@Override
//	public boolean shouldRenderEvent(JSONObject event) throws JSONException
//	{
//		return !event.getString(PLAYBACK_RELEVANCE).equals(PlaybackFilter.UNNEEDED);
//	}
//	
//	@Override
//	public void addInsertEvent(JSONObject event) throws JSONException
//	{
//		super.addInsertEvent(event);
//		
//		//get the id of this text event
//		String thisID = event.getString(ID);
//		
//		//get the id of the previous neighbor
//		String idOfPreviousNeighbor = event.getString(PREVIOUS_NEIGHBOR_ID);
//		if (idOfPreviousNeighbor.equals("null")||idOfPreviousNeighbor.equals(""))			//since JSON can't actually be null, we need to change from fake null to actual null
//		{
//			idOfPreviousNeighbor=null;
//		}
//		String pasteParentId = event.getString(PASTE_PARENT_ID);
//		
//		if (!pasteParentId.equals(""))
//		{
//			pastedInsertEventsPreviousNeighborToIDMap.put(idOfPreviousNeighbor, thisID);
//			pastedInsertEventsIDtoJSONMap.put(thisID, event);
//		}
//	}
//	
//	/**
//	 * Pings the playback server for the tree of nodes and renders it
//	 * @return
//	 */
//	@Override
//	public DefaultTreeModel getExpectedTreeOfNodes()
//	{
//		DefaultTreeModel tree = null;
//		try
//		{
//			JSONArray nodeArray = getNodesAndSession(getDevEmail()).getJSONArray(NODE_TREE);
//			if (nodeArray.length()==0)
//			{
//				return null;			//I'm not sure if this should ever happen
//			}
//			DefaultMutableTreeNode root = new DefaultMutableTreeNode(nodeArray.getJSONObject(0).getString(ID));	//the root is always the first node returned
//			tree = new DefaultTreeModel(root);
//			for(int i = 1;i<nodeArray.length();i++)
//			{
//				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(nodeArray.getJSONObject(i).getString(ID));
//				MutableTreeNode parent = getChildWithValue(root, nodeArray.getJSONObject(i).getString(PARENT_NODE_ID));
//				parent.insert(newNode, 0);			//put them all at the beginning
//			}
//
//		}
//		catch (Exception e)
//		{
//			throw new RuntimeException(e);
//		}
//		return tree;
//	}
//
//	/**
//	 * Sends the node data indicated and returns the filter data in JSONObject form
//	 * @param sessionId
//	 * @param nodeId
//	 * @return
//	 */
//	public JSONObject getFilterDataForNode(String sessionId, String nodeId) throws IOException, JSONException
//	{
//		String httpMessage = "GET /" + PlaybackProxy.GET_NODE + PlaybackProxy.START_ARGUMENT_BLOCK_CHAR +
//		ID + PlaybackProxy.ARGUMENT_ASSIGNER_CHAR + nodeId + PlaybackProxy.ARGUMENT_SPACER_CHAR
//		+ SESSION_ID + PlaybackProxy.ARGUMENT_ASSIGNER_CHAR + sessionId + PlaybackProxy.END_ARGUMENT_BLOCK_CHAR;
//		
//		//create a connection to the playback server
//		createSocketToPlaybackProxy();
//		outToPlaybackProxy.writeBytes(httpMessage +"\n");
//		timer.trace("Requested Filter arguments" + httpMessage);
//		outToPlaybackProxy.writeBytes("\n");
//		logger.trace("Requested Filter arguments" + httpMessage);
//		
//		inFromPlaybackProxy.readLine();
//		inFromPlaybackProxy.readLine();							 //blank line
//		inFromPlaybackProxy.readLine();							 // application/json
//		String filterArguments = inFromPlaybackProxy.readLine();
//		timer.trace("Received some Filter Arguments "+ filterArguments);
//		logger.trace("Received some Filter Arguments "+ filterArguments);
//		
//		JSONObject jobj = new JSONObject(filterArguments);
//		
//		return jobj;
//	}
//	
//	/**
//	 * Parses a JSONObject for all the filter data (isn't that convenient?) and sends it to the server
//	 * @param filterData
//	 */
//	private void parseAndSendPlaybackFilters(JSONObject filterData) throws JSONException, IOException
//	{
//
//		//this.filter = parseFilterDataToPlaybackFilter(filterData, false, false, 0, false, PlaybackFilter.SHOW_CHARACTERS);			
//		//make a filter based on the extremes given by the server and "defaults"
//		
//		this.filter = new PlaybackFilter(filterData);
//		sendFilterToServer();
//		
//	}
//	
//	private void sendFilterToServer() throws JSONException, IOException
//	{
//
//		//Prepares the query using the helper method
//		String httpMessage = "GET /" + PlaybackProxy.START_PLAYBACK + PlaybackProxy.START_ARGUMENT_BLOCK_CHAR+
//		turnFilterIntoParams(filter, getSessionId())+PlaybackProxy.END_ARGUMENT_BLOCK_CHAR;
//
//		//create a connection to the playback server
//		createSocketToPlaybackProxy();
//		outToPlaybackProxy.writeBytes(httpMessage +"\n");
//		outToPlaybackProxy.writeBytes("\n");		
//	}
//
//	private PlaybackFilter parseFilterDataToPlaybackFilter(JSONObject filterDataFromServer, boolean showPasteOrigin, boolean showOnlyDeletes, int hideDeletes, boolean showEndResultOnly, int showGroups) throws JSONException
//	{
//		long startTime = filterDataFromServer.getLong(START_TIME);
//		long endTime = filterDataFromServer.getLong(END_TIME);
//		
//		JSONArray docJarr = filterDataFromServer.getJSONArray(LIST_OF_DOCUMENTS);		//Parse out the documents
//
//		JSONArray devJarr = filterDataFromServer.getJSONArray(DEVELOPER_GROUPS);	//Parse out the Developer Groups
//
//		return null;
//		//return new PlaybackFilter(startTime, endTime, docJarr, devJarr, showPasteOrigin, showOnlyDeletes, hideDeletes, showEndResultOnly, showGroups);
//	}
//
//	/**  Handshake part 1
//	 * Gets the NodeTree and SessionID from the playback proxy
//	 * @return a JSONObject with a JSON Array of the nodes (to make a node tree) and the sessionID
//	 */
//	private JSONObject getNodesAndSession(String developerEmailAddress) throws IOException, JSONException
//	{
//		//create a connection to the playback server
//		createSocketToPlaybackProxy();
//		
//		String httpMessage = "GET /" +PlaybackProxy.GET_PROJECT + START_ARGUMENT_BLOCK_CHAR+EMAIL+ARGUMENT_ASSIGNER_CHAR+developerEmailAddress+END_ARGUMENT_BLOCK_CHAR;
//		
//		//write an http request on the socket			
//		outToPlaybackProxy.writeBytes(httpMessage +"\n");
//		timer.trace("Requested nodes and session"+httpMessage);
//		outToPlaybackProxy.writeBytes("\n");
//		logger.trace("Requested nodes and session"+httpMessage);
//		//wait for a response from the playback server
//		logger.trace(inFromPlaybackProxy.readLine());    						  //the header, which we promptly ignore
//		logger.trace(inFromPlaybackProxy.readLine());							 //blank line
//		logger.trace(inFromPlaybackProxy.readLine());							 // text/text
//		String projectData = inFromPlaybackProxy.readLine();
//		timer.trace("Recieved some ProjectData "+projectData);
//		logger.trace("Recieved some ProjectData "+projectData);
//		
//		JSONObject jobj = new JSONObject(projectData);
//		
//		return jobj;
//	}
//	
//	/**
//	 * Queries the PlaybackProxy multiple times for events in batches of 100 (just like the client would)
//	 * and returns them all in a List
//	 * @param sessionId
//	 * @return
//	 */
//	private List<JSONObject> getAllEventsInGroups() throws IOException, JSONException
//	{
//		List<JSONObject> allEvents= new LinkedList<JSONObject>();
//		int currentIndex = 0;
//		int blockSize = 100;
//		while(true)
//		{
//			String httpMessage = GET_BLOCK_OF_EVENTS+START_ARGUMENT_BLOCK_CHAR+INDEX+ARGUMENT_ASSIGNER_CHAR+currentIndex+ARGUMENT_SPACER_CHAR+
//			BLOCK_SIZE+ARGUMENT_ASSIGNER_CHAR+blockSize+ARGUMENT_SPACER_CHAR+
//			SESSION_ID+ARGUMENT_ASSIGNER_CHAR+getSessionId()+END_ARGUMENT_BLOCK_CHAR;
//			
//			//create a connection to the playback server
//			createSocketToPlaybackProxy();
//			outToPlaybackProxy.writeBytes(httpMessage +"\n");
//			timer.trace("Requested some events "+httpMessage);
//			outToPlaybackProxy.writeBytes("\n");
//			logger.trace("Requested some events "+httpMessage);
//			
//			inFromPlaybackProxy.readLine();
//			inFromPlaybackProxy.readLine();							 //blank line
//			inFromPlaybackProxy.readLine();							 // application/json
//			
//			String jarrString = inFromPlaybackProxy.readLine();
//			timer.trace("Received those events");
//			logger.trace("Received those events "+jarrString);
//			JSONArray jarr = new JSONArray(jarrString);
//			if (jarr.length()==0)
//			{
//				break;		//we are out of objects
//			}	
//			
//			for(int i = 0;i<jarr.length();i++)
//			{
//				JSONObject eventObject = jarr.getJSONObject(i);
//				filter(eventObject);
//				allEvents.add(eventObject);
//			}
//			timer.trace("Filtered those events");
//			currentIndex=allEvents.size();
//		}
//		return allEvents;
//	}
//	
//
//	/**
//	 * This method follows the proper "handshake" to talk to the playback proxy
//	 * The handshake is as follows:
//	 * Client connects and asks for project data, like the name of the project and tree of Nodes
//	 * PlaybackProxy sends projectData and a session ID
//	 * Client chooses a node and asks for filter information
//	 * PlaybackProxy returns the filter information, including the bounds for time, a list of developerGroups and a list of documents that exist at that node
//	 * Client provides filterInformation
//	 * 
//	 * Client then asks, in 10 event chunks, for all of the events to get to the last created node
//	 * 
//	 * @return
//	 */
//	protected List<JSONObject> getEventsFromPlaybackServerUpThroughLastNode()
//	{
//		//list of all events from the server
//		List<JSONObject> allEvents = new ArrayList < JSONObject >();
//		
//		try
//		{		
//			//Handshake part 1 : Ask for/Get projectData
//			JSONObject projectData = getNodesAndSession(getDevEmail());
//			JSONArray jarr = projectData.getJSONArray(NODE_TREE);		
//			setSessionId(projectData.getString(SESSION_ID));
//			
//			String lastNodeId = jarr.getJSONObject(jarr.length()-1).getString(ID);		//choose the last created node
//
//			//Handshake part 2 : Ask for/Get Filter Settings			
//			JSONObject filterData = getFilterDataForNode(getSessionId(),lastNodeId );
//			
//			//Handshake part 3 : Client Sends Playback Filters
//			parseAndSendPlaybackFilters(filterData);
//			
//			//Ask for all events in groups of 10
//			allEvents = getAllEventsInGroups();
//			
//			//and close the connection(s) that may linger.  
//			closeSocketToPlaybackProxy();
//			
//		} 
//		catch (Exception ex)
//		{
//			logger.fatal("Ouch!  Problem getting state up to last node",ex);
//		}
//		
//		return allEvents;
//	}
//	
//	
//	private List<JSONObject> getEventsFromPlaybackServerUpThroughNodeId(String nodeID)
//	{
//		//list of all events from the server
//		List<JSONObject> allEvents = new ArrayList < JSONObject >();
//		
//		try
//		{		
//			//Handshake part 1 : Ask for/Get projectData
//			JSONObject projectData = getNodesAndSession(getDevEmail());	
//			setSessionId(projectData.getString(SESSION_ID));
//
//			//Handshake part 2 : Ask for/Get Filter Settings			
//			JSONObject filterData = getFilterDataForNode(getSessionId(), nodeID);
//			
//			//Handshake part 3 : Client Sends Playback Filters
//			parseAndSendPlaybackFilters(filterData);
//			
//			//Ask for all events in groups of 10
//			allEvents = getAllEventsInGroups();
//			
//			//and close the connection(s) that may linger.  
//			closeSocketToPlaybackProxy();
//			
//		} 
//		catch (Exception ex)
//		{
//			logger.fatal("Ouch!  Problem getting state up to node "+nodeID,ex);
//		}
//		
//		return allEvents;
//	}
//	
//
//	/**
//	 * A convenience method for building the parameter string needed to send filter arguments to the PlaybackProxy
//	 * @param startTime
//	 * @param endTime
//	 * @param docIds
//	 * @param devIds
//	 * @param showPasteOrigin
//	 * @param showOnlyDeletes
//	 * @param hideDeletes
//	 * @param showEndResultOnly
//	 * @param sessionId
//	 * @return
//	 */
//	private String turnFilterIntoParams(PlaybackFilter filter, String sessionId) throws JSONException
//	{
//		StringBuilder builder = new StringBuilder();
//		builder.append(START_TIME);
//		builder.append(ARGUMENT_ASSIGNER_CHAR);
//		builder.append(filter.getStartTime());
//		builder.append(ARGUMENT_SPACER_CHAR);
//	
//		builder.append(END_TIME);
//		builder.append(ARGUMENT_ASSIGNER_CHAR);
//		builder.append(filter.getEndTime());
//		builder.append(ARGUMENT_SPACER_CHAR);
//		
//		/*
//		for(JSONObject jobj: filter.getDocs())
//		{
//			builder.append(DOCUMENT_ID);
//			builder.append(ARGUMENT_ASSIGNER_CHAR);
//			builder.append(jobj.getString(DOCUMENT_ID));
//			builder.append(ARGUMENT_SPACER_CHAR);
//		}
//		
//		for(JSONObject jobj: filter.getDevGroups())
//		{
//			builder.append(DEVELOPER_GROUP_ID);
//			builder.append(ARGUMENT_ASSIGNER_CHAR);
//			builder.append(jobj.getString(DEVELOPER_GROUP_ID));
//			builder.append(ARGUMENT_SPACER_CHAR);
//		}
//		*/
//		
//		builder.append(SHOW_PASTE_ORIGIN);
//		builder.append(ARGUMENT_ASSIGNER_CHAR);
//		builder.append(filter.isShowPasteOrigin());
//		builder.append(ARGUMENT_SPACER_CHAR);
//	
//		builder.append(SHOW_ONLY_DELETES);
//		builder.append(ARGUMENT_ASSIGNER_CHAR);
//		builder.append(filter.isShowOnlyDeletes());
//		builder.append(ARGUMENT_SPACER_CHAR);
//		
//		builder.append(HIDE_DELETES);
//		builder.append(ARGUMENT_ASSIGNER_CHAR);
//		builder.append(filter.getHideDeletes());
//		builder.append(ARGUMENT_SPACER_CHAR);
//	
//		builder.append(SHOW_END_RESULT_ONLY);
//		builder.append(ARGUMENT_ASSIGNER_CHAR);
//		builder.append(filter.isShowOnlyEndResult());
//		builder.append(ARGUMENT_SPACER_CHAR);
//		
//		builder.append(GROUPING_TO_SHOW);
//		builder.append(ARGUMENT_ASSIGNER_CHAR);
//		builder.append(filter.getRelevantBlockType());
//		builder.append(ARGUMENT_SPACER_CHAR);
//		
//		builder.append(SESSION_ID);
//		builder.append(ARGUMENT_ASSIGNER_CHAR);
//		builder.append(sessionId);
//		builder.append(ARGUMENT_SPACER_CHAR);
//		
//		builder.append(END_ARGUMENT_BLOCK_CHAR);
//		
//		return builder.toString();
//	}
//		
//	
//	public void getEventsFromServer() 	//XXX Is this ever used?  If so, what for?
//	{
//		try
//		{
//			sendFilterToServer();
//			setEventsToRender(getAllEventsInGroups());
//		}
//		catch (Exception e)
//		{
//			logger.fatal("",e);
//		}
//		
//	}
//	public void playbackAllEvents()
//	{
//		setCurrentNode(null);
//		
//		//go through all the events in the playback one at a time
//		while(playForward(1))
//		{
//			//do nothing, events are being added to the playback
//		}
//	}
//	
//
//	/**
//	 * A convenience method used to create a playbackViewer and play through all the events up and through the newest node.
//	 * @return
//	 */
//	public static MockPlaybackViewer playBackAll(String pathToServer) 
//	{
//		//create a simple test playbackViewer
//		MockPlaybackViewer playbackViewer = new MockPlaybackViewer(pathToServer);
//		
//		//ask the playbackViewer to go get all events from the server
//		List < JSONObject > allEvents = playbackViewer.getEventsFromPlaybackServerUpThroughLastNode();
//		
//		//tell the playbackViewer to render those events
//		playbackViewer.setEventsToRender(allEvents);
//		
//		playbackViewer.playbackAllEvents();
//		
//		return playbackViewer;
//	}
//	
//	/**
//	 * A convenience method used to create a playbackViewer and play through all the events up and through the node of the ID passed in.
//	 * @return
//	 */
//	public static MockPlaybackViewer playBackToNode(String pathToServer, String nodeID)
//	{
//		//create a simple test playbackViewer
//		MockPlaybackViewer playbackViewer = new MockPlaybackViewer(pathToServer);
//
//		//ask the playbackViewer to go get all events from the server
//		List < JSONObject > allEvents = playbackViewer.getEventsFromPlaybackServerUpThroughNodeId(nodeID);
//
//		//
//		//tell the playbackViewer to render those events
//		playbackViewer.setEventsToRender(allEvents);
//
//		
//		playbackViewer.playbackAllEvents();
//		
//		
//		return playbackViewer;
//	}
//	
//	/**
//	 * Returns a playbackViewer with a filter set to all the defaults for the last node, but nothing has been played.
//	 * @param pathToServer
//	 * @return
//	 */
//	public static MockPlaybackViewer prepForFilteringAllEvents(String pathToServer)
//	{
//		try
//		{
//			//create a simple test playbackViewer
//			MockPlaybackViewer playbackViewer = new MockPlaybackViewer(pathToServer);
//
//			//Handshake part 1 : Ask for/Get projectData
//			JSONObject projectData = playbackViewer.getNodesAndSession(getDevEmail());
//			JSONArray jarr = projectData.getJSONArray(NODE_TREE);		
//			String sessionId = projectData.getString(SESSION_ID);
//
//			String lastNodeId = jarr.getJSONObject(jarr.length()-1).getString(ID);		//choose the last created node
//
//			//Handshake part 2 : Ask for/Get Filter Settings			
//			JSONObject filterData = playbackViewer.getFilterDataForNode(sessionId,lastNodeId );
//
//			//create a filter with the defaults
//			//PlaybackFilter filter = playbackViewer.parseFilterDataToPlaybackFilter(filterData, false, false, 0, false, PlaybackFilter.CHARACTER_BLOCK_TYPE);
//			PlaybackFilter filter = null;
//
//			playbackViewer.setFilter(filter);
//			
//			playbackViewer.setSessionId(sessionId);
//
//			return playbackViewer;
//		}
//		catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//
//	/**
//	 * Returns a map of any chunks that got pasted.  The UUID of the first event are mapped to where the chunk currently is
//	 * , or -1 if they were deleted, and the original location (if that is still around)
//	 * 
//	 * Ex. 
//	 * Hi my name is mark.
//	 * I hope that Spain's shots are on mark.
//	 * 
//	 * The first mark was copied and pasted for the second mark, so the return will be a map with a key of the ID of the 2nd 'm' and
//	 * and JSONarray of one string "mark" and two integers 50 ,15 because the pasted mark is at 50 and the original mark is currently at 15. 
//	 *  If either were deleted, then the corresponding integer would be -1
//	 * 
//	 * If anyone else has a better way to do this, feel free to update the method.
//	 * 
//	 * @param docIdToInspect 
//	 * 
//	 * @return
//	 */
//	public Map<String, JSONArray> getPastedStrings(String docIdToInspect)
//	{
//		Map<String, JSONArray> result = new HashMap<String, JSONArray>();
//		Set<String> keySet=pastedInsertEventsPreviousNeighborToIDMap.keySet();
//		Collection<String> values = pastedInsertEventsPreviousNeighborToIDMap.values();
//		Iterator<String> iterator = keySet.iterator();
//		//Iterate through all the pasted text events by their previous neighbors
//		//If the previous neighbor is not in the keyset, it must be the beginning of a block
//		while (iterator.hasNext())
//		{
//			String prevNeighbor = iterator.next();
//			//if the event that this text event is supposed to follow is not in the values, it must be an event external to this map
//			//Therefore marking the beginning of a chunk
//			if (!values.contains(prevNeighbor))
//			{
//				StringBuilder builder = new StringBuilder();  //to build this "chunk" into a string
//
//				String thisId =pastedInsertEventsPreviousNeighborToIDMap.get(prevNeighbor);
//
//				//pull out the JSON object with the pertinent information
//				JSONObject jobj = pastedInsertEventsIDtoJSONMap.get(thisId);
//
//				try
//				{
//					String thisChar = jobj.getString(EVENT_DATA);
//					
//					String pasteParentId = jobj.getString(PASTE_PARENT_ID);
//					
//					//pull out the correct document that this pasted event is in
//					String docId = jobj.getString(DOCUMENT_ID);
//					if (!docId.equals(docIdToInspect))
//					{
//						continue;		//skip this if it's not in the document we are looking at
//					}
//					//get the current location of the start of this block
//					Integer currentIndex = getDocumentSetToRender().getDocumentBuffers().get(docId).getIndexOfInsertEvent(thisId);		//return this
//					Integer currentIndexOfPasteParent = getDocumentSetToRender().getDocumentBuffers().get(docId).getIndexOfInsertEvent(pasteParentId); //return this too
//					String keyUUID = thisId;		//return this three
//					
//					builder.append(thisChar);
//
//					//continue the chain until no event is "waiting" for the last event played.  This signifies the end of the chunk
//					while(keySet.contains(thisId))
//					{
//						//slide over one event
//						prevNeighbor=thisId;
//						//pull the JSON object out
//						thisId =pastedInsertEventsPreviousNeighborToIDMap.get(prevNeighbor);
//						jobj = pastedInsertEventsIDtoJSONMap.get(thisId);
//						thisChar = jobj.getString(EVENT_DATA);
//						builder.append(thisChar);
//					}
//					//and thus, we have a string, the current location, and the originIndex
//					JSONArray jarray = new JSONArray();
//					jarray.put(builder.toString());
//					jarray.put(currentIndex);
//					jarray.put(currentIndexOfPasteParent);
//					result.put(keyUUID, jarray);
//				}
//				catch (JSONException e) 
//				{
//					logger.error("Pasted string malformatted?",e);
//				}
//			}
//		}
//		return result;
//	}
//
//
//
//
//	public void branchAtId(String eventId, String devGroupId)
//	{
//		StringBuilder request = new StringBuilder();
//		request.append("POST/ ");
//		request.append(PlaybackProxy.POST_ID_TO_BRANCH_AT);
//		request.append(PlaybackProxy.START_ARGUMENT_BLOCK_CHAR);
//		request.append(JSONConstants.DEVELOPER_GROUP_ID);
//		request.append(PlaybackProxy.ARGUMENT_ASSIGNER_CHAR);
//		request.append(devGroupId);
//		request.append(PlaybackProxy.ARGUMENT_SPACER_CHAR);
//		request.append(JSONConstants.ID);
//		request.append(PlaybackProxy.ARGUMENT_ASSIGNER_CHAR);
//		request.append(eventId);
//		request.append(PlaybackProxy.END_ARGUMENT_BLOCK_CHAR);
//		request.append("\n");
//		
//		createSocketToPlaybackProxy();
//		
//		try
//		{
//			outToPlaybackProxy.writeBytes(request.toString());
//			outToPlaybackProxy.writeBytes("\n");
//		}
//		catch (IOException e)
//		{
//			logger.fatal("Problem with the connection while branching",e);
//		}
//		logger.trace("Branching Request: "+request.toString()+" sent");		//TODO maybe make an acknowledgment here.
//		
//	}
//	
//	public void branchAtEndOfNode(String firstNodeId, String devGroupId)
//	{
//		branchAtId(firstNodeId, devGroupId);
//		
//	}
//
//	public void setFilter(PlaybackFilter filter)
//	{
//		this.filter = filter;
//	}
//
//	public PlaybackFilter getFilter()
//	{
//		return filter;
//	}
//
//
//
//	/**
//	 * Adds a key-value pair base to the JSONObject based on if this event is relevant (according to the setFilter)
//	 * @param eventObject
//	 * @throws JSONException 
//	 */
//	private void filter(JSONObject eventObject) throws JSONException
//	{
//		//String response = filter.analyze(eventObject);
//		//eventObject.put(PLAYBACK_RELEVANCE, response);
//	}
//
//
//
//	@Override
//	public String toString()
//	{
//		StringBuilder builder = new StringBuilder();
//		builder.append("MockPlaybackViewer [filter=");
//		builder.append(filter);
//		builder.append(", getDocumentSetToRender()=");
//		builder.append(getDocumentSetToRender());
//		builder.append("]");
//		return builder.toString();
//	}
//
//
//
//
//
//
//
//
//}