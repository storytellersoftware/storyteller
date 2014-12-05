package playback.handler;


import playback.PlaybackSessionServer;
import core.StorytellerCore;
import httpserver.HTTPException;
import httpserver.HTTPHandler;
import httpserver.HTTPHandlerFactory;
import httpserver.HTTPRequest;

public class StorytellerHandlerFactory extends HTTPHandlerFactory 
{
	private PlaybackSessionServer sessionManager;

	public StorytellerHandlerFactory(PlaybackSessionServer sessionManager)
	{
		this.setSessionManager(sessionManager);
	}

	@Override
	public HTTPHandler determineHandler(String pathSegment, HTTPRequest request) throws HTTPException 
	{
		HTTPHandler retVal;
		
		if (checkIfEquals(pathSegment, "clip", request))
		{
			retVal = new ClipHandler(request, getSessionManager());
		}
		else if (checkIfEquals(pathSegment, "developer", request))
		{
			retVal = new DeveloperHandler(request, getSessionManager());
		}
		else if (checkIfEquals(pathSegment, "node", request))
		{
			retVal = new NodeHandler(request, getSessionManager());
		}
		else if (checkIfEquals(pathSegment, "playback", request))
		{
			retVal = new PlaybackHandler(request, getSessionManager());
		}
		else if (checkIfEquals(pathSegment, "storyboard", request))
		{
			retVal = new StoryboardHandler(request, getSessionManager());
		}
//		else if (checkIfEquals(pathSegment, "filesystem" , request))
//		{
//			retVal = new FileSystemHandler(request, getSessionManager());
//		}
//		else if (checkIfEquals(pathSegment, "event", request))
//		{
//			retVal = new EventHandler(request, getSessionManager());
//		}
		else
		{
			retVal = new FileHandler(request, getSessionManager());
		}
		
		return retVal;
	}

	public PlaybackSessionServer getSessionManager()
	{
		return sessionManager;
	}

	public void setSessionManager(PlaybackSessionServer sessionManager)
	{
		this.sessionManager = sessionManager;
	}
}
