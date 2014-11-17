package playback.handler;

import java.util.ArrayList;
import java.util.List;

import httpserver.HTTPException;
import httpserver.HTTPRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import playback.PlaybackSession;
import playback.PlaybackSessionServer;

import core.Constants;
import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.entities.Clip;
import core.entities.ClipComment;
import core.services.json.DeJSONiffy;
import core.services.json.JSONiffy;


public class ClipHandler extends StorytellerHTTPHandler {

	public ClipHandler(HTTPRequest request, PlaybackSessionServer sessionManager) throws HTTPException {
		super(request, sessionManager);

		//Clips
		// /clip/234/sessionID/123
		addGET("/{String}/sessionID/{String}", "getSingle");
		
		// /clip/all/sessionID/123
		addGET("/all/sessionID/{String}", "getAll");
		
		// /clip/new/sessionID/123
		addPOST("/new/sessionID/{String}", "postCreate");
		
		// /clip/delete/234/sessionID/123
		addGET("/delete/{String}/sessionID/{String}", "getDelete");
		
		//not using
		//addGET("/all/delete", "getDeleteAllClips");
		
		//not using
		//addPOST("/{String}/update" , "postUpdateClip");
		
		//Comments
		// /clip/234/sessionID/123/comment/new
		addPOST("/{String}/sessionID/{String}/comment/new", "newComment");
		
		//not using
		//addGET("/{String}/comment/all", "getAllComments");
		
		//not using
		//addGET("/comment/{String}", "getSingleComment");
		
		//not using
		//addGET("/comment/{String}/delete", "deleteComment");
		
		//not using
		//addGET("/{String}/comment/deleteall" , "getDeleteAllComments");
		
		//not using
		//addPOST("/comment/{String}/update" , "updateComment");
	}
	
	/**
	 * 
	 * Called on: GET /clip/234/sessionID/123
	 * addGET("/{String}/sessionID/{String}", "getSingle");
	 *
	 * Attempts to send information about a single clip back to the client.
	 * If no clip exists with the id of `clipID`, a 418 is sent back to the
	 * client.
	 *
	 * @param clipID	The ID of the Clip being requested
	 * @see Clip
	 */
	public void getSingle(String clipID, String sessionId)
	{
		try 
		{
			//get the db from the passed in session id
			DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();
			
			//get the clip
			Clip clip = db.getClip(clipID);

			//if the clip is not found
			if (clip == null)
			{
				//return an error
				message(418, INVALID_ID_ERROR);
				return;
			}

			//get all the clip comments for the clip
			List < ClipComment > allComments = db.getAllClipCommentsAssociatedWithAClip(clip.getId());
		
			//wrap up the clip and comments and return 
			JSONObject clipJSON = JSONiffy.toJSON(clip, allComments);
			
			//return the clip
			setJSON(clipJSON);
		} 
		catch (JSONException | DBAbstractionException e) 
		{
			error(500, EXCEPTION_ERROR, e);
		}
	}
	
	/**
	 * Called on  GET /clip/all/sessionID/123
	 * addGET("/all/sessionID/{String}", "getAll");
	 *
	 * Send an array of clip objects in JSON back to the server. If anything
	 * bad happens while attempting to do this, we send back a 500 error.
	 */
	public void getAll(String sessionId)
	{
		try
		{
			//get the database from the passed in session id
			DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();
			
			//get all the clips from the db
			List < Clip > allClips = db.getAllClips();
			
			//holds lists of comments for each clip
			List < List < ClipComment > > allClipComments = new ArrayList < List < ClipComment > >();
			
			//for each clip
			for(Clip clip : allClips)
			{
				//get the comments associated with the clip
				allClipComments.add(db.getAllClipCommentsAssociatedWithAClip(clip.getId()));
			}			
			
			//get all the clips and wrap them up in json
			JSONArray clips = JSONiffy.toJSON(allClips, allClipComments);
			
			//return all the clips
			setJSON(clips);
		} 
		catch (JSONException | DBAbstractionException e) 
		{
			error(500, EXCEPTION_ERROR, e);
		}
	}
	
