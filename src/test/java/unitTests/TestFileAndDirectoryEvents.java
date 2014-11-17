//package unitTests;
//
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.File;
//import java.util.List;
//import java.util.UUID;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import Playback.MockPlaybackViewer;
//import Playback.PlaybackEventRenderer;
//import StorytellerServer.DBFactory;
//import StorytellerServer.SQLiteDBFactory;
//import StorytellerServer.SQLiteDatabase;
//import StorytellerServer.StorytellerServer;
//import StorytellerServer.ide.IDEProxy;
//import StorytellerServer.playback.PlaybackProxy;
//
//public class TestFileAndDirectoryEvents 
//{
//	private static String pathToServer="127.0.0.1";
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
//	private static String testDbFileName = "unitTestDBForTestFileAndDirectoryEvents" + SQLiteDatabase.DB_EXTENSION_NAME;
//	
//	//this is the IDEProxy that the IDE Faker will talk to
//	private static IDEProxy ideProxy = null;
//
//	//this is the PlaybackBackProxy that the browser emulator will listen to
//	private static PlaybackProxy playbackProxy = null;
//	
//	private static Thread ideThread = null;
//	
//	private static Thread playbackThread = null;
//	
//	private static Logger logger = Logger.getLogger(TestFileAndDirectoryEvents.class.getName());
//	
//	
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception 
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
//			logger.fatal("What is interupted mean?",e);
//		}
//		//create a project using the mockIDE for the test server
//		rootDirId = UUID.randomUUID().toString();
//		
//		mockIDE = new MockIDE(pathToServer);
//
//		MockPlaybackViewer.setDevFirstName("Mark");
//		MockPlaybackViewer.setDevLastName("Mahoney");
//		MockPlaybackViewer.setDevEmailAddress("mmahoney@carthage.edu");
//		mockIDE.createNewProject(testDbFileName, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, TestFileAndDirectoryEvents.class.getName()+"Project");
//	
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception 
//	{
//		logger.debug("Closing down playback and serverproxy");
//		
//		mockIDE.closeProject();
//		
//		//playbackProxy.quit();
//		ideProxy.quit();
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
//			logger.fatal("What does interupted mean?",e);
//		}
//
//	}
//
//	@Test
//	public void testHasRootDir()			//makes sure that the rootDir exists before we do anything
//	{
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		assertEquals(true, playbackViewer.directoryExists(rootDirId));
//	}
//	
//	
//	@Test
//	public void testCreateDirectory()
//	{
//		//Set up a test Directory
//		String testDirID = UUID.randomUUID().toString();
//		String testDirName = "testCreateDirectory";
//		
//		//Now make a subDirectory in the testCreateDirectory/ folder
//		String testSubDirID = UUID.randomUUID().toString();
//		String testSubDirName = "testSubDirectory";
//		
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(testDirID, testDirName, rootDirId);
//		
//		//run the playback and test
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		assertEquals(true,playbackViewer.directoryExists(testDirID));
//		assertEquals(false,playbackViewer.directoryExists("testCreateDirectory"));
//		assertEquals(false,playbackViewer.directoryExists(testSubDirID));			//this shouldn't have been created yet
//		
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(testSubDirID, testSubDirName, testDirID);
//		
//		//rerun the playback and test
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		//Make sure this still works
//		assertEquals(true,playbackViewer.directoryExists(testDirID));
//		assertEquals(false,playbackViewer.directoryExists("testCreateDirectory"));
//		
//		assertEquals(true,playbackViewer.directoryExists(testSubDirID));
//		assertEquals(false,playbackViewer.directoryExists("testSubDirectory"));
//		
//		//Test that 
//		assertEquals(true,playbackViewer.getAllSubdirectoryNames(rootDirId).size()>=1);			//The root directory may have more stuff in it than just the directory we made.
//		assertEquals(true,playbackViewer.getAllSubdirectoryNames(rootDirId).contains(testDirName));
//		
//		//There should be exactly one thing in the subdirectory
//		assertEquals(1,playbackViewer.getAllSubdirectoryNames(testDirID).size());
//		assertEquals(testSubDirName,playbackViewer.getAllSubdirectoryNames(testDirID).get(0));
//		
//		
//	}
//	
//	@Test
//	public void testCreateDocument()
//	{
//		//Set up a test Directory for our document to go
//		String testDirID = UUID.randomUUID().toString();
//		String testDirName = "testCreateDocument";
//		
//		//create the newDirectory
//		mockIDE.sendCreateNewDirectoryEventToServer(testDirID, testDirName, rootDirId);
//		
//		//run the playback and test
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		assertEquals(0, playbackViewer.getAllDocumentNames(testDirID).size());		//make sure the directories start off empty
//		
//		//Set up a test Document
//		String testDocID = UUID.randomUUID().toString();
//		String testDocName = "iExistThereforIam.txt";
//		
//		mockIDE.sendCreateNewDocumentEventToServer(testDocID, testDocName, testDirID);
//		
//		//rerun the playback and test
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		assertEquals(1, playbackViewer.getAllDocumentNames(testDirID).size());
//		assertEquals(testDocName,playbackViewer.getAllDocumentNames(testDirID).get(0));
//		
//	}
//	
//	@Test
//	public void testMoveDocument()
//	{
//		String moveExampleDirId = UUID.randomUUID().toString();		//"/MoveExample/"
//		String dirOddsId = UUID.randomUUID().toString();		//"/MoveExample/dirOdds"
//		String dirEvensId = UUID.randomUUID().toString();		//"/MoveExample/dirEvens"
//		
//		String moveExampleDirName = "testMoveDocument";
//		String dirOddsName = "dirOdds";
//		String dirEvensName = "dirEvens";
//		
//		String docId1 = UUID.randomUUID().toString();
//		String docId2 = UUID.randomUUID().toString();
//		String docId3 = UUID.randomUUID().toString();
//		String docId4 = UUID.randomUUID().toString();
//		String docId5 = UUID.randomUUID().toString();
//		
//		String docName1 = "document1.txt";
//		String docName2 = "document2.txt";
//		String docName3 = "document3.txt";
//		String docName4 = "document4.txt";
//		String docName5 = "document5.txt";
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(moveExampleDirId, moveExampleDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(dirOddsId, dirOddsName, moveExampleDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(dirEvensId, dirEvensName, moveExampleDirId);
//		
//		mockIDE.sendCreateNewDocumentEventToServer(docId1, docName1, dirOddsId);
//		mockIDE.sendCreateNewDocumentEventToServer(docId3, docName3, dirOddsId);
//		mockIDE.sendCreateNewDocumentEventToServer(docId5, docName5, dirOddsId);
//		
//		mockIDE.sendCreateNewDocumentEventToServer(docId2, docName2, dirEvensId);
//		mockIDE.sendCreateNewDocumentEventToServer(docId4, docName4, dirEvensId);
//		
//		//run the playback and test
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		List<String> odds = playbackViewer.getAllDocumentNames(dirOddsId);
//		List<String> evens =playbackViewer.getAllDocumentNames(dirEvensId);
//		
//		assertEquals(3, odds.size());
//		assertEquals(2,evens.size());
//		assertEquals(true,odds.contains(docName1));
//		assertEquals(true,odds.contains(docName3));
//		assertEquals(true,odds.contains(docName5));
//		
//		assertEquals(true,evens.contains(docName2));
//		assertEquals(true,evens.contains(docName4));
//		
//		//Shuffle up the files (move the lows into odds and the high numbers into evens
//		mockIDE.sendMoveDocumentEventToServer(docId2,dirEvensId,dirOddsId);
//		mockIDE.sendMoveDocumentEventToServer(docId5,dirOddsId,dirEvensId);
//		
//			
//		
//		//rerun the playback and test
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		
//		odds = playbackViewer.getAllDocumentNames(dirOddsId);
//		evens =playbackViewer.getAllDocumentNames(dirEvensId);
//		
//		assertEquals(3, odds.size());
//		assertEquals(2,evens.size());
//		assertEquals(true,odds.contains(docName1));
//		assertEquals(true,odds.contains(docName2));
//		assertEquals(true,odds.contains(docName3));
//		
//		assertEquals(true,evens.contains(docName4));
//		assertEquals(true,evens.contains(docName5));
//	}
//	
//	
//	
//	
//	@Test
//	public void testRenameDocument()
//	{
//		//Set up a test Directory for our document to go
//		String testDirID = UUID.randomUUID().toString();
//		String testDirName = "testRenameDocument";
//		
//		//Make a pair of documents, one misspelled and one not.  
//		String badFileID = UUID.randomUUID().toString();
//		String goodFileID = UUID.randomUUID().toString();
//		String badFileName = "mispeledDocument.txt";
//		String goodFileName = "IamSpelledCorrectly.java";
//		String renamedFileName = "misspelledDocument.txt";
//		
//		//create the newDirectory
//		mockIDE.sendCreateNewDirectoryEventToServer(testDirID, testDirName, rootDirId);
//		
//		//add the files
//		mockIDE.sendCreateNewDocumentEventToServer(badFileID, badFileName, testDirID);
//		mockIDE.sendCreateNewDocumentEventToServer(goodFileID, goodFileName, testDirID);
//		
//		//Run playback up til now
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		assertEquals(true, playbackViewer.getAllDocumentNames(testDirID).contains(badFileName));
//		assertEquals(true, playbackViewer.getAllDocumentNames(testDirID).contains(goodFileName));
//		assertEquals(false, playbackViewer.getAllDocumentNames(testDirID).contains(renamedFileName));
//		
//		mockIDE.sendRenameDocumentEventToServer(badFileID, badFileName, renamedFileName, testDirID);
//		
//		//rerun the playback and test
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		assertEquals(false, playbackViewer.getAllDocumentNames(testDirID).contains(badFileName));
//		assertEquals(true, playbackViewer.getAllDocumentNames(testDirID).contains(goodFileName));
//		assertEquals(true, playbackViewer.getAllDocumentNames(testDirID).contains(renamedFileName));
//	}
//	
//	@Test
//	public void testDeleteDocument()
//	{
//		//Set up a test Directory for our document to go
//		String testDirID = UUID.randomUUID().toString();
//		String testDirName = "testDeleteDocument";
//		
//		String deleteMeName = "DeleteMe.jpg";
//		String deleteMeID = UUID.randomUUID().toString();
//		String dontDeleteMeName = "IhaveAWifeAndChildren-PleaseDontDeleteMe.xlsx";
//		String dontDeleteMeID = UUID.randomUUID().toString();
//		
//		//create the new Directory
//		mockIDE.sendCreateNewDirectoryEventToServer(testDirID, testDirName, rootDirId);
//		//create the documents
//		mockIDE.sendCreateNewDocumentEventToServer(dontDeleteMeID, dontDeleteMeName, testDirID);
//		mockIDE.sendCreateNewDocumentEventToServer(deleteMeID, deleteMeName, testDirID);
//		
//		//Run playback up til now
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		assertEquals(true, playbackViewer.directoryExists(testDirID));
//		assertEquals(true, playbackViewer.documentExists(deleteMeID));
//		assertEquals(true, playbackViewer.documentExists(dontDeleteMeID));
//		assertEquals(2, playbackViewer.getAllDocumentNames(testDirID).size());
//		assertEquals(true, playbackViewer.getAllDocumentNames(testDirID).contains(deleteMeName));
//		assertEquals(true, playbackViewer.getAllDocumentNames(testDirID).contains(dontDeleteMeName));
//		
//		
//		//delete the file
//		mockIDE.sendDeleteDocumentEventToServer(deleteMeID, deleteMeName, testDirID);
//		
//		//run playback now
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		assertEquals(true, playbackViewer.directoryExists(testDirID));
//		assertEquals(false, playbackViewer.documentExists(deleteMeID));
//		assertEquals(true, playbackViewer.documentExists(dontDeleteMeID));
//		assertEquals(1, playbackViewer.getAllDocumentNames(testDirID).size());
//		assertEquals(false, playbackViewer.getAllDocumentNames(testDirID).contains(deleteMeName));
//		assertEquals(true, playbackViewer.getAllDocumentNames(testDirID).contains(dontDeleteMeName));
//
//	}
//	
//	@Test
//	public void testMoveDirectory()
//	{
//		String testDirID = UUID.randomUUID().toString();
//		String testDirName = "testMoveDirectory";
//		
//		//create the new Directory
//		mockIDE.sendCreateNewDirectoryEventToServer(testDirID, testDirName, rootDirId);
//		
//		//Create two Directories
//		String dontMoveDirName = "IWillStayRightHere";
//		String dontMoveDirId = UUID.randomUUID().toString();
//		String moveDirName = "IveGotToGetOutOfThisPlace";
//		String moveDirId = UUID.randomUUID().toString();
//		
//		//Create some documents to populate the directories
//		String docId1 = UUID.randomUUID().toString();
//		String docId2 = UUID.randomUUID().toString();
//		String docId3 = UUID.randomUUID().toString();
//		String docId4 = UUID.randomUUID().toString();
//		String docId5 = UUID.randomUUID().toString();
//		
//		String docName1 = "document1.txt";
//		String docName2 = "document2.txt";
//		String docName3 = "document3.txt";
//		String docName4 = "document4.txt";
//		String docName5 = "document5.txt";
//		
//		//create the two directories
//		mockIDE.sendCreateNewDirectoryEventToServer(dontMoveDirId, dontMoveDirName, testDirID);
//		mockIDE.sendCreateNewDirectoryEventToServer(moveDirId, moveDirName, testDirID);
//		
//		//add the documents to the directories: 1,2,3 to "dontMove" and 4,5 to move
//		mockIDE.sendCreateNewDocumentEventToServer(docId1, docName1, dontMoveDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(docId2, docName2, dontMoveDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(docId3, docName3, dontMoveDirId);
//		
//		mockIDE.sendCreateNewDocumentEventToServer(docId4, docName4, moveDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(docId5, docName5, moveDirId);
//		
//		//Run playback up til now
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		
//		//Check that the documents are where we expect them to be
//		List<String> dontMoveDocs = playbackViewer.getAllDocumentNames(dontMoveDirId);
//		List<String> moveDocs =playbackViewer.getAllDocumentNames(moveDirId);
//		
//		assertEquals(3, dontMoveDocs.size());
//		assertEquals(2,moveDocs.size());
//		assertEquals(true,dontMoveDocs.contains(docName1));
//		assertEquals(true,dontMoveDocs.contains(docName2));
//		assertEquals(true,dontMoveDocs.contains(docName3));
//		
//		assertEquals(true,moveDocs.contains(docName4));
//		assertEquals(true,moveDocs.contains(docName5));
//		
//		assertEquals(2,playbackViewer.getAllSubdirectoryNames(testDirID).size());
//		int currentNumberOfRootSubdirectories = playbackViewer.getAllSubdirectoryNames(rootDirId).size();
//		
//		//move the moveDir up one directory
//		mockIDE.sendMoveDirectoryEventToServer(moveDirId, testDirID, rootDirId);
//		
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		dontMoveDocs = playbackViewer.getAllDocumentNames(dontMoveDirId);
//		moveDocs =playbackViewer.getAllDocumentNames(moveDirId);
//		
//		//Nothing should have changed with the documents
//		assertEquals(3, dontMoveDocs.size());
//		assertEquals(2,moveDocs.size());
//		assertEquals(true,dontMoveDocs.contains(docName1));
//		assertEquals(true,dontMoveDocs.contains(docName2));
//		assertEquals(true,dontMoveDocs.contains(docName3));
//		
//		assertEquals(true,moveDocs.contains(docName4));
//		assertEquals(true,moveDocs.contains(docName5));
//		
//		//Check that testDir now only has 1 subdirectory
//		assertEquals(1,playbackViewer.getAllSubdirectoryNames(testDirID).size());
//		
//		//Check that the root directory has one more directory than it used to and that root directory contains moveDirName
//		assertEquals(currentNumberOfRootSubdirectories+1,playbackViewer.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(true, playbackViewer.getAllSubdirectoryNames(rootDirId).contains(moveDirName));
//		
//	}
//
//	@Test
//	public void testRenameDirectory()
//	{
//		String testDirID = UUID.randomUUID().toString();
//		String testDirNameIncorrect = "testRnameDirectory";			//yes.  It's misspelled here.  That will be fixed with the call to rename
//		String testDirNameCorrect = "testRenameDirectory";
//		
//		String dontChangeMeDirName = "ImPerfectTheWayIam";
//		String dontChangeMeDirId = UUID.randomUUID().toString();
//		
//		//create the new Directory
//		mockIDE.sendCreateNewDirectoryEventToServer(testDirID, testDirNameIncorrect, rootDirId);
//		
//		//create the subDirectory
//		mockIDE.sendCreateNewDirectoryEventToServer(dontChangeMeDirId, dontChangeMeDirName, testDirID);
//		
//		//Run playback up til now
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		//check that the testDir is okay
//		assertEquals(true, playbackViewer.directoryExists(testDirID));
//		assertEquals(true, playbackViewer.getAllSubdirectoryNames(rootDirId).contains(testDirNameIncorrect));
//		assertEquals(false, playbackViewer.getAllSubdirectoryNames(rootDirId).contains(testDirNameCorrect));
//		//test that its subDirectory is okay
//		assertEquals(1, playbackViewer.getAllSubdirectoryNames(testDirID).size());
//		assertEquals(dontChangeMeDirName, playbackViewer.getAllSubdirectoryNames(testDirID).get(0));
//		
//		
//		//Perform the rename
//		mockIDE.sendRenameDirectoryEventToServer(testDirID, testDirNameIncorrect, testDirNameCorrect, rootDirId);
//		
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		//make sure the directory still exists
//		assertEquals(true, playbackViewer.directoryExists(testDirID));
//		assertEquals(false, playbackViewer.getAllSubdirectoryNames(rootDirId).contains(testDirNameIncorrect));
//		assertEquals(true, playbackViewer.getAllSubdirectoryNames(rootDirId).contains(testDirNameCorrect));
//		//test that its subDirectory is still okay
//		assertEquals(1, playbackViewer.getAllSubdirectoryNames(testDirID).size());
//		assertEquals(dontChangeMeDirName, playbackViewer.getAllSubdirectoryNames(testDirID).get(0));
//	}
//	
//	@Test
//	public void testDeleteDirectory()
//	{
//		String testDirId = UUID.randomUUID().toString();
//		String testDirName = "testDeleteDirectory";			
//
//		
//		String childDirName = "ImNotGoingToLiveVeryLong";
//		String childDirId = UUID.randomUUID().toString();
//		
//		//Create some documents to populate the directories
//		String docId1 = UUID.randomUUID().toString();
//		String docId2 = UUID.randomUUID().toString();
//		String docId3 = UUID.randomUUID().toString();
//		String docId4 = UUID.randomUUID().toString();
//		String docId5 = UUID.randomUUID().toString();
//		
//		String docName1 = "document1.txt";
//		String docName2 = "document2.txt";
//		String docName3 = "document3.txt";
//		String docName4 = "document4.txt";
//		String docName5 = "document5.txt";
//		
//		//create the new Directory
//		mockIDE.sendCreateNewDirectoryEventToServer(testDirId, testDirName, rootDirId);
//		
//		//create the subDirectory
//		mockIDE.sendCreateNewDirectoryEventToServer(childDirId, childDirName, testDirId);
//		
//		//put 3 documents in the delete directory and 2 in the child directory
//		mockIDE.sendCreateNewDocumentEventToServer(docId1, docName1, testDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(docId2, docName2, testDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(docId3, docName3, testDirId);
//		
//		mockIDE.sendCreateNewDocumentEventToServer(docId4, docName4, childDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(docId5, docName5, childDirId);
//		
//		//Run playback up til now
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		
//		//check that things are as we expected
//		assertEquals(true, playbackViewer.directoryExists(testDirId));
//		assertEquals(true, playbackViewer.directoryExists(childDirId));
//		assertEquals(true, playbackViewer.documentExists(docId1));
//		assertEquals(true, playbackViewer.documentExists(docId2));
//		assertEquals(true, playbackViewer.documentExists(docId3));
//		assertEquals(true, playbackViewer.documentExists(docId4));
//		assertEquals(true, playbackViewer.documentExists(docId5));
//		assertEquals(true, playbackViewer.getAllSubdirectoryNames(rootDirId).contains(testDirName));
//		assertEquals(false, playbackViewer.getAllSubdirectoryNames(rootDirId).contains(childDirName));
//		
//		//delete things now
//		mockIDE.sendDeleteDirectoryEventToServer(testDirId, testDirName, rootDirId);
//		
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		assertEquals(false, playbackViewer.directoryExists(testDirId));
//		assertEquals(false, playbackViewer.directoryExists(childDirId));
//		assertEquals(false, playbackViewer.documentExists(docId1));
//		assertEquals(false, playbackViewer.documentExists(docId2));
//		assertEquals(false, playbackViewer.documentExists(docId3));
//		assertEquals(false, playbackViewer.documentExists(docId4));
//		assertEquals(false, playbackViewer.documentExists(docId5));
//		assertEquals(false, playbackViewer.getAllSubdirectoryNames(rootDirId).contains(testDirName));
//		assertEquals(false, playbackViewer.getAllSubdirectoryNames(rootDirId).contains(childDirName));
//		
//		//TODO try to work on old documents/directories and get errors
//		
//	}
//	
//}
