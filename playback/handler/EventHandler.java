//if we do put this back remember to update the StorytellerHandlerFactory's 
//determineHandler() method

//package playback.handler;
//
//import httpserver.HTTPException;
//import httpserver.HTTPRequest;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import playback.PlaybackSessionServer;
//
//import core.StorytellerCore;
//import core.data.DBAbstractionException;
//import core.events.StorytellerEvent;
//import core.services.json.JSONiffy;
//
//
//public class EventHandler extends StorytellerHTTPHandler {
//
//	public EventHandler(HTTPRequest request, PlaybackSessionServer sessionManager) throws HTTPException {
//		super(request, sessionManager);
//
//		//Create
//		addPOST("/new" , "postCreate");
//
//		//Retrieve
//		addGET("/{String}" , "getEvent");
//
//		//Update
//		addPOST("/{String}/update" , "postUpdate");
//
//		//Delete
//		addGET("/{String}/delete" , "getDelete");
//	}
//
//	public void postUpdate(String eventID)
//	{
//
//	}
//
//	/**
//	 * Called on "/new"
//	 * 
//	 * Creates a new Event to be saved in the database
//	 * 
//	 */
//	public void postCreate()
//	{
//
//	}
//
//	/**
//	 * Called on /{String}
//	 * 
//	 * Attempts to retrieve the event with the given ID
//	 * 
//	 * @param eventID ID of the event to retrieve
//	 */
//	public void getEvent(String eventID)
//	{
//		try
//		{
//			StorytellerEvent event = getPlaybackSessionServer().getCore().getLocalDatabase().getEvent(eventID);
//			if(event == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			JSONObject eventJSON = JSONiffy.toJSON(event);
//			setJSON(eventJSON);
//		}
//		catch(DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//	public void getDelete(String eventID)
//	{
//		try
//		{
//			StorytellerEvent event = getPlaybackSessionServer().getCore().getLocalDatabase().getEvent(eventID);
//			if(event == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			getPlaybackSessionServer().getCore().getLocalDatabase().deleteEvent(event);
//			message(200, STATUS_GOOD);
//		}
//		catch(DBAbstractionException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//}
