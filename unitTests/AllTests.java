package unitTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
//		TestStorytellerServer.class,
//		TestDatabase.class,
//		TestDocumentBuffers.class,
//		TestTextEvents.class,
// TestPasteEvents.class,
// TestFileAndDirectoryEvents.class,
// TestNodes.class,
// TestFilters.class,
// TestMergingPartOne.class,
// TestMergingPartTwo.class,
// TestMergingPartThree.class,
// TestMergingPartFour.class,
// TestSelectedText.class,
// TestDevelopers.class
})
public class AllTests
{
	// for unit tests, the logger's settings will be found in this file
	// unit tests should call PropertyConfigurator.configure(AllTests.LOGGING_FILE_PATH); in the static initializer
	public static final String LOGGING_FILE_PATH = "./log4j.settings";
}
