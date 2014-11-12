//package unitTests;
//
//import static org.junit.Assert.*;
//
//import java.io.File;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.List;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import StorytellerEntities.Developer;
//import StorytellerEntities.DeveloperGroup;
//import StorytellerEntities.Directory;
//import StorytellerEntities.Document;
//import StorytellerEntities.Events.CloseNodeEvent;
//import StorytellerEntities.Events.CreateDirectoryEvent;
//import StorytellerEntities.Events.CreateDocumentEvent;
//import StorytellerEntities.Events.DeleteDirectoryEvent;
//import StorytellerEntities.Events.DeleteDocumentEvent;
//import StorytellerEntities.Events.DeleteEvent;
//import StorytellerEntities.Events.InsertEvent;
//import StorytellerEntities.Events.MoveDirectoryEvent;
//import StorytellerEntities.Events.MoveDocumentEvent;
//import StorytellerEntities.Events.OpenNodeEvent;
//import StorytellerEntities.Events.RenameDirectoryEvent;
//import StorytellerEntities.Events.RenameDocumentEvent;
//import StorytellerServer.DBAbstraction;
//import StorytellerServer.SQLiteDatabase;
//import StorytellerServer.exception.DBAbstractionException;
//
//public class TestDatabase
//{
//	private static TestSQLiteDatabase testDb;
//	private static String testFileName;
//
//	private final String createdUnderNodeId = "createdUnderNodeId";
//	private final String devGroupId = "devGroupId";
//	private final int nodeSequenceNum = 0;
//	private final String eventData = "eventData";
//	private final String previousNeighborEventId = "createdUnderNodeId_1754"; 	// This is what eventIds should look like
//	private final String sequentiallyBeforeEventId = "createdUnderNodeId_1754";	// This is what eventIds should look like
//	private final String sequentiallyBeforeNodeId = "sequentiallyBeforeNodeId";
//	private final String pasteParentId = "pasteParentId";
//	private final String documentId = "docId";
//	private final String directoryId = "directoryId";
//	private final String newName = "newName";
//	private final String oldName = "oldName";
//	private final String parentDirectoryId = "parentDirectoryId";
//	private final String newParentDirectoryId = "newParentDirectoryId";
//
//	private final String email = "email@email.com";
//	private final String firstName = "firstName";
//	private final String lastName = "lastName";
//
//	private static Logger logger = Logger.getLogger(TestDatabase.class.getName());
//
//
//	@BeforeClass
//	public static void setUp() throws Exception
//	{
//		PropertyConfigurator.configure(AllTests.LOGGING_FILE_PATH);
//		// file name for db
//		testFileName = "db" + SQLiteDatabase.DB_EXTENSION_NAME;
//
//		// clear out the old DB file
//		File oldDB = new File(testFileName);
//		oldDB.delete();
//		//create a database
//		testDb = new TestSQLiteDatabase(testFileName);
//	}
//
//
//	@AfterClass
//	public static void tearDown() throws Exception
//	{
//		// get access to the db file
//		File dbFile = new File(testFileName);
//
//		// delete the db file
//		dbFile.delete();
//	}
//
//
//	@Test
//	public void testCreatingDBWithGoodDBName()
//	{
//		try
//		{
//			String testFileName = "testDB" + SQLiteDatabase.DB_EXTENSION_NAME;
//			// create a database with the right file name
//			@SuppressWarnings("unused")
//			DBAbstraction db = new SQLiteDatabase(testFileName);
//
//			// get rid of the db file
//			File dbFile = new File(testFileName);
//			dbFile.delete();
//		}
//		catch (DBAbstractionException ex)
//		{
//			logger.fatal("", ex);
//			fail("Could not create a DB");
//		}
//	}
//
//
//	@Test
//	public void testCreatingDBWithBadDBName()
//	{
//		try
//		{
//			// attempt to create a db without the required .ali extension
//			@SuppressWarnings("unused")
//			DBAbstraction db = new SQLiteDatabase("test");
//
//			// should not get here
//			fail("File name exception should have happened here!!!");
//		}
//		catch (DBAbstractionException ex)
//		{
//			// do nothing, the exception is expected here
//		}
//	}
//
//
//	@Test
//	public void testInsertEvent()
//	{
//		// insert
//		Date timestamp = new Date();
//		InsertEvent event = new InsertEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, documentId, previousNeighborEventId, eventData, pasteParentId, "0000");
//		InsertEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (InsertEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(documentId, eventFromDb.getDocumentId());
//		assertEquals(previousNeighborEventId, eventFromDb.getPreviousNeighborEventId());
//		// assertEquals(originalPositionInDocument, eventFromDb.getOriginalPositionInDocument());
//		assertEquals(eventData, eventFromDb.getEventData());
//		assertEquals(pasteParentId, eventFromDb.getPasteParentId());
//		assertEquals(InsertEvent.INSERT_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testDeleteEvent()
//	{
//		// delete
//		Date timestamp = new Date();
//		DeleteEvent event = new DeleteEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, documentId, previousNeighborEventId);
//		DeleteEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (DeleteEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(documentId, eventFromDb.getDocumentId());
//		assertEquals(previousNeighborEventId, eventFromDb.getPreviousNeighborEventId());
//		// assertEquals(originalPositionInDocument, eventFromDb.getOriginalPositionInDocument());
//		assertEquals(DeleteEvent.DELETE_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testDeleteUpdatesInsert()
//	{
//		try
//		{
//			Date timestamp = new Date();
//			String devGroup1Id = "devGroup1Id"; // group id that inserts
//			String devGroup2Id = "devGroup2Id"; // group id that deletes
//
//			// create some insert events "cat"
//			InsertEvent event_c = new InsertEvent(timestamp, // timestamp
//			"createdUnderNodeId", // createdUnderNodeId
//			devGroup1Id, // devGroupId
//			1, // nodeSequenceNum
//			sequentiallyBeforeEventId, // sequentiallyBeforeEventId
//			"docId", // docId
//			null, // previousNeighborEventId
//			// 1, //origPosInDoc
//			"c", // eventData
//			"", // pasteParentId
//			"1" //sortOrder
//			);
//
//			InsertEvent event_a = new InsertEvent(timestamp, // timestamp
//			"createdUnderNodeId", // createdUnderNodeId
//			devGroup1Id, // devGroupId
//			2, // nodeSequenceNum
//			sequentiallyBeforeEventId, // sequentiallyBeforeEventId
//			"docId", // docId
//			event_c.getId(), // previousNeighborEventId
//			// 2, //origPosInDoc
//			"a", // eventData
//			"", // pasteParentId
//			"2" //sortOrder
//			);
//
//			InsertEvent event_t = new InsertEvent(timestamp, // timestamp
//			"createdUnderNodeId", // createdUnderNodeId
//			devGroup1Id, // devGroupId
//			3, // nodeSequenceNum
//			sequentiallyBeforeEventId, // sequentiallyBeforeEventId
//			"docId", // docId
//			event_a.getId(), // previousNeighborEventId
//			// 3, //origPosInDoc
//			"t", // eventData
//			"", // pasteParentId
//			"3" //sortOrder
//			);
//
//			// and a delete "ct"
//			DeleteEvent delete_a = new DeleteEvent(timestamp, // timestamp
//			"createdUnderNodeId", // createdUnderNodeId
//			devGroup2Id, // devGroupId,
//			4, // nodeSequenceNum
//			sequentiallyBeforeEventId, // sequentiallyBeforeEventId
//			"docId", // docId
//			event_a.getId() // previousNeighborEventId
//			// ,2//origPosInDoc
//			);
//
//			// add the inserts and the delete
//			testDb.insertEvent(event_c);
//			testDb.insertEvent(event_a);
//			testDb.insertEvent(event_t);
//			testDb.insertEvent(delete_a);
//
//		}
//		catch (DBAbstractionException ex)
//		{
//			logger.fatal("TestDatabse exception", ex);
//			fail();
//		}
//	}
//
//
//	@Test
//	public void testOpenNodeEvent()
//	{
//		// open node
//		Date timestamp = new Date();
//		OpenNodeEvent event = new OpenNodeEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, sequentiallyBeforeNodeId);
//		OpenNodeEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (OpenNodeEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(sequentiallyBeforeNodeId, eventFromDb.getSequentiallyBeforeNodeID());
//		assertEquals(OpenNodeEvent.OPEN_NODE_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testCloseNodeEvent()
//	{
//		// close node
//		Date timestamp = new Date();
//		CloseNodeEvent event = new CloseNodeEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId);
//		CloseNodeEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (CloseNodeEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(CloseNodeEvent.CLOSE_NODE_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testCreateDocumentEvent()
//	{
//		// create doc
//		Date timestamp = new Date();
//
//		CreateDocumentEvent event = new CreateDocumentEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, documentId, newName, parentDirectoryId);
//		CreateDocumentEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (CreateDocumentEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(documentId, eventFromDb.getDocumentId());
//		assertEquals(newName, eventFromDb.getDocumentNewName());
//		assertEquals(parentDirectoryId, eventFromDb.getParentDirectoryId());
//		assertEquals(CreateDocumentEvent.CREATE_DOCUMENT_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testRenameDocumentEvent()
//	{
//		// create doc
//		Date timestamp = new Date();
//
//		RenameDocumentEvent event = new RenameDocumentEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, documentId, newName, oldName, parentDirectoryId);
//		RenameDocumentEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (RenameDocumentEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(documentId, eventFromDb.getDocumentId());
//		assertEquals(newName, eventFromDb.getDocumentNewName());
//		assertEquals(oldName, eventFromDb.getDocumentOldName());
//		assertEquals(parentDirectoryId, eventFromDb.getParentDirectoryId());
//		assertEquals(RenameDocumentEvent.RENAME_DOCUMENT_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testMoveDocumentEvent()
//	{
//		// create doc
//		Date timestamp = new Date();
//
//		MoveDocumentEvent event = new MoveDocumentEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, documentId, parentDirectoryId, newParentDirectoryId);
//		MoveDocumentEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (MoveDocumentEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(documentId, eventFromDb.getDocumentId());
//		assertEquals(parentDirectoryId, eventFromDb.getParentDirectoryId());
//		assertEquals(newParentDirectoryId, eventFromDb.getNewParentDirectoryId());
//		assertEquals(MoveDocumentEvent.MOVE_DOCUMENT_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testDeleteDocumentEvent()
//	{
//		// create doc
//		Date timestamp = new Date();
//
//		DeleteDocumentEvent event = new DeleteDocumentEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, documentId, parentDirectoryId, oldName);
//		DeleteDocumentEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (DeleteDocumentEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(documentId, eventFromDb.getDocumentId());
//		assertEquals(parentDirectoryId, eventFromDb.getParentDirectoryId());
//		assertEquals(oldName, eventFromDb.getDocumentOldName());
//		assertEquals(DeleteDocumentEvent.DELETE_DOCUMENT_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testCreateDirectoryEvent()
//	{
//		// create doc
//		Date timestamp = new Date();
//
//		CreateDirectoryEvent event = new CreateDirectoryEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, directoryId, newName, parentDirectoryId);
//		CreateDirectoryEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (CreateDirectoryEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(directoryId, eventFromDb.getDirectoryId());
//		assertEquals(newName, eventFromDb.getDirectoryNewName());
//		assertEquals(parentDirectoryId, eventFromDb.getParentDirectoryId());
//		assertEquals(CreateDirectoryEvent.CREATE_DIRECTORY_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testRenameDirectoryEvent()
//	{
//		// create doc
//		Date timestamp = new Date();
//
//		RenameDirectoryEvent event = new RenameDirectoryEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, directoryId, newName, oldName, parentDirectoryId);
//		RenameDirectoryEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (RenameDirectoryEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(directoryId, eventFromDb.getDirectoryId());
//		assertEquals(newName, eventFromDb.getDirectoryNewName());
//		assertEquals(oldName, eventFromDb.getDirectoryOldName());
//		assertEquals(parentDirectoryId, eventFromDb.getParentDirectoryId());
//		assertEquals(RenameDirectoryEvent.RENAME_DIRECTORY_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testMoveDirectoryEvent()
//	{
//		// create doc
//		Date timestamp = new Date();
//
//		MoveDirectoryEvent event = new MoveDirectoryEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, directoryId, parentDirectoryId, newParentDirectoryId);
//		MoveDirectoryEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (MoveDirectoryEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(directoryId, eventFromDb.getDirectoryId());
//		assertEquals(parentDirectoryId, eventFromDb.getParentDirectoryId());
//		assertEquals(newParentDirectoryId, eventFromDb.getNewParentDirectoryId());
//		assertEquals(MoveDirectoryEvent.MOVE_DIRECTORY_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testDeleteDirectoryEvent()
//	{
//		// create doc
//		Date timestamp = new Date();
//
//		DeleteDirectoryEvent event = new DeleteDirectoryEvent(timestamp, createdUnderNodeId, devGroupId, 0, sequentiallyBeforeEventId, directoryId, parentDirectoryId, oldName);
//		DeleteDirectoryEvent eventFromDb = null;
//		String id = event.getId();
//
//		try
//		{
//			// add the event to the db
//			testDb.insertEvent(event);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the event to the db");
//		}
//
//		// grab the event's auto-generated id
//		String eventId = event.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the event from the db
//			eventFromDb = (DeleteDirectoryEvent) testDb.getEvent(eventId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the event from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(eventFromDb.getId().equals(""));
//		assertEquals(id, eventFromDb.getId());
//		assertEquals(timestamp, eventFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, eventFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, eventFromDb.getCreatedByDevGroupId());
//		assertEquals(nodeSequenceNum, eventFromDb.getNodeSequenceNum());
//		assertEquals(sequentiallyBeforeEventId, eventFromDb.getSequentiallyBeforeEventId());
//		assertEquals(directoryId, eventFromDb.getDirectoryId());
//		assertEquals(parentDirectoryId, eventFromDb.getParentDirectoryId());
//		assertEquals(oldName, eventFromDb.getDirectoryOldName());
//		assertEquals(DeleteDirectoryEvent.DELETE_DIRECTORY_EVENT_TYPE, eventFromDb.getEventType());
//
//		try
//		{
//			// delete the event
//			testDb.deleteEvent(eventFromDb);
//			assertNull(testDb.getEvent(eventFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testInsertDeveloper()
//	{
//		// insert
//		Date timestamp = new Date();
//		Developer developer = new Developer(timestamp, createdUnderNodeId, devGroupId, email, firstName, lastName);
//		Developer devFromDb = null;
//		String id = developer.getId();
//
//		try
//		{
//			// add the dev to the db
//			testDb.insertDeveloper(developer);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the dev to the db");
//		}
//
//		// grab the dev's auto-generated id
//		String devId = developer.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the dev from the db
//			devFromDb = testDb.getDeveloper(devId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the developer from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(devFromDb.getId().equals(""));
//		assertEquals(id, devFromDb.getId());
//		assertEquals(timestamp, devFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, devFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, devFromDb.getCreatedByDevGroupId());
//		assertEquals(email, devFromDb.getEmail());
//		assertEquals(firstName, devFromDb.getFirstName());
//		assertEquals(lastName, devFromDb.getLastName());
//
//
//		try
//		{
//			devFromDb.setEmail("NEWEMAIL");
//			devFromDb.setFirstName("NEWFIRSTNAME");
//			devFromDb.setLastName("NEWLASTNAME");
//
//			testDb.updateDeveloper(devFromDb);
//
//			// get the dev from the db
//			devFromDb = testDb.getDeveloper(devId);
//			assertEquals("NEWEMAIL", devFromDb.getEmail());
//			assertEquals("NEWFIRSTNAME", devFromDb.getFirstName());
//			assertEquals("NEWLASTNAME", devFromDb.getLastName());
//
//
//		}
//		catch (DBAbstractionException e1)
//		{
//			logger.fatal("", e1);
//			fail("Could not update the dev in the db");
//		}
//
//		try
//		{
//			// delete the developer
//			testDb.deleteDeveloper(devFromDb);
//			assertNull(testDb.getDeveloper(devFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testInsertDocument()
//	{
//		// insert
//		Date timestamp = new Date();
//		Document document = new Document(timestamp, createdUnderNodeId, devGroupId, parentDirectoryId);
//		Document docFromDb = null;
//		String id = document.getId();
//
//		try
//		{
//			// add the doc to the db
//			testDb.insertDocument(document);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the doc to the db");
//		}
//
//		// grab the doc's auto-generated id
//		String docId = document.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the doc from the db
//			docFromDb = testDb.getDocument(docId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the doc from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(docFromDb.getId().equals(""));
//		assertEquals(id, docFromDb.getId());
//		assertEquals(timestamp, docFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, docFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, docFromDb.getCreatedByDevGroupId());
//		assertEquals(parentDirectoryId, docFromDb.getParentDirectoryId());
//
//		try
//		{
//			// delete the document
//			testDb.deleteDocument(docFromDb);
//			assertNull(testDb.getDocument(docFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testInsertDirectory()
//	{
//		// insert
//		Date timestamp = new Date();
//		Directory directory = new Directory(timestamp, createdUnderNodeId, devGroupId, parentDirectoryId);
//		Directory dirFromDb = null;
//		String id = directory.getId();
//
//		try
//		{
//			// add the dir to the db
//			testDb.insertDirectory(directory);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not add the dir to the db");
//		}
//
//		// grab the dir's auto-generated id
//		String dirId = directory.getId();
//
//		// retrieve and test
//		try
//		{
//			// get the dir from the db
//			dirFromDb = testDb.getDirectory(dirId);
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not retrieve the dir from the db");
//		}
//
//		// test the data is the same coming out
//		assertFalse(dirFromDb.getId().equals(""));
//		assertEquals(id, dirFromDb.getId());
//		assertEquals(timestamp, dirFromDb.getTimestamp());
//		assertEquals(createdUnderNodeId, dirFromDb.getCreatedUnderNodeId());
//		assertEquals(devGroupId, dirFromDb.getCreatedByDevGroupId());
//		assertEquals(parentDirectoryId, dirFromDb.getParentDirectoryId());
//
//		try
//		{
//			// delete the directory
//			testDb.deleteDirectory(dirFromDb);
//			assertNull(testDb.getDirectory(dirFromDb.getId()));
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("", e);
//			fail("Could not delete from the db");
//		}
//	}
//
//
//	@Test
//	public void testGettingDeveloperGroupFromAListOfDevelopers()
//	{
//		try
//		{
//			Date timestamp = new Date();
//
//			// create some developers- mark, kevin, voytek
//			Developer dev1 = new Developer(timestamp, createdUnderNodeId, devGroupId, "mmahoney@carthage.edu", "Mark", "Mahoney");
//			Developer dev2 = new Developer(timestamp, createdUnderNodeId, devGroupId, "klubick@carthage.edu", "Kevin", "Lubick");
//			Developer dev3 = new Developer(timestamp, createdUnderNodeId, devGroupId, "wsnarski@carthage.edu", "Voytek", "Snarski");
//			Developer dev4 = new Developer(timestamp, createdUnderNodeId, devGroupId, "dbcooper@carthage.edu", "D.B.", "Cooper");
//
//			testDb.insertDeveloper(dev1);
//			testDb.insertDeveloper(dev2);
//			testDb.insertDeveloper(dev3);
//
//			// create some groups
//			DeveloperGroup devGroupMarkOnly = new DeveloperGroup(timestamp, createdUnderNodeId, devGroupId);
//			DeveloperGroup devGroupKevinOnly = new DeveloperGroup(timestamp, createdUnderNodeId, devGroupId);
//			DeveloperGroup devGroupVoytekOnly = new DeveloperGroup(timestamp, createdUnderNodeId, devGroupId);
//			DeveloperGroup devGroupMarkAndKevin = new DeveloperGroup(timestamp, createdUnderNodeId, devGroupId);
//			DeveloperGroup devGroupMarkAndVoytek = new DeveloperGroup(timestamp, createdUnderNodeId, devGroupId);
//			DeveloperGroup devGroupKevinAndVoytek = new DeveloperGroup(timestamp, createdUnderNodeId, devGroupId);
//			DeveloperGroup devGroupMarkKevinVoytek = new DeveloperGroup(timestamp, createdUnderNodeId, devGroupId);
//
//			testDb.insertDeveloperGroup(devGroupMarkOnly);
//			testDb.insertDeveloperGroup(devGroupKevinOnly);
//			testDb.insertDeveloperGroup(devGroupVoytekOnly);
//			testDb.insertDeveloperGroup(devGroupMarkAndKevin);
//			testDb.insertDeveloperGroup(devGroupMarkAndVoytek);
//			testDb.insertDeveloperGroup(devGroupKevinAndVoytek);
//			testDb.insertDeveloperGroup(devGroupMarkKevinVoytek);
//
//			// join the dev's to their groups
//			testDb.joinDeveloperAndDeveloperGroup(dev1, devGroupMarkOnly, createdUnderNodeId, devGroupId);
//			testDb.joinDeveloperAndDeveloperGroup(dev2, devGroupKevinOnly, createdUnderNodeId, devGroupId);
//			testDb.joinDeveloperAndDeveloperGroup(dev3, devGroupVoytekOnly, createdUnderNodeId, devGroupId);
//
//			testDb.joinDeveloperAndDeveloperGroup(dev1, devGroupMarkAndKevin, createdUnderNodeId, devGroupId);
//			testDb.joinDeveloperAndDeveloperGroup(dev2, devGroupMarkAndKevin, createdUnderNodeId, devGroupId);
//
//			testDb.joinDeveloperAndDeveloperGroup(dev1, devGroupMarkAndVoytek, createdUnderNodeId, devGroupId);
//			testDb.joinDeveloperAndDeveloperGroup(dev3, devGroupMarkAndVoytek, createdUnderNodeId, devGroupId);
//
//			testDb.joinDeveloperAndDeveloperGroup(dev2, devGroupKevinAndVoytek, createdUnderNodeId, devGroupId);
//			testDb.joinDeveloperAndDeveloperGroup(dev3, devGroupKevinAndVoytek, createdUnderNodeId, devGroupId);
//
//			testDb.joinDeveloperAndDeveloperGroup(dev1, devGroupMarkKevinVoytek, createdUnderNodeId, devGroupId);
//			testDb.joinDeveloperAndDeveloperGroup(dev2, devGroupMarkKevinVoytek, createdUnderNodeId, devGroupId);
//			testDb.joinDeveloperAndDeveloperGroup(dev3, devGroupMarkKevinVoytek, createdUnderNodeId, devGroupId);
//
//			List<String> dev1OnlyIds = new ArrayList<String>();
//			dev1OnlyIds.add(dev1.getId());
//
//			List<String> dev2OnlyIds = new ArrayList<String>();
//			dev2OnlyIds.add(dev2.getId());
//
//			List<String> dev3OnlyIds = new ArrayList<String>();
//			dev3OnlyIds.add(dev3.getId());
//
//			List<String> dev4OnlyIds = new ArrayList<String>();
//
//			List<String> dev1And2Ids = new ArrayList<String>();
//			dev1And2Ids.add(dev1.getId());
//			dev1And2Ids.add(dev2.getId());
//
//			List<String> dev1And3Ids = new ArrayList<String>();
//			dev1And3Ids.add(dev1.getId());
//			dev1And3Ids.add(dev3.getId());
//
//			List<String> dev1And4Ids = new ArrayList<String>();
//			dev1And4Ids.add(dev1.getId());
//			dev1And4Ids.add(dev4.getId());
//
//			List<String> dev2And3Ids = new ArrayList<String>();
//			dev2And3Ids.add(dev2.getId());
//			dev2And3Ids.add(dev3.getId());
//
//			List<String> dev123Ids = new ArrayList<String>();
//			dev123Ids.add(dev1.getId());
//			dev123Ids.add(dev2.getId());
//			dev123Ids.add(dev3.getId());
//
//
//			DeveloperGroup resultMarkOnly = testDb.getDeveloperGroupByDeveloperIds(dev1OnlyIds);
//			DeveloperGroup resultKevinOnly = testDb.getDeveloperGroupByDeveloperIds(dev2OnlyIds);
//			DeveloperGroup resultVoytekOnly = testDb.getDeveloperGroupByDeveloperIds(dev3OnlyIds);
//			DeveloperGroup resultDBCooperOnly = testDb.getDeveloperGroupByDeveloperIds(dev4OnlyIds);
//			DeveloperGroup resultMarkAndKevin = testDb.getDeveloperGroupByDeveloperIds(dev1And2Ids);
//			DeveloperGroup resultMarkAndVoytek = testDb.getDeveloperGroupByDeveloperIds(dev1And3Ids);
//			DeveloperGroup resultMarkAndDB = testDb.getDeveloperGroupByDeveloperIds(dev1And4Ids);
//			DeveloperGroup resultKevinAndVoytek = testDb.getDeveloperGroupByDeveloperIds(dev2And3Ids);
//			DeveloperGroup resultMarkKevinVoytek = testDb.getDeveloperGroupByDeveloperIds(dev123Ids);
//
//			assertEquals(devGroupMarkOnly.getId(), resultMarkOnly.getId());
//			assertEquals(devGroupKevinOnly.getId(), resultKevinOnly.getId());
//			assertEquals(devGroupVoytekOnly.getId(), resultVoytekOnly.getId());
//			assertNull(resultDBCooperOnly);
//			assertEquals(devGroupMarkAndKevin.getId(), resultMarkAndKevin.getId());
//			assertEquals(devGroupMarkAndVoytek.getId(), resultMarkAndVoytek.getId());
//			assertNull(resultMarkAndDB);
//			assertEquals(devGroupKevinAndVoytek.getId(), resultKevinAndVoytek.getId());
//			assertEquals(devGroupMarkKevinVoytek.getId(), resultMarkKevinVoytek.getId());
//
//		}
//		catch (DBAbstractionException e)
//		{
//			logger.fatal("TestDatabase", e);
//			fail();
//		}
//	}
//	
//	@Test
//	public void testCreatingTables()
//	{
//		//Confirm that the events table does not exist in the database
//		try 
//		{
//			/*
//			HashSet<String> hs = testDb.getTables();
//			assertFalse(hs.contains("Events"));
//			assertFalse(hs.contains("FutureRelatedCombinatorialEventsHasEventsTable"));
//			assertFalse(hs.contains("Storyboards"));
//			assertFalse(hs.contains("ClipComments"));
//			assertFalse(hs.contains("ClipStoryboardJoinTable"));
//			assertFalse(hs.contains("EventClipJoinTable"));
//			assertFalse(hs.contains("Projects"));
//			assertFalse(hs.contains("ManualConflictsHasEvents"));
//			assertFalse(hs.contains("Nodes"));
//			assertFalse(hs.contains("Developers"));
//			assertFalse(hs.contains("DeveloperGroups"));
//			assertFalse(hs.contains("DevelopersBelongToDeveloperGroups"));
//			assertFalse(hs.contains("Documents"));
//			assertFalse(hs.contains("Directories"));
//			assertFalse(hs.contains("Clips"));
//
//			//Create the tables
//			testDb.createTablesTest();
//			*/
//			
//			//confirm that they exist
//			HashSet<String> hs2 = testDb.getTables();
//			assertTrue(hs2.contains("Events"));
//			assertTrue(hs2.contains("FutureRelatedCombinatorialEventsHasEventsTable"));
//			assertTrue(hs2.contains("Storyboards"));
//			assertTrue(hs2.contains("ClipComments"));
//			assertTrue(hs2.contains("ClipStoryboardJoinTable"));
//			assertTrue(hs2.contains("EventClipJoinTable"));
//			assertTrue(hs2.contains("Projects"));
//			assertTrue(hs2.contains("ManualConflictsHasEvents"));
//			assertTrue(hs2.contains("Nodes"));
//			assertTrue(hs2.contains("Developers"));
//			assertTrue(hs2.contains("DeveloperGroups"));
//			assertTrue(hs2.contains("DevelopersBelongToDeveloperGroups"));
//			assertTrue(hs2.contains("Documents"));
//			assertTrue(hs2.contains("Directories"));
//			assertTrue(hs2.contains("Clips"));
//		
//		} catch (DBAbstractionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//}
