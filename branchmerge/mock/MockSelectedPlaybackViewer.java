package branchmerge.mock;
//package Playback;
//
//import static StorytellerServer.json.JSONConstants.ID;
//import static StorytellerServer.json.JSONConstants.SESSION_ID;
//
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.apache.log4j.Logger;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import StorytellerEntities.Events.DeleteEvent;
//import StorytellerEntities.Events.InsertEvent;
//import StorytellerServer.ide.DocumentBuffer;
//import StorytellerServer.json.JSONConstants;
//import StorytellerServer.playback.PlaybackProxy;
//
//public class MockSelectedPlaybackViewer extends MockPlaybackViewer 
//{
//	//for logging
//	private Logger logger = Logger.getLogger(getClass().getName());
//	@SuppressWarnings("unused")
//	private Logger timer = Logger.getLogger("timing."+getClass().getName());
//	
//	//holds the playback session id that the server is maintaining 
//	private String sessionId;
//	//a set of relevant event ids
//	private Set <String> relevantEventIds;
//	
//	public MockSelectedPlaybackViewer(String pathToServer) 
//	{
//		super(pathToServer);
//		relevantEventIds = new HashSet<String>();
//	}
//
//	public static MockSelectedPlaybackViewer playback(String pathToServer, String sessionId) throws JSONException, IOException
//	{
//		//create a playback viewer
//		MockSelectedPlaybackViewer mockSelectedPlaybackViewer = new MockSelectedPlaybackViewer(pathToServer);
//		mockSelectedPlaybackViewer.logger.info("In MockSelectedPlaybackViewer playback()");
//		//store the session id
//		mockSelectedPlaybackViewer.setSessionId(sessionId);
//		mockSelectedPlaybackViewer.getRelevantEventsFromServer();
//		mockSelectedPlaybackViewer.setEventsToRender(mockSelectedPlaybackViewer.getEventsFromPlaybackServerUpThroughLastNode());
//		mockSelectedPlaybackViewer.playbackAllEvents();
//		return mockSelectedPlaybackViewer;
//	}
//
//	public void getRelevantEventsFromServer() throws JSONException, IOException 
//	{
//		//build up an http string to send to the server that requests the relevant event ids
//		String httpMessage = "GET /" + PlaybackProxy.GET_SPECIAL_EVENT_IDS + PlaybackProxy.START_ARGUMENT_BLOCK_CHAR +
//		SESSION_ID + PlaybackProxy.ARGUMENT_ASSIGNER_CHAR + sessionId + PlaybackProxy.END_ARGUMENT_BLOCK_CHAR;
//		
//		//create a connection to the playback server
//		createSocketToPlaybackProxy();
//		outToPlaybackProxy.writeBytes(httpMessage +"\n");
//		outToPlaybackProxy.writeBytes("\n");
//		
//		//wait for a response
//		inFromPlaybackProxy.readLine();
//		inFromPlaybackProxy.readLine();							 //blank line
//		inFromPlaybackProxy.readLine();							 // application/json
//		String jsonFromServer = inFromPlaybackProxy.readLine();
//	
//		//create a json object and array of event ids
//		JSONObject jobj = new JSONObject(jsonFromServer);
//		JSONArray jsonArrayOfSpecialEventIds = jobj.getJSONArray(JSONConstants.EVENT_ID);
//		
//		//add the ids to the set of relevant events
//		for(int i = 0; i < jsonArrayOfSpecialEventIds.length(); i++)
//		{
//			getRelevantEventIds().add(jsonArrayOfSpecialEventIds.getString(i));
//		}
//	}
//
//	public boolean relevantEventsMatch(String matchString) 
//	{
//		//matches the passed in string with the computed relevant ids
//		return matchString.equals(getRelevantEventsAsHumanText());
//	}
//
//	/**
//	 * This method builds up a string of relevant events so that that it can be tested.
//	 * All events that are rendered (that are relevant in a selected text scenario)
//	 * will be added to the return string. If a rendered event is an insert it will
//	 * be preceeded with an 'I'. If the event is a delete it will contain just a 'D'  
//	 * @return
//	 */
//	public String getRelevantEventsAsHumanText() 
//	{
//		StringBuilder text = new StringBuilder();
//		try
//		{
//			//for each event to render
//			for(JSONObject jobj: getEventsToRender())
//			{
//				if (relevantEventIds.contains(jobj.getString(ID)))
//				{
//					text.append(jsonObjectToHumanText(jobj));
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			logger.fatal(e);
//			throw new RuntimeException(e);
//		}
//		return text.toString();
//	}
//
//	private String jsonObjectToHumanText(JSONObject jobj) throws JSONException 
//	{
//		if (jobj.getString(JSONConstants.TYPE).equals(InsertEvent.INSERT_EVENT_TYPE))
//		{
//			return "I"+jobj.getString(JSONConstants.EVENT_DATA);
//		}
//		else if (jobj.getString(JSONConstants.TYPE).equals(DeleteEvent.DELETE_EVENT_TYPE))
//		{
//			DocumentBuffer buffer = getDocumentSetToRender().getDocumentBuffers().get(jobj.getString(JSONConstants.DOCUMENT_ID));
//			String previousNeighborId = jobj.getString(JSONConstants.PREVIOUS_NEIGHBOR_ID);
//			JSONObject deletedEventJSON = getJSONObjectByEventID(previousNeighborId);
//			String deletedText = deletedEventJSON.getString(JSONConstants.EVENT_DATA);
//			return "D"+deletedText;
//		}
//		else
//		{
//			logger.error("jsonObjectToHumanText() has no idea what to do with "+jobj.toString());
//		}
//		return null;
//	}
//	
//	public String getSessionId() 
//	{
//		return sessionId;
//	}
//	public void setSessionId(String sessionId) 
//	{
//		this.sessionId = sessionId;
//	}
//	
////	private void setRelevantEventIds(Set<String> relevantEventIds) //The user should not call this
////	{
////		this.relevantEventIds = relevantEventIds;
////	}
//	public Set<String> getRelevantEventIds()
//	{
//		return relevantEventIds;
//	}	
////	@Override
////	public boolean shouldRenderEvent(JSONObject event) throws JSONException 
////	{
////		return relevantEventIds.contains(event.getString(ID));		//Everything that is not part of the selected bit is
////	}
//}
