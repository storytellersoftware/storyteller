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
//
///**
// * 
// * Some lessons were learned during the creation of partOne and number one was DONT MAKE TESTS THAT BUILD ON EACH OTHER.
// * 
// * It gets very confusing and long to test and long to debug.  That being said, partOne does a good job of testing more 
// * "real-world" scenarios because it will be common to have long trees of nodes.  However, since partOne has tested those 
// * extended cases well, partTwo will keep things as concise and modular as possible.  
// * 
// * So far, what has been tested by partOne?
// * - Text against Text, both deletes and inserts
// * - CreateDocuments against CreateDocuments, RenameDocuments and MoveDocuments
// * - Merging branches that happened in the middle of a node, (to test issue #29)
// * 
// * partTwo should handle at least all the rest of the document conflict cases
// *
// * 8/3/12 for now, two developers deleting the same document will not be tested because that should be handled 
// * as an automatic conflict, but for now is a manual conflict.  See issue #28 in bitbucket.
// */
//public class TestMergingPartTwo
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
//	private static String testDbFileName = "unitTestDBForTestMergePartTwoTest";
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
//	private static Logger logger = Logger.getLogger(TestMergingPartTwo.class.getName());
//	@SuppressWarnings("unused")
//	private static Logger timer = Logger.getLogger("timing."+TestMergingPartTwo.class.getName());
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
//		//mockIDE.closeProject();		//we'll have the tests responsible for closing the project
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
//
//	@After
//	public void tearDown() throws Exception
//	{
//		logger.debug("Tearing down test case");
//		mockIDE.closeProject();
//		Thread.sleep(500);
//		logger.debug("Test case torn down");
//	}
//
//
//
//	@Test
//	public void testRenameDocumentAgainstRMandD() throws Exception		//tests conflicts between rename/rename (by id) rename/move (by id) and rename/delete
//	{
//		logger.debug("Starting testRenameDocumentAgainstRMandD()");
//		final String thisFilePath = testDbFileName+1+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+1+"Project";
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String originalFirstDocName = "a.txt";
//		final String originalSecondDocName = "b.txt";
//		final String originalThirdDocName = "c.txt";
//		final String firstDocId = UUID.randomUUID().toString();
//		final String secondDocId = UUID.randomUUID().toString();
//		final String thirdDocId = UUID.randomUUID().toString();
//
//		final String firstDirName = "dir";
//		final String firstDirId = UUID.randomUUID().toString();
//
//		mockIDE.sendCreateNewDirectoryEventToServer(firstDirId, firstDirName, rootDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(secondDocId, originalSecondDocName, rootDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(thirdDocId, originalThirdDocName, rootDirId);
//
//		mockIDE.commit("Test One", "All systems set");
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch one more time so that we can have two nodes to merge
//
//		assertEquals(3, viewer.getAllDocumentNames(rootDirId).size());
//		assertEquals(1, viewer.getAllSubdirectoryNames(rootDirId).size());
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
//		final String devOneFirstDocName = "alpha.txt";
//		final String devOneSecondDocName = "beta.txt";
//		final String devOneThirdDocName = "gamma.txt";
//		mockIDE.openProject(thisFilePath, thisProjectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		mockIDE.sendRenameDocumentEventToServer(firstDocId, originalFirstDocName, devOneFirstDocName, rootDirId);
//		mockIDE.sendRenameDocumentEventToServer(secondDocId, originalSecondDocName, devOneSecondDocName, rootDirId);
//		mockIDE.sendRenameDocumentEventToServer(thirdDocId, originalThirdDocName, devOneThirdDocName, rootDirId);
//
//		logger.info("All renameDocuments in test 1 sent, closing project");
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		//second developer is going to rename the first document, move the second document and delete the third document
//		final String devTwoFirstDocName = "A.txt";
//
//		mockIDE.openProject(thisFilePath, thisProjectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		//We'll things to from third document to first document.  Just because.  
//		mockIDE.sendDeleteDocumentEventToServer(thirdDocId, originalThirdDocName, rootDirId);
//		mockIDE.sendMoveDocumentEventToServer(secondDocId, rootDirId, firstDirId);
//		mockIDE.sendRenameDocumentEventToServer(firstDocId, originalFirstDocName, devTwoFirstDocName, rootDirId);
//
//		//And now we merge, handling the three conflicts, using all 27 combinations possible for those resolutions
//
//		//Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//					FirstNode:														SecondNode:
//		//				(umc) Rename doc "a.txt" to "alpha.txt"						(umc) delete doc "c.txt"
//		//				(umc) Rename doc "b.txt" to "beta.txt"						(umc) move doc "b.txt" to /dir/ 
//		//				(umc) Rename doc "c.txt" to "gamma.txt"						(umc) Rename doc "a.txt" to "A.txt"
//
//		Set<String> previousMergeIds = new HashSet<String>();
//
//		for(int firstConflictResolution = 0; firstConflictResolution<3;firstConflictResolution++)
//		{
//			for(int secondConflictResolution = 0; secondConflictResolution<3;secondConflictResolution++)
//			{
//				for(int thirdConflictResolution = 0;thirdConflictResolution<3;thirdConflictResolution++)
//				{
//					MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//					testRenameDocumentAgainstRMandDHelper(handler, firstNodeId, secondNodeId, firstConflictResolution, secondConflictResolution, 
//							thirdConflictResolution, rootDirId, devOneFirstDocName, devOneSecondDocName, devOneThirdDocName, devTwoFirstDocName,
//							firstDocId, secondDocId, thirdDocId, originalFirstDocName, originalSecondDocName, originalThirdDocName, firstDirId, previousMergeIds);
//				}
//			}
//		}
//		logger.debug("Finishing testRenameDocumentAgainstRMandD()");
//
//	}
//
//	/**
//	 * A helper method that runs the merging given the three conflict resolutions and then properly asserts went well
//	 */
//	private void testRenameDocumentAgainstRMandDHelper(MockMergeHandler handler,String firstNodeId, String secondNodeId, int firstConflictResolution, int secondConflictResolution, int thirdConflictResolution, 
//			String rootDirId, String devOneFirstDocName, String devOneSecondDocName, String devOneThirdDocName, String devTwoFirstDocName, String firstDocId, String secondDocId,
//			String thirdDocId, String originalFirstDocName, String originalSecondDocName, String originalThirdDocName, String firstDirId, Set<String> previousMergeIds)
//	{
//		assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		assertEquals(1, handler.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));
//		assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));		//just check that everything is at the ancestor state
//
//		//=======================================================================================================
//		handler.handleManualConflictCompletely(firstConflictResolution);		//this will be the rename vs rename
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		assertEquals(1, handler.getAllSubdirectoryNames(rootDirId).size());
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(devOneFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));	//no change
//		}
//		assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		assertEquals(1, handler.getAllSubdirectoryNames(rootDirId).size());
//		if (firstConflictResolution == 2)
//		{
//			assertEquals(devTwoFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));	//no change
//		}
//		assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		assertEquals(1, handler.getAllSubdirectoryNames(rootDirId).size());
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(devOneFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else if (firstConflictResolution == 2)
//		{
//			assertEquals(devTwoFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));	//no change
//		}
//		assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//
//		//=======================================================================================================
//		handler.handleManualConflictCompletely(secondConflictResolution);		//this will be the rename vs move
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(1, handler.getAllSubdirectoryNames(rootDirId).size());
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(devOneFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));	//no change
//		}
//
//		if (secondConflictResolution == 1)
//		{
//			assertEquals(devOneSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		}
//		else 
//		{
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		}
//
//		assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(1, handler.getAllSubdirectoryNames(rootDirId).size());
//		if (firstConflictResolution == 2)
//		{
//			assertEquals(devTwoFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));	//no change
//		}
//
//		if (secondConflictResolution == 2)
//		{
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(2, handler.getAllDocumentNames(rootDirId).size());
//			assertEquals(originalSecondDocName, handler.getAllDocumentNames(firstDirId).get(0));
//		}
//		else 
//		{
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		}
//		assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(1, handler.getAllSubdirectoryNames(rootDirId).size());
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(devOneFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else if (firstConflictResolution == 2)
//		{
//			assertEquals(devTwoFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));	//no change
//		}
//
//		if (secondConflictResolution == 1)
//		{
//			assertEquals(devOneSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		}
//		else if (secondConflictResolution == 2)
//		{
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(2, handler.getAllDocumentNames(rootDirId).size());
//			assertEquals(originalSecondDocName, handler.getAllDocumentNames(firstDirId).get(0));
//		}
//		else 
//		{
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		}
//		assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//		//=======================================================================================================
//		handler.handleManualConflictCompletely(thirdConflictResolution);		//this will be the rename vs delete
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(1, handler.getAllSubdirectoryNames(rootDirId).size());
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(devOneFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));	//no change
//		}
//
//		if (secondConflictResolution == 1)
//		{
//			assertEquals(devOneSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		}
//		else 
//		{
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		}
//
//		if (thirdConflictResolution == 1)
//		{
//			assertEquals(devOneThirdDocName, handler.getDocumentName(thirdDocId));
//		}
//		else 
//		{
//			assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(1, handler.getAllSubdirectoryNames(rootDirId).size());
//		if (firstConflictResolution == 2)
//		{
//			assertEquals(devTwoFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));	//no change
//		}
//
//		if (secondConflictResolution == 2)
//		{
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(originalSecondDocName, handler.getAllDocumentNames(firstDirId).get(0));
//		}
//		else 
//		{
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		}
//
//		if (thirdConflictResolution == 2)
//		{
//			assertEquals(false, handler.hasDocument(thirdDocId));
//		}
//		else 
//		{
//			assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//		}
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(1, handler.getAllSubdirectoryNames(rootDirId).size());
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(devOneFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else if (firstConflictResolution == 2)
//		{
//			assertEquals(devTwoFirstDocName, handler.getDocumentName(firstDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));	//no change
//		}
//
//		if (secondConflictResolution == 1)
//		{
//			assertEquals(devOneSecondDocName, handler.getDocumentName(secondDocId));
//		}
//		else if (secondConflictResolution == 2)
//		{
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//			assertEquals(originalSecondDocName, handler.getAllDocumentNames(firstDirId).get(0));
//		}
//		else 
//		{
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		}
//
//		if (thirdConflictResolution == 1)
//		{
//			assertEquals(devOneThirdDocName, handler.getDocumentName(thirdDocId));
//		}
//		else if (thirdConflictResolution == 2)
//		{
//			assertEquals(false, handler.hasDocument(thirdDocId));
//		}
//		else 
//		{
//			assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//		}
//		//=======================================================================================================
//		//Done merging, now to watch the playback
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
//		assertEquals(1, viewer.getAllSubdirectoryNames(rootDirId).size());
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(devOneFirstDocName, viewer.getDocumentName(firstDocId));
//		}
//		else if (firstConflictResolution == 2)
//		{
//			assertEquals(devTwoFirstDocName, viewer.getDocumentName(firstDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, viewer.getDocumentName(firstDocId));	//no change
//		}
//
//		if (secondConflictResolution == 1)
//		{
//			assertEquals(devOneSecondDocName, viewer.getDocumentName(secondDocId));
//		}
//		else if (secondConflictResolution == 2)
//		{
//			assertEquals(originalSecondDocName, viewer.getDocumentName(secondDocId));
//			assertEquals(originalSecondDocName, viewer.getAllDocumentNames(firstDirId).get(0));
//		}
//		else 
//		{
//			assertEquals(originalSecondDocName, viewer.getDocumentName(secondDocId));
//		}
//
//		if (thirdConflictResolution == 1)
//		{
//			assertEquals(devOneThirdDocName, viewer.getDocumentName(thirdDocId));
//		}
//		else if (thirdConflictResolution == 2)
//		{
//			assertEquals(false, viewer.hasDocument(thirdDocId));
//		}
//		else 
//		{
//			assertEquals(originalThirdDocName, viewer.getDocumentName(thirdDocId));
//		}
//	}
//
//	@Test
//	public void testRenameDocumentAndMoveDocumentAgainstRandM() throws Exception		//Tests Rename/Rename (by name), Rename/Move (by name) and Move/Move (by name)
//	{
//		logger.debug("Starting testRenameDocumentAndMoveDocumentAgainstRandM()");
//		final String thisFilePath = testDbFileName+8+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+8+"Project";
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String conflictingNameRenameRename = "renameRename.txt";			//will be the name used in the rename vs rename (by name)
//		final String conflictingNameRenameMove = "renameMove.txt";				//will be the name used in the rename vs move (by name)
//		final String conflictingNameMoveMove = "moveMove.txt";					//will be the name used in the move vs move (by name)
//
//		final String originalFirstDocName = "a.txt";				//will be the point of conflict against a rename vs rename (first doc)
//		final String originalSecondDocName = "b.txt";				//will be the point of conflict against a rename vs rename (second doc)
//		final String originalThirdDocName = "c.txt";				//will be the point of conflict against a rename vs move (renaming doc)
//		final String originalFourthDocName = conflictingNameRenameMove;				//will be the point of conflict against a rename vs move (moving doc)
//		final String originalFifthDocName = conflictingNameMoveMove;				//will be the point of conflict against a move vs move (second doc)
//		final String originalSixthDocName = conflictingNameMoveMove;				//will be the point of conflict against a move vs move (first doc)
//
//		final String firstDocId = UUID.randomUUID().toString();
//		final String secondDocId = UUID.randomUUID().toString();
//		final String thirdDocId = UUID.randomUUID().toString();
//		final String fourthDocId = UUID.randomUUID().toString();
//		final String fifthDocId = UUID.randomUUID().toString();
//		final String sixthDocId = UUID.randomUUID().toString();
//
//		final String firstChildDirId = UUID.randomUUID().toString();
//		final String firstChildDirName = "child";
//		final String secondChildDirId = UUID.randomUUID().toString();
//		final String secondChildDirName = "otherChild";
//
//		mockIDE.sendCreateNewDirectoryEventToServer(firstChildDirId, firstChildDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(secondChildDirId, secondChildDirName, rootDirId);
//
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(secondDocId, originalSecondDocName, rootDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(thirdDocId, originalThirdDocName, rootDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(fourthDocId, originalFourthDocName, firstChildDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(fifthDocId, originalFifthDocName, firstChildDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(sixthDocId, originalSixthDocName, secondChildDirId);
//
//		//Shared state is set with
//		//		/a.txt						(document)
//		//		/b.txt						(document)
//		//		/c.txt						(document)
//		//		/child						(directory)
//		//		/child/renameMove.txt		(document)
//		//		/child/moveMove.txt			(document)
//		//		/otherChild					(directory)
//		//		/otherChild/moveMove.txt	(document)
//
//		mockIDE.commit("Test Eight", "All systems set");
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch one more time so that we can have two nodes to merge
//
//		assertEquals(3, viewer.getAllDocumentNames(rootDirId).size());
//		assertEquals(2, viewer.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(2, viewer.getAllDocumentNames(firstChildDirId).size());
//		assertEquals(1, viewer.getAllDocumentNames(secondChildDirId).size());
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
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
//		//First dev will rename the first and third docs to the names that will be conflicts and
//		//move the fifth doc into root
//		mockIDE.openProject(thisFilePath, thisProjectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		mockIDE.sendRenameDocumentEventToServer(firstDocId, originalFirstDocName, conflictingNameRenameRename, rootDirId);
//		mockIDE.sendRenameDocumentEventToServer(thirdDocId, originalThirdDocName, conflictingNameRenameMove, rootDirId);
//		mockIDE.sendMoveDocumentEventToServer(fifthDocId, firstChildDirId, rootDirId);
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		mockIDE.openProject(thisFilePath, thisProjectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		mockIDE.sendMoveDocumentEventToServer(sixthDocId, secondChildDirId, rootDirId);
//		mockIDE.sendRenameDocumentEventToServer(secondDocId, originalSecondDocName, conflictingNameRenameRename, rootDirId);
//		mockIDE.sendMoveDocumentEventToServer(fourthDocId, firstChildDirId, rootDirId);
//
//		//Now we merge
//		//Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//					FirstNode:														SecondNode:
//		//				(umc) Rename doc "a.txt" to "renameRename.txt"						(umc) Move /otherChild/moveMove.txt to rootDoc
//		//				(umc) Rename doc "c.txt" to "renameMove.txt"						(umc) Rename doc "b.txt" to "renameRename" 
//		//				(umc) Move doc /child/moveMove.txt to rootdir						(umc) Move /child/renameMove.txt to rootDoc
//
//		Set<String> previousMergeIds = new HashSet<String>();
//
//		for(int firstConflictResolution = 0;firstConflictResolution<3;firstConflictResolution++)
//		{
//			for(int secondConflictResolution = 0;secondConflictResolution<3;secondConflictResolution++)
//			{
//				for (int thirdConflictResolution = 0;thirdConflictResolution<3;thirdConflictResolution++)
//				{
//					testRenameDocumentAndMoveDocumentAgainstRandMHelper(firstNodeId, secondNodeId, firstConflictResolution, secondConflictResolution, 
//							thirdConflictResolution, rootDirId, firstChildDirId, secondChildDirId, firstDocId, secondDocId, thirdDocId, originalFirstDocName,
//							originalSecondDocName, originalThirdDocName, originalFourthDocName, originalFifthDocName, originalSixthDocName, 
//							conflictingNameRenameMove, conflictingNameRenameRename, previousMergeIds);
//				}
//			}
//		}
//		logger.debug("Finishing testRenameDocumentAndMoveDocumentAgainstRandM()");
//	}
//
//	private void testRenameDocumentAndMoveDocumentAgainstRandMHelper(String firstNodeId, String secondNodeId, int firstConflictResolution, int secondConflictResolution,
//			int thirdConflictResolution, String rootDirId, String firstChildDirId, String secondChildDirId, String firstDocId, String secondDocId, String thirdDocId,
//			String originalFirstDocName, String originalSecondDocName, String originalThirdDocName, String originalFourthDocName, String originalFifthDocName,
//			String originalSixthDocName, String conflictingNameRenameMove, String conflictingNameRenameRename, Set<String> previousMergeIds) throws IOException, JSONException
//			{
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//
//		assertEquals(3, handler.getAllDocumentNames(rootDirId).size());
//		assertEquals(2, handler.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(2, handler.getAllDocumentNames(firstChildDirId).size());
//		assertEquals(1, handler.getAllDocumentNames(secondChildDirId).size());
//
//		handler.handleManualConflictCompletely(firstConflictResolution);		//this will be the rename vs rename
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(firstConflictResolution==1?conflictingNameRenameRename:originalFirstDocName, handler.getDocumentName(firstDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(firstConflictResolution==2?conflictingNameRenameRename:originalSecondDocName, handler.getDocumentName(secondDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(conflictingNameRenameRename, handler.getDocumentName(firstDocId));
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));
//			assertEquals(conflictingNameRenameRename, handler.getDocumentName(secondDocId));
//		}
//
//
//		handler.handleManualConflictCompletely(secondConflictResolution);		//this will be the rename vs move
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(secondConflictResolution==1?conflictingNameRenameMove:originalThirdDocName, handler.getDocumentName(thirdDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		if (secondConflictResolution == 2)
//		{
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFourthDocName));
//		}
//		else 
//		{
//			assertEquals(true, handler.getAllDocumentNames(firstChildDirId).contains(originalFourthDocName));
//		}
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(conflictingNameRenameRename, handler.getDocumentName(firstDocId));
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));
//			assertEquals(conflictingNameRenameRename, handler.getDocumentName(secondDocId));
//		}
//		if (secondConflictResolution == 0)
//		{
//			assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//			assertEquals(true, handler.getAllDocumentNames(firstChildDirId).contains(originalFourthDocName));
//		}
//		else if (secondConflictResolution == 1)
//		{
//			assertEquals(conflictingNameRenameMove, handler.getDocumentName(thirdDocId));
//			assertEquals(true, handler.getAllDocumentNames(firstChildDirId).contains(originalFourthDocName));
//		}
//		else 
//		{
//			assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFourthDocName));
//		}
//
//
//		handler.handleManualConflictCompletely(thirdConflictResolution);		//this will be the move vs move
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		if (thirdConflictResolution == 1)
//		{
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFifthDocName));
//		}
//		else 
//		{
//			assertEquals(true, handler.getAllDocumentNames(firstChildDirId).contains(originalFifthDocName));
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		if (thirdConflictResolution == 2)
//		{
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalSixthDocName));
//		}
//		else 
//		{
//			assertEquals(true, handler.getAllDocumentNames(secondChildDirId).contains(originalSixthDocName));
//		}
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		if (firstConflictResolution==0)
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(conflictingNameRenameRename, handler.getDocumentName(firstDocId));
//			assertEquals(originalSecondDocName, handler.getDocumentName(secondDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));
//			assertEquals(conflictingNameRenameRename, handler.getDocumentName(secondDocId));
//		}
//		if (secondConflictResolution == 0)
//		{
//			assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//			assertEquals(true, handler.getAllDocumentNames(firstChildDirId).contains(originalFourthDocName));
//		}
//		else if (secondConflictResolution == 1)
//		{
//			assertEquals(conflictingNameRenameMove, handler.getDocumentName(thirdDocId));
//			assertEquals(true, handler.getAllDocumentNames(firstChildDirId).contains(originalFourthDocName));
//		}
//		else 
//		{
//			assertEquals(originalThirdDocName, handler.getDocumentName(thirdDocId));
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFourthDocName));
//		}
//		if (thirdConflictResolution == 0)
//		{
//			assertEquals(true, handler.getAllDocumentNames(firstChildDirId).contains(originalFifthDocName));
//			assertEquals(true, handler.getAllDocumentNames(secondChildDirId).contains(originalSixthDocName));
//		}
//		else if (thirdConflictResolution == 1)
//		{
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFifthDocName));
//			assertEquals(true, handler.getAllDocumentNames(secondChildDirId).contains(originalSixthDocName));
//		}
//		else 
//		{
//			assertEquals(true, handler.getAllDocumentNames(firstChildDirId).contains(originalFifthDocName));
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalSixthDocName));
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
//			assertEquals(originalFirstDocName, viewer.getDocumentName(firstDocId));
//			assertEquals(originalSecondDocName, viewer.getDocumentName(secondDocId));
//		}
//		else if (firstConflictResolution==1)
//		{
//			assertEquals(conflictingNameRenameRename, viewer.getDocumentName(firstDocId));
//			assertEquals(originalSecondDocName, viewer.getDocumentName(secondDocId));
//		}
//		else
//		{
//			assertEquals(originalFirstDocName, viewer.getDocumentName(firstDocId));
//			assertEquals(conflictingNameRenameRename, viewer.getDocumentName(secondDocId));
//		}
//		if (secondConflictResolution == 0)
//		{
//			assertEquals(originalThirdDocName, viewer.getDocumentName(thirdDocId));
//			assertEquals(true, viewer.getAllDocumentNames(firstChildDirId).contains(originalFourthDocName));
//		}
//		else if (secondConflictResolution == 1)
//		{
//			assertEquals(conflictingNameRenameMove, viewer.getDocumentName(thirdDocId));
//			assertEquals(true, viewer.getAllDocumentNames(firstChildDirId).contains(originalFourthDocName));
//		}
//		else 
//		{
//			assertEquals(originalThirdDocName, viewer.getDocumentName(thirdDocId));
//			assertEquals(true, viewer.getAllDocumentNames(rootDirId).contains(originalFourthDocName));
//		}
//		if (thirdConflictResolution == 0)
//		{
//			assertEquals(true, viewer.getAllDocumentNames(firstChildDirId).contains(originalFifthDocName));
//			assertEquals(true, viewer.getAllDocumentNames(secondChildDirId).contains(originalSixthDocName));
//		}
//		else if (thirdConflictResolution == 1)
//		{
//			assertEquals(true, viewer.getAllDocumentNames(rootDirId).contains(originalFifthDocName));
//			assertEquals(true, viewer.getAllDocumentNames(secondChildDirId).contains(originalSixthDocName));
//		}
//		else 
//		{
//			assertEquals(true, viewer.getAllDocumentNames(firstChildDirId).contains(originalFifthDocName));
//			assertEquals(true, viewer.getAllDocumentNames(rootDirId).contains(originalSixthDocName));
//		}
//			}
//
//
//	@Test
//	public void testAutomaticRenameDocumentAndAddingOfText() throws Exception
//	{
//		logger.debug("Starting testAutomaticRenameDocumentAndAddingOfText()");
//		final String thisFilePath = testDbFileName+2+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+2+"Project";
//				
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String originalFirstDocName = "test.java";
//		final String firstDocId = UUID.randomUUID().toString();
//
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//		mockIDE.setCurrentDocumentId(firstDocId);
//		final String originalText = "System.out.println(\"Hello World!\");";
//		mockIDE.sendStringToServer(originalText, 0);
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch twice time so that we can have two nodes to merge
//
//		assertEquals(originalFirstDocName, viewer.getAllDocumentNames(rootDirId).get(0));
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		//Here's the branch
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//		//Go find the two nodes
//		tree = viewer.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//
//		String firstNodeId = root.getChildAt(0).toString();
//		String secondNodeId = root.getChildAt(1).toString();
//
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		//First dev renames the file 
//		final String devOneFirstDocName = "HelloWorld.java";
//		mockIDE.openProject(thisFilePath, thisProjectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(originalText, mockIDE.getCurrentBuffer());
//
//		mockIDE.sendRenameDocumentEventToServer(firstDocId, originalFirstDocName, devOneFirstDocName, rootDirId);
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		//Dev 2 did some work in that file
//		mockIDE.openProject(thisFilePath, thisProjectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(originalText, mockIDE.getCurrentBuffer());
//		//					mockIDE is 	 "System.out.println(\"Hello World!\");"
//		mockIDE.sendDeletesToServer ("_____________________XXXXXXXXXXX", 0);
//		mockIDE.insertStringToServer("_____________________Hey there Fox", "_", 0);
//		final String devTwoFinalText = mockIDE.getCurrentBuffer();
//
//		//Now we merge
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));
//		assertEquals(originalText, handler.getDocumentText(firstDocId));
//
//		handler.handleAutomaticConflict();			//this should be the rename
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(true,handler.isMerging());
//		assertEquals(devOneFirstDocName, handler.getDocumentName(firstDocId));
//		assertEquals(originalText, handler.getDocumentText(firstDocId));
//
//		for(int i = 0;i<12;i++)
//		{
//			handler.handleAutomaticConflict();			//this should be the text editing. 11 deletes and 1 text
//		}
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(false,handler.isMerging());
//		assertEquals(devOneFirstDocName, handler.getDocumentName(firstDocId));
//		assertEquals(devTwoFinalText, handler.getDocumentText(firstDocId));
//
//
//		String mergeId = null;
//		tree = handler.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			String childId = root.getChildAt(i).toString();
//			if (!childId.equals(firstNodeId)&&!childId.equals(secondNodeId))
//			{
//				mergeId = childId;
//				break;
//			}
//		}
//		if (mergeId == null)
//		{
//			fail("Merge node could not be found");
//		}
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeId);
//		assertEquals(devOneFirstDocName, viewer.getDocumentName(firstDocId));
//		assertEquals(devTwoFinalText, viewer.getDocumentText(firstDocId));
//
//
//		logger.debug("Finishing testAutomaticRenameDocumentAndAddingOfText()");
//	}
//
//
//	@Test
//	public void testMoveDocumentAgainstMandD() throws Exception		//Tests moveDocumentEvents against moveDocumentEvents and deleteDocumentEvents
//	{
//		logger.debug("Starting testMoveDocumentAgainstMandD()");
//		final String thisFilePath = testDbFileName+3+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+3+"Project";
//				
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String originalFirstDocName = "a.txt";
//		final String originalSecondDocName = "b.txt";
//		final String firstDocId = UUID.randomUUID().toString();
//		final String secondDocId = UUID.randomUUID().toString();
//
//		final String firstDirName = "dir";
//		final String firstDirId = UUID.randomUUID().toString();
//		final String secondDirName = "secondDir";
//		final String secondDirId = UUID.randomUUID().toString();
//
//		mockIDE.sendCreateNewDirectoryEventToServer(firstDirId, firstDirName, rootDirId);
//		mockIDE.sendCreateNewDirectoryEventToServer(secondDirId, secondDirName, rootDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//		mockIDE.sendCreateNewDocumentEventToServer(secondDocId, originalSecondDocName, rootDirId);
//
//		//Shared state is set with
//		//		/a.txt					(document)
//		//		/b.txt					(document)
//		//		/dir					(directory)
//		//		/secondDir				(directory)
//
//		mockIDE.commit("Test Three", "All systems ready");
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch twice so that we can have two nodes to merge
//
//		assertEquals(true, viewer.getAllDocumentNames(rootDirId).contains(originalFirstDocName));
//		assertEquals(true, viewer.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//		assertEquals(2, viewer.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(0, viewer.getAllDocumentNames(firstDirId).size());
//		assertEquals(0, viewer.getAllDocumentNames(secondDirId).size());
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
//		//The first dev is going to try to move both files to firstDir
//		mockIDE.openProject(thisFilePath, thisProjectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		mockIDE.sendMoveDocumentEventToServer(firstDocId, rootDirId, firstDirId);
//		mockIDE.sendMoveDocumentEventToServer(secondDocId, rootDirId, firstDirId);
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		//The second dev is going to try to move the first file to firstDir and delete the second file
//		mockIDE.openProject(thisFilePath, thisProjectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		mockIDE.sendMoveDocumentEventToServer(firstDocId, rootDirId, secondDirId);
//		mockIDE.sendDeleteDocumentEventToServer(secondDocId, originalSecondDocName, rootDirId);
//
//		//Now we merge		
//		Set<String> previousMergeIds = new HashSet<String>();
//
//		for(int firstConflictResolution = 0; firstConflictResolution<3;firstConflictResolution++)
//		{
//			for(int secondConflictResolution = 0; secondConflictResolution<3;secondConflictResolution++)
//			{
//				MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//				testMoveDocumentAgainstMandDHelper(handler, firstNodeId, secondNodeId, firstConflictResolution, secondConflictResolution, rootDirId, 
//						firstDocId, secondDocId, originalFirstDocName, originalSecondDocName, firstDirId, secondDirId, previousMergeIds);
//			}
//		}
//		logger.debug("Finishing testMoveDocumentAgainstMandD()");
//	}
//
//	private void testMoveDocumentAgainstMandDHelper(MockMergeHandler handler,String firstNodeId, String secondNodeId, int firstConflictResolution, int secondConflictResolution,  
//			String rootDirId, String firstDocId, String secondDocId, String originalFirstDocName, String originalSecondDocName, String firstDirId, String secondDirId, Set<String> previousMergeIds)
//	{
//		assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFirstDocName));
//		assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//		assertEquals(2, handler.getAllSubdirectoryNames(rootDirId).size());
//		assertEquals(0, handler.getAllDocumentNames(firstDirId).size());
//		assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//
//		handler.handleManualConflictCompletely(firstConflictResolution);		//this is the moving of the first document
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(true, handler.hasDocument(firstDocId));
//		assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(true, handler.getAllDocumentNames(firstDirId).contains(originalFirstDocName));
//			assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//		}
//		else
//		{
//			assertEquals(0, handler.getAllDocumentNames(firstDirId).size());
//			assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFirstDocName));
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(true, handler.hasDocument(firstDocId));
//		assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//		if (firstConflictResolution == 2)
//		{
//			assertEquals(true, handler.getAllDocumentNames(secondDirId).contains(originalFirstDocName));
//			assertEquals(0, handler.getAllDocumentNames(firstDirId).size());
//		}
//		else
//		{
//			assertEquals(0, handler.getAllDocumentNames(firstDirId).size());
//			assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFirstDocName));
//		}
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(true, handler.hasDocument(firstDocId));
//		assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(true, handler.getAllDocumentNames(firstDirId).contains(originalFirstDocName));
//			assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//		}
//		else if (firstConflictResolution == 2)
//		{
//			assertEquals(true, handler.getAllDocumentNames(secondDirId).contains(originalFirstDocName));
//			assertEquals(0, handler.getAllDocumentNames(firstDirId).size());
//		}
//		else
//		{
//			assertEquals(0, handler.getAllDocumentNames(firstDirId).size());
//			assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFirstDocName));
//		}
//
//		//========================================================================================
//		handler.handleManualConflictCompletely(secondConflictResolution);					//this is the move vs delete
//		assertEquals(false, handler.isMerging());
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals(true, handler.hasDocument(firstDocId));
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(true, handler.getAllDocumentNames(firstDirId).contains(originalFirstDocName));
//			assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//		}
//		else
//		{
//			assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFirstDocName));
//		}
//
//		if (secondConflictResolution == 1)
//		{
//			assertEquals(false, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//			assertEquals(true, handler.getAllDocumentNames(firstDirId).contains(originalSecondDocName));
//		}
//		else
//		{
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//			assertEquals(false, handler.getAllDocumentNames(firstDirId).contains(originalSecondDocName));
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(true, handler.hasDocument(firstDocId));
//		if (firstConflictResolution == 2)
//		{
//			assertEquals(true, handler.getAllDocumentNames(secondDirId).contains(originalFirstDocName));
//			assertEquals(0, handler.getAllDocumentNames(firstDirId).size());
//		}
//		else
//		{
//			assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFirstDocName));
//		}
//
//		if (secondConflictResolution == 2)
//		{
//			assertEquals(false, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//			assertEquals(false, handler.getAllDocumentNames(firstDirId).contains(originalSecondDocName));
//			assertEquals(false, handler.hasDocument(secondDocId));
//		}
//		else
//		{
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//			assertEquals(false, handler.getAllDocumentNames(firstDirId).contains(originalSecondDocName));
//		}
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(true, handler.hasDocument(firstDocId));
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(true, handler.getAllDocumentNames(firstDirId).contains(originalFirstDocName));
//			assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//		}
//		else if (firstConflictResolution == 2)
//		{
//			assertEquals(true, handler.getAllDocumentNames(secondDirId).contains(originalFirstDocName));
//		}
//		else
//		{
//			assertEquals(0, handler.getAllDocumentNames(secondDirId).size());
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalFirstDocName));
//		}
//
//		if (secondConflictResolution == 1)
//		{
//			assertEquals(false, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//			assertEquals(true, handler.getAllDocumentNames(firstDirId).contains(originalSecondDocName));
//		}
//		else if (secondConflictResolution == 2)
//		{
//			assertEquals(false, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//			assertEquals(false, handler.getAllDocumentNames(firstDirId).contains(originalSecondDocName));
//			assertEquals(false, handler.hasDocument(secondDocId));
//		}
//		else
//		{
//			assertEquals(true, handler.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//			assertEquals(false, handler.getAllDocumentNames(firstDirId).contains(originalSecondDocName));
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
//		assertEquals(true, viewer.hasDocument(firstDocId));
//		if (firstConflictResolution == 1)
//		{
//			assertEquals(true, viewer.getAllDocumentNames(firstDirId).contains(originalFirstDocName));
//			assertEquals(0, viewer.getAllDocumentNames(secondDirId).size());
//		}
//		else if (firstConflictResolution == 2)
//		{
//			assertEquals(true, viewer.getAllDocumentNames(secondDirId).contains(originalFirstDocName));
//		}
//		else
//		{
//			assertEquals(0, viewer.getAllDocumentNames(secondDirId).size());
//			assertEquals(true, viewer.getAllDocumentNames(rootDirId).contains(originalFirstDocName));
//		}
//
//		if (secondConflictResolution == 1)
//		{
//			assertEquals(false, viewer.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//			assertEquals(true, viewer.getAllDocumentNames(firstDirId).contains(originalSecondDocName));
//		}
//		else if (secondConflictResolution == 2)
//		{
//			assertEquals(false, viewer.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//			assertEquals(false, viewer.getAllDocumentNames(firstDirId).contains(originalSecondDocName));
//			assertEquals(false, viewer.hasDocument(secondDocId));
//		}
//		else
//		{
//			assertEquals(true, viewer.getAllDocumentNames(rootDirId).contains(originalSecondDocName));
//			assertEquals(false, viewer.getAllDocumentNames(firstDirId).contains(originalSecondDocName));
//		}
//	}
//
//	@Test
//	public void testAutomaticMoveDocumentAndAddingOfText() throws Exception
//	{
//		logger.debug("Starting testAutomaticMoveDocumentAndAddingOfText()");
//		final String thisFilePath = testDbFileName+4+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+4+"Project";
//				
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String originalFirstDocName = "test.java";
//		final String firstDocId = UUID.randomUUID().toString();
//		final String firstDirName = "dir";
//		final String firstDirId = UUID.randomUUID().toString();
//
//
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//		mockIDE.setCurrentDocumentId(firstDocId);
//		final String originalText = "System.out.println(\"Hello World!\");";
//		mockIDE.sendStringToServer(originalText, 0);
//
//		mockIDE.sendCreateNewDirectoryEventToServer(firstDirId, firstDirName, rootDirId);
//
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch twice time so that we can have two nodes to merge
//
//		assertEquals(originalText, viewer.getDocumentText(firstDocId));
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		//Here's the branch
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//		//Go find the two nodes
//		tree = viewer.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//
//		String firstNodeId = root.getChildAt(0).toString();
//		String secondNodeId = root.getChildAt(1).toString();
//
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		//First dev moves the file 
//		mockIDE.openProject(thisFilePath, thisProjectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(originalText, mockIDE.getCurrentBuffer());
//
//		mockIDE.sendMoveDocumentEventToServer(firstDocId, rootDirId, firstDirId);
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		//Dev 2 did some work in that file
//		mockIDE.openProject(thisFilePath, thisProjectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(originalText, mockIDE.getCurrentBuffer());
//		//						mockIDE is	 "System.out.println(\"Hello World!\");"
//		mockIDE.sendDeletesToServer ("_____________________XXXXXXXXXXX", 0);
//		mockIDE.insertStringToServer("_____________________Hey there Fox", "_", 0);
//		final String devTwoFinalText = mockIDE.getCurrentBuffer();
//
//		//Now we merge
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(originalFirstDocName, handler.getDocumentName(firstDocId));
//		assertEquals(originalText, handler.getDocumentText(firstDocId));
//
//		handler.handleAutomaticConflict();			//this should be the move
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(true,handler.isMerging());
//		assertEquals(originalFirstDocName, handler.getAllDocumentNames(firstDirId).get(0));
//		assertEquals(originalText, handler.getDocumentText(firstDocId));
//
//		for(int i = 0;i<12;i++)
//		{
//			handler.handleAutomaticConflict();			//this should be the text editing. 11 deletes and 1 text
//		}
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals(false,handler.isMerging());
//		assertEquals(originalFirstDocName, handler.getAllDocumentNames(firstDirId).get(0));
//		assertEquals(devTwoFinalText, handler.getDocumentText(firstDocId));
//
//
//		String mergeId = null;
//		tree = handler.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			String childId = root.getChildAt(i).toString();
//			if (!childId.equals(firstNodeId)&&!childId.equals(secondNodeId))
//			{
//				mergeId = childId;
//				break;
//			}
//		}
//		if (mergeId == null)
//		{
//			fail("Merge node could not be found");
//		}
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeId);
//		assertEquals(originalFirstDocName, handler.getAllDocumentNames(firstDirId).get(0));
//		assertEquals(devTwoFinalText, viewer.getDocumentText(firstDocId));
//		logger.debug("Finishing testAutomaticMoveDocumentAndAddingOfText()");
//	}
//
//	@Test
//	public void testDeleteDocumentAgainstText()	throws Exception	//this simulates if one dev deletes a document that the other dev was working in
//	{
//		logger.debug("Starting testDeleteDocumentAgainstText()");
//		final String thisFilePath = testDbFileName+5+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+5+"Project";
//				
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String originalFirstDocName = "a.txt";
//		final String firstDocId = UUID.randomUUID().toString();
//
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//		mockIDE.setCurrentDocumentId(firstDocId);
//		final String originalText = "System.out.println(\"Hello World!\");";
//		mockIDE.sendStringToServer(originalText, 0);
//
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch twice time so that we can have two nodes to merge
//
//		assertEquals(originalFirstDocName, viewer.getAllDocumentNames(rootDirId).get(0));
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		//Here's the branch
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
//		//Dev 1 did some work in that file
//		mockIDE.openProject(thisFilePath, thisProjectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(originalText, mockIDE.getCurrentBuffer());
//		//							mockIDE is "System.out.println(\"Hello World!\");"
//		mockIDE.sendDeletesToServer ("_____________________XXXXXXXXXXX", 0);
//		mockIDE.insertStringToServer("_____________________Hey there Fox", "_", 0);
//		mockIDE.sendStringToServer("//This is a comment\n", 0);
//		final String devOneFinalText = mockIDE.getCurrentBuffer();
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		//Dev 2 deleted the file
//		mockIDE.openProject(thisFilePath, thisProjectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.sendDeleteDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//
//		//Now we merge
//		//Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//							FirstNode:													SecondNode:
//		//				(umc) * 11	deletes									(umc) delete document
//		//				(umc) add in "Hey there fox"						
//		//				(umc) add "//This is a comment"
//
//		//If the user decides to stick with firstNode: the list would be 
//		//						FirstNode:													SecondNode:
//		//				(rc) * 11	deletes									(rc) delete document
//		//				(uac) add in "Hey there fox"						
//		//				(uac) add "//This is a comment"
//		//		and all the rest of firstNode's events can play out automatically
//
//		//If the user decides to stick with the secondNode and delete the document the list would look like
//		//						FirstNode:													SecondNode:
//		//				(rc) * 11	deletes									(rc) delete document
//		//				(un) add in "Hey there fox"						
//		//				(un) add "//This is a comment"
//		//		and that is all she wrote
//
//		//If the user decides to go with nobody and delete the document the list would also look like
//		//						FirstNode:													SecondNode:
//		//				(rc) * 11	deletes									(rc) delete document
//		//				(un) add in "Hey there fox"						
//		//				(un) add "//This is a comment"
//		//		and that is all she wrote, no more events to run
//
//		Set<String> previousMergeIds = new HashSet<String>();
//
//		for (int conflictResolution = 0; conflictResolution < 3; conflictResolution++)
//		{
//			logger.trace("Conflict Resolution: "+conflictResolution);
//			MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//			testDeleteDocumentAgainstTextHelper(handler, firstNodeId, secondNodeId, conflictResolution, firstDocId, originalText, devOneFinalText, previousMergeIds);
//		}
//		logger.debug("Finishing testDeleteDocumentAgainstText()");
//	}
//
//	private void testDeleteDocumentAgainstTextHelper(MockMergeHandler handler, String firstNodeId, String secondNodeId, int conflictResolution, 
//			String firstDocId, String originalText, String devOneFinalText, Set<String> previousMergeIds)
//	{
//		handler.handleManualConflictCompletely(conflictResolution);
//
//		if (conflictResolution==1)
//		{
//			for(int i = 0;i<12;i++)	
//			{
//				handler.handleAutomaticConflict();		//11 deletes and 2 text blocks, but one of them was already done
//			}
//		}
//		assertEquals(false, handler.isMerging());
//
//		if (conflictResolution == 2)
//		{
//			handler.setDocumentsToRender(FIRST_DEVELOPER);
//			assertEquals(originalText, handler.getDocumentText(firstDocId));
//			handler.setDocumentsToRender(SECOND_DEVELOPER);
//			assertEquals(false, handler.hasDocument(firstDocId));
//			handler.setDocumentsToRender(COMBINED_STATE);
//			assertEquals(false, handler.hasDocument(firstDocId));
//		}
//		else if (conflictResolution ==1)
//		{
//			handler.setDocumentsToRender(FIRST_DEVELOPER);
//			assertEquals(devOneFinalText, handler.getDocumentText(firstDocId));
//			handler.setDocumentsToRender(SECOND_DEVELOPER);
//			assertEquals(true, handler.hasDocument(firstDocId));
//			handler.setDocumentsToRender(COMBINED_STATE);
//			assertEquals(devOneFinalText, handler.getDocumentText(firstDocId));
//		}
//		else 
//		{
//			handler.setDocumentsToRender(FIRST_DEVELOPER);
//			assertEquals(originalText, handler.getDocumentText(firstDocId));
//			handler.setDocumentsToRender(SECOND_DEVELOPER);
//			assertEquals(true, handler.hasDocument(firstDocId));
//			handler.setDocumentsToRender(COMBINED_STATE);
//			assertEquals(originalText, handler.getDocumentText(firstDocId));
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
//		if (conflictResolution == 2)
//		{
//			assertEquals(false, viewer.hasDocument(firstDocId));
//		}
//		else if (conflictResolution ==1)
//		{
//
//			assertEquals(devOneFinalText, viewer.getDocumentText(firstDocId));
//		}
//		else 
//		{
//			assertEquals(originalText, viewer.getDocumentText(firstDocId));
//		}
//	}
//
//	@Test
//	public void testDeleteDocumentAgainstTextBackwards()	throws Exception	//this is the exact same as testDeleteDocumentAgainstText except node 1 does what node 2 did.  Just to be sure.
//	{
//		logger.debug("Starting testDeleteDocumentAgainstTextBackwards()");
//		final String thisFilePath = testDbFileName+6+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+6+"Project";
//				
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String originalFirstDocName = "a.txt";
//		final String firstDocId = UUID.randomUUID().toString();
//
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//		mockIDE.setCurrentDocumentId(firstDocId);
//		final String originalText = "System.out.println(\"Hello World!\");";
//		mockIDE.sendStringToServer(originalText, 0);
//
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need to branch twice time so that we can have two nodes to merge
//
//		assertEquals(originalFirstDocName, viewer.getAllDocumentNames(rootDirId).get(0));
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		//Here's the branch
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
//		//Dev 2 did some work in that file
//		mockIDE.openProject(thisFilePath, thisProjectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(originalText, mockIDE.getCurrentBuffer());
//		//							 "System.out.println(\"Hello World!\");"
//		mockIDE.sendDeletesToServer ("_____________________XXXXXXXXXXX", 0);
//		mockIDE.insertStringToServer("_____________________Hey there Fox", "_", 0);
//		mockIDE.sendStringToServer("//This is a comment\n", 0);
//		final String devOneFinalText = mockIDE.getCurrentBuffer();
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		//Dev 1 deleted the file
//		mockIDE.openProject(thisFilePath, thisProjectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.sendDeleteDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//
//		//Now we merge
//		//Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//							SecondNode:												FirstNode:
//		//				(umc) * 11	deletes									(umc) delete document
//		//				(umc) add in "Hey there fox"						
//		//				(umc) add "//This is a comment"
//
//		//If the user decides to stick with firstNode: the list would be 
//		//						SecondNode:													FirstNode:
//		//				(rc) * 11	deletes									(rc) delete document
//		//				(uac) add in "Hey there fox"						
//		//				(uac) add "//This is a comment"
//		//		and all the rest of firstNode's events can play out automatically
//
//		//If the user decides to stick with the secondNode and delete the document the list would look like
//		//						SecondNode:													FirstNode:
//		//				(rc) * 11	deletes									(rc) delete document
//		//				(un) add in "Hey there fox"						
//		//				(un) add "//This is a comment"
//		//		and that is all she wrote
//
//		//If the user decides to go with nobody and delete the document the list would also look like
//		//						SecondNode:													FirstNode:
//		//				(rc) * 11	deletes									(rc) delete document
//		//				(un) add in "Hey there fox"						
//		//				(un) add "//This is a comment"
//		//		and that is all she wrote, no more events to run
//
//		//int conflictResolution = 2;
//
//		Set<String> previousMergeIds = new HashSet<String>();
//
//		for (int conflictResolution = 0; conflictResolution < 3; conflictResolution++)
//		{
//			logger.trace("Conflict Resolution: "+conflictResolution);
//			MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//			testDeleteDocumentAgainstTextBackwardsHelper(handler, firstNodeId, secondNodeId, conflictResolution, firstDocId, originalText, devOneFinalText, previousMergeIds);
//		}
//		logger.debug("Finishing testDeleteDocumentAgainstTextBackwards()");
//	}
//
//
//	private void testDeleteDocumentAgainstTextBackwardsHelper(MockMergeHandler handler, String firstNodeId, String secondNodeId,
//			int conflictResolution, String firstDocId, String originalText,
//			String devOneFinalText, Set<String> previousMergeIds)		//exact same except 1s turned to 2s and 2s to 1s
//	{
//		handler.handleManualConflictCompletely(conflictResolution);
//
//		if (conflictResolution==2)
//		{
//			for(int i = 0;i<12;i++)	
//			{
//				handler.handleAutomaticConflict();		//11 deletes and 2 text blocks, but one of them was already done
//			}
//		}
//		assertEquals(false, handler.isMerging());
//
//		if (conflictResolution == 1)
//		{
//			handler.setDocumentsToRender(FIRST_DEVELOPER);
//			assertEquals(false, handler.hasDocument(firstDocId));
//			handler.setDocumentsToRender(SECOND_DEVELOPER);
//			assertEquals(originalText, handler.getDocumentText(firstDocId));
//			handler.setDocumentsToRender(COMBINED_STATE);
//			assertEquals(false, handler.hasDocument(firstDocId));
//		}
//		else if (conflictResolution ==2)
//		{
//			handler.setDocumentsToRender(FIRST_DEVELOPER);
//			assertEquals(true, handler.hasDocument(firstDocId));
//			handler.setDocumentsToRender(SECOND_DEVELOPER);
//			assertEquals(devOneFinalText, handler.getDocumentText(firstDocId));
//			handler.setDocumentsToRender(COMBINED_STATE);
//			assertEquals(devOneFinalText, handler.getDocumentText(firstDocId));
//		}
//		else 
//		{
//			handler.setDocumentsToRender(FIRST_DEVELOPER);
//			assertEquals(true, handler.hasDocument(firstDocId));
//			handler.setDocumentsToRender(SECOND_DEVELOPER);
//			assertEquals(originalText, handler.getDocumentText(firstDocId));			
//			handler.setDocumentsToRender(COMBINED_STATE);
//			assertEquals(originalText, handler.getDocumentText(firstDocId));
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
//		if (conflictResolution == 1)
//		{
//			assertEquals(false, viewer.hasDocument(firstDocId));
//		}
//		else if (conflictResolution ==2)
//		{
//
//			assertEquals(devOneFinalText, viewer.getDocumentText(firstDocId));
//		}
//		else 
//		{
//			assertEquals(originalText, viewer.getDocumentText(firstDocId));
//		}
//
//	}
//
//	@Test
//	public void testBranchingInMiddleOfNodeAndMerging() throws Exception	//This will build some text, then make a branch, then continue working in that node and then merge the two nodes back together
//	{
//		final String thisFilePath = testDbFileName+7+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+7+"Project";
//				
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String originalFirstDocName = "test.java";
//		final String firstDocId = UUID.randomUUID().toString();
//
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//		mockIDE.setCurrentDocumentId(firstDocId);
//		String originalText = "System.out.println(\"Hello World!\");";
//		mockIDE.sendStringToServer(originalText, 0);
//		mockIDE.findAndDeleteToServer("\"Hello World!\"", 1);
//		final String commonAncestorText = mockIDE.getCurrentBuffer();
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need a viewer to branch
//
//		assertEquals(commonAncestorText, viewer.getDocumentText(firstDocId)); //firstDoc is "System.out.println();"
//
//		//We need to find the first node id
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		final String firstNodeId = root.toString();
//		//Here's the branch
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//
//		//Now to find the second node's id
//		tree = viewer.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//
//		final String secondNodeId = root.getChildAt(0).toString();
//
//
//
//		mockIDE.insertStringAfterToServer("\"Strawberries\"", "println(", 1);
//		final String firstNodeFinalText = mockIDE.getCurrentBuffer();
//		mockIDE.closeProject();
//		Thread.sleep(500); 
//
//		//Reopen project to the second (branched) node
//		mockIDE.openProject(thisFilePath, thisProjectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(commonAncestorText, mockIDE.getCurrentBuffer());
//
//		mockIDE.insertStringAfterToServer("\"Blueberries\"", "println(", 1);
//		final String secondNodeFinalText = mockIDE.getCurrentBuffer();
//
//		//Now we can merge
//		//Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//					FirstNode:													SecondNode:
//		//				(umc) "Strawberries"									(umc) "Blueberries"
//
//		Set<String> previousMergeIds = new HashSet<String>();
//
//		for(int devToPick = 0;devToPick<3;devToPick++)
//		{
//			MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//			assertEquals(commonAncestorText, handler.getDocumentText(firstDocId));
//
//			handler.handleManualConflictCompletely(devToPick);
//			assertEquals(false, handler.isMerging());
//
//			handler.setDocumentsToRender(FIRST_DEVELOPER);
//			assertEquals(devToPick==1?firstNodeFinalText:commonAncestorText, handler.getDocumentText(firstDocId));	
//
//			handler.setDocumentsToRender(SECOND_DEVELOPER);
//			assertEquals(devToPick==2?secondNodeFinalText:commonAncestorText, handler.getDocumentText(firstDocId));
//
//			handler.setDocumentsToRender(COMBINED_STATE);
//			if(devToPick==0)
//			{
//				assertEquals(commonAncestorText, handler.getDocumentText(firstDocId));
//			}
//			else if (devToPick==1)
//			{
//				assertEquals(firstNodeFinalText, handler.getDocumentText(firstDocId));
//			}
//			else 
//			{
//				assertEquals(secondNodeFinalText, handler.getDocumentText(firstDocId));
//			}
//
//			//Now to play it back, just to be safe
//			String mergeId = null;
//			tree = handler.getExpectedTreeOfNodes();
//			root = (MutableTreeNode) tree.getRoot();
//			for(int i = 0;i<root.getChildCount();i++)
//			{
//				String childId = root.getChildAt(i).toString();
//				if (!childId.equals(firstNodeId)&&!childId.equals(secondNodeId)&&!previousMergeIds.contains(childId))
//				{
//					mergeId = childId;
//					break;
//				}
//			}
//			if (mergeId == null)
//			{
//				fail("Merge node could not be found");
//			}
//			previousMergeIds.add(mergeId);
//
//			viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeId);
//			if(devToPick==0)
//			{
//				assertEquals(commonAncestorText, viewer.getDocumentText(firstDocId));
//			}
//			else if (devToPick==1)
//			{
//				assertEquals(firstNodeFinalText, viewer.getDocumentText(firstDocId));
//			}
//			else 
//			{
//				assertEquals(secondNodeFinalText, viewer.getDocumentText(firstDocId));
//			}
//
//		}
//
//	}
//
//	@Test
//	public void testMergingWithBothNodesInMiddleOfNode() throws Exception	//This will build some text, then make a branch, continue working in that node, branch again (work some more) and then merge the two branches back together
//	{
//		logger.debug("Starting testMergingWithBothNodesInMiddleOfNode()");
//		final String thisFilePath = testDbFileName+9+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = testDbFileName+9+"Project";
//				
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//
//		final String originalFirstDocName = "test.java";
//		final String firstDocId = UUID.randomUUID().toString();
//
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocId, originalFirstDocName, rootDirId);
//		mockIDE.setCurrentDocumentId(firstDocId);
//		String originalText = "System.out.println(\"Hello World!\");";
//		mockIDE.sendStringToServer(originalText, 0);
//		mockIDE.findAndDeleteToServer("\"Hello World!\"", 1);
//		final String commonAncestorText = mockIDE.getCurrentBuffer();
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		//I need a viewer to branch
//
//		assertEquals(commonAncestorText, viewer.getDocumentText(firstDocId)); //firstDoc is "System.out.println();"
//
//		//We need to find the first node id
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		final String ancestorNodeId = root.toString();
//
//		//Here's the branch
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//
//		//Now to find the first node's id
//		tree = viewer.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//
//		final String firstNodeId = root.getChildAt(0).toString();
//
//
//		mockIDE.insertStringAfterToServer("\"Strawberries\"", "println(", 1);
//
//		final String ancestorTextBeforeSecondBranch = mockIDE.getCurrentBuffer();
//
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//
//		//Now to find the second node's id
//		tree = viewer.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//
//		String secondNodeId = null;
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			if (!root.getChildAt(i).toString().equals(firstNodeId))
//			{
//				secondNodeId=root.getChildAt(i).toString();
//			}
//		}
//		if (secondNodeId==null)
//		{
//			fail("Could not find the secondNodeId");
//		}
//		logger.debug("In testMergingWithBothNodesInMiddleOfNode()");
//		logger.debug("Tree looks like: \n"+PlaybackEventRenderer.treeModelToString(tree));
//		logger.debug("AncestorNodeId = "+ancestorNodeId);
//		logger.debug("FirstNodeId = "+firstNodeId);
//		logger.debug("SecondNodeId = "+secondNodeId);
//
//		mockIDE.insertStringAfterToServer(" and Scotch", "\"Strawberries", 1);			//The ancestor has "Strawberries and Scotch"
//
//		mockIDE.closeProject();
//		Thread.sleep(500); 
//
//		//Reopen project to the first branched node
//		mockIDE.openProject(thisFilePath, thisProjectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(commonAncestorText, mockIDE.getCurrentBuffer());
//
//		mockIDE.insertStringAfterToServer("\"Blueberries\"", "println(", 1);
//		final String firstNodeFinalText = mockIDE.getCurrentBuffer();			//FirstNode looks like println("Blueberries"
//
//		mockIDE.closeProject();
//		Thread.sleep(500); 
//
//		//Reopen project to the second branched node
//		mockIDE.openProject(thisFilePath, thisProjectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(ancestorTextBeforeSecondBranch, mockIDE.getCurrentBuffer());
//
//		mockIDE.insertStringAfterToServer(" and Cream", "\"Strawberries", 1);
//		final String secondNodeFinalText = mockIDE.getCurrentBuffer();				//SecondNode looks like println("Strawberries and Cream")
//
//		//Now we can merge
//		//Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//					FirstNode:													SecondNode:
//		//				(umc) "Blueberries"									(umc) "Strawberries and Cream"
//
//		Set<String> previousMergeIds = new HashSet<String>();
//
//		for(int devToPick = 0;devToPick<3;devToPick++)
//		{
//			MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//			assertEquals(commonAncestorText, handler.getDocumentText(firstDocId));
//
//			handler.handleManualConflictCompletely(devToPick);
//			assertEquals(false, handler.isMerging());
//
//			handler.setDocumentsToRender(FIRST_DEVELOPER);
//			assertEquals(devToPick==1?firstNodeFinalText:commonAncestorText, handler.getDocumentText(firstDocId));	
//
//			handler.setDocumentsToRender(SECOND_DEVELOPER);
//			assertEquals(devToPick==2?secondNodeFinalText:commonAncestorText, handler.getDocumentText(firstDocId));
//
//			handler.setDocumentsToRender(COMBINED_STATE);
//			if(devToPick==0)
//			{
//				assertEquals(commonAncestorText, handler.getDocumentText(firstDocId));
//			}
//			else if (devToPick==1)
//			{
//				assertEquals(firstNodeFinalText, handler.getDocumentText(firstDocId));
//			}
//			else 
//			{
//				assertEquals(secondNodeFinalText, handler.getDocumentText(firstDocId));
//			}
//
//
//			//Now to play it back, just to be safe
//			String mergeId = null;
//			tree = handler.getExpectedTreeOfNodes();
//			root = (MutableTreeNode) tree.getRoot();
//			for(int i = 0;i<root.getChildCount();i++)
//			{
//				String childId = root.getChildAt(i).toString();
//				if (!childId.equals(firstNodeId)&&!childId.equals(secondNodeId)&&!previousMergeIds.contains(childId))
//				{
//					mergeId = childId;
//					break;
//				}
//			}
//			if (mergeId == null)
//			{
//				fail("Merge node could not be found");
//			}
//			previousMergeIds.add(mergeId);
//
//			viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeId);
//			//TODO maybe check to see that ancestorNodeId is still open while both branched nodes are closed (they are as of 8/27/12)
//			if(devToPick==0)
//			{
//				assertEquals(commonAncestorText, viewer.getDocumentText(firstDocId));
//				logger.trace("Should be "+ commonAncestorText+" and it is "+viewer.getDocumentText(firstDocId));
//			}
//			else if (devToPick==1)
//			{
//				assertEquals(firstNodeFinalText, viewer.getDocumentText(firstDocId));
//				logger.trace("Should be "+ firstNodeFinalText+" and it is "+viewer.getDocumentText(firstDocId));
//			}
//			else 
//			{
//				assertEquals(secondNodeFinalText, viewer.getDocumentText(firstDocId));
//				logger.trace("Should be "+ secondNodeFinalText+" and it is "+viewer.getDocumentText(firstDocId));
//			}
//
//		}
//		logger.debug("Finishing testMergingWithBothNodesInMiddleOfNode()");
//	}
//
//}
