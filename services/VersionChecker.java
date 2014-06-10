package appeng.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import net.minecraft.nbt.NBTTagCompound;
import appeng.core.AEConfig;
import appeng.core.AELog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.event.FMLInterModComms;

public class VersionChecker implements Runnable
{

	public static VersionChecker instance = null;

	private long delay = 0;
	private boolean VersionChecker = true;

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
				String MCVersion = cpw.mods.fml.common.Loader.instance().getMCVersionString().replace( "Minecraft ", "" );
				URL url = new URL( "http://ae2.ae-mod.info/builds/latest.json?VersionMC=" + MCVersion + "&Channel=" + AEConfig.CHANNEL + "&CurrentVersion="
						+ AEConfig.VERSION );

				URLConnection yc = url.openConnection();
				yc.setRequestProperty( "User-Agent", "AE2/" + AEConfig.VERSION + " (Channel:" + AEConfig.CHANNEL + "," + MCVersion.replace( " ", ":" ) + ")" );
				BufferedReader in = new BufferedReader( new InputStreamReader( yc.getInputStream() ) );

				String Version = "";
				String inputLine;

				while ((inputLine = in.readLine()) != null)
					Version += inputLine;

				in.close();

				if ( Version.length() > 2 )
				{
					JsonElement element = (new JsonParser()).parse( Version );

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

							if ( VersionChecker && !AEConfig.VERSION.equals( AEConfig.instance.latestVersion ) )
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
								FMLInterModComms.sendMessage( "VersionChecker", "addUpdate", versionInf );
								VersionChecker = false;
							}
						}
					}
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
