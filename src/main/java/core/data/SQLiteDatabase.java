package core.data;

import core.entities.*;
import core.events.*;
import core.services.json.DeJSONiffy;
import org.json.JSONException;
import playback.PlaybackFilter;
import util.Pair;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.*;
import java.util.*;
import java.util.Date;


public class SQLiteDatabase implements DBAbstraction {
    //path to the database file to connect to
    protected String pathToFile;

    //JDBC connection to the database
    protected Connection connection;

    //sqlite file extension for all dbs
    public final static String DB_EXTENSION_NAME = ".ali";

    /**
     * Used to build a sqlite database abstraction for storyteller.
     *
     * @param path This is the path to the file that holds
     *             the database
     */
    public SQLiteDatabase(String path) throws DBAbstractionException {
        //check the filename has the right extension
        if (path.endsWith(DB_EXTENSION_NAME)) {
            //open a connection to a db so that this server can access the db
            open(path);
        } else //incorrect file name
        {
            throw new DBAbstractionException("The database file name must end with " + DB_EXTENSION_NAME + " : " + path);
        }
    }

    //protected helpers to perform queries
    protected void executeWithNoResults(PreparedStatement statement) throws DBAbstractionException {
        try {
            // set timeout to 30 sec.
            statement.setQueryTimeout(30);

            //execute the query
            statement.executeUpdate();

            //close the statement
            statement.close();
        } catch (SQLException e) {
            //if something bad happened in the sql, wrap up the
            //exception and pass it on up
            throw new DBAbstractionException(e);
        }
    }

    protected ResultSet executeWithResults(PreparedStatement statement) throws DBAbstractionException {
        ResultSet results;

        try {
            // set timeout to 30 sec.
            statement.setQueryTimeout(30);

            //execute the query and get back a generic set of results
            results = statement.executeQuery();
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }

        return results;
    }


