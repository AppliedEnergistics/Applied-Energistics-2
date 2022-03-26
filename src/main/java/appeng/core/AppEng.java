/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core;


import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import appeng.helpers.NonBlockingItems;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import team.chisel.ctm.CTM;

import appeng.api.AEApi;
import appeng.core.crash.CrashInfo;
import appeng.core.crash.IntegrationCrashEnhancement;
import appeng.core.crash.ModCrashEnhancement;
import appeng.core.features.AEFeature;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.hooks.TickHandler;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.server.AECommand;
import appeng.services.VersionChecker;
import appeng.services.export.ExportConfig;
import appeng.services.export.ExportProcess;
import appeng.services.export.ForgeExportConfig;
import appeng.services.version.VersionCheckerConfig;
import appeng.util.Platform;


@Mod( modid = AppEng.MOD_ID, acceptedMinecraftVersions = "[1.12.2]", name = AppEng.MOD_NAME, version = AEConfig.VERSION, dependencies = AppEng.MOD_DEPENDENCIES, guiFactory = "appeng.client.gui.config.AEConfigGuiFactory", certificateFingerprint = "dfa4d3ac143316c6f32aa1a1beda1e34d42132e5" )
public final class AppEng
{
	@SidedProxy( clientSide = "appeng.client.ClientHelper", serverSide = "appeng.server.ServerHelper", modId = AppEng.MOD_ID )
	public static CommonHelper proxy;

	public static final String MOD_ID = "appliedenergistics2";
	public static final String MOD_NAME = "Applied Energistics 2";

	public static final String ASSETS = "appliedenergistics2:";

	private static final String FORGE_CURRENT_VERSION = ForgeVersion.majorVersion + "." + ForgeVersion.minorVersion + "." + ForgeVersion.revisionVersion + "." + ForgeVersion.buildVersion;
	private static final String FORGE_MAX_VERSION = ( ForgeVersion.majorVersion + 1 ) + ".0.0.0";
	public static final String MOD_DEPENDENCIES = "required-after:forge@[" + FORGE_CURRENT_VERSION + "," + FORGE_MAX_VERSION + ");after:ctm@[" + CTM.VERSION + ",);";

	@Nonnull
	private static final AppEng INSTANCE = new AppEng();

	private final Registration registration;

	private File configDirectory;

	/**
	 * determined in pre-init but used in init
	 */
	private ExportConfig exportConfig;

	private AppEng()
	{
		FMLCommonHandler.instance().registerCrashCallable( new ModCrashEnhancement( CrashInfo.MOD_VERSION ) );

		this.registration = new Registration();
		MinecraftForge.EVENT_BUS.register( this.registration );
	}

	@Nonnull
	@Mod.InstanceFactory
	public static AppEng instance()
	{
		return INSTANCE;
	}

	public Biome getStorageBiome()
	{
		return this.registration.storageBiome;
	}

	public DimensionType getStorageDimensionType()
	{
		return this.registration.storageDimensionType;
	}

	public int getStorageDimensionID()
	{
		return this.registration.storageDimensionID;
	}

	public AdvancementTriggers getAdvancementTriggers()
	{
		return this.registration.advancementTriggers;
	}

