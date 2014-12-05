//package unitTests;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.fail;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import org.apache.log4j.Logger;
//import org.apache.log4j.PropertyConfigurator;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import StorytellerEntities.StorytellerEvent;
//import StorytellerServer.Utilities;
//import StorytellerServer.ide.DocumentBuffer;
//
//
//public class TestDocumentBuffers
//{
//	private static Logger logger = Logger.getLogger(TestDocumentBuffers.class.getName());
//	private final Random random = new Random();
//
//
//	@BeforeClass
//	public static void setupClass()
//	{
//		PropertyConfigurator.configure(AllTests.LOGGING_FILE_PATH);
//	}
//
//
//	@Test
//	public void testDocBufferAddingTextWithIndices()
//	{
//		DocumentBuffer documentBuffer = new DocumentBuffer();
//		assertEquals(null, documentBuffer.addTextEvent("1", 0, "A"));
//		assertEquals("1", documentBuffer.addTextEvent("2", 1, "B"));
//		assertEquals("2", documentBuffer.addTextEvent("3", 2, "C"));
//		assertEquals("3", documentBuffer.addTextEvent("4", 3, "D"));
//		assertEquals("4", documentBuffer.addTextEvent("5", 4, "F"));
//		assertEquals("5", documentBuffer.addTextEvent("6", 5, "H"));
//
//		assertEquals("6", documentBuffer.addTextEvent("7", 6, "I"));
//		assertEquals("5", documentBuffer.addTextEvent("8", 5, "G"));
//		assertEquals("4", documentBuffer.addTextEvent("9", 4, "E"));
//		assertEquals(null, documentBuffer.addTextEvent("10", 0, "!"));
//
//		assertEquals("!ABCDEFGHI", documentBuffer.toString());
//	}
//
//
//	@Test
//	public void testDocBufferWithDeletesWithIndices()
//	{
//		DocumentBuffer documentBuffer = new DocumentBuffer();
//		assertEquals(null, documentBuffer.addTextEvent("1", 0, "A"));
//		assertEquals("1", documentBuffer.addTextEvent("2", 1, "B"));
//		assertEquals("2", documentBuffer.addTextEvent("3", 2, "C"));
//		assertEquals("3", documentBuffer.addTextEvent("4", 3, "D"));
//		assertEquals("4", documentBuffer.addTextEvent("5", 4, "F"));
//		assertEquals("5", documentBuffer.addTextEvent("6", 5, "H"));
//
//		assertEquals("6", documentBuffer.addTextEvent("7", 6, "I"));
//		assertEquals("5", documentBuffer.addTextEvent("8", 5, "G"));
//		assertEquals("4", documentBuffer.addTextEvent("9", 4, "E"));
//		assertEquals(null, documentBuffer.addTextEvent("10", 0, "!"));
//
//		assertEquals("1", documentBuffer.addDeleteEvent(1));
//		assertEquals("7", documentBuffer.addDeleteEvent(8));
//		assertEquals("9", documentBuffer.addDeleteEvent(4));
//		assertEquals("10", documentBuffer.addDeleteEvent(0));
//
//		assertEquals("BCDFGH", documentBuffer.toString());
//
//		assertEquals("2", documentBuffer.addDeleteEvent(0));
//		assertEquals("3", documentBuffer.addDeleteEvent(0));
//		assertEquals("4", documentBuffer.addDeleteEvent(0));
//		assertEquals("5", documentBuffer.addDeleteEvent(0));
//		assertEquals("8", documentBuffer.addDeleteEvent(0));
//		assertEquals("6", documentBuffer.addDeleteEvent(0));
//
//		assertEquals("", documentBuffer.toString());
//		try
//		{
//			documentBuffer.addDeleteEvent(0);
//			fail();
//		}
//		catch (Exception e)
//		{
//			// We are expecting an exception from deleting the empty thing
//		}
//	}
//
//
//	@Test
//	public void testDocBufferAddingTextWithIds()
//	{
//		DocumentBuffer documentBuffer = new DocumentBuffer();
//		documentBuffer.addTextEvent("1", null, "A");
//		documentBuffer.addTextEvent("2", "1", "B");
//		documentBuffer.addTextEvent("3", "2", "C");
//		documentBuffer.addTextEvent("4", "3", "D");
//		documentBuffer.addTextEvent("5", "4", "F");
//		documentBuffer.addTextEvent("6", "5", "H");
//
//		assertEquals("ABCDFH", documentBuffer.toString());
//
//		documentBuffer.addTextEvent("7", "6", "I");
//		documentBuffer.addTextEvent("8", "5", "G");
//		documentBuffer.addTextEvent("9", "4", "E");
//		documentBuffer.addTextEvent("10", null, "!");
//
//		assertEquals("!ABCDEFGHI", documentBuffer.toString());
//	}
//
//
//	@Test
//	public void testDocBufferAddingAndDeletingTextWithIds()
//	{
//		DocumentBuffer documentBuffer = new DocumentBuffer();
//		documentBuffer.addTextEvent("1", null, "A");
//		documentBuffer.addTextEvent("2", "1", "B");
//		documentBuffer.addTextEvent("3", "2", "C");
//		documentBuffer.addTextEvent("4", "3", "D");
//		documentBuffer.addTextEvent("5", "4", "F");
//		documentBuffer.addTextEvent("6", "5", "H");
//		documentBuffer.addTextEvent("7", "6", "I");
//		documentBuffer.addTextEvent("8", "5", "G");
//		documentBuffer.addTextEvent("9", "4", "E");
//		documentBuffer.addTextEvent("10", null, "!");
//
//		assertEquals("!ABCDEFGHI", documentBuffer.toString());
//
//		documentBuffer.addDeleteEvent("1");
//		documentBuffer.addDeleteEvent("7");
//		documentBuffer.addDeleteEvent("9");
//		documentBuffer.addDeleteEvent("10");
//
//		assertEquals("BCDFGH", documentBuffer.toString());
//
//		documentBuffer.addDeleteEvent("8");
//		documentBuffer.addDeleteEvent("6");
//		documentBuffer.addDeleteEvent("2");
//
//		assertEquals("CDF", documentBuffer.toString());
//
//		documentBuffer.addDeleteEvent("3");
//		documentBuffer.addDeleteEvent("5");
//		documentBuffer.addDeleteEvent("4");
//
//		assertEquals("", documentBuffer.toString());
//		try
//		{
//			documentBuffer.addDeleteEvent(0);
//			fail();
//		}
//		catch (Exception e)
//		{
//			// We are expecting an exception from deleting the empty thing
//		}
//	}
//
//
//	@Test
//	public void testWritingAndDeletingToTheFrontOfADocument()
//	{
//		DocumentBuffer documentBuffer = new DocumentBuffer();
//		// add many things to the front of the queue
//		documentBuffer.addTextEvent("0", null, "G");
//		documentBuffer.addTextEvent("1", null, "F");
//		documentBuffer.addTextEvent("2", null, "E");
//		documentBuffer.addTextEvent("3", null, "D");
//		documentBuffer.addTextEvent("4", null, "C");
//		documentBuffer.addTextEvent("5", null, "B");
//		documentBuffer.addTextEvent("6", null, "A");
//
//		assertEquals("ABCDEFG", documentBuffer.toString());
//
//		// delete them all from the front
//		documentBuffer.addDeleteEvent("6");
//		documentBuffer.addDeleteEvent("5");
//		documentBuffer.addDeleteEvent("4");
//
//		assertEquals("DEFG", documentBuffer.toString());
//
//		documentBuffer.addDeleteEvent("3");
//		documentBuffer.addDeleteEvent("2");
//		documentBuffer.addDeleteEvent("1");
//		documentBuffer.addDeleteEvent("0");
//
//		assertEquals("", documentBuffer.toString());
//	}
//
//
//	@Test
//	public void testToJSON() throws Exception
//	{
//		DocumentBuffer documentBuffer = new DocumentBuffer();
//		assertEquals(null, documentBuffer.addTextEvent("1", 0, "A"));
//		assertEquals("1", documentBuffer.addTextEvent("2", 1, "B"));
//		assertEquals("2", documentBuffer.addTextEvent("3", 2, "C"));
//		assertEquals("3", documentBuffer.addTextEvent("4", 3, "D"));
//		assertEquals("4", documentBuffer.addTextEvent("5", 4, "F"));
//		assertEquals("5", documentBuffer.addTextEvent("6", 5, "H"));
//
//		assertEquals("6", documentBuffer.addTextEvent("7", 6, "I"));
//		assertEquals("5", documentBuffer.addTextEvent("8", 5, "G"));
//		assertEquals("4", documentBuffer.addTextEvent("9", 4, "E"));
//		assertEquals(null, documentBuffer.addTextEvent("10", 0, "!"));
//
//		assertEquals("!ABCDEFGHI", documentBuffer.toString());
//
//		DocumentBuffer newDocumentBuffer = new DocumentBuffer(documentBuffer.toJSONArray());
//
//		assertEquals("!ABCDEFGHI", newDocumentBuffer.toString());
//	}
//
//
//	public void testCompressionSmallFile() throws Exception
//	{
//		// Creates a document buffer with 2 nodes in it and not much in it.
//		DocumentBuffer buffer = new DocumentBuffer();
//
//		List<String> nodeIds = new ArrayList<String>();
//		for (int i = 0; i < 2; i++)
//		{
//			nodeIds.add(Utilities.getRandomID());
//		}
//
//		String textString = "private static Logger logger = Logger.getLogger(TestDocumentBuffers.class.getName());\n" +
//				"/t/n" +
//				"/t@BeforeClass/n";
//
//		// 100,000 events is about 1250 lines of code
//		logger.info("Starting the creation of 3 lines of code");
//		for (int i = 0; i < textString.length(); i++)
//		{
//			String randomEventId = generateRandomEventId(nodeIds);
//			String randomEventData = "" + textString.charAt(i);
//			buffer.addTextEvent(randomEventId, i, randomEventData);
//
//		}
//		logger.info("Document buffer created, turning it into a string");
//		String bufferString = buffer.toJSONArray().toString();
//		logger.info("The document buffer as a string is " + bufferString.getBytes().length + " bytes long");
//		logger.info("Starting compacting");
//		byte[] compressedBytes = buffer.getAsCompressedBytes();
//		logger.info("Compacting finished");
//		logger.info("After compacting, the string was " + compressedBytes.length + " bytes long");
//
//		logger.info("Compacting Ratio was " + (1.0 * compressedBytes.length) / bufferString.getBytes().length);
//		logger.info("Inflating");
//		DocumentBuffer deflatedBuffer = DocumentBuffer.getBufferFromCompressedString(compressedBytes);
//		assertEquals(textString, deflatedBuffer.toString());
//		logger.info("Deflated back to normal");
//	}
//
//
//	public void testCompressionMediumFile() throws Exception
//	{
//		// Creates a document buffer with 4 nodes in it and 1,250 or so lines of code worth of events.
//		DocumentBuffer buffer = new DocumentBuffer();
//
//		List<String> nodeIds = new ArrayList<String>();
//		for (int i = 0; i < 4; i++)
//		{
//			nodeIds.add(Utilities.getRandomID());
//		}
//		String alphabetString = "abcdefghijklmnopqrstuvwzyzABCDEFGHIJKLMNOPQRSTUVWXYZ()[].;*+-\n";
//
//		// 100,000 events is about 1250 lines of code
//		logger.info("Starting the creation of 1250 lines of code");
//		for (int i = 0; i < 100000; i++)
//		{
//			String randomEventId = generateRandomEventId(nodeIds);
//			String randomEventData = "" + alphabetString.charAt(random.nextInt(alphabetString.length()));
//			buffer.addTextEvent(randomEventId, i, randomEventData);
//
//		}
//		logger.info("Document buffer created, turning it into a string");
//		String bufferString = buffer.toJSONArray().toString();
//		logger.info("The document buffer as a string is " + bufferString.getBytes().length + " bytes long");
//		logger.info("Starting compacting");
//		byte[] compressedBytes = buffer.getAsCompressedBytes();
//		logger.info("Compacting finished");
//		logger.info("After compacting, the string was " + compressedBytes.length + " bytes long");
//
//		logger.info("Compacting Ratio was " + (1.0 * compressedBytes.length) / bufferString.getBytes().length);
//		logger.info("Inflating");
//		DocumentBuffer deflatedBuffer = DocumentBuffer.getBufferFromCompressedString(compressedBytes);
//		logger.info("Deflated back to normal");
//		assertEquals(buffer.toString(), deflatedBuffer.toString());
//	}
//
//
//	public void testCompressionLargeFile() throws Exception
//	{
//		// Creates a document buffer with 10 nodes in it and 12,500 or so lines of code worth of events.
//		DocumentBuffer buffer = new DocumentBuffer();
//
//		List<String> nodeIds = new ArrayList<String>();
//		for (int i = 0; i < 10; i++)
//		{
//			nodeIds.add(Utilities.getRandomID());
//		}
//		String alphabetString = "abcdefghijklmnopqrstuvwzyzABCDEFGHIJKLMNOPQRSTUVWXYZ()[].;*+-\n";
//
//		// 100,000 events is about 1250 lines of code
//		logger.info("Starting the creation of 12500 lines of code");
//		for (int i = 0; i < 1000000; i++)
//		{
//			String randomEventId = generateRandomEventId(nodeIds);
//			String randomEventData = "" + alphabetString.charAt(random.nextInt(alphabetString.length()));
//			buffer.addTextEvent(randomEventId, i, randomEventData);
//
//		}
//		logger.info("Document buffer created, turning it into a string");
//		String bufferString = buffer.toJSONArray().toString();
//		logger.info("The document buffer as a string is " + bufferString.getBytes().length + " bytes long");
//		logger.info("Starting compacting");
//		byte[] compressedBytes = buffer.getAsCompressedBytes();
//		logger.info("Compacting finished");
//		logger.info("After compacting, the string was " + compressedBytes.length + " bytes long");
//
//		logger.info("Compacting Ratio was " + (1.0 * compressedBytes.length) / bufferString.getBytes().length);
//		logger.info("Inflating");
//		DocumentBuffer deflatedBuffer = DocumentBuffer.getBufferFromCompressedString(compressedBytes);
//		logger.info("Deflated back to normal");
//		assertEquals(buffer.toString(), deflatedBuffer.toString());
//	}
//
//
//	private String generateRandomEventId(List<String> nodeIds)
//	{
//		return "";//StorytellerEvent.createEventId(nodeIds.get(random.nextInt(nodeIds.size())), random.nextInt(5000));
//	}
//}
