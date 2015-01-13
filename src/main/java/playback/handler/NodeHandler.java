package playback.handler;

import core.Constants;
import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.entities.Node;
import core.services.json.JSONiffy;
import httpserver.HTTPException;
import httpserver.HTTPRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import playback.PlaybackSession;
import playback.PlaybackSessionServer;

import java.util.List;


public class NodeHandler extends StorytellerHTTPHandler {
    public NodeHandler(HTTPRequest request, PlaybackSessionServer sessionManager) throws HTTPException {
        super(request, sessionManager);

        //Create
        //addPOST("/new/sessionID/{String}" , "postCreateNode");

        //Retrieve
        //addGET("/{String}/sessionID/{String}", "getSingle");

        // /node/all/sessionID/123
        addGET("/all/sessionID/{String}", "getAll");

        // /node/234/sessionID/123/getfilterparams
        addGET("/{String}/sessionID/{String}/getfilterparams", "getFilterParams");

        //Update
        //addPOST("/{String}/sessionID/{String}/update" , "postUpdate");

        //Destroy
        //addGET("/{String}/sessionID/{String}/delete" , "getDelete");
        //addGET("/all/sessionID/{String}/delete" , "getDeleteAll");
    }

    /**
     * /node/all/sessionID/123
     * addGET("/all/sessionID/{String}", "getAll");
     */
    public void getAll(String sessionId) {
        try {
            //using the session id, get the db for the session
            DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();

            //get all the nodes in the database
            List<Node> nodes = db.getAllNodes();

            //add them to a json array
            JSONArray allNodes = JSONiffy.toJSON(nodes);
            JSONObject json = new JSONObject();
            json.put(Constants.NODES, allNodes);

            setJSON(json);

            setHandled(true);
        } catch (JSONException | DBAbstractionException e) {
            error(500, EXCEPTION_ERROR, e);
        }
    }

    /**
     * /node/234/sessionID/123/getfilterparams
     * addGET("/{String}/sessionID/{String}/getfilterparams", "getFilterParams");
     */
    public void getFilterParams(String nodeId, String sessionId) {
        PlaybackSession temporaryPlaybackSession = null;

        try {
            //get the logged in developer group from the session
            String loggedInDevGroupId = getPlaybackSessionServer().getPlaybackSession(sessionId).getLoggedInDeveloperGroupId();

            //using the session id, get the db for the session
            DBAbstraction db = getPlaybackSessionServer().getPlaybackSession(sessionId).getDatabase();

            //create a throwaway playback session that builds up the required nodes to build the filter info
            temporaryPlaybackSession = new PlaybackSession(db, loggedInDevGroupId, nodeId, -1);

            //create a JSON object with the filter information for the client
            JSONObject returnFilters = temporaryPlaybackSession.createFilterInfo(true);

            //return the json
            setJSON(returnFilters);
            setHandled(true);
        } catch (DBAbstractionException e) {
            error(418, "no node by that id", e);
            return;
        } catch (Exception e) {
            error(500, EXCEPTION_ERROR, e);
        }
    }

//currently unused	
//	public void postCreateNode(String sessionId)
//	{
//		try
//		{
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.NODE));
//			Node newNode = DeJSONiffy.node(input);
//			
//			getPlaybackSessionServer().getCore().getLocalDatabase().insertNode(newNode);
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.NODE_ID, newNode.getId());
//			setResponseCode(201);
//			setJSON(response);
//		}
//		catch (JSONException | DBAbstractionException e)
//		{
//			error(418, MALFORMED_INPUT_ERROR, e);
//		}
//	}
//	
//	public void postUpdate(String id, String sessionId)
//	{
//		try
//		{
//			Node currentNode = getPlaybackSessionServer().getCore().getLocalDatabase().getNode(id);
//			if(currentNode == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.NODE));
//			Node updateNode = DeJSONiffy.node(input);
//			currentNode.update(updateNode);
//			getPlaybackSessionServer().getCore().getLocalDatabase().updateNode(currentNode);
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.NODE_ID, currentNode.getId());
//			setResponseCode(200);
//			setJSON(response);
//		}
//		catch(DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//	public void getSingle(String nodeId, String sessionId)
//	{
//		try
//		{
//			Node node = getPlaybackSessionServer().getCore().getLocalDatabase().getNode(nodeId);
//			if(node ==null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			JSONObject json = JSONiffy.toJSON(node);
//			setJSON(json);
//			setHandled(true);
//		}
//		catch(JSONException | DBAbstractionException e)
//		{
//			error(418, "Not a Node", e);
//		}
//	}
//
//
//	/** 
//	 * Called on /{String}/delete
//	 * 
//	 * Attempts to delete the node with the given ID
//	 * 
//	 * @param nodeID ID of the node to be deleted
//	 */
//	public void getDelete(String nodeID, String sessionId)
//	{
//		try
//		{
//			Node node = getPlaybackSessionServer().getCore().getLocalDatabase().getNode(nodeID);
//			if(node == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			getPlaybackSessionServer().getCore().getLocalDatabase().deleteNode(node);
//			message(200, STATUS_GOOD);
//		}
//		catch(DBAbstractionException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//	public void getDeleteAll(String sessionId)
//	{
//		try
//		{
//			List<Node> nodes = getPlaybackSessionServer().getCore().getLocalDatabase().getAllNodes();
//			if(nodes.isEmpty())
//			{
//				message(200, "No nodes to delete");
//				return;
//			}
//			for(Node deleteMe : nodes)
//			{
//				getPlaybackSessionServer().getCore().getLocalDatabase().deleteNode(deleteMe);
//			}
//			message(200, STATUS_GOOD);
//		}
//		catch(DBAbstractionException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}	
}