	@EventHandler
	private void preInit( final FMLPreInitializationEvent event )
	{
		final Stopwatch watch = Stopwatch.createStarted();
		this.configDirectory = new File( event.getModConfigurationDirectory().getPath(), "AppliedEnergistics2" );

		final File configFile = new File( this.configDirectory, "AppliedEnergistics2.cfg" );
		final File facadeFile = new File( this.configDirectory, "Facades.cfg" );
		final File versionFile = new File( this.configDirectory, "VersionChecker.cfg" );
		final File recipeFile = new File( this.configDirectory, "CustomRecipes.cfg" );
		final Configuration recipeConfiguration = new Configuration( recipeFile );

		AEConfig.init( configFile );
		FacadeConfig.init( facadeFile );

		final VersionCheckerConfig versionCheckerConfig = new VersionCheckerConfig( versionFile );
		this.exportConfig = new ForgeExportConfig( recipeConfiguration );

		AELog.info( "Pre Initialization ( started )" );

		CreativeTab.init();
		if( AEConfig.instance().isFeatureEnabled( AEFeature.FACADES ) )
		{
			CreativeTabFacade.init();
		}

		for( final IntegrationType type : IntegrationType.values() )
		{
			IntegrationRegistry.INSTANCE.add( type );
		}

		this.registration.preInitialize( event );

		if( Platform.isClient() )
		{
			AppEng.proxy.preinit();
		}

		IntegrationRegistry.INSTANCE.preInit();

		if( versionCheckerConfig.isVersionCheckingEnabled() )
		{
			final VersionChecker versionChecker = new VersionChecker( versionCheckerConfig );
			final Thread versionCheckerThread = new Thread( versionChecker );

			this.startService( "AE2 VersionChecker", versionCheckerThread );
		}

		AELog.info( "Pre Initialization ( ended after " + watch.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );

		// Instantiate all Plugins
		List<Object> injectables = Lists.newArrayList( AEApi.instance() );
		new PluginLoader().loadPlugins( injectables, event.getAsmData() );
	}

	private void startService( final String serviceName, final Thread thread )
	{
		thread.setName( serviceName );
		thread.setPriority( Thread.MIN_PRIORITY );

		AELog.info( "Starting " + serviceName );
		thread.start();
	}

	@EventHandler
	private void init( final FMLInitializationEvent event )
	{
		final Stopwatch start = Stopwatch.createStarted();
		AELog.info( "Initialization ( started )" );

		AppEng.proxy.init();

		if( this.exportConfig.isExportingItemNamesEnabled() )
		{
			if( FMLCommonHandler.instance().getSide().isClient() )
			{
				final ExportProcess process = new ExportProcess( this.configDirectory, this.exportConfig );
				final Thread exportProcessThread = new Thread( process );

				this.startService( "AE2 CSV Export", exportProcessThread );
			}
			else
			{
				AELog.info( "Disabling item.csv export for custom recipes, since creative tab information is only available on the client." );
			}
		}

		this.registration.initialize( event, this.configDirectory );
		IntegrationRegistry.INSTANCE.init();

		AELog.info( "Initialization ( ended after " + start.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	@EventHandler
	private void postInit( final FMLPostInitializationEvent event )
	{
		final Stopwatch start = Stopwatch.createStarted();
		AELog.info( "Post Initialization ( started )" );

		this.registration.postInit( event );
		IntegrationRegistry.INSTANCE.postInit();
		FMLCommonHandler.instance().registerCrashCallable( new IntegrationCrashEnhancement() );

		AppEng.proxy.postInit();
		AEConfig.instance().save();

		NonBlockingItems.INSTANCE.init();

		NetworkRegistry.INSTANCE.registerGuiHandler( this, GuiBridge.GUI_Handler );
		NetworkHandler.init( "AE2" );

		AELog.info( "Post Initialization ( ended after " + start.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	@EventHandler
	private void handleIMCEvent( final FMLInterModComms.IMCEvent event )
	{
		final IMCHandler imcHandler = new IMCHandler();

		imcHandler.handleIMCEvent( event );
	}

	@EventHandler
	private void serverAboutToStart( final FMLServerAboutToStartEvent evt )
	{
		WorldData.onServerAboutToStart( evt.getServer() );
	}

	@EventHandler
	private void serverStopping( final FMLServerStoppingEvent event )
	{
		WorldData.instance().onServerStopping();
	}

	@EventHandler
	private void serverStopped( final FMLServerStoppedEvent event )
	{
		WorldData.instance().onServerStoppped();
		TickHandler.INSTANCE.shutdown();
	}

	@EventHandler
	private void serverStarting( final FMLServerStartingEvent evt )
	{
		evt.registerServerCommand( new AECommand( evt.getServer() ) );
	}
}
