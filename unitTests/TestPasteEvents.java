//package unitTests;
//
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.fail;
//
//import java.io.File;
//import java.util.Map;
//import java.util.UUID;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.json.JSONArray;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import Playback.MockPlaybackViewer;
//import StorytellerServer.DBFactory;
//import StorytellerServer.SQLiteDBFactory;
//import StorytellerServer.SQLiteDatabase;
//import StorytellerServer.StorytellerServer;
//import StorytellerServer.ide.IDEProxy;
//import StorytellerServer.playback.PlaybackProxy;
//
//public class TestPasteEvents
//{
//	private static String pathToServer="127.0.0.1";
//
//	//These will be involved in pasting tests below
//	private static final String stringToPaste1 = "throw new RuntimeExeption(\"You shall not ... pass!!\");"; 
//	private static final String stringToPaste2 = "public static final String";
//	private static final String stringToCut = "public static final String GENERAL_CONSTANT = \"3.1415\";";
//
//	private static final String testProjectName = "TestPasteEvents";
//
//	//IDE generated id of the root directory
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
//	private static String testDbFileName = "unitTestDBForPasteEvents" + SQLiteDatabase.DB_EXTENSION_NAME;
//
//	//this is the IDEProxy that the IDE Faker will talk to
//	private static IDEProxy serverProxy = null;
//
//	//this is the PlaybackBackProxy that the browser emulator will listen to
//	private static PlaybackProxy playbackProxy = null;
//
//	private static Thread ideThread = null;
//
//	private static Thread playbackThread = null;
//
//	private static String defaultDocId = null;
//	
//	private static Logger logger = Logger.getLogger(TestPasteEvents.class.getName());
//	private static Logger timer = Logger.getLogger("timing."+TestPasteEvents.class.getName());
//
//	/*
//	 * So, we set this test up statically, so we have the same database, the same
//	 * project, the same server and the same mockIDE for all of them.  
//	 * 
//	 * Why?  Because A. It's more lifelike and B. the constant switching isn't 
//	 * consistent on Windows machines due to the funkiness of Java's deletes
//	 */
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception
//	{
//		PropertyConfigurator.configure(AllTests.LOGGING_FILE_PATH);
//		
//		
//		File oldDB = new File(testDbFileName);
//		if (oldDB.exists())
//			oldDB.delete();
//
//		sqliteDbFactory = new SQLiteDBFactory();
//		//create a default test server and pass in the factory
//		testServer = new StorytellerServer(sqliteDbFactory);
//
//		//create a thread to listen for IDE data
//		serverProxy = new IDEProxy(testServer);
//		ideThread = new Thread(serverProxy);
//		//create a thread to listen for playback requests
//		playbackProxy = new PlaybackProxy(testServer);
//		playbackThread = new Thread(playbackProxy);
//
//		ideThread.start();
//		playbackThread.start();
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
//		MockPlaybackViewer.setDevFirstName("Mark");
//		MockPlaybackViewer.setDevLastName("Mahoney");
//		MockPlaybackViewer.setDevEmailAddress("mmahoney@carthage.edu");
//		mockIDE.createNewProject(testDbFileName, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, testProjectName);
//	
//		defaultDocId = UUID.randomUUID().toString();
//
//		//name of a new document
//		String newDocName = "AllSortsOfPasting.java";
//
//		//create a new document event in the root dir and send it to the server
//		mockIDE.sendCreateNewDocumentEventToServer(defaultDocId, newDocName, rootDirId);
//		mockIDE.enableDateFaking();			//in order to fake pasting, you must enable DateFaking to have good results
//
//	}
//
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception 
//	{
//		logger.debug("Closing down playback and serverproxy");
//
//		mockIDE.closeProject();
//
//		//playbackProxy.quit();
//		serverProxy.quit();
//
//		//give the threads a moment to die
//		try
//		{
//			while (playbackThread.isAlive()||ideThread.isAlive())
//			{
//				Thread.sleep(100);
//			}
//		} 
//		catch (InterruptedException e)
//		{
//			logger.fatal("",e);
//		}
//	}
//
//	/*
//	 * In case you are wondering, the first three tests (firstTest...SecondTest...ThirdTest...) build off of each other.  
//	 * Why?  Because it's easier to follow.
//	 */
//
//	@Test
//	public void testAllPastes() throws Exception
//	{
//		firstTestNoPasting();
//		secondTestFirstPaste();
//		thirdTestSecondPaste();
//		fourthTestCutAndPaste();
//	}
//	
//	public void firstTestNoPasting()	//this test needs to get done first, so that we are sure no pasteEvents exist yet
//	{
//		MockPlaybackViewer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		assertEquals(0,playbackViewer.getPastedStrings(defaultDocId).size());		//double verify that nothing has been pasted yet
//
//		mockIDE.sendStringToServer(stringToPaste1, 0);
//		mockIDE.sendStringToServer(stringToPaste2+" PATH_TO_SERVER=\"192.168.1.1\";\n\n", 0);
//
//		logger.trace(mockIDE.getCurrentBuffer());
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		assertEquals(0,playbackViewer.getPastedStrings(defaultDocId).size());		//triple verify that nothing has been pasted yet
//
//	}
//
//
//	public void secondTestFirstPaste() throws Exception
//	{
//		int indexToPasteAt = mockIDE.insertStringAfterToServer("\n", "\"192.168.1.1\";", 1);
//
//		timer.trace("pasting "+stringToPaste2);
//		mockIDE.copyAndPasteToServer(stringToPaste2, 1, indexToPasteAt);
//		timer.trace("pasted "+stringToPaste2);
//		mockIDE.sendStringToServer(" GENERAL_CONSTANT = \"3.1415\";", indexToPasteAt+stringToPaste2.length());
//
//		assertEquals("public static final String PATH_TO_SERVER=\"192.168.1.1\";\npublic static final String GENERAL_CONSTANT = \"3.1415\";\n\nthrow new RuntimeExeption(\"You shall not ... pass!!\");"
//				, mockIDE.getCurrentBuffer());
//
//		MockPlaybackViewer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(defaultDocId));	//check that all is well so far
//
//		timer.trace("pasting "+stringToPaste2);
//		mockIDE.copyAndPasteToServer(stringToPaste2, 1, 1+mockIDE.findStringInCurrentBuffer(" GENERAL_CONSTANT = \"3.1415\";", 1, false));	//paste another one after the second constant
//		timer.trace("pasted "+stringToPaste2);
//		mockIDE.insertStringAfterToServer(" NAME = \"Mark Mahoney\";", stringToPaste2, 3);
//
//		//Play it back
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//
//		assertEquals("public static final String PATH_TO_SERVER=\"192.168.1.1\";\npublic static final String GENERAL_CONSTANT " +
//				"= \"3.1415\";\npublic static final String NAME = \"Mark Mahoney\";\nthrow new RuntimeExeption(\"You shall not " +
//				"... pass!!\");", mockIDE.getCurrentBuffer());
//
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(defaultDocId));
//
//		Map<String, JSONArray> pastedEvents = playbackViewer.getPastedStrings(defaultDocId);
//		assertEquals(2, pastedEvents.size());
//
//		for (String thisKey : pastedEvents.keySet())	//this loop should only get run twice
//		{
//			JSONArray array = pastedEvents.get(thisKey);
//			// all the pastes thus far have been from index 0 and are
//			assertEquals(stringToPaste2, array.getString(0));
//			assertEquals(0, array.getInt(2));	
//
//			//The hashmap doesn't guarantee order, so there are exactly two possibilities
//			assertEquals(true,  array.getInt(1)==mockIDE.findStringInCurrentBuffer(stringToPaste2, 2, true) ||
//					array.getInt(1)==mockIDE.findStringInCurrentBuffer(stringToPaste2, 3, true));
//		}
//		logger.trace(pastedEvents.toString());
//
//	}
//
//	public void thirdTestSecondPaste() throws Exception							//put something at the beginning and the end  (part 3 of 3)
//	{
//
//		mockIDE.copyAndPasteToServer(stringToPaste1, 1, 0);			//put the throws bit at the beginning of the document
//		mockIDE.insertStringAfterToServer("\n", stringToPaste1, 1);	//and add a newline after (now the first) throws
//
//		mockIDE.sendStringToServer("\n", mockIDE.getCurrentLength());
//		mockIDE.copyAndPasteToServer(stringToPaste1, 1, mockIDE.getCurrentLength());	//paste at the end, from the already pasted spot
//
//		//sanity check on the MockIDE
//		assertEquals("throw new RuntimeExeption(\"You shall not ... pass!!\");\n" +
//				"public static final String PATH_TO_SERVER=\"192.168.1.1\";\n" +
//				"public static final String GENERAL_CONSTANT = \"3.1415\";\n" +
//				"public static final String NAME = \"Mark Mahoney\";\n" +
//				"throw new RuntimeExeption(\"You shall not ... pass!!\");\n" +
//				"throw new RuntimeExeption(\"You shall not ... pass!!\");",
//				mockIDE.getCurrentBuffer());
//
//		MockPlaybackViewer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(defaultDocId));	//check that all is well so far
//
//		Map<String, JSONArray> pastedEvents = playbackViewer.getPastedStrings(defaultDocId);
//		assertEquals(4, pastedEvents.size());		//there should be 4 total pasted things
//
//		for (String thisKey : pastedEvents.keySet())	//this loop should get run 4 times
//		{
//			JSONArray array = pastedEvents.get(thisKey);
//
//			String str = array.getString(0);
//			if (str.equals(stringToPaste1))
//			{
//				int pastedOriginIndex = array.getInt(2);
//				if (pastedOriginIndex==mockIDE.findStringInCurrentBuffer(stringToPaste1, 1, true))		//this was the second paste i.e. the 3rd over all
//				{
//					assertEquals(true, array.getInt(1)==mockIDE.findStringInCurrentBuffer(stringToPaste1, 3, true));
//				}
//				else if (pastedOriginIndex==mockIDE.findStringInCurrentBuffer(stringToPaste1, 2, true))		//this was the first paste i.e. the 1st overall
//				{
//					assertEquals(true, array.getInt(1)==mockIDE.findStringInCurrentBuffer(stringToPaste1, 1, true));
//				}
//				else 
//				{
//					fail("Unexpected paste origin");
//				}
//			}
//			else if (str.equals(stringToPaste2))
//			{
//				//both the origins of stringToPaste2 are from the first one
//				assertEquals(mockIDE.findStringInCurrentBuffer(stringToPaste2,1,true), array.getInt(2));
//				//they'll be located at one of two points
//				assertEquals(true,  array.getInt(1)==mockIDE.findStringInCurrentBuffer(stringToPaste2, 2, true) ||
//						array.getInt(1)==mockIDE.findStringInCurrentBuffer(stringToPaste2, 3, true));
//			}
//			else
//			{
//				fail("The string pasted was definately not what was expected");
//			}
//		}
//
//	}
//
//	public void fourthTestCutAndPaste() throws Exception
//	{
//		mockIDE.sendStringToServer("\n", mockIDE.getCurrentLength());
//		timer.trace("pasting "+stringToCut);
//		mockIDE.cutAndPaste(stringToCut, 1, mockIDE.getCurrentLength());		//cut public static final String GENERAL_CONSTANT = "3.1415" and paste it on the end
//		timer.trace("pasted "+stringToCut);
//		//sanity check on the MockIDE
//		assertEquals("throw new RuntimeExeption(\"You shall not ... pass!!\");\n" +
//				"public static final String PATH_TO_SERVER=\"192.168.1.1\";\n" +
//				"\n" +
//				"public static final String NAME = \"Mark Mahoney\";\n" +
//				"throw new RuntimeExeption(\"You shall not ... pass!!\");\n" +
//				"throw new RuntimeExeption(\"You shall not ... pass!!\");\n" +
//				"public static final String GENERAL_CONSTANT = \"3.1415\";",
//				mockIDE.getCurrentBuffer());
//
//		MockPlaybackViewer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(defaultDocId));	//check that all is well so far
//
//		Map<String, JSONArray> pastedEvents = playbackViewer.getPastedStrings(defaultDocId);
//		assertEquals(5, pastedEvents.size());		//there should be 5 total pasted things
//
//		for (String thisKey : pastedEvents.keySet())	//this loop should get run 5 times
//		{
//			JSONArray array = pastedEvents.get(thisKey);
//
//			String str = array.getString(0);
//			if (str.equals(stringToPaste1))
//			{
//				int pastedOriginIndex = array.getInt(2);
//				if (pastedOriginIndex==mockIDE.findStringInCurrentBuffer(stringToPaste1, 1, true))		//this was the second paste i.e. the 3rd over all
//				{
//					assertEquals(true, array.getInt(1)==mockIDE.findStringInCurrentBuffer(stringToPaste1, 3, true));
//				}
//				else if (pastedOriginIndex==mockIDE.findStringInCurrentBuffer(stringToPaste1, 2, true))		//this was the first paste i.e. the 1st overall
//				{
//					assertEquals(true, array.getInt(1)==mockIDE.findStringInCurrentBuffer(stringToPaste1, 1, true));
//				}
//				else 
//				{
//					fail("Unexpected paste origin");
//				}
//			}
//			else if (str.equals(stringToPaste2))
//			{
//				//both the origins of stringToPaste2 are from the first one
//				assertEquals(mockIDE.findStringInCurrentBuffer(stringToPaste2,1,true), array.getInt(2));
//
//			}
//			else if (str.equals(stringToCut))
//			{
//				assertEquals(true, array.getInt(1)==mockIDE.findStringInCurrentBuffer(stringToCut, 1, true));
//			}
//			else
//			{
//				fail("The string pasted was definately not what was expected");
//			}
//		}
//
//	}
//
//}
