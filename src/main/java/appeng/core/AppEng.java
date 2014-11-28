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

package appeng.core;


import java.io.File;
import java.util.concurrent.TimeUnit;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

import com.google.common.base.Stopwatch;

import appeng.core.crash.CrashEnhancement;
import appeng.core.crash.CrashInfo;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.hooks.TickHandler;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.server.AECommand;
import appeng.services.VersionChecker;
import appeng.util.Platform;


@Mod( modid = AppEng.modid, acceptedMinecraftVersions = "[1.7.10]", name = AppEng.name, version = AEConfig.VERSION, dependencies = AppEng.dependencies, guiFactory = "appeng.client.gui.config.AEConfigGuiFactory" )
public class AppEng
{
	public final static String modid = "appliedenergistics2";
	public final static String name = "Applied Energistics 2";
	public final static String dependencies =
			// a few mods, AE should load after, probably.
			// required-after:AppliedEnergistics2API|all;
			// "after:gregtech_addon;after:Mekanism;after:IC2;after:ThermalExpansion;after:BuildCraft|Core;" +

			// depend on version of forge used for build.
			"after:appliedenergistics2-core;" + "required-after:Forge@[" // require forge.
					+ net.minecraftforge.common.ForgeVersion.majorVersion + '.' // majorVersion
					+ net.minecraftforge.common.ForgeVersion.minorVersion + '.' // minorVersion
					+ net.minecraftforge.common.ForgeVersion.revisionVersion + '.' // revisionVersion
					+ net.minecraftforge.common.ForgeVersion.buildVersion + ",)"; // buildVersion
	public static AppEng instance;

	private final IMCHandler imcHandler;

	private String configPath;

	public AppEng()
	{
		instance = this;

		this.imcHandler = new IMCHandler();

		FMLCommonHandler.instance().registerCrashCallable( new CrashEnhancement( CrashInfo.MOD_VERSION ) );
	}

	public String getConfigPath()
	{
		return configPath;
	}

	public boolean isIntegrationEnabled( IntegrationType Name )
	{
		return IntegrationRegistry.INSTANCE.isEnabled( Name );
	}

	public Object getIntegration( IntegrationType Name )
	{
		return IntegrationRegistry.INSTANCE.getInstance( Name );
	}

	@EventHandler
	void PreInit( FMLPreInitializationEvent event )
	{
		if ( !Loader.isModLoaded( "appliedenergistics2-core" ) )
		{
			CommonHelper.proxy.missingCoreMod();
		}

		Stopwatch star = Stopwatch.createStarted();
		configPath = event.getModConfigurationDirectory().getPath() + File.separator + "AppliedEnergistics2" + File.separator;

		AEConfig.instance = new AEConfig( configPath );
		FacadeConfig.instance = new FacadeConfig( configPath );

		AELog.info( "Starting ( PreInit )" );

		CreativeTab.init();
		if ( AEConfig.instance.isFeatureEnabled( AEFeature.Facades ) )
			CreativeTabFacade.init();

		if ( Platform.isClient() )
			CommonHelper.proxy.init();

		Registration.instance.PreInit( event );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.VersionChecker ) )
		{
			AELog.info( "Starting VersionChecker" );
			startService( "AE2 VersionChecker", new Thread( new VersionChecker() ) );
		}

		AELog.info( "PreInit ( end " + star.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	private void startService( String serviceName, Thread thread )
	{
		thread.setName( serviceName );
		thread.setPriority( Thread.MIN_PRIORITY );
		thread.start();
	}

	@EventHandler
	void Init( FMLInitializationEvent event )
	{
		Stopwatch star = Stopwatch.createStarted();
		AELog.info( "Init" );

		Registration.instance.Init( event );
		IntegrationRegistry.INSTANCE.init();

		AELog.info( "Init ( end " + star.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	@EventHandler
	void PostInit( FMLPostInitializationEvent event )
	{
		Stopwatch star = Stopwatch.createStarted();
		AELog.info( "PostInit" );

		Registration.instance.PostInit( event );
		IntegrationRegistry.INSTANCE.postInit();
		FMLCommonHandler.instance().registerCrashCallable( new CrashEnhancement( CrashInfo.INTEGRATION ) );

		CommonHelper.proxy.postInit();
		AEConfig.instance.save();

		NetworkRegistry.INSTANCE.registerGuiHandler( this, GuiBridge.GUI_Handler );
		NetworkHandler.instance = new NetworkHandler( "AE2" );

		AELog.info( "PostInit ( end " + star.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	@EventHandler
	public void handleIMCEvent( FMLInterModComms.IMCEvent event )
	{
		this.imcHandler.handleIMCEvent( event );
	}

	@EventHandler
	public void serverStopping( FMLServerStoppingEvent event )
	{
		WorldSettings.getInstance().shutdown();
		TickHandler.instance.shutdown();
	}

	@EventHandler
	public void serverStarting( FMLServerAboutToStartEvent evt )
	{
		WorldSettings.getInstance().init();
	}

	@EventHandler
	public void serverStarting( FMLServerStartingEvent evt )
	{
		evt.registerServerCommand( new AECommand( evt.getServer() ) );
	}
}
