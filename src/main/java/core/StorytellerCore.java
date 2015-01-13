package core;

import core.data.DBAbstraction;
import core.data.DBAbstractionException;
import core.data.DBFactory;
import core.data.SQLiteDatabase;
import httpserver.HTTPServer;
import ide.IDEServer;
import merge.MergeServer;
import playback.PlaybackSessionServer;
import playback.handler.StorytellerHandlerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import ide.IDEState;
//import ide.StoryTellerEventRenderer;

/**
 * The StorytellerCore is used to create the independent servers (IDE, PlayBack,
 * Merge) and to act as a repository for the databases that the servers may need.
 * The constructor will create the requested servers. After that it will handle
 * requests from the servers, primarily for data from one or more storyteller
 * databases. In addition, there are some helpers that are useful to more than
 * one server. (is this true???)
 * -----------------------------------------------
 * |            |         http server            |
 * |            |--------------------------------|
 * | ide server | playback server | merge server |
 * |---------------------------------------------|
 * |                    core                     |
 * |      -------------       ------------       |
 * |      | databases |       | entities |       |
 * |      -------------       ------------       |
 * -----------------------------------------------
 */
public class StorytellerCore {
    //references to the servers. One server will occasionally need to ask
    //another to do something. For example, if some text is selected from the ide
    //for playback, information about the selected text needs to get to the
    //playback server. The core will take care of the notification.
    private IDEServer ideServer;
    private HTTPServer httpServer;
    private MergeServer mergeServer;
    //this is used so that the core can request new playback sessions from selected text
    private PlaybackSessionServer playbackServer = null;

    // Playback proxy's port
    public static final int PLAYBACK_PROXY_PORT = 4444;

    // information about the HTTP server
    public static final String SERVER_NAME = "The Magnificent StorytellerServer";
    public static final String SERVER_VERSION = "0.3";
    public static final String SERVER_ETC = "now in Glorious Extra Color, with Handlers!";

    //map of open databases
    private Map<String, DBAbstraction> openDatabases = new HashMap<String, DBAbstraction>();

    //factory that can create different types of db's (currently there is only
    //a file based SQLite db but there may be others in the future). The server
    //should not know about specific types of db's just what an abstract db
    //(DBAbstraction) can do. Whoever creates the server will pass in a factory
    //that can create the correct type of db.
    private DBFactory dbFactory;

    /**
     * Creates a core object with the requested servers
     */
    public StorytellerCore(DBFactory dbf, boolean withIDEServer, boolean withPlaybackServer, boolean withMergeServer) {
        //store the db factory (for creating databases when needed)
        setDbFactory(dbf);

        //should the core set up an IDE server?
        if (withIDEServer) {
            //create a thread to listen for IDE data
            ideServer = new IDEServer(this);
            Thread ideThread = new Thread(ideServer);

            //start the ide server
            ideThread.start();

            System.out.println("Starting IDE server");
        }

        //should the core set up a playback server?
        if (withPlaybackServer) {
            //create a thread to handle playbacks
            httpServer = new HTTPServer(PLAYBACK_PROXY_PORT, SERVER_NAME, SERVER_VERSION, SERVER_ETC);
            playbackServer = new PlaybackSessionServer(this);
            httpServer.setHandlerFactory(new StorytellerHandlerFactory(playbackServer));
            Thread playbackThread = new Thread(httpServer);

            //start the playback server
            playbackThread.start();

            System.out.println("Starting Playback server");
        }

        //should the core set up a merge server?
        if (withMergeServer) {
            //create a thread to handle merge requests
            mergeServer = new MergeServer(this);
            Thread mergeThread = new Thread(mergeServer);

            //start the merge server
            mergeThread.start();

            System.out.println("Starting Merge server");
        }
    }

    /**
     * Used to create a reference to a database object. These will be cached in
     * case more than one server wants to use a db (this is likely if you are
     * editing code and showing playbacks)
     */
    public DBAbstraction createDatabaseAbstraction(String pathToFile) throws DBAbstractionException {
        //a reference to a db abstraction
        DBAbstraction db = null;

        //if there is already a db abstraction that has been created previously
        if (openDatabases.containsKey(pathToFile)) {
            //retrieve the existing one
            db = openDatabases.get(pathToFile);
        } else //there has not been one created yet
        {
            //create a database using the factory
            db = getDbFactory().createDatabaseAbstraction(pathToFile);

            //store it for later use
            openDatabases.put(pathToFile, db);
        }

        //return the db abstraction
        return db;
    }

    public void closeAndDeleteDatabase(DBAbstraction db) throws DBAbstractionException {
        //close the database
        closeDatabaseAbstraction(db);

        //TODO make a method in the ??? to delete the file so that we don't have to cast here
        //now remove the file
        File dbFile = new File(((SQLiteDatabase) db).getPathToFile());

        //if the file is found
        if (dbFile.exists()) {
            //remove it
            dbFile.delete();
        }
    }

    /**
     * Close a specific database
     */
    public void closeDatabaseAbstraction(DBAbstraction db) throws DBAbstractionException {
        //if the database is present
        if (openDatabases.containsValue(db)) {
            //go through all the keys
            for (String key : openDatabases.keySet()) {
                //if we have found the db
                if (openDatabases.get(key).equals(db)) {
                    //remove it
                    openDatabases.remove(key);

                    //stop looking
                    break;
                }
            }

            //close the database
            db.close();
        }
    }

    /**
     * To clean up the core (in particular all the open databases)
     */
    public void closeAllDatabaseAbstractions() throws DBAbstractionException {
        //for each database in the collection of open dbs
        for (DBAbstraction db : openDatabases.values()) {
            //close the db
            db.close();
        }
    }

    /**
     * Return a list of all open databases. This will be used whenever we want
     * to do an all open project operation like finding all the storyboards in
     * all the open databases.
     */
    public List<DBAbstraction> getAllOpenDatabases() {
        return new ArrayList<DBAbstraction>(openDatabases.values());
    }

    public DBFactory getDbFactory() {
        return dbFactory;
    }

    public void setDbFactory(DBFactory dbFactory) {
        this.dbFactory = dbFactory;
    }

    /**
     * Used when the IDE would like to request that a new selected text playback
     * session be created.
     */
    public String sendSelectedTextToPlaybackServer(DBAbstraction db, List<String> selectedEventIds, String nodeId, String developerGroupId) throws DBAbstractionException {
        //ask the playback server to create a new playback session that holds
        //on to the selected events
        return playbackServer.receiveSelectedTextForPlayback(db, selectedEventIds, nodeId, -1, developerGroupId);
    }

    /**
     * Used when the IDE would like to request a plain old playback session
     */
    public String sendRequestForPlaybackToPlaybackServer(DBAbstraction db, String developerGroupId) throws DBAbstractionException {
        //ask the server to create a new playback session for a plain old playback
        return playbackServer.addPlaybackSession(db, developerGroupId);
    }
}
