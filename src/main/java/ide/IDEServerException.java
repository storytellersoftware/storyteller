package ide;

/**
 * IDE server specific exception type. 
 */
public class IDEServerException extends Exception
{
	private static final long serialVersionUID = 589211991227125966L;

	public IDEServerException(String message)
	{
		super(message);
	}
	
	public IDEServerException(Throwable wrappedException)
	{
		super(wrappedException);
	}
}
