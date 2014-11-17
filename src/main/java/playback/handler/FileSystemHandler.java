//if we do put this back remember to update the StorytellerHandlerFactory's 
//determineHandler() method

//package playback.handler;
//
//import ide.IDEServerException;
//import httpserver.HTTPException;
//import httpserver.HTTPRequest;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import playback.PlaybackSessionServer;
//
//import core.Constants;
//import core.StorytellerCore;
//import core.data.DBAbstractionException;
//import core.entities.Directory;
//import core.entities.Document;
//import core.services.json.DeJSONiffy;
//import core.services.json.JSONiffy;
//
//
//public class FileSystemHandler extends StorytellerHTTPHandler
//{
//
//	public FileSystemHandler(HTTPRequest request, PlaybackSessionServer sessionManager) throws HTTPException
//	{
//		super(request, sessionManager);
//		//****** Documents
//		
//		//Create
//		addPOST("/document/new" , "postCreateDocument");
//		
//		//Retrieve
//		addGET("/document/{String}", "getDocument");
//		
//		//Update
//		addPOST("/document/{String}/update" , "postUpdateDocument");
//		
//		//Delete
//		addGET("document/{String}/delete", "getDocumentDelete");
//		
//		//****** Directories
//		
//		//Create
//		addPOST("directory/new" , "postCreateDirectory");
//		
//		//Retrieve
//		addGET("/directory/{String}", "getDirectory");
//		
//		//Update
//		addPOST("directory/{String}/update", "postUpdateDirectory");
//		
//		//Destroy
//		addGET("/directory/{String}/delete", "getDirectoryDelete");
//	}
//	
//	//TODO
//		// JSONCONSTANTS does not have a Directory!!
//	/** 
//	 * called on /directory/{String}/update
//	 * 
//	 * Attempts to update the directory
//	 * 
//	 */
//	public void postUpdateDirectory(String id)
//	{
//		message(501, "not yet implemented!!");
//	}
//	
//	//TODO
//	// JSONCONSTANTS does not have a Directory!!
//	/** 
//	 * Called on /directory/new
//	 * 
//	 * Attempts to create a new directory
//	 */
//	public void postCreateDirectory()
//	{
//		message(501, "Not yet implemented");
//	}
//	/**
//	 * Called on /document/{String}/update
//	 * 
//	 * Attempts to update the information about the document
//	 * 
//	 * @param id ID of the document to update
//	 */
//	public void postUpdateDocument(String id)
//	{
//		try
//		{
//			Document document = getPlaybackSessionServer().getCore().getLocalDatabase().getDocument(id);
//			if(document == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.DOCUMENTS));
//			Document updateDocument = DeJSONiffy.document(input);
//			document.update(updateDocument);
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.DOCUMENT_ID, document.getId());
//			setResponseCode(200);
//			setJSON(response);
//			
//		}
//		catch(DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//	/**
//	 * Called on /document/new
//	 * 
//	 * Attempts to create a new document
//	 * 
//	 */
//	public void postCreateDocument()
//	{
//		try
//		{
//			JSONObject input = new JSONObject(getRequest().getPostData().get(Constants.DOCUMENTS));
//			Document document = DeJSONiffy.document(input);
//			getPlaybackSessionServer().getCore().getLocalDatabase().insertDocument(document);
//			
//			JSONObject response = new JSONObject();
//			response.put(Constants.DOCUMENT_ID, document.getId());
//			setResponseCode(201);
//			setJSON(response);
//		}
//		catch(JSONException | DBAbstractionException e)
//		{
//			error(418, MALFORMED_INPUT_ERROR, e);
//		}
//	}
//
//
//	/**
//	 * Called from GET /document/{String}
//	 * 
//	 * Attempts to retrieve a document with the given ID
//	 * 
//	 * @param documentID
//	 *            ID of the document to retrieve
//	 */
//	public void getDocument(String documentID)
//	{
//		try
//		{
//			Document document = getPlaybackSessionServer().getCore().getLocalDatabase().getDocument(
//					documentID);
//			if (document == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			JSONObject documentJSON = JSONiffy.toJSON(document);
//			setJSON(documentJSON);
//		}
//		catch (DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//
//	/**
//	 * called on GET /directory/{String}
//	 * 
//	 * Attempts to retrieve a directory from the database with the given ID
//	 * 
//	 * @param directoryID
//	 *            ID of the directory to retrieve
//	 */
//	public void getDirectory(String directoryID)
//	{
//		try
//		{
//			Directory directory = getPlaybackSessionServer().getCore().getLocalDatabase().getDirectory(
//					directoryID);
//			if (directory == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			JSONObject directoryJSON = JSONiffy.toJSON(directory);
//			setJSON(directoryJSON);
//		}
//		catch (DBAbstractionException | JSONException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//
//	/**
//	 * Called on GET /document/{String}/delete
//	 * 
//	 * Attempts to delete the document of the given ID from the database
//	 * 
//	 * @param documentID
//	 *            ID of the document to be deleted
//	 */
//	public void getDocumentDelete(String documentID)
//	{
//		try
//		{
//			Document document = getPlaybackSessionServer().getCore().getLocalDatabase().getDocument(documentID);
//			if (document == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			getPlaybackSessionServer().getCore().getLocalDatabase().deleteDocument(document);
//			message(200, "Document Deleted");
//		}
//		catch (DBAbstractionException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//
//
//	/**
//	 * Called on /directory/{String}/delete
//	 * 
//	 * Attempts to delete the directory of the given ID from the database
//	 * 
//	 * 
//	 * @param directoryID
//	 *            ID of the directory to be deleted
//	 */
//	public void getDirectoryDelete(String directoryID)
//	{
//		try
//		{
//			Directory directory = getPlaybackSessionServer().getCore().getLocalDatabase().getDirectory(directoryID);
//			
//			if (directory == null)
//			{
//				message(418, INVALID_ID_ERROR);
//				return;
//			}
//			getPlaybackSessionServer().getCore().getLocalDatabase().deleteDirectory(directory);
//			message(200, "Directory Deleted");
//		}
//		catch (DBAbstractionException e)
//		{
//			error(500, EXCEPTION_ERROR, e);
//		}
//	}
//}
