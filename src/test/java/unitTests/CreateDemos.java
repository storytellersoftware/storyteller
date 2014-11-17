//package unitTests;
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
//import StorytellerServer.DBFactory;
//import StorytellerServer.SQLiteDBFactory;
//import StorytellerServer.SQLiteDatabase;
//import StorytellerServer.StorytellerServer;
//import StorytellerServer.ide.IDEProxy;
//import StorytellerServer.playback.PlaybackProxy;
//
//public class CreateDemos
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
//	private static String testDbFileName = "exampleSix" + SQLiteDatabase.DB_EXTENSION_NAME;
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
//	private static Logger logger = Logger.getLogger(CreateDemos.class.getName());
//
//	private static String filterDir;
//
//	private static String headerId;
//
//	private static String cppId;
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
//		mockIDE.enableDateFaking("15:30:12 15/10/2012");
//		MockPlaybackViewer.setDevFirstName("Kevin");
//		MockPlaybackViewer.setDevLastName("Lubick");
//		MockPlaybackViewer.setDevEmailAddress("klubick@carthage.edu");
//
//}
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
//	public void testEditingMultipleDocuments() throws Exception			//creates a .h and .cpp for a Date class and writes them
//	{
//		mockIDE.createNewProject(testDbFileName, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId,"DemoProject");		
//		
//		//create a new dir id
//		filterDir = UUID.randomUUID().toString();
//
//		//create a new directory under the root
//		String newDirectoryName = "multiEditingFilters";
//		mockIDE.sendCreateNewDirectoryEventToServer(filterDir, newDirectoryName, rootDirId);
//
//		//IDE creates a unique id for the new document
//		headerId = UUID.randomUUID().toString();
//		//IDE creates a unique id for the new document
//		cppId = UUID.randomUUID().toString();
//
//		//create two new documents
//		String newHeaderName = "Date.h";
//		mockIDE.sendCreateNewDocumentEventToServer(headerId, newHeaderName, filterDir);
//		String newCPPName = "Date.cpp";
//		mockIDE.sendCreateNewDocumentEventToServer(cppId, newCPPName, filterDir);
//
//		mockIDE.setCurrentDocumentId(headerId);
//		mockIDE.sendStringToServer("#pragma once\nclass Date\n{\npublic:\n\tDate();\n\t~Date();\n\n\nprivate:\n\n\n}", 0); //computer generated Initial Header
//
//		mockIDE.setCurrentDocumentId(cppId);
//
//		mockIDE.sendStringToServer("#include \"Date.h\"\n\nDate::Date()\n{\n\n}\n\nDate::~Date()\n{\n\n}", 0);			//computer generated initial .cpp
//
//		mockIDE.setCurrentDocumentId(headerId);
//
//		mockIDE.setHumanTyping(true);
//
//		mockIDE.insertStringAfterToServer("\tint year;\n\tint month;\n\tint day;", "private:\n", 1);		//add some private members		
//		mockIDE.insertStringAfterToServer("int y, int m, int d", "Date(", 1);		//add the arguments for year, month and day
//
//		mockIDE.setCurrentDocumentId(cppId);
//
//		mockIDE.delayTime(2000);		//it takes  2 seconds to hit control c and then switch documents, and then control v
//
//		mockIDE.setHumanTyping(false);		//we are pasting, which is at computer speed
//
//		int lastDelete= mockIDE.findAndDeleteToServer("Date()", 1);		//replacing the Date() with the Date(int y, int m, int d) from the .h
//		mockIDE.copyAndPasteToServer("Date(int y, int m, int d)", headerId, 1, cppId, lastDelete);
//
//		mockIDE.delayTime(250);  		//a quarter of a second to locate the pointer to start typing
//		mockIDE.setHumanTyping(true);
//		mockIDE.insertStringAfterToServer("\tyear = u", ")\n{\n", 1);
//		lastDelete = mockIDE.findAndDeleteToServer("u", 2);						//don't forget that there's a u in "#include", so second occurrence to get rid of the typo
//		mockIDE.sendStringToServer("y;\n\tmonth = m;\n\tday = d;", lastDelete);
//
//		mockIDE.setCurrentDocumentId(headerId);
//
//		mockIDE.delayTime(60*1000);			//sixty seconds to switch back to the header and have another developer log in
//		
//		
//		
//		mockIDE.changeDeveloper("Mark", "Mahoney","mmahoney@carthage.edu");
//
//		mockIDE.insertStringAfterToServer("\tprint();", "~Date();\n", 1);			//add a print function to the header
//		mockIDE.delayTime(500);		//half a second to get the cursor located correctly
//		mockIDE.insertStringAfterToServer("void ", "~Date();\n\t", 1);
//
//		mockIDE.setCurrentDocumentId(cppId);
//
//		mockIDE.delayTime(2000);		//it takes  2 seconds to hit control c and then switch documents, and then control v
//
//		mockIDE.setHumanTyping(false);		//we are pasting, which is at computer speed
//		int location = mockIDE.findStringInCurrentBuffer("}", 2, false);		//go to after the destructor
//		mockIDE.copyAndPasteToServer("print()", headerId, 1, cppId, location);
//		mockIDE.setHumanTyping(true);
//
//		mockIDE.insertStringAfterToServer("\n{\n\n}", "print()", 1);
//		mockIDE.delayTime(500);		//half a second to get the cursor located correctly
//		mockIDE.insertStringBeforeToServer("\n\nDate::", "print()", 1);
//		mockIDE.delayTime(500);		//half a second to get the cursor located correctly
//		mockIDE.insertStringAfterToServer("\tcout<<month<<\"/\"<<day<<\"/\"<<year<<endl;", "print()\n{\n", 1);			//implement print()
//
//		mockIDE.delayTime(1000);		//a second to get the cursor located correctly
//		mockIDE.insertStringAfterToServer("\n#include <iostream>\n\nusing namespace std;", "#include \"Date.h\"", 1);	//include stuff	
//
//		mockIDE.delayTime(24*60*60*1000);		//delay for 26 hours
//
//		mockIDE.insertStringAfterToServer("\"Today is: \"<<", "\tcout<<", 1);		//The next day, the developer wants to modify the print function
//
//
//		//delay for an hour
//		mockIDE.delayTime(60*60*1000);
//		
//		mockIDE.changeDevelopers(new String[]{"klubick@carthage.edu","mmahoney@carthage.edu"});
//		
//		mockIDE.setCurrentDocumentId(headerId);
//		
//		mockIDE.insertStringAfterToServer("Time", "Date", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("Time", "Date", 2);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("Time", "Date", 3);
//		mockIDE.delayTime(1000);
//		mockIDE.sendRenameDocumentEventToServer(headerId, newHeaderName, "DateTime.h", rootDirId);
//		
//		
//		int lastInsert = mockIDE.insertStringAfterToServer(", inthou", " int d" , 1);
//		mockIDE.delayTime(1000);
//		mockIDE.sendDeletesToServer("XXX", lastInsert-3);
//		mockIDE.delayTime(1000);
//		mockIDE.sendStringToServer(" hour , int min, int second", lastInsert-3 );
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("ear", "int y", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("onth", "int m", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("ay", "int d", 1);
//		mockIDE.delayTime(1000);
//		
//		
//		mockIDE.delayTime(2000);
//		mockIDE.setCurrentDocumentId(cppId);
//		mockIDE.sendRenameDocumentEventToServer(cppId, newCPPName, "DateTime.cpp", rootDirId);
//		mockIDE.insertStringAfterToServer("Time", "Date", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("Time", "Date", 2);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("Time", "Date", 3);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("Time", "Date", 4);
//		mockIDE.delayTime(1111);
//		mockIDE.insertStringAfterToServer("Time", "Date", 5);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("Time", "Date", 6);
//		mockIDE.delayTime(1000);
//		
//		mockIDE.insertStringAfterToServer(", int hour, int min, int second", "int d", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("ear", "int y", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("onth", "int m", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("ay", "int d", 1);
//		mockIDE.delayTime(2000);
//		
//		mockIDE.insertStringBeforeToServer("this->", "year", 2);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringBeforeToServer("this->", "month", 2);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringBeforeToServer("this->", "day", 2);
//		mockIDE.delayTime(2000);
//		
//		mockIDE.insertStringAfterToServer("ear", "this->year = y", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("onth", "this->month = m", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("ay", "this->day = d", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("\n\n\tthis->hour = hour;\n\tthis->minute = minute;\n\tthis->second = second;", "this->day = day;", 1);
//		mockIDE.delayTime(1000);
//		
//		mockIDE.setCurrentDocumentId(headerId);
//		
//		
//		mockIDE.insertStringAfterToServer("\n\tint hour;\n\tint minute;\n\tint second;", "int day;", 1);
//		mockIDE.delayTime(1000);
//		
//		mockIDE.setCurrentDocumentId(cppId);
//		mockIDE.delayTime(10000);
//		
////		mockIDE.insertStringAfterToServer("onth", "<<m", 1);
////		mockIDE.delayTime(1000);
////		mockIDE.insertStringAfterToServer("ay", "<<d", 1);
////		mockIDE.delayTime(1000);
////		mockIDE.insertStringAfterToServer("ear", "<<y", 1);
//		mockIDE.delayTime(1000);
//		mockIDE.insertStringAfterToServer("<<hour<<\":\"<<minute<<\":\"<<second<<\" \"","Today is: \"", 1);
//		
//		mockIDE.findAndDeleteToServer("Today ", 1);
//		mockIDE.insertStringAfterToServer("It ","cout<<\"", 1);
//		mockIDE.insertStringAfterToServer(" now","It is", 1);
//		logger.info(mockIDE.getCurrentBuffer());
//		
//		
//	}
//
//}
