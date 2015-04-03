package appeng.services.version;


/**
 * Base version of {@link Version}.
 *
 * Provides a unified way to test for equality and print a formatted string
 */
public abstract class BaseVersion implements Version
{
	private final int revision;
	private final Channel channel;
	private final int build;

	/**
	 * @param revision revision in natural number
	 * @param channel  channel
	 * @param build    build in natural number
	 *
	 * @throws AssertionError if assertion are enabled and revision or build are not natural numbers
	 */
	public BaseVersion( int revision, Channel channel, int build )
	{
		assert revision >= 0;
		assert build >= 0;

		this.revision = revision;
		this.channel = channel;
		this.build = build;
	}

	@Override
	public final int revision()
	{
		return this.revision;
	}

	@Override
	public final Channel channel()
	{
		return this.channel;
	}

	@Override
	public final int build()
	{
		return this.build;
	}

	@Override
	public String formatted()
	{
		return "rv" + this.revision + '-' + this.channel.name().toLowerCase() + '-' + this.build;
	}

	@Override
	public final int hashCode()
	{
		int result = this.revision;
		result = 31 * result + ( this.channel != null ? this.channel.hashCode() : 0 );
		result = 31 * result + this.build;
		return result;
	}

	@Override
	public final boolean equals( Object o )
	{
		if( this == o )
			return true;
		if( !( o instanceof Version ) )
			return false;

		Version that = (Version) o;

		if( this.revision != that.revision() )
			return false;
		if( this.build != that.build() )
			return false;
		return this.channel == that.channel();
	}

	@Override
	public final String toString()
	{
		return "Version{" +
				"revision=" + this.revision +
				", channel=" + this.channel +
				", build=" + this.build +
				'}';
	}
}
