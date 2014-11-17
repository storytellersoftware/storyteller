//package unitTests;
//
//import static Playback.MockMergeHandler.COMBINED_STATE;
//import static Playback.MockMergeHandler.FIRST_DEVELOPER;
//import static Playback.MockMergeHandler.SECOND_DEVELOPER;
//import static org.junit.Assert.*;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//
//import javax.swing.tree.DefaultTreeModel;
//import javax.swing.tree.MutableTreeNode;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.json.JSONException;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import Playback.MockMergeHandler;
//import Playback.MockPlaybackViewer;
//import Playback.PlaybackEventRenderer;
//import StorytellerServer.DBFactory;
//import StorytellerServer.SQLiteDBFactory;
//import StorytellerServer.SQLiteDatabase;
//import StorytellerServer.StorytellerServer;
//import StorytellerServer.ide.IDEProxy;
//import StorytellerServer.playback.PlaybackProxy;
//
///**
// * Part 3 tests all possible ways in which DirectoryEvents can conflict with each other.
// * 
// * 8/30/12 for now, two developers deleting the same directory will not be tested because that should be handled 
// * as an automatic conflict, but for now is a manual conflict.  See issue #28 in bitbucket.
// */
//public class TestMergingPartThree
//{
//
//	private static String pathToServer="127.0.0.1";
//
//	//IDE generated id of the root directory
//
//	private static MockIDE mockIDE; 
//
//	private static DBFactory sqliteDbFactory;
//
//	//default test server
//	private static StorytellerServer testServer;
//
//	//test database file name
//	private static String testDbFileName = "unitTestDBForTestMergePartThreeTest";
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
//	private static Logger logger = Logger.getLogger(TestMergingPartThree.class.getName());
//	@SuppressWarnings("unused")
//	private static Logger timer = Logger.getLogger("timing."+TestMergingPartThree.class.getName());
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
//			logger.fatal("",e);
//		}
//		mockIDE = new MockIDE(pathToServer);
//
//		MockPlaybackViewer.setDevFirstName("Mark");
//		MockPlaybackViewer.setDevLastName("Mahoney");
//		MockPlaybackViewer.setDevEmailAddress("mmahoney@carthage.edu");
//		MockMergeHandler.setDevFirstName("Mark");
//		MockMergeHandler.setDevLastName("Mahoney");
//		MockMergeHandler.setDevEmailAddress("mmahoney@carthage.edu");
//	}
//
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception 
//	{
//		logger.debug("Closing down playback and serverproxy");
//	
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
//			logger.fatal("",e);
//		}
//
//	}
//	@After
//	public void closeProject() throws Exception
//	{
//		mockIDE.closeProject();
//		Thread.sleep(500);
//	}
//	
//	@Test
//	public void testCreateDirectoryAgainstRandM() throws Exception		//Tests conflicts between create/create, create/move, and create/rename
//	{
//		final String thisFilePath = testDbFileName+1+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+1+"Project";
//		
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath,MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String conflictingNameCreate = "create";			//will be the name used in the create vs create
//		final String conflictingNameRename = "rename";
//		final String conflictingNameMove = "move";
//		
//		final String originalFirstDirName = "a";				//will be the point of conflict against a renameDirectory
//		final String originalSecondDirName = conflictingNameMove;				//will be the point of conflict against a moveDirectory
//		final String firstDirId = UUID.randomUUID().toString();
//		final String secondDirId = UUID.randomUUID().toString();
//
//		final String parentDirName = "dir";				//this will hold the  originalSecondDir, which will be moved out of here to conflict with MoveDirectory
//		final String parentDirId = UUID.randomUUID().toString();
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(parentDirId, parentDirName, rootDirId);
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(firstDirId, originalFirstDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(secondDirId, originalSecondDirName, parentDirId);
//		
//		//Shared state is set with
//		//		/a			(directory)
//		//		/dir		(directory)
//		//		/dir/move	(directory)
//		
//		mockIDE.commit("Test One", "All systems set");
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch one more time so that we can have two nodes to merge
//
//		assertEquals(0, viewer.getAllDocumentNames(rootDirId).size());
//		assertEquals(2, viewer.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(1, viewer.getAllSubdirectoryNames(parentDirId).size());
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		//Here's the branch
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//		//Go find the two nodes
//		tree = viewer.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//
//		String firstNodeId = root.getChildAt(0).toString();
//		String secondNodeId = root.getChildAt(1).toString();
//		
//		mockIDE.closeProject();
//		Thread.sleep(500);
//		
//		//Dev 1 creates 3 directories
//		final String devOneFirstNewDirId = UUID.randomUUID().toString();
//		final String devOneSecondNewDirId = UUID.randomUUID().toString();
//		final String devOneThirdNewDirId = UUID.randomUUID().toString();
//		
//		mockIDE.openProject(thisFilePath, thisProjectName,firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(devOneFirstNewDirId, conflictingNameCreate, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(devOneSecondNewDirId, conflictingNameRename, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(devOneThirdNewDirId, conflictingNameMove, rootDirId);
//		
//		mockIDE.closeProject();
//		Thread.sleep(500);
//		
//		final String devTwoFirstNewDirId = UUID.randomUUID().toString();
//		
//		final String devTwoFirstNewDocId = UUID.randomUUID().toString();		//some new docs, just to make sure this clumping
//		final String devTwoSecondNewDocId = UUID.randomUUID().toString();		
//		final String devTwoThirdNewDocId = UUID.randomUUID().toString();
//		final String devTwoFirstNewDocName = "2First.java";		
//		final String devTwoSecondNewDocName = "2Second.java";		
//		final String devTwoThirdNewDocName = "2Third.java";
//		final String devTwoThirdNewDocText = "BlahBLAHblah";
//		
//		mockIDE.openProject(thisFilePath, thisProjectName,secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(devTwoFirstNewDirId, conflictingNameCreate, rootDirId);		//conflicting createDirectory
//		mockIDE.sendCreateNewDocumentEventToServer(devTwoFirstNewDocId, devTwoFirstNewDocName, devTwoFirstNewDirId);
//		
//		mockIDE.sendRenameDirectoryEventToServer(firstDirId, originalFirstDirName, conflictingNameRename, rootDirId);		//conflicting renameDirectory
//		mockIDE.sendCreateNewDocumentEventToServer(devTwoSecondNewDocId, devTwoSecondNewDocName, firstDirId);				//create a new doc in a dir that's been renamed
//		
//		mockIDE.sendCreateNewDocumentEventToServer(devTwoThirdNewDocId, devTwoThirdNewDocName, secondDirId);	//create a new document in a dir that will be moved and add some text to it
//		mockIDE.setCurrentDocumentId(devTwoThirdNewDocId);
//		mockIDE.sendStringToServer(devTwoThirdNewDocText, 0);
//		mockIDE.sendMoveDirectoryEventToServer(secondDirId, parentDirId, rootDirId);		//conflicting moveDirectory
//		
//		
//		//Now we merge
//		Set<String> previousMergeIds = new HashSet<String>();
//		
////		int firstConflictResolution = 0;
////		int secondConflictResolution = 1;
////		int thirdConflictResolution = 2;
//		
//		//Now we can merge
//		//Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//					FirstNode:													SecondNode:
//		//				(umc) Create dir "create"								(umc) Create dir "create" and new doc "2First.java" in it
//		//				(umc) Create dir "rename"								(umc) rename firstDir to "rename"
//		//				(umc) Create dir "move"									(uac) create "2Second.java" in firstDir
//		//																		(uac) create "2Third.java" in 2nd dir and type "BlahBLAHblah" in it
//		//																		(umc) move "move" to rootDir
//		
//		for(int firstConflictResolution = 0;firstConflictResolution<3;firstConflictResolution++)
//		{
//			for(int secondConflictResolution = 0;secondConflictResolution<3;secondConflictResolution++)
//			{
//				for (int thirdConflictResolution = 0;thirdConflictResolution<3;thirdConflictResolution++)
//				{
//					testCreateDirectoryAgainstRandMHelper(firstNodeId, secondNodeId, firstConflictResolution, secondConflictResolution, thirdConflictResolution, rootDirId, parentDirId, firstDirId, conflictingNameRename, originalFirstDirName, devOneFirstNewDirId, devOneSecondNewDirId, devOneThirdNewDirId, devTwoFirstNewDirId, devTwoFirstNewDocId, devTwoSecondNewDocId, devTwoThirdNewDocId, devTwoThirdNewDocText, previousMergeIds);
//				}
//			}
//		}
//	}
//	
//	private void testCreateDirectoryAgainstRandMHelper(String firstNodeId, String secondNodeId, Integer firstConflictResolution, int secondConflictResolution,
//			int thirdConflictResolution, String rootDirId, String parentDirId, String firstDirId, String conflictingNameRename,
//			String originalFirstDirName, String devOneFirstNewDirId, String devOneSecondNewDirId, String devOneThirdNewDirId, String devTwoFirstNewDirId,
//			String devTwoFirstNewDocId, String devTwoSecondNewDocId, String devTwoThirdNewDocId, String devTwoThirdNewDocText, Set<String> previousMergeIds) throws IOException, JSONException
//	{
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//		assertEquals(0, handler.getAllDocumentNames(rootDirId).size());
//		assertEquals(2, handler.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(1, handler.getAllSubdirectoryNames(parentDirId).size());
//		
//		handler.handleManualConflictCompletely(firstConflictResolution);			//This should resolve the create vs create
//		assertEquals(true, handler.isMerging());
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(firstConflictResolution==1, handler.directoryExists(devOneFirstNewDirId));	
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(firstConflictResolution==2, handler.directoryExists(devTwoFirstNewDirId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if(firstConflictResolution==0)
//		{
//			assertEquals(false, handler.directoryExists(devOneFirstNewDirId));
//			assertEquals(false, handler.directoryExists(devTwoFirstNewDirId));
//			assertEquals(false, handler.hasDocument(devTwoFirstNewDocId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(true, handler.directoryExists(devOneFirstNewDirId));
//			assertEquals(false, handler.directoryExists(devTwoFirstNewDirId));
//			assertEquals(false, handler.hasDocument(devTwoFirstNewDocId));
//		}
//		else 
//		{
//			assertEquals(false, handler.directoryExists(devOneFirstNewDirId));
//			assertEquals(true, handler.directoryExists(devTwoFirstNewDirId));
//			assertEquals(true, handler.hasDocument(devTwoFirstNewDocId));
//		}
//		
//		
//		handler.handleManualConflictCompletely(secondConflictResolution);			//This should resolve the create vs rename
//		assertEquals(true, handler.isMerging());
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(secondConflictResolution==1, handler.directoryExists(devOneSecondNewDirId));	
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(secondConflictResolution==2?conflictingNameRename:originalFirstDirName, handler.getDirectoryName(firstDirId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if(firstConflictResolution==0)
//		{
//			assertEquals(false, handler.directoryExists(devOneFirstNewDirId));
//			assertEquals(false, handler.directoryExists(devTwoFirstNewDirId));
//			assertEquals(false, handler.hasDocument(devTwoFirstNewDocId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(true, handler.directoryExists(devOneFirstNewDirId));
//			assertEquals(false, handler.directoryExists(devTwoFirstNewDirId));
//			assertEquals(false, handler.hasDocument(devTwoFirstNewDocId));
//		}
//		else 
//		{
//			assertEquals(false, handler.directoryExists(devOneFirstNewDirId));
//			assertEquals(true, handler.directoryExists(devTwoFirstNewDirId));
//			assertEquals(true, handler.hasDocument(devTwoFirstNewDocId));
//		}
//		if(secondConflictResolution==0)
//		{
//			assertEquals(false, handler.directoryExists(devOneSecondNewDirId));
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		else if (secondConflictResolution==1)
//		{
//			assertEquals(true, handler.directoryExists(devOneSecondNewDirId));
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		else 
//		{
//			assertEquals(false, handler.directoryExists(devOneSecondNewDirId));
//			assertEquals(conflictingNameRename, handler.getDirectoryName(firstDirId));
//		}
//		
//		
//		//Now we have the two automatic conflicts
//		handler.handleAutomaticConflict();		//creating new document in firstDirId
//		assertEquals(true, handler.isMerging());
//		
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(false, handler.hasDocument(devTwoSecondNewDocId));
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(true, handler.hasDocument(devTwoSecondNewDocId));
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(true, handler.hasDocument(devTwoSecondNewDocId));
//		
//		handler.handleAutomaticConflict();		//creating new document in secondDirId and typing in it
//		assertEquals(true, handler.isMerging());
//		
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(false, handler.hasDocument(devTwoThirdNewDocId));
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(true, handler.hasDocument(devTwoThirdNewDocId));
//		assertEquals(devTwoThirdNewDocText, handler.getDocumentText(devTwoThirdNewDocId));
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(true, handler.hasDocument(devTwoThirdNewDocId));
//		assertEquals(devTwoThirdNewDocText, handler.getDocumentText(devTwoThirdNewDocId));
//		
//		
//		handler.handleManualConflictCompletely(thirdConflictResolution);			//This should resolve the create vs move
//		assertEquals(false, handler.isMerging());
//		
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(thirdConflictResolution==1, handler.directoryExists(devOneThirdNewDirId));
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(thirdConflictResolution==2?0:1, handler.getAllSubdirectoryNames(parentDirId).size());		//the move happened or it didn't
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if(firstConflictResolution==0)
//		{
//			assertEquals(false, handler.directoryExists(devOneFirstNewDirId));
//			assertEquals(false, handler.directoryExists(devTwoFirstNewDirId));
//			assertEquals(false, handler.hasDocument(devTwoFirstNewDocId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(true, handler.directoryExists(devOneFirstNewDirId));
//			assertEquals(false, handler.directoryExists(devTwoFirstNewDirId));
//			assertEquals(false, handler.hasDocument(devTwoFirstNewDocId));
//		}
//		else 
//		{
//			assertEquals(false, handler.directoryExists(devOneFirstNewDirId));
//			assertEquals(true, handler.directoryExists(devTwoFirstNewDirId));
//			assertEquals(true, handler.hasDocument(devTwoFirstNewDocId));
//		}
//		if(secondConflictResolution==0)
//		{
//			assertEquals(false, handler.directoryExists(devOneSecondNewDirId));
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		else if (secondConflictResolution==1)
//		{
//			assertEquals(true, handler.directoryExists(devOneSecondNewDirId));
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		else 
//		{
//			assertEquals(false, handler.directoryExists(devOneSecondNewDirId));
//			assertEquals(conflictingNameRename, handler.getDirectoryName(firstDirId));
//		}
//		assertEquals(true, handler.hasDocument(devTwoSecondNewDocId));		//bring those automatic conflicts back in
//		assertEquals(true, handler.hasDocument(devTwoThirdNewDocId));		//bring those automatic conflicts back in
//		assertEquals(devTwoThirdNewDocText, handler.getDocumentText(devTwoThirdNewDocId));	//bring those automatic conflicts back in
//		if (thirdConflictResolution==0)
//		{
//			assertEquals(false, handler.directoryExists(devOneThirdNewDirId));
//			assertEquals(1, handler.getAllSubdirectoryNames(parentDirId).size());
//		}
//		else if (thirdConflictResolution==1)
//		{
//			assertEquals(true, handler.directoryExists(devOneThirdNewDirId));
//			assertEquals(1, handler.getAllSubdirectoryNames(parentDirId).size());
//		}
//		else 
//		{
//			assertEquals(false, handler.directoryExists(devOneThirdNewDirId));
//			assertEquals(0, handler.getAllSubdirectoryNames(parentDirId).size());
//		}
//		
//		//And now we play it back and make sure everything is good
//		String mergeId = null;
//		DefaultTreeModel tree = handler.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			String childId = root.getChildAt(i).toString();
//			if (!childId.equals(firstNodeId)&&!childId.equals(secondNodeId)&&!previousMergeIds.contains(childId))
//			{
//				mergeId = childId;
//				break;
//			}
//		}
//		if (mergeId == null)
//		{
//			fail("Merge node could not be found");
//		}
//		previousMergeIds.add(mergeId);
//
//		PlaybackEventRenderer viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeId);
//		if(firstConflictResolution==0)
//		{
//			assertEquals(false, viewer.directoryExists(devOneFirstNewDirId));
//			assertEquals(false, viewer.directoryExists(devTwoFirstNewDirId));
//			assertEquals(false, viewer.hasDocument(devTwoFirstNewDocId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(true, viewer.directoryExists(devOneFirstNewDirId));
//			assertEquals(false, viewer.directoryExists(devTwoFirstNewDirId));
//			assertEquals(false, viewer.hasDocument(devTwoFirstNewDocId));
//		}
//		else 
//		{
//			assertEquals(false, viewer.directoryExists(devOneFirstNewDirId));
//			assertEquals(true, viewer.directoryExists(devTwoFirstNewDirId));
//			assertEquals(true, viewer.hasDocument(devTwoFirstNewDocId));
//		}
//		if(secondConflictResolution==0)
//		{
//			assertEquals(false, viewer.directoryExists(devOneSecondNewDirId));
//			assertEquals(originalFirstDirName, viewer.getDirectoryName(firstDirId));
//		}
//		else if (secondConflictResolution==1)
//		{
//			assertEquals(true, viewer.directoryExists(devOneSecondNewDirId));
//			assertEquals(originalFirstDirName, viewer.getDirectoryName(firstDirId));
//		}
//		else 
//		{
//			assertEquals(false, viewer.directoryExists(devOneSecondNewDirId));
//			assertEquals(conflictingNameRename, viewer.getDirectoryName(firstDirId));
//		}
//		assertEquals(true, viewer.hasDocument(devTwoSecondNewDocId));		//bring those automatic conflicts back in
//		assertEquals(true, viewer.hasDocument(devTwoThirdNewDocId));		//bring those automatic conflicts back in
//		assertEquals(devTwoThirdNewDocText, viewer.getDocumentText(devTwoThirdNewDocId));	//bring those automatic conflicts back in
//		if (thirdConflictResolution==0)
//		{
//			assertEquals(false, viewer.directoryExists(devOneThirdNewDirId));
//			assertEquals(1, viewer.getAllSubdirectoryNames(parentDirId).size());
//		}
//		else if (thirdConflictResolution==1)
//		{
//			assertEquals(true, viewer.directoryExists(devOneThirdNewDirId));
//			assertEquals(1, viewer.getAllSubdirectoryNames(parentDirId).size());
//		}
//		else 
//		{
//			assertEquals(false, viewer.directoryExists(devOneThirdNewDirId));
//			assertEquals(0, viewer.getAllSubdirectoryNames(parentDirId).size());
//		}
//	}
//	
//	@Test
//	public void testRenameDirectoryAgainstRMandD() throws Exception			//tests conflicts between rename/rename (by id) rename/move (by id) and rename/delete
//	{
//		final String thisFilePath = testDbFileName+2+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+2+"Project";
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//		
//		final String originalFirstDirName = "a";				//will be the point of conflict against a renameDirectory
//		final String originalSecondDirName = "b";				//will be the point of conflict against a moveDirectory
//		final String originalThirdDirName = "c";				//will be the point of conflict against a deleteDirectory
//		final String firstDirId = UUID.randomUUID().toString();
//		final String secondDirId = UUID.randomUUID().toString();
//		final String thirdDirId = UUID.randomUUID().toString();
//		
//		final String parentDirName = "dir";				//this will hold the  originalSecondDir, which will be moved out of here to conflict with MoveDirectory
//		final String parentDirId = UUID.randomUUID().toString();
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(parentDirId, parentDirName, rootDirId);
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(firstDirId, originalFirstDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(secondDirId, originalSecondDirName, parentDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(thirdDirId, originalThirdDirName, rootDirId);
//		
//		//Shared state is set with
//		//		/a			(directory)
//		//		/dir		(directory)
//		//		/dir/b		(directory)
//		//		/c			(directory)
//		
//		mockIDE.commit("Test Two", "All systems set");
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch one more time so that we can have two nodes to merge
//
//		assertEquals(0, viewer.getAllDocumentNames(rootDirId).size());
//		assertEquals(3, viewer.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(1, viewer.getAllSubdirectoryNames(parentDirId).size());
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		//Here's the branch
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//		//Go find the two nodes
//		tree = viewer.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//
//		String firstNodeId = root.getChildAt(0).toString();
//		String secondNodeId = root.getChildAt(1).toString();
//		
//		mockIDE.closeProject();
//		Thread.sleep(500);
//		
//		//First developer is going to rename all the files to greek names
//		final String devOneFirstDirName = "alpha";
//		final String devOneSecondDirName = "beta";
//		final String devOneThirdDirName = "gamma";
//		mockIDE.openProject(thisFilePath, thisProjectName,firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		mockIDE.sendRenameDirectoryEventToServer(firstDirId, originalFirstDirName, devOneFirstDirName, rootDirId);
//		mockIDE.sendRenameDirectoryEventToServer(secondDirId, originalSecondDirName, devOneSecondDirName, rootDirId);
//		mockIDE.sendRenameDirectoryEventToServer(thirdDirId, originalThirdDirName, devOneThirdDirName, rootDirId);
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//		
//		final String devTwoFirstDirName = "A";
//
//		mockIDE.openProject(thisFilePath, thisProjectName,secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		//We'll things to from third document to first document.  Just because.  
//		mockIDE.sendDeleteDirectoryEventToServer(thirdDirId, originalThirdDirName, rootDirId);
//		mockIDE.sendMoveDirectoryEventToServer(secondDirId, parentDirId, rootDirId);
//		mockIDE.sendRenameDirectoryEventToServer(firstDirId, originalFirstDirName, devTwoFirstDirName, rootDirId);
//		
//		//Now we should merge
//		//Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//					FirstNode:													SecondNode:
//		//				(umc) Rename dir "a" to "alpha"								(umc) Delete dir "c"
//		//				(umc) Rename dir "b" to "beta"								(umc) Move dir "b" from parentDir to rootDir 
//		//				(umc) Rename dir "c" to "gamma"								(umc) Rename dir "a" to "A"
//		
//		Set<String> previousMergeIds = new HashSet<String>();
//		
//		for(int firstConflictResolution = 0;firstConflictResolution<3;firstConflictResolution++)
//		{
//			for(int secondConflictResolution = 0;secondConflictResolution<3;secondConflictResolution++)
//			{
//				for (int thirdConflictResolution = 0;thirdConflictResolution<3;thirdConflictResolution++)
//				{
//					testRenameDirectoryAgainstRMandDHelper(firstNodeId, secondNodeId, firstConflictResolution, secondConflictResolution, thirdConflictResolution, rootDirId, parentDirId, firstDirId, secondDirId, thirdDirId, originalFirstDirName, originalSecondDirName, originalThirdDirName, devOneFirstDirName, devOneSecondDirName, devOneThirdDirName, devTwoFirstDirName, previousMergeIds);
//				}
//			}
//		}
//	}
//	
//	private void testRenameDirectoryAgainstRMandDHelper(String firstNodeId, String secondNodeId, int firstConflictResolution, int secondConflictResolution,
//			int thirdConflictResolution, String rootDirId, String parentDirId, String firstDirId, String secondDirId, String thirdDirId, 
//			String originalFirstDirName, String originalSecondDirName, String originalThirdDirName, String devOneFirstDirName, String devOneSecondDirName,
//			String devOneThirdDirName, String devTwoFirstDirName, Set<String> previousMergeIds) throws IOException, JSONException
//	{
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//		
//		assertEquals(0, handler.getAllDocumentNames(rootDirId).size());
//		assertEquals(3, handler.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(1, handler.getAllSubdirectoryNames(parentDirId).size());
//		assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//		assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//		assertEquals(originalThirdDirName, handler.getDirectoryName(thirdDirId));		//just check that everything is at the ancestor state
//			
//		//=======================================================================================================
//		handler.handleManualConflictCompletely(firstConflictResolution);		//this will be the rename vs rename
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(firstConflictResolution==1?devOneFirstDirName:originalFirstDirName, handler.getDirectoryName(firstDirId));
//		
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(firstConflictResolution==2?devTwoFirstDirName:originalFirstDirName, handler.getDirectoryName(firstDirId));
//		
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(devOneFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		else
//		{
//			assertEquals(devTwoFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		
//		//=======================================================================================================
//		handler.handleManualConflictCompletely(secondConflictResolution);		//this will be the rename vs move
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		if (secondConflictResolution == 1)
//		{
//			assertEquals(devOneSecondDirName, handler.getDirectoryName(secondDirId));
//			assertEquals(3, handler.getAllSubdirectoryNames(rootDirId).size());
//		}
//		else 
//		{
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//			assertEquals(3, handler.getAllSubdirectoryNames(rootDirId).size());
//		}
//		
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		if (secondConflictResolution == 2)
//		{
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalSecondDirName));
//		}
//		else 
//		{
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//			assertEquals(3, handler.getAllSubdirectoryNames(rootDirId).size());
//		}
//		
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(devOneFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		else
//		{
//			assertEquals(devTwoFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		if (secondConflictResolution == 0)
//		{
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//			assertEquals(3, handler.getAllSubdirectoryNames(rootDirId).size());
//		}
//		else if (secondConflictResolution == 1)
//		{
//			assertEquals(devOneSecondDirName, handler.getDirectoryName(secondDirId));
//			assertEquals(3, handler.getAllSubdirectoryNames(rootDirId).size());
//		}
//		else  
//		{
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalSecondDirName));
//		}
//		
//		
//		//=======================================================================================================
//		handler.handleManualConflictCompletely(thirdConflictResolution);		//this will be the rename vs delete
//		assertEquals(false, handler.isMerging());
//		
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(thirdConflictResolution == 1?devOneThirdDirName:originalThirdDirName, handler.getDirectoryName(thirdDirId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		if (thirdConflictResolution == 2)
//		{
//			assertEquals(false, handler.directoryExists(thirdDirId));
//		}
//		else 
//		{
//			assertEquals(originalThirdDirName, handler.getDirectoryName(thirdDirId));
//		}
//		
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(devOneFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		else
//		{
//			assertEquals(devTwoFirstDirName, handler.getDirectoryName(firstDirId));
//		}
//		if (secondConflictResolution == 0)
//		{
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//		}
//		else if (secondConflictResolution == 1)
//		{
//			assertEquals(devOneSecondDirName, handler.getDirectoryName(secondDirId));
//		}
//		else  
//		{
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalSecondDirName));
//		}
//		if (thirdConflictResolution == 0)
//		{
//			assertEquals(originalThirdDirName, handler.getDirectoryName(thirdDirId));
//		}
//		else if (thirdConflictResolution == 1)
//		{
//			assertEquals(devOneThirdDirName, handler.getDirectoryName(thirdDirId));
//		}
//		else
//		{
//			assertEquals(false, handler.directoryExists(thirdDirId));
//		}
//		
//		//======================================================================================================
//		//All done merging
//		String mergeId = null;
//		DefaultTreeModel tree = handler.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			String childId = root.getChildAt(i).toString();
//			if (!childId.equals(firstNodeId)&&!childId.equals(secondNodeId)&&!previousMergeIds.contains(childId))
//			{
//				mergeId = childId;
//				break;
//			}
//		}
//		if (mergeId == null)
//		{
//			fail("Merge node could not be found");
//		}
//		previousMergeIds.add(mergeId);
//
//		PlaybackEventRenderer viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeId);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDirName, viewer.getDirectoryName(firstDirId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(devOneFirstDirName, viewer.getDirectoryName(firstDirId));
//		}
//		else
//		{
//			assertEquals(devTwoFirstDirName, viewer.getDirectoryName(firstDirId));
//		}
//		if (secondConflictResolution == 0)
//		{
//			assertEquals(originalSecondDirName, viewer.getDirectoryName(secondDirId));
//		}
//		else if (secondConflictResolution == 1)
//		{
//			assertEquals(devOneSecondDirName, viewer.getDirectoryName(secondDirId));
//		}
//		else  
//		{
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalSecondDirName));
//		}
//		if (thirdConflictResolution == 0)
//		{
//			assertEquals(originalThirdDirName, viewer.getDirectoryName(thirdDirId));
//		}
//		else if (thirdConflictResolution == 1)
//		{
//			assertEquals(devOneThirdDirName, viewer.getDirectoryName(thirdDirId));
//		}
//		else
//		{
//			assertEquals(false, viewer.directoryExists(thirdDirId));
//		}
//	}
//	
//	
//	@Test
//	public void testRenameDirectoryAndMoveDirectoryAgainstRandM() throws Exception		//Tests Rename/Rename (by name), Rename/Move (by name) and Move/Move (by name)
//	{
//		final String thisFilePath = testDbFileName+3+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+3+"Project";
//				
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath,MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//		
//		final String conflictingNameRenameRename = "renameRename";			//will be the name used in the rename vs rename (by name)
//		final String conflictingNameRenameMove = "renameMove";				//will be the name used in the rename vs move (by name)
//		final String conflictingNameMoveMove = "moveMove";					//will be the name used in the move vs move (by name)
//		
//		final String originalFirstDirName = "a";				//will be the point of conflict against a rename vs rename (first dir)
//		final String originalSecondDirName = "b";				//will be the point of conflict against a rename vs rename (second dir)
//		final String originalThirdDirName = "c";				//will be the point of conflict against a rename vs move (renaming dir)
//		final String originalFourthDirName = conflictingNameRenameMove;				//will be the point of conflict against a rename vs move (moving dir)
//		final String originalFifthDirName = conflictingNameMoveMove;				//will be the point of conflict against a move vs move (second dir)
//		final String originalSixthDirName = conflictingNameMoveMove;				//will be the point of conflict against a move vs move (first dir)
//		
//		final String firstDirId = UUID.randomUUID().toString();
//		final String secondDirId = UUID.randomUUID().toString();
//		final String thirdDirId = UUID.randomUUID().toString();
//		final String fourthDirId = UUID.randomUUID().toString();
//		final String fifthDirId = UUID.randomUUID().toString();
//		final String sixthDirId = UUID.randomUUID().toString();
//		
//		final String firstChildDirId = UUID.randomUUID().toString();
//		final String firstChildDirName = "child";
//		final String secondChildDirId = UUID.randomUUID().toString();
//		final String secondChildDirName = "otherChild";
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(firstChildDirId, firstChildDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(secondChildDirId, secondChildDirName, rootDirId);
//		
//		mockIDE.sendCreateNewDirectoryEventToServer(firstDirId, originalFirstDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(secondDirId, originalSecondDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(thirdDirId, originalThirdDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(fourthDirId, originalFourthDirName, firstChildDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(fifthDirId, originalFifthDirName, firstChildDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(sixthDirId, originalSixthDirName, secondChildDirId);
//		
//		//Shared state is set with
//		//		/a						(directory)
//		//		/b						(directory)
//		//		/c						(directory)
//		//		/child					(directory)
//		//		/child/renameMove		(directory)
//		//		/child/moveMove			(directory)
//		//		/otherChild				(directory)
//		//		/otherChild/moveMove	(directory)
//		
//		mockIDE.commit("Test Two", "All systems set");
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch one more time so that we can have two nodes to merge
//
//		assertEquals(0, viewer.getAllDocumentNames(rootDirId).size());
//		assertEquals(5, viewer.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(2, viewer.getAllSubdirectoryNames(firstChildDirId).size());
//		assertEquals(1, viewer.getAllSubdirectoryNames(secondChildDirId).size());
//		
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		//Here's the branch
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//		//Go find the two nodes
//		tree = viewer.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//
//		String firstNodeId = root.getChildAt(0).toString();
//		String secondNodeId = root.getChildAt(1).toString();
//		
//		mockIDE.closeProject();
//		Thread.sleep(500);
//		
//		//First dev will rename the first and third dir to the names that will be conflicts and
//		//move the fifth dir into root
//		mockIDE.openProject(thisFilePath, thisProjectName,firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		
//		mockIDE.sendRenameDirectoryEventToServer(firstDirId, originalFirstDirName, conflictingNameRenameRename, rootDirId);
//		mockIDE.sendRenameDirectoryEventToServer(thirdDirId, originalThirdDirName, conflictingNameRenameMove, rootDirId);
//		mockIDE.sendMoveDirectoryEventToServer(fifthDirId, firstChildDirId, rootDirId);
//		
//		mockIDE.closeProject();
//		Thread.sleep(500);
//		
//		
//		mockIDE.openProject(thisFilePath, thisProjectName,secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		
//		mockIDE.sendMoveDirectoryEventToServer(sixthDirId, secondChildDirId, rootDirId);
//		mockIDE.sendRenameDirectoryEventToServer(secondDirId, originalSecondDirName, conflictingNameRenameRename, rootDirId);
//		mockIDE.sendMoveDirectoryEventToServer(fourthDirId, firstChildDirId, rootDirId);
//		
//		//Now we merge
//		//Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//					FirstNode:															SecondNode:
//		//				(umc) Rename dir "a" to "renameRename"								(umc) Move /otherChild/moveMove to rootDir
//		//				(umc) Rename dir "c" to "renameMove"								(umc) Rename dir "b" to "renameRename" 
//		//				(umc) Move dir /child/moveMove to rootdir							(umc) Move /child/renameMove to rootDir
//		Set<String> previousMergeIds = new HashSet<String>();
//		
//		for(int firstConflictResolution = 0;firstConflictResolution<3;firstConflictResolution++)
//		{
//			for(int secondConflictResolution = 0;secondConflictResolution<3;secondConflictResolution++)
//			{
//				for (int thirdConflictResolution = 0;thirdConflictResolution<3;thirdConflictResolution++)
//				{
//					testRenameDirectoryAndMoveDirectoryAgainstRandMHelper(firstNodeId, secondNodeId, firstConflictResolution, secondConflictResolution, 
//							thirdConflictResolution, rootDirId, firstChildDirId, secondChildDirId, firstDirId, secondDirId, thirdDirId, originalFirstDirName, 
//							originalSecondDirName, originalThirdDirName, originalFourthDirName, originalFifthDirName, originalSixthDirName, conflictingNameRenameMove,
//							conflictingNameRenameRename, previousMergeIds);
//				}
//			}
//		}
//	}
//	
//	private void testRenameDirectoryAndMoveDirectoryAgainstRandMHelper(String firstNodeId, String secondNodeId, int firstConflictResolution, int secondConflictResolution,
//			int thirdConflictResolution, String rootDirId, String firstChildDirId, String secondChildDirId, String firstDirId, String secondDirId, String thirdDirId,
//			String originalFirstDirName, String originalSecondDirName, String originalThirdDirName, String originalFourthDirName, String originalFifthDirName,
//			 String originalSixthDirName, String conflictingNameRenameMove, String conflictingNameRenameRename, Set<String> previousMergeIds) throws IOException, JSONException
//	{
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//
//		assertEquals(0, handler.getAllDocumentNames(rootDirId).size());
//		assertEquals(5, handler.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(2, handler.getAllSubdirectoryNames(firstChildDirId).size());
//		assertEquals(1, handler.getAllSubdirectoryNames(secondChildDirId).size());		//just check that everything is at the ancestor state
//
//		handler.handleManualConflictCompletely(firstConflictResolution);		//this will be the rename vs rename
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(firstConflictResolution==1?conflictingNameRenameRename:originalFirstDirName, handler.getDirectoryName(firstDirId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(firstConflictResolution==2?conflictingNameRenameRename:originalSecondDirName, handler.getDirectoryName(secondDirId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(conflictingNameRenameRename, handler.getDirectoryName(firstDirId));
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//		}
//		else
//		{
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//			assertEquals(conflictingNameRenameRename, handler.getDirectoryName(secondDirId));
//		}
//
//
//		handler.handleManualConflictCompletely(secondConflictResolution);		//this will be the rename vs move
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(secondConflictResolution==1?conflictingNameRenameMove:originalThirdDirName, handler.getDirectoryName(thirdDirId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		if (secondConflictResolution == 2)
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalFourthDirName));
//		}
//		else 
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(firstChildDirId).contains(originalFourthDirName));
//		}
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(conflictingNameRenameRename, handler.getDirectoryName(firstDirId));
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//		}
//		else
//		{
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//			assertEquals(conflictingNameRenameRename, handler.getDirectoryName(secondDirId));
//		}
//		if (secondConflictResolution == 0)
//		{
//			assertEquals(originalThirdDirName, handler.getDirectoryName(thirdDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(firstChildDirId).contains(originalFourthDirName));
//		}
//		else if (secondConflictResolution == 1)
//		{
//			assertEquals(conflictingNameRenameMove, handler.getDirectoryName(thirdDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(firstChildDirId).contains(originalFourthDirName));
//		}
//		else 
//		{
//			assertEquals(originalThirdDirName, handler.getDirectoryName(thirdDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalFourthDirName));
//		}
//
//
//		handler.handleManualConflictCompletely(thirdConflictResolution);		//this will be the move vs move
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		if (thirdConflictResolution == 1)
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalFifthDirName));
//		}
//		else 
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(firstChildDirId).contains(originalFifthDirName));
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		if (thirdConflictResolution == 2)
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalSixthDirName));
//		}
//		else 
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(secondChildDirId).contains(originalSixthDirName));
//		}
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(conflictingNameRenameRename, handler.getDirectoryName(firstDirId));
//			assertEquals(originalSecondDirName, handler.getDirectoryName(secondDirId));
//		}
//		else
//		{
//			assertEquals(originalFirstDirName, handler.getDirectoryName(firstDirId));
//			assertEquals(conflictingNameRenameRename, handler.getDirectoryName(secondDirId));
//		}
//		if (secondConflictResolution == 0)
//		{
//			assertEquals(originalThirdDirName, handler.getDirectoryName(thirdDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(firstChildDirId).contains(originalFourthDirName));
//		}
//		else if (secondConflictResolution == 1)
//		{
//			assertEquals(conflictingNameRenameMove, handler.getDirectoryName(thirdDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(firstChildDirId).contains(originalFourthDirName));
//		}
//		else 
//		{
//			assertEquals(originalThirdDirName, handler.getDirectoryName(thirdDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalFourthDirName));
//		}
//		if (thirdConflictResolution == 0)
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(firstChildDirId).contains(originalFifthDirName));
//			assertEquals(true, handler.getAllSubdirectoryNames(secondChildDirId).contains(originalSixthDirName));
//		}
//		else if (thirdConflictResolution == 1)
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalFifthDirName));
//			assertEquals(true, handler.getAllSubdirectoryNames(secondChildDirId).contains(originalSixthDirName));
//		}
//		else 
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(firstChildDirId).contains(originalFifthDirName));
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalSixthDirName));
//		}
//		
//		
//		//All done merging
//		String mergeId = null;
//		DefaultTreeModel tree = handler.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			String childId = root.getChildAt(i).toString();
//			if (!childId.equals(firstNodeId)&&!childId.equals(secondNodeId)&&!previousMergeIds.contains(childId))
//			{
//				mergeId = childId;
//				break;
//			}
//		}
//		if (mergeId == null)
//		{
//			fail("Merge node could not be found");
//		}
//		previousMergeIds.add(mergeId);
//
//		PlaybackEventRenderer viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeId);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDirName, viewer.getDirectoryName(firstDirId));
//			assertEquals(originalSecondDirName, viewer.getDirectoryName(secondDirId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(conflictingNameRenameRename, viewer.getDirectoryName(firstDirId));
//			assertEquals(originalSecondDirName, viewer.getDirectoryName(secondDirId));
//		}
//		else
//		{
//			assertEquals(originalFirstDirName, viewer.getDirectoryName(firstDirId));
//			assertEquals(conflictingNameRenameRename, viewer.getDirectoryName(secondDirId));
//		}
//		if (secondConflictResolution == 0)
//		{
//			assertEquals(originalThirdDirName, viewer.getDirectoryName(thirdDirId));
//			assertEquals(true, viewer.getAllSubdirectoryNames(firstChildDirId).contains(originalFourthDirName));
//		}
//		else if (secondConflictResolution == 1)
//		{
//			assertEquals(conflictingNameRenameMove, viewer.getDirectoryName(thirdDirId));
//			assertEquals(true, viewer.getAllSubdirectoryNames(firstChildDirId).contains(originalFourthDirName));
//		}
//		else 
//		{
//			assertEquals(originalThirdDirName, viewer.getDirectoryName(thirdDirId));
//			assertEquals(true, viewer.getAllSubdirectoryNames(rootDirId).contains(originalFourthDirName));
//		}
//		if (thirdConflictResolution == 0)
//		{
//			assertEquals(true, viewer.getAllSubdirectoryNames(firstChildDirId).contains(originalFifthDirName));
//			assertEquals(true, viewer.getAllSubdirectoryNames(secondChildDirId).contains(originalSixthDirName));
//		}
//		else if (thirdConflictResolution == 1)
//		{
//			assertEquals(true, viewer.getAllSubdirectoryNames(rootDirId).contains(originalFifthDirName));
//			assertEquals(true, viewer.getAllSubdirectoryNames(secondChildDirId).contains(originalSixthDirName));
//		}
//		else 
//		{
//			assertEquals(true, viewer.getAllSubdirectoryNames(firstChildDirId).contains(originalFifthDirName));
//			assertEquals(true, viewer.getAllSubdirectoryNames(rootDirId).contains(originalSixthDirName));
//		}
//	}
//
//	@Test
//	public void testMoveDocumentAgainstMandD() throws Exception		//tests conflicts between move/move (by id) move/delete (by id)
//	{
//		final String thisFilePath = testDbFileName+4+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+4+"Project";
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath,MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String originalFirstDirName = "a";
//		final String originalSecondDirName = "b";
//		final String firstDirId = UUID.randomUUID().toString();
//		final String secondDirId = UUID.randomUUID().toString();
//
//		final String firstParentDirName = "dir";
//		final String firstParentDirId = UUID.randomUUID().toString();
//		final String secondParentDirName = "secondDir";
//		final String secondParentDirId = UUID.randomUUID().toString();
//
//		mockIDE.sendCreateNewDirectoryEventToServer(firstParentDirId, firstParentDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(secondParentDirId, secondParentDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(firstDirId, originalFirstDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(secondDirId, originalSecondDirName, rootDirId);
//		
//		//Shared state is set with
//		//		/a						(directory)
//		//		/b						(directory)
//		//		/dir					(directory)
//		//		/secondDir				(directory)
//		
//		mockIDE.commit("Test Four", "All systems ready");
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch twice so that we can have two nodes to merge
//
//		assertEquals(true, viewer.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//		assertEquals(true, viewer.getAllSubdirectoryNames(rootDirId).contains(originalSecondDirName));
//		assertEquals(4, viewer.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(0, viewer.getAllSubdirectoryNames(firstDirId).size());
//		assertEquals(0, viewer.getAllSubdirectoryNames(secondDirId).size());
//		
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		//Here's the branches
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//		//Go find the two nodes
//		tree = viewer.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//
//		String firstNodeId = root.getChildAt(0).toString();
//		String secondNodeId = root.getChildAt(1).toString();
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//		
//		
//		//The first dev is going to try to move both files to firstDir
//		mockIDE.openProject(thisFilePath, thisProjectName,firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		mockIDE.sendMoveDirectoryEventToServer(firstDirId, rootDirId, firstParentDirId);
//		mockIDE.sendMoveDirectoryEventToServer(secondDirId, rootDirId, firstParentDirId);
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//		
//		//The second dev is going to try to move the first file to firstDir and delete the second file
//		mockIDE.openProject(thisFilePath, thisProjectName,secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		mockIDE.sendDeleteDirectoryEventToServer(secondDirId, originalSecondDirName, rootDirId);
//		mockIDE.sendMoveDirectoryEventToServer(firstDirId, rootDirId, secondParentDirId);
//
//		//Now we merge		
//		//Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//					FirstNode:													SecondNode:
//		//				(umc) Move dir "a" to dir								(umc) Delete dir "a"
//		//				(umc) Move dir "b" from rootDir to dir					(umc) Move dir "b" from rootDir to secondDir
//		
//		Set<String> previousMergeIds = new HashSet<String>();
//
//		for(int firstConflictResolution = 0; firstConflictResolution<3;firstConflictResolution++)
//		{
//			for(int secondConflictResolution = 0; secondConflictResolution<3;secondConflictResolution++)
//			{
//				testMoveDocumentAgainstMandDHelper(firstNodeId, secondNodeId, firstConflictResolution, secondConflictResolution, rootDirId, firstParentDirId, secondParentDirId, firstDirId, secondDirId, originalFirstDirName, originalSecondDirName, previousMergeIds);
//			}
//		}
//	}
//	
//	private void testMoveDocumentAgainstMandDHelper(String firstNodeId, String secondNodeId, int firstConflictResolution, int secondConflictResolution,
//			String rootDirId, String firstParentDirId, String secondParentDirId, String firstDirId, String secondDirId,
//			String originalFirstDirName, String originalSecondDirName, Set<String> previousMergeIds) throws Exception
//	{
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//		
//		assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//		assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalSecondDirName));
//		assertEquals(4, handler.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(0, handler.getAllSubdirectoryNames(firstParentDirId).size());
//		assertEquals(0, handler.getAllSubdirectoryNames(secondParentDirId).size());
//		
//		//========================================================================================
//		handler.handleManualConflictCompletely(firstConflictResolution);		//this is the moving of the first directory
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(firstConflictResolution==1, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalFirstDirName));
//		
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(firstConflictResolution==2, handler.getAllSubdirectoryNames(secondParentDirId).contains(originalFirstDirName));
//		
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(true, handler.directoryExists(firstDirId));
//		if (firstConflictResolution==0)
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//			assertEquals(false, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalFirstDirName));
//			assertEquals(false, handler.getAllSubdirectoryNames(secondParentDirId).contains(originalFirstDirName));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(false, handler.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//			assertEquals(true, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalFirstDirName));
//			assertEquals(false, handler.getAllSubdirectoryNames(secondParentDirId).contains(originalFirstDirName));
//		}
//		else
//		{
//			assertEquals(false, handler.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//			assertEquals(false, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalFirstDirName));
//			assertEquals(true, handler.getAllSubdirectoryNames(secondParentDirId).contains(originalFirstDirName));
//		}
//		
//		//========================================================================================
//		handler.handleManualConflictCompletely(secondConflictResolution);					//this is the move vs delete
//		assertEquals(false, handler.isMerging());
//		
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(secondConflictResolution==1, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalSecondDirName));
//		
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(secondConflictResolution!=2, handler.directoryExists(secondDirId));
//		
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(true, handler.directoryExists(firstDirId));
//		if (firstConflictResolution==0)
//		{
//			assertEquals(true, handler.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//			assertEquals(false, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalFirstDirName));
//			assertEquals(false, handler.getAllSubdirectoryNames(secondParentDirId).contains(originalFirstDirName));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(false, handler.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//			assertEquals(true, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalFirstDirName));
//			assertEquals(false, handler.getAllSubdirectoryNames(secondParentDirId).contains(originalFirstDirName));
//		}
//		else
//		{
//			assertEquals(false, handler.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//			assertEquals(false, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalFirstDirName));
//			assertEquals(true, handler.getAllSubdirectoryNames(secondParentDirId).contains(originalFirstDirName));
//		}
//		if (secondConflictResolution==0)
//		{
//			assertEquals(true, handler.directoryExists(secondDirId));
//			assertEquals(false, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalSecondDirName));
//		}
//		else if (secondConflictResolution==1)
//		{
//			assertEquals(true, handler.directoryExists(secondDirId));
//			assertEquals(true, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalSecondDirName));
//		}
//		else 
//		{
//			assertEquals(false, handler.directoryExists(secondDirId));
//			assertEquals(false, handler.getAllSubdirectoryNames(firstParentDirId).contains(originalSecondDirName));
//		}
//		
//		//======================================================================
//		//Now to play it back
//		String mergeId = null;
//		DefaultTreeModel tree = handler.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			String childId = root.getChildAt(i).toString();
//			if (!childId.equals(firstNodeId)&&!childId.equals(secondNodeId)&&!previousMergeIds.contains(childId))
//			{
//				mergeId = childId;
//				break;
//			}
//		}
//		if (mergeId == null)
//		{
//			fail("Merge node could not be found");
//		}
//		previousMergeIds.add(mergeId);
//
//		PlaybackEventRenderer viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeId);
//		
//		if (firstConflictResolution==0)
//		{
//			assertEquals(true, viewer.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//			assertEquals(false, viewer.getAllSubdirectoryNames(firstParentDirId).contains(originalFirstDirName));
//			assertEquals(false, viewer.getAllSubdirectoryNames(secondParentDirId).contains(originalFirstDirName));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(false, viewer.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//			assertEquals(true, viewer.getAllSubdirectoryNames(firstParentDirId).contains(originalFirstDirName));
//			assertEquals(false, viewer.getAllSubdirectoryNames(secondParentDirId).contains(originalFirstDirName));
//		}
//		else
//		{
//			assertEquals(false, viewer.getAllSubdirectoryNames(rootDirId).contains(originalFirstDirName));
//			assertEquals(false, viewer.getAllSubdirectoryNames(firstParentDirId).contains(originalFirstDirName));
//			assertEquals(true, viewer.getAllSubdirectoryNames(secondParentDirId).contains(originalFirstDirName));
//		}
//		if (secondConflictResolution==0)
//		{
//			assertEquals(true, viewer.directoryExists(secondDirId));
//			assertEquals(false, viewer.getAllSubdirectoryNames(firstParentDirId).contains(originalSecondDirName));
//		}
//		else if (secondConflictResolution==1)
//		{
//			assertEquals(true, viewer.directoryExists(secondDirId));
//			assertEquals(true, viewer.getAllSubdirectoryNames(firstParentDirId).contains(originalSecondDirName));
//		}
//		else 
//		{
//			assertEquals(false, viewer.directoryExists(secondDirId));
//			assertEquals(false, viewer.getAllSubdirectoryNames(firstParentDirId).contains(originalSecondDirName));
//		}
//	}
//	
//}
