package appeng.api;

/**
 * 
 * Entry point for api.
 * 
 * Available IMCs:
 * 
 */
public class AEApi
{

	static private IAppEngApi api = null;

	/**
	 * API Entry Point.
	 * 
	 * @return the {@link IAppEngApi} or null if the instance could not be retrieved
	 */
	public static IAppEngApi instance()
	{
		if ( api == null )
		{
			try
			{
				Class c = Class.forName( "appeng.core.Api" );
				api = (IAppEngApi) c.getField( "instance" ).get( c );
			}
			catch (Throwable e)
			{
				return null;
			}
		}

		return api;
	}

}
