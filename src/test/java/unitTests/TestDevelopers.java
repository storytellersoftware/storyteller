//package unitTests;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import StorytellerEntities.Developer;
//import StorytellerEntities.DeveloperGroup;
//import StorytellerServer.DBFactory;
//import StorytellerServer.SQLiteDBFactory;
//import StorytellerServer.SQLiteDatabase;
//import StorytellerServer.StorytellerServer;
//import StorytellerServer.ide.IDEProxy;
//
//public class TestDevelopers 
//{
//	private static String pathToServer="127.0.0.1";
//
//	private static String rootDirId;
//	
//	private static MockIDE mockIDE; 
//
//	private static DBFactory sqliteDbFactory;
//
//	//default test server
//	private static StorytellerServer testServer;
//	
//	//test database file name
//	private static String testDbFileName = "unitTestDBForDevelopers" + SQLiteDatabase.DB_EXTENSION_NAME;
//	
//	//this is the IDEProxy that the IDE Faker will talk to
//	private static IDEProxy ideProxy = null;
//	
//	private static Thread ideThread = null;
//	
//	private static Logger logger = Logger.getLogger(TestFileAndDirectoryEvents.class.getName());
//	
//	private static String dev1FirstName = "Mark";
//	private static String dev1LastName = "Mahoney";
//	private static String dev1Email = "mmahoney@carthage.edu";
//	
//	private static String dev2FirstName = "Voytek";
//	private static String dev2LastName = "Snarski";
//	private static String dev2Email = "wsnarski@carthage.edu";
//	
//	private static String dev3FirstName = "Kevin";
//	private static String dev3LastName = "Lubick";
//	private static String dev3Email = "klubick@carthage.edu";
//	
//	private static String dev4FirstName = "Don";
//	private static String dev4LastName = "Kuntz";
//	private static String dev4Email = "dkuntz@carthage.edu";
//	
//	private static String dev5FirstName = "Adam";
//	private static String dev5LastName = "Van de Ven";
//	private static String dev5Email = "avandeven@carthage.edu";
//	
//	@Before
//	public void setUp() throws Exception 
//	{
//		PropertyConfigurator.configure(AllTests.LOGGING_FILE_PATH);
//		File oldDB = new File(testDbFileName);
//		if (oldDB.exists())
//			oldDB.delete();
//		
//		sqliteDbFactory = new SQLiteDBFactory();
//		//create a default test server and pass in the factory
//		testServer = new StorytellerServer(sqliteDbFactory);
//		
//		//create a thread to listen for IDE data
//		ideProxy = new IDEProxy(testServer);
//		ideThread = new Thread(ideProxy);
//		
//		ideThread.start();
//		try
//		{
//			Thread.sleep(100);
//		} 
//		catch (InterruptedException e)
//		{
//			logger.fatal("",e);
//		}
//		//create a project using the mockIDE for the test server
//		rootDirId = UUID.randomUUID().toString();
//		
//		mockIDE = new MockIDE(pathToServer);
//
//		mockIDE.createNewProject(testDbFileName, getClass().getName()+"Project", dev1FirstName, dev1LastName, dev1Email, rootDirId);
//	}
//
//	@After
//	public void tearDown() throws Exception 
//	{
//		logger.debug("Closing down playback and serverproxy");
//		
//		mockIDE.closeProject();
//		
//		ideProxy.quit();
//		
//		//give the threads a moment to die
//		try
//		{
//			while (ideThread.isAlive())
//			{
//				Thread.sleep(100);
//			}
//		} 
//		catch (InterruptedException e)
//		{
//			logger.fatal("What does interupted mean?",e);
//		}
//	}
//
//	@Test
//	public void testAddNewDeveloper() throws Exception			
//	{
//		//assert that wsnarski@carthage.edu is not in the db as a developer
//		assertNull(testServer.getLocalDatabase().getDeveloperByEmailAddress(dev2Email));
//		
//		//create the dev
//		mockIDE.createNewDeveloper(dev2FirstName, dev2LastName, dev2Email);
//		
//		//assert that wsnarski@carthage.edu is now in the db as a developer
//		assertNotNull(testServer.getLocalDatabase().getDeveloperByEmailAddress(dev2Email));
//	}
//	
//	@Test
//	public void testChangeDeveloperGroups() throws Exception			
//	{
//		//create some developers
//		mockIDE.createNewDeveloper(dev1FirstName, dev1LastName, dev1Email);	
//		mockIDE.createNewDeveloper(dev2FirstName, dev2LastName, dev2Email);
//		
//		//get access to the developers
//		Developer dev1 = testServer.getLocalDatabase().getDeveloperByEmailAddress(dev1Email);
//		Developer dev2 = testServer.getLocalDatabase().getDeveloperByEmailAddress(dev2Email);
//		
//		//add the dev ids to a list
//		List < String > devIds = new ArrayList < String >(); 
//		devIds.add(dev1.getId());
//		devIds.add(dev2.getId());
//
//		//add the dev emails to a list
//		List < String > devEmails = new ArrayList < String >(); 
//		devEmails.add(dev1Email);
//		devEmails.add(dev2Email);
//		
//		//make sure there is not a dev group with dev1 and dev2
//		assertNull(testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds));
//		
//		//change the developers
//		String [] devEmailArray = devEmails.toArray(new String[0]);
//		mockIDE.changeDevelopers(devEmailArray);
//		
//		//make sure there is now a dev group with dev1 and dev2
//		assertNotNull(testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds));
//
//		//get the dev group and make sure the mock ide knows thats the right one
//		DeveloperGroup currentDevGroup = testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds);
//		assertEquals(currentDevGroup.getId(), mockIDE.getCurrentDevGroupID());
//	}
//	
//	@Test
//	public void testChangeDeveloperGroupsMultipleTimes() throws Exception			
//	{
//		//create some developers
//		mockIDE.createNewDeveloper(dev1FirstName, dev1LastName, dev1Email);	
//		mockIDE.createNewDeveloper(dev2FirstName, dev2LastName, dev2Email);
//		mockIDE.createNewDeveloper(dev3FirstName, dev3LastName, dev3Email);
//		mockIDE.createNewDeveloper(dev4FirstName, dev4LastName, dev4Email);
//		mockIDE.createNewDeveloper(dev5FirstName, dev5LastName, dev5Email);
//
//		//get access to the developers
//		Developer dev1 = testServer.getLocalDatabase().getDeveloperByEmailAddress(dev1Email);	
//		Developer dev2 = testServer.getLocalDatabase().getDeveloperByEmailAddress(dev2Email);
//		Developer dev3 = testServer.getLocalDatabase().getDeveloperByEmailAddress(dev3Email);
//		Developer dev4 = testServer.getLocalDatabase().getDeveloperByEmailAddress(dev4Email);
//		Developer dev5 = testServer.getLocalDatabase().getDeveloperByEmailAddress(dev5Email);
//
//		List < String > devIds = new ArrayList < String >(); 
//		devIds.add(dev1.getId());
//		devIds.add(dev2.getId());
//		devIds.add(dev3.getId());
//		
//		List < String > devEmails = new ArrayList < String >(); 
//		devEmails.add(dev1Email);
//		devEmails.add(dev2Email);
//		devEmails.add(dev3Email);
//				
//		String [] devEmailArray = devEmails.toArray(new String[0]);
//		mockIDE.changeDevelopers(devEmailArray);
//		
//		//make sure there is now a dev group with dev1, dev2, and dev3
//		assertNotNull(testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds));
//		DeveloperGroup currentDevGroup = testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds);
//		assertEquals(currentDevGroup.getId(), mockIDE.getCurrentDevGroupID());
//		
//		
//		//clear the lists
//		devIds.clear();
//		devEmails.clear();
//		
//		devIds.add(dev3.getId());
//		devIds.add(dev4.getId());
//		devIds.add(dev5.getId());
//		
//		devEmails.add(dev3Email);
//		devEmails.add(dev4Email);
//		devEmails.add(dev5Email);
//		
//		devEmailArray = devEmails.toArray(new String[0]);
//		mockIDE.changeDevelopers(devEmailArray);
//		
//		//make sure there is now a dev group with dev3, dev4, and dev5
//		assertNotNull(testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds));
//		currentDevGroup = testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds);
//		assertEquals(currentDevGroup.getId(), mockIDE.getCurrentDevGroupID());
//
//		
//		//clear the lists
//		devIds.clear();
//		devEmails.clear();
//		
//		devIds.add(dev1.getId());
//		
//		devEmails.add(dev1Email);
//		
//		devEmailArray = devEmails.toArray(new String[0]);
//		mockIDE.changeDevelopers(devEmailArray);
//		
//		//make sure there is now a dev group with dev1 
//		assertNotNull(testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds));
//		currentDevGroup = testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds);
//		assertEquals(currentDevGroup.getId(), mockIDE.getCurrentDevGroupID());
//
//		
//		//clear the lists
//		devIds.clear();
//		devEmails.clear();
//		
//		devIds.add(dev1.getId());
//		devIds.add(dev2.getId());
//		devIds.add(dev3.getId());
//		devIds.add(dev4.getId());
//		devIds.add(dev5.getId());
//		
//		devEmails.add(dev1Email);
//		devEmails.add(dev2Email);
//		devEmails.add(dev3Email);
//		devEmails.add(dev4Email);
//		devEmails.add(dev5Email);
//		
//		devEmailArray = devEmails.toArray(new String[0]);
//		mockIDE.changeDevelopers(devEmailArray);
//		
//		//make sure there is now a dev group with dev1 - dev5
//		assertNotNull(testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds));
//		currentDevGroup = testServer.getLocalDatabase().getDeveloperGroupByDeveloperIds(devIds);
//		assertEquals(currentDevGroup.getId(), mockIDE.getCurrentDevGroupID());
//	}
//}
