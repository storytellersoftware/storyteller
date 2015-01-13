package playback.handler;

import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.entities.Developer;
import core.entities.DeveloperGroup;
import core.services.json.JSONiffy;
import httpserver.HTTPException;
import httpserver.HTTPRequest;
import org.json.JSONException;
import playback.PlaybackSessionServer;

import java.util.ArrayList;
import java.util.List;

public class DeveloperHandler extends StorytellerHTTPHandler {

    public DeveloperHandler(HTTPRequest request, PlaybackSessionServer sessionManager) throws HTTPException {
        super(request, sessionManager);

        //******Developers
        //Create
        //addPOST("/new" , "postCreateDeveloper");
        //Update
        //addPOST("/{String}/update" , "postUpdateDeveloper");
        //Retrieve
        //addGET("/{String}", "getSingle");

        // /developer/all/sessionID/123
        addGET("/all/sessionID/{String}", "getAll");

        //Destroy
        //addGET("/{String}/delete" , "getDelete");
        //addGET("/all/delete" , "getDeleteAllSingles");

        //*****Developer Groups
        //Create
        //addPOST("/group/new" , "postCreateDeveloperGroup");
        //Update
        //addPOST("/group/{String}/update" , "postUpdateDeveloperGroup");
        //Retrieve

        // /developer/group/all/sessionID/123
        addGET("/group/all/sessionID/{String}", "getAllDevGroups");

        //addGET("/group/{String}" , "getSingleDevGroup");
        //Destroy
        //addGET("/group/{String}/delete" , "getDeleteDevGroup");
        //addGET("/group/delete" , "getDeleteAllGroups");
    }

    /**
     * Gets all the developers in a playback session
     * <p>
     * /developer/all/sessionID/123
     * addGET("/all/sessionID/{String}", "getAll");
     */
    public void getAll(String sessionId) {
        try {
            //get the db associated with the passed in session id
            DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();

            //get all of the developers in the db
            List<Developer> developers = db.getAllDevelopers();

            //wrap them up and return them
            setJSON(JSONiffy.toJSON(developers));
        } catch (DBAbstractionException | JSONException e) {    // JSON couldn't do it... Not good at all.
            error(500, EXCEPTION_ERROR, e);
        }
    }

