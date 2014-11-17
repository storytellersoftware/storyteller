//package unitTests;
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
//public class TestdeJSONiffy {
//
//	@Test
//	public void test() {
//		try{
//		Developer first = new Developer(new Date(), "under josh", "by awesome group", "me@josh.com", "josh", "king");
//		System.out.println(first.toString());
//		
//		Developer second = new Developer(new Date(), "under jorge", "by inferior" , "jorge@sucks.net" , "jorge" , "zamora");
//		System.out.println(second.toString());
//		
//		JSONObject json = JSONiffy.toJSON(first);
//		
//		Developer input = DeJSONiffy.developer(json);
//		
//		second.update(input);
//		System.out.println(second.toString());
//		}
//		catch(JSONException | DBAbstractionException e)
//		{
//			System.out.println("FUCKFUCKFUCK");
//		}
//	}
//
//}
