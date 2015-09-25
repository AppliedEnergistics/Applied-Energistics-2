
package appeng.services.version;


/**
 * Exceptional template when the {@link Version} could not be retrieved
 */
public final class MissingVersion extends BaseVersion
{
	public MissingVersion()
	{
		super( 0, Channel.Alpha, 0 );
	}

	/**
	 * @param maybeOlder ignored
	 *
	 * @return false
	 */
	@Override
	public boolean isNewerAs( final Version maybeOlder )
	{
		return false;
	}

	@Override
	public String formatted()
	{
		return "missing";
	}
}
