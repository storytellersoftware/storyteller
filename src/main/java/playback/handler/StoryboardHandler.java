package playback.handler;

import core.Constants;
import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.entities.Clip;
import core.entities.ClipComment;
import core.entities.Storyboard;
import core.services.json.JSONiffy;
import httpserver.HTTPException;
import httpserver.HTTPRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import playback.PlaybackSessionServer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class StoryboardHandler extends StorytellerHTTPHandler {

    public StoryboardHandler(HTTPRequest request, PlaybackSessionServer sessionManager) throws HTTPException {
        super(request, sessionManager);

        //Create
        // /storyboard/new/sessionID/123
        addPOST("/new/sessionID/{String}", "postCreate");

        //Retrieve
        // /storyboard/234/sessionID/123
        addGET("/{String}/sessionID/{String}", "getSingle");

        // /storyboard/all/sessionID/123
        addGET("/all/sessionID/{String}", "getAll");

        //Update
        //addPOST("/{String}/update" , "postUpdate");

        //Destroy
        // /storyboard/234/delete/sessionID/123
        addGET("/{String}/delete/sessionID/{String}", "getDelete");

        //addGET("/all/delete" , "getDeleteAll");

        //Other
        // /storyboard/selection/sessionID/123
        addPOST("/selection/sessionID/{String}", "postInSelection");
    }

    /**
     * /storyboard/new/sessionID/123
     * addPOST("/new/sessionID/{String}", "postCreate");
     */
    public void postCreate(String sessionId) {
        JSONObject json = null;

        try {
            //attempt to get the storyboard post data
            json = new JSONObject(getRequest().getPostData().get(Constants.STORYBOARD));
        } catch (JSONException e) {
            error(418, MALFORMED_INPUT_ERROR, e);
            return;
        }

        try {
            //get the storyboard data
            JSONArray clipIDs = json.getJSONArray(Constants.CLIPS);
            String name = json.getString(Constants.NAME);
            String description = json.getString(Constants.DESCRIPTION);

            //get the logged in dev group id from the session
            String loggedInDevGroupId = getPlaybackSessionServer().getPlaybackSession(sessionId).getLoggedInDeveloperGroupId();

            //get the db from the passed in session id
            DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();

            //create a new storyboard
            Storyboard newStoryboard = new Storyboard(new Date(), null, loggedInDevGroupId, name, description);

            //insert the new storyboard into the db
            db.insertStoryboard(newStoryboard);

            //for each of the clips
            for (int i = 0; i < clipIDs.length(); i++) {
                //get the clip from the db
                Clip tempClip = db.getClip(clipIDs.getString(i));

                //join the clip and the storyboard
                db.joinClipAndStoryboard(tempClip, newStoryboard, i);
            }

            //wrap up the id of the new storyboard and return
            JSONObject response = new JSONObject();
            response.put(Constants.STORYBOARD_ID, newStoryboard.getId());

            setJSON(response);
        } catch (DBAbstractionException | JSONException e) {
            error(500, EXCEPTION_ERROR, e);
        }
    }

    /**
     * /storyboard/234/sessionID/123
     * addGET("/{String}/sessionID/{String}", "getSingle");
     */
    public void getSingle(String storyboardId, String sessionId) {
        try {
            //get the db from the passed in session id
            DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();

            //attempt to get the storyboard
            Storyboard storyboard = db.getStoryboard(storyboardId);

            //if the storyboard was found
            if (storyboard != null) {
                //wrap it up in JSON and return it
                JSONObject json = JSONiffy.toJSON(storyboard);
                json.put(Constants.CLIPS, getClipsInStoryboard(db, storyboard.getId()));

                setJSON(json);
            } else //no storyboard with that id
            {
                //return an error
                message(418, INVALID_ID_ERROR);
                return;
            }
        } catch (DBAbstractionException | JSONException e) {
            error(500, EXCEPTION_ERROR, e);
        }
    }

    /**
     * /storyboard/all/sessionID/123
     * addGET("/all/sessionID/{String}", "getAll");
     */
    public void getAll(String sessionId) {
        try {
            //TODO Eventually I want to show all storyboards in all open databases, do this then
            //when we want to see
//			for(DBAbstraction db : getPlaybackSessionServer().getCore().getAllOpenDatabases())
//			{
//				//TODO maybe get the project name as well to list the storyboards by project??
//				
//				//get this database's storyboards
//				storyboards.addAll(db.getAllStoryboards());
//			}

            //get the db from the passed in session id
            DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();

            //get all the storyboards in this playback session
            List<Storyboard> storyboards = db.getAllStoryboards();
            JSONArray jsonArr = new JSONArray();

            //go through each storyboards
            for (Storyboard storyboard : storyboards) {
                //wrap up the storyboard objects
                JSONObject json = JSONiffy.toJSON(storyboard);

                json.put(Constants.CLIPS, getClipsInStoryboard(db, storyboard.getId()));
                jsonArr.put(json);
            }

            setJSON(jsonArr);
        } catch (DBAbstractionException | JSONException e) {
            error(500, EXCEPTION_ERROR, e);
        }
    }

    /**
     * /storyboard/234/delete/sessionID/123
     * addGET("/{String}/delete/sessionID/{String}", "getDelete");
     */
    public void getDelete(String storyboardId, String sessionId) {
        try {
            //get the database from the passed in session id
            DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();

            //get the storyboard
            Storyboard storyboard = db.getStoryboard(storyboardId);

            //if the storyboard is found
            if (storyboard != null) {
                //delete it
                db.deleteStoryboard(storyboard);
            } else //no storyboard with that id
            {
                //return an error
                message(418, INVALID_ID_ERROR);
                return;
            }

            message(200, STATUS_GOOD);
        } catch (DBAbstractionException e) {
            error(418, StorytellerHTTPHandler.INVALID_ID_ERROR, e);
        }
    }

    /**
     * /storyboard/selection/sessionID/123
     * addPOST("/selection/sessionID/{String}", "postInSelection");
     */
    public void postInSelection(String sessionId) {
        try {
            //TODO come back and finish this- finding playbacks from selected text
            //get the selected event ids
            JSONArray eventIDs = new JSONArray(getRequest().getPostData().get(Constants.EVENT_IDS));

//			//get the storyboards from the selected event ids
//			setJSON(getSessionManager().getStoryboardsFromEvents(eventIDs, PlaybackSession.GUEST_DEVELOPER_GROUP_ID));
//			
//			
//			JSONObject retVal = new JSONObject();
//			
//			PlaybackSession newSession = new PlaybackSession(getSessionManager().getCore(), PlaybackSession.GUEST_DEVELOPER_GROUP_ID);
//			getSessionManager().currentPlaybackSessions.put(newSession.getId(), newSession);
//			
//			retVal.put(Constants.SESSION_ID, newSession.getId());
//	
//			List<String> listOfEventIds = new ArrayList<String>();
//			for (int i = 0; i < eventIds.length(); i++)
//			{
//				listOfEventIds.add(eventIds.getString(i));
//			}
//	
//			List<Storyboard> storyboardList = getCore().getLocalDatabase().getAllStoryboardsAssociatedWithEvents(listOfEventIds);
//			//newSession.setStoryboardsFromSelectedEvents(storyboardList);
//
            setHandled(true);
        } catch (/*DBAbstractionException | */JSONException e) {
            error(418, MALFORMED_INPUT_ERROR, e);
        }
    }

    /**
     * Helper to get the clips in a storyboard and wrap them up in JSON
     */
    private JSONArray getClipsInStoryboard(DBAbstraction db, String storyboardId) throws DBAbstractionException, JSONException {
        JSONArray clipArr = null;

        //get all the clips from the db
        List<Clip> allClips = db.getClipsAssociatedWithStoryboard(storyboardId);//getSessionManager().getAllClipsFromStoryboard(storyboardId);

        //holds lists of comments for each clip
        List<List<ClipComment>> allClipComments = new ArrayList<List<ClipComment>>();

        //for each clip
        for (Clip clip : allClips) {
            //get the comments associated with the clip
            allClipComments.add(db.getAllClipCommentsAssociatedWithAClip(clip.getId()));
        }

        //wrap up the clips
        clipArr = JSONiffy.toJSON(allClips, allClipComments);

        return clipArr;
    }

//currently unused
//	public void postUpdate(String id)
//	{
//		try
//		{
//			Storyboard currentStoryboard = getPlaybackSessionServer().getCore().getLocalDatabase().getStoryboard(id);
//			if(currentStoryboard == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.STORYBOARD));
//			Storyboard updateStoryboard = DeJSONiffy.storyboard(input);
//			
//			currentStoryboard.update(updateStoryboard);
//			getPlaybackSessionServer().getCore().getLocalDatabase().updateStoryboard(currentStoryboard);
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.STORYBOARD_ID, currentStoryboard.getId());
//			setResponseCode(200);
//			setJSON(response);
//		}
//		catch(DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//	public void getDeleteAll()
//	{
//		try
//		{
//			List<Storyboard> storyboards = getPlaybackSessionServer().getCore().getLocalDatabase().getAllStoryboards();
//			if(storyboards.isEmpty())
//			{
//				message(200, "No Storyboards to delete");
//				return;
//			}
//			for(Storyboard deleteMe : storyboards)
//			{
//				getPlaybackSessionServer().getCore().getLocalDatabase().deleteStoryboard(deleteMe);
//			}
//			message(200, "All Storyboards deleted");
//		}
//		catch(DBAbstractionException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//	/**
//	 * Get a storyboard from any of the open databases based on the storyboard id
//	 */
//	private Storyboard getStoryboard(String storyboardId) throws DBAbstractionException
//	{
//		//the storyboard with the passed in id
//		Storyboard storyboard = null;
//		
//		//go through all of the open databases 
//		for(DBAbstraction db : getPlaybackSessionServer().getCore().getAllOpenDatabases())
//		{
//			//look for a storyboard with the passed in id
//			storyboard = db.getStoryboard(storyboardId);
//			
//			//if a storyboard was found
//			if(storyboard != null)
//			{
//				//stop looking
//				break;
//			}				
//		}
//		
//		return storyboard;
//	}
//
//	private DBAbstraction getDBAbstraction(String storyboardId) throws DBAbstractionException
//	{
//		//the db abstraction that we are looking for
//		DBAbstraction dbAbstraction = null;
//		
//		//go through all of the open databases 
//		for(DBAbstraction db : getPlaybackSessionServer().getCore().getAllOpenDatabases())
//		{
//			//look for a storyboard with the passed in id
//			Storyboard storyboard = db.getStoryboard(storyboardId);
//			
//			//if a storyboard was found
//			if(storyboard != null)
//			{
//				//we have found the correct database
//				dbAbstraction = db;
//				
//				//stop looking
//				break;
//			}				
//		}
//		
//		return dbAbstraction;
//	}
}