    /**
     * Gets all the dev groups in a playback session along with the developers
     * in that group.
     * <p>
     * /developer/group/all/sessionID/123
     * addGET("/group/all/sessionID/{String}", "getAllDevGroups");
     */
    public void getAllDevGroups(String sessionId) {
        try {
            //get the db associated with the passed in session id
            DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();

            //get all the developer groups in the playback session
            List<DeveloperGroup> devGroups = db.getAllDeveloperGroups();

            //holds the devs in each of the groups
            List<List<Developer>> devsInDevGroups = new ArrayList<List<Developer>>();

            //for each dev group
            for (DeveloperGroup devGroup : devGroups) {
                //get the devs in that group
                devsInDevGroups.add(db.getDevelopersInADeveloperGroup(devGroup.getId()));
            }

            setJSON(JSONiffy.toJSON(devGroups, devsInDevGroups));
        } catch (JSONException | DBAbstractionException e) {
            error(500, EXCEPTION_ERROR, e);
        }
    }

//currently unused methods
//	/**
//	 * Called on  GET /{String id}
//	 * 
//	 * Attempt to return information about a single developer with the ID of
//	 * `id`.
//	 * 
//	 * @param id	ID of a Developer
//	 */
//	public void getSingle(String id)
//	{
//		try
//		{
//			//Developer dev = Developer.get(id);
//			Developer dev = getPlaybackSessionServer().getCore().getLocalDatabase().getDeveloper(id);
//			setJSON(JSONiffy.toJSON(dev));
//			setHandled(true);
//		}
//		catch (JSONException | DBAbstractionException e)
//		{	// JSON couldn't do it... Not good at all.
//			error(500, EXCEPTION_ERROR, e);
//		}
//		catch (NullPointerException e)
//		{	// error occurred when trying to grab developer (AKA dev == null)
//			error(418, NONEXISTENT_ENTITY_ERROR, e);
//		}
//	}
//
//	/**
//	 * called on /{String}/delete
//	 * 
//	 * attempts to delete a developer from the database
//	 * 
//	 * @param developerID ID of the developer to be deleted
//	 */
//	public void getDelete(String developerID)
//	{
//		try
//		{
//			Developer dev =getPlaybackSessionServer(). getCore().getLocalDatabase().getDeveloper(developerID);
//			if(dev == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			getPlaybackSessionServer().getCore().getLocalDatabase().deleteDeveloper(dev);
//			message(200, STATUS_GOOD);
//		}
//		catch (DBAbstractionException e) 
//		{
//			e.printStackTrace();
//		}
//	}	
//	/**
//	 * called on group/{String}
//	 * 
//	 * attempts to retrieve a single developer group
//	 * 
//	 * @param groupID ID number of the dev group to find
//	 */
//	public void getSingleDevGroup(String groupID)
//	{
//		try
//		{
//			//DeveloperGroup devGroup = DeveloperGroup.get(groupID);
//			DeveloperGroup devGroup = getPlaybackSessionServer().getCore().getLocalDatabase().getDeveloperGroup(groupID);
//			List < Developer > allDevsInGroup = getPlaybackSessionServer().getCore().getLocalDatabase().getDevelopersInADeveloperGroup(devGroup.getId());
//			if(devGroup == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//		
//			setJSON(JSONiffy.toJSON(devGroup, allDevsInGroup));
//		}
//		catch(DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//	/** 
//	 * called on /all/delete
//	 * 
//	 * Attempts to delete all the single developers
//	 * 
//	 */
//	public void getDeleteAllSingles()
//	{
//		try 
//		{
//			List<Developer> devList = getPlaybackSessionServer().getCore().getLocalDatabase().getAllDevelopers();
//			if(!devList.isEmpty())
//			{
//				for(Developer deleteMe : devList)
//				{
//					getPlaybackSessionServer().getCore().getLocalDatabase().deleteDeveloper(deleteMe);
//				}
//			}
//			message(200,STATUS_GOOD);
//		} 
//		catch (DBAbstractionException e) 
//		{
//			e.printStackTrace();
//		}
//	}
//	
//	public void getDeleteDevGroup(String groupID)
//	{
//		try
//		{
//			DeveloperGroup devGroup = getPlaybackSessionServer().getCore().getLocalDatabase().getDeveloperGroup(groupID);
//			if(devGroup == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			getPlaybackSessionServer().getCore().getLocalDatabase().deleteDeveloperGroup(devGroup);
//			message(200, STATUS_GOOD);
//		}
//		catch (DBAbstractionException e) 
//		{
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * 
//	 * called on GET /delete/allgroups
//	 * 
//	 * attepts to delete all DeveloperGroups
//	 * 
//	 */
//	public void getDeleteAllGroups()
//	{
//		try 
//		{
//			List<DeveloperGroup> devGroupList = getPlaybackSessionServer().getCore().getLocalDatabase().getAllDeveloperGroups();
//			if(!devGroupList.isEmpty())
//			{
//				for(DeveloperGroup deleteMe : devGroupList)
//				{
//					getPlaybackSessionServer().getCore().getLocalDatabase().deleteDeveloperGroup(deleteMe);
//				}
//			}
//			message(200,STATUS_GOOD);
//		} 
//		catch (DBAbstractionException e) 
//		{
//			e.printStackTrace();
//		}
//	}
//	
//
//	/**
//	 * Called on /new
//	 * 
//	 * Attempts to create a new developer and add it to the database
//	 */
//	public void postCreateDeveloper()
//	{
//		try
//		{
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.DEVELOPERS));
//			Developer newDev = DeJSONiffy.developer(input);
//			getPlaybackSessionServer().getCore().getLocalDatabase().insertDeveloper(newDev);
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.DEVELOPER_ID, newDev.getId());
//			setResponseCode(201);
//			setJSON(response);
//		}
//		catch (JSONException | DBAbstractionException e)
//		{
//			error(418, MALFORMED_INPUT_ERROR, e);
//		}
//	}
//	
//	/** 
//	 * Called on /{String}/update
//	 * 
//	 * Attempts to update the chosen developer
//	 * 
//	 * Recieves a JSON containing a developer, DeJSONiffy's it, and sets the 
//	 * information of the developer in the database to match this new developer.
//	 * 
//	 * @param developerID ID of the developer to update
//	 */
//	public void postUpdateDeveloper(String developerID)
//	{
//
//		
//		try
//		{
//			Developer currentDev = getPlaybackSessionServer().getCore().getLocalDatabase().getDeveloper(developerID);
//			if(currentDev == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.DEVELOPERS));
//			Developer updateDev = DeJSONiffy.developer(input);
//			currentDev.update(updateDev);
//			getPlaybackSessionServer().getCore().getLocalDatabase().updateDeveloper(currentDev);
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.DEVELOPER_ID, currentDev.getId());
//			setResponseCode(200);
//			setJSON(response);
//			
//		}
//		catch (JSONException | DBAbstractionException e)
//		{
//			error(418, MALFORMED_INPUT_ERROR, e);
//		}
//	}
//	
//	/** 
//	 * Called on /group/new
//	 * 
//	 * Attempts to create a new developergroup with the information send to the server
//	 * 
//	 */
//	public void postCreateDeveloperGroup()
//	{
//		try
//		{
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.DEVELOPER_GROUPS));
//			DeveloperGroup newDevGroup = DeJSONiffy.developerGroup(input);
//			getPlaybackSessionServer().getCore().getLocalDatabase().insertDeveloperGroup(newDevGroup);
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.DEVELOPER_GROUP_ID, newDevGroup.getId());
//			setResponseCode(201);
//			setJSON(response);
//		}
//		catch (JSONException | DBAbstractionException e)
//		{
//			error(418, MALFORMED_INPUT_ERROR, e);
//		}
//	}
//	
//	/** 
//	 * Called on /group/{String}/update
//	 * 
//	 * Attempts to empty the developers in the group we have chosen, and then
//	 * refill it with the developers in the new group that was posted, thus
//	 * updating the developers in the group
//	 * 
//	 * @param devGroupID The ID of the developer group we wish to update
//	 */
//	public void postUpdateDeveloperGroup(String devGroupID)
//	{
//		try
//		{
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.DEVELOPER_GROUPS));
//			DeveloperGroup newDevGroup = DeJSONiffy.developerGroup(input);
//			DeveloperGroup currentDevGroup = getCore().getLocalDatabase().getDeveloperGroup(devGroupID);
//			if(currentDevGroup == null)
//			{
//				message(418, INVALID_ID_ERROR);
//			}
//			//Empty the developers we currently have in the group
//			currentDevGroup.getDevelopers().clear();
//			//Add the new ones we want in
//			for(Developer dev : newDevGroup.getDevelopers())
//			{
//				currentDevGroup.getDevelopers().add(dev);
//			}
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.DEVELOPER_GROUP_ID, newDevGroup.getId());
//			setResponseCode(200);
//			setJSON(response);
//		}
//		catch (JSONException | DBAbstractionException e)
//		{
//			error(418, MALFORMED_INPUT_ERROR, e);
//		}
//	}
}