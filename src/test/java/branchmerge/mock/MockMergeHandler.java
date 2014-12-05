package branchmerge.mock;
//package Playback;
//
//import static StorytellerServer.json.JSONConstants.*;
//import static StorytellerServer.playback.PlaybackProxy.*;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
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
//import unitTests.MockConflictResolutionIDE;
//import StorytellerServer.Utilities;
//import StorytellerServer.json.JSONConstants;
//import StorytellerServer.playback.PlaybackProxy;
//
///**
// * This class handles merging for unit tests.  Most of the thinking is abstracted away and test cases simply call
// * handleAutomaticConflict() or handleManualConflict() depending on which type is expected.  
// * 
// * The choice to explicitly handle manual and automatic was made because this class is used in unit tests and merging
// * should follow a well defined, reproducible script.
// * 
// * There are three document sets that this MockMergeHandler keeps track of:
// * 	-First Developer, keeps the documents as the First Developer saw them (minus any conflicts that his side did not win)
// * 	-Second Developer, keeps the documents as the Second Developer saw them (minus any conflicts that her side did not win)
// * 	-Combined State, keeps the end result, with all accepted conflicts and custom resolutions (if any)
// * 
// * @author Kevin
// *
// */
//public class MockMergeHandler extends PlaybackEventRenderer
//{
//	public static final int FIRST_DEVELOPER = 0, SECOND_DEVELOPER = 1, COMBINED_STATE = 2;
//	private static String devFirstName = null;
//
//	private static String devLastName = null;
//
//	private static String devEmailAddress = null;
//
//	private PlaybackDocumentRenderer firstDeveloperDocumentState;
//	private PlaybackDocumentRenderer secondDeveloperDocumentState;
//
//	//This is what the product of the merging will look like
//	private PlaybackDocumentRenderer combinedDocumentState;
//
//	private String developerGroupId;
//
//	private boolean isMerging = false;
//
//	private Logger logger = Logger.getLogger(getClass().getName());
//	private Logger timer = Logger.getLogger("timing."+getClass().getName());
//
//	//These hold info on the MergeBlock that is currently being handled.
//	private String idOfFirstBlock;
//
//	private String idOfSecondBlock;
//
//	private String winningId;
//
//	//private int mergeProxyPort = MergeProxy.MERGE_BROWSER_PROXY_PORT;
//
//	//Allow us to talk to the merge proxy.
//	//private Socket socketToMergeProxy;
//	//private DataOutputStream outToMergeProxy;
//	//private BufferedReader inFromMergeProxy;
//	
//	private MockConflictResolutionIDE mcrIDE = null;
//
//	/**
//	 * It is not recommended to call the constructor.  Instead, use
//	 * getMergeHandlerUpThroughFirstConflict()
//	 */
//	public MockMergeHandler()
//	{
//		super("127.0.0.1");
//		mcrIDE = new MockConflictResolutionIDE();
//	}
//
//	public static String getDevFirstName()
//	{
//		return devFirstName;
//	}
//	
//	public static void setDevFirstName(String devFirstName)
//	{
//		MockMergeHandler.devFirstName = devFirstName;
//	}
//	
//	public static String getDevLastName()
//	{
//		return devLastName;
//	}
//	
//	public static void setDevLastName(String devLastName)
//	{
//		MockMergeHandler.devLastName = devLastName;
//	}
//	
//	public static void setDevEmailAddress(String devEmailAddress)
//	{
//		MockMergeHandler.devEmailAddress = devEmailAddress;
//	}
//	
//	public static String getDevEmail()
//	{
//		return MockMergeHandler.devEmailAddress;
//	}
//	
//	/**
//	 * Switches between which state should be viewed or edited.
//	 * 
//	 * There are three document sets that this MockMergeHandler keeps track of:
//	 * 	-PlaybackEventRenderer.FIRST_DEVELOPER, keeps the documents as the First Developer saw them (minus any conflicts that his side did not win)
//	 * 	-PlaybackEventRenderer.SECOND_DEVELOPER, keeps the documents as the Second Developer saw them (minus any conflicts that her side did not win)
//	 * 	-PlaybackEventRenderer.COMBINED_STATE, keeps the end result, with all accepted conflicts and custom resolutions (if any)
//	 * 
//	 * @param whoseState
//	 */
//	public void setDocumentsToRender(int whoseState)
//	{
//		switch (whoseState)
//		{
//		case FIRST_DEVELOPER:
//			setDocumentSetToRender(firstDeveloperDocumentState);
//			break;
//		case SECOND_DEVELOPER:
//			setDocumentSetToRender(secondDeveloperDocumentState);
//			break;
//		case COMBINED_STATE:
//		default:
//			setDocumentSetToRender(combinedDocumentState);
//			break;
//		}
//	}
//
//	public MockConflictResolutionIDE getMcrIDE()
//	{
//		return mcrIDE;
//	}
//
//	/**
//	 * Creates a merge handler that is ready to handle the first conflict.  
//	 * 
//	 * This version of the call will not allow a MockConflictResolutionIDE to connect to the MergeProxy
//	 * @param firstNodeId
//	 * @param secondNodeId
//	 * @param developerGroupId
//	 * @return
//	 * @throws IOException
//	 * @throws JSONException
//	 */
//	public static MockMergeHandler getMergeHandlerUpThroughFirstConflictNoCustomResolutions(String firstNodeId, String secondNodeId, String developerGroupId) throws IOException, JSONException
//	{
//		MockMergeHandler handler = new MockMergeHandler();
//
//		JSONObject mergeSessionData = handler.getMergeSession();
//		handler.setSessionId(mergeSessionData.getString(SESSION_ID));
//		
//		handler.logger.debug("Merge Session Id recieved was "+handler.getSessionId());
//
//		handler.getAndSetCommonAncestorState(firstNodeId, secondNodeId, developerGroupId);
//
////		//Now the MergeProxy should be up, so, we should connect to it.
////		handler.createSocketToMergeProxy();
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		
//		return handler;
//	}
//	
//	/**
//	 * Creates a merge handler that is ready to handle the first conflict.  
//	 * 
//	 * This method REQUIRES a MockConflictResolutionIDE to be created before the resolution of the last conflict, to connect to the MergeProxy so that
//	 * this handler can kill the MergeProxy, remotely.  TODO possible alternative would be for the StorytellerServer to kill the MergeProxy
//	 * when the MergeSession terminates.
//	 * 
//	 * @param firstNodeId
//	 * @param secondNodeId
//	 * @param developerGroupId
//	 * @return
//	 */
//	public static MockMergeHandler getMergeHandlerUpThroughFirstConflictPreppedForCustomResolutions(String firstNodeId, String secondNodeId, String developerGroupId) throws IOException, JSONException
//	{
//		MockMergeHandler handler = new MockMergeHandler();
//
//		JSONObject mergeSessionData = handler.getMergeSession();
//		handler.setSessionId(mergeSessionData.getString(SESSION_ID));
//		
//		handler.logger.debug("Merge Session Id recieved was "+handler.getSessionId());
//
//		handler.getAndSetCommonAncestorState(firstNodeId, secondNodeId, developerGroupId);
//
//		
//		handler.setDocumentsToRender(COMBINED_STATE);
//		
//		return handler;
//	}
//
//	/**
//	 * Starts the MergeSession and gets the state of the youngest common ancestor.  Updates the 
//	 * trio of PlaybackRenderers to that state.
//	 * @param firstNodeId
//	 * @param secondNodeId
//	 * @param developerGroupId
//	 */
//	private void getAndSetCommonAncestorState(String firstNodeId, String secondNodeId, String developerGroupId) throws IOException, JSONException
//	{
//		isMerging = true;
//		JSONObject state = startMergeSession(firstNodeId, secondNodeId, developerGroupId);
//
//		firstDeveloperDocumentState = new PlaybackDocumentRenderer(state);
//		secondDeveloperDocumentState = new PlaybackDocumentRenderer(state);
//		combinedDocumentState  = new PlaybackDocumentRenderer(state);
//
//
//	}
//
////	/**
////	 * Makes a connection to the MergeProxy on the set Port and path
////	 */
////	private void createSocketToMergeProxy()
////	{
////		try
////		{
////			//create a socket connection with a merge proxy server so we can talk to the ide for custom resolutions.
////			socketToMergeProxy = new Socket(pathToServer, mergeProxyPort);
////
////			//we can use outToPlaybackProxy to write to the socket
////			outToMergeProxy = new DataOutputStream(socketToMergeProxy.getOutputStream());
////
////			//we can use inFromPlayback to read in to the socket
////			inFromMergeProxy = new BufferedReader(new InputStreamReader(socketToMergeProxy.getInputStream()));
////		}
////		catch(IOException ex)
////		{
////			logger.fatal("",ex);
////		}
////	}
//
////	/**
////	 * Closes the socket (Freeing it up for other classes to use) 
////	 */
////	private void closeSocketToMergeProxy()
////	{
////		try
////		{
////			//close the socket after we are done with it
////			socketToMergeProxy.close();
////		}
////		catch(IOException ex)
////		{
////			logger.fatal("",ex);
////		}
////	}
//
//
//
//	/**
//	 * Part 1 of the Merge Handshake
//	 * 
//	 * @return
//	 * @throws IOException
//	 * @throws JSONException
//	 */
//	private JSONObject getMergeSession() throws IOException, JSONException
//	{
//		createSocketToPlaybackProxy();
//		String httpMessage = "GET /" +PlaybackProxy.GET_MERGE_SESSION + START_ARGUMENT_BLOCK_CHAR+
//				EMAIL+ARGUMENT_ASSIGNER_CHAR+getDevEmail()+END_ARGUMENT_BLOCK_CHAR;
//
//		//write an http request on the socket			
//		outToPlaybackProxy.writeBytes(httpMessage +"\n");
//		timer.trace("mergeHandler requested MergeSession" + httpMessage);
//		outToPlaybackProxy.writeBytes("\n");
//		logger.trace("mergeHandler requested MergeSession" + httpMessage);
//
//		//wait for a response from the playback server
//		inFromPlaybackProxy.readLine();    						  //the header, which we promptly ignore
//		inFromPlaybackProxy.readLine();							 //blank line
//		inFromPlaybackProxy.readLine();							 // text/text
//		String projectData = inFromPlaybackProxy.readLine();
//		timer.trace("mergeHandler received a MergeSession" + httpMessage);
//		logger.trace("mergeHandler received some ProjectData "+projectData);
//
//		JSONObject jobj = new JSONObject(projectData);
//
//
//		return jobj;
//	}
//
//	/**
//	 * Part 2 of the merging handshake
//	 * @param firstNodeId
//	 * @param secondNodeId
//	 * @param developerGroupId
//	 * @return
//	 * @throws IOException
//	 * @throws JSONException
//	 */
//	private JSONObject startMergeSession(String firstNodeId, String secondNodeId, String developerGroupId) throws IOException, JSONException
//	{
//		this.developerGroupId=developerGroupId;
//		createSocketToPlaybackProxy();
//		String httpMessage =START_MERGE_SESSION+START_ARGUMENT_BLOCK_CHAR+FIRST_NODE+ARGUMENT_ASSIGNER_CHAR+firstNodeId+ARGUMENT_SPACER_CHAR+
//				SECOND_NODE+ARGUMENT_ASSIGNER_CHAR+secondNodeId+ARGUMENT_SPACER_CHAR+
//				DEVELOPER_GROUP_ID+ARGUMENT_ASSIGNER_CHAR+developerGroupId+ARGUMENT_SPACER_CHAR+
//				SESSION_ID+ARGUMENT_ASSIGNER_CHAR+getSessionId()+END_ARGUMENT_BLOCK_CHAR;
//
//		//write an http request on the socket			
//		outToPlaybackProxy.writeBytes(httpMessage +"\n");
//		timer.trace("mergeHandler started MergeSession" + httpMessage);
//		outToPlaybackProxy.writeBytes("\n");
//		logger.trace("mergeHandler started MergeSession" + httpMessage);
//
//		//wait for a response from the playback server
//		inFromPlaybackProxy.readLine();    						  //the header, which we promptly ignore
//		inFromPlaybackProxy.readLine();							 //blank line
//		inFromPlaybackProxy.readLine();							 // text/text
//		String stateData = inFromPlaybackProxy.readLine();
//		timer.trace("mergeHandler started MergeSession and recieved ack (plus state)");
//		logger.trace("mergeHandler started MergeSession" + stateData);
//		JSONObject jobj = new JSONObject(stateData);
//		return jobj;
//	}
//
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
//			JSONArray nodeArray = getMergeSession().getJSONArray(NODE_TREE);
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
//	 * Sets isMerging to be false and tells the MergeProxy that we are so totally done with this.
//	 */
//	private void setDoneWithMerging()
//	{
//		isMerging = false;
//	}
//
//
//	/**
//	 * Bread and butter of the merge handler.  Gets an merge block and then passes it to be handled.
//	 * @return
//	 * @throws IOException
//	 * @throws JSONException
//	 */
//	private JSONObject getNextConflict() throws IOException, JSONException
//	{
//		createSocketToPlaybackProxy();
//		String httpMessage =GET_MERGE_CONFLICT+START_ARGUMENT_BLOCK_CHAR+
//				DEVELOPER_GROUP_ID+ARGUMENT_ASSIGNER_CHAR+developerGroupId+ARGUMENT_SPACER_CHAR+
//				SESSION_ID+ARGUMENT_ASSIGNER_CHAR+getSessionId()+END_ARGUMENT_BLOCK_CHAR;
//
//		//write an http request on the socket			
//		outToPlaybackProxy.writeBytes(httpMessage +"\n");
//		timer.trace("mergeHandler requested next conflict" + httpMessage);
//		outToPlaybackProxy.writeBytes("\n");
//		logger.trace("mergeHandler requested next conflict" + httpMessage);
//
//		//wait for a response from the playback server
//		inFromPlaybackProxy.readLine();    						  //the header, which we promptly ignore
//		inFromPlaybackProxy.readLine();							 //blank line
//		inFromPlaybackProxy.readLine();							 // text/text
//		String conflict = inFromPlaybackProxy.readLine();
//		timer.trace("mergeHandler received conflict" + conflict);
//		logger.trace("mergeHandler received conflict" + conflict);
//		JSONObject jobj = new JSONObject(conflict);
//		if (jobj.has(IS_LAST))
//		{
//			setDoneWithMerging();			// we are done with the merging
//		}
//		return jobj;
//	}
//
//	/**
//	 * Follows the protocol to send a conflict resolution for a mergeBlock to the server.
//	 * @param idOfFirstBlock
//	 * @param idOfSecondBlock
//	 * @param winningId
//	 * @param customResolution
//	 * @return
//	 */
//	private JSONArray postConflictResolution(String idOfFirstBlock, String idOfSecondBlock, String winningId, JSONArray customResolution) throws IOException, JSONException
//	{
//		createSocketToPlaybackProxy();
//		String httpMessage = POST_CONFLICT_RESOLUTION+START_ARGUMENT_BLOCK_CHAR+
//				DEVELOPER_GROUP_ID+ARGUMENT_ASSIGNER_CHAR+developerGroupId+ARGUMENT_SPACER_CHAR+
//				ID+ARGUMENT_ASSIGNER_CHAR+idOfFirstBlock+ARGUMENT_SPACER_CHAR+
//				PARTNER_ID+ARGUMENT_ASSIGNER_CHAR+idOfSecondBlock+ARGUMENT_SPACER_CHAR+
//				WINNER_ID+ARGUMENT_ASSIGNER_CHAR+winningId+ARGUMENT_SPACER_CHAR;
//		//For whatever reason, it seems that JSONArrays do not like being instantiated if they are empty.  So, null customResolutions will just be ommited 
//		//from the request.
//		if (customResolution!=null)
//		{
//			httpMessage+=CUSTOM_RESOLUTION_EVENTS+ARGUMENT_ASSIGNER_CHAR+customResolution.toString()+ARGUMENT_SPACER_CHAR;
//		}
//		httpMessage+=SESSION_ID+ARGUMENT_ASSIGNER_CHAR+getSessionId()+END_ARGUMENT_BLOCK_CHAR;
//
//		//write an http request on the socket			
//		outToPlaybackProxy.writeBytes(httpMessage +"\n");
//		timer.trace("mergeHandler posted conflict resolution" + httpMessage);
//		outToPlaybackProxy.writeBytes("\n");
//		logger.trace("mergeHandler posted conflict resolution" + httpMessage);
//
//
//		inFromPlaybackProxy.readLine();    						  //the header, which we promptly ignore
//		inFromPlaybackProxy.readLine();							 //blank line
//		inFromPlaybackProxy.readLine();							 // text/text
//		String ack = inFromPlaybackProxy.readLine();
//		timer.trace("mergeHandler recieved ack from post" + httpMessage);
//		logger.debug("mergeHandler recieved ack from post" + httpMessage);
//		JSONObject jobj = new JSONObject(ack);
//		if (jobj.has(IS_LAST))
//		{
//			setDoneWithMerging();		//we are done with merging
//		}
//		if (customResolution!=null)
//		{
//			return new JSONArray(jobj.getString(JSONConstants.PLAYBACK_EVENTS_OF_CUSTOM_RESOLUTION));
//		}
//		return null;
//	}
//
//
//
//	/**
//	 * This is a convenience method to sort the paperwork and handle the automatic conflict
//	 */
//	public void handleAutomaticConflict()
//	{
//		try
//		{
//			JSONObject conflict = getNextConflict();
//			if (conflict.getBoolean(IS_MANUAL_CONFLICT))
//			{
//				throw new RuntimeException("Expected an Automatic Conflict, but got a manual conflict instead");
//			}
//			logger.debug("This is the conflict "+ conflict);
//			JSONArray eventsInConflict = conflict.getJSONArray(EVENTS_IN_FIRST_CONFLICT_BLOCK);
//			int owner = conflict.getInt(FROM_LIST);
//
//			setCurrentEventIndex(0);
//			setEventsToRender(Utilities.jsonArrayOfJsonObjectsToList(eventsInConflict));
//			//Render the automatic conflict in the appropriate developer
//			if (owner == 1)
//			{
//				setDocumentsToRender(FIRST_DEVELOPER);
//			}
//			else
//			{
//				setDocumentsToRender(SECOND_DEVELOPER);
//			}
//			playForward(eventsInConflict.length());
//
//			setCurrentEventIndex(0);
//			setDocumentsToRender(COMBINED_STATE);		//also render it in the "combined" state to show what everything will look like after merging
//			playForward(eventsInConflict.length());	
//		} 
//		catch (Exception e)
//		{
//			throw new RuntimeException(e);
//		} 
//
//
//	}
//	
//	/**
//	 * Returns if there are more conflicts yet to handle.
//	 * @return
//	 */
//	public boolean isMerging()
//	{
//		return isMerging;
//	}
//
//	/**
//	 * This is a convenience method to sort the paperwork and handle the manual conflict by simply saying
//	 * which of the two branches (or 0 if none) should be used to resolve the conflict.
//	 * @param whoWon
//	 */
//	public void handleManualConflictCompletely(int whoWon)
//	{
//		//Handle the conflict without involving the IDE
//		handleManualConflictPartOne(whoWon,false);
//		//Post the conflict without waiting for the IDE
//		handleManualConflictPartTwo(false);
//
//	}
//
//
//	/**
//	 * Handles the first part of a merge and then sends the state of the document affected (if any)
//	 * to the MergeProxy (who hands it off to the IDE) so that we can get a custom resolution.
//	 * @param whoWon
//	 */
//	public void handleManualConflictPartOne(int whoWon)
//	{
//		//If the user is calling in partOne manually, they want to wait for the IDE's response.
//		handleManualConflictPartOne(whoWon,true);
//		
//	}
//
//	/**
//	 * Handles the first part of a merge based on the passed in "winner" 
//	 * @param whoWon
//	 * @param isTestExpectingCustomResolution should do Custom Resolution
//	 * @return
//	 */
//	private void handleManualConflictPartOne(int whoWon, boolean isTestExpectingCustomResolution)
//	{
//		try
//		{
//			JSONObject conflict = getNextConflict();
//			if (!conflict.getBoolean(IS_MANUAL_CONFLICT))
//			{
//				throw new RuntimeException("Expected a Manual Conflict, but got an Automatic conflict instead");
//			}
//			logger.debug(conflict);
//			JSONArray eventsInFirstConflict = conflict.getJSONArray(EVENTS_IN_FIRST_CONFLICT_BLOCK);
//			JSONArray eventsInSecondConflict = conflict.getJSONArray(EVENTS_IN_SECOND_CONFLICT_BLOCK);
//
//			idOfFirstBlock = conflict.getString(ID);
//			idOfSecondBlock = conflict.getString(PARTNER_ID);
//			winningId = null;
//
//			//Render the automatic conflict in the appropriate developer
//			if (whoWon == 1)
//			{
//				setCurrentEventIndex(0);
//				setEventsToRender(Utilities.jsonArrayOfJsonObjectsToList(eventsInFirstConflict));
//				setDocumentsToRender(FIRST_DEVELOPER);
//				playForward(eventsInFirstConflict.length());
//				winningId = idOfFirstBlock;
//
//			}
//			else if (whoWon == 2)
//			{
//				setCurrentEventIndex(0);
//				setEventsToRender(Utilities.jsonArrayOfJsonObjectsToList(eventsInSecondConflict));
//				setDocumentsToRender(SECOND_DEVELOPER);
//				playForward(eventsInSecondConflict.length());
//				winningId = idOfSecondBlock;
//
//			}
//			else 
//			{
//				//Nobody won.  render nothing
//				setCurrentEventIndex(0);
//				setEventsToRender(new ArrayList<JSONObject>());		//nothing to render to the conflict
//			}
//			
//			//also render it in the "combined" state to show what everything will look like after merging
//			setCurrentEventIndex(0);
//			setDocumentsToRender(COMBINED_STATE);		
//			playForward(getEventsToRender().size());
//			
//			if (isTestExpectingCustomResolution)
//			{
//				updateMockConflictResolutionIDWithStateOfDocument(eventsInFirstConflict.getJSONObject(0),eventsInSecondConflict.getJSONObject(0));
//			}
//		}
//		catch (Exception e)
//		{
//			throw new RuntimeException(e);
//		}
//		
//	}
//	
//	/**
//	 * If only one document was affected, send the String representation of that document to the 
//	 * @param firstEventInFirstConflict
//	 * @param firstEventInSecondConflict
//	 * @throws JSONException 
//	 * @throws IOException 
//	 */
//	private void updateMockConflictResolutionIDWithStateOfDocument(JSONObject firstEventInFirstConflict, JSONObject firstEventInSecondConflict) throws JSONException, IOException
//	{
//		if (!(firstEventInFirstConflict.has(DOCUMENT_ID)&&firstEventInSecondConflict.has(DOCUMENT_ID)))
//		{
//			logger.error("at least one of the jsonObjects was missing a DocumentId");
//			return;		//This MergeBlock is not guaranteed to have effected just one DocumentId
//		}
//		if (!firstEventInFirstConflict.getString(DOCUMENT_ID).equals(firstEventInSecondConflict.getString(DOCUMENT_ID)))
//		{
//			logger.error("The documentIDs of the two blocks did not match.  Probably a rename conflict or something like that.");
//			return;		//This MergeBlock is not guaranteed to have effected just one DocumentId
//		}
//		String documentId = firstEventInFirstConflict.getString(DOCUMENT_ID);
//		String documentState = getDocumentText(documentId);
//		
//		JSONObject jobjToSend = new JSONObject();
//		jobjToSend.put(DOCUMENT_ID, documentId);
//		jobjToSend.put(MERGE_STATE_STRING, documentState);
//		
//		mcrIDE.updateToState(jobjToSend);
//		
//		//outToMergeProxy.writeBytes(jobjToSend.toString()+"\n");
//		
//	}
//
//	/**
//	 * Allows a user to manually resolve a conflict by waiting for the output of the IDE, which will be sent to the server and all buffers will be updated.
//	 */
//	public void handleManualConflictPartTwo()
//	{
//		handleManualConflictPartTwo(true);
//	}
//
//	/**
//	 * Allows a user to manually resolve a conflict by possibly waiting for the IDE.
//	 */
//	private void handleManualConflictPartTwo(boolean thereWillBeCustomEvents)
//	{
//		if (thereWillBeCustomEvents)
//		{
//			try
//			{
//				JSONArray customResolutionIDEEvents = mcrIDE.getAllEventsTyped();
//
//				JSONArray eventsYetToRender = postConflictResolution(idOfFirstBlock, idOfSecondBlock, winningId, customResolutionIDEEvents);
//
//				List<JSONObject> listOfExtraEvents = Utilities.jsonArrayOfJsonObjectsToList(eventsYetToRender);
//				logger.debug("In Handling Manual Conflict Part 2, the list Of Extra Events is "+listOfExtraEvents);
//				getEventsToRender().addAll(listOfExtraEvents);
//			} 
//			catch (Exception e)
//			{
//				throw new RuntimeException(e);
//			}
//		}
//		else
//		{
//			try
//			{
//				logger.debug("In Handling Manual Conflict Part 2, there were no Extra Events");
//				postConflictResolution(idOfFirstBlock, idOfSecondBlock, winningId, null);
//			}
//			catch (Exception e)
//			{
//				throw new RuntimeException(e);
//			}
//		}
//
//		//Render the rest of the events in the Combined State.  This may be zero if the user chose to do no events.
//		setDocumentsToRender(COMBINED_STATE);		//also render it in the "combined" state to show what everything will look like after merging
//		playForward(getEventsToRender().size());
//
//
//	}
//
////	private JSONArray getCustomResolutionFromMergeProxy() throws IOException, JSONException
////	{
////		String jarrString = inFromMergeProxy.readLine();
////		return new JSONArray(jarrString);
////	}
//
//
//}
