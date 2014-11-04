package core.data;

/**
 * This exception class represents something that is
 * wrong with the DBAbstraction class that interacts
 * with a database in storyteller. It either wraps an 
 * existing exception or creates a new one with an
 * error message. It defines its own type for specific 
 * try/catch scenarios in storyteller code
 */
public class DBAbstractionException extends Exception
{
	private static final long serialVersionUID = 2621829806909174269L;

	public DBAbstractionException(String message)
	{
		super(message);
	}
	
	public DBAbstractionException(Throwable wrappedException)
	{
		super(wrappedException);
	}
}
