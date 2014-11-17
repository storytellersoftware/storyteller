package playback.handler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import playback.PlaybackSessionServer;

import core.StorytellerCore;

import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPRequest;


/**
 * A StorytellerHTTPHandler is the basis for all handlers explicitly used
 * by Storyteller (explicitly because there are some implicit ones used when
 * bad things happen with the httpserver code).
 *
 * Storyteller Handlers require an additional piece of information compared
 * to regular handlers, a reference to the running StorytellerServer, so that
 * they can access the data.
 */
public class StorytellerHTTPHandler extends HTTPHandler {

	public static final String INVALID_ID_ERROR = "Invalid ID";
	public static final String EXCEPTION_ERROR = "Exception Error";
	
	public static final String NONEXISTENT_ENTITY_ERROR = "Nonexistent entity";

	public static final String RESPONSE_TYPE_JSON = "application/json";

	//TODO this is todo
	//private StorytellerCore core;
	private PlaybackSessionServer sessionServer;

	/**
	 * All StorytellerHTTPHandlers require an {@link httpserver#HTTPRequest}
	 * and a StorytellerServer. The HTTPRequest is used to figure out what
	 * the client wants. The StorytellerServer is used to get the information
	 * to send back to the client.
	 *
	 * @param request	The incoming data from the client.
	 * @param core	The running StorytellerServer to get data from.
	 *
	 * @throws HTTPException	when something not good happens.
	 */
	public StorytellerHTTPHandler(HTTPRequest request, PlaybackSessionServer sessionManager) throws HTTPException
	{
		super(request);
		//setCore(core);
		setPlaybackSessionServer(sessionManager);
	}


	/**
	 * Set the response to a JSON Object.
	 *
	 * This sets the response's content-type to application/json,
	 * and the response's body to the passed in JSONObject. It tries to set
	 * the response body to a pretty, human readable JSON string, but if it
	 * can't, it just sends back the hard to read version.
	 * 
	 * @param json	The JSONObject to be used as the response's body
	 *
	 * @see StorytellerHTTPHandler#setJSON(JSONArray)
	 * @see HTTPHandler#message(int, String)
	 */
	public void setJSON(JSONObject json)
	{
		setResponseType(RESPONSE_TYPE_JSON);

		try
		{
			setResponseText(json.toString(2));
		}
		catch (JSONException e)
		{
			setResponseText(json.toString());
		}

		setHandled(true);
	}
	
	public void setJSON(int statusCode, JSONObject json)
	{
		setResponseCode(statusCode);
		setJSON(json);
	}

	/**
	 * Set the response to a JSON Array.
	 * 
	 * Similar to the {@link StorytellerHTTPHandler#setJSON(JSONObject)},
	 * this sets the response's body to a JSON Array. It tries to make set
	 * a pretty, human readable version, but if it can't, it uses the harder
	 * to decipher version.
	 * 
	 * Also sets the response's content type to application/json
	 * 
	 * @param json	JSON Object to be used as the response's body.
	 * 
	 * @see StorytellerHTTPHandler#setJSON(JSONObject)
	 * @see HTTPHandler#message(int, String)
	 */
	public void setJSON(JSONArray json)
	{
		setResponseType(RESPONSE_TYPE_JSON);
		try
		{
			setResponseText(json.toString(2));
		}
		catch (JSONException e)
		{
			setResponseText(json.toString());
		}

		setHandled(true);
	}
	
	public void setJSON(int statusCode, JSONArray json)
	{
		setResponseCode(statusCode);
		setJSON(json);
	}
	

	public static String getResource(String path)
	{
		try
		{
			return URLDecoder.decode(ClassLoader.getSystemResource(URLDecoder.decode(path, "UTF-8")).getPath(), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{	// This shouldn't happen...
			e.printStackTrace();
		}
		return path;
	}

/*
	private void setCore(StorytellerCore core)
	{
		this.core = core;
	}

	public StorytellerCore getCore()
	{
		return core;
	}
*/

	public PlaybackSessionServer getPlaybackSessionServer()
	{
		return sessionServer;
	}

	public void setPlaybackSessionServer(PlaybackSessionServer sessionServer)
	{
		this.sessionServer = sessionServer;
	}
}
