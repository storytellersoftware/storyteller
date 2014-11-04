//package unitTests;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.File;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.json.JSONObject;
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
//import StorytellerServer.json.JSONConstants;
//import StorytellerServer.json.JSONConstants;
//import StorytellerServer.playback.PlaybackFilter;
//import StorytellerServer.playback.PlaybackProxy;
//
//
//public class TestFilters
//{
//
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
//	private static String testDbFileName = "unitTestDBForTestFilters" + SQLiteDatabase.DB_EXTENSION_NAME;
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
//	private static String filterDir;
//
//	private static String headerId;
//
//	private static String cppId;
//
//	private static Logger logger = Logger.getLogger(TestFilters.class.getName());
//
//	private static String markOnlyDevGroupId = null;
//
//	private static String markAndKevinDevGroupId = null;
//
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
//		mockIDE.createNewProject(testDbFileName, MockPlaybackViewer.getDevFirstName(), MockPlaybackViewer.getDevLastName(), MockPlaybackViewer.getDevEmail(), rootDirId, TestFilters.class.getName()+"Project");
//
//		markOnlyDevGroupId = mockIDE.getCurrentDevGroupID();
//
//		mockIDE.enableDateFaking();		//let's fake in real time
//
//		setUpPlaybackBeforeFiltering();
//	}
//
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
//
//	}
//
//	private static void setUpPlaybackBeforeFiltering() throws Exception
//	{
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
//		mockIDE.sendCreateNewDocumentEventToServer(cppId, "Date.cpp", filterDir);
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
//		mockIDE.delayTime(1000);			//a second to switch back to the header
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
//		mockIDE.delayTime(26*60*60*1000);		//delay for 26 hours
//
//		mockIDE.changeDevelopers(new String[]{"Kevin", "Mark"}, new String[]{"Lubick", "Mahoney"}, new String[]{"klubick@carthage.edu", "mmahoney@carthage.edu"});
//
//		markAndKevinDevGroupId = mockIDE.getCurrentDevGroupID();
//
//		mockIDE.insertStringAfterToServer("\"Today is: \"<<", "\tcout<<", 1);		//The next day, the developer wants to modify the print function
//
//		//run the playback and test for sanity
//		PlaybackEventRenderer playbackViewer = MockPlaybackViewer.playBackAll(pathToServer);	
//
//		//make sure the text from the keystrokes matches the text in the database
//		mockIDE.setCurrentDocumentId(headerId);
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(headerId));
//
//		mockIDE.setCurrentDocumentId(cppId);
//		assertEquals(mockIDE.getCurrentBuffer(), playbackViewer.getDocumentText(cppId));
//
//
//		assertEquals(newHeaderName, playbackViewer.getDocumentName(headerId));
//		assertEquals(newCPPName, playbackViewer.getDocumentName(cppId));
//		assertEquals(newDirectoryName, playbackViewer.getDirectoryName(filterDir));
//
//	}
//
//	@Test
//	public void testStartTimeFilter() throws Exception
//	{
//		int numSecondsToAdvanceFilter = 10;
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.prepForFilteringAllEvents(pathToServer);
//
//		PlaybackFilter currentFilter = viewer.getFilter();
//		logger.trace("Filter at the start of StartTimeFilter()" +currentFilter.toString());
//
//		Date startDate = new Date(currentFilter.getStartTime());
//
//		Date modifiedStartDate = new Date(startDate.getTime()+numSecondsToAdvanceFilter*1000);		//10 seconds in from beginning
//
//		//PlaybackFilter newFilter = new PlaybackFilter(modifiedStartDate.getTime(), currentFilter.getEndTime(), currentFilter.getDocs(),
//		//		currentFilter.getDevGroups(), false, false, 0, false, 0);
//		JSONObject filterJSON = new JSONObject();
//		filterJSON.append(JSONConstants.NODE_ID, currentFilter.getNodeID());
//		filterJSON.append(JSONConstants.START_TIME, modifiedStartDate.getTime());
//		filterJSON.append(JSONConstants.END_TIME, currentFilter.getEndTime());
//		filterJSON.append(JSONConstants.DOCUMENT_IDS, currentFilter.getDocumentIDs());
//		filterJSON.append(JSONConstants.DEVELOPER_GROUP_IDS, currentFilter.getDeveloperGroupIDs());
//		filterJSON.append(JSONConstants.SHOW_ONLY_DELETES, false);
//		filterJSON.append(JSONConstants.SHOW_ONLY_END_RESULT, false);
//		filterJSON.append(JSONConstants.SHOW_PASTE_ORIGIN, false);
//		filterJSON.append(JSONConstants.HIDE_DELETES, 0);
//		filterJSON.append(JSONConstants.RELEVANT_BLOCK_TYPE, PlaybackFilter.CHARACTER_BLOCK_TYPE);
//		
//		PlaybackFilter newFilter = new PlaybackFilter(filterJSON);
//		
//		viewer.setFilter(newFilter);
//		viewer.getEventsFromServer();
//
//		viewer.playbackAllEvents();
//
//		List<JSONObject> events = viewer.getEventsToRender();
//		for (JSONObject jsonObject : events)
//		{
//			Date thisTime = new Date(jsonObject.getLong(JSONConstants.TIMESTAMP));
//			if ((thisTime.getTime()-startDate.getTime())<numSecondsToAdvanceFilter*1000)
//			{
//				assertEquals(PlaybackFilter.IRRELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//			else
//			{
//				assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//		}
//		//make sure the text from the keystrokes matches the text in the database
//		mockIDE.setCurrentDocumentId(headerId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(headerId));
//
//		mockIDE.setCurrentDocumentId(cppId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(cppId));
//	}
//
//
//	/*
//	@Test
//	public void testEndTimeFilter() throws Exception
//	{
//		int numSecondsToRetreateFilter = 12*60*60;		//go back 12 hours from the end
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.prepForFilteringAllEvents(pathToServer);
//
//		PlaybackFilter currentFilter = viewer.getFilter();
//
//		logger.trace("Filter at the start of EndTimeFilter()" +currentFilter.toString());
//
//		Date endDate = currentFilter.getEndTimeAsDate();
//
//		Date modifiedEndDate = new Date(endDate.getTime()-(numSecondsToRetreateFilter*1000));		//12 hours before the ending
//
//		PlaybackFilter newFilter = new PlaybackFilter(currentFilter.getStartTime(), modifiedEndDate.getTime(), currentFilter.getDocs(),
//				currentFilter.getDevGroups(), false, false, 0, false, 0);
//		//System.err.println(newFilter.toString());
//		viewer.setFilter(newFilter);
//		viewer.getEventsFromServer();
//
//		viewer.playbackAllEvents();
//
//		List<JSONObject> events = viewer.getEventsToRender();
//		for (JSONObject jsonObject : events)
//		{
//			Date thisTime = new Date(jsonObject.getLong(JSONConstants.TIMESTAMP));
//			if ((endDate.getTime()-thisTime.getTime())<=(numSecondsToRetreateFilter*1000))
//			{
//				assertEquals(PlaybackFilter.UNNEEDED, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//			else
//			{
//				assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//		}
//		//The header should look the same
//		mockIDE.setCurrentDocumentId(headerId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(headerId));
//
//		//The cpp file should not have the "Today is", and thus, is different than the mockIDE
//		assertEquals("#include \"Date.h\"\n#include <iostream>\n\nusing namespace std;\n\nDate::Date(int y, int m, int d)\n{\n\tyear = y;\n\tmonth = m;\n" +
//				"\tday = d;\n}\n\nDate::~Date()\n{\n\n}\n\nDate::print()\n{\n\tcout<<month<<\"/\"<<day<<\"/\"<<year<<endl;\n}", viewer.getDocumentText(cppId));
//
//	}
//	*/
//	
//	/*
//	@Test
//	public void testBothTimeFilters() throws Exception
//	{
//		int numSecondsToAdvanceFilter = 20;				//start 20 seconds after the beginning
//		int numSecondsToRetreateFilter = 1*60*60;		//go back 1 hour from the end
//
//		MockPlaybackViewer viewer = MockPlaybackViewer.prepForFilteringAllEvents(pathToServer);
//
//		PlaybackFilter currentFilter = viewer.getFilter();
//
//		Date startDate = currentFilter.getStartTimeAsDate();
//
//		Date modifiedStartDate = new Date(startDate.getTime()+numSecondsToAdvanceFilter*1000);		//create a new date to represent the begin time
//
//		Date endDate = currentFilter.getEndTimeAsDate();
//
//		Date modifiedEndDate = new Date(endDate.getTime()-numSecondsToRetreateFilter*1000);		//create a new date to represent 12 hours before the ending
//
//		PlaybackFilter newFilter = new PlaybackFilter(modifiedStartDate.getTime(), modifiedEndDate.getTime(), currentFilter.getDocs(),
//				currentFilter.getDevGroups(), false, false, 0, false, 0);
//		viewer.setFilter(newFilter);
//		viewer.getEventsFromServer();
//
//		viewer.playbackAllEvents();
//
//		List<JSONObject> events = viewer.getEventsToRender();
//		for (JSONObject jsonObject : events)
//		{
//			Date thisTime = new Date(jsonObject.getLong(JSONConstants.TIMESTAMP));
//			if ((thisTime.getTime()-startDate.getTime())<numSecondsToAdvanceFilter*1000)		//check that anything before start Time is irrelevant
//			{
//				assertEquals(PlaybackFilter.IRRELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//			else if ((endDate.getTime()-thisTime.getTime())<numSecondsToRetreateFilter*1000)	//check that anything after end time is unnneeded
//			{
//				assertEquals(PlaybackFilter.UNNEEDED, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//			else
//			{
//				assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//		}
//		//make sure the text from the keystrokes matches the text in the database
//		mockIDE.setCurrentDocumentId(headerId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(headerId));
//
//		//The cpp file should not have the "Today is", and thus, is different than the mockIDE
//		assertEquals("#include \"Date.h\"\n#include <iostream>\n\nusing namespace std;\n\nDate::Date(int y, int m, int d)\n{\n\tyear = y;\n\tmonth = m;\n" +
//				"\tday = d;\n}\n\nDate::~Date()\n{\n\n}\n\nDate::print()\n{\n\tcout<<month<<\"/\"<<day<<\"/\"<<year<<endl;\n}", viewer.getDocumentText(cppId));
//
//	}
//	*/
//
//	/*
//	@Test
//	public void testDocFilter() throws Exception			//filters out just the header, and then just the cpp 
//	{
//		MockPlaybackViewer viewerOne = MockPlaybackViewer.prepForFilteringAllEvents(pathToServer);
//
//		PlaybackFilter currentFilter = viewerOne.getFilter();
//
//		List<JSONObject> listOfDocs = new ArrayList<JSONObject>();
//		JSONObject jobj = new JSONObject();
//		jobj.put(DOCUMENT_ID, headerId);
//		listOfDocs.add(jobj);
//
//		PlaybackFilter newFilter = new PlaybackFilter(currentFilter.getStartTime(), currentFilter.getEndTime(), listOfDocs,
//				currentFilter.getDevGroups(), false, false, 0, false, 0);
//
//		viewerOne.setFilter(newFilter);
//
//		viewerOne.getEventsFromServer();
//
//		viewerOne.playbackAllEvents();
//
//		List<JSONObject> events = viewerOne.getEventsToRender();
//		for (JSONObject jsonObject : events)
//		{
//			if (!jsonObject.has(DOCUMENT_ID)||jsonObject.getString(DOCUMENT_ID).equals(headerId))		//If it's related to this document or is document agnostic, it is relevant
//			{
//				assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//			else
//			{
//				assertEquals(PlaybackFilter.UNNEEDED, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//		}
//		assertEquals(1, viewerOne.getAllDocumentNames(filterDir).size());		//there should only be one document
//
//		MockPlaybackViewer viewerTwo = MockPlaybackViewer.prepForFilteringAllEvents(pathToServer);
//
//		currentFilter = viewerTwo.getFilter();
//
//		listOfDocs = new ArrayList<JSONObject>();
//		jobj = new JSONObject();
//		jobj.put(DOCUMENT_ID, cppId);
//		listOfDocs.add(jobj);
//
//		newFilter = new PlaybackFilter(currentFilter.getStartTime(), currentFilter.getEndTime(), listOfDocs,
//				currentFilter.getDevGroups(), false, false, 0, false, 0);
//
//		viewerTwo.setFilter(newFilter);
//
//		viewerTwo.getEventsFromServer();
//
//		viewerTwo.playbackAllEvents();
//
//		events = viewerTwo.getEventsToRender();
//		for (JSONObject jsonObject : events)
//		{
//			if (!jsonObject.has(DOCUMENT_ID)||jsonObject.getString(DOCUMENT_ID).equals(cppId))		//If it's related to this document or is document agnostic, it is relevant
//			{
//				assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//			else
//			{
//				assertEquals(PlaybackFilter.UNNEEDED, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//		}
//		assertEquals(1, viewerTwo.getAllDocumentNames(filterDir).size());		//there should only be one document
//
//		//Make sure that the two different filters got different things
//		assertEquals(false, viewerOne.getAllDocumentNames(filterDir).get(0).equals(viewerTwo.getAllDocumentNames(filterDir).get(0)));
//	}
//	*/
//
//	/*
//	@Test
//	public void testIgnoreDeletedInsertEventsFilter() throws Exception
//	{
//		int typoTime= 5;
//		MockPlaybackViewer viewer = MockPlaybackViewer.prepForFilteringAllEvents(pathToServer);
//
//		PlaybackFilter currentFilter = viewer.getFilter();
//
//		PlaybackFilter newFilter = new PlaybackFilter(currentFilter.getStartTime(), currentFilter.getEndTime(), currentFilter.getDocs(),
//				currentFilter.getDevGroups(), false, false, typoTime, false, 0);
//
//		viewer.setFilter(newFilter);
//		viewer.getEventsFromServer();
//
//		viewer.playbackAllEvents();
//
//		List<JSONObject> events = viewer.getEventsToRender();
//		for (JSONObject jsonObject : events)
//		{
//			String type = jsonObject.getString(TYPE);
//			if (type.equals(InsertEvent.INSERT_EVENT_TYPE))
//			{
//				if (jsonObject.getLong(DELETED_AT_TIMESTAMP)==0)
//				{	//undeleted events are always relevant here
//					assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//				}
//				else
//				{
//					Date createdTime = new Date(jsonObject.getLong(TIMESTAMP));
//					Date deletedTime = new Date(jsonObject.getLong(DELETED_AT_TIMESTAMP));
//					if ((deletedTime.getTime()-createdTime.getTime())<typoTime*1000)
//					{
//						assertEquals(PlaybackFilter.UNNEEDED, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//					}
//					else
//					{	//this was not deleted with in the given timeframe, so it should be relevant
//						assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//					}
//				}
//			}
//			else if (type.equals(DeleteEvent.DELETE_EVENT_TYPE))
//			{
//				JSONObject correspondingInsertEvent = viewer.getJSONObjectByEventID(jsonObject.getString(PREVIOUS_NEIGHBOR_ID));	//go look up the corresponding Insert Event, using this delete's previous neighbor
//
//				//If the insert Event was relevent, so should the delete event.  if it was unneeded, so too shall the delete event
//				assertEquals(jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE), correspondingInsertEvent.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//			else
//			{	//Nontext events are always relevant here
//				assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//		}
//		//make sure the text from the keystrokes matches the text in the database
//		mockIDE.setCurrentDocumentId(headerId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(headerId));
//
//		mockIDE.setCurrentDocumentId(cppId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(cppId));
//
//
//	}
//	*/
//
//	/*
//	@Test
//	public void testIgnoreAllDeletedInsertEventsFilter() throws Exception
//	{
//		int typoTime= -1;
//		MockPlaybackViewer viewer = MockPlaybackViewer.prepForFilteringAllEvents(pathToServer);
//
//		PlaybackFilter currentFilter = viewer.getFilter();
//
//		PlaybackFilter newFilter = new PlaybackFilter(currentFilter.getStartTime(), currentFilter.getEndTime(), currentFilter.getDocs(),
//				currentFilter.getDevGroups(), false, false, typoTime, false, 0);
//
//		viewer.setFilter(newFilter);
//		viewer.getEventsFromServer();
//
//		viewer.playbackAllEvents();
//
//		List<JSONObject> events = viewer.getEventsToRender();
//		for (JSONObject jsonObject : events)
//		{
//			String type = jsonObject.getString(TYPE);
//			if (type.equals(InsertEvent.INSERT_EVENT_TYPE))
//			{
//				if (jsonObject.getLong(DELETED_AT_TIMESTAMP)==0)
//				{	//undeleted events are always relevant here
//					assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//				}
//				else
//				{
//					//All deletes are unneccessary
//					assertEquals(PlaybackFilter.UNNEEDED, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//
//				}
//			}
//			else if (type.equals(DeleteEvent.DELETE_EVENT_TYPE))
//			{
//				JSONObject correspondingInsertEvent = viewer.getJSONObjectByEventID(jsonObject.getString(PREVIOUS_NEIGHBOR_ID));	//go look up the corresponding Insert Event, using this delete's previous neighbor
//
//				//If the insert Event was relevent, so should the delete event.  if it was unneeded, so too shall the delete event
//				assertEquals(jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE), correspondingInsertEvent.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//			else
//			{	//Nontext events are always relevant here
//				assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//		}
//		//make sure the text from the keystrokes matches the text in the database
//		mockIDE.setCurrentDocumentId(headerId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(headerId));
//
//		mockIDE.setCurrentDocumentId(cppId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(cppId));
//	}
//	*/
//
//	/*
//	@Test
//	public void testOnlyWordsFilter() throws Exception
//	{
//		MockPlaybackViewer viewer = MockPlaybackViewer.prepForFilteringAllEvents(pathToServer);
//
//		PlaybackFilter currentFilter = viewer.getFilter();
//
//		PlaybackFilter newFilter = new PlaybackFilter(currentFilter.getStartTime(), currentFilter.getEndTime(), currentFilter.getDocs(),
//				currentFilter.getDevGroups(), false, false, 0, false, PlaybackFilter.SHOW_WORDS);
//
//		viewer.setFilter(newFilter);
//		viewer.getEventsFromServer();
//
//		viewer.playbackAllEvents();
//
//		List<JSONObject> events = viewer.getEventsToRender();
//		for (JSONObject jsonObject : events)
//		{
//			String type = jsonObject.getString(TYPE);
//			if (type.equals(InsertEvent.INSERT_EVENT_TYPE))
//			{
//				String eventData = jsonObject.getString(EVENT_DATA);
//				if (eventData.equals(" ")||eventData.equals("\t")||eventData.equals("\n"))
//				{
//					assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//				}
//				else
//				{	//We don't want to see it if it's a text event that isn't white space
//					assertEquals(PlaybackFilter.IRRELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//				}
//			}
//			else
//			{
//				assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//
//		}
//		//make sure the text from the keystrokes matches the text in the database
//		mockIDE.setCurrentDocumentId(headerId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(headerId));
//
//		mockIDE.setCurrentDocumentId(cppId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(cppId));
//
//	}
//	*/
//
//	/*
//	@Test
//	public void testOnlyLinesFilter() throws Exception
//	{
//		MockPlaybackViewer viewer = MockPlaybackViewer.prepForFilteringAllEvents(pathToServer);
//
//		PlaybackFilter currentFilter = viewer.getFilter();
//
//		PlaybackFilter newFilter = new PlaybackFilter(currentFilter.getStartTime(), currentFilter.getEndTime(), currentFilter.getDocs(),
//				currentFilter.getDevGroups(), false, false, 0, false, PlaybackFilter.SHOW_LINES);
//
//		viewer.setFilter(newFilter);
//		viewer.getEventsFromServer();
//
//		viewer.playbackAllEvents();
//
//		List<JSONObject> events = viewer.getEventsToRender();
//		for (JSONObject jsonObject : events)
//		{
//			String type = jsonObject.getString(TYPE);
//			if (type.equals(InsertEvent.INSERT_EVENT_TYPE))
//			{
//				String eventData = jsonObject.getString(EVENT_DATA);
//				if (eventData.equals("\n"))
//				{
//					assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//				}
//				else
//				{	//We don't want to see it if it's a text event that isn't white space
//					assertEquals(PlaybackFilter.IRRELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//				}
//			}
//			else
//			{
//				assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//
//		}
//		//make sure the text from the keystrokes matches the text in the database
//		mockIDE.setCurrentDocumentId(headerId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(headerId));
//
//		mockIDE.setCurrentDocumentId(cppId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(cppId));
//
//	}
//	*/
//
//	/*
//	@Test
//	public void testDeveloperFilters() throws Exception
//	{
//		MockPlaybackViewer viewer = MockPlaybackViewer.prepForFilteringAllEvents(pathToServer);
//
//		PlaybackFilter currentFilter = viewer.getFilter();
//
//		List<JSONObject> listOfDevGroup = new ArrayList<JSONObject>();
//		JSONObject devJsonObject = new JSONObject();
//		devJsonObject.put(DEVELOPER_GROUP_ID, markOnlyDevGroupId);
//
//		listOfDevGroup.add(devJsonObject);
//		
//		PlaybackFilter newFilter = new PlaybackFilter(currentFilter.getStartTime(), currentFilter.getEndTime(), currentFilter.getDocs(),
//				listOfDevGroup, false, false, 0, false, 0);
//
//		viewer.setFilter(newFilter);
//		viewer.getEventsFromServer();
//
//		viewer.playbackAllEvents();
//
//		List<JSONObject> events = viewer.getEventsToRender();
//		for (JSONObject jsonObject : events)
//		{
//			String devGroupId = jsonObject.getString(DEVELOPER_GROUP_ID);
//			logger.trace(jsonObject);
//			if (devGroupId.equals(markOnlyDevGroupId))
//			{
//				assertEquals(PlaybackFilter.RELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//			else
//			{
//				assertEquals(PlaybackFilter.IRRELEVANT, jsonObject.getString(MockPlaybackViewer.PLAYBACK_RELEVANCE));
//			}
//		}
//
//		//make sure the text from the keystrokes matches the text in the database
//		mockIDE.setCurrentDocumentId(headerId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(headerId));
//
//		mockIDE.setCurrentDocumentId(cppId);
//		assertEquals(mockIDE.getCurrentBuffer(), viewer.getDocumentText(cppId));
//	}
//	*/
//}
