package core.data;


/**
 * A factory that knows how to create a sqlite db.
 */
public class SQLiteDBFactory implements DBFactory
{
	/**
	 * This method creates a sqlite database object and returns it.
	 * @param filename Filename of the file to create
	 * @return A sqlite DBAbstraction object  
	 * 
	 * @throws DBAbstractionException
	 */
	@Override
	public DBAbstraction createDatabaseAbstraction(String filename) throws DBAbstractionException
	{
		//create a sqlite db object and return it to the user
		return new SQLiteDatabase(filename);
	}
}
