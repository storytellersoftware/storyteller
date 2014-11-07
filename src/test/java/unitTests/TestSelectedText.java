//package unitTests;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.fail;
//
//import java.io.File;
//import java.util.UUID;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import Playback.MockPlaybackViewer;
//import Playback.MockSelectedPlaybackViewer;
//import StorytellerServer.DBFactory;
//import StorytellerServer.SQLiteDBFactory;
//import StorytellerServer.SQLiteDatabase;
//import StorytellerServer.StorytellerServer;
//import StorytellerServer.ide.IDEProxy;
//import StorytellerServer.playback.PlaybackProxy;
//
//public class TestSelectedText 
//{
//	private static String pathToServer = "127.0.0.1";
//
//	//IDE generated id of the root directory
//	private static String rootDirId;
//
//	//mock IDE to act as a real IDE
//	private static MockIDE mockIDE;
//
//	private static DBFactory sqliteDbFactory;
//
//	// default test server
//	private static StorytellerServer testServer;
//
//	// test database file name
//	private static String testDbFileName = "unitTestDBForSelectedText" + SQLiteDatabase.DB_EXTENSION_NAME;
//	
//	private static final String testProjectName = "TestSelectedTestProject";
//
//	// this is the IDEProxy that the IDE Faker will talk to
//	private static IDEProxy serverProxy = null;
//
//	// this is the PlaybackBackProxy that the browser emulator will listen to
//	private static PlaybackProxy playbackProxy = null;
//
//	private static Thread ideThread = null;
//
//	private static Thread playbackThread = null;
//
//	private static Logger logger = Logger.getLogger(TestTextEvents.class.getName());
//
//	@SuppressWarnings("unused")
//	private static Logger timer = Logger.getLogger("timing."+TestTextEvents.class.getName());
//
//	/*
//	 * So, we set this test up statically, so we have the same database, the
//	 * same project, the same server and the same mockIDE for all of them.
//	 * 
//	 * Why? Because A. It's more lifelike and B. the constant switching isn't
//	 * consistent on Windows machines due to the funkiness of Java's deletes
//	 */
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
//		PropertyConfigurator.configure(AllTests.LOGGING_FILE_PATH);
//
//		File oldDB = new File(testDbFileName);
//		if (oldDB.exists())
//			oldDB.delete();
//
//		sqliteDbFactory = new SQLiteDBFactory();
//		// create a default test server and pass in the factory
//		testServer = new StorytellerServer(sqliteDbFactory);
//
//		// create a thread to listen for IDE data
//		serverProxy = new IDEProxy(testServer);
//		ideThread = new Thread(serverProxy);
//		// create a thread to listen for playback requests
//		playbackProxy = new PlaybackProxy(testServer);
//		playbackThread = new Thread(playbackProxy);
//
//		ideThread.start();
//		playbackThread.start();
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			logger.fatal("", e);
//		}
//		// create a project using the mockIDE for the test server
//		rootDirId = UUID.randomUUID().toString();
//
//		mockIDE = new MockIDE(pathToServer);
//
//		MockPlaybackViewer.setDevFirstName("Mark");
//		MockPlaybackViewer.setDevLastName("Mahoney");
//		MockPlaybackViewer.setDevEmailAddress("mmahoney@carthage.edu");
//		mockIDE.createNewProject(testDbFileName, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, testProjectName);
//	
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception {
//		logger.info("Closing down playback and serverproxy");
//
//		mockIDE.closeProject();
//
//		//playbackProxy.quit();
//		serverProxy.quit();
//
//		// give the threads a moment to die
//		try {
//			while (playbackThread.isAlive() || ideThread.isAlive()) {
//				logger.debug("Waiting for ideThread and playbackThread to die");
//				Thread.sleep(100);
//			}
//		} catch (InterruptedException e) {
//			logger.fatal("", e);
//		}
//
//	}
//	
//	@Test
//	public void testSimpleSelectionWithNoHistory() throws Exception {
//		// IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//
//		// name of a new document
//		String newDocName = "SimpleSelectionWithNoHistory.java";
//
//		// create a new document event in the root dir and send it to the server
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName,
//				rootDirId);
//
//		// text to type into the new document
//		String testText = "The quick brown fox jumped over the lazy dog";
//
//		// insert the characters from the passed in string starting at pos 0
//		mockIDE.sendStringToServer(testText, 0);		    
//
//		//select the first occurrence of the text "brown" (this returns the playabck
//		//session id that holds the relevant selected events)
//		String selectedPlaybackSessionId = mockIDE.createSelectedPlayback("brown", 1);
//
//		//get a mock playback viewer for selected playbacks
//		MockSelectedPlaybackViewer mspv = MockSelectedPlaybackViewer.playback(pathToServer, selectedPlaybackSessionId);  
//
//		//assert that the playback viewer has the right text in it after playback
//		assertEquals(testText, mspv.getDocumentText(newDocId));
//
//		//there should be 5 relevant events- 'b' 'r' 'o' 'w' 'n'
//		assertEquals(5, mspv.getRelevantEventIds().size());
//
//		//all the relevant events in the order they are played back (the pattern IxIyIz
//		//means the letters xyz were inserted in the playback and were relevant)
//		String matchString = "IbIrIoIwIn";
//
//		//if the relevant events do not match
//		if(!mspv.relevantEventsMatch(matchString))
//		{
//			fail("Selected events was not "+matchString +" , It was "+mspv.getRelevantEventsAsHumanText());
//		}			
//	}
//
//	@Test
//	public void testSimpleSelectionWithShallowHistory() throws Exception
//	{
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//
//		//name of a new document
//		String newDocName = "SimpleSelectionWithHistory.java";
//
//		//create a new document event in the root dir and send it to the server
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//
//		//text to type into the new document
//		String testText = "The quick brown fox jumped over the lazy dog";
//
//		//insert the characters from the passed in string starting at pos 0
//		mockIDE.sendStringToServer(testText, 0);
//		//delete "brown"
//		mockIDE.sendDeletesToServer ("__________XXXXX", 0);
//		//insert "red"
//		mockIDE.insertStringToServer("__________red", "_", 0);
//		//delete "red"
//		mockIDE.sendDeletesToServer ("__________XXX", 0);
//		//insert "groon"
//		mockIDE.insertStringToServer("__________groon", "_", 0);
//		//insert " m"- gr moon
//		mockIDE.insertStringToServer("____________ m", "_", 0);
//		//insert "een"- green moon
//		mockIDE.insertStringToServer("____________een", "_", 0);
//
//		//select the first occurrence of the text "green"
//		String selectedPlaybackSessionId = mockIDE.createSelectedPlayback("green", 1);
//
//		//get a mock playback viewer for selected playbacks
//		MockSelectedPlaybackViewer mspv = MockSelectedPlaybackViewer.playback(pathToServer, selectedPlaybackSessionId);  
//
//		//make sure the text in the doc buffer is correct
//		assertEquals("The quick green moon fox jumped over the lazy dog", mockIDE.getCurrentBuffer());
//
//		//there should be 10 relevant events- "green moon"
//		assertEquals(10, mspv.getRelevantEventIds().size());
//
//		//all the relevant events in the order they are played back
//		String matchString = "IgIrIoIoInI ImIeIeIn";
//
//		//if the correct relevant events were NOT added in the right order
//		if(!mspv.relevantEventsMatch(matchString))
//		{
//			fail("Selected events was not "+matchString +" , It was "+mspv.getRelevantEventsAsHumanText());
//		}		
//	}
//
//	@Test
//	public void testSimpleSelectionWithDeepHistory() throws Exception
//	{
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//
//		//name of a new document
//		String newDocName = "SimpleSelectionWithHistory.java";
//
//		//create a new document event in the root dir and send it to the server
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//
//		//text to type into the new document
//		String testText = "The quick brown fox jumped over the lazy dog";
//
//		//insert the characters from the passed in string starting at pos 0
//		mockIDE.sendStringToServer(testText, 0);
//		//delete "brown"
//		mockIDE.sendDeletesToServer ("__________XXXXX", 0);
//		//insert "red"
//		mockIDE.insertStringToServer("__________red", "_", 0);
//		//delete "red"
//		mockIDE.sendDeletesToServer ("__________XXX", 0);
//		//insert "groon"
//		mockIDE.insertStringToServer("__________groon", "_", 0);
//		//insert " m"- gr moon
//		mockIDE.insertStringToServer("____________ m", "_", 0);
//		//insert "een"- green moon
//		mockIDE.insertStringToServer("____________een", "_", 0);		
//
//		//select the first occurrence of the text " green" with a space for deep history (brown and red)
//		String selectedPlaybackSessionId = mockIDE.createSelectedPlayback(" green", 1);
//
//		//get a mock playback viewer for selected playbacks
//		MockSelectedPlaybackViewer mspv = MockSelectedPlaybackViewer.playback(pathToServer, selectedPlaybackSessionId);  
//
//		//make sure the text in the doc buffer is correct
//		assertEquals("The quick green moon fox jumped over the lazy dog", mockIDE.getCurrentBuffer());
//
//		//there should be 27 relevant events- " brownXXXXXredXXXgreen moon"
//		assertEquals(27, mspv.getRelevantEventIds().size()); 
//
//		//all the relevant events in the order they are played back
//		String matchString = "I IbIrIoIwInDnDwDoDrDbIrIeIdDdDeDrIgIrIoIoInI ImIeIeIn";
//
//		//if the correct relevant events were NOT added in the right order
//		if(!mspv.relevantEventsMatch(matchString))
//		{
//			fail("Selected events was not "+matchString +" , It was "+mspv.getRelevantEventsAsHumanText());
//		}		
//	}
//
//	@Test
//	public void testSimpleSelectionWithDeeperHistory() throws Exception	//Very similar to testSimpleSelectionWithDeepHistory, execpt quick is added in front of brown
//	{
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//
//		//name of a new document
//		String newDocName = "SimpleSelectionWithDeeperHistory.java";
//
//		//create a new document event in the root dir and send it to the server
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//
//		//text to type into the new document
//		String testText = "The brown fox jumped over the lazy dog";
//
//		//insert the characters from the passed in string starting at pos 0
//		mockIDE.sendStringToServer(testText, 0);
//		//insert "quick" before brown
//		mockIDE.insertStringBeforeToServer("quick ", "brown", 1);
//		//delete "brown"
//		mockIDE.sendDeletesToServer ("__________XXXXX", 0);
//		//insert "red"
//		mockIDE.insertStringToServer("__________red", "_", 0);
//		//delete "red"
//		mockIDE.sendDeletesToServer ("__________XXX", 0);
//		//insert "groon"
//		mockIDE.insertStringToServer("__________groon", "_", 0);
//		//insert " m"- gr moon
//		mockIDE.insertStringToServer("____________ m", "_", 0);
//		//insert "een"- green moon
//		mockIDE.insertStringToServer("____________een", "_", 0);		
//
//		//select the first occurrence of the text " green" with a space for deep history (brown, quick and red)
//		String selectedPlaybackSessionId = mockIDE.createSelectedPlayback(" quick green", 1);
//
//		//get a mock playback viewer for selected playbacks
//		MockSelectedPlaybackViewer mspv = MockSelectedPlaybackViewer.playback(pathToServer, selectedPlaybackSessionId);  
//
//		//make sure the text in the doc buffer is correct
//		assertEquals("The quick green moon fox jumped over the lazy dog", mockIDE.getCurrentBuffer());
//
//		//there should be 33 relevant events- " brownquick XXXXXredXXXgreen moon"
//		assertEquals(33, mspv.getRelevantEventIds().size()); 
//
//		//all the relevant events in the order they are played back
//		String matchString = "I IbIrIoIwInIqIuIiIcIkI DnDwDoDrDbIrIeIdDdDeDrIgIrIoIoInI ImIeIeIn";
//
//		//if the correct relevant events were NOT added in the right order
//		if(!mspv.relevantEventsMatch(matchString))
//		{
//			fail("Selected events was not "+matchString +" , It was "+mspv.getRelevantEventsAsHumanText());
//		}		
//	}
//
//	@Test
//	public void testSelectionWithMultipleNodesHistory() throws Exception
//	{
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//
//		//name of a new document
//		String newDocName = "SimpleSelectionWithHistory.java";
//
//		//create a new document event in the root dir and send it to the server
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//
//		//text to type into the new document
//		String testText = "The quick brown fox jumped over the lazy dog";
//
//		//insert the characters from the passed in string starting at pos 0
//		mockIDE.sendStringToServer(testText, 0);
//		//close the current node and open a new node
//		mockIDE.commit("closing brown node", "");
//		//send some more text in the new node
//		mockIDE.sendDeletesToServer ("__________XXXXX", 0);
//		mockIDE.insertStringToServer("__________red", "_", 0);
//		//close the current node and open a new node
//		mockIDE.commit("closing red node", "");
//		//send some more text in the new node
//		mockIDE.sendDeletesToServer ("__________XXX", 0);
//		mockIDE.insertStringToServer("__________groon", "_", 0);
//		mockIDE.insertStringToServer("____________ m", "_", 0);
//		mockIDE.insertStringToServer("____________een", "_", 0);
//
//		//select the first occurrence of the text " green"
//		String selectedPlaybackSessionId = mockIDE.createSelectedPlayback(" green", 1);
//
//		//get a mock playback viewer for selected playbacks
//		MockSelectedPlaybackViewer mspv = MockSelectedPlaybackViewer.playback(pathToServer, selectedPlaybackSessionId);  
//
//		//make sure the text in the doc buffer is correct
//		assertEquals("The quick green moon fox jumped over the lazy dog", mockIDE.getCurrentBuffer());
//
//		//there should be 27 relevant events- " brownXXXXXredXXXgreen moon"
//		assertEquals(27, mspv.getRelevantEventIds().size());
//
//		//all the relevant events in the order they are played back
//		String matchString = "I IbIrIoIwInDnDwDoDrDbIrIeIdDdDeDrIgIrIoIoInI ImIeIeIn";
//
//		//if the correct relevant events were NOT added in the right order AND in the right nodes
//		if(!mspv.relevantEventsMatch(matchString))
//		{
//			fail("Selected events was not "+matchString +" , It was "+mspv.getRelevantEventsAsHumanText());
//		}		
//	}
//
//	@Test
//	public void testSelectionWithBranchInTheMiddleOfANodesHistory() throws Exception 
//	{
//		/*
//	  //IDE creates a unique id for the new document
//	  String newDocId = UUID.randomUUID().toString();
//	  
//	  //name of a new document 
//	  String newDocName = "SimpleSelectionWithHistory.java";
//	  
//	  //create a new document event in the root dir and send it to the server 
//	  mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//	  
//	  //text to type into the new document 
//	  String testText = "The quick brown fox";
//	  
//	  //insert the characters from the passed in string starting at pos 0
//	  mockIDE.sendStringToServer(testText, 0); //close the current node and open a new node mockIDE.commit("closing brown node", "");
//	  
//	  mockIDE.commit("closing brown node", "");
//	  
//	  mockIDE.sendStringToServer("jumped over the lazy dog", mockIDE.getCurrentLength());
//	  
//	  mockIDE.commit("closing action node", "");
//	  
//	  MockPlaybackViewer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//	  
//	  String idOfElementToBranchAt = playbackViewer.getIdOfEvent(newDocId, testText.indexOf("rown"));
//	  
//	  playbackViewer.branchAtId(idOfElementToBranchAt,mockIDE.getCurrentDevGroupID());
//	  
//	  MutableTreeNode root = (MutableTreeNode) playbackViewer.getExpectedTreeOfNodes().getRoot();
//	  
//	  String idOfNewNode = null; 
//	  MutableTreeNode oldNode = null;
//	  
//	  for(int i = 0;i<root.getChildCount();i++)
//	  { 
//		  MutableTreeNode child = (MutableTreeNode) root.getChildAt(i); 
//		  if (child.getChildCount()==0) //this is the new branch
//		  {
//			  idOfNewNode=child.toString(); //store the id 
//		  }
//	  	  else 
//	  	  {
//	  		  oldNode=child; //grab the id of the grandChild as the old"head" 
//  		  } 
//	  }
//	  
//	  mockIDE.closeProject();
//	  
//	  Thread.sleep(500); //just wait a second... hold.... hold..........GO!
//	  
//	  mockIDE.openProject(testDbFileName, idOfNewNode , MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail()); 
//	  mockIDE.setCurrentDocumentId(newDocId);
//	  
//	  String text = "ight fox did something interesting";
//	  
//	  mockIDE.sendStringToServer(text, mockIDE.getCurrentLength());
//	  
//	  assertEquals("The quick bright fox did something interesting", mockIDE.getCurrentBuffer());
//	  
//	  //select the first occurrence of the text " green" 
//	  String selectedPlaybackSessionId = mockIDE.createSelectedPlayback("bright", 1);
//	  
//	  //get a mock playback viewer for selected playbacks
//	  MockSelectedPlaybackViewer mspv =  MockSelectedPlaybackViewer.playback(pathToServer, selectedPlaybackSessionId);
//	  
//	  
//	  //make sure the text in the doc buffer is correct
//	 
//	  
//	  //there should be 6 relevant events- " bright"
//	  assertEquals(6, mspv.getRelevantEventIds().size());
//	  
//	  //all the relevant events in the order they are played back 
//	  String matchString = "IbIrIiIgIhIt";
//	  
//	  //if the correct relevant events were NOT added in the right order AND in the right nodes 
//	  if(!mspv.relevantEventsMatch(matchString)) 
//	  {
//		  fail("Selected events was not "+matchString+" , It was "+mspv.getRelevantEventsAsHumanText()); 
//	  }
//	 */
//}
//
//	/*
//	@Test
//	public void testSelectionWithMergedHistory() throws Exception 
//	{
//
//	  String firstNodeId = null;
//	  //IDE creates a unique id for the new document
//	  String newDocId = UUID.randomUUID().toString();
//	  
//	  //name of a new document 
//	  String newDocName = "SimpleSelectionWithMergingHistory.java";
//	  
//	  //create a new document event in the root dir and send it to the server 
//	  mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//	  
//	  //text to type into the new document 
//	  String testText = "The quick brown fox jumped over the lazy dog";
//	  
//	  //insert the characters from the passed in string starting at pos 0
//	  mockIDE.sendStringToServer(testText, 0); //close the current node and open a new node mockIDE.commit("closing brown node", "");
//	  MockPlaybackViewer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//	  
//	  String idOfElementToBranchAt = playbackViewer.getIdOfEvent(newDocId, mockIDE.getCurrentLength() - 1);
//	  
//	  mockIDE.commit("closing initial node", "");
//	  
//	  mockIDE.insertStringToServer("-------------------------- carefully", "-", 0);
//	  
//	  
//	  playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//	  
//	  DefaultTreeModel tree = playbackViewer.getExpectedTreeOfNodes();
//	  
//	  logger.info("Before Branch: "+PlaybackEventRenderer.treeModelToString(tree));
//	  
//	  MutableTreeNode root = (MutableTreeNode) tree.getRoot();
//	  
//	  firstNodeId = root.getChildAt(0).toString();
//	  
//	  // branch for second node
//	  playbackViewer.branchAtId(idOfElementToBranchAt, mockIDE.getCurrentDevGroupID());
//		
//	  tree = playbackViewer.getExpectedTreeOfNodes();
//	  root = (MutableTreeNode) tree.getRoot();
//	  
//	  String secondNodeId = null;
//		// parse through the tree to find the newly branched fork. That is
//		// "secondNode"
//		for (int i = 0; i < root.getChildCount(); i++) {
//			if (!(root.getChildAt(i).toString().equals(firstNodeId))) {
//				secondNodeId = root.getChildAt(i).toString();
//			}
//		}
//
//		if (secondNodeId == null) {
//			fail("Unable to find the id of the secondNode");
//		}
//		tree = playbackViewer.getExpectedTreeOfNodes();
//		logger.info("After Branch: "+PlaybackEventRenderer.treeModelToString(tree));
//		
//		
//		mockIDE.closeProject();
//
//		Thread.sleep(500); // just wait a second... hold.... hold.......... GO!
//
//		mockIDE.openProject(testDbFileName, secondNodeId, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail());
//		mockIDE.setCurrentDocumentId(newDocId);
//		
//		assertEquals("The quick brown fox jumped over the lazy dog", mockIDE.getCurrentBuffer());
//	  
//	    mockIDE.insertStringToServer("------------------- and the slow turtle", "-", 0);
//	    
//	    assertEquals("The quick brown fox and the slow turtle jumped over the lazy dog", mockIDE.getCurrentBuffer());
//	    
//	    MockMergeHandler handler = MockMergeHandler.getMergeHandlerUpThroughFirstConflictNoCustomResolutions(firstNodeId, secondNodeId, mockIDE.getCurrentDevGroupID());
//	    
//	    handler.handleAutomaticConflict();
//	    assertTrue(handler.isMerging());
//	    handler.handleAutomaticConflict();
//	    assertFalse(handler.isMerging());
//	    
//	    String mergeChildId = null;
//	    tree = playbackViewer.getExpectedTreeOfNodes();
//	    logger.info("After Merge: "+PlaybackEventRenderer.treeModelToString(tree));
//		root = (MutableTreeNode) tree.getRoot();
//		  
//			for (int i = 0; i < root.getChildCount(); i++) 
//			{
//				if (!(root.getChildAt(i).toString().equals(firstNodeId)&&!(root.getChildAt(i).toString().equals(secondNodeId))))
//				{
//					mergeChildId = root.getChildAt(i).getChildAt(0).toString();
//					break;
//				}
//			}
//
//			if (mergeChildId == null) {
//				fail("Unable to find the id of the secondNode");
//			}
//	    
//
//	
//	}
//	*/
//
//}