//package unitTests;
//
//import static org.junit.Assert.fail;
//
//import java.io.File;
//import java.util.Date;
//import java.util.UUID;
//
//import org.apache.log4j.PropertyConfigurator;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import StorytellerServer.DBFactory;
//import StorytellerServer.SQLiteDBFactory;
//import StorytellerServer.SQLiteDatabase;
//import StorytellerServer.StorytellerServer;
//import StorytellerServer.exception.DBAbstractionException;
//
//public class TestStorytellerServer
//{
//	// factory to create a sqlite database in a server
//	private DBFactory sqliteDbFactory;
//
//	// default test server
//	private StorytellerServer testServer;
//
//	private final String projectName = "Test Project";
//
//	// test database file name
//	private static String testDbFileName = "testDB" + SQLiteDatabase.DB_EXTENSION_NAME;
//
//
//	@BeforeClass
//	public static void superSetup()
//	{
//		PropertyConfigurator.configure(AllTests.LOGGING_FILE_PATH);
//
//		// delete the old DB file
//		File oldDB = new File(testDbFileName);
//		oldDB.delete();
//	}
//
//
//	@Before
//	public void setUp()
//	{
//		try
//		{
//			// create a sqlite factory
//			sqliteDbFactory = new SQLiteDBFactory();
//
//			// create a default test server and pass in the factory
//			testServer = new StorytellerServer(sqliteDbFactory);
//
//			// create a project for the test server
//			testServer.createProject(testDbFileName, null, "Mark", "Mahoney", "mmahoney@carthage.edu",
//					UUID.randomUUID().toString(), String.valueOf((new Date()).getTime()), "Test Project");
//		}
//		catch (Exception ex)
//		{
//			// this should never happen but if it does, fail all tests
//			ex.printStackTrace();
//			fail(ex.getStackTrace().toString());
//		}
//	}
//
//
//	@After
//	public void tearDown() throws Exception
//	{
//		// get a handle to the test db file
//		File dbFile = new File(testDbFileName);
//
//		// if the test file exists
//		if (dbFile.exists())
//		{
//			// close the test project
//			testServer.closeProject();
//
//			// delete the project
//			testServer.deleteProject(testDbFileName);
//
//		}
//	}
//
//
//	@Test
//	public void testCreateNewProjectThatDoesNotExist()
//	{
//		// we can't use the test server in this case
//		try
//		{
//			// filename of file that does not exists
//			String fileName = "fileThatDoesNotExist" + SQLiteDatabase.DB_EXTENSION_NAME;
//
//			// create a filename that does not exist
//			File nonExistentFile = new File(fileName);
//
//			// if the file exists for some reason
//			if (nonExistentFile.exists())
//			{
//				// fail the test
//				fail("File name already exists!!!");
//			}
//
//			// create a server and pass in the factory
//			StorytellerServer server = new StorytellerServer(sqliteDbFactory);
//
//			// create the new project with the test file name
//			server.createProject(fileName, null, "Mark", "Mahoney", "mmahoney@carthage.edu",
//					UUID.randomUUID().toString(), String.valueOf((new Date()).getTime()), projectName);
//
//			// close the (locally made) project and delete it
//			server.closeProject();
//			server.deleteProject(fileName);
//		}
//		catch (Exception ex)
//		{
//			// couldn't create the project, fail the test
//			fail(ex.getMessage());
//		}
//	}
//
//
//	@Test
//	public void testCreateNewProjectThatAlreadyExists()
//	{
//		// the test server object already exists with the file testDbFileName
//		try
//		{
//			// close the test project
//			testServer.closeProject();
//
//			// make sure the db file still exists pre-delete
//			File closedDbFile = new File(testDbFileName);
//
//			// if the db file does not exist
//			if (!closedDbFile.exists())
//			{
//				fail("DB file was not saved");
//			}
//
//			// create the new project with the same test file name
//
//			testServer.createProject(testDbFileName, null, "Mark", "Mahoney", "mmahoney@carthage.edu",
//					UUID.randomUUID().toString(), String.valueOf((new Date()).getTime()), projectName);
//
//			// we should never get here
//			fail("Created a project that already exists!");
//		}
//		catch (Exception ex)
//		{
//			// do nothing, exception expected here
//		}
//	}
//
//
//	@Test
//	public void testOpenProjectThatExists()
//	{
//		// the test server has created the project already
//		try
//		{
//			// close the test server project
//			testServer.closeProject();
//
//			// now we know that a project exists, lets create a new server
//			// and try and open the existing project
//			testServer = new StorytellerServer(sqliteDbFactory);
//
//			// attempt to open a project we know exists already
//			testServer.openProject(testDbFileName, projectName);
//		}
//		catch (Exception ex)
//		{
//			// can't open an existing project, fail the test
//			fail(ex.getMessage());
//		}
//	}
//
//
//	@Test
//	public void testOpenProjectThatDoesntExists()
//	{
//		// we can't use the test server in this case
//		try
//		{
//			// filename of file that does not exists
//			String fileName = "fileThatDoesNotExist" + SQLiteDatabase.DB_EXTENSION_NAME;
//
//			// create a filename that does not exist
//			File nonExistentFile = new File(fileName);
//
//			// if the file exists for some reason
//			if (nonExistentFile.exists())
//			{
//				// fail the test
//				fail("File name already exists!!!");
//			}
//
//			// create a server and pass in the factory
//			StorytellerServer server = new StorytellerServer(sqliteDbFactory);
//
//			// open the project with the test file name
//			server.openProject(fileName, projectName);
//
//			fail("Opened a project that doesn't exist");
//		}
//		catch (Exception ex)
//		{
//			// do nothing, exception expected here
//		}
//	}
//
//
//	@Test
//	public void testCloseProject()
//	{
//		try
//		{
//			// close the test project
//			testServer.closeProject();
//
//			// make sure the db file associated with the project still exists
//			File closedDbFile = new File(testDbFileName);
//
//			// if the db file does not exist
//			if (!closedDbFile.exists())
//			{
//				fail("DB file was not saved");
//			}
//		}
//		catch (DBAbstractionException ex)
//		{
//			// couldn't close the project
//			fail(ex.getMessage());
//		}
//	}
//
//
//	@Test
//	public void testDeleteProjectThatExists()
//	{
//		try
//		{
//			// close the test project
//			testServer.closeProject();
//
//			// make sure the db file still exists pre-delete
//			File closedDbFile = new File(testDbFileName);
//
//			// if the db file does not exist
//			if (!closedDbFile.exists())
//			{
//				fail("DB file was not saved");
//			}
//
//			// delete the project
//			testServer.deleteProject(testDbFileName);
//
//			// attempt to open the file again
//			closedDbFile = new File(testDbFileName);
//
//			// if the db file does exist
//			if (closedDbFile.exists())
//			{
//				fail("DB file was not deleted");
//			}
//		}
//		catch (Exception ex)
//		{
//			// couldn't delete the project
//			fail(ex.getMessage());
//		}
//	}
//
//
//	@Test
//	public void testDeleteProjectThatDoesNotExists()
//	{
//		try
//		{
//			// filename of file that does not exists
//			String fileName = "fileThatDoesNotExist" + SQLiteDatabase.DB_EXTENSION_NAME;
//
//			// create a filename that does not exist
//			File nonExistentFile = new File(fileName);
//
//			// if the file exists for some reason
//			if (nonExistentFile.exists())
//			{
//				// fail the test
//				fail("File name already exists!!!");
//			}
//
//			// delete the project
//			testServer.deleteProject(fileName);
//
//			// we should not get here
//			fail("Cannot delete a project that doesn't exist");
//		}
//		catch (Exception ex)
//		{
//			// do nothing, exception is expected here
//		}
//	}
//}
