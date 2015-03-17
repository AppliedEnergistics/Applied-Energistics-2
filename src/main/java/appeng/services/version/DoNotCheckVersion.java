package appeng.services.version;


/**
 * Exceptional template for {@link Version}, when the mod does not want a check
 */
public final class DoNotCheckVersion extends BaseVersion
{
	public DoNotCheckVersion()
	{
		super( Integer.MAX_VALUE, Channel.Release, Integer.MAX_VALUE );
	}

	@Override
	public boolean isNewerAs( Version maybeOlder )
	{
		return true;
	}

	@Override
	public String formatted()
	{
		return "dev build";
	}
}
