package core.data;


/**
 * This class specifies how to create a specific database for
 * storyteller. The server does not know about specific db's
 * (like sqlite) it only knows about DBAbstraction. However,
 * since the server is the one who has to create db's it needs 
 * a factory to tell it which kind to create.
 */
public interface DBFactory
{
	/**
	 * This method creates a specific type of DBAbstraction. Subclasses
	 * will do the creating and return it from the method.
	 * 
	 * @param filename Filename of the file to create
	 * @return A specific type of DBAbstraction object that was created 
	 * 
	 * @throws DBAbstractionException
	 */
	public DBAbstraction createDatabaseAbstraction(String filename) throws DBAbstractionException; 
}
