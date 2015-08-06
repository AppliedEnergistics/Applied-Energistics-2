
package appeng.services.version;


/**
 * AE prints version like rv2-beta-8
 * GitHub prints version like rv2.beta.8
 */
public final class DefaultVersion extends BaseVersion
{
	/**
	 * @param revision natural number
	 * @param channel either alpha, beta or release
	 * @param build natural number
	 */
	public DefaultVersion( int revision, Channel channel, int build )
	{
		super( revision, channel, build );
	}

	@Override
	public boolean isNewerAs( Version maybeOlder )
	{
		if( this.revision() > maybeOlder.revision() )
		{
			return true;
		}

		if( this.channel().compareTo( maybeOlder.channel() ) > 0 )
		{
			return true;
		}

		return this.build() > maybeOlder.build();
	}
}
