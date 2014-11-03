package appeng.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import net.minecraft.nbt.NBTTagCompound;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.event.FMLInterModComms;

public class VersionChecker implements Runnable
{
	private long delay = 0;

	public VersionChecker()
	{
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
			sleep( delay );
		}
		catch (InterruptedException e)
		{
			// :(
		}

		while (true)
		{
			Thread.yield();

			try
			{
				String MCVersion = cpw.mods.fml.common.Loader.instance().getMCVersionString().replace( "Minecraft ", "" );
				URL url = new URL( "http://feeds.ae-mod.info/latest.json?VersionMC=" + MCVersion + "&Channel=" + AEConfig.CHANNEL + "&CurrentVersion="
						+ AEConfig.VERSION );

				URLConnection yc = url.openConnection();
				yc.setRequestProperty( "User-Agent", "AE2/" + AEConfig.VERSION + " (Channel:" + AEConfig.CHANNEL + "," + MCVersion.replace( " ", ":" ) + ")" );
				BufferedReader in = new BufferedReader( new InputStreamReader( yc.getInputStream() ) );

				StringBuilder Version = new StringBuilder();
				String inputLine;

				while ((inputLine = in.readLine()) != null)
					Version.append( inputLine );

				in.close();

				if ( Version.length() > 2 )
				{
					JsonElement element = (new JsonParser()).parse( Version.toString() );

					int version = element.getAsJsonObject().get( "FormatVersion" ).getAsInt();
					if ( version == 1 )
					{
						JsonObject Meta = element.getAsJsonObject().get( "Meta" ).getAsJsonObject();
						JsonArray Versions = element.getAsJsonObject().get( "Versions" ).getAsJsonArray();
						if ( Versions.size() > 0 )
						{
							JsonObject Latest = Versions.get( 0 ).getAsJsonObject();

							AEConfig.instance.latestVersion = Latest.get( "Version" ).getAsString();
							AEConfig.instance.latestTimeStamp = (new Date()).getTime();
							AEConfig.instance.save();

							if ( !AEConfig.VERSION.equals( AEConfig.instance.latestVersion ) )
							{
								NBTTagCompound versionInf = new NBTTagCompound();
								versionInf.setString( "modDisplayName", "Applied Energistics 2" );
								versionInf.setString( "oldVersion", AEConfig.VERSION );
								versionInf.setString( "newVersion", AEConfig.instance.latestVersion );
								versionInf.setString( "updateUrl", Latest.get( "UserBuild" ).getAsString() );
								versionInf.setBoolean( "isDirectLink", true );

								JsonElement changeLog = Latest.get( "ChangeLog" );
								if ( changeLog == null )
									versionInf.setString( "changeLog", "For full change log please see: " + Meta.get( "DownloadLink" ).getAsString() );
								else
									versionInf.setString( "changeLog", changeLog.getAsString() );

								versionInf.setString( "newFileName", "appliedenergistics2-" + AEConfig.instance.latestVersion + ".jar" );
								FMLInterModComms.sendRuntimeMessage( AppEng.instance, "VersionChecker", "addUpdate", versionInf );

								AELog.info( "Stopping VersionChecker" );
								return;
							}
						}
					}
				}

				sleep( 1000 * 3600 * 4 );
			}
			catch (Exception e)
			{
				try
				{
					sleep( 1000 * 3600 * 4 );
				}
				catch (InterruptedException e1)
				{
					AELog.error( e );
				}
			}
		}
	}

	private void sleep(long i) throws InterruptedException
	{
		Thread.sleep( i );
	}
}