	/**
	 * Called on  POST /clip/new/sessionID/123
	 * addPOST("/new/sessionID/{String}", "postCreate");
	 *
	 * Uses the POST data, keyed to "clip" to create a new Clip on the server.
	 * 
	 * If everything goes right, a 201 Created is sent back to the client,
	 * along with the newly created Clip's ID.
	 * 
	 * If one of the following happens, a 418 is sent back, with a malformed
	 * input error.
	 * 
	 * 		- There is no post data with the key "clip"
	 * 		- The post data with the key "clip" can't be turned into a
	 * 			JSONObject
	 * 		- The clip cannot be created (most likely due to the required
	 * 			data not being present).
	 */
	public void postCreate(String sessionId)
	{
		try
		{
			//get the clip info from the client (clip name, clip description, 
			//developerGroupID, sessionID)
			JSONObject clipJSON = new JSONObject(getRequest().getPostData().get(Constants.CLIP));

			//pull data from the json object
			String name = clipJSON.getString(Constants.NAME);
			String description = clipJSON.getString(Constants.DESCRIPTION);
			
			//get the playback session to add this clip to
			PlaybackSession session = getPlaybackSessionServer().getPlaybackSession(sessionId);

			//get the node and sequence num
			String nodeID = session.getFilter().getNodeID();
			int nodeSeqNum = session.getFilter().getNodeSequenceNumber();

			//create a new clip object (in memory only)
			Clip clip = new Clip(nodeID, session.getLoggedInDeveloperGroupId(), name, description, session.getFilter());

			//add the clip to the db
			session.getDatabase().insertClip(clip);
			
			// Set the response to the new clip's ID, and set the response
			// code to 201 created.
			JSONObject response = new JSONObject();
			
			//write the new clip's id back to the client
			response.put(Constants.CLIP_ID, clip.getId());
			
			setResponseCode(201); // 201 Created
			
			setJSON(response);
		}
		catch (JSONException | DBAbstractionException e)
		{
			error(418, MALFORMED_INPUT_ERROR, e);
		}
	}

	/**
	 * Called on  GET /clip/delete/234/sessionID/123
	 * addGET("/delete/{String}/sessionID/{String}", "getDelete");
	 * 
	 * Tries to delete a clip. We don't have to ask the database to delete the
	 * associated comments, because it already does it automatically when
	 * deleting a clip.
	 * 
	 * @param clipID	the ID of the clip to be deleted
	 */
	public void getDelete(String clipID, String sessionId) 
	{
		try
		{
			//get the db from the passed in session id
			DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();
			
			//get the clip we are about to delete
			Clip clip = db.getClip(clipID);
			
			//if the clip was not found it cannot be deleted
			if (clip == null)
			{
				message(418, INVALID_ID_ERROR);
				return;
			}

			//delete the clip
			db.deleteClip(clip);
			
			message(200, "Clip Deleted!");
		}
		catch (DBAbstractionException e)
		{
			error(500, EXCEPTION_ERROR, e);
		}
	}
	
	/**
	 * Called on  GET /clip/234/sessionID/123/comment/new
	 * addPOST("/{String}/sessionID/{String}/comment/new", "newComment");
	 *
	 * Attempts to create a new comment for the passed in clipID. If the clip
	 * doesn't exist in the database, a 418 error is sent back to the client.
	 * 
	 * After the comment is created, the ID is sent back to the client.
	 *
	 * @param clipId	The ID of the Clip the new comment is being added to.
	 *
	 * @see Clip
	 */
	public void newComment(String clipId, String sessionId) 
	{
		try
		{
			//get the db from the passed in session id
			DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();

			//get the logged in dev group from the playback session
			String loggedInDevGroupId = getPlaybackSessionServer().getPlaybackSession(sessionId).getLoggedInDeveloperGroupId();
			
			//get the clip
			Clip clip = db.getClip(clipId);
			
			//if the clip cannot be found we cannot add a comment to it
			if (clip == null)
			{
				message(418, INVALID_ID_ERROR);
				return;
			}
		
			//get the comment info
			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.COMMENT));			
			//add the logged in dev group id from the playback session
			input.put(Constants.DEVELOPER_GROUP_ID, loggedInDevGroupId);

