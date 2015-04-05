package appeng.core.api;


public class ApiConflictException extends RuntimeException
{

	private static final long serialVersionUID = -2678606528548790136L;

	public ApiConflictException(String string)
	{
		super( string );
	}

}
