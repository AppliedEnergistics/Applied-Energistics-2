package appeng.services.version.github;


import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

import appeng.core.AELog;
import appeng.services.version.Channel;
import appeng.services.version.Version;
import appeng.services.version.VersionCheckerConfig;
import appeng.services.version.VersionParser;


public final class ReleaseFetcher
{
	private static final String GITHUB_RELEASES_URL = "https://api.github.com/repos/AppliedEnergistics/Applied-Energistics-2/releases";
	private static final FormattedRelease EXCEPTIONAL_RELEASE = new MissingFormattedRelease();

	private final VersionCheckerConfig config;
	private final VersionParser parser;

	public ReleaseFetcher( VersionCheckerConfig config, VersionParser parser )
	{
		this.config = config;
		this.parser = parser;
	}

	public FormattedRelease get()
	{
		final Gson gson = new Gson();
		final Type type = new ReleasesTypeToken().getType();

		try
		{
			final URL releasesURL = new URL( GITHUB_RELEASES_URL );
			final String rawReleases = this.getRawReleases( releasesURL );

			this.config.updateLastCheck();

			final List<Release> releases = gson.fromJson( rawReleases, type );
			final FormattedRelease latestFitRelease = this.getLatestFitRelease( releases );

			return latestFitRelease;
		}
		catch( Exception e )
		{
			AELog.error( e );

			return EXCEPTIONAL_RELEASE;
		}
	}

	private String getRawReleases( URL url ) throws IOException
	{
		return IOUtils.toString( url );
	}

	private FormattedRelease getLatestFitRelease( Iterable<Release> releases )
	{
		final String levelInConfig = this.config.level();
		final Channel level = Channel.valueOf( levelInConfig );
		final int levelOrdinal = level.ordinal();

		for( Release release : releases )
		{
			final String rawVersion = release.tag_name;
			final String changelog = release.body;

			final Version version = this.parser.parse( rawVersion );

			if( version.channel().ordinal() >= levelOrdinal )
			{
				return new DefaultFormattedRelease( version, changelog );
			}
		}

		return EXCEPTIONAL_RELEASE;
	}

	private static final class ReleasesTypeToken extends TypeToken<List<Release>>
	{
	}
}