			//create a clip comment
			ClipComment comment = DeJSONiffy.clipComment(clip, input);
			
			//insert the comment in the db
			db.insertClipComment(comment);
			
			setResponseCode(201); // created
			
			setJSON(new JSONObject().put(Constants.COMMENT_ID, comment.getId()));
		}
		catch (JSONException | DBAbstractionException e)
		{
			error(500, EXCEPTION_ERROR, e);
		}
	}
	
//currently unused methods
//	/**
//	 * Called on Get /{String}/comment/all
//	 * 
//	 * Attempts to retrieve all of the comments for a specific clip
//	 * 
//	 *
//	 * 
//	 * @param clipID  The ID of the clip to retrieve comments for
//	 */
//	public void getAllComments(String clipID) 
//	{
//		try
//		{
//			//Clip clip = Clip.get(clipID);
//			Clip clip = getPlaybackSessionServer().getCore().getLocalDatabase().getClip(clipID);
//			
//			if (clip == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//		
//			List < ClipComment > clipComments = getPlaybackSessionServer().getCore().getLocalDatabase().getAllClipCommentsAssociatedWithAClip(clipID);
//			JSONArray comments = JSONiffy.toJSON(clipComments);
//			setJSON(comments);
//		}
//		catch (JSONException | DBAbstractionException e) 
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//	
//	
//	/**
//	 * Called on GET /comment/{String commentID}/delete
//	 * 
//	 * Delete's a ClipComment. We don't have to do anything extra because clips
//	 * don't really know anything about their comments in the DB.
//	 * 
//	 * @param commentID	ID of the comment to delete
//	 */
//	public void deleteComment(String commentID) 
//	{
//		try
//		{
//			//ClipComment comment = ClipComment.get(commentID);
//			ClipComment comment = getPlaybackSessionServer().getCore().getLocalDatabase().getClipComment(commentID);
//	
//			if (comment == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//
//			getPlaybackSessionServer().getCore().getLocalDatabase().deleteClipComment(comment);
//			//comment.delete();
//			message(200, "Deleted!");
//		}
//		catch (DBAbstractionException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//	
//	/**
//	 * Called on /comment/{String}/update
//	 * 
//	 * Updates the text of the comment
//	 * 
//	 * @param commentID ID of the comment to update
//	 */
//	public void updateComment(String commentID)
//	{
//		try
//		{
//			//ClipComment comment = ClipComment.get(commentID);
//			ClipComment comment = getPlaybackSessionServer().getCore().getLocalDatabase().getClipComment(commentID);
//	
//			if (comment == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//		
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.COMMENT));		
//			ClipComment updateComment = DeJSONiffy.clipComment(input);
//			comment.update(updateComment);
//			getPlaybackSessionServer().getCore().getLocalDatabase().updateClipComment(comment);
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.COMMENT_ID, comment.getId());
//			setResponseCode(200);
//			setJSON(response);
//		}
//		catch(DBAbstractionException | JSONException | IDEServerException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//	
//	
//	/**
//	 * Called on GET /comment/{String}
//	 * 
//	 * Attempts to retrieve a single comment
//	 * 
//	 * @param commentID 	ID of the comment to be retrieved
//	 */	
//	public void getSingleComment(String commentID)
//	{
//		try
//		{
//			//ClipComment comment = ClipComment.get(commentID);
//			ClipComment comment = getPlaybackSessionServer().getCore().getLocalDatabase().getClipComment(commentID);
//	
//			if(comment == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//
//			JSONObject commentJSON = JSONiffy.toJSON(comment);
//			setJSON(commentJSON);
//		}
//		catch(DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//	
//	
//	
//	/**
//	 * Called on /{String}/comment/deleteall
//	 * 
//	 * Attempts to delete all comments associated with the given clip
//	 * 
//	 * @param clipID ID of the clip that needs all of its comments deleted
//	 */
//	public void getDeleteAllComments(String clipID)
//	{
//		try
//		{
//			//Clip clip = Clip.get(clipID);
//			Clip clip = getPlaybackSessionServer().getCore().getLocalDatabase().getClip(clipID);
//			
//			if(clip == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//
//			for(ClipComment comment : getPlaybackSessionServer().getCore().getLocalDatabase().getAllClipCommentsAssociatedWithAClip(clipID))
//			{
//				getPlaybackSessionServer().getCore().getLocalDatabase().deleteClipComment(comment);
//				//comment.delete();
//			}
//			message(200, STATUS_GOOD);
//		}
//		catch(DBAbstractionException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//	
//	/**
//	 * Called on GET /all/delete
//	 * 
//	 * Attempts to delete all of the clips
//	 * 
//	 * 
//	 */
//	public void getDeleteAllClips()
//	{
//		try 
//		{			
//			//List<Clip> clipList = Clip.all();
//			List<Clip> clipList = getPlaybackSessionServer().getCore().getLocalDatabase().getAllClips();
//			
//			if(!clipList.isEmpty())
//			{
//				for(Clip deleteMe : clipList)
//				{
//					getPlaybackSessionServer().getCore().getLocalDatabase().deleteClip(deleteMe);
//					//deleteMe.delete();
//				}
//			}
//			
//			message(200,STATUS_GOOD);
//		} 
//		catch (DBAbstractionException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//
//	/**
//	 * Called on "/{String}/update"
//	 * 
//	 * Attempts to update the clip with the new data passed in
//	 * 
//	 * 
//	 * @param clipID ID of the clip to be updated
//	 */
//	public void postUpdateClip(String clipID)
//	{
//		try
//		{
//			//Clip currentClip = Clip.get(clipID);
//			Clip currentClip = getPlaybackSessionServer().getCore().getLocalDatabase().getClip(clipID);
//	
//			if(currentClip == null)
//			{
//				message(418, INVALID_ID_ERROR);
//			}
//
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.CLIP));
//			Clip newClip = DeJSONiffy.clip(input);
//			currentClip.update(newClip);
//			getPlaybackSessionServer().getCore().getLocalDatabase().updateClip(currentClip);
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.CLIP_ID, currentClip.getId());
//			setResponseCode(200);
//			setJSON(response);
//		}
//		catch (JSONException | DBAbstractionException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//	/**
//	 * Get a clip from any of the open databases based on the clip id
//	 */
//	private Clip getClip(String clipId) throws DBAbstractionException
//	{
//		//the clip with the passed in id
//		Clip clip = null;
//		
//		//go through all of the open databases 
//		for(DBAbstraction db : getPlaybackSessionServer().getCore().getAllOpenDatabases())
//		{
//			//look for a clip with the passed in id
//			clip = db.getClip(clipId);
//			
//			//if a clip was found
//			if(clip != null)
//			{
//				//stop looking
//				break;
//			}				
//		}
//		
//		return clip;
//	}
//
//	private DBAbstraction getDBAbstraction(String clipId) throws DBAbstractionException
//	{
//		//the db abstraction that we are looking for
//		DBAbstraction dbAbstraction = null;
//		
//		//go through all of the open databases 
//		for(DBAbstraction db : getPlaybackSessionServer().getCore().getAllOpenDatabases())
//		{
//			//look for a clip with the passed in id
//			Clip clip = db.getClip(clipId);
//			
//			//if a clip was found
//			if(clip != null)
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
