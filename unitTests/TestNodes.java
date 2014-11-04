//package unitTests;
//
//import static Playback.MockPlaybackViewer.treeModelToString;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//
//import java.io.File;
//import java.util.UUID;
//
//import javax.swing.tree.MutableTreeNode;
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
///**
// * This test case tests to see if branching works by making a few versions of a big text document.
// * 
// * 
// * @author Kevin
// *
// */
//public class TestNodes
//{
//	private static String pathToServer="127.0.0.1";
//	
//	
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
//	private static String testDbFileName = "unitTestDBForTestNodes" + SQLiteDatabase.DB_EXTENSION_NAME;
//	
//	private static String testProjectName = "unitTestForTestNodesProject";
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
//	private static String defaultDocId;
//		
//	private String firstNodeText = "The Battle of Schellenberg,\n also known as the Battle of Donauworth, was fought on 2 July 1704" +
//	" during the War of the Spanish Succession.";
//	private String secondNodeText = "The engagement was part of the Duke of Marlborough's campaign to save" +
//	" the Habsburg capital of Vienna from a threatened advance by King Louis XIV's Franco-Bavarian forces ranged in" +
//	" southern Germany. Marlborough had commenced his 250-mile (400 km) march from Bedburg, near Cologne, on 19 May;" +
//	" within five weeks he had linked his forces with those of the Margrave of Baden, before continuing on to the river Danube.";
//	
//	private String thirdNodeText = "\n\nAnd some dangling";
//
//	private String shortendFirstNodeText = "The Battle of ";
//	
//	private String unicornString = "Broken Horn was a devestating loss to the unicorn forces there and everywhere around the world.\n" +
//			"Over 3 million (and as many as 5 million, according to some sources) unicorns died at the hands of the small, yet well" +
//			"-armored dragons over a 18 hour period.";
//	
//	
//	private static Logger logger = Logger.getLogger(TestNodes.class.getName());
//	@SuppressWarnings("unused")
//	private static Logger timer = Logger.getLogger("timing."+TestNodes.class.getName());
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
//		mockIDE.createNewProject(testDbFileName, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, testProjectName);		
//	}
//
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception 
//	{
//		logger.info("Closing down playback and serverproxy");
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
//			logger.fatal("",e);
//		}
//
//	}
//	@Test
//	public void writingCommitWriting()			//this was the article of the day on 7/2/12
//	{
//		defaultDocId = UUID.randomUUID().toString();
//
//		//name of a new document
//		String newDocName = "CommitmentIssues.java";
//		
//		//create a new document event in the root dir and send it to the server
//		mockIDE.sendCreateNewDocumentEventToServer(defaultDocId, newDocName, rootDirId);
//		
//		mockIDE.sendStringToServer(firstNodeText,0);
//		
//		mockIDE.commit("Woo!", "End of the first Sentance");
//		
//		mockIDE.sendStringToServer(secondNodeText, mockIDE.getCurrentLength());
//		
//		mockIDE.commit("Progress", "End of the Paragraph");
//		
//		mockIDE.sendStringToServer(thirdNodeText, mockIDE.getCurrentLength());
//		
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		
//		assertEquals(firstNodeText+secondNodeText+thirdNodeText, playbackViewer.getDocumentText(defaultDocId));
//		
//		assertFalse(playbackViewer.getRenderedTreeOfNodes()==null);
//		assertFalse(playbackViewer.getRenderedTreeOfNodes().getRoot()==null);
//		MutableTreeNode root = (MutableTreeNode) playbackViewer.getRenderedTreeOfNodes().getRoot();
//		
//		//There should be exactly three nodes, one of which is a leaf
//		assertEquals(1,root.getChildCount());
//		assertEquals(1,root.getChildAt(0).getChildCount());
//		assertEquals(0,root.getChildAt(0).getChildAt(0).getChildCount());
//		
//		//compare expected to rendered using the toString method because the standard .equals doesn't work well for DefaultTreeModels
//		assertEquals(treeModelToString(playbackViewer.getExpectedTreeOfNodes()), treeModelToString(playbackViewer.getRenderedTreeOfNodes()));
//		
//		playbackViewer = MockPlaybackViewer.playBackToNode(pathToServer, root.toString());		//play just the first node
//		
//		assertEquals(firstNodeText,playbackViewer.getDocumentText(defaultDocId));
//		
//		assertFalse(treeModelToString(playbackViewer.getExpectedTreeOfNodes()).equals(treeModelToString(playbackViewer.getRenderedTreeOfNodes())));
//		
//	}
//
//	
//	@Test
//	public void testOpenToLastNode() throws Exception
//	{
//		mockIDE.closeProject();
//		
//		Thread.sleep(500);		//just wait a second... hold.... hold.......... GO!
//		mockIDE.openProject(testDbFileName, testProjectName, null , MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		
//		assertEquals(firstNodeText+secondNodeText+thirdNodeText, playbackViewer.getDocumentText(defaultDocId));	
//	}
//	
//	
//	@Test
//	public void testBranching() throws Exception
//	{
//		MockPlaybackViewer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		
//		String idOfElementToBranchAt = playbackViewer.getIdOfEvent(defaultDocId,13);		
//		
//		playbackViewer.branchAtId(idOfElementToBranchAt, mockIDE.getCurrentDevGroupID());
//		
//		MutableTreeNode root = (MutableTreeNode) playbackViewer.getExpectedTreeOfNodes().getRoot();
//		
//		assertEquals(2, root.getChildCount());
//		
//		mockIDE.closeProject();
//		
//		Thread.sleep(500);				//just wait a second... hold.... hold.......... GO!
//		
//		String idOfNewNode = null;
//		String idOfOldNode = null;
//	
//		
//		for(int i = 0;i<root.getChildCount();i++)
//		{
//			MutableTreeNode child = (MutableTreeNode) root.getChildAt(i);
//			if (child.getChildCount()==0)		//this is the new branch
//			{
//				idOfNewNode=child.toString();		//store the id
//			}
//			else
//			{
//				idOfOldNode=child.getChildAt(0).toString();		//grab the id of the grandChild as the old "head"
//			}
//		}
//		
//		mockIDE.openProject(testDbFileName, testProjectName, idOfNewNode , MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		
//		mockIDE.setCurrentDocumentId(defaultDocId);
//		
//		assertEquals(firstNodeText.substring(0, 14), mockIDE.getCurrentBuffer());		//check that only the first 13 letters "The Battle of" are in the mockIDE
//		
//		playbackViewer = MockPlaybackViewer.playBackToNode(pathToServer, idOfNewNode);
//		assertEquals(firstNodeText.substring(0, 14), playbackViewer.getDocumentText(defaultDocId));		//check also the playback is at the same spot
//		
//		mockIDE.sendStringToServer(unicornString, mockIDE.getCurrentLength());		//add some text
//		
//		assertEquals(shortendFirstNodeText+unicornString, mockIDE.getCurrentBuffer());	//sanity check on mockIDE
//		
//		playbackViewer = MockPlaybackViewer.playBackToNode(pathToServer, idOfNewNode);
//		assertEquals(shortendFirstNodeText+unicornString, playbackViewer.getDocumentText(defaultDocId));	//check that the playback got the new text
//		
//		
//		playbackViewer = MockPlaybackViewer.playBackToNode(pathToServer,idOfOldNode);
//		assertEquals(firstNodeText+secondNodeText+thirdNodeText, playbackViewer.getDocumentText(defaultDocId));	//make sure that the original branch hasn't changed	
//	}
//}
