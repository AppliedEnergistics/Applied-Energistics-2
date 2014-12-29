/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.services;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.event.FMLInterModComms;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;

public class VersionChecker implements Runnable
{
	private static final int FOUR_HOURS = 1000 * 3600 * 4;

	private final long delay;

	public VersionChecker()
	{
		final long now = new Date().getTime();
		final long timeDiff = now - AEConfig.instance.latestTimeStamp;

		this.delay = Math.max( 1, FOUR_HOURS - timeDiff);
	}

	@Override
	public void run()
	{
		try
		{
			this.sleep( this.delay );
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
				yc.setRequestProperty( "User-Agent", "AE2/" + AEConfig.VERSION + " (Channel:" + AEConfig.CHANNEL + ',' + MCVersion.replace( " ", ":" ) + ')' );
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

				this.sleep( FOUR_HOURS );
			}
			catch (Exception e)
			{
				try
				{
					this.sleep( FOUR_HOURS );
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
