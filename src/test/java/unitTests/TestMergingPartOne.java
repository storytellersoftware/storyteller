//package unitTests;
//
//import static Playback.MockMergeHandler.COMBINED_STATE;
//import static Playback.MockMergeHandler.FIRST_DEVELOPER;
//import static Playback.MockMergeHandler.SECOND_DEVELOPER;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.fail;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
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
// * Notes about the testing:
// * -These tests build on each other, which while not "good practice" is more lifelike merging.  Copious documentation about the states of the code has been done
// * so that future testers can easily follow what is going on.
// * 
// * -You see quite a few "sanity checks" on the mockIDE and may be wondering why?  It's simple, the mockIDE, when opening to a node, gets the 
// * state from StorytellerServer, so this is more a check on that part, rather than testing MockIDE again and again.  MockIDE is pretty stable.
// * StorytellerServer may not be after certain changes, so this verifies that any IDEs listening will get what they expect.
// * 
// * 
// * 
// *
// */
//public class TestMergingPartOne
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
//	private static String testDbFileName = "unitTestDBForTestMerge" + SQLiteDatabase.DB_EXTENSION_NAME;
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
//	private static final String defaultDocId = UUID.randomUUID().toString();
//
//	private static final String bothersomeDocId = UUID.randomUUID().toString();		//for conflicts later across multiple tests
//	private static String bothersomeDocName = "InnocuousDoc.txt";				//for conflicts later across multiple tests
//	private static String firstNodeId;
//
//	private static String originalFirstNodeId;		
//
//	private static String secondNodeId;
//
//	private static String originalSecondNodeId;
//
//	private static ArrayList<String> pathToYoungestCommonAncestor = new ArrayList<String>();
//
//	private static ArrayList<String> mergeNodeIds = new ArrayList<String>();
//	
//	private static Logger logger = Logger.getLogger(TestMergingPartOne.class.getName());
//	private static Logger timer = Logger.getLogger("timing."+TestMergingPartOne.class.getName());
//	
//	private static final String projectName = TestMergingPartOne.class.getName()+"Project";
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
//		MockMergeHandler.setDevFirstName("Mark");
//		MockMergeHandler.setDevLastName("Mahoney");
//		MockMergeHandler.setDevEmailAddress("mmahoney@carthage.edu");
//		
//		mockIDE.createNewProject(testDbFileName, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, projectName);
//		
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
//		mockIDE.closeProject();
//
//		Thread.sleep(1000);
//	}
//
//	/**
//	 * A convenience method for getting the proper root of the current common ancestor
//	 * @param fromTree
//	 * @return
//	 */
//	private static MutableTreeNode getYoungestAncestor(DefaultTreeModel fromTree)
//	{
//		MutableTreeNode retVal = (MutableTreeNode) fromTree.getRoot();
//		if (pathToYoungestCommonAncestor.size()==1)
//		{
//			if (retVal.toString().equals(pathToYoungestCommonAncestor.get(0)))
//			{
//				return retVal;
//			}
//			return null;
//		}
//		for(int i = 1;i<pathToYoungestCommonAncestor.size();i++)
//		{
//			String nextKid = pathToYoungestCommonAncestor.get(i);
//			boolean childFound = false;
//			for(int j = 0;j<retVal.getChildCount();j++)
//			{
//				if (retVal.getChildAt(j).toString().equals(nextKid))
//				{
//					retVal = (MutableTreeNode) retVal.getChildAt(j);
//					childFound = true;
//					break;
//				}
//			}
//			if (!childFound)
//			{
//				return null;
//			}
//		}
//		return retVal;
//
//	}
//
//	/**
//	 * Sets the youngest common ancestor of firstNodeId and secondNodeId (the static members of the class) to to be what
//	 * nodeId is.  
//	 * 
//	 * Prerequisite: nodeId must be a child of the current youngestCommonAncestor
//	 * Prerequisite: nodeId must have one open node linking from it (this always happens if nodeId is a merge node)
//	 * 
//	 * Note: afterwards, mockIDE will be logged out, so if you need to use a MockPlaybackViewer, you'll have to log back in with mockIDE again
//	 * 
//	 * Also, firstNodeId and secondNodeId will be updated to be branching off of the youngestCommonAncestor.  They are safe to open and write to.
//	 * 
//	 * @param nodeId
//	 */
//	private static void restablishCommonAncestorAt(String nodeId) throws IOException, JSONException, InterruptedException
//	{
//
//		mockIDE.openProject(testDbFileName, projectName, nodeId , MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		pathToYoungestCommonAncestor.add(nodeId);		//we are adding on the path needed to traverse from the root to the current pathToYoungestCommonAncestor
//
//		MockPlaybackViewer viewer = new MockPlaybackViewer(pathToServer);
//
//		viewer.branchAtEndOfNode(nodeId, mockIDE.getCurrentDevGroupID());	//create a second open node
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//
//		MutableTreeNode root = getYoungestAncestor(tree);	//traverse down the tree using the pathToYoungestCommonAncestor that we just modified
//
//		originalFirstNodeId = root.getChildAt(0).toString();		//this is the open node that was automatically created after the merge
//		firstNodeId = originalFirstNodeId;
//
//		originalSecondNodeId = root.getChildAt(1).toString();		//this is the freshly created node that we made by branching 
//		secondNodeId = originalSecondNodeId;
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//	}
//
//	/**
//	 * A convenience method to walk down the tree of nodes and find the newest mergeNode (assuming that the newest
//	 * merge node is a child of the currentYoungestCommonAncestor) 
//	 * @param sourceOfExpectedTreeOfNodes		Either a MockPlaybackViewer or MockMergeHandler that can be used to get the expectedTreeOfNodes
//	 */
//	private static void updateMergeNodeList(PlaybackEventRenderer sourceOfExpectedTreeOfNodes)
//	{
//		//there should be only one new merge node
//		int previousSizeOfMergeNodeIds = mergeNodeIds.size();
//		//get the tree of current nodes
//		DefaultTreeModel tree = sourceOfExpectedTreeOfNodes.getExpectedTreeOfNodes();
//		//traverse the tree to the youngestCommonAncestor
//		MutableTreeNode root = getYoungestAncestor(tree);
//
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			//if the youngestCommonAncestor has a new "growth" that is not the firstNode, nor the secondNode and isn't already in the list of mergeNodeIds, then add it to the list
//			if (!(root.getChildAt(i).toString().equals(originalFirstNodeId)) && !(root.getChildAt(i).toString().equals(originalSecondNodeId)) && !(mergeNodeIds.contains(root.getChildAt(i).toString())))
//			{
//				mergeNodeIds.add(root.getChildAt(i).toString());		
//			}
//		}
//		//there should be only one new merge node.  Assert that that is the case
//		if (mergeNodeIds.size()!=(previousSizeOfMergeNodeIds+1))
//		{
//			fail("Unable to find the id of the mergeNode");
//		}
//
//
//	}
//
//	@Test 
//	public void testAllMergesInOrder() throws Exception {
//		firstTestSimpleAutomaticConflicts();
//		tearDown();
//		logger.info("Done with first test.");
//		secondTestSimpleManualConflict();
//		logger.info("Done with second test.");
//		tearDown();
//		thirdTestAutoManualConflict();
//		logger.info("Done with third test.");
//		tearDown();
//		fourthTestConflictsWithDeletes();
//		logger.info("Done with fourth test.");
//		tearDown();
//		fifthTestAutoConflictCreateDocuments();
//		logger.info("Done with fifth test.");
//		tearDown();
//		sixthTestManualConflictCreateDocuments();
//		logger.info("Done with sixth test.");
//		tearDown();
//		seventhTestManualConflictCreateRenameDocuments();
//		logger.info("Done with seventh test.");
//		tearDown();
//		eighthTestManualConflictCreateMoveDocument();
//		logger.info("Done with eighth test.");
//		tearDown();
//		ninthTestManualConflictCreateRenameDocument();
//		logger.info("Done with ninth test.");
//	}
//
//	public void firstTestSimpleAutomaticConflicts() throws Exception			//tests an automatic vs an automatic
//	{
//		//name of a new document
//		String newDocName = "Merging.java";
//
//		//create a new document event in the root dir and send it to the server
//		mockIDE.sendCreateNewDocumentEventToServer(defaultDocId, newDocName, rootDirId);
//
//		mockIDE.sendStringToServer("AM",0);
//
//		MockPlaybackViewer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		String idOfElementToBranchAt = playbackViewer.getIdOfEvent(defaultDocId,1);			//we branch after the M, which is at index 1
//
//		mockIDE.commit("Woo!", "Basic outline done");
//
//		mockIDE.sendStringToServer("BCDEFGHIJKL",1);			//part 1 of the alphabet
//
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//
//		DefaultTreeModel tree = playbackViewer.getRenderedTreeOfNodes();
//		pathToYoungestCommonAncestor.add(tree.getRoot().toString());		//save where the root is
//		MutableTreeNode root = getYoungestAncestor(tree);
//
//		firstNodeId = root.getChildAt(0).toString();
//		originalFirstNodeId = firstNodeId;
//
//		//branch for second node
//		playbackViewer.branchAtId(idOfElementToBranchAt, mockIDE.getCurrentDevGroupID());		
//
//		root = getYoungestAncestor(playbackViewer.getExpectedTreeOfNodes());
//
//		secondNodeId = null;
//		//parse through the tree to find the newly branched fork.  That is "secondNode"
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			if (!(root.getChildAt(i).toString().equals(firstNodeId)))
//			{
//				secondNodeId=root.getChildAt(i).toString();
//				originalSecondNodeId = secondNodeId;
//			}
//		}
//
//		if (secondNodeId==null)
//		{
//			fail("Unable to find the id of the secondNode");
//		}
//
//
//		mockIDE.closeProject();
//
//		Thread.sleep(500);				//just wait a second... hold.... hold.......... GO!
//
//		mockIDE.openProject(testDbFileName, projectName, secondNodeId , MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//
//		assertEquals("AM", mockIDE.getCurrentBuffer());		//sanity check to make sure we are at the stage we want to be
//
//		mockIDE.insertStringAfterToServer("nopqrstuvwxyz", "M", 1);
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * AM
//		 * 
//		 * FirstNode:
//		 * ABCDEFGHIJKLM
//		 * 
//		 * SecondNode:
//		 * AMnopqrstuvwxyz
//		 */
//		//Now we merge
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//
//		assertEquals("AM", handler.getDocumentText(defaultDocId));
//
//
//		handler.handleAutomaticConflict();			//handle the first automatic conflict (branch 1 adding BCDEFGHIJKLM)
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("AM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.handleAutomaticConflict();			//handle the second automatic conflict  (branch 2 adding nopqrstuvwxyz)
//
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("AMnopqrstuvwxyz",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("ABCDEFGHIJKLMnopqrstuvwxyz",handler.getDocumentText(defaultDocId));
//
//		//now watch the playback and hope all goes well
//
//		updateMergeNodeList(handler);  		//now that the merge is complete, add it to the list
//
//		PlaybackEventRenderer viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(0));
//
//		assertEquals(handler.getDocumentText(defaultDocId), viewer.getDocumentText(defaultDocId));
//
//	}
//
//	/*
//	 * At this point, the state is as follows
//	 * 
//	 * Common Ancestor between FirstNode and SecondNode:
//	 * AM
//	 * 
//	 * FirstNode:
//	 * ABCDEFGHIJKLM
//	 * 
//	 * SecondNode:
//	 * AMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(0):
//	 * ABCDEFGHIJKLMnopqrstuvwxyz
//	 */
//	
//	public void secondTestSimpleManualConflict() throws Exception			//Tests {automatic, manual} vs {manual}
//	{	
//		//first I want to expand what is in firstNode.  To do that, I'll need to create a new, open node from the end of firstNode, then write in that node
//
//		//so that playback works, I need to open the project
//		mockIDE.openProject(testDbFileName, projectName, null , MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("AMnopqrstuvwxyz", mockIDE.getCurrentBuffer());	//sanity check to make sure we are at the stage we want to be
//
//		//we need to keep writing after this node, so we'll branch
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackToNode(pathToServer, firstNodeId);
//
//		viewer.branchAtEndOfNode(firstNodeId, mockIDE.getCurrentDevGroupID());		//branch after firstNodeId to make a new open node
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = getYoungestAncestor(tree);
//
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			if ((root.getChildAt(i).toString().equals(originalFirstNodeId)))
//			{
//				firstNodeId = root.getChildAt(i).getChildAt(0).toString();			//update the firstNodeId to be the newly opened node (because we can't write in the originalFirstNodeId; it's closed)
//			}
//		}
//		mockIDE.closeProject();
//		Thread.sleep(500);		//wait before reopening...
//
//		mockIDE.openProject(testDbFileName, projectName, firstNodeId , MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("ABCDEFGHIJKLM", mockIDE.getCurrentBuffer());	//sanity check to make sure we are at the stage we want to be
//		mockIDE.sendStringToServer("NOPQRSTUVWXYZ!", mockIDE.getCurrentLength());
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, firstNodeId);
//
//		assertEquals(mockIDE.getCurrentBuffer(),viewer.getDocumentText(defaultDocId));		//sanity check the playback
//
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * AM
//		 * 
//		 * FirstNode:
//		 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * SecondNode:
//		 * AMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(0):
//		 * ABCDEFGHIJKLMnopqrstuvwxyz
//		 */
//		//Now merge
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//
//		assertEquals("AM", handler.getDocumentText(defaultDocId));	//sanity check to make sure we are at the stage we want to be
//
//
//		handler.handleAutomaticConflict();			//handle the first automatic conflict (which is first developer adding BCDEFGHIJKL after A)
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("AM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.handleManualConflictCompletely(1);		//keep the changes from the first node
//
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("AM", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		//Now check the merge node playback and assert that it is what we expect
//		updateMergeNodeList(viewer);			//now that the merge is complete, add it to the list
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(1));
//
//		assertEquals(handler.getDocumentText(defaultDocId), viewer.getDocumentText(defaultDocId));
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * AM
//		 * 
//		 * FirstNode:
//		 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * SecondNode:
//		 * AMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(0):
//		 * ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(1):
//		 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 */
//
//		//Now let's try merging it the next way (where we use the other dev's code
//
//		handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//
//		assertEquals("AM", handler.getDocumentText(defaultDocId));
//
//
//		handler.handleAutomaticConflict();			//handle the first automatic conflict (which is first developer adding BCDEFGHIJKL after A)
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("AM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.handleManualConflictCompletely(2);		//keep dev 2's changes 
//
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLM", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("AMnopqrstuvwxyz", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("ABCDEFGHIJKLMnopqrstuvwxyz", handler.getDocumentText(defaultDocId));
//
//
//		//Now check the merge node playback and assert that it is what we expect
//		updateMergeNodeList(viewer);    	//now that the merge is complete, add it to the list
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(2));
//
//		assertEquals(handler.getDocumentText(defaultDocId), viewer.getDocumentText(defaultDocId));
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * AM
//		 * 
//		 * FirstNode:
//		 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * SecondNode:
//		 * AMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(0):
//		 * ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(1):
//		 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(2):
//		 * ABCDEFGHIJKLMnopqrstuvwxyz
//		 */
//
//		//now let's try merging but we select neither node to use
//
//		handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//
//		assertEquals("AM", handler.getDocumentText(defaultDocId));
//
//
//		handler.handleAutomaticConflict();			//handle the first automatic conflict (which is first developer adding BCDEFGHIJKL after A)
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("AM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.handleManualConflictCompletely(0);		//keep nobody's changes
//
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLM", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("AM", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("ABCDEFGHIJKLM", handler.getDocumentText(defaultDocId));
//
//
//		//Now check the merge node playback and assert that it is what we expect
//		updateMergeNodeList(viewer);   		//now that the merge is complete, add it to the list
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(3));
//
//		assertEquals(handler.getDocumentText(defaultDocId), viewer.getDocumentText(defaultDocId));
//
//
//	}
//
//	/*
//	 * At this point, the state is as follows
//	 * 
//	 * Common Ancestor between FirstNode and SecondNode:
//	 * AM
//	 * 
//	 * FirstNode:
//	 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 * 
//	 * SecondNode:
//	 * AMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(0):
//	 * ABCDEFGHIJKLMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(1):
//	 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 * 
//	 * mergeNodeIds.get(2):
//	 * ABCDEFGHIJKLMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(3):
//	 * ABCDEFGHIJKLM
//	 */
//
//
//	
//	public void thirdTestAutoManualConflict() throws Exception  //Tests {automatic, manual} vs {manual, automatic}
//	{
//		//first I want to expand what is in secondNode.  To do that, I'll need to create a new, open node from the end of secondNode, then write in that node
//		mockIDE.openProject(testDbFileName, projectName, mergeNodeIds.get(3) ,MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//
//		//we need to keep writing after this node, so we'll branch
//		MockPlaybackViewer viewer = MockPlaybackViewer.playBackToNode(pathToServer, secondNodeId);
//
//		viewer.branchAtEndOfNode(secondNodeId, mockIDE.getCurrentDevGroupID());		//branch after firstNodeId to make a new open node
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		MutableTreeNode root = getYoungestAncestor(tree);
//
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			if ((root.getChildAt(i).toString().equals(originalSecondNodeId)))
//			{
//				secondNodeId = root.getChildAt(i).getChildAt(0).toString();			//update the firstNodeId to be the newly opened node (because we can't write in the originalFirstNodeId; it's closed)
//			}
//		}
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//
//		mockIDE.openProject(testDbFileName, projectName, secondNodeId , MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("AMnopqrstuvwxyz", mockIDE.getCurrentBuffer());	//sanity check to make sure we are at the stage we want to be
//
//		mockIDE.sendStringToServer("This is the alphabet: ", 0);
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, secondNodeId);
//
//		assertEquals(mockIDE.getCurrentBuffer(),viewer.getDocumentText(defaultDocId));		//sanity check the playback
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * AM
//		 * 
//		 * FirstNode:
//		 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * SecondNode:
//		 * This is the alphabet: AMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(0):
//		 * ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(1):
//		 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(2):
//		 * ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(3):
//		 * ABCDEFGHIJKLM
//		 */
//
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//
//		assertEquals("AM", handler.getDocumentText(defaultDocId));
//
//
//		handler.handleAutomaticConflict();			//handle the first automatic conflict (which is first developer adding BCDEFGHIJKL after A)
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("AM",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("ABCDEFGHIJKLM",handler.getDocumentText(defaultDocId));
//
//		handler.handleManualConflictCompletely(1);		//This is the conflict between NOPQRSTUVWXYZ! and nopqrstuvwxyz, keep dev 1's changes 
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("AM", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.handleAutomaticConflict();			//handle the second automatic conflict (which is second developer adding "This is the alphabet: " before A)
//
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ!",handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the alphabet: AM", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!",handler.getDocumentText(defaultDocId));
//
//
//		//Now check the merge node playback and assert that it is what we expect
//		tree = viewer.getExpectedTreeOfNodes();
//		root = getYoungestAncestor(tree);
//
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			if (!(root.getChildAt(i).toString().equals(originalFirstNodeId)) && !(root.getChildAt(i).toString().equals(originalSecondNodeId)) && !(mergeNodeIds.contains(root.getChildAt(i).toString())))
//			{
//				mergeNodeIds.add(root.getChildAt(i).toString());		
//			}
//		}
//
//		if (mergeNodeIds.size()!=5)
//		{
//			fail("Unable to find the id of the mergeNode");
//		}
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(4));
//
//		assertEquals(handler.getDocumentText(defaultDocId), viewer.getDocumentText(defaultDocId));
//
//	}
//
//	/*
//	 * At this point, the state is as follows
//	 * 
//	 * Common Ancestor between FirstNode and SecondNode:
//	 * AM
//	 * 
//	 * FirstNode:
//	 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 * 
//	 * SecondNode:
//	 * This is the alphabet: AMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(0):
//	 * ABCDEFGHIJKLMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(1):
//	 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 * 
//	 * mergeNodeIds.get(2):
//	 * ABCDEFGHIJKLMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(3):
//	 * ABCDEFGHIJKLM
//	 * 
//	 * mergeNodeIds.get(4):
//	 * This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 */
//	
//	public void fourthTestConflictsWithDeletes() throws Exception
//	{
//		//first I want to add another branch off of mergeNodeIds.get(4) so that we have more to work with
//		restablishCommonAncestorAt(mergeNodeIds.get(4));
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * FirstNode:
//		 * This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * SecondNode:
//		 * This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(0):
//		 * ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(1):
//		 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(2):
//		 * ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(3):
//		 * ABCDEFGHIJKLM
//		 * 
//		 * mergeNodeIds.get(4):
//		 * This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 */
//
//		mockIDE.openProject(testDbFileName, projectName, firstNodeId , MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);	
//		assertEquals("This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!", mockIDE.getCurrentBuffer());	//sanity check to make sure we are at the stage we want to be
//
//		mockIDE.findAndDeleteToServer("ABCDEFGHIJKLMN", 1);
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//
//		mockIDE.openProject(testDbFileName, projectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!", mockIDE.getCurrentBuffer());	//sanity check to make sure we are at the stage we want to be
//
//
//		mockIDE.insertStringAfterToServer("lpha", "A", 1);
//		mockIDE.insertStringAfterToServer("eta", "B", 1);
//		mockIDE.insertStringBeforeToServer("Greekish ", "alphabet", 1);
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * FirstNode:
//		 * This is the alphabet: OPQRSTUVWXYZ!
//		 * 
//		 * SecondNode:
//		 * This is the Greekish alphabet: AlphaBetaCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(0):
//		 * ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(1):
//		 * ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(2):
//		 * ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(3):
//		 * ABCDEFGHIJKLM
//		 * 
//		 * mergeNodeIds.get(4):
//		 * This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 */
//
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//
//		assertEquals("This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		for(int i = 0;i<12;i++)		//there are the 12 deletions of text NMLKJIGHFEDC before we start with manuals
//		{
//			handler.handleAutomaticConflict();
//			assertEquals(true, handler.isMerging());
//		}
//		//check that the things were deleted okay before we handle b
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the alphabet: ABOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the alphabet: ABOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//
//		handler.handleManualConflictCompletely(2);		//This is the deletion of the B vs inserting "eta"; eta wins
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the alphabet: ABOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the alphabet: ABetaCDEFGHIJKLMNOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the alphabet: ABetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.handleManualConflictCompletely(1);		//This is the deletion of the A vs inserting "lpha"; Deleting wins
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the alphabet: BOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the alphabet: ABetaCDEFGHIJKLMNOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the alphabet: BetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//
//
//		handler.handleAutomaticConflict();
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the alphabet: BOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: ABetaCDEFGHIJKLMNOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: BetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//
//
//		//Now check the merge node playback and assert that it is what we expect
//		PlaybackEventRenderer viewer = new MockPlaybackViewer(pathToServer);
//		updateMergeNodeList(viewer);   //now that the merge is complete, add it to the list
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(5));
//
//		assertEquals(handler.getDocumentText(defaultDocId), viewer.getDocumentText(defaultDocId));
//
//	}
//
//	/*
//	 * At this point, the state is as follows
//	 * 
//	 * Common Ancestor between FirstNode and SecondNode:
//	 * Merging.java - This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 * 
//	 * FirstNode:
//	 * Merging.java - This is the alphabet: OPQRSTUVWXYZ!
//	 * 
//	 * SecondNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 * 
//	 * mergeNodeIds.get(0):
//	 * Merging.java - ABCDEFGHIJKLMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(1):
//	 * Merging.java - ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 * 
//	 * mergeNodeIds.get(2):
//	 * Merging.java - ABCDEFGHIJKLMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(3):
//	 * Merging.java - ABCDEFGHIJKLM
//	 * 
//	 * mergeNodeIds.get(4):
//	 * Merging.java - This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 * 
//	 * mergeNodeIds.get(5):
//	 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//	 * 
//	 */
//
//
//	 
//	public void fifthTestAutoConflictCreateDocuments() throws Exception
//	{
//		//before doing anything, I need to reestablish the common ancestor at mergeNodesIds.get(5)
//		restablishCommonAncestorAt(mergeNodeIds.get(5));
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * 
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(0):
//		 * Merging.java - ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(1):
//		 * Merging.java - ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(2):
//		 * Merging.java - ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(3):
//		 * Merging.java - ABCDEFGHIJKLM
//		 * 
//		 * mergeNodeIds.get(4):
//		 * Merging.java - This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(5):
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * 
//		 */
//
//
//		mockIDE.openProject(testDbFileName, projectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("This is the Greekish alphabet: BetaOPQRSTUVWXYZ!", mockIDE.getCurrentBuffer());		//Another sanity check
//
//		String tempDocID = UUID.randomUUID().toString();
//		mockIDE.sendCreateNewDocumentEventToServer(tempDocID, "WooDocument.xlsx", rootDirId);
//		mockIDE.setCurrentDocumentId(tempDocID);
//		mockIDE.sendStringToServer("Just some text", 0);
//		mockIDE.insertStringAfterToServer(" amazing", "some", 1);
//		mockIDE.findAndDeleteToServer("some ", 1);
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * 
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(0):
//		 * Merging.java - ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(1):
//		 * Merging.java - ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(2):
//		 * Merging.java - ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(3):
//		 * Merging.java - ABCDEFGHIJKLM
//		 * 
//		 * mergeNodeIds.get(4):
//		 * Merging.java - This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(5):
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * 
//		 */
//
//		mockIDE.openProject(testDbFileName, projectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("This is the Greekish alphabet: BetaOPQRSTUVWXYZ!", mockIDE.getCurrentBuffer());		//Another sanity check
//
//		mockIDE.insertStringBeforeToServer("Alpha", "Beta", 1);
//		String secondTempDocID = UUID.randomUUID().toString();
//		mockIDE.sendCreateNewDocumentEventToServer(secondTempDocID, "NonConflictingName.cpp", rootDirId);
//		mockIDE.setCurrentDocumentId(secondTempDocID);
//		mockIDE.sendStringToServer("All sorts of writing!!!!", 0);
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * 
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * 
//		 * mergeNodeIds.get(0):
//		 * Merging.java - ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(1):
//		 * Merging.java - ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(2):
//		 * Merging.java - ABCDEFGHIJKLMnopqrstuvwxyz
//		 * 
//		 * mergeNodeIds.get(3):
//		 * Merging.java - ABCDEFGHIJKLM
//		 * 
//		 * mergeNodeIds.get(4):
//		 * Merging.java - This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//		 * 
//		 * mergeNodeIds.get(5):
//		 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//		 * 
//		 */
//
//		MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//		assertEquals("This is the Greekish alphabet: BetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//
//		handler.handleAutomaticConflict();		//this should be dev 1 adding his document and all of the typing
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: BetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals("Just amazing text", handler.getDocumentText(tempDocID));
//		assertEquals(false, handler.hasDocument(secondTempDocID));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: BetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals(false, handler.hasDocument(secondTempDocID));
//		assertEquals(false, handler.hasDocument(tempDocID));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: BetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals("Just amazing text", handler.getDocumentText(tempDocID));
//		assertEquals(false, handler.hasDocument(secondTempDocID));
//
//		handler.handleAutomaticConflict();		//this should be dev 2 modifying the defaultDocId
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: BetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals("Just amazing text", handler.getDocumentText(tempDocID));
//		assertEquals(false, handler.hasDocument(secondTempDocID));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals(false, handler.hasDocument(secondTempDocID));
//		assertEquals(false, handler.hasDocument(tempDocID));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals("Just amazing text", handler.getDocumentText(tempDocID));
//		assertEquals(false, handler.hasDocument(secondTempDocID));
//
//		handler.handleAutomaticConflict();		//this should be dev 2 adding her document
//
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: BetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals("Just amazing text", handler.getDocumentText(tempDocID));
//		assertEquals(false, handler.hasDocument(secondTempDocID));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals("All sorts of writing!!!!", handler.getDocumentText((secondTempDocID)));
//		assertEquals(false, handler.hasDocument(tempDocID));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals("All sorts of writing!!!!", handler.getDocumentText((secondTempDocID)));
//		assertEquals("Just amazing text", handler.getDocumentText(tempDocID));
//
//		PlaybackEventRenderer viewer = new MockPlaybackViewer(pathToServer);
//		updateMergeNodeList(viewer);			//now that the merge is complete, add it to the list
//
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(6));
//
//
//		assertEquals(handler.getDocumentText(defaultDocId), viewer.getDocumentText(defaultDocId));
//		assertEquals(handler.getDocumentText(secondTempDocID), viewer.getDocumentText(secondTempDocID));
//		assertEquals(handler.getDocumentText(tempDocID), viewer.getDocumentText(tempDocID));
//
//	}
//
//	/*
//	 * At this point, the state is as follows
//	 * 
//	 * Common Ancestor between FirstNode and SecondNode:
//	 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//	 * 
//	 * FirstNode:
//	 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//	 * WooDocument.xlsx - Just amazing text
//	 * 
//	 * SecondNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * 
//	 * mergeNodeIds.get(0):
//	 * Merging.java - ABCDEFGHIJKLMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(1):
//	 * Merging.java - ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 * 
//	 * mergeNodeIds.get(2):
//	 * Merging.java - ABCDEFGHIJKLMnopqrstuvwxyz
//	 * 
//	 * mergeNodeIds.get(3):
//	 * Merging.java - ABCDEFGHIJKLM
//	 * 
//	 * mergeNodeIds.get(4):
//	 * Merging.java - This is the alphabet: ABCDEFGHIJKLMNOPQRSTUVWXYZ!
//	 * 
//	 * mergeNodeIds.get(5):
//	 * Merging.java - This is the Greekish alphabet: BetaOPQRSTUVWXYZ!
//	 * 
//	 * mergeNodeIds.get(6):
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * 
//	 */
//
//	/**
//	 * Helper for the sixthTestManualConflictCreateDocuments.  Allows all 3 combinations of who wins the manual conflict to be tested easily
//	 * @param handler
//	 * @param whoShouldWin
//	 * @param firstTempDocID
//	 * @param firstText
//	 * @param secondTempDocID
//	 * @param secondText
//	 */
//	private void sixthMergeHelper(MockMergeHandler handler, int whoShouldWin, String firstTempDocID, String firstText, String secondTempDocID, String secondText)
//	{
//		//Right now the list of conflict blocks should look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//							FirstNode:													SecondNode:
//		//				(umc)create (and type in) PoppyMeadow.txt					(uac)Type (and then fix) "GammaDelta" in defaultDoc
//		//				(uac)Type "Omega" 	in defaultDoc							(umc)create (and type in) PoppyMeadow.txt
//		//				(uac)delete "!" in defaultDoc
//
//		//That means that the first thing that should be resolved should be the first uac in SecondNode:
//
//		handler.handleAutomaticConflict();
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals(false, handler.hasDocument(firstTempDocID));
//		assertEquals(false, handler.hasDocument(secondTempDocID));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals(false, handler.hasDocument(firstTempDocID));
//		assertEquals(false, handler.hasDocument(secondTempDocID));
//
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		assertEquals(false, handler.hasDocument(firstTempDocID));
//		assertEquals(false, handler.hasDocument(secondTempDocID));
//
//		//The next thing should be the manual conflict at the creation of PoppyMeadow
//		handler.handleManualConflictCompletely(whoShouldWin);		//this is the developer that won
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		if (whoShouldWin == 1)
//		{
//			assertEquals(firstText, handler.getDocumentText(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//		else 
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		if (whoShouldWin == 2)
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(secondText, handler.getDocumentText(secondTempDocID));
//		}
//		else 
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}		
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		if (whoShouldWin == 1)
//		{
//			assertEquals(firstText, handler.getDocumentText(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//		else if (whoShouldWin == 2)
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(secondText, handler.getDocumentText(secondTempDocID));
//		}
//		else 
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}		
//
//		//next we are at the addition of omega
//		handler.handleAutomaticConflict();
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZOmega!", handler.getDocumentText(defaultDocId));
//		if (whoShouldWin == 1)
//		{
//			assertEquals(firstText, handler.getDocumentText(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//		else 
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		if (whoShouldWin == 2)
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(secondText, handler.getDocumentText(secondTempDocID));
//		}
//		else 
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}	
//
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZOmega!", handler.getDocumentText(defaultDocId));
//		if (whoShouldWin == 1)
//		{
//			assertEquals(firstText, handler.getDocumentText(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//		else if (whoShouldWin == 2)
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(secondText, handler.getDocumentText(secondTempDocID));
//		}
//		else 
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//
//		//Finally, we are at the deletion of !
//
//		handler.handleAutomaticConflict();
//
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZOmega", handler.getDocumentText(defaultDocId));
//		if (whoShouldWin == 1)
//		{
//			assertEquals(firstText, handler.getDocumentText(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//		else 
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//		if (whoShouldWin == 2)
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(secondText, handler.getDocumentText(secondTempDocID));
//		}
//		else 
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}	
//
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZOmega", handler.getDocumentText(defaultDocId));
//		if (whoShouldWin == 1)
//		{
//			assertEquals(firstText, handler.getDocumentText(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//		else if (whoShouldWin == 2)
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(secondText, handler.getDocumentText(secondTempDocID));
//		}
//		else 
//		{
//			assertEquals(false, handler.hasDocument(firstTempDocID));
//			assertEquals(false, handler.hasDocument(secondTempDocID));
//		}
//
//		PlaybackEventRenderer viewer = new MockPlaybackViewer(pathToServer);
//
//		updateMergeNodeList(viewer);			//now that the merge is complete, add it to the list
//
//		timer.trace("Beginning playBackToNode");
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(mergeNodeIds.size()-1));
//		timer.trace("Finished playBackToNode");
//
//		assertEquals(handler.getDocumentText(defaultDocId), viewer.getDocumentText(defaultDocId));
//		if (whoShouldWin == 1)
//		{
//			assertEquals(handler.getDocumentText(firstTempDocID), viewer.getDocumentText(firstTempDocID));
//		}
//
//		if (whoShouldWin == 2)
//		{
//			assertEquals(handler.getDocumentText(secondTempDocID), viewer.getDocumentText(secondTempDocID));
//		}
//
//
//	}
//
//	
//	public void sixthTestManualConflictCreateDocuments() throws Exception
//	{
//		//before doing anything, I need to reestablish the common ancestor at mergeNodesIds.get(6)
//		restablishCommonAncestorAt(mergeNodeIds.get(6));
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 *  
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * 
//		 * ...
//		 *
//		 * mergeNodeIds.get(6):
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * 
//		 */
//
//		mockIDE.openProject(testDbFileName, projectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", mockIDE.getCurrentBuffer());		//Another sanity check
//
//
//		String firstTempDocID = UUID.randomUUID().toString();
//		final String conflictingName = "PoppyMeadow.txt";
//		mockIDE.sendCreateNewDocumentEventToServer(firstTempDocID, conflictingName, rootDirId);
//
//		mockIDE.setCurrentDocumentId(firstTempDocID);
//		//featured article on 8/1/2012
//		String firstText = "Poppy Meadow is a fictional character from the BBC soap opera EastEnders, played by Rachel Bright. " +
//				"She was introduced by executive producer Bryan Kirkwood on 11 January 2011 as the best friend of established character " +
//				"Jodie Gold (Kylie Babbington) in scenes filling in for those cut from a controversial baby-swap storyline. ";
//		mockIDE.sendStringToServer(firstText, 0);
//
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		mockIDE.insertStringBeforeToServer("Omega", "!", 1);
//		mockIDE.findAndDeleteToServer("!", 1);
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 *  
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZOmega
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * PoppyMeadow.txt - Poppy Meadow is a fictional character [...] baby-swap storyline. 
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * 
//		 * ...
//		 *
//		 * mergeNodeIds.get(6):
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * 
//		 */
//
//		mockIDE.openProject(testDbFileName, projectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", mockIDE.getCurrentBuffer());		//Another sanity check
//
//		//The typo should test blocking deletes in with text.  Ideally, all 3 of these things, typing, deletes, typing should be one block.  
//		mockIDE.insertStringAfterToServer("GammaDeta", "Beta", 1);
//		mockIDE.findAndDeleteToServer("ta", 2);		//the 2nd one is in Deta (sic)
//		mockIDE.insertStringAfterToServer("lta", "GammaDe", 1);
//
//		String secondTempDocID = UUID.randomUUID().toString();
//		mockIDE.sendCreateNewDocumentEventToServer(secondTempDocID, conflictingName, rootDirId);
//
//		mockIDE.setCurrentDocumentId(secondTempDocID);
//		//featured article on 8/1/2012
//		String secondText = "Poppy returned to the series in June 2011 as a supporting character and comedy element, " +
//				"in a move that was generally welcomed by the tabloid press; her storylines focused on her friendship with " +
//				"Jodie and their intertwined love lives.";
//		mockIDE.sendStringToServer(secondText, 0);
//
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 *  
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZOmega
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * PoppyMeadow.txt - Poppy Meadow is a fictional character [...] baby-swap storyline. 
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * PoppyMeadow.txt - Poppy returned to the series [...] intertwined love lives.
//		 * 
//		 * ...
//		 *
//		 * mergeNodeIds.get(6):
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 */
//
//
//
//		for(int i =0;i<3;i++)		//tests all 3 cases of this 
//		{
//			MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//			assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//			sixthMergeHelper(handler, i, firstTempDocID, firstText, secondTempDocID, secondText);		//go see this method for all the assertions and handling of conflicts
//		}
//
//	}
//
//	/*
//	 * At this point, the state is as follows
//	 * 
//	 * Common Ancestor between FirstNode and SecondNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 *  
//	 * FirstNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZOmega
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * PoppyMeadow.txt - Poppy Meadow is a fictional character [...] baby-swap storyline. 
//	 * 
//	 * SecondNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZ!
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * PoppyMeadow.txt - Poppy returned to the series [...] intertwined love lives.
//	 * 
//	 * ...
//	 *
//	 * mergeNodeIds.get(6):
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * 
//	 * mergeNodeIds.get(7):
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZOmega
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * 
//	 * mergeNodeIds.get(8):
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZOmega
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * PoppyMeadow.txt - Poppy Meadow is a fictional character [...] baby-swap storyline.
//	 * 
//	 * mergeNodeIds.get(9):
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDeltaOPQRSTUVWXYZOmega
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * PoppyMeadow.txt - Poppy returned to the series [...] intertwined love lives.
//	 */
//
//	private void seventhTestHelper(MockMergeHandler handler, int whoWon, String bothersomeDocId, String firstDocID)
//	{	//Right now the list of conflict blocks should look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//							FirstNode:													SecondNode:
//		//				(uac * 13)Delete OPQRSTUVWXYZ! from defaultDoc					(umc) rename InnocuousDoc.txt to Greekish.txt
//		//				(umc)create Greekish.txt and type in it							(uac) type in InnocuousDoc.txt/Greekish.txt		
//
//		//this is the deleting of defaultDoc O-Z!
//		String defaultDocText = "This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!";
//		for(int i =1;i<=13;i++)		//deletes get chunked by themselves.  We deleted 13 things, thus repeat 13 times
//		{
//			handler.handleAutomaticConflict();
//
//			assertEquals(true, handler.isMerging());
//
//			handler.setDocumentsToRender(FIRST_DEVELOPER);
//			assertEquals(defaultDocText.substring(0, defaultDocText.length()-i), handler.getDocumentText(defaultDocId));
//			assertEquals("Just some text", handler.getDocumentText(bothersomeDocId));
//			assertEquals(false, handler.hasDocument(firstDocID));
//
//			handler.setDocumentsToRender(SECOND_DEVELOPER);
//			assertEquals(defaultDocText, handler.getDocumentText(defaultDocId));
//			assertEquals("Just some text", handler.getDocumentText(bothersomeDocId));
//			assertEquals(false, handler.hasDocument(firstDocID));
//
//
//			handler.setDocumentsToRender(COMBINED_STATE);
//			assertEquals(defaultDocText.substring(0, defaultDocText.length()-i), handler.getDocumentText(defaultDocId));
//			assertEquals("Just some text", handler.getDocumentText(bothersomeDocId));
//			assertEquals(false, handler.hasDocument(firstDocID));
//		}
//		//Just to check that we repeated enough
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//
//		handler.handleManualConflictCompletely(whoWon);		//This is who got selected
//
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("Just some text", handler.getDocumentText(bothersomeDocId));
//		if (whoWon!=1)
//		{	
//			assertEquals(false, handler.hasDocument(firstDocID));
//		}
//		else
//		{
//			assertEquals("Small text.... for simplicity", handler.getDocumentText(firstDocID));
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(defaultDocText, handler.getDocumentText(defaultDocId));
//		assertEquals("Just some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(firstDocID));
//
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("Just some text", handler.getDocumentText(bothersomeDocId));
//		if (whoWon!=1)
//		{	
//			assertEquals(false, handler.hasDocument(firstDocID));
//		}
//		else
//		{
//			assertEquals("Small text.... for simplicity", handler.getDocumentText(firstDocID));
//		}
//
//		handler.handleAutomaticConflict();
//
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("Just some text", handler.getDocumentText(bothersomeDocId));
//		if (whoWon!=1)
//		{	
//			assertEquals(false, handler.hasDocument(firstDocID));
//		}
//		else
//		{
//			assertEquals("Small text.... for simplicity", handler.getDocumentText(firstDocID));
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals(defaultDocText, handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(firstDocID));
//
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		if (whoWon!=1)
//		{	
//			assertEquals(false, handler.hasDocument(firstDocID));
//		}
//		else
//		{
//			assertEquals("Small text.... for simplicity", handler.getDocumentText(firstDocID));
//		}
//
//		PlaybackEventRenderer viewer = new MockPlaybackViewer(pathToServer);
//
//		updateMergeNodeList(viewer);			//now that the merge is complete, add it to the list
//
//		timer.trace("Beginning playBackToNode");
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(mergeNodeIds.size()-1));
//		timer.trace("Finished playBackToNode");
//
//		assertEquals(handler.getDocumentText(defaultDocId), viewer.getDocumentText(defaultDocId));
//		if (whoWon == 1)
//		{
//			assertEquals(handler.getDocumentText(firstDocID), viewer.getDocumentText(firstDocID));
//		}
//		assertEquals(handler.getDocumentText(bothersomeDocId), viewer.getDocumentText(bothersomeDocId));
//	}
//
//	
//	public void seventhTestManualConflictCreateRenameDocuments() throws Exception
//	{
//		//because the last create had so many extra text events, if we branch from there, all subsequent (and there will probably be many) tests will be slowed down
//		//So, we'll make another branch off of mergeNodeIds.get(6) and start anew
//
//		mockIDE.openProject(testDbFileName, projectName, mergeNodeIds.get(6), MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//
//		MockPlaybackViewer viewer = new MockPlaybackViewer(pathToServer);
//
//		//branch once for the new ancestral node
//		viewer.branchAtEndOfNode(mergeNodeIds.get(6), mockIDE.getCurrentDevGroupID());
//
//		DefaultTreeModel tree = viewer.getExpectedTreeOfNodes();
//		//traverse the tree to the youngestCommonAncestor
//		MutableTreeNode root = getYoungestAncestor(tree);
//
//		String newNodeId = null;
//
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			//if the youngestCommonAncestor has a new "growth" that is not the firstNode, nor the secondNode and isn't already in the list of mergeNodeIds, then add it to the list
//			if (!(root.getChildAt(i).toString().equals(originalFirstNodeId)) && !(root.getChildAt(i).toString().equals(originalSecondNodeId)) && !(mergeNodeIds.contains(root.getChildAt(i).toString())))
//			{
//				newNodeId = root.getChildAt(i).toString();		
//			}
//		}
//
//		if (newNodeId == null)
//		{
//			fail("Couldn't find the new node Id");
//		}
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//		//Now that we have a fresh node, let's open to that node and then start by creating a document for use in the conflicts
//
//		mockIDE.openProject(testDbFileName, projectName, newNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", mockIDE.getCurrentBuffer());		//Another sanity check
//
//
//		mockIDE.sendCreateNewDocumentEventToServer(bothersomeDocId , bothersomeDocName, rootDirId);
//		mockIDE.setCurrentDocumentId(bothersomeDocId);
//		mockIDE.sendStringToServer("Just some text", 0);
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, newNodeId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(bothersomeDocId));
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals(mockIDE.getCurrentBuffer(),viewer.getDocumentText(defaultDocId));
//
//		//Now that my new common ancestor is set, I need to branch once from it, then I can call my helper function to set up the rest
//		logger.debug("Branching");
//		viewer.branchAtEndOfNode(newNodeId, mockIDE.getCurrentDevGroupID());
//		Thread.sleep(100);		//wait for the branch to go
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		restablishCommonAncestorAt(newNodeId);
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - Just some text
//		 *  
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - Just some text
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - Just some text
//		 * 
//		 * ...
//		 *
//		 */
//
//		mockIDE.openProject(testDbFileName, projectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", mockIDE.getCurrentBuffer());
//
//		mockIDE.findAndDeleteToServer("OPQRSTUVWXYZ!", 1);
//
//		mockIDE.setCurrentDocumentId(bothersomeDocId);
//		assertEquals("Just some text", mockIDE.getCurrentBuffer());		//one more check to be sure we are where we are
//
//
//		String firstDocID = UUID.randomUUID().toString();
//		final String conflictingFileName = "Greekish.txt";
//		mockIDE.sendCreateNewDocumentEventToServer(firstDocID, conflictingFileName, rootDirId);
//		mockIDE.setCurrentDocumentId(firstDocID);
//		String firstDocString = "Small text.... for simplicity";
//		mockIDE.sendStringToServer(firstDocString, 0);
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//		mockIDE.openProject(testDbFileName, projectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", mockIDE.getCurrentBuffer());
//		mockIDE.setCurrentDocumentId(bothersomeDocId);
//		assertEquals("Just some text", mockIDE.getCurrentBuffer());
//
//		//Rename another temporary document to the same name as what dev 1 did
//		mockIDE.sendRenameDocumentEventToServer(bothersomeDocId, bothersomeDocName, conflictingFileName, rootDirId);
//		mockIDE.sendStringToServer("DEV 2 IS AWESOME\n", 0);	//some propaganda in the renamed document
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - Just some text
//		 *  
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBeta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - Just some text
//		 * Greekish.txt - Small text.... for simplicity
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * Greekish.txt - DEV 2 IS AWESOME\nJust some text
//		 * 
//		 * ...
//		 *
//		 */
//		for(int i = 2;i>=0;i--)
//		{
//			MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//			assertEquals("This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!", handler.getDocumentText(defaultDocId));
//			seventhTestHelper(handler, i, bothersomeDocId, firstDocID);		//see this method for the ins and outs
//		}
//
//
//	}
//
//	/*
//	 * At this point, the state is as follows
//	 * 
//	 * Common Ancestor between FirstNode and SecondNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * InnocuousDoc.txt - Just some text
//	 *  
//	 * FirstNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBeta
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * InnocuousDoc.txt - Just some text
//	 * Greekish.txt - Small text.... for simplicity
//	 * 
//	 * SecondNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaOPQRSTUVWXYZ!
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * Greekish.txt - DEV 2 IS AWESOME\nJust some text
//	 * 
//	 * ...
//	 *
//	 * mergeNodeIds.get(10):
//	 * Merging.java - This is the Greekish alphabet: AlphaBeta
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * Greekish.txt - DEV 2 IS AWESOME\nJust some text
//	 * 
//	 * mergeNodeIds.get(11):
//	 * Merging.java - This is the Greekish alphabet: AlphaBeta
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * InnocuousDoc.txt - DEV 2 IS AWESOME\nJust some text
//	 * Greekish.txt - Small text.... for simplicity
//	 * 
//	 * mergeNodeIds.get(12):
//	 * Merging.java - This is the Greekish alphabet: AlphaBeta
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * InnocuousDoc.txt - DEV 2 IS AWESOME\nJust some text
//	 */
//	private void eighthTestHelper(MockMergeHandler handler, int devToPick, String newDirId, String myDirName, String newDocId, String conflictingName)
//	{
//		//Right now the list of conflict blocks should look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//							FirstNode:													SecondNode:
//		//				(uac)	create dir "MyDir"									(uac) add GammaDelta to default doc
//		//				(uac) move InnocuousDoc.txt to MyDir						(umc) create new doc ConflictingDocumentName.lol and type in it
//		//				(uac) rename InnocuousDoc.txt to Conflict...
//		//				(umc) move Conflict... to root dir
//		//				(uac) delete the 2 Conflict...
//		//				(uac) type a 1 in conflict...
//
//
//
//		handler.handleAutomaticConflict();			//create the directory
//		assertEquals(true, handler.isMerging());	
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);	
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		assertEquals(myDirName,handler.getAllSubdirectoryNames(rootDirId).get(0));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		assertEquals(0,handler.getAllSubdirectoryNames(rootDirId).size());
//
//		handler.setDocumentsToRender(COMBINED_STATE);	
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		assertEquals(myDirName,handler.getAllSubdirectoryNames(rootDirId).get(0));
//
//
//		handler.handleAutomaticConflict();			//move the document to the directory
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);	
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		assertEquals(bothersomeDocName,handler.getAllDocumentNames(newDirId).get(0));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		assertEquals(0,handler.getAllSubdirectoryNames(rootDirId).size());
//
//		handler.setDocumentsToRender(COMBINED_STATE);	
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		assertEquals(bothersomeDocName,handler.getAllDocumentNames(newDirId).get(0));
//
//
//		handler.handleAutomaticConflict();			//rename the document
//		assertEquals(true, handler.isMerging());
//
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		assertEquals(conflictingName,handler.getAllDocumentNames(newDirId).get(0));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		assertEquals(0,handler.getAllSubdirectoryNames(rootDirId).size());
//
//		handler.setDocumentsToRender(COMBINED_STATE);	
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		assertEquals(conflictingName,handler.getAllDocumentNames(newDirId).get(0));
//
//
//		handler.handleAutomaticConflict();			//Add GammaDelta
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);	
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//
//		handler.setDocumentsToRender(COMBINED_STATE);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//
//		//Right now the list of conflict blocks should look like this:   (uac) means unresolved automatic conflict (umc) means unresolved manual conflict (rc) means resolved conflict (un) means unneeded  (See documentation on mering for more details)
//		//							FirstNode:													SecondNode:
//		//				(rc)	create dir "MyDir"									(rc) add GammaDelta to default doc
//		//				(rc) move InnocuousDoc.txt to MyDir						(umc) create new doc ConflictingDocumentName.lol and type in it
//		//				(rc) rename InnocuousDoc.txt to Conflict...
//		//				(umc) move Conflict... to root dir
//		//				(uac) delete the 2 Conflict...
//		//				(uac) type a 1 in conflict...
//
//		handler.handleManualConflictCompletely(devToPick);
//		assertEquals(true, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		if (devToPick==1)
//		{
//			assertEquals(0,handler.getAllDocumentNames(newDirId).size());	//there's no more document in the dir
//		}
//		else
//		{
//			assertEquals(conflictingName,handler.getAllDocumentNames(newDirId).get(0));	//The document with the conflicting name is still here
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(0,handler.getAllSubdirectoryNames(rootDirId).size());
//		if (devToPick==2)
//		{
//			assertEquals(conflictingName, handler.getDocumentName(newDocId));
//			assertEquals("The concise brown moose ate cereal", handler.getDocumentText(newDocId));
//		}
//		else
//		{
//			assertEquals(false, handler.hasDocument(newDocId));
//		}
//
//
//
//		handler.setDocumentsToRender(COMBINED_STATE);	
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));		
//		if (devToPick==1)
//		{
//			assertEquals(0,handler.getAllDocumentNames(newDirId).size());	//there's no more document in the dir
//		}
//		else
//		{
//			assertEquals(conflictingName,handler.getAllDocumentNames(newDirId).get(0));	//The document with the conflicting name is still here
//		}
//		if (devToPick==2)
//		{
//			assertEquals(conflictingName, handler.getDocumentName(newDocId));
//			assertEquals("The concise brown moose ate cereal", handler.getDocumentText(newDocId));
//		}
//		else
//		{
//			assertEquals(false, handler.hasDocument(newDocId));
//		}
//
//
//		handler.handleAutomaticConflict();		//handle the deletion of the character
//		assertEquals(true, handler.isMerging());
//		handler.handleAutomaticConflict(); 		//handle the replacement with a 1
//		assertEquals(false, handler.isMerging());
//
//		handler.setDocumentsToRender(FIRST_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 1 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(false, handler.hasDocument(newDocId));
//		if (devToPick==1)
//		{
//			assertEquals(0,handler.getAllDocumentNames(newDirId).size());	//there's no more document in the dir
//		}
//		else
//		{
//			assertEquals(conflictingName,handler.getAllDocumentNames(newDirId).get(0));	//The document with the conflicting name is still here
//		}
//
//		handler.setDocumentsToRender(SECOND_DEVELOPER);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 2 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//		assertEquals(0,handler.getAllSubdirectoryNames(rootDirId).size());
//		if (devToPick==2)
//		{
//			assertEquals(conflictingName, handler.getDocumentName(newDocId));
//			assertEquals("The concise brown moose ate cereal", handler.getDocumentText(newDocId));
//		}
//		else
//		{
//			assertEquals(false, handler.hasDocument(newDocId));
//		}
//
//
//
//		handler.setDocumentsToRender(COMBINED_STATE);	
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", handler.getDocumentText(defaultDocId));
//		assertEquals("DEV 1 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));		
//		if (devToPick==1)
//		{
//			assertEquals(0,handler.getAllDocumentNames(newDirId).size());	//there's no more document in the dir
//		}
//		else
//		{
//			assertEquals(conflictingName,handler.getAllDocumentNames(newDirId).get(0));	//The document with the conflicting name is still here
//		}
//		if (devToPick==2)
//		{
//			assertEquals(conflictingName, handler.getDocumentName(newDocId));
//			assertEquals("The concise brown moose ate cereal", handler.getDocumentText(newDocId));
//		}
//		else
//		{
//			assertEquals(false, handler.hasDocument(newDocId));
//		}
//
//		PlaybackEventRenderer viewer = new MockPlaybackViewer(pathToServer);
//
//		updateMergeNodeList(viewer);			//now that the merge is complete, add it to the list
//
//		viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(mergeNodeIds.size()-1));
//
//		assertEquals(handler.getDocumentText(defaultDocId), viewer.getDocumentText(defaultDocId));
//		assertEquals(handler.getDocumentText(bothersomeDocId), viewer.getDocumentText(bothersomeDocId));
//		if (devToPick==2)
//		{
//			assertEquals(conflictingName, handler.getDocumentName(newDocId));
//			assertEquals("The concise brown moose ate cereal", handler.getDocumentText(newDocId));
//		}
//		else
//		{
//			assertEquals(false, handler.hasDocument(newDocId));
//		}
//	}
//
//
//	
//	public void eighthTestManualConflictCreateMoveDocument() throws Exception
//	{
//		//before doing anything, I want to reestablish the common ancestor at mergeNodesIds.get(12)
//		restablishCommonAncestorAt(mergeNodeIds.get(12));
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBeta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - DEV 2 IS AWESOME\nJust some text
//		 *  
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBeta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - DEV 2 IS AWESOME\nJust some text
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBeta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - DEV 2 IS AWESOME\nJust some text
//		 * 
//		 * ...
//		 *
//		 * mergeNodeIds.get(12):
//		 * Merging.java - This is the Greekish alphabet: AlphaBeta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - DEV 2 IS AWESOME\nJust some text
//		 */
//
//		mockIDE.openProject(testDbFileName, projectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", mockIDE.getCurrentBuffer());		//Another sanity check
//		mockIDE.setCurrentDocumentId(bothersomeDocId);
//		assertEquals("DEV 2 IS AWESOME\nJust some text", mockIDE.getCurrentBuffer());
//
//		String newDirId = UUID.randomUUID().toString();
//		String myDirName = "MyDir";
//		mockIDE.sendCreateNewDirectoryEventToServer(newDirId, myDirName, rootDirId);
//
//		mockIDE.sendMoveDocumentEventToServer(bothersomeDocId, rootDirId, newDirId);
//
//		final String conflictingName = "ConflictingDocumentName.lol";
//		mockIDE.sendRenameDocumentEventToServer(bothersomeDocId, bothersomeDocName, conflictingName, newDirId);
//
//		//This move will be in conflict.  
//		mockIDE.sendMoveDocumentEventToServer(bothersomeDocId, newDirId, rootDirId);
//		mockIDE.setCurrentDocumentId(bothersomeDocId);
//		mockIDE.sendDeletesToServer ("____X", 0);		//deletes the 2
//		mockIDE.insertStringToServer("____1", "_", 0);		//inserts a 1
//
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//
//		//Switch to the second node
//		mockIDE.openProject(testDbFileName, projectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("This is the Greekish alphabet: AlphaBeta", mockIDE.getCurrentBuffer());		//Another sanity check
//		mockIDE.setCurrentDocumentId(bothersomeDocId);
//		assertEquals("DEV 2 IS AWESOME\nJust some text", mockIDE.getCurrentBuffer());
//
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		mockIDE.sendStringToServer("GammaDelta", mockIDE.getCurrentLength());
//
//		String newDocId = UUID.randomUUID().toString();
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, conflictingName, rootDirId);
//		mockIDE.setCurrentDocumentId(newDocId);
//		mockIDE.sendStringToServer("The concise brown moose ate cereal", 0);
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBeta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - DEV 2 IS AWESOME\nJust some text
//		 *  
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBeta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//		 * MyDir/
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * InnocuousDoc.txt - DEV 2 IS AWESOME\nJust some text
//		 * ConflictingDocumentName.lol - The concise brown moose ate cereal
//		 * ...
//		 *
//		 */
//
//
//		for(int i = 0;i<3;i++)
//		{
//			MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//			assertEquals("This is the Greekish alphabet: AlphaBeta", handler.getDocumentText(defaultDocId));
//			eighthTestHelper(handler, i, newDirId, myDirName, newDocId, conflictingName);
//		}
//
//		assertEquals(16,mergeNodeIds.size());		//just an idiot checking assert
//
//	}
//
//	/*
//	 * At this point, the state is as follows
//	 * 
//	 * Common Ancestor between FirstNode and SecondNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBeta
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * InnocuousDoc.txt - DEV 2 IS AWESOME\nJust some text
//	 *  
//	 * FirstNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBeta
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//	 * MyDir/
//	 * 
//	 * SecondNode:
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * InnocuousDoc.txt - DEV 2 IS AWESOME\nJust some text
//	 * ConflictingDocumentName.lol - The concise brown moose ate cereal
//	 * 
//	 * ...
//	 *
//	 * mergeNodeIds.get(13):
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * MyDir/ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//	 * 
//	 * mergeNodeIds.get(14):
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//	 * 
//	 * mergeNodeIds.get(15):
//	 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//	 * WooDocument.xlsx - Just amazing text
//	 * NonConflictingName.cpp - All sorts of writing!!!!
//	 * ConflictingDocumentName.lol - The concise brown moose ate cereal
//	 * MyDir/ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//	 * 
//	 */
//	public void ninthTestManualConflictCreateRenameDocument() throws Exception
//	{
//		restablishCommonAncestorAt(mergeNodeIds.get(14));		//I don't want troublesomeDoc to be in a directory, so I'll use 14
//		bothersomeDocName = "ConflictingDocumentName.lol";
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//		 * MyDir/
//		 *  
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//		 * MyDir/
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//		 * MyDir/
//		 * 
//		 * ...
//		 *
//		 * 
//		 * mergeNodeIds.get(14):
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//		 * MyDir/
//		 * 
//		 * ...
//		 */
//
//		mockIDE.openProject(testDbFileName, projectName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", mockIDE.getCurrentBuffer());		//Another sanity check
//		mockIDE.setCurrentDocumentId(bothersomeDocId);
//		assertEquals("DEV 1 IS AWESOME\nJust some text", mockIDE.getCurrentBuffer());
//
//		final String conflictingDocName = "thing.jar";
//		mockIDE.sendRenameDocumentEventToServer(bothersomeDocId, bothersomeDocName, conflictingDocName, rootDirId);
//		mockIDE.closeProject();
//		Thread.sleep(500);
//
//
//
//		mockIDE.openProject(testDbFileName, projectName, firstNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", mockIDE.getCurrentBuffer());		//Another sanity check
//		mockIDE.setCurrentDocumentId(bothersomeDocId);
//		assertEquals("DEV 1 IS AWESOME\nJust some text", mockIDE.getCurrentBuffer());
//
//		String newDocId = UUID.randomUUID().toString();
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, conflictingDocName, rootDirId);
//		mockIDE.setCurrentDocumentId(newDocId);
//		mockIDE.sendStringToServer("blaH", 0);
//
//		/*
//		 * At this point, the state is as follows
//		 * 
//		 * Common Ancestor between FirstNode and SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//		 * MyDir/
//		 *  
//		 * FirstNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * ConflictingDocumentName.lol - DEV 1 IS AWESOME\nJust some text
//		 * thing.jar - blaH
//		 * MyDir/
//		 * 
//		 * SecondNode:
//		 * Merging.java - This is the Greekish alphabet: AlphaBetaGammaDelta
//		 * WooDocument.xlsx - Just amazing text
//		 * NonConflictingName.cpp - All sorts of writing!!!!
//		 * thing.jar - DEV 1 IS AWESOME\nJust some text
//		 * MyDir/
//		 * 
//		 * ...
//		 */
//
//		for(int whoWon = 0;whoWon<3;whoWon++)
//		{
//			MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//			assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", handler.getDocumentText(defaultDocId));
//
//
//			handler.handleManualConflictCompletely(whoWon);
//			assertEquals(false, handler.isMerging());
//
//			assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", handler.getDocumentText(defaultDocId));
//			assertEquals("DEV 1 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//			if (whoWon == 1)
//			{
//				assertEquals("blaH", handler.getDocumentText(newDocId));
//			}
//			else
//			{
//				assertEquals(false, handler.hasDocument(newDocId));
//			}
//
//
//
//			handler.setDocumentsToRender(SECOND_DEVELOPER);
//			assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", handler.getDocumentText(defaultDocId));
//			assertEquals("DEV 1 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//			assertEquals(false, handler.hasDocument(newDocId));
//			if (whoWon == 2)
//			{
//				assertEquals(conflictingDocName, handler.getDocumentName(bothersomeDocId));
//			}
//			else
//			{
//				assertEquals(bothersomeDocName, handler.getDocumentName(bothersomeDocId));
//			}
//
//
//			handler.setDocumentsToRender(COMBINED_STATE);	
//			assertEquals("This is the Greekish alphabet: AlphaBetaGammaDelta", handler.getDocumentText(defaultDocId));
//			assertEquals("DEV 1 IS AWESOME\nJust some text", handler.getDocumentText(bothersomeDocId));
//			if (whoWon == 1)
//			{
//				assertEquals("blaH", handler.getDocumentText(newDocId));
//			}
//			else
//			{
//				assertEquals(false, handler.hasDocument(newDocId));
//			}
//			if (whoWon == 2)
//			{
//				assertEquals(conflictingDocName, handler.getDocumentName(bothersomeDocId));
//			}
//			else
//			{
//				assertEquals(bothersomeDocName, handler.getDocumentName(bothersomeDocId));
//			}
//
//			PlaybackEventRenderer viewer = new MockPlaybackViewer(pathToServer);
//
//			updateMergeNodeList(viewer);			//now that the merge is complete, add it to the list
//
//			viewer = MockPlaybackViewer.playBackToNode(pathToServer, mergeNodeIds.get(mergeNodeIds.size()-1));
//			if (whoWon == 1)
//			{
//				assertEquals("blaH", viewer.getDocumentText(newDocId));
//			}
//			else
//			{
//				assertEquals(false, viewer.hasDocument(newDocId));
//			}
//			if (whoWon == 2)
//			{
//				assertEquals(conflictingDocName, viewer.getDocumentName(bothersomeDocId));
//			}
//			else
//			{
//				assertEquals(bothersomeDocName, viewer.getDocumentName(bothersomeDocId));
//			}
//
//		}
//	}
//
//	//This unit test is getting quite long and complicated.  I'm going to create a part two
//}
