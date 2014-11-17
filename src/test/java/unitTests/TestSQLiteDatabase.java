//package unitTests;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashSet;
//
//import StorytellerServer.exception.DBAbstractionException;
//import StorytellerServer.SQLiteDatabase;
//
//public class TestSQLiteDatabase extends SQLiteDatabase {
//
//	public TestSQLiteDatabase(String path) throws DBAbstractionException 
//	{
//		super(path);
//	}
//
//	public HashSet<String> getTables() throws DBAbstractionException, SQLException
//	{
//		HashSet<String> retval = new HashSet<String>();
//		String statement = "SELECT * FROM sqlite_master WHERE type='table'";
//		PreparedStatement ps = connection.prepareStatement(statement);
//		ResultSet rs = this.executeWithResults(ps);
//		while(rs.next())
//		{
//			retval.add(rs.getString("name"));
//		}
//		return retval;
//	}
//	public boolean getTableExists(String tablename) throws DBAbstractionException
//	{
//		StringBuilder statement = new StringBuilder();
//		statement.append("SELECT * FROM ?");
//		try 
//		{
//			PreparedStatement ps = connection.prepareStatement(statement.toString());
//			ps.setString(1, tablename);
//			ps.setQueryTimeout(15);
//			if(ps.execute())
//			{
//				return true;
//			}
//			else
//			{
//				return false;
//			}
//		} 
//		catch (SQLException e) 
//		{
//			logger.error("Problem with Query Text: \n"+ statement.toString());
//			//if something bad happened in the sql, wrap up the
//			//exception and pass it on up
//			throw new DBAbstractionException(e);
//		} 
//	}
//	public void createEventsTableTest() throws DBAbstractionException
//	{
//		super.createEventsTable();
//	}
//	public void createTablesTest() throws DBAbstractionException
//	{
//		super.createTables();
//	}
//
//}
