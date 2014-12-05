package playback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import core.StorytellerCore;
import core.data.DBAbstraction;
import core.data.DBAbstractionException;

/**
 * This class holds all of the playback sessions in a map keyed on a playback
 * session id. This id is passed in for all playback operations and this server
 * is queried to get the correct playback session.
 */ 
public class PlaybackSessionServer
{
    //reference to the storyteller core for services
    private StorytellerCore core;

    //hold all of the playback sessions that this server is managing by the 
    //playback session's id
	private final Map<String, PlaybackSession> currentPlaybackSessions;

	/**
	 * Create a server 
	 */
    public PlaybackSessionServer(StorytellerCore core)
    {
    		setCore(core);
    		
    		currentPlaybackSessions = new HashMap<String, PlaybackSession>();
    }
    
    /**
     * This method is called when the IDE needs to inform the playback server that it
     * is requesting a selected text playback.       
     */
	public String receiveSelectedTextForPlayback(DBAbstraction db, List < String > selectedEventIds, String nodeId, int nodeSeqNum, String developerGroupId) throws DBAbstractionException
	{
		//create a playback session to the latest event in the passed in node 
		PlaybackSession session = new PlaybackSession(db, developerGroupId, nodeId, nodeSeqNum);

		//holds all of the selected events and others that were touching the 
		//selected ones at some point
		Set < String > relevantEventIds = session.findEventIdsRelevantToTheSelectedEvents(selectedEventIds);
		
		//store the selected and relevant events in the filter
		session.getFilter().getSelectedAndRelevantEventIds().addAll(relevantEventIds);		

		//filter the events
		session.startFilteringEventsv2();

		//add it to the map of all playback sessions
		currentPlaybackSessions.put(session.getId(), session);
		
		//return the session id to the proxy
		return session.getId();
	}
	
	/**
	 * Gets a playback session 
	 */
	public PlaybackSession getPlaybackSession(String id)
    {
    		return currentPlaybackSessions.get(id);
    }

	/*
	 * Gets all the playback sessions for operations that need to query all of 
	 * them. For example, when someone requests the storyboards page we want to
	 * show the user all of the storyboards in all of the playback sessions, not 
	 * just one database's worth of storyboards.
	 */
	public List < PlaybackSession > getAllPlaybackSessions()
    {
    		return new ArrayList < PlaybackSession >(currentPlaybackSessions.values());
    }

	/**
	 * Creates a new playback session 
	 */
	public String addPlaybackSession(DBAbstraction db, String developerGroupId) throws DBAbstractionException
	{
		//create a new (mostly empty) playback session
		PlaybackSession session = new PlaybackSession(db, developerGroupId);
		
		//store it in the map of all sessions
		currentPlaybackSessions.put(session.getId(), session);

		//return the id of the new playback session
		return session.getId();
	}	
	
	/**
	 * Deletes a playbacksession for currentPlaybackSessions
	 */
	public void deletePlaybackSession(PlaybackSession session)
	{
		currentPlaybackSessions.remove(session.getId());
	}

	//getter/setter
    public StorytellerCore getCore()
    {
    		return core;
    }
    public void setCore(StorytellerCore server)
    {
    		this.core = server;
    }
}