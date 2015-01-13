package playback.handler;

import core.Constants;
import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.events.StorytellerEvent;
import core.services.json.DeJSONiffy;
import core.services.json.JSONiffy;
import httpserver.HTTPException;
import httpserver.HTTPRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import playback.PlaybackFilter;
import playback.PlaybackSession;
import playback.PlaybackSessionServer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class PlaybackHandler extends StorytellerHTTPHandler {
    public PlaybackHandler(HTTPRequest request, PlaybackSessionServer sessionManager) throws HTTPException {
        super(request, sessionManager);

        //Create
        //change these to session ids
        // /playback/new/sessionID/123
        addGET("/new/sessionID/{String}", "getNew");

        //addGET("/new/{String}/{Integer}/{Integer}", "getNewSelectedText");

        //Retrieve
        // /playback/filter/sessionID/123
        addGET("/filter/sessionID/{String}", "getSessionsFilter");

        //addGET("/{String}/events/all", "getAllEvents");

        // /playback/events/sessionID/123/from/0/to/1000
        addGET("/events/sessionID/{String}/from/{Integer}/to/{Integer}", "getEvents");

        // /playback/export
        addPOST("/export", "export");

        // /playback/selectedText/sessionID/123
        addPOST("/selectedText/sessionID/{String}", "postSelection");

        //Update
        // /playback/filter/sessionID/123
        addPOST("/filter/sessionID/{String}", "postFilter");

        //Destroy
        // /playback/delete/sessionID/123
        addGET("/delete/sessionID/{String}", "getDeleteSession");
    }

    /**
     * /playback/new/sessionID/123
     * addGET("/new/sessionID/{String}", "getNew");
     */
    public void getNew(String originalSessionId) {
        try {
            //get the logged in developer group
            String loggedInDevGroupId = getPlaybackSessionServer().getPlaybackSession(originalSessionId).getLoggedInDeveloperGroupId();
            //get the db from the original session id
            DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(originalSessionId).getDatabase();

            //create a brand new session and return its id
            String newSessionId = getPlaybackSessionServer().addPlaybackSession(db, loggedInDevGroupId);

            JSONObject json = new JSONObject();
            json.put(Constants.SESSION_ID, newSessionId);

            setJSON(json);
        } catch (DBAbstractionException | JSONException e) {
            error(500, EXCEPTION_ERROR, e);
        }
    }

    /**
     * /playback/filter/sessionID/123
     * addGET("/filter/sessionID/{String}", "getSessionsFilter");
     */
    public void getSessionsFilter(String sessionID) {
        try {
            //get the filter info for the session id
            JSONObject filter = getPlaybackSessionServer().getPlaybackSession(sessionID).createFilterInfo(true);

            //send the filter info back
            JSONObject response = new JSONObject();
            response.put(Constants.FILTERS, filter);

            setJSON(response);
        } catch (DBAbstractionException | JSONException e) {
            error(418, MALFORMED_INPUT_ERROR, e);
        }
    }

    /**
     * /playback/events/sessionID/123/from/0/to/1000
     * addGET("/events/sessionID/{String}/from/{Integer}/to/{Integer}", "getEvents");
     */
    public void getEvents(String sessionID, Integer startIndex, Integer endIndex) {
        try {
            //holds the events to be returned
            List<StorytellerEvent> events = null;

            //the total number of events to be returned
            int blockSize = endIndex - startIndex;

            //if the requested numbers are off
            if (startIndex < 0 || blockSize <= 0) {
                throw new NumberFormatException("startIndex or blockSize is < 0");
            }

            //get the playback session based on the passed in session id
            PlaybackSession playbackSession = getPlaybackSessionServer().getPlaybackSession(sessionID);

            //if there are more events being requested in the first request than
            //there are in the playback queue
            if (startIndex == 0 && playbackSession.getEventsToPlayback().size() <= endIndex) {
                //return all the events
                events = playbackSession.getEventsToPlayback();
            }
            //the start and end index are within the bounds of list
            else if (endIndex <= playbackSession.getEventsToPlayback().size()) {
                //pull out only the next group of event
                events = playbackSession.getEventsToPlayback().subList(startIndex, endIndex);
            }
            //the end index is beyond the number of events that are left
            else if (startIndex < playbackSession.getEventsToPlayback().size() &&
                    endIndex > playbackSession.getEventsToPlayback().size()) {
                //get the events that are left (but not quite enough to reach the end index)
                events = playbackSession.getEventsToPlayback().subList(startIndex, playbackSession.getEventsToPlayback().size());
            }

            setJSON(JSONiffy.toJSON(events));
            setHandled(true);
        } catch (JSONException e) {
            error(500, EXCEPTION_ERROR, e);
        }
    }

    /**
     * /playback/export
     * addPOST("/export", "export");
     */
    public void export() {
        JSONObject json = new JSONObject();

        try {
            // Put the objects into the JSON object.
            // TODO: on both client AND server side - convert this from a bunch
            //		 of standalone items in the post data to one json object in
            //		 the post data, and grab from that.
            //		 It would make this code SOOOOOOOOO much nicer.
            json.put(Constants.EVENTS, new JSONObject(getRequest().getPostData().get(Constants.EVENTS)));
            json.put(Constants.ORDER_OF_EVENTS, new JSONArray(getRequest().getPostData().get(Constants.ORDER_OF_EVENTS)));
            json.put(Constants.RELEVANT_EVENTS, new JSONArray(getRequest().getPostData().get(Constants.RELEVANT_EVENTS)));
            json.put(Constants.COMMENTS, new JSONObject(getRequest().getPostData().get(Constants.COMMENTS)));
            json.put(Constants.DEVELOPERS, new JSONObject(getRequest().getPostData().get(Constants.DEVELOPERS)));
            json.put(Constants.DEVELOPER_GROUPS, new JSONObject(getRequest().getPostData().get(Constants.DEVELOPER_GROUPS)));
            json.put(Constants.STORYBOARD, new JSONObject(getRequest().getPostData().get(Constants.STORYBOARD)));

            // Get the filename.
            String filename = new JSONObject(getRequest().getPostData().get(Constants.STORYBOARD)).getString(Constants.NAME) + ".zip";

            // Write everything to the zip file.
            File zipFile = new File(getResource("frontend/") + filename);
            ZipOutputStream outZip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));

            ZipEntry file = new ZipEntry("data.json");
            outZip.putNextEntry(file);
            outZip.write(new String("playbackData =" + json.toString(2)).getBytes());
            outZip.closeEntry();

            outZip.putNextEntry(new ZipEntry("standalone.min.html"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream("export/standalone.min.html")));
            StringBuilder standaloneStr = new StringBuilder();

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                standaloneStr.append(line);
                standaloneStr.append("\n");
            }

            reader.close();
            outZip.write(standaloneStr.toString().getBytes());
            outZip.closeEntry();

            outZip.finish();
            outZip.close();

            // Set the response to the filename so the user can request it later
            setResponseText(filename);
            setHandled(true);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * /playback/selectedText/sessionID/123
     * addPOST("/selectedText/sessionID/{String}", "postSelection");
     */
    public void postSelection(String sessionId) {
        try {
            //get the selected event ids
            JSONArray selectedEvents = new JSONArray(getRequest().getPostData().get(Constants.EVENT_IDS));

            List<String> eventIDs = new ArrayList<String>();
            for (int i = 0; i < selectedEvents.length(); i++) {
                eventIDs.add(selectedEvents.getString(i));
            }

            //get the database from the passed in session id
            //DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();

            //get the playback session
            PlaybackSession playbackSession = getPlaybackSessionServer().getPlaybackSession(sessionId);
            //String nodeID = getCore().getRootNode().getId();
            //get the root node in the db
            //String nodeID = db.getRootNode().getId();

            //get the last node in the playback session
            String nodeID = playbackSession.getLastNode().getNodeId();

            //create a new session id for the selected text playback
            String newSessionID = getPlaybackSessionServer().receiveSelectedTextForPlayback(playbackSession.getDatabase(), eventIDs, nodeID, -1, playbackSession.getLoggedInDeveloperGroupId());

            JSONObject response = new JSONObject();
            response.put(Constants.SESSION_ID, newSessionID);

            setJSON(response);
        } catch (DBAbstractionException | JSONException e) {
            error(418, MALFORMED_INPUT_ERROR, e);
        }
    }

    /**
     * /playback/filter/sessionID/123
     * addPOST("/filter/sessionID/{String}", "postFilter");
     */
    public void postFilter(String sessionId) {
        try {
            //create a new filter object from the passed in JSON filter object in the post data
            PlaybackFilter filter = DeJSONiffy.playbackFilter(getRequest().getPostData().get(Constants.FILTERS));
            //PlaybackFilter filter = new PlaybackFilter();

            //get the selected playback session
            PlaybackSession selectedSession = getPlaybackSessionServer().getPlaybackSession(sessionId);

            //update the playback session to be in the filter's node
            selectedSession.updatePlaybackSessionToNode(filter.getNodeID(), -1);

            //store the new filter in the playback session
            selectedSession.setFilter(filter);

            //start filtering events
            getPlaybackSessionServer().getPlaybackSession(sessionId).startFilteringEventsv2();

            //respond back to the client with no content
            noContent();
        } catch (DBAbstractionException | JSONException e) {
            error(418, MALFORMED_INPUT_ERROR, e);
        }
    }

    /**
     * /playback/delete/sessionID/123
     * addGET("/delete/sessionID/{String}" , "getDeleteSession");
     */
    public void getDeleteSession(String sessionId) {
        //get the playback session from the passed in session id
        PlaybackSession playbackSession = getPlaybackSessionServer().getPlaybackSession(sessionId);

        //remove the playback session from the playback session server
        getPlaybackSessionServer().deletePlaybackSession(playbackSession);

        //nothing to return
        noContent();
    }