    @Override
    public void open(String path) throws DBAbstractionException {
        try {
            //load the sqlite-JDBC driver using the class loader
            Class.forName("org.sqlite.JDBC");

            //set the path to the sqlite database file
            setPathToFile(path);

            //create a database connection, will open the sqlite db if it
            //exists and create a new sqlite database if it does not exist
            setConnection(DriverManager.getConnection("jdbc:sqlite:" + getPathToFile()));

            //create the tables (if they do not already exist)
            createTables();
        } catch (ClassNotFoundException e) {
            throw new DBAbstractionException("Problem with Class.forName in SQLiteDatabase");
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }


    @Override
    public void close() throws DBAbstractionException {
        try {
            //close the JDBC connection
            getConnection().close();
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    /**
     * Creates the tables for the databases
     */
    protected void createTables() throws DBAbstractionException {
        //create each of the tables
        createEventsTable();
        createFutureRelatedCombinatorialEventsHasEventsTable();
        createManualConflictsHasEventsTable();
        createNodesTable();
        createDevelopersTable();
        createDeveloperGroupsTable();
        createDevelopersBelongToDeveloperGroupsTable();
        createDocumentsTable();
        createDirectoriesTable();
        createClipsTable();
        createStoryboardsTable();
        createClipCommentsTable();
        createClipBelongsToStoryboardTable();
        createEventsBelongToClipTable();
        createProjectsTable();
    }

    protected void createEventsTable() throws DBAbstractionException {
        /*
		Event Schema
			CREATE TABLE IF NOT EXISTS Events (
				id TEXT PRIMARY_KEY
				timestamp INTEGER,
				created_under_node_id TEXT PRIMARY_KEY,
				created_by_dev_group_id TEXT,
				node_sequence_num INTEGER PRIMARY_KEY,
				event_data TEXT,
				previous_neighbor_event_id TEXT,
				sequentially_before_event_id TEXT,
				event_type TEXT,
				sort_order TEXT, 
				paste_parent_id TEXT,
				document_id TEXT,
				directory_id TEXT,
				new_name TEXT,
				old_name TEXT,
				parent_directory_id TEXT,
				new_parent_directory_id TEXT,
				sequentially_before_node_id TEXT,
				first_node_to_merge_id TEXT,
				second_node_to_merge_id TEXT,
				base_resolution_id TEXT
			)
		 */
        //build up the sql
        StringBuilder builder = new StringBuilder();

        builder.append("CREATE TABLE IF NOT EXISTS Events ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("timestamp INTEGER, ");
        builder.append("created_under_node_id TEXT PRIMARY_KEY, ");
        builder.append("created_by_dev_group_id TEXT, ");
        builder.append("node_sequence_num INTEGER PRIMARY_KEY, ");
        builder.append("event_data TEXT, ");
        builder.append("sort_order TEXT, ");
        builder.append("previous_neighbor_event_id TEXT, ");
        builder.append("sequentially_before_event_id TEXT, ");
        builder.append("event_type TEXT, ");
        builder.append("paste_parent_id TEXT, ");
        builder.append("document_id TEXT, ");
        builder.append("directory_id TEXT, ");
        builder.append("new_name TEXT, ");
        builder.append("old_name TEXT, ");
        builder.append("parent_directory_id TEXT, ");
        builder.append("new_parent_directory_id TEXT, ");
        builder.append("sequentially_before_node_id TEXT, ");
        builder.append("first_node_to_merge_id TEXT, ");
        builder.append("second_node_to_merge_id TEXT, ");
        builder.append("base_resolution_id TEXT) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }


    protected void createFutureRelatedCombinatorialEventsHasEventsTable() throws DBAbstractionException {
        //This table is a join table for all combinatorial events that have a define impact on the future
        //Automatic Conflict Events and Manual Conflict Events

		/*
 		FutureRelatedCombinatorialEventsHasEventsTable Schema
		CREATE TABLE IF NOT EXISTS FutureRelatedCombinatorialEventsHasEventsTable (
			ce_sequence_num INTEGER PRIMARY_KEY,
			ce_created_under_node_id TEXT PRIMARY_KEY,
			event_order_in_conflict INTEGER PRIMARY_KEY, 			//could maybe not be a primary key because the same event will never be played twice
			event_sequence_num INTEGER PRIMARY_KEY,
			event_created_under_node_id TEXT PRIMARY_KEY
		)
		 */
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS FutureRelatedCombinatorialEventsHasEventsTable ( ");
        builder.append("ce_sequence_num INTEGER PRIMARY_KEY, ");                    //the sequence number of the AutomaticConflictEvent (ACE) (referring to Events table)
        builder.append("ce_created_under_node_id TEXT PRIMARY_KEY, ");                //the id of the node that the ACE belongs to
        builder.append("event_order_in_conflict INTEGER PRIMARY_KEY, ");            //the order in which the event gets played as a part of the conflict
        builder.append("event_sequence_num INTEGER PRIMARY_KEY, ");                    //the sequence number of one of the events in Events that is a part of this ACE
        builder.append("event_created_under_node_id TEXT PRIMARY_KEY) ");            //the id of the node of one of the events in Events that is a part of this ACE

        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createManualConflictsHasEventsTable() throws DBAbstractionException {
		/*
 		ManualConflictsHasEvents Schema
		CREATE TABLE IF NOT EXISTS ManualConflictsHasEvents (
			mce_sequence_num INTEGER PRIMARY_KEY,
			mce_created_under_node_id TEXT PRIMARY_KEY,
			event_order_in_conflict INTEGER PRIMARY_KEY,
			event_sequence_num INTEGER PRIMARY_KEY,
			event_created_under_node_id TEXT PRIMARY_KEY,
			first_or_second_list INTEGER PRIMARY_KEY
		)
		 */
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ManualConflictsHasEvents ( ");
        builder.append("mce_sequence_num INTEGER PRIMARY_KEY, ");                    //the sequence number of the ManualConflictEvent (MCE)
        builder.append("mce_created_under_node_id TEXT PRIMARY_KEY, ");                //the id of the node that the MCE belongs to
        builder.append("event_order_in_conflict INTEGER PRIMARY_KEY, ");            //the order in which the event gets played as a part of the conflict
        builder.append("event_sequence_num INTEGER PRIMARY_KEY, ");                    //the sequence number of one of the events in Events that is a part of this MCE
        builder.append("event_created_under_node_id TEXT PRIMARY_KEY, ");            //the id of the node of one of the events in Events that is a part of this MCE
        builder.append("first_or_second_list INTEGER PRIMARY_KEY) ");                //Tells us which list (first or second) this event is for the MCE

        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createNodesTable() throws DBAbstractionException {
		/*
 		Nodes Schema
		CREATE TABLE IF NOT EXISTS Nodes (
			id TEXT PRIMARY_KEY,
			timestamp INTEGER,
			created_under_node_id TEXT,
			created_by_dev_group_id TEXT,
			project_id TEXT,
			name TEXT,
			description TEXT,
			node_lineage_number INTEGER,
			node_type TEXT
		)
		 */
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS Nodes ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("timestamp INTEGER, ");
        builder.append("created_under_node_id TEXT, ");
        builder.append("created_by_dev_group_id TEXT, ");
        builder.append("project_id TEXT, ");
        builder.append("name TEXT, ");
        builder.append("description TEXT, ");
        builder.append("node_lineage_number INTEGER, ");
        builder.append("node_type TEXT ) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createProjectsTable() throws DBAbstractionException {
		/*
 		Projects Schema
		CREATE TABLE IF NOT EXISTS Projects (
			id TEXT PRIMARY_KEY,
			timestamp INTEGER,
			created_by_dev_group_id TEXT,
			project_name TEXT,
		)
		 */
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS Projects ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("timestamp INTEGER, ");
        builder.append("created_by_dev_group_id TEXT, ");
        builder.append("project_name TEXT UNIQUE)");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createDevelopersTable() throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS Developers ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("timestamp INTEGER, ");
        builder.append("created_under_node_id TEXT, ");
        builder.append("created_by_dev_group_id TEXT, ");
        builder.append("email TEXT UNIQUE, ");
        builder.append("first_name TEXT, ");
        builder.append("last_name TEXT ) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createDeveloperGroupsTable() throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS DeveloperGroups ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("timestamp INTEGER, ");
        builder.append("created_under_node_id TEXT, ");
        builder.append("created_by_dev_group_id TEXT ) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createDevelopersBelongToDeveloperGroupsTable() throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS DevelopersBelongToDeveloperGroups ( ");
        builder.append("timestamp INTEGER, ");
        builder.append("joined_under_node_id TEXT, ");
        builder.append("developer_group_id_that_made_this_join TEXT, ");
        builder.append("developer_id TEXT PRIMARY_KEY, ");
        builder.append("developer_group_id TEXT PRIMARY_KEY) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createDocumentsTable() throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS Documents ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("timestamp INTEGER, ");
        builder.append("created_under_node_id TEXT, ");
        builder.append("created_by_dev_group_id TEXT, ");
        builder.append("parent_directory_id TEXT ) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createDirectoriesTable() throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS Directories ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("timestamp INTEGER, ");
        builder.append("created_under_node_id TEXT, ");
        builder.append("created_by_dev_group_id TEXT, ");
        builder.append("parent_directory_id TEXT ) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createClipsTable() throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS Clips ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("timestamp INTEGER, ");
        builder.append("created_under_node_id TEXT, ");
        builder.append("created_by_dev_group_id TEXT, ");
        builder.append("name TEXT, ");
        builder.append("description TEXT, ");
        builder.append("filter_string TEXT, ");
        builder.append("playback_node_id TEXT) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createStoryboardsTable() throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS Storyboards ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("timestamp INTEGER, ");
        builder.append("created_under_node_id TEXT, ");
        builder.append("created_by_dev_group_id TEXT, ");
        builder.append("name TEXT, ");
        builder.append("description TEXT) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createClipCommentsTable() throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ClipComments ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("timestamp INTEGER, ");
        builder.append("created_under_node_id TEXT, ");
        builder.append("created_by_dev_group_id TEXT, ");
        builder.append("text TEXT, ");
        builder.append("display_comment_event_id TEXT, ");
        builder.append("clip_id TEXT, ");
        builder.append("start_highlight_event_id TEXT, ");
        builder.append("end_highlight_event_id TEXT) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createClipBelongsToStoryboardTable() throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ClipStoryboardJoinTable ( ");
        builder.append("id TEXT PRIMARY_KEY, ");
        builder.append("clip_id TEXT, ");
        builder.append("storyboard_id TEXT, ");
        builder.append("clip_position_in_storyboard INTEGER) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    protected void createEventsBelongToClipTable() throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS EventClipJoinTable ( ");
        builder.append("event_id TEXT PRIMARY_KEY, ");
        builder.append("clip_id TEXT PRIMARY_KEY) ");

        //execute the query
        try {
            executeWithNoResults(connection.prepareStatement(builder.toString()));
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    //inserts
    @Override
    public void insertEvent(InsertEvent event) throws DBAbstractionException {
        StringBuilder builder = new StringBuilder();
        //build up the sql
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, event_data, sort_order, previous_neighbor_event_id, ");
        builder.append("sequentially_before_event_id, event_type, paste_parent_id, document_id ) ");
        builder.append("VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            //ps.setString(6, (event.getEventData().equals("'")?"''":event.getEventData()));			//event data with a check for a single quote.  If the event data is a single quote, escape it
            ps.setString(6, event.getEventData());            //event data with a check for a single quote.  If the event data is a single quote, escape it
            ps.setString(7, event.getSortOrder());            //sort order
            ps.setString(8, event.getPreviousNeighborEventId() == null ? "null" : event.getPreviousNeighborEventId()); //previous_neighbor_event_id
            ps.setString(9, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(10, event.getEventType()); //event_type
            ps.setString(11, event.getPasteParentId()); //paste_parent_id
            ps.setString(12, event.getDocumentId()); //document_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(DeleteEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, previous_neighbor_event_id, sequentially_before_event_id, ");
        builder.append("event_type, document_id ) ");
        builder.append("VALUES ( ?, ?, ?, ?, ?, ?, ?, ? ,?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getPreviousNeighborEventId() == null ? "null" : event.getPreviousNeighborEventId()); //previous_neighbor_event_id
            ps.setString(7, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(8, event.getEventType()); //event_type
            ps.setString(9, event.getDocumentId()); //document_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(CreateDocumentEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, node_sequence_num, ");
        builder.append("sequentially_before_event_id, event_type, parent_directory_id, new_name, document_id ) ");
        builder.append("VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type
            ps.setString(8, event.getParentDirectoryId()); //parent_directory_id
            ps.setString(9, event.getDocumentNewName()); //document_new_name
            ps.setString(10, event.getDocumentId()); //directory_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(RenameDocumentEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, sequentially_before_event_id, event_type, ");
        builder.append("new_name, old_name, document_id, parent_directory_id ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type
            ps.setString(8, event.getDocumentNewName()); //document_new_name
            ps.setString(9, event.getDocumentOldName()); //document_old_name
            ps.setString(10, event.getDocumentId()); //document_id
            ps.setString(11, event.getParentDirectoryId()); //parent_directory_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(MoveDocumentEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, sequentially_before_event_id, event_type, ");
        builder.append("parent_directory_id, new_parent_directory_id, document_id ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type)
            ps.setString(8, event.getParentDirectoryId()); //parent_directory_id
            ps.setString(9, event.getNewParentDirectoryId()); //new_parent_directory_id
            ps.setString(10, event.getDocumentId()); //document_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(DeleteDocumentEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, sequentially_before_event_id, event_type, ");
        builder.append("old_name, document_id, parent_directory_id ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type
            ps.setString(8, event.getDocumentOldName()); //old_name
            ps.setString(9, event.getDocumentId()); //document_id
            ps.setString(10, event.getParentDirectoryId()); //parent_directory_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(CreateDirectoryEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, sequentially_before_event_id, event_type, ");
        builder.append("parent_directory_id, new_name, directory_id ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp)
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type
            ps.setString(8, event.getParentDirectoryId()); //parent_directory_id
            ps.setString(9, event.getDirectoryNewName()); //directory_new_name
            ps.setString(10, event.getDirectoryId()); //directory_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }

    }

    @Override
    public void insertEvent(RenameDirectoryEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, sequentially_before_event_id, ");
        builder.append("event_type, new_name, old_name, directory_id, parent_directory_id ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type
            ps.setString(8, event.getDirectoryNewName()); //directory_new_name
            ps.setString(9, event.getDirectoryOldName()); //directory_old_name
            ps.setString(10, event.getDirectoryId()); //directory_id
            ps.setString(11, event.getParentDirectoryId()); //parent_directory_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(MoveDirectoryEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, sequentially_before_event_id, event_type, ");
        builder.append("parent_directory_id, new_parent_directory_id, directory_id ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type
            ps.setString(8, event.getParentDirectoryId()); //parent_directory_id
            ps.setString(9, event.getNewParentDirectoryId()); //new_parent_directory_id
            ps.setString(10, event.getDirectoryId()); //directory_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(DeleteDirectoryEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, sequentially_before_event_id, event_type, ");
        builder.append("old_name, directory_id, parent_directory_id ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type
            ps.setString(8, event.getDirectoryOldName()); //old_name
            ps.setString(9, event.getDirectoryId()); //directory_id
            ps.setString(10, event.getParentDirectoryId()); //parent_directory_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(OpenNodeEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, sequentially_before_event_id, ");
        builder.append("event_type, sequentially_before_node_id ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type
            ps.setString(8, event.getSequentiallyBeforeNodeID()); //sequentially_before_node_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(CloseNodeEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, sequentially_before_event_id, event_type ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(MergeEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Events ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("node_sequence_num, sequentially_before_event_id, event_type, ");
        builder.append("first_node_to_merge_id, second_node_to_merge_id, sequentially_before_node_id ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, event.getId());
            ps.setLong(2, event.getTimestamp().getTime()); //timestamp
            ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
            ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
            ps.setString(7, event.getEventType()); //event_type
            ps.setString(8, event.getFirstNodeToMergeID()); //event_type
            ps.setString(9, event.getSecondNodeToMergeID()); //event_type
            ps.setString(10, event.getSequentiallyBeforeNodeID()); //sequentially_before_node_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertEvent(AutomaticConflictEvent event) throws DBAbstractionException {
		/*
		List<PreparedStatement> sqlQueries = new ArrayList<PreparedStatement>();
		//build up the sql
		StringBuilder builder = new StringBuilder();

		builder.append("INSERT INTO Events ( ");
		builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
		builder.append("node_sequence_num, sequentially_before_event_id, event_type, ");
		builder.append("document_id ) ");
		builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		
		try 
		{
			PreparedStatement ps = connection.prepareStatement(builder.toString());
			
			ps.setString(1, event.getId());
			ps.setLong(2, event.getTimestamp().getTime()); //timestamp
			ps.setString(3, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId()); //created_under_node_id
			ps.setString(4, event.getCreatedByDevGroupId()); //created_by_dev_group_id
			ps.setInt(5, event.getNodeSequenceNum()); //node_sequence_num
			ps.setString(6, event.getSequentiallyBeforeEventId() == null ? "null" : event.getSequentiallyBeforeEventId()); //sequentially_before_event_id
			ps.setString(7, event.getEventType()); //event_type
			ps.setString(8, event.getDocumentId()); //document_id
	
			sqlQueries.add(ps);
			builder = new StringBuilder();
	
			//Insert the events into the join table
			for(int i = 0;i<event.getIdsOfEventsInThisBlock().size();i++)
			{
				builder.append("INSERT INTO FutureRelatedCombinatorialEventsHasEventsTable ( ");
				builder.append("ce_sequence_num, ce_created_under_node_id, event_order_in_conflict, ");
				builder.append("event_sequence_num, event_created_under_node_id ) ");
				builder.append("VALUES (?, ?, ?, ?, ?)");
				
				PreparedStatement ps2 = connection.prepareStatement(builder.toString());
				
				ps2.setInt(1, event.getNodeSequenceNum());
				ps2.setString(2, event.getCreatedUnderNodeId() == null ? "null" : event.getCreatedUnderNodeId());
				ps2.setInt(3, i);
				
				//returns an old id that is then broken up into two parts- change to getting the event and pull the seq num and node id
				ps2.setInt(4, StorytellerEvent.getSequenceNumFromId(event.getIdsOfEventsInThisBlock().get(i)));
				ps2.setString(5, StorytellerEvent.getNodeIdFromId(event.getIdsOfEventsInThisBlock().get(i)));
	
				sqlQueries.add(ps2);
				builder = new StringBuilder();
			}
		} 
		catch (SQLException e)
		{
			throw new DBAbstractionException(e);
		}
		
		//execute the query
		executeLargeTransactionWithNoResults(sqlQueries);
		*/
    }


    @Override
    public void insertEvent(ManualConflictEvent event) throws DBAbstractionException {
        throw DBAbstraction.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void insertEvent(ConflictResolutionEvent event) throws DBAbstractionException {
        throw DBAbstraction.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void insertNode(Node node) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Nodes ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("project_id, name, description, node_lineage_number, node_type ) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, node.getId()); //id
            ps.setLong(2, node.getTimestamp().getTime()); //timestamp
            ps.setString(3, node.getCreatedUnderNodeId() == null ? "null" : node.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, node.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(5, node.getProjectId()); //projectId
            ps.setString(6, node.getName()); //name
            ps.setString(7, node.getDescription()); //description
            ps.setInt(8, node.getNodeLineageNumber()); //node_lineage_number
            ps.setString(9, node.getNodeType()); //node_type

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

	/*
		Projects Schema
	CREATE TABLE IF NOT EXISTS Projects (
		id TEXT PRIMARY_KEY,
		timestamp INTEGER,
		created_by_dev_group_id TEXT,
		project_name TEXT,
	)
	 */

    @Override
    public void insertProject(Project newProject) throws DBAbstractionException {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Projects ( ");
        builder.append("id, timestamp, created_by_dev_group_id, project_name) ");
        builder.append("VALUES (?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, newProject.getId()); //id
            ps.setLong(2, newProject.getTimestamp().getTime()); //timestamp
            ps.setString(3, newProject.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(4, sanitizeAlphabeticalInput(newProject.getProjectName())); //created_under_node_id

            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertDeveloper(Developer developer) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Developers ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("email, first_name, last_name) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, developer.getId()); //id
            ps.setLong(2, developer.getTimestamp().getTime()); //timestamp
            ps.setString(3, developer.getCreatedUnderNodeId() == null ? "null" : developer.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, developer.getCreatedByDevGroupId() == null ? "null" : developer.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(5, developer.getEmail()); //email
            ps.setString(6, developer.getFirstName()); //first_name
            ps.setString(7, developer.getLastName()); //last_name

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertDeveloperGroup(DeveloperGroup developerGroup) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO DeveloperGroups ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id) ");
        builder.append("VALUES (?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, developerGroup.getId()); //id
            ps.setLong(2, developerGroup.getTimestamp().getTime()); //timestamp
            ps.setString(3, developerGroup.getCreatedUnderNodeId() == null ? "null" : developerGroup.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, developerGroup.getCreatedByDevGroupId() == null ? "null" : developerGroup.getCreatedByDevGroupId()); //created_by_dev_group_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void joinDeveloperAndDeveloperGroup(Developer developerWhoIsJoiningGroup, DeveloperGroup developerGroupToJoin, String joinedUnderNodeId, String createdByDeveloperGroupId) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO DevelopersBelongToDeveloperGroups ( ");
        builder.append("timestamp, joined_under_node_id, developer_group_id_that_made_this_join, ");
        builder.append("developer_id, developer_group_id) ");
        builder.append("VALUES (?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setLong(1, new Date().getTime()); //timestamp
            ps.setString(2, joinedUnderNodeId == null ? "null" : joinedUnderNodeId); //created_under_node_id
            ps.setString(3, createdByDeveloperGroupId == null ? "null" : createdByDeveloperGroupId); //developer_group_id_that_made_this_join
            ps.setString(4, developerWhoIsJoiningGroup.getId()); //developer_id
            ps.setString(5, developerGroupToJoin.getId()); //developer_group_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public Pair<DeveloperGroup, Developer> addADeveloperAndCreateANewDeveloperGroup(Date creationDate, String firstName, String lastName, String email, String createdUnderNodeId, String createdByDeveloperId) throws DBAbstractionException {
        //create a developer object
        Developer developer = new Developer(creationDate, createdUnderNodeId, createdByDeveloperId, email, firstName, lastName);

        //insert it into the database
        insertDeveloper(developer);

        //make a list of one with the dev's id
        List<Developer> devList = new ArrayList<>();
        devList.add(developer);

        //create the developer group that has only this developer in it
        DeveloperGroup developerGroup = new DeveloperGroup(devList, creationDate, createdUnderNodeId, createdByDeveloperId);

        //insert it into the database
        insertDeveloperGroup(developerGroup);

        //join the developer and the developer group together in the database
        joinDeveloperAndDeveloperGroup(developer, developerGroup, createdUnderNodeId, createdByDeveloperId);

        return new Pair<>(developerGroup, developer);
    }

    @Override
    public void insertDocument(Document document) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Documents ( ");
        builder.append("id, timestamp, created_under_node_id, ");
        builder.append("created_by_dev_group_id, parent_directory_id) ");
        builder.append("VALUES (?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, document.getId()); //id
            ps.setLong(2, document.getTimestamp().getTime()); //timestamp
            ps.setString(3, document.getCreatedUnderNodeId() == null ? "null" : document.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, document.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(5, document.getParentDirectoryId()); //parent_directory_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertDirectory(Directory directory) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Directories ( ");
        builder.append("id, timestamp, created_under_node_id, ");
        builder.append("created_by_dev_group_id, parent_directory_id) ");
        builder.append("VALUES (?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, directory.getId()); //id
            ps.setLong(2, directory.getTimestamp().getTime()); //timestamp
            ps.setString(3, directory.getCreatedUnderNodeId() == null ? "null" : directory.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, directory.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(5, directory.getParentDirectoryId()); //parent_directory_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertClip(Clip clip) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Clips ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("name, description, filter_string, playback_node_id) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, clip.getId()); //id
            ps.setLong(2, clip.getTimestamp().getTime()); //timestamp
            ps.setString(3, clip.getCreatedUnderNodeId() == null ? "null" : clip.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, clip.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(5, encode(clip.getName())); //name
            ps.setString(6, encode(clip.getDescription())); //description
            ps.setString(7, clip.getFilterString()); //filter_string
            ps.setString(8, clip.getPlaybackNodeId()); //playback_node_id


            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertStoryboard(Storyboard storyboard) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO Storyboards ( ");
        builder.append("id, timestamp, created_under_node_id, ");
        builder.append("created_by_dev_group_id, name, description) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, storyboard.getId()); //id
            ps.setLong(2, storyboard.getTimestamp().getTime()); //timestamp
            ps.setString(3, storyboard.getCreatedUnderNodeId() == null ? "null" : storyboard.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, storyboard.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(5, encode(storyboard.getName())); //name
            ps.setString(6, encode(storyboard.getDescription())); //description

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void insertClipComment(ClipComment clipComment) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ClipComments ( ");
        builder.append("id, timestamp, created_under_node_id, created_by_dev_group_id, ");
        builder.append("text, display_comment_event_id, clip_id, start_highlight_event_id, ");
        builder.append("end_highlight_event_id) ");
        builder.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, clipComment.getId()); //id
            ps.setLong(2, clipComment.getTimestamp().getTime()); //timestamp
            ps.setString(3, clipComment.getCreatedUnderNodeId() == null ? "null" : clipComment.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(4, clipComment.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(5, encode(clipComment.getText())); //text with the ' safely escaped
            ps.setString(6, clipComment.getDisplayCommentEventId()); //display_comment_event_id
            ps.setString(7, clipComment.getClipId()); //clip_id
            ps.setString(8, clipComment.getStartHighlightEventId()); //start_highlight_event_id
            ps.setString(9, clipComment.getEndHighlightEventId()); //end_highlight_event_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void joinClipAndStoryboard(Clip clip, Storyboard storyboard, int pos) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ClipStoryboardJoinTable ( ");
        builder.append("clip_id, storyboard_id, clip_position_in_storyboard) ");
        builder.append("VALUES (?, ?, ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, clip.getId()); //clip_id
            ps.setString(2, storyboard.getId()); //storyboard_id
            ps.setInt(3, pos); //clip_position_in_storyboard

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public void unjoinClipAndStoryboard(Clip clip, Storyboard storyboard) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ClipStoryboardJoinTable ");
        builder.append("WHERE clip_id = ? AND storyboard_id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, clip.getId()); //clip_id
            ps.setString(2, storyboard.getId()); //storyboard_id

            //execute the query
            executeWithNoResults(ps);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    //retrieves
    @Override
    public StorytellerEvent getEvent(String id) throws DBAbstractionException {
        StorytellerEvent retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE id = ? ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, id);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one event with the passed in id
            if (results.next()) {
                //create the appropriate event type from the result set
                retVal = convertResultSetToStorytellerEvent(results);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public StorytellerEvent getEvent(String nodeId, int sequenceNumber) throws DBAbstractionException {
        StorytellerEvent retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id = ? ");
        builder.append("AND node_sequence_num = ? ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, nodeId);
            ps.setInt(2, sequenceNumber);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one event with the passed in id
            if (results.next()) {
                //create the appropriate event type from the result set
                retVal = convertResultSetToStorytellerEvent(results);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    public DeleteEvent getDeleteEventForAnInsertEvent(String nodeId, int seqNumber, InsertEvent event) throws DBAbstractionException {
        DeleteEvent retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE event_type = ? ");
        builder.append("AND created_under_node_id = ? ");
        builder.append("AND node_sequence_num <= ? ");
        builder.append("AND previous_neighbor_event_id = ? ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, DeleteEvent.DELETE_EVENT_TYPE);
            ps.setString(2, nodeId);
            ps.setInt(3, seqNumber);
            ps.setString(4, event.getId());

            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one event with the passed in id
            if (results.next()) {
                //create the appropriate event type from the result set
                retVal = (DeleteEvent) convertResultSetToStorytellerEvent(results);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<StorytellerEvent> getAllEventsByIds(List<String> eventIds) throws DBAbstractionException {
        List<StorytellerEvent> retVal = new ArrayList<>();

        //if there are some events
        if (eventIds.size() > 0) {
            //build up the sql
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT * ");
            builder.append("FROM Events ");
            builder.append("WHERE id IN (");
            builder.append(join(eventIds, ", ", true));
            builder.append(") ");

            try {
                Statement statement = connection.createStatement();

                //perform the query
                ResultSet results = statement.executeQuery(builder.toString());

                //there should be only one event with the passed in id
                while (results.next()) {
                    //create the appropriate event type from the result set
                    retVal.add(convertResultSetToStorytellerEvent(results));
                }
            } catch (SQLException ex) {
                throw new DBAbstractionException(ex);
            }
        }
        return retVal;
    }

    @Override
    public List<InsertEvent> getAllInsertEventsByIds(List<String> eventIds) throws DBAbstractionException {
        List<InsertEvent> retVal = new ArrayList<>();

        //if there are some events
        if (eventIds.size() > 0) {
            //build up the sql
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT * ");
            builder.append("FROM Events ");
            builder.append("event_type = '");
            builder.append(InsertEvent.INSERT_EVENT_TYPE);
            builder.append("' ");
            builder.append("WHERE id IN (");
            builder.append(join(eventIds, ", ", true));
            builder.append(") ");
            builder.append("ORDER BY sort_order ");
            try {
                Statement statement = connection.createStatement();

                //perform the query
                ResultSet results = statement.executeQuery(builder.toString());

                //there should be only one event with the passed in id
                while (results.next()) {
                    //create the appropriate event type from the result set
                    retVal.add((InsertEvent) convertResultSetToStorytellerEvent(results));
                }
            } catch (SQLException ex) {
                throw new DBAbstractionException(ex);
            }
        }
        return retVal;
    }

    @Override
    public StorytellerEvent getLastEventInNode(String nodeID) throws DBAbstractionException {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id = ? ");
        builder.append("ORDER BY node_sequence_num DESC ");
        builder.append("LIMIT 1");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, nodeID);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for the one event returned
            if (results.next()) {
                //create the appropriate event type from the result set
                return convertResultSetToStorytellerEvent(results);
            }
            return null;
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

    }

    @Override
    public StorytellerEvent getFirstEventInNode(String nodeID) throws DBAbstractionException {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id = ? ");
        builder.append("ORDER BY node_sequence_num ASC ");
        builder.append("LIMIT 1");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, nodeID);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for the one event returned
            if (results.next()) {
                //create the appropriate event type from the result set
                return convertResultSetToStorytellerEvent(results);
            }
            return null;
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public List<StorytellerEvent> getEventsByNode(String id) throws DBAbstractionException {
        List<StorytellerEvent> retVal = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, id);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for all events in the database
            while (results.next()) {
                //create the appropriate event type from the result set
                retVal.add(convertResultSetToStorytellerEvent(results));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<StorytellerEvent> getEventsByNodeAndSequenceNumber(String nodeId, int sequenceNumber, List<String> acceptableDocIds) throws DBAbstractionException {
        List<StorytellerEvent> retVal = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id = ? ");
        builder.append("AND node_sequence_num <= ? ");
        //if there are some acceptable doc ids
        if (acceptableDocIds != null && acceptableDocIds.size() > 0) {
            builder.append("AND (document_id IN (");
            builder.append(join(acceptableDocIds, ", ", true));
            builder.append(") OR document_id is null) ");
        }
        builder.append("ORDER BY node_sequence_num ASC ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, nodeId);
            ps.setInt(2, sequenceNumber);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for all events in the database
            while (results.next()) {
                //create the appropriate event type from the result set
                retVal.add(convertResultSetToStorytellerEvent(results));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<StorytellerEvent> getEventsInNodeFromOneEventToAnother(String nodeId, int startNodeSequenceNum, int endNodeSequenceNum, List<String> acceptableDocIds) throws DBAbstractionException {
        List<StorytellerEvent> retVal = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id = ? ");
        builder.append("AND node_sequence_num >= ? ");
        builder.append("AND node_sequence_num <= ? ");

        //if there are some acceptable document ids
        if (acceptableDocIds != null && acceptableDocIds.size() > 0) {
            builder.append("AND (document_id IN (");
            builder.append(join(acceptableDocIds, ", ", true));
            builder.append(") OR document_id is null) ");
        }
        builder.append("ORDER BY node_sequence_num ASC ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, nodeId);
            ps.setInt(2, startNodeSequenceNum);
            ps.setInt(3, endNodeSequenceNum);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for all events in the database
            while (results.next()) {
                //create the appropriate event type from the result set
                retVal.add(convertResultSetToStorytellerEvent(results));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public StorytellerEvent getFirstRelevantEventInNode(String nodeId, int sequenceNumber, long startTime, long endTime, List<String> documentIDs, List<String> developerGroupIDs) throws DBAbstractionException {
        StorytellerEvent retVal = null;

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id = ? ");
        builder.append("AND node_sequence_num <= ? ");
        builder.append("AND timestamp >= ? ");
        builder.append("AND timestamp <= ? ");

        //if there are some acceptable doc ids passed in
        if (documentIDs != null && documentIDs.size() > 0) {
            builder.append("AND (document_id IN (");
            builder.append(join(documentIDs, ", ", true));
            builder.append(") OR document_id is null) ");
        }

        //if there are some acceptable dev group ids passed in
        if (developerGroupIDs != null && developerGroupIDs.size() > 0) {
            builder.append("AND created_by_dev_group_id IN (");
            builder.append(join(developerGroupIDs, ", ", true));
            builder.append(") ");
        }

        builder.append("ORDER BY node_sequence_num ASC ");
        builder.append("LIMIT 1 ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, nodeId);
            ps.setInt(2, sequenceNumber);
            ps.setLong(3, startTime);
            ps.setLong(4, endTime);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for all events in the database
            if (results.next()) {
                //create the appropriate event type from the result set
                retVal = convertResultSetToStorytellerEvent(results);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public StorytellerEvent getFirstRelevantEventInNode(String nodeId, int sequenceNumber, List<String> selectedAndRelevantEventIds) throws DBAbstractionException {
        StorytellerEvent retVal = null;

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id = ? ");
        builder.append("AND node_sequence_num <= ? ");
        builder.append("AND id IN ( ");
        builder.append(join(selectedAndRelevantEventIds, ", ", true));
        builder.append(") ");
        builder.append("ORDER BY node_sequence_num ASC ");
        builder.append("LIMIT 1 ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, nodeId);
            ps.setInt(2, sequenceNumber);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for all events in the database
            if (results.next()) {
                //create the appropriate event type from the result set
                retVal = convertResultSetToStorytellerEvent(results);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public StorytellerEvent getLastRelevantEventInNode(String nodeId, int sequenceNumber, long startTime, long endTime, List<String> documentIDs, List<String> developerGroupIDs) throws DBAbstractionException {
        StorytellerEvent retVal = null;

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id = ? ");
        builder.append("AND node_sequence_num <= ? ");
        builder.append("AND timestamp >= ? ");
        builder.append("AND timestamp <= ? ");

        //if there are some acceptable doc ids passed in
        if (documentIDs != null && documentIDs.size() > 0) {
            builder.append("AND (document_id IN (");
            builder.append(join(documentIDs, ", ", true));
            builder.append(") OR document_id is null) ");
        }

        //if there are some acceptable dev group ids passed in
        if (developerGroupIDs != null && developerGroupIDs.size() > 0) {
            builder.append("AND created_by_dev_group_id IN (");
            builder.append(join(developerGroupIDs, ", ", true));
            builder.append(") ");
        }

        builder.append("ORDER BY node_sequence_num DESC ");
        builder.append("LIMIT 1 ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, nodeId);
            ps.setInt(2, sequenceNumber);
            ps.setLong(3, startTime);
            ps.setLong(4, endTime);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for all events in the database
            if (results.next()) {
                //create the appropriate event type from the result set
                retVal = convertResultSetToStorytellerEvent(results);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public StorytellerEvent getCreateDirectoryEvent(String dirId) throws DBAbstractionException {
        StorytellerEvent retVal = null;

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE event_type = ? ");
        builder.append("AND directory_id = ? ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, CreateDirectoryEvent.CREATE_DIRECTORY_EVENT_TYPE);
            ps.setString(2, dirId);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for all events in the database
            if (results.next()) {
                //create the appropriate event type from the result set
                retVal = convertResultSetToStorytellerEvent(results);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public StorytellerEvent getCreateDocumentEvent(String docId) throws DBAbstractionException {
        StorytellerEvent retVal = null;

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE event_type = ? ");
        builder.append("AND document_id = ? ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, CreateDocumentEvent.CREATE_DOCUMENT_EVENT_TYPE);
            ps.setString(2, docId);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for all events in the database
            if (results.next()) {
                //create the appropriate event type from the result set
                retVal = convertResultSetToStorytellerEvent(results);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    /**
     * Gets all events relevant to the future that occur in a given merge node
     */
    @Override
    public List<StorytellerEvent> getAllEventsRelevantToFutureInMergeNode(String mergeNodeId) throws DBAbstractionException {
        throw DBAbstraction.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public List<InsertEvent> getInsertEventsThatBackUpToEventsByNode(String nodeId, int nodeSequenceNum, List<String> idsToLookFor, List<String> idsToIgnore) throws DBAbstractionException {
        List<InsertEvent> retVal = new ArrayList<>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE event_type = ? ");
        builder.append("AND created_under_node_id = ? ");
        builder.append("AND node_sequence_num <= ? ");
        builder.append("AND previous_neighbor_event_id IN ( ");
        builder.append(join(idsToLookFor, ", ", true));
        builder.append(") ");
        builder.append("AND id NOT IN (");
        builder.append(join(idsToIgnore, ", ", true));
        builder.append(") ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, InsertEvent.INSERT_EVENT_TYPE);
            ps.setString(2, nodeId);
            ps.setInt(3, nodeSequenceNum);

            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //create the appropriate event type from the result set
                retVal.add((InsertEvent) convertResultSetToStorytellerEvent(results));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;

    }

    @Override
    public List<Node> getAllNodes() throws DBAbstractionException {
        List<Node> retVal = new ArrayList<>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Nodes ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");
                String projectId = results.getString("project_id");
                //Node attributes
                String name = results.getString("name");
                String description = results.getString("description");
                int nodeLineageNumber = results.getInt("node_lineage_number");
                String nodeType = results.getString("node_type");

                //create a node from the result set
                retVal.add(new Node(nodeId, timestamp, createdUnderNodeId,
                        devGroupId, name, description, projectId, nodeLineageNumber, nodeType));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Node> getAllNodesInALineage(String childNodeId, boolean includePassedInNode) throws DBAbstractionException {
        //list of nodes in a lineage from the root to the child node id
        List<Node> retVal = new ArrayList<>();

        //get the youngest child
        Node childNode = getNode(childNodeId);

        //if the user wants the passed in node to be on the list
        if (childNode != null && includePassedInNode) {
            //add it to the list of nodes
            retVal.add(childNode);
        }

        String parentNodeId;

        //while there is a node
        while (childNode != null) {
            //get the id of the parent node
            parentNodeId = childNode.getCreatedUnderNodeId();

            //find the parent node
            childNode = getNode(parentNodeId);

            if (childNode != null) {
                //add it to the list of nodes
                retVal.add(childNode);
            }
        }

        //the nodes were added from youngest to oldest, we will reverse the order from oldest to youngest
        Collections.reverse(retVal);

        return retVal;
    }

    @Override
    public List<Project> getAllProjects() throws DBAbstractionException {
        //get all the project names in the database
        List<Project> retVal = new ArrayList<Project>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Projects ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String projectId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdByDevGroupId = results.getString("created_by_dev_group_id");
                //Project attributes
                String projectName = results.getString("project_name");

                //create a project from the result set
                retVal.add(new Project(projectId, timestamp, createdByDevGroupId, projectName));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    /**
     * Since the database table holds all types of events in the hierarchy this
     * method is responsible for creating the correct type of object based on
     * the event type stored in the table.
     *
     * @param results A result set that holds the results from executing a query
     * @return A generic event reference that points to a specific type of event
     */
    protected StorytellerEvent convertResultSetToStorytellerEvent(ResultSet results) throws DBAbstractionException {
        StorytellerEvent retVal = null;

        try {
            //entity attributes
            String id = results.getString("id");
            Date timestamp = new Date(results.getLong("timestamp"));
            String createdUnderNodeId = results.getString("created_under_node_id");
            String devGroupId = results.getString("created_by_dev_group_id");

            //storyteller event attributes
            int nodeSequenceNum = results.getInt("node_sequence_num");
            String sequentiallyBeforeEventId = results.getString("sequentially_before_event_id");


            //text event attributes
            String documentId = results.getString("document_id");
            String previousNeighborEventId = results.getString("previous_neighbor_event_id");

            //insert event attributes
            String eventData = results.getString("event_data");
            String pasteParentId = results.getString("paste_parent_id");
            String sortOrder = results.getString("sort_order");

            //delete event attributes (none)

            //document event attributes (reuse document_id)

            //create document/directory attributes
            String parentDirectoryId = results.getString("parent_directory_id");
            String newName = results.getString("new_name");
            //rename document attributes (reuse new_name)
            String oldName = results.getString("old_name");
            //delete document attributes (none)
            //move document/directory attributes (reuse parent_directory_id as source dir id)
            String newParentDirectoryId = results.getString("new_parent_directory_id");

            //directory event attributes
            String directoryId = results.getString("directory_id");
            //delete directory attributes (none)

            //the event_type column says what type of event this is
            String eventType = results.getString("event_type");

            String sequentiallyBeforeNodeId = results.getString("sequentially_before_node_id");
            String firstNodeToMergeId = results.getString("first_node_to_merge_id");
            String secondNodeToMergeId = results.getString("second_node_to_merge_id");
            String baseResolutionId = results.getString("base_resolution_id");

            //check the type of event from the event_type column
            //and create the appropriate type of event
            if (eventType.equals(InsertEvent.INSERT_EVENT_TYPE)) {
                retVal = new InsertEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        documentId,
                        previousNeighborEventId,
                        eventData,
                        pasteParentId,
                        sortOrder);
            } else if (eventType.equals(DeleteEvent.DELETE_EVENT_TYPE)) {
                retVal = new DeleteEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        documentId,
                        previousNeighborEventId);
            } else if (eventType.equals(CreateDocumentEvent.CREATE_DOCUMENT_EVENT_TYPE)) {
                retVal = new CreateDocumentEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        documentId,
                        newName,
                        parentDirectoryId);
            } else if (eventType.equals(RenameDocumentEvent.RENAME_DOCUMENT_EVENT_TYPE)) {
                retVal = new RenameDocumentEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        documentId,
                        newName,
                        oldName,
                        parentDirectoryId);
            } else if (eventType.equals(MoveDocumentEvent.MOVE_DOCUMENT_EVENT_TYPE)) {
                retVal = new MoveDocumentEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        documentId,
                        parentDirectoryId,
                        newParentDirectoryId);
            } else if (eventType.equals(DeleteDocumentEvent.DELETE_DOCUMENT_EVENT_TYPE)) {
                retVal = new DeleteDocumentEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        documentId,
                        parentDirectoryId,
                        oldName);
            } else if (eventType.equals(CreateDirectoryEvent.CREATE_DIRECTORY_EVENT_TYPE)) {
                retVal = new CreateDirectoryEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        directoryId,
                        newName,
                        parentDirectoryId);
            } else if (eventType.equals(RenameDirectoryEvent.RENAME_DIRECTORY_EVENT_TYPE)) {
                retVal = new RenameDirectoryEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        directoryId,
                        newName,
                        oldName,
                        parentDirectoryId);
            } else if (eventType.equals(MoveDirectoryEvent.MOVE_DIRECTORY_EVENT_TYPE)) {
                retVal = new MoveDirectoryEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        directoryId,
                        parentDirectoryId,
                        newParentDirectoryId);
            } else if (eventType.equals(DeleteDirectoryEvent.DELETE_DIRECTORY_EVENT_TYPE)) {
                retVal = new DeleteDirectoryEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        directoryId,
                        parentDirectoryId,
                        oldName);
            } else if (eventType.equals(OpenNodeEvent.OPEN_NODE_EVENT_TYPE)) {
                retVal = new OpenNodeEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        sequentiallyBeforeNodeId);
            } else if (eventType.equals(CloseNodeEvent.CLOSE_NODE_EVENT_TYPE)) {
                retVal = new CloseNodeEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId);
            } else if (eventType.equals(MergeEvent.MERGE_EVENT_TYPE)) {
                retVal = new MergeEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        firstNodeToMergeId,
                        secondNodeToMergeId,
                        sequentiallyBeforeNodeId);
            } else if (eventType.equals(AutomaticConflictEvent.AUTOMATIC_CONFLICT_EVENT_TYPE)) {
                retVal = new AutomaticConflictEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        //get the ids of events in this block, after asking Storyteller event to piece together the event id of this event
                        getIdsFromCETableUsingId(createdUnderNodeId, nodeSequenceNum),
                        documentId);
            } else if (eventType.equals(ManualConflictEvent.MANUAL_CONFLICT_EVENT_TYPE)) {
                retVal = new ManualConflictEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        //get the ids of events in this block in both lists, after asking Storyteller event to piece together the event id of this event
                        getIdsFromMCETableUsingMCEId(createdUnderNodeId, nodeSequenceNum, 1),
                        getIdsFromMCETableUsingMCEId(createdUnderNodeId, nodeSequenceNum, 2),
                        documentId);
            } else if (eventType.equals(ConflictResolutionEvent.CONFLICT_RESOLUTION_EVENT_TYPE)) {
                retVal = new ConflictResolutionEvent(
                        id,
                        timestamp,
                        createdUnderNodeId,
                        devGroupId,
                        nodeSequenceNum,
                        sequentiallyBeforeEventId,
                        baseResolutionId,
                        //get the ids of events in this block, after asking Storyteller event to piece together the event id of this event
                        getIdsFromCETableUsingId(createdUnderNodeId, nodeSequenceNum),
                        documentId);
            } else {
                throw new DBAbstractionException("Could not create an event from the result set");
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }


    /**
     * helper for getIdsFrom***TableUsing***Id().  Extracts some number of attributes 'event_id'
     * from the result set
     *
     * @param results
     * @return
     * @throws DBAbstractionException
     */
    protected List<String> pullEventIdsFromResultSet(ResultSet results) throws DBAbstractionException {
        List<String> list = new ArrayList<String>();
        try {
            while (results.next()) {
                //TODO look at this...might be bad
                //this was changed to reflect the change in event id
                list.add(results.getString("id"));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
        return list;
    }

    /**
     * Gets the list of ids from the  ManualConflictsHasEvents associated with the
     * manual event that has the given id in the specified list
     *
     * @param mceNodeId
     * @param mceSequenceNum
     * @param listNumber
     * @return
     */
    protected List<String> getIdsFromMCETableUsingMCEId(String mceNodeId, int mceSequenceNum, int listNumber) throws DBAbstractionException {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT event_created_under_node_id, event_sequence_num ");
        builder.append("FROM ManualConflictsHasEvents ");
        builder.append("WHERE mce_sequence_num = ?");
        builder.append(" AND mce_created_under_node_id = ?");
        builder.append(" AND first_or_second_list = ?");
        builder.append(" ORDER BY event_order_in_conflict ASC ");

        PreparedStatement ps;

        try {
            ps = connection.prepareStatement(builder.toString());
            ps.setInt(1, mceSequenceNum);
            ps.setString(2, mceNodeId);
            ps.setInt(3, listNumber);
            ResultSet results = executeWithResults(ps);

            List<String> eventIds = pullEventIdsFromResultSet(results);

            return eventIds;
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }


    }

    /**
     * Gets the list of ids from the FutureRelatedCombinatorialEventsHasEventsTable associated with the
     * conflictResolutionEvent that has the given id
     *
     * @param ceNodeId
     * @param ceSequenceNum
     * @return
     */
    protected List<String> getIdsFromCETableUsingId(String ceNodeId, int ceSequenceNum) throws DBAbstractionException {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT event_created_under_node_id, event_sequence_num ");
        builder.append("FROM FutureRelatedCombinatorialEventsHasEventsTable ");
        builder.append("WHERE ce_sequence_num = ?");
        builder.append(" AND ce_created_under_node_id = ?");
        builder.append(" ORDER BY event_order_in_conflict ASC ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setInt(1, ceSequenceNum);
            ps.setString(2, ceNodeId);

            ResultSet results = executeWithResults(ps);

            List<String> eventIds = pullEventIdsFromResultSet(results);

            return eventIds;
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    @Override
    public Node getOpenNode() throws DBAbstractionException {
        Node retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Nodes ");
        builder.append("WHERE node_type = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, Node.OPEN_NODE);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one open node
            if (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Node attributes
                String name = results.getString("name");
                String description = results.getString("description");
                int nodeLineageNumber = results.getInt("node_lineage_number");
                String nodeType = results.getString("node_type");

                String projectId = results.getString("project_id");
                //create a node from the result set
                retVal = new Node(nodeId, timestamp, createdUnderNodeId,
                        devGroupId, name, description, projectId, nodeLineageNumber,
                        nodeType);
            }
            if (results.next()) {
                throw new DBAbstractionException("More than one open node");
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public Node getNode(String id) throws DBAbstractionException {
        Node retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Nodes ");
        builder.append("WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, id);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one node with the passed in id
            if (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Node attributes
                String name = results.getString("name");
                String description = results.getString("description");
                int nodeLineageNumber = results.getInt("node_lineage_number");
                String nodeType = results.getString("node_type");

                String projectId = results.getString("project_id");
                //create a node from the result set
                retVal = new Node(nodeId, timestamp, createdUnderNodeId,
                        devGroupId, name, description, projectId, nodeLineageNumber,
                        nodeType);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public Project getProject(String id) throws DBAbstractionException {
        Project retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Projects ");
        builder.append("WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, id);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one node with the passed in id
            if (results.next()) {
                //entity attributes
                String projectId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String devGroupId = results.getString("created_by_dev_group_id");

                //Project attributes
                String projectName = results.getString("project_name");

                //create a node from the result set
                retVal = new Project(projectId, timestamp, devGroupId, projectName);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public Project getProjectByName(String projectName) throws DBAbstractionException {
        Project retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Projects ");
        builder.append("WHERE project_name = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, sanitizeAlphabeticalInput(projectName));
            //perform the query
            ResultSet results = executeWithResults(ps);

            if (results.next()) {
                //entity attributes
                String projectId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String devGroupId = results.getString("created_by_dev_group_id");


                //create a node from the result set
                retVal = new Project(projectId, timestamp, devGroupId, projectName);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public Developer getDeveloper(String id) throws DBAbstractionException {
        Developer retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Developers ");
        builder.append("WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, id);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one developer with the passed in id
            if (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Developer attributes
                String email = results.getString("email");
                String firstName = results.getString("first_name");
                String lastName = results.getString("last_name");

                //create a developer from the result set
                retVal = new Developer(nodeId, timestamp, createdUnderNodeId,
                        devGroupId, email, firstName,
                        lastName);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public Developer getDeveloperByEmailAddress(String emailAddress) throws DBAbstractionException {
        Developer retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Developers ");
        builder.append("WHERE email = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, emailAddress);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one developer with the passed in id
            if (results.next()) {
                //entity attributes
                String id = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Developer attributes
                String email = results.getString("email");
                String firstName = results.getString("first_name");
                String lastName = results.getString("last_name");

                //create a developer from the result set
                retVal = new Developer(id, timestamp, createdUnderNodeId,
                        devGroupId, email, firstName,
                        lastName);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;

    }

    @Override
    public DeveloperGroup getDeveloperGroup(String id) throws DBAbstractionException {
        DeveloperGroup retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM DeveloperGroups ");
        builder.append("WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, id);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one developer group with the passed in id
            if (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //create a developer group from the result set
                retVal = new DeveloperGroup(nodeId, timestamp, createdUnderNodeId, devGroupId);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public DeveloperGroup getDevelopersSoleDeveloperGroup(String developerId) throws DBAbstractionException {
        //create a list of developer ids (we're looking for the group that just has one)
        List<String> soleDeveloperIds = new ArrayList<String>();
        //add the sole developer
        soleDeveloperIds.add(developerId);

        //get the one group that has only this developer
        DeveloperGroup devGroup = getDeveloperGroupByDeveloperIds(soleDeveloperIds);

        if (devGroup == null) {
            throw new DBAbstractionException("Developer does not exist- no dev group");
        }

        return devGroup;
    }

    @Override
    public DeveloperGroup getDeveloperGroupByDeveloperIds(List<String> devIds) throws DBAbstractionException {
        DeveloperGroup retVal = null;

        //number of developers that must be in the group
        int numDevsInProposedGroup = devIds.size();

		/*
			SELECT *
		 	FROM DeveloperGroups
			WHERE id IN (
				-- all dev group ids with only the specified members
		  		SELECT developer_group_id
				FROM DevelopersBelongToDeveloperGroups
				WHERE developer_id IN ('1', '2')
				AND developer_group_id IN (
			  		-- all dev group ids with the right number of members
			  		SELECT developer_group_id
			  		FROM DevelopersBelongToDeveloperGroups
			  		GROUP BY developer_group_id
			  		HAVING COUNT(*) = 2 )
				GROUP BY developer_group_id
				HAVING COUNT(*) = 2
			)
		 */
        //TODO do this better
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM DeveloperGroups ");
        builder.append("WHERE id IN ( ");
        builder.append("  SELECT developer_group_id ");
        builder.append("  FROM DevelopersBelongToDeveloperGroups ");
        builder.append("  WHERE developer_id IN (");
        //create a comma separated list of dev ids
        for (int i = 0; i < devIds.size(); i++) {
            builder.append(" ?");
            //there is no comma after the last one
            if (i < devIds.size() - 1) {
                builder.append(",");
            }
        }
        builder.append(") ");
        builder.append("  AND developer_group_id IN ( ");
        builder.append("    SELECT developer_group_id ");
        builder.append("    FROM DevelopersBelongToDeveloperGroups ");
        builder.append("    GROUP BY developer_group_id ");
        builder.append("    HAVING COUNT(*) = ?)");
        builder.append("  GROUP BY developer_group_id ");
        builder.append("  HAVING COUNT(*) = ?)");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            for (int i = 0; i < devIds.size(); i++) {
                ps.setString(i + 1, devIds.get(i));
            }
            int offset = devIds.size();
            ps.setInt(offset + 1, numDevsInProposedGroup);
            ps.setInt(offset + 2, numDevsInProposedGroup);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //get the developer ids with the passed in group id
            if (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //create a developer group from the result set
                retVal = new DeveloperGroup(nodeId, timestamp, createdUnderNodeId, devGroupId);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Developer> getDevelopersInADeveloperGroup(String groupId) throws DBAbstractionException {
        List<Developer> retVal = new ArrayList<Developer>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Developers ");
        builder.append("WHERE id IN (");
        builder.append("  SELECT developer_id ");
        builder.append("  FROM DevelopersBelongToDeveloperGroups ");
        builder.append("  WHERE developer_group_id = ?) ");
        builder.append("ORDER BY last_name, first_name");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, groupId);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //get the developer ids with the passed in group id
            while (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Developer attributes
                String email = results.getString("email");
                String firstName = results.getString("first_name");
                String lastName = results.getString("last_name");

                //create a developer from the result set
                retVal.add(new Developer(nodeId, timestamp, createdUnderNodeId,
                        devGroupId, email, firstName,
                        lastName));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    public List<Developer> getDevelopersInDeveloperGroups(List<String> devGroupIds) throws DBAbstractionException {
        List<Developer> retVal = new ArrayList<Developer>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Developers ");
        builder.append("WHERE id IN (");
        builder.append("  SELECT DISTINCT developer_id ");
        builder.append("  FROM DevelopersBelongToDeveloperGroups ");
        builder.append("  WHERE developer_group_id IN ( ");
        builder.append(join(devGroupIds, ", ", true));
        builder.append("   ) ");
        builder.append(" ) ");
        builder.append("ORDER BY last_name, first_name");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            //perform the query
            ResultSet results = executeWithResults(ps);

            //get the developer ids with the passed in group id
            while (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Developer attributes
                String email = results.getString("email");
                String firstName = results.getString("first_name");
                String lastName = results.getString("last_name");

                //create a developer from the result set
                retVal.add(new Developer(nodeId, timestamp, createdUnderNodeId,
                        devGroupId, email, firstName,
                        lastName));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public Document getDocument(String id) throws DBAbstractionException {
        Document retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Documents ");
        builder.append("WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, id);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one document with the passed in id
            if (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //document attributes
                String parentDirectoryId = results.getString("parent_directory_id");

                //create a document
                retVal = new Document(nodeId, timestamp, createdUnderNodeId,
                        devGroupId, parentDirectoryId);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;

    }

    @Override
    public Directory getDirectory(String id) throws DBAbstractionException {
        Directory retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Directories ");
        builder.append("WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, id);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one directory with the passed in id
            if (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Directory attributes
                String parentDirectoryId = results.getString("parent_directory_id");

                retVal = new Directory(nodeId, timestamp, createdUnderNodeId,
                        devGroupId, parentDirectoryId);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    //TODO MM- have to change because there is no node selection!!
    @Override
    public List<DeveloperGroup> getAllDevGroupsInNodes(List<String> nodeIDs) throws DBAbstractionException {
        List<DeveloperGroup> retVal = new ArrayList<DeveloperGroup>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM DeveloperGroups ");
        builder.append("WHERE created_under_node_id IN (");
        for (int i = 0; i < nodeIDs.size(); i++) {
            builder.append(" ?");
            if (i < nodeIDs.size() - 1) {
                builder.append(",");
            }
        }
        builder.append(")");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            for (int i = 0; i < nodeIDs.size(); i++) {
                ps.setString(i + 1, nodeIDs.get(i));
            }
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be multiple documents with the passed in ids
            while (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //create a developer group from the result set
                retVal.add(new DeveloperGroup(nodeId, timestamp, createdUnderNodeId,
                        devGroupId));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<DeveloperGroup> getAllDeveloperGroupsInANode(String nodeId, int numEventsInNode) throws DBAbstractionException {
        //holds all the dev groups who contributed in the passed in node up to an event
        List<DeveloperGroup> retVal = new ArrayList<DeveloperGroup>();

        //holds the ids of the dev groups who contributed
        List<String> devGroupIds = new ArrayList<String>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT created_by_dev_group_id ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id = ? ");
        builder.append("AND node_sequence_num <= ? ");
        builder.append("GROUP BY created_by_dev_group_id ");
        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, nodeId);
            ps.setInt(2, numEventsInNode);

            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                devGroupIds.add(results.getString("created_by_dev_group_id"));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        //now get the actual dev groups from the ids
        for (String devGroupId : devGroupIds) {
            retVal.add(getDeveloperGroup(devGroupId));
        }

        return retVal;
    }

    @Override
    public List<DeveloperGroup> getAllDeveloperGroups() throws DBAbstractionException {
        List<DeveloperGroup> retVal = new ArrayList<DeveloperGroup>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM DeveloperGroups ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be multiple documents with the passed in ids
            while (results.next()) {
                //entity attributes
                String nodeId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //create a developer group from the result set
                retVal.add(new DeveloperGroup(nodeId, timestamp, createdUnderNodeId,
                        devGroupId));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Developer> getAllDevelopers() throws DBAbstractionException {
        List<Developer> retVal = new ArrayList<Developer>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Developers ");
        builder.append("ORDER BY last_name, first_name");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String devId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //developer attributes
                String firstName = results.getString("first_name");
                String lastName = results.getString("last_name");
                String email = results.getString("email");

                //create a developer from the result set
                retVal.add(new Developer(devId, timestamp, createdUnderNodeId, devGroupId, email, firstName, lastName));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Document> getAllDocuments() throws DBAbstractionException {
        List<Document> retVal = new ArrayList<Document>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Documents ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            //perform the query
            ResultSet results = executeWithResults(ps);

            //for all documents
            while (results.next()) {
                //entity attributes
                String docId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //document attributes
                String parentDirectoryId = results.getString("parent_directory_id");

                //create a document
                retVal.add(new Document(docId, timestamp, createdUnderNodeId, devGroupId, parentDirectoryId));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Document> getAllDocumentsCreatedInANode(String nodeId) throws DBAbstractionException {
        List<Document> retVal = new ArrayList<Document>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Documents ");
        builder.append("WHERE created_under_node_id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, nodeId);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be multiple documents with the passed in ids
            while (results.next()) {
                //entity attributes
                String docId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //document attributes
                String parentDirectoryId = results.getString("parent_directory_id");

                //create a document
                retVal.add(new Document(docId, timestamp, createdUnderNodeId, devGroupId, parentDirectoryId));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public Clip getClip(String id) throws DBAbstractionException {
        Clip retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Clips ");
        builder.append("WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, id);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one clip with the passed in id
            if (results.next()) {
                //entity attributes
                String clipId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Clip attributes
                String name = decode(results.getString("name"));
                String description = decode(results.getString("description"));
                String filterString = results.getString("filter_string");
                String playbackNodeId = results.getString("playback_node_id");

                try {
                    PlaybackFilter filter = DeJSONiffy.playbackFilter(decode(filterString));

                    retVal = new Clip(clipId, timestamp, createdUnderNodeId,
                            devGroupId, name, description, filter,
                            playbackNodeId);
                } catch (JSONException e) {
                    throw new DBAbstractionException(e);
                }

            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Clip> getAllClips() throws DBAbstractionException {
        List<Clip> retVal = new ArrayList<Clip>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Clips ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String clipId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //clip attributes
                String name = decode(results.getString("name"));
                String description = decode(results.getString("description"));
                String filterString = results.getString("filter_string");
                String playbackNodeId = results.getString("playback_node_id");

                PlaybackFilter filter = DeJSONiffy.playbackFilter(decode(filterString));

                //create a clip from the result set
                retVal.add(new Clip(clipId, timestamp, createdUnderNodeId,
                        devGroupId, name, description, filter,
                        playbackNodeId));
            }
        } catch (SQLException | JSONException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Clip> getClipsAssociatedWithStoryboard(String storyboardId) throws DBAbstractionException {
        List<Clip> retVal = new ArrayList<Clip>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM ClipStoryboardJoinTable ");
        builder.append("WHERE storyboard_id = ? ORDER BY clip_position_in_storyboard");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, storyboardId);
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //get the clip id
                String clipId = results.getString("clip_id");

                //TODO do this in a single batch query instead??
                //create a clip from the id
                retVal.add(getClip(clipId));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Clip> getAllClipsByDevGroups(List<String> ids) throws DBAbstractionException {
        List<Clip> retVal = new ArrayList<Clip>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Clips ");
        builder.append("WHERE created_by_dev_group_id IN (");
        //create a comma separated list of dev group ids
        for (int i = 0; i < ids.size(); i++) {
            builder.append("?");

            if (i < ids.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            for (int i = 0; i < ids.size(); i++) {
                ps.setString(i + 1, ids.get(i));
            }
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String clipId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //clip attributes
                String name = results.getString("name");
                String description = results.getString("description");
                String filterString = results.getString("filter_string");
                String playbackNodeId = results.getString("playback_node_id");

                //create a clip from the result set
                try {
                    PlaybackFilter filter = DeJSONiffy.playbackFilter(filterString);

                    retVal.add(new Clip(clipId, timestamp, createdUnderNodeId,
                            devGroupId, name, description, filter,
                            playbackNodeId));
                } catch (JSONException e) {
                    // I'm a cheater
                    throw new DBAbstractionException(e);
                }
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Clip> getAllClipsByTime(Date startTime, Date endTime) throws DBAbstractionException {
        List<Clip> retVal = new ArrayList<Clip>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Clips ");
        builder.append("WHERE timestamp >= ? AND timestamp <= ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setLong(1, startTime.getTime());
            ps.setLong(2, endTime.getTime());
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String clipId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //clip attributes
                String name = results.getString("name");
                String description = results.getString("description");
                String filterString = results.getString("filter_string");
                String playbackNodeId = results.getString("playback_node_id");

                PlaybackFilter filter = DeJSONiffy.playbackFilter(filterString);

                //create a clip from the result set
                retVal.add(new Clip(clipId, timestamp, createdUnderNodeId,
                        devGroupId, name, description, filter,
                        playbackNodeId));
            }
        } catch (SQLException | JSONException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public Storyboard getStoryboard(String id) throws DBAbstractionException {
        Storyboard retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Storyboards ");
        builder.append("WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, id);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one clip with the passed in id
            if (results.next()) {
                //entity attributes
                String storyboardId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Storyboard attributes
                String name = decode(results.getString("name"));
                String description = decode(results.getString("description"));

                retVal = new Storyboard(storyboardId, timestamp, createdUnderNodeId,
                        devGroupId, name, description);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Storyboard> getAllStoryboards() throws DBAbstractionException {
        List<Storyboard> retVal = new ArrayList<Storyboard>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Storyboards ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String storyboardId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Storyboard attributes
                String name = decode(results.getString("name"));
                String description = decode(results.getString("description"));

                //create a Storyboard from the result set
                retVal.add(new Storyboard(storyboardId, timestamp, createdUnderNodeId,
                        devGroupId, name, description));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Storyboard> getAllStoryboardsByDevGroups(List<String> ids) throws DBAbstractionException {
        List<Storyboard> retVal = new ArrayList<Storyboard>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Storyboards ");
        builder.append("WHERE created_by_dev_group_id IN (");
        //create a comma separated list of dev group ids
        for (int i = 0; i < ids.size(); i++) {
            builder.append("?");
            if (i < ids.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            for (int i = 0; i < ids.size(); i++) {
                ps.setString(i + 1, ids.get(i));
            }

            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String storyboardId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Storyboard attributes
                String name = results.getString("name");
                String description = results.getString("description");

                //create a Storyboard from the result set
                retVal.add(new Storyboard(storyboardId, timestamp, createdUnderNodeId,
                        devGroupId, name, description));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Storyboard> getAllStoryboardsByTime(Date startTime, Date endTime) throws DBAbstractionException {
        List<Storyboard> retVal = new ArrayList<Storyboard>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Storyboards ");
        builder.append("WHERE timestamp >= ? AND timestamp <= ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setLong(1, startTime.getTime());
            ps.setLong(2, endTime.getTime());
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String storyboardId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Storyboard attributes
                String name = results.getString("name");
                String description = results.getString("description");

                //create a Storyboard from the result set
                retVal.add(new Storyboard(storyboardId, timestamp, createdUnderNodeId,
                        devGroupId, name, description));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<Storyboard> getAllStoryboardsAssociatedWithAClip(String clipId) throws DBAbstractionException {
        List<Storyboard> retVal = new ArrayList<Storyboard>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM ClipStoryboardJoinTable ");
        builder.append("WHERE clip_id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, clipId);
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String storyboardId = results.getString("storyboard_id");

                //create a Storyboard from id
                retVal.add(getStoryboard(storyboardId));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public ClipComment getClipComment(String id) throws DBAbstractionException {
        ClipComment retVal = null;

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM ClipComments ");
        builder.append("WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, id);
            //perform the query
            ResultSet results = executeWithResults(ps);

            //there should be only one clip comment with the passed in id
            if (results.next()) {
                //entity attributes
                String clipCommentId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Clip comment attributes
                String text = decode(results.getString("text"));
                String displayCommentEventId = results.getString("display_comment_event_id");
                String clipId = results.getString("clip_id");
                String startHighlightEventId = results.getString("start_highlight_event_id");
                String endHighlightEventId = results.getString("end_highlight_event_id");

                retVal = new ClipComment(clipCommentId, timestamp, createdUnderNodeId,
                        devGroupId, text, displayCommentEventId, clipId,
                        startHighlightEventId, endHighlightEventId);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<ClipComment> getAllClipCommentsAssociatedWithAClip(String clipId) throws DBAbstractionException {
        List<ClipComment> retVal = new ArrayList<ClipComment>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM ClipComments ");
        builder.append("WHERE clip_id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, clipId);
            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String clipCommentId = results.getString("id");
                Date timestamp = new Date(results.getLong("timestamp"));
                String createdUnderNodeId = results.getString("created_under_node_id");
                String devGroupId = results.getString("created_by_dev_group_id");

                //Clip comment attributes
                String text = decode(results.getString("text"));
                String displayCommentEventId = results.getString("display_comment_event_id");
                String startHighlightEventId = results.getString("start_highlight_event_id");
                String endHighlightEventId = results.getString("end_highlight_event_id");

                //create a clip comment
                retVal.add(new ClipComment(clipCommentId, timestamp, createdUnderNodeId,
                        devGroupId, text, displayCommentEventId, clipId,
                        startHighlightEventId, endHighlightEventId));
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<StorytellerEvent> getEventsByType(String nodeId, int seqNumber, String typeOfEvent) throws DBAbstractionException {
        //a list of all the events at a certain point in time
        List<StorytellerEvent> retVal = new ArrayList<StorytellerEvent>();

        //get all the events in a node 
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE event_type = ? ");
        builder.append("AND created_under_node_id = ? ");
        builder.append("AND node_sequence_num <= ? ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, typeOfEvent);
            ps.setString(2, nodeId);
            ps.setInt(3, seqNumber);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for each event
            while (results.next()) {
                //get the event
                StorytellerEvent event = convertResultSetToStorytellerEvent(results);

                //add the event to the list
                retVal.add(event);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public List<StorytellerEvent> getEventsByType(String nodeId, int seqNumber, String typeOfEvent, List<String> documentIds) throws DBAbstractionException {
        //a list of all the events at a certain point in time
        List<StorytellerEvent> retVal = new ArrayList<StorytellerEvent>();

        //get all the events in a node 
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE event_type = ? ");
        builder.append("AND created_under_node_id = ? ");
        builder.append("AND node_sequence_num <= ? ");
        //if there are some document ids passed in 
        if (documentIds != null && documentIds.size() > 0) {
            builder.append("AND (document_id IN (");
            builder.append(join(documentIds, ", ", true));
            builder.append(") OR document_id is null) ");
        }

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            ps.setString(1, typeOfEvent);
            ps.setString(2, nodeId);
            ps.setInt(3, seqNumber);

            //perform the query
            ResultSet results = executeWithResults(ps);

            //for each event
            while (results.next()) {
                //get the event
                StorytellerEvent event = convertResultSetToStorytellerEvent(results);

                //add the event to the list
                retVal.add(event);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public void updateNode(Node node) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE Nodes ");
        builder.append("SET timestamp = ?, created_under_node_id = ?, created_by_dev_group_id = ?, ");
        builder.append("name = ?, description = ?, node_lineage_number = ?, node_type = ? WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setLong(1, node.getTimestamp().getTime()); //timestamp
            ps.setString(2, node.getCreatedUnderNodeId() == null ? "null" : node.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(3, node.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(4, node.getName()); //name
            ps.setString(5, node.getDescription()); //description
            ps.setInt(6, node.getNodeLineageNumber().intValue()); //node_lineage_number
            ps.setString(7, node.getNodeType()); //node_type
            ps.setString(8, node.getId()); //id

            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void updateInsertEventsWithDeletedTimestamps(List<String> nodeIds, Map<String, InsertEvent> insertEvents) throws DBAbstractionException {
        //Construct the query
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM Events ");
        builder.append("WHERE created_under_node_id IN (");
        for (int i = 0; i < nodeIds.size(); i++) {
            builder.append("'");
            builder.append(nodeIds.get(i));
            builder.append("'");
            if (i <= nodeIds.size() - 2) {
                builder.append(" , ");
            }
        }
        builder.append(") AND ");
        builder.append("previous_neighbor_event_id IN (");
        int count = 0;
        for (String eventId : insertEvents.keySet()) {
            builder.append("'");
            builder.append(insertEvents.get(eventId).getId());
            builder.append("'");

            if (count <= insertEvents.size() - 2) {
                builder.append(", ");
            }
            count++;
        }
        builder.append(") AND event_type = '");
        builder.append(DeleteEvent.DELETE_EVENT_TYPE);
        builder.append("'");

        try {
            Statement statement = connection.createStatement();

            //perform the query
            ResultSet results = statement.executeQuery(builder.toString());

            //there may be any positive number of delete events returned
            while (results.next()) {
                //Get the information from the delete event that has deleted an insert event in the list
                String deleteEventId = results.getString("id");
                Date deletedAtTimestamp = new Date(results.getLong("timestamp"));
                String deletedInsertEventId = results.getString("previous_neighbor_event_id");
                String deletedByDevGroupId = results.getString("created_by_dev_group_id");

                InsertEvent deletedInsertEvent = insertEvents.get(deletedInsertEventId);
                deletedInsertEvent.setDeletedAtTimestamp(deletedAtTimestamp);
                deletedInsertEvent.setDeletedByDevGroupId(deletedByDevGroupId);
                deletedInsertEvent.setDeleteEventId(deleteEventId);

                //remove it from the mapping because we don't need to keep checking it, the insertEvent can only be deleted once
                insertEvents.remove(deletedInsertEventId);
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

    }

    @Override
    public void updateDeveloper(Developer developer) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE Developers ");
        builder.append("SET timestamp = ?, created_under_node_id = ?, created_by_dev_group_id = ?, ");
        builder.append("id = ?, email = ?, first_name = ?, last_name = ? WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setLong(1, developer.getTimestamp().getTime()); //timestamp
            ps.setString(2, developer.getCreatedUnderNodeId() == null ? "null" : developer.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(3, developer.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(4, developer.getEmail()); //id
            ps.setString(5, developer.getEmail()); //email
            ps.setString(6, developer.getFirstName()); //first_name
            ps.setString(7, developer.getLastName()); //last_name
            ps.setString(8, developer.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void updateDeveloper(String firstName, String lastName, String email) throws DBAbstractionException {
        firstName = sanitizeAlphabeticalInput(firstName);
        lastName = sanitizeAlphabeticalInput(lastName);
        email = sanitizeEmailInput(email);

        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE Developers ");
        builder.append("SET first_name = ?, last_name = ? WHERE email = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, firstName); //first_name
            ps.setString(2, lastName); //last_name
            ps.setString(3, email);
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void updateDocument(Document document) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE Documents ");
        builder.append("SET timestamp = ?, created_under_node_id = ?, created_by_dev_group_id = ?, ");
        builder.append("parent_directory_id = ? WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setLong(1, document.getTimestamp().getTime()); //timestamp
            ps.setString(2, document.getCreatedUnderNodeId() == null ? "null" : document.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(3, document.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(4, document.getParentDirectoryId()); //parentDirectoryId
            ps.setString(5, document.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void updateDirectory(Directory directory) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE Directories ");
        builder.append("SET timestamp = ?, created_under_node_id = ?, created_by_dev_group_id = ?, ");
        builder.append("parent_directory_id = ? WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setLong(1, directory.getTimestamp().getTime()); //timestamp
            ps.setString(2, directory.getCreatedUnderNodeId() == null ? "null" : directory.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(3, directory.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(4, directory.getParentDirectoryId()); //parentDirectoryId
            ps.setString(5, directory.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void updateClip(Clip clip) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE Clips ");
        builder.append("SET timestamp = ?, created_under_node_id = ?, created_by_dev_group_id = ?, ");
        builder.append("name = ?, description = ?, filter_string = ?, playback_node_id = ?, WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setLong(1, clip.getTimestamp().getTime()); //timestamp
            ps.setString(2, clip.getCreatedUnderNodeId() == null ? "null" : clip.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(3, clip.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(4, clip.getName()); //name
            ps.setString(5, clip.getDescription()); //description
            ps.setString(6, clip.getFilterString()); //filter_string
            ps.setString(7, clip.getPlaybackNodeId()); //playback_node_id
            ps.setString(8, clip.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void updateStoryboard(Storyboard storyboard) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE Storyboards ");
        builder.append("SET timestamp = ?, created_under_node_id = ?, created_by_dev_group_id = ?, ");
        builder.append("name = ?, description = ?, WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setLong(1, storyboard.getTimestamp().getTime()); //timestamp
            ps.setString(2, storyboard.getCreatedUnderNodeId() == null ? "null" : storyboard.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(3, storyboard.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(4, storyboard.getName()); //name
            ps.setString(5, storyboard.getDescription()); //description
            ps.setString(6, storyboard.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void updateClipComment(ClipComment clipComment) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ClipComments ");
        builder.append("SET timestamp = ?, created_under_node_id = ?, created_by_dev_group_id = ?,");
        builder.append("text = ?, display_comment_event_id = ?, clip_id = ?, start_highlight_event_id = ?, ");
        builder.append("end_highlight_event_id = ? WHERE id = ?");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setLong(1, clipComment.getTimestamp().getTime()); //timestamp
            ps.setString(2, clipComment.getCreatedUnderNodeId() == null ? "null" : clipComment.getCreatedUnderNodeId()); //created_under_node_id
            ps.setString(3, clipComment.getCreatedByDevGroupId()); //created_by_dev_group_id
            ps.setString(4, clipComment.getText()); //text
            ps.setString(5, clipComment.getDisplayCommentEventId()); //display_comment_event_id
            ps.setString(6, clipComment.getClipId()); //clip_id
            ps.setString(7, clipComment.getStartHighlightEventId()); //start_highlight_event_id
            ps.setString(8, clipComment.getEndHighlightEventId()); //end_highlight_event_id
            ps.setString(9, clipComment.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void updateClipPositionInStoryboard(String clipId, String storyboardId, int newPosition) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ClipStoryboardJoinTable ");
        builder.append("SET clip_position_in_storyboard = ? ");
        builder.append("WHERE clip_id = ? ");
        builder.append("AND storyboard_id = ? ");

        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setInt(1, newPosition); //clip_position_in_storyboard
            ps.setString(2, clipId);
            ps.setString(3, storyboardId);
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    /**
     * Helper for deleting a StorytellerEntity (read: anything) from the database.  This is nonrecoverable.
     *
     * @param entity
     * @param tableName
     */
    protected void deleteStorytellerEntity(StorytellerEntity entity, String tableName) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE ");
        builder.append("FROM ");
        builder.append(tableName);
        builder.append(" ");
        builder.append("WHERE id = ? ");

        //perform the query
        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, entity.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void deleteEvent(StorytellerEvent event) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE ");
        builder.append("FROM Events ");
        builder.append("WHERE id = ?");
        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, event.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

    }

    @Override
    public void deleteNode(Node node) throws DBAbstractionException {
        deleteStorytellerEntity(node, "Nodes");
    }

    @Override
    public void deleteDeveloper(Developer developer) throws DBAbstractionException {
        deleteStorytellerEntity(developer, "Developers");
    }

    @Override
    public void deleteDeveloperGroup(DeveloperGroup developerGroup) throws DBAbstractionException {
        deleteStorytellerEntity(developerGroup, "DeveloperGroups");
    }

    @Override
    public void removeDeveloperFromDeveloperGroup(Developer developerWhoIsLeavingGroup, DeveloperGroup developerGroupToLeave) throws DBAbstractionException {
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE ");
        builder.append("FROM ");
        builder.append("DevelopersBelongToDeveloperGroups ");
        builder.append("WHERE developer_id = ? AND developer_group_id = ?");
        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, developerWhoIsLeavingGroup.getId());
            ps.setString(2, developerGroupToLeave.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void deleteDocument(Document document) throws DBAbstractionException {
        deleteStorytellerEntity(document, "Documents");
    }

    @Override
    public void deleteDirectory(Directory directory) throws DBAbstractionException {
        deleteStorytellerEntity(directory, "Directories");
    }

    @Override
    public void deleteClip(Clip clip) throws DBAbstractionException {
        //delete the clip
        deleteStorytellerEntity(clip, "Clips");

        //get rid of any entries in the join table linking this clip to a storyboard
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ClipStoryboardJoinTable ");
        builder.append("WHERE clip_id = ?");
        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, clip.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        //get rid of any clip comment associated with the clip
        builder = new StringBuilder();
        builder.append("DELETE FROM ClipComments ");
        builder.append("WHERE clip_id = ?");
        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, clip.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }
    }

    @Override
    public void deleteStoryboard(Storyboard storyboard) throws DBAbstractionException {
        //get rid of the storyboard
        deleteStorytellerEntity(storyboard, "Storyboards");

        //get rid of any clips joined to the storyboard
        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ClipStoryboardJoinTable ");
        builder.append("WHERE storyboard_id = ?");
        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());
            ps.setString(1, storyboard.getId());
            //perform the query
            executeWithNoResults(ps);
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

    }

    @Override
    public void deleteClipComment(ClipComment clipComment) throws DBAbstractionException {
        //get rid of the clip comment
        deleteStorytellerEntity(clipComment, "ClipComments");
    }

    //getters/setters
    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void associateEventsWithClip(List<String> listOfEventIds, String clipId, String devGroupId) throws DBAbstractionException {
        //TODO do we want to store the dev? ..no
        //build up the sql
        for (String eventId : listOfEventIds) {
            StringBuilder builder = new StringBuilder();
            builder.append("INSERT INTO EventClipJoinTable ( event_id, clip_id ) VALUES ( ?, ? )");
            try {
                PreparedStatement ps = connection.prepareStatement(builder.toString());
                ps.setString(1, eventId); //event_id
                ps.setString(2, clipId);
                //execute the query
                executeWithNoResults(ps);
            } catch (SQLException e) {
                throw new DBAbstractionException(e);
            }
        }
    }

    @Override
    public List<Storyboard> getAllStoryboardsAssociatedWithEvents(List<String> listOfEventIds) throws DBAbstractionException {
        //GET ALL STORYBOARDS ASSOCIATED WITH A CLIP
        List<Storyboard> retVal = new ArrayList<Storyboard>();

        //build up the sql
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM ClipStoryboardJoinTable ");
        builder.append("WHERE clip_id IN( ");
        //We will not be adding the id's to the Prepared Statement individually because
        //the getAllClipIdsAssociatedWithEvents method does so and returns properly
        //formatted results.
        builder.append(getAllClipIdsAssociatedWithEvents(listOfEventIds));
        builder.append(")");
        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            //perform the query
            ResultSet results = executeWithResults(ps);

            while (results.next()) {
                //entity attributes
                String storyboardId = results.getString("storyboard_id");

                //create a Storyboard from id
                if (!retVal.contains(getStoryboard(storyboardId))) {
                    retVal.add(getStoryboard(storyboardId));
                }
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public String getAllClipIdsAssociatedWithEvents(List<String> listOfEventIds) throws DBAbstractionException {
        String retVal = null;
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * ");
        builder.append("FROM EventClipJoinTable ");
        builder.append("WHERE event_id IN ( ");

        //TODO look at this??
        for (int i = 0; i < listOfEventIds.size(); i++) {
            builder.append("?");
            if (i < listOfEventIds.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        try {
            PreparedStatement ps = connection.prepareStatement(builder.toString());

            for (int i = 0; i < listOfEventIds.size(); i++) {
                ps.setString(i, listOfEventIds.get(i));
            }
            //perform the query
            ResultSet results = executeWithResults(ps);

            if (results.next()) {
                String initialClipId = results.getString("clip_id");
                retVal = "'" + initialClipId + "'";
            }
            while (results.next()) {
                //entity attributes
                String clipId = results.getString("clip_id");
                if (!retVal.contains(clipId)) {
                    //create a Storyboard from id
                    retVal += ", " + "'" + clipId + "'";
                }
            }
        } catch (SQLException ex) {
            throw new DBAbstractionException(ex);
        }

        return retVal;
    }

    @Override
    public Node getRootNode() throws DBAbstractionException {
        String sql = "SELECT * FROM Nodes WHERE name =\"root\"";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet result = executeWithResults(ps);
            String id = result.getString("id");
            Date timestamp = result.getDate("timestamp");
            String devGroupId = result.getString("created_by_dev_group_id");
            String name = result.getString("name");
            String description = result.getString("description");
            String projectId = result.getString("project_id");
            int nodeLineageNumber = result.getInt("node_lineage_number");
            String nodeType = result.getString("node_type");

            return new Node(id, timestamp, null, devGroupId, name, description, projectId, nodeLineageNumber, nodeType);
        } catch (SQLException e) {
            throw new DBAbstractionException(e);
        }
    }

    /**
     * This method will go through all the documents in the database and 
     * shorten the sort orders to be as small as possible. 
     */
//	@Override
//	public void shortenAllSortOrders() throws DBAbstractionException
//	{
//		//get all the doc ids in the database
//		List < Document > allDocs = getAllDocuments();
//		
//		//send each doc id to the helper to shorten the sort order
//		for(Document doc : allDocs)
//		{
//			shortenSortOrders(doc.getId());
//		}
//	}

    /**
     * This method will go through all the events in a document and make the 
     * sort orders as short as possible. Sort orders can get quite long if
     * they are not cleaned up periodically.
     *
     * @param docId The id of the document to shorten the sort order field
     */
//	@Override
//	public void shortenSortOrders(String docId) throws DBAbstractionException
//	{
//		//retrieve all the insert events in sort order
//		List < InsertEvent > allInserts = getAllInsertEventsInADocument(docId);
//		
//		String smallestSortOrder = "00000";
//		//go through all the insert event events
//		for(InsertEvent insertEvent : allInserts)
//		{			
//			//update the sort orders starting with the shortest value and make the
//			//smallest increment for each additional event
//			insertEvent.setSortOrder(smallestSortOrder);
//			
//			//get the next smallest sort order
//			smallestSortOrder = InBetweenHelper.inbetween(smallestSortOrder, null);
//			
//			//TODO
//			//update all insert event
//		}
//	}

    /**
     * Internal method to prep items for database entry.
     * <p>
     * We're URL encoding them because it's a kind of encoding that makes stuff safe for
     * the DB, and we're using the same methods elsewhere.
     *
     * @param item
     * @return
     */
    protected String encode(String item) {
        try {
            return URLEncoder.encode(item, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return item;
        }
    }

    /**
     * Internal method to be used after we pull stuff out of the DB.
     * <p>
     * This unsafe-iffies it.
     *
     * @param item
     * @return
     */
    protected String decode(String item) {
        try {
            return URLDecoder.decode(item, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return item;
        }
    }

    /**
     * Sanitizes a query.  To be used if we ever take input from the outside.
     *
     * @param input
     * @return
     */
    protected String sanitizeAlphabeticalInput(String input) {
        return input.replaceAll("[^a-zA-Z]", "");
    }

    protected String sanitizeEmailInput(String input) {
        return input.replaceAll("[^a-zA-Z0-9\\.@]", "");
    }

    private String join(List<String> strings, String seperator, boolean wrapWithSingleQuotes) {
        StringBuilder builder = new StringBuilder();

        //go through all the strings
        for (int i = 0; i < strings.size(); i++) {
            //if we should wrap with single quotes
            if (wrapWithSingleQuotes) {
                //add the quotes
                builder.append("'");

                //add the string
                builder.append(strings.get(i));

                //add the quotes
                builder.append("'");
            } else //don't wrap the strings in quotes
            {
                //add the string
                builder.append(strings.get(i));
            }

            //if it is not the last string
            if (i < (strings.size() - 1)) {
                //add the seperator
                builder.append(seperator);
            }
        }

        return builder.toString();
    }
}