//package unitTests;
//
//import static org.junit.Assert.*;
//
//import java.io.File;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//
//import javax.swing.tree.DefaultTreeModel;
//import javax.swing.tree.MutableTreeNode;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import Playback.MockMergeHandler;
//import Playback.MockPlaybackViewer;
//import StorytellerServer.DBFactory;
//import StorytellerServer.SQLiteDBFactory;
//import StorytellerServer.SQLiteDatabase;
//import StorytellerServer.StorytellerServer;
//import StorytellerServer.ide.IDEProxy;
//import StorytellerServer.playback.PlaybackProxy;
//
//
///**
// * This will test the mockIDE being able to merge a branch together and then supply some extra events for conflict resolution
// * 
// * @author Kevin
// *
// */
//public class TestMergingPartFour
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
//	private static String testDbFileName = "unitTestDBForTestMergePartFourTest";
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
//	private static Logger logger = Logger.getLogger(TestMergingPartFour.class.getName());
//	@SuppressWarnings("unused")
//	private static Logger timer = Logger.getLogger("timing."+TestMergingPartFour.class.getName());
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
//		/*
//		MockPlaybackViewer.setDevFirstName("Mark");
//		MockPlaybackViewer.setDevLastName("Mahoney");
//		MockPlaybackViewer.setDevEmailAddress("mmahoney@carthage.edu");
//		MockMergeHandler.setDevFirstName("Mark");
//		MockMergeHandler.setDevLastName("Mahoney");
//		MockMergeHandler.setDevEmailAddress("mmahoney@carthage.edu");
//		*/
//		MockPlaybackViewer.setDevFirstName("Kevin");
//		MockPlaybackViewer.setDevLastName("Lubick");
//		MockPlaybackViewer.setDevEmailAddress("klubick@carthage.edu");
//		MockMergeHandler.setDevFirstName("Kevin");
//		MockMergeHandler.setDevLastName("Lubick");
//		MockMergeHandler.setDevEmailAddress("klubick@carthage.edu");
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
//	/**
//	 * If we ever crash, we need to close the current project so that we can continue, without hanging.
//	 * @throws Exception
//	 */
//	@After
//	public void tearDown() throws Exception
//	{
//		logger.debug("Tearing down test case");
//		mockIDE.closeProject();
//		Thread.sleep(100);
//		logger.debug("Test case torn down");
//	}
//	
//	/*
//	 * This is a great example of how to set up a unit test with different nodes
//	 */
//	@Test
//	public void testSimpleResolution() throws Exception
//	{
//		//First, we are going to make the database file, by appending which test we are in onto the default testDBFileName
//		final String thisFilePath = testDbFileName+1+SQLiteDatabase.DB_EXTENSION_NAME;
//		final String thisProjectName = "testSimpleResolutionProject";
//		
//		File oldDB = new File(thisFilePath);
//		if (oldDB.exists())
//			oldDB.delete();
//		
//		//Make the rootDirId.  I like to make these ids final, just to prove to myself that nothing is getting changed accidentally.
//		final String rootDirId = UUID.randomUUID().toString();
//		mockIDE.createNewProject(thisFilePath, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, thisProjectName);
//		
//		//We need to create a document to write in, and we will create it in the root directory
//		final String firstDocId = UUID.randomUUID().toString();
//		final String firstDocName = "The Fox Always Jumps at Midnight.txt";
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocId, firstDocName, rootDirId);
//		
//		//It is usually a good idea to hold onto the state of the ancestor before you merge.  This allows you to verify that
//		//the MockMergeHandler is in the state that we expect.  I make it final just to idiot-proof the process.
//		final String ancestorString = "The brown fox jumped over the lazy dog";
//		mockIDE.sendStringToServer(ancestorString, 0);
//		
//		//We should commit, to anchor that this is the ancestor node
//		mockIDE.commit("brown fox", "The fox has landed");
//		
//		//To do any sort of splitting, we need to open up a viewer
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackAll(pathToServer);		
//		
//		//We might as well check that the viewer is working, so we'll add in two asserts
//		assertEquals(ancestorString, mockIDE.getCurrentBuffer());	//super sanity check
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(firstDocId));
//
//		//Presently, the mockIDE uses a DefaultTreeModel to model the trees.  Why? because it was
//		//the only way to model a tree using the API stuff.  This may be changed in the future
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//
//		//Here's how we branch.  We only need to branch once because we committed earlier, which gave us a free node.
//		//We are branching after the root, which is the first node of course.
//		viewer.branchAtEndOfNode(root.toString(), mockIDE.getCurrentDevGroupID());
//		
//		//Go find the two nodes by traversing through the tree (since it's a short tree, this is an easy task)
//		tree = viewer.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
//
//		//First child node is the id of "firstNodeId" and the second child node is the id of the "secondNodeId"
//		//We need to hold on to these because we'll be merging them together and referencing them later, thus
//		//it'll be handy to know these.
//		String firstNodeId = root.getChildAt(0).toString();
//		String secondNodeId = root.getChildAt(1).toString();
//
//		//Now we close the project
//		mockIDE.closeProject();
//		//We pause for a brief while, just to make sure the database is fully shut down.
//		//In theory, we don't need this because mockIDE blocks until the IDEProxy gives it the all clear that
//		//the database is shut down, but I include a small pause, just to be safe.
//		Thread.sleep(100);
//		//Current State (I like to have "Current States" every now and again, especially if the changes are going to get
//		//							complicated so that I'll be able to follow the test design later)
//		//Parent Node:
//		//	The Fox Always Jumps at Midnight.txt - "The brown fox jumped over the lazy dog"
//		//First Node:
//		//	The Fox Always Jumps at Midnight.txt - "The brown fox jumped over the lazy dog"
//		//Second Node:
//		//	The Fox Always Jumps at Midnight.txt - "The brown fox jumped over the lazy dog"
//
//		//=====================All the happenings that are in child node one=========================================
//		//If you are creating any files or directories, it's a good idea to define them here, just so they are all in one place
//		//ex. final String secondDocId = UUID.randomUUID().toString();
//		
//		//Opens the project using the     				   firstNodeId  , which should update the mockIDE to the state at that time.
//		mockIDE.openProject(thisFilePath, thisProjectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(ancestorString,mockIDE.getCurrentBuffer());		//I like to check that the mockIDE has been updated to what we expect
//		
//		//Although we do have a mockIDE.insertStringToServer() method, it's most likely easier to just use
//		//mockIDE.insertStringBeforeToServer() or mockIDE.insertStringAfterToServer()
//		mockIDE.insertStringBeforeToServer("quick ", "brown", 1);		//Inserts quick before brown
//		
//		final String nodeOneString = "The quick brown fox jumped over the lazy dog";
//		assertEquals(nodeOneString, mockIDE.getCurrentBuffer());		//a sanity test, optional for the most part
//		
//		//Now we are done with our changes, we should shut down
//		mockIDE.closeProject();
//		Thread.sleep(100);
//		
//		//=====================All the happenings that are in child node two=========================================
//		//If you are creating any files or directories, it's a good idea to define them here, just so they are all in one place
//		//ex. final String firstDirId = UUID.randomUUID().toString();
//
//		//Opens the project using the     				   secondNodeId  , which should update the mockIDE to the state at that time.
//		mockIDE.openProject(thisFilePath, thisProjectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		
//		mockIDE.setCurrentDocumentId(firstDocId);
//		assertEquals(ancestorString, mockIDE.getCurrentBuffer());		//I like to check that the mockIDE has been updated to what we expect
//				
//		//Although we do have a mockIDE.insertStringToServer() method, it's most likely easier to just use
//		//mockIDE.insertStringBeforeToServer() or mockIDE.insertStringAfterToServer()
//		mockIDE.insertStringBeforeToServer("very fast ", "brown", 1);		//Inserts very fast before brown
//		
//		
//		final String nodeTwoString = "The very fast brown fox jumped over the lazy dog";
//		assertEquals(nodeTwoString, mockIDE.getCurrentBuffer());		//a sanity test, optional for the most part
//		
//		//Important, do not shut down the second node.  Or, if you do, reopen the project.  Otherwise,
//		//there will be no database open and serving stuff for the playback viewer to see
//		
//		//Current State
//		//Parent Node:
//		//	The Fox Always Jumps at Midnight.txt - "The brown fox jumped over the lazy dog"
//		//First Node:
//		//	The Fox Always Jumps at Midnight.txt - "The quick brown fox jumped over the lazy dog"
//		//Second Node:
//		//	The Fox Always Jumps at Midnight.txt - "The very fast brown fox jumped over the lazy dog"
//
//		//===============================The merging stuff happens now===============================================
//		/*If you want to test merging in multiple cases where there are many cases, I recommend putting the merge logic (both handling and
//		* asserting) in a helper method and then put the calls to that helper in one or more for loops.  See TestMergingPartTwo and 
//		* TestMergingPartThree for examples (most of them are documented acceptably)
//		* If you are just doing one merge case (as is being demonstrated here), simply handle it in the function as I have done because
//		* setting up a helper with the endless parameters can be a bit "head-desk" inducing.
//		* 
//		* At this point, I like to make a drawing of the merge state
//		* Right now the list of conflict blocks would look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		*					FirstNode:														SecondNode:
//		*				(umc) Type "quick " before brown						(umc) Type "very fast " before brown
//		* 
//		*/
//		
//		//This creates a mockMergeHandler that is primed to begin merging
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictPreppedForCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//		
//		assertEquals(ancestorString, handler.getDocumentText(firstDocId)); //Always make sure you are starting at the right place
//		//more tests to aver that you are at the ancestor state (like checking the names of the documents ...) may be wanted
//		
//		int winningNode = 2;		//the very fast temporarily wins out
//		
//		//We want to create a MockConflictResolutionIDE based on the firstDoc, so we tell the handler that we want to see the buffer of the firstDoc
//		//and we tell the mcrIDE that its documentID is the firstDocId
//		MockConflictResolutionIDE mcrIDE = handler.getMcrIDE();
//		
//		handler.handleManualConflictPartOne(winningNode);
//		
//		//mcrIDE.getMergeStateFromMergeProxy();
//		
//		//Since we said that 2 was our winning node, the mcrIDE should be at the state in which mcrIDE wins
//		assertEquals(nodeTwoString, mcrIDE.getCurrentBuffer());
//		
//		//Manually combining the two states by deleting turning "The very fast brown fox jumped over the lazy dog"
//		//into "The very QUICK brown fox jumped over the lazy dog"
//		mcrIDE.findAndDeleteToServer("fast ", 1);
//		mcrIDE.insertStringBeforeToServer("QUICK ", "brown", 1);
//		//Make a "constant" for our final state.  Run a quick sanity check
//		final String finalMCRIDEState = "The very QUICK brown fox jumped over the lazy dog";
//		assertEquals(finalMCRIDEState, mcrIDE.getCurrentBuffer());
//		
//		//send the "User typed" resolution to this manual conflict to the server, finishing up this manualConflict
//		handler.handleManualConflictPartTwo();
//		
//		//Now we check the fruits of our labor
//		handler.setDocumentsToRender(MockMergeHandler.COMBINED_STATE);
//		assertEquals(finalMCRIDEState, handler.getDocumentText(firstDocId));
//		assertFalse(handler.isMerging());
//		
//		//===============================Final State Checking===========================================
//		//Because Storyteller is all about playback, we should check the playback of whatever we have created
//		
//		//In this case, if you are doing many merges (e.g. you want to check all resolution states (first node wins, second node wins, nobody wins))
//		//I recommend making a HashSet of strings that will keep track of all the ids of the merge nodes that 
//		//we have already checked.  So, if you want to use that, simply move the next line of code up and outside
//		//of the loop (see TestMergingPartTwo for a fuller example)
//		Set<String> previousMergeIds = new HashSet<String>();
//		
//		//This will go through the tree and find a child that is not the first, nor the second node , nor in previousMergeIds
//		String mergeId = null;
//		tree = handler.getExpectedTreeOfNodes();
//		root = (MutableTreeNode) tree.getRoot();
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
//		//Now we do the playback, testing our final state
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeId);
//		
//		logger.debug(viewer.getDocumentText(firstDocId));
//		assertEquals(finalMCRIDEState, viewer.getDocumentText(firstDocId));		
//	}
//}
