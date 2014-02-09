package appeng.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import appeng.core.AELog;
import appeng.core.AEConfig;

public class VersionChecker implements Runnable
{

	public static VersionChecker instance = null;

	private long delay = 0;

	public VersionChecker() {
		long now = (new Date()).getTime();
		delay = (1000 * 3600 * 5) - (now - AEConfig.instance.latestTimeStamp);
		if ( delay < 1 )
			delay = 1;
	}

	@Override
	public void run()
	{
		try
		{
			Thread.sleep( delay );
		}
		catch (InterruptedException e)
		{
		}

		while (true)
		{
			Thread.yield();
			try
			{
				URL url = new URL( "http://ae-mod.info/releases/?latest" );

				String MCVersion = cpw.mods.fml.common.Loader.instance().getMCVersionString();

				URLConnection yc = url.openConnection();
				yc.setRequestProperty( "User-Agent", "AE2/" + AEConfig.VERSION + " (Channel:" + AEConfig.CHANNEL + "," + MCVersion.replace( " ", ":" )
						+ ")" );
				BufferedReader in = new BufferedReader( new InputStreamReader( yc.getInputStream() ) );

				String Version = "";
				String inputLine;

				while ((inputLine = in.readLine()) != null)
					Version += inputLine;

				in.close();

				if ( Version.length() > 2 )
				{
					Matcher m = Pattern.compile( "\"Version\":\"([^\"]+)\"" ).matcher( Version );
					m.find();
					Version = m.group( 1 );
					AEConfig.instance.latestVersion = Version;
					AEConfig.instance.latestTimeStamp = (new Date()).getTime();
					AEConfig.instance.save();
				}

				Thread.sleep( 1000 * 3600 * 4 );
			}
			catch (Exception e)
			{
				try
				{
					Thread.sleep( 1000 * 3600 * 4 );
				}
				catch (InterruptedException e1)
				{
					AELog.error( e );
				}
			}
		}
	}

}
