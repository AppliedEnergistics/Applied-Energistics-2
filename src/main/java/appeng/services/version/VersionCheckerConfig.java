package appeng.services.version;


import java.io.File;
import java.util.Date;

import net.minecraftforge.common.config.Configuration;


/**
 * Separate config file to handle the version checker
 */
public final class VersionCheckerConfig
{
	private static final int DEFAULT_INTERVAL_HOURS = 24;
	private static final int MIN_INTERVAL_HOURS = 0;
	private static final int MAX_INTERVAL_HOURS = 7 * 24;

	private final Configuration config;

	private final boolean isEnabled;

	private final String lastCheck;
	private final int interval;

	private final String level;

	private final boolean shouldNotifyPlayer;
	private final boolean shouldPostChangelog;

	/**
	 * @param file requires fully qualified file in which the config is saved
	 */
	public VersionCheckerConfig( File file )
	{
		this.config = new Configuration( file );

		// initializes default values by caching
		this.isEnabled = this.config.getBoolean( "enabled", "general", true, "If true, the version checker is enabled. Acts as a master switch." );

		this.lastCheck = this.config.getString( "lastCheck", "cache", "0", "The number of milliseconds since January 1, 1970, 00:00:00 GMT of the last successful check." );
		this.interval = this.config.getInt( "interval", "cache", DEFAULT_INTERVAL_HOURS, MIN_INTERVAL_HOURS, MAX_INTERVAL_HOURS, "Waits as many hours, until it checks again." );

		this.level = this.config.getString( "level", "channel", "Beta", "Determines the channel level which should be checked for updates. Can be either Release, Beta or Alpha." );

		this.shouldNotifyPlayer = this.config.getBoolean( "notify", "client", true, "If true, the player is getting a notification, that a new version is available." );
		this.shouldPostChangelog = this.config.getBoolean( "changelog", "client", true, "If true, the player is getting a notification including changelog. Only happens if notification are enabled." );
	}

	public boolean isEnabled()
	{
		return this.isEnabled;
	}

	public String lastCheck()
	{
		return this.lastCheck;
	}

	/**
	 * Stores the current date in milli seconds into the "lastCheck" field of the config
	 * and makes it persistent.
	 */
	public void updateLastCheck()
	{
		final Date now = new Date();
		final long nowInMs = now.getTime();
		final String nowAsString = Long.toString( nowInMs );

		this.config.get( "cache", "lastCheck", "0" ).set( nowAsString );

		this.config.save();
	}

	public int interval()
	{
		return this.interval;
	}

	public String level()
	{
		return this.level;
	}

	public boolean shouldNotifyPlayer()
	{
		return this.shouldNotifyPlayer;
	}

	public boolean shouldPostChangelog()
	{
		return this.shouldPostChangelog;
	}
}
