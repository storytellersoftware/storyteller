//package unitTests;
//
//
//
//import static org.junit.Assert.*;
//
//import java.util.Date;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.Test;
//
//import StorytellerEntities.Developer;
//import StorytellerServer.exception.DBAbstractionException;
//import StorytellerServer.json.DeJSONiffy;
//import StorytellerServer.json.JSONiffy;
//
//public class HandlerTests {
//
//	@Test
//	public void test() {
//		Developer dev = new Developer("Dev ID", new Date(), "Node ID", "dev group id", "email", "josh", "king");
//		try {
//			JSONObject json = JSONiffy.toJSON(dev);
//			Developer newDev = DeJSONiffy.developer(json);
//			System.out.println(newDev.toString());
//		} catch (JSONException | DBAbstractionException e) {
//			e.printStackTrace();
//		}
//	}
//
//}