//currently unused	
//	public void getNewSelectedText(String docId, Integer startPos, Integer endPos)
//	{
//		System.out.println("Doc id: " + docId + "\nStart: " + startPos + "\nEnd: " + endPos);
//		
//		try
//		{
//			String sessionID = getSessionManager().addPlaybackSession(PlaybackSession.GUEST_DEVELOPER_GROUP_ID);
//			JSONObject json = new JSONObject();
//			json.put(Constants.SESSION_ID, sessionID);
//
//			setJSON(json);
//		}
//		catch(DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//	public void getNewFromStoryboard(String storyboardId)
//	{
//		try
//		{
//			DBAbstraction db = null; 
//			for(DBAbstraction potentialDb : getPlaybackSessionServer().getCore().getAllOpenDatabases())
//			{
//				if(potentialDb.getStoryboard(storyboardId) != null)
//				{
//					db = potentialDb;
//					break;
//				}
//			}
//			String sessionID = getPlaybackSessionServer().addPlaybackSession(db, PlaybackSession.GUEST_DEVELOPER_GROUP_ID);
//
//			JSONObject json = new JSONObject();
//			json.put(Constants.SESSION_ID, sessionID);
//
//			setJSON(json);
//		}
//		catch(DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//	
//	public void getNewFromClip(String clipId)
//	{
//		try
//		{
//			DBAbstraction db = null; 
//			for(DBAbstraction potentialDb : getPlaybackSessionServer().getCore().getAllOpenDatabases())
//			{
//				if(potentialDb.getClip(clipId) != null)
//				{
//					db = potentialDb;
//					break;
//				}
//			}
//
//			String sessionID = getPlaybackSessionServer().addPlaybackSession(db, PlaybackSession.GUEST_DEVELOPER_GROUP_ID);
//
//			JSONObject json = new JSONObject();
//			json.put(Constants.SESSION_ID, sessionID);
//
//			setJSON(json);
//		}
//		catch(DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//
//	public void getAllEvents(String sessionID)
//	{
//		try
//		{
//			List<StorytellerEvent> events = getSessionManager().getAllEvents(sessionID);
//			
//			/*
//			JSONArray json = new JSONArray();
//			for(StorytellerEvent event: events)
//				json.put(event.toJSON());
//			*/
//			
//			setJSON(JSONiffy.toJSON(events));
//
//			//setResponseText(json.toString());
//		}
//		catch (DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}	
}
