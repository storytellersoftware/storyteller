//package unitTests;
//
//import static org.junit.Assert.assertEquals;
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
//import Playback.PlaybackEventRenderer;
//import StorytellerServer.DBFactory;
//import StorytellerServer.SQLiteDBFactory;
//import StorytellerServer.SQLiteDatabase;
//import StorytellerServer.StorytellerServer;
//import StorytellerServer.ide.IDEProxy;
//import StorytellerServer.playback.PlaybackProxy;
//
//
//public class TestTextEvents 
//{
//	private static String pathToServer = "127.0.0.1";
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
//	private static String testDbFileName = "unitTestDBForTestTextEvents" + SQLiteDatabase.DB_EXTENSION_NAME;
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
//	private static Logger logger = Logger.getLogger(TestTextEvents.class.getName());
//	private static Logger timer = Logger.getLogger("timing."+TestTextEvents.class.getName());
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
//		
//		MockPlaybackViewer.setDevFirstName("Mark");
//		MockPlaybackViewer.setDevLastName("Mahoney");
//		MockPlaybackViewer.setDevEmailAddress("mmahoney@carthage.edu");
//		mockIDE.createNewProject(testDbFileName, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, "TestTextEventsProject");
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
//		serverProxy.quit();
//
//		//give the threads a moment to die
//		try
//		{
//			while (playbackThread.isAlive()||ideThread.isAlive())
//			{
//				logger.debug("Waiting for ideThread and playbackThread to die");
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
//	@Test
//	public void testSimpleMessageNoDeletes()
//	{
//		timer.trace("Starting testSimpleMessageNoDeletes()");
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//
//		//name of a new document
//		String newDocName = "SimpleMessageNoDeletes.java";
//
//		timer.trace("sendCreateNewDocumentEventToServer");
//		//create a new document event in the root dir and send it to the server
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//		timer.trace("Finished sendCreateNewDocumentEventToServer");
//
//		//text to type into the new document
//		String testText = "The quick brown fox jumped over the lazy dog";
//
//		timer.trace("Sending "+testText);
//		//insert the characters from the passed in string starting at pos 0
//		mockIDE.sendStringToServer(testText, 0);
//		timer.trace("Sent "+testText);
//
//		//perform a playback and test that we have the data in the db
//		timer.trace("Starting playback");
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);
//		timer.trace("Playback recieved and rendered");
//
//		//make sure the playback viewer recognizes the name of the document
//		assertEquals(newDocName, playbackViewer.getDocumentName(newDocId));
//
//		//make sure the text from the keystrokes matches the text in the database
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));		
//	}
//
//	@Test
//	public void testEscapeCharacters()			//the goal for this function is to try and break anything that needs escaping.
//	{
//		timer.trace("Starting testEscapeCharacters()");
//		String newDocId = UUID.randomUUID().toString();
//
//		//name of a new document
//		String newDocName = "ComplicatedMessage.java";
//		timer.trace("sendCreateNewDocumentEventToServer");
//		//create a new document event in the root dir and send it to the server
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//		timer.trace("Finished sendCreateNewDocumentEventToServer");
//
//		String toughString = "{\\/?%%&(*&$##@)!~`\"123456''789iienslig?\'-drop>.0<,.\\.\\\'";
//
//		timer.trace("Sending "+toughString);
//		mockIDE.sendStringToServer(toughString, 0);
//		timer.trace("Sent "+toughString);
//
//		timer.trace("Starting playback");
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		timer.trace("Playback recieved and rendered");
//
//		//sanity check on the sender
//		assertEquals(toughString, playbackViewer.getDocumentText(newDocId));
//
//		//make sure the playback viewer recognizes the name of the document
//		assertEquals(newDocName, playbackViewer.getDocumentName(newDocId));
//
//		//make sure the text from the keystrokes matches the text in the database
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));		
//	}
//
//
//	@Test
//	public void testSimpleRandomAccessTyping()
//	{
//		timer.trace("Starting testSimpleRandomAccessTyping()");
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//
//		//create a new document
//		String newDocName = "SimpleRandomAccessTyping.java";
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//
//		//add some text
//		timer.trace("Sending Text");
//		mockIDE.sendStringToServer  ("The quick fox jumped over the lazy dog", 0);
//		timer.trace("Sent Text");
//
//		//go back and add a color in the middle of the text
//		timer.trace("Sending 3 deletes");
//		mockIDE.insertStringToServer("----------red ", "-", 0);
//		timer.trace("Sent 3 deletes");
//		//make sure the color was added
//		assertEquals("The quick red fox jumped over the lazy dog", mockIDE.getCurrentBuffer());
//
//
//		timer.trace("Starting playback");
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		timer.trace("Playback recieved and rendered");
//
//		//make sure the text from the keystrokes matches the text in the database
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));
//
//		assertEquals(newDocName, playbackViewer.getDocumentName(newDocId));
//	}
//
//	@Test
//	public void testComplicatedRandomAccessTyping()			//simulates writing the above method
//	{
//		timer.trace("Starting testComplicatedRandomAccessTyping()");
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//
//		//create a new document
//		String newDocName = "SimpleRandomAccessTyping.java";
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//
//		//start typing code
//		timer.trace("Sending Text");
//		mockIDE.sendStringToServer  ("public void testSimpleRandomAccessTyping()\n{\n\t\n}", 0);
//
//		mockIDE.insertStringToServer("_________________________________________________sendStringToServer(\"The quick" +
//				" fox jumped over the lazy dog\", 0);\n\t", "_", -3); //why a negative 3?  well, I added 3 too many '_' while lining up the this line 
//		//with the previous action because of the escaped characters above.  They only
//		//count as one character in length, but they take two to write here.  Since there are
//		//3 of them (\n \n and \t before the text I want to insert, I start at index -3
//		mockIDE.sendStringToServer("insertStringToServer(\"----------red \", '-', 0);", mockIDE.getCurrentLength()-2);  //insert this line right after the last stuff
//
//		assertEquals("public void testSimpleRandomAccessTyping()\n{\n\tsendStringToServer(\"The quick fox jumped over the lazy dog\", 0);\n\tinsertStringToServer(\"----------red \", '-', 0);\n}", mockIDE.getCurrentBuffer());
//
//		mockIDE.insertStringBeforeToServer("  ", "(\"The quick fox", 1);		//insert two spaces before this section of code, to even out the send and insert lines
//
//		mockIDE.sendStringToServer("\n\tSystem.out.println(toString());", mockIDE.getCurrentLength()-2);  //insert this line as the last line before the last \n and }
//
//		mockIDE.insertStringBeforeToServer("\n\t", "System.out", 1);		//add a new line before the System.out statement
//
//		//insert the assertEquals statement in the gap we just made
//		mockIDE.insertStringBeforeToServer("assertEquals(\"The quick red fox jumped over the lazy dog\", documentBuffers.get(currentDocumentId).toString());", "\n\tSystem.out",1);
//
//		timer.trace("Sent Text");
//
//		assertEquals("public void testSimpleRandomAccessTyping()\n" +
//				"{\n\t" +
//				"sendStringToServer  (\"The quick fox jumped over the lazy dog\", 0);\n\t"+
//				"insertStringToServer(\"----------red \", '-', 0);\n\t"+
//				"assertEquals(\"The quick red fox jumped over the lazy dog\", documentBuffers.get(currentDocumentId).toString());\n\t"+
//				"System.out.println(toString());\n" +
//				"}",
//				mockIDE.getCurrentBuffer());
//
//		//perform a playback and test that we have the data in the db
//		timer.trace("Starting playback");
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		timer.trace("Playback recieved and rendered");		
//
//		//make sure the text from the keystrokes matches the text in the database
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));
//
//		assertEquals(newDocName, playbackViewer.getDocumentName(newDocId));
//	}
//
//	@Test
//	public void testSimpleDeletes()
//	{
//		timer.trace("Starting testSimpleDeletes()");
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//		//create new document
//		String newDocName = "SimpleDeletes.java";
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//
//		timer.trace("Sending text and deletes");
//		mockIDE.sendStringToServer  ("The quick Brow",0);
//		mockIDE.sendDeletesToServer ("__________XXXX", 0);
//		assertEquals            ("The quick ", mockIDE.getCurrentBuffer());
//
//		mockIDE.insertStringToServer("__________brown fox jumpedd ", "_", 0);
//		mockIDE.sendDeletesToServer ("__________________________XX", 0);	
//		mockIDE.insertStringToServer("__________________________ over the lazy dog.", "_", 0);
//		timer.trace("Sent text and deletes");
//
//		assertEquals("The quick brown fox jumped over the lazy dog.",mockIDE.getCurrentBuffer());
//
//		timer.trace("Starting playback");
//		//Run Playback and test that the expected values are there
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);		
//		timer.trace("Playback recieved and rendered");	
//
//		//make sure the text from the keystrokes matches the text in the database
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));
//
//		assertEquals(newDocName, playbackViewer.getDocumentName(newDocId));
//	}
//
//	@Test
//	public void testTrickyDeletes()			//used to delete stuff at the beginning and end and see how the rendering holds up
//	{
//		timer.trace("Starting testTrickyDeletes()");
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//		//create new document
//		String newDocName = "TrickyDeletes.java";
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//
//		//Delete everything
//		timer.trace("Sending text and deletes");
//		mockIDE.sendStringToServer ("Hello ", 0);
//		mockIDE.sendDeletesToServer("XXXXXX", 0);
//		timer.trace("Sent text and deletes");
//
//		//check that the mockIDE is behaving
//		assertEquals("", mockIDE.getCurrentBuffer());
//		timer.trace("Starting playback");
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		timer.trace("Playback recieved and rendered");
//
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));
//
//		timer.trace("Sending \"Some bombastic sentance\"");
//		mockIDE.sendStringToServer("Some bombastic sentance", 0);
//		timer.trace("Sent that text");
//
//		timer.trace("Starting playback");
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		timer.trace("Playback recieved and rendered");
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));
//
//		//Delete first word and put something else there
//		timer.trace("Sending more text");
//		mockIDE.sendDeletesToServer("XXXX", 0);
//		mockIDE.sendStringToServer("A seriously", 0);
//		timer.trace("Sent some text");
//
//		assertEquals("A seriously bombastic sentance", mockIDE.getCurrentBuffer());
//		timer.trace("Starting playback");
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		timer.trace("Playback recieved and rendered");
//
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));
//
//		timer.trace("Sending some text intermixed with deletes");
//		mockIDE.findAndDeleteToServer("sentance", 1);
//		mockIDE.sendStringToServer("and less concise sentance than earlier", mockIDE.getCurrentBuffer().length());
//		timer.trace("Sent that text");
//
//		assertEquals("A seriously bombastic and less concise sentance than earlier", mockIDE.getCurrentBuffer());
//
//		timer.trace("Starting playback");
//		playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//		timer.trace("Playback recieved and rendered");
//
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));
//
//	}
//
//	@Test
//	public void testComplicatedInsertsAndDeletes()			//Walks through making a function from FakeTextEventSender
//	{
//		timer.trace("Starting testComplicatedInsertsAndDeletes()");
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//		//create a new document
//		String newDocName = "ComplicatedInsertsAndDeletes.java";
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, "ComplicatedInsertsAndDeletes.java", rootDirId);
//
//
//		//start writing the function
//		mockIDE.sendStringToServer("public void sendDeletesToServer(String message, int index)\n{\n\t\n}",0);
//
//		mockIDE.sendStringToServer("for(int i = 0;i<message.length();i++)\n\t{\n\t}", mockIDE.getCurrentLength()-2);
//
//		mockIDE.insertStringBeforeToServer("\n\t\t", "\n\t}",1);		//the user hit enter after the 2nd open curly brace
//
//		mockIDE.insertStringBeforeToServer("\n\t\t", "\n\t}",1);		//the user hit enter again
//
//		mockIDE.insertStringBeforeToServer("if (deleteMessage.charAt(i)!='_')","\n\t\t\n\t}",1);  	//the user goes up and then types the beginning of the if statement
//
//		int lastDelete = mockIDE.findAndDeleteToServer("!='_'",1);
//		mockIDE.sendStringToServer("=='X'", lastDelete);
//		mockIDE.insertStringAfterToServer("\n\t\t{\n\t\t\t\n\t\t}", "=='X')", 1);
//		mockIDE.insertStringAfterToServer("if (i+index<0||i+index", "{\n\t\t\t", 1);
//
//		mockIDE.insertStringBeforeToServer("int offsetIndex = i+index;\n\t\t\t", "if (", 2);	//the user decides to use another variable instead of i+index
//		lastDelete = mockIDE.findAndDeleteToServer("i+index", 2);			
//		mockIDE.sendStringToServer("offsetIndex", lastDelete);
//		lastDelete = mockIDE.findAndDeleteToServer("i+index", 2);			
//		mockIDE.sendStringToServer  ("offsetIndex", lastDelete);
//		mockIDE.insertStringToServer("___________>currentBuffer.length())\n\t\t\t{\n\t\t\t\t\n\t\t\t}", "_", lastDelete);
//
//		mockIDE.insertStringBeforeToServer("throw new RuntimeException(\"Illegal use of sendStringToServer.  \" +\n\t\t\t\t\t\t"+
//				"\"You tried to send text starting at a location outside what has been written.  \" +\n\t\t\t\t\t\t"+
//				"\"Current length of text is \"+currentBuffer.length()+\" and you tried writing at \"+index);" 
//				, "\n\t\t\t}", 1);		//TODO user copied and pasted the throw from elsewhere
//
//		lastDelete = mockIDE.findAndDeleteToServer("sendString", 1);
//		mockIDE.sendStringToServer("sendDeletes", lastDelete);
//
//		lastDelete = mockIDE.findAndDeleteToServer("text starting at", 1);
//		mockIDE.sendStringToServer  ("a deelt", lastDelete);		//random set of typos, just because
//		mockIDE.sendDeletesToServer ("____XXX", lastDelete);
//		mockIDE.insertStringToServer("____lete comand","_", lastDelete);
//		mockIDE.insertStringToServer("____________m___ to", "_", lastDelete);
//
//		mockIDE.insertStringAfterToServer("\n\t\t\tthis.sendDeleteToServer(offsetIndex);", "\n\t\t\t}", 1);
//
//		lastDelete = mockIDE.findAndDeleteToServer("writing", 1);
//		mockIDE.sendStringToServer("deleting", lastDelete);
//
//		assertEquals("public void sendDeletesToServer(String message, int index)\n{\n\tfor(int i = 0;i<message.length();i++)" +
//				"\n\t{\n\t\tif (deleteMessage.charAt(i)=='X')\n\t\t{\n\t\t\tint offsetIndex = i+index;" +
//				"\n\t\t\tif (offsetIndex<0||offsetIndex>currentBuffer.length())\n\t\t\t{\n\t\t\t\t" +
//				"throw new RuntimeException(\"Illegal use of sendDeletesToServer.  \" +\n\t\t\t\t\t\t\"You tried to send a " +
//				"delete command to a location outside what has been written.  \" +\n\t\t\t\t\t\t\"Current length of text is \"" +
//				"+currentBuffer.length()+\" and you tried deleting at \"+index);\n\t\t\t}\n\t\t\tthis.sendDeleteToServer(offsetIndex)" +
//				";\n\t\t}\n\t\t\n\t}\n}", mockIDE.getCurrentBuffer());
//
//
//		//Run Playback and test that the expected values are there
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//
//		//make sure the text from the keystrokes matches the text in the database
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));
//
//		assertEquals(newDocName, playbackViewer.getDocumentName(newDocId));
//
//	}
//	@Test
//	public void testEditingMultipleDocuments()			//creates a .h and .cpp for a Date class and writes them
//	{
//		//create a new dir id
//		String newDirectoryId = UUID.randomUUID().toString();
//
//		//create a new directory under the root
//		String newDirectoryName = "multiEditing";
//		mockIDE.sendCreateNewDirectoryEventToServer(newDirectoryId, newDirectoryName, rootDirId);
//
//		//IDE creates a unique id for the new document
//		String newHeaderId = UUID.randomUUID().toString();
//		//IDE creates a unique id for the new document
//		String newCPPId = UUID.randomUUID().toString();
//
//		//create two new documents
//		String newHeaderName = "Date.h";
//		mockIDE.sendCreateNewDocumentEventToServer(newHeaderId, newHeaderName, newDirectoryId);
//		String newCPPName = "Date.cpp";
//		mockIDE.sendCreateNewDocumentEventToServer(newCPPId, "Date.cpp", newDirectoryId);
//
//		//setCurrentPathOfDocument("Date.h","Testing/multiEditing/");
//		mockIDE.setCurrentDocumentId(newHeaderId);
//		mockIDE.sendStringToServer("#pragma once\nclass Date\n{\npublic:\n\tDate();\n\t~Date();\n\n\nprivate:\n\n\n}", 0); //computer generated Initial Header
//
//		mockIDE.setCurrentDocumentId(newCPPId);
//		//setCurrentPathOfDocument("Date.cpp","Testing/multiEditing/");
//		mockIDE.sendStringToServer("#include \"Date.h\"\n\nDate::Date()\n{\n\n}\n\nDate::~Date()\n{\n\n}", 0);			//computer generated initial .cpp
//
//		assertEquals("#include \"Date.h\"\n\nDate::Date()\n{\n\n}\n\nDate::~Date()\n{\n\n}", mockIDE.getCurrentBuffer());
//
//		mockIDE.setCurrentDocumentId(newHeaderId);
//		//setCurrentPathOfDocument("Date.h","Testing/multiEditing/");
//		assertEquals("#pragma once\nclass Date\n{\npublic:\n\tDate();\n\t~Date();\n\n\nprivate:\n\n\n}", mockIDE.getCurrentBuffer());
//
//		mockIDE.insertStringAfterToServer("\tint year;\n\tint month;\n\tint day;", "private:\n", 1);		//add some private members		
//		mockIDE.insertStringAfterToServer("int y, int m, int d", "Date(", 1);		//add the arguments for year, month and day
//
//		//setCurrentPathOfDocument("Date.cpp","Testing/multiEditing/");
//		mockIDE.setCurrentDocumentId(newCPPId);
//
//		mockIDE.insertStringAfterToServer("int y, int m, int d", "Date(", 1);		//edit the arguments in the cpp file
//		mockIDE.insertStringAfterToServer("\tyear = u", ")\n{\n", 1);
//		int lastDelete = mockIDE.findAndDeleteToServer("u", 2);						//don't forget that there's a u in "#include"
//		mockIDE.sendStringToServer("y;\n\tmonth = m;\n\tday = d;", lastDelete);
//
//		//setCurrentPathOfDocument("Date.h","Testing/multiEditing/");
//		mockIDE.setCurrentDocumentId(newHeaderId);
//
//		assertEquals("#pragma once\nclass Date\n{\npublic:\n\tDate(int y, int m, int d);\n\t~Date();\n\n\nprivate:\n\tint year;\n" +
//				"\tint month;\n\tint day;\n\n}", mockIDE.getCurrentBuffer());		//check that our edits didn't effect the header
//
//		mockIDE.insertStringAfterToServer("\tprint();", "~Date();\n", 1);			//add a print function to the header
//		mockIDE.insertStringAfterToServer("void ", "~Date();\n\t", 1);
//
//		//setCurrentPathOfDocument("Date.cpp","Testing/multiEditing/");
//		mockIDE.setCurrentDocumentId(newCPPId);
//
//		mockIDE.insertStringAfterToServer("\n\nDate::print()\n{\n\n}", "}", 2);
//
//		mockIDE.insertStringAfterToServer("\tcout<<m<<\"/\"<<d<<\"/\"<<y<<endl;", "print()\n{\n", 1);			//implement print()
//
//		mockIDE.insertStringAfterToServer("\n#include <iostream>\n\nusing namespace std;", "#include \"Date.h\"", 1);	//include stuff	
//
//		assertEquals("#include \"Date.h\"\n#include <iostream>\n\nusing namespace std;\n\nDate::Date(int y, int m, int d)" +
//				"\n{\n\tyear = y;\n\tmonth = m;\n\tday = d;\n}\n\nDate::~Date()\n{\n\n}\n\nDate::print()\n{\n\t" +
//				"cout<<m<<\"/\"<<d<<\"/\"<<y<<endl;\n}", mockIDE.getCurrentBuffer());		//check the final .cpp
//
//		//setCurrentPathOfDocument("Date.h","Testing/multiEditing/");
//		mockIDE.setCurrentDocumentId(newHeaderId);
//
//		assertEquals("#pragma once\nclass Date\n{\npublic:\n\tDate(int y, int m, int d);\n\t~Date();\n\tvoid print();" +
//				"\n\nprivate:\n\tint year;\n\tint month;\n\tint day;\n\n}", mockIDE.getCurrentBuffer());		//test the final .h
//
//
//		//run the playback and test
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//
//		//make sure the text from the keystrokes matches the text in the database
//		mockIDE.setCurrentDocumentId(newHeaderId);
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newHeaderId));
//
//		mockIDE.setCurrentDocumentId(newCPPId);
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newCPPId));
//
//
//		assertEquals(newHeaderName, playbackViewer.getDocumentName(newHeaderId));
//		assertEquals(newCPPName, playbackViewer.getDocumentName(newCPPId));
//		assertEquals(newDirectoryName, playbackViewer.getDirectoryName(newDirectoryId));
//	}
//
//
//	@Test
//	public void testLongMessageNoDeletes()			//sends a few paragraphs from Wikipedia.  Moved to the last test in this set because it slows down every other test
//	{
//		timer.trace("testLongMessageNoDeletes() is starting");
//		//IDE creates a unique id for the new document
//		String newDocId = UUID.randomUUID().toString();
//
//		//create a new document
//		String newDocName = "LongMessageNoDeletes.java";
//		mockIDE.sendCreateNewDocumentEventToServer(newDocId, newDocName, rootDirId);
//
//		timer.trace("Start part 1");
//		mockIDE.sendStringToServer("Revision control, also known as version control and source control (and an aspect of software configuration management)," +
//				" is the management of changes to documents, computer programs, large web sites, and other collections of information. " +
//				"Changes are usually identified by a number or letter code, termed the \"revision number\", \"revision level\", or simply \"revision\". " +
//				"For example, an initial set of files is \"revision 1\". When the first change is made, the resulting set is \"revision 2\", and so on. " +
//				"Each revision is associated with a timestamp and the person making the change. Revisions can be compared, restored, and with some types of " +
//				"files, merged.", 0);
//		timer.trace("Start part 2");
//		mockIDE.sendStringToServer("\nThe need for a logical way to organize and control revisions has existed for almost as long as writing has existed, " +
//				"but revision control became much more important, and complicated, when the era of computing began. The numbering of book editions and " +
//				"of specification revisions are examples that date back to the print-only era. Today, the most capable (as well as complex) revision " +
//				"control systems are those used in software development, where a team of people may change the same files.", mockIDE.getCurrentLength());
//		timer.trace("Start part 3");
//		assertEquals("Revision control, also known as version control and source control (and an aspect of software configuration management)," +
//				" is the management of changes to documents, computer programs, large web sites, and other collections of information. " +
//				"Changes are usually identified by a number or letter code, termed the \"revision number\", \"revision level\", or simply \"revision\". " +
//				"For example, an initial set of files is \"revision 1\". When the first change is made, the resulting set is \"revision 2\", and so on. " +
//				"Each revision is associated with a timestamp and the person making the change. Revisions can be compared, restored, and with some types of " +
//				"files, merged.\nThe need for a logical way to organize and control revisions has existed for almost as long as writing has existed, " +
//				"but revision control became much more important, and complicated, when the era of computing began. The numbering of book editions and " +
//				"of specification revisions are examples that date back to the print-only era. Today, the most capable (as well as complex) revision " +
//				"control systems are those used in software development, where a team of people may change the same files.", mockIDE.getCurrentBuffer());
//		timer.trace("Done");
//		//perform a playback and test that we have the data in the db
//
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);		
//
//		//make sure the text from the keystrokes matches the text in the database
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(newDocId));
//
//
//	}
//
//
//
//
//
//
//}
