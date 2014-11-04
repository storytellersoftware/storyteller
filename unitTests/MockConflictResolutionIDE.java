//package unitTests;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.naming.OperationNotSupportedException;
//
//import org.apache.log4j.Logger;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import StorytellerServer.Utilities;
//import StorytellerServer.json.JSONConstants;
//
//public class MockConflictResolutionIDE extends MockIDE
//{
//	private static Logger logger = Logger.getLogger(MockConflictResolutionIDE.class.getName());
//	
//	private List<JSONObject> capturedEvents = new ArrayList<JSONObject>();
//
//	//private int mergeProxyPort = MergeProxy.MERGE_IDE_PROXY_PORT;
//
//	//Allow us to talk to the merge proxy.
//	//private Socket socketToMergeProxy;
//	//private DataOutputStream outToMergeProxy;
//	//private BufferedReader inFromMergeProxy;
//	
//	/**
//	 * Creates a MockConflictResolutionIDE that will be "operated" by the passed in developerID
//	 * and talk to the server in pathToServer.
//	 * @param pathToServer
//	 * @param developerId
//	 */
//	public MockConflictResolutionIDE()
//	{
//		super(null);
//		//the server will be using the DevGroupID of the mergeSession, not the one from each event, so this devGroupId will be disregarded.
//		setCurrentDevGroupID(null);
//	//	createSocketToMergeProxy();
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
////	
//	
//	
//	/**
//	 * Rather than sending the jsonObject to the server, we hold onto it for a while
//	 */
//	@Override
//	protected void sendJSONObject(JSONObject jsonObject) throws IOException
//	{
//		logger.trace("MockConflictResolutionIDE recieved event jsonObject");
//		capturedEvents.add(jsonObject);
//	
//	}
//	
//	/**
//	 * A MockConflictResolutionIDE does not support creating playbacks based on selected text.
//	 */
//	@Override
//	public String createSelectedPlayback(String selectedText, int occurrence) throws JSONException, IOException
//	{
//		//Selected playback is not supported (or at least shouldn't be supported) while resolving a manual conflict
//		throw new RuntimeException(new OperationNotSupportedException("You cannot make a selected playback while resolving a manual conflict"));
//	}
//	
//	/**
//	 * A MockConflictResolutionIDE has zero or one editable documents- the user cannot change which one to edit.
//	 */
//	@Override
//	public void setCurrentDocumentId(String currentDocumentId)
//	{
//		//You can't set the currentDocumentId after the MockConflictResolutionIDE has been created
//		throw new RuntimeException("There can only be one documentId handled by the MockConflictResolutionIDE and that is currently " +getCurrentDocumentId());
//	}
//	
//	public JSONArray getAllEventsTyped()
//	{
//		return Utilities.listOfJsonObjectsToJsonArray(capturedEvents);
//	}
//
//	public void updateToState(JSONObject state) throws JSONException
//	{
//		super.setCurrentDocumentId(state.getString(JSONConstants.DOCUMENT_ID));
//		documentBuffers.put(getCurrentDocumentId(), new StringBuilder(state.getString(JSONConstants.MERGE_STATE_STRING)));
//	}
//
//	
////	/**
////	 * The Merge state should be a JSONObject that has two parts
////	 * Document ID - the DocumentId of the document
////	 * Merge State String - the String representation of the document
////	 */
////	public void getMergeStateFromMergeProxy()
////	{
////		//Erase any old document left overs
////		documentBuffers.clear();
////		try
////		{
////			String mergeState = inFromMergeProxy.readLine();
////			JSONObject jObject = new JSONObject(mergeState);
////			super.setCurrentDocumentId(jObject.getString(JSONConstants.DOCUMENT_ID));
////			documentBuffers.put(getCurrentDocumentId(), new StringBuilder(jObject.getString(JSONConstants.MERGE_STATE_STRING)));
////		} 
////		catch (Exception e)
////		{
////			logger.fatal("Error in getMergeStateFromMergeProxy",e);
////		}		
////	}
//	
////	/**
////	 * Takes whatever events have been built up thus far and sends them to the MergeProxy.
////	 */
////	public void flushCapturedEventsToMergeProxy()
////	{
////		try
////		{
////			JSONArray jarrCapturedEvents = Utilities.listOfJsonObjectsToJsonArray(capturedEvents);
////			logger.debug("writing to outToMergeProxy "+jarrCapturedEvents.toString());
////			outToMergeProxy.writeBytes(jarrCapturedEvents.toString()+"\n");
////			//wait for the ack before continuing
////			String ack = inFromMergeProxy.readLine();
////			logger.trace("received ack "+ack);
////			//Once those events have hit the MergeProxy, reset for the next conflict resolution.
////			capturedEvents.clear();
////		} 
////		catch (Exception e)
////		{
////			logger.fatal("Error in flushCapturedEventsToMergeProxy()",e);
////		}	
////	}
//	
////	/**
////	 * Should be called when the MockConflictResolutionIDE is no longer needed.
////	 */
////	public void doneWithMerging()
////	{
////		closeSocketToMergeProxy();
////	}
//	
//	
//}
