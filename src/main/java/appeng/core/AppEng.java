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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.IInitComponent;
import appeng.client.ClientHelper;
import appeng.client.render.model.AutoRotatingModel;
import appeng.client.render.model.AutoRotatingModelLoader;
import appeng.client.render.model.GlassModelLoader;
import appeng.client.render.model.SkyCompassModelLoader;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.worlddata.WorldData;
import appeng.hooks.TickHandler;
import appeng.parts.PartPlacement;
import appeng.server.ServerHelper;
import com.google.gson.Gson;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.BlockModelDefinition;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;

import appeng.core.crash.ModCrashEnhancement;
import appeng.services.export.ExportConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nonnull;


@Mod(AppEng.MOD_ID)
public final class AppEng
{
	public static CommonHelper proxy;

	public static final String MOD_ID = "appliedenergistics2";
	public static final String MOD_NAME = "Applied Energistics 2";

	public static final String ASSETS = "appliedenergistics2:";

	// FIXME replicate this in mods.toml!
	// FIXME private static final String FORGE_CURRENT_VERSION = ForgeVersion.getVersion();
	// FIXME private static final String FORGE_MAX_VERSION = ( ForgeVersion.majorVersion + 1 ) + ".0.0.0";
	// FIXME public static final String MOD_DEPENDENCIES = "required-after:forge@[" + FORGE_CURRENT_VERSION + "," + FORGE_MAX_VERSION + ");after:ctm@[" + CTM.VERSION + ",);";

	private static AppEng INSTANCE;

//FIXME	private final Registration registration;

	private File configDirectory;

	/**
	 * determined in pre-init but used in init
	 */
	private ExportConfig exportConfig;

	public AppEng()
	{
		if (INSTANCE != null) {
			throw new IllegalStateException();
		}
		INSTANCE = this;

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AEConfig.CLIENT_SPEC);

		proxy = DistExecutor.runForDist(() -> ClientHelper::new, () -> ServerHelper::new);

		CrashReportExtender.registerCrashCallable( new ModCrashEnhancement() );

		//FIXMEthis.registration = new Registration();
		//FIXMEMinecraftForge.EVENT_BUS.register( this.registration );

		CreativeTab.init();
		CreativeTabFacade.init();

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		Registration registration = new Registration();
		modEventBus.addGenericListener(Block.class, registration::registerBlocks);
		modEventBus.addGenericListener(Item.class, registration::registerItems);
		modEventBus.addGenericListener(EntityType.class, registration::registerEntities);
		modEventBus.addGenericListener(ParticleType.class, registration::registerParticleTypes);
		modEventBus.addGenericListener(TileEntityType.class, registration::registerTileEntities);
		modEventBus.addGenericListener(ContainerType.class, registration::registerContainerTypes);
		modEventBus.addGenericListener(IRecipeSerializer.class, registration::registerRecipeSerializers);
		modEventBus.addListener(registration::registerParticleFactories);
		modEventBus.addListener(registration::registerTextures);

		modEventBus.addListener(this::commonSetup);

		// Register client-only events
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(this::clientSetup));
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(registration::modelRegistryEvent));

		MinecraftForge.EVENT_BUS.addListener( TickHandler.INSTANCE::unloadWorld );
		MinecraftForge.EVENT_BUS.addListener( TickHandler.INSTANCE::onTick );
		MinecraftForge.EVENT_BUS.addListener( this::serverAboutToStart );
		MinecraftForge.EVENT_BUS.addListener( this::serverStopped );
		MinecraftForge.EVENT_BUS.addListener( this::serverStopping );

		MinecraftForge.EVENT_BUS.register( new PartPlacement() );
	}

	private void commonSetup(FMLCommonSetupEvent event) {

		ApiDefinitions definitions = Api.INSTANCE.definitions();
		definitions.getRegistry().getBootstrapComponents( IInitComponent.class ).forEachRemaining(IInitComponent::initialize);

		Registration.setupInternalRegistries();
	}

	@OnlyIn(Dist.CLIENT)
	private void clientSetup(FMLClientSetupEvent event) {
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		definitions.getRegistry().getBootstrapComponents( IClientSetupComponent.class ).forEachRemaining(IClientSetupComponent::setup);
		ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "glass"), GlassModelLoader.INSTANCE);
		ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "sky_compass"), SkyCompassModelLoader.INSTANCE);
		ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "auto_rotating"), AutoRotatingModelLoader.INSTANCE);
	}

	@Nonnull
	public static AppEng instance()
	{
		if (INSTANCE == null) {
			throw new IllegalStateException();
		}
		return INSTANCE;
	}

//	public Biome getStorageBiome()
//	{
//		return this.registration.storageBiome;
//	}
//
//	public DimensionType getStorageDimensionType()
//	{
//		return this.registration.storageDimensionType;
//	}
//
//	public int getStorageDimensionID()
//	{
//		return this.registration.storageDimensionID;
//	}

	public AdvancementTriggers getAdvancementTriggers()
	{
		return null; // FIXME this.registration.advancementTriggers;
	}

//	@EventHandler
//	private void preInit( final FMLPreInitializationEvent event )
//	{
//		final Stopwatch watch = Stopwatch.createStarted();
//		this.configDirectory = new File( event.getModConfigurationDirectory().getPath(), "AppliedEnergistics2" );
//
//		final File configFile = new File( this.configDirectory, "AppliedEnergistics2.cfg" );
//		final File facadeFile = new File( this.configDirectory, "Facades.cfg" );
//		final File versionFile = new File( this.configDirectory, "VersionChecker.cfg" );
//		final File recipeFile = new File( this.configDirectory, "CustomRecipes.cfg" );
//		final Configuration recipeConfiguration = new Configuration( recipeFile );
//
//		AEConfig.init( configFile );
//		FacadeConfig.init( facadeFile );
//
//		final VersionCheckerConfig versionCheckerConfig = new VersionCheckerConfig( versionFile );
//		this.exportConfig = new ForgeExportConfig( recipeConfiguration );
//
//		AELog.info( "Pre Initialization ( started )" );
//
//
//		for( final IntegrationType type : IntegrationType.values() )
//		{
//			IntegrationRegistry.INSTANCE.add( type );
//		}
//
//		this.registration.preInitialize( event );
//
//		if( Platform.isClient() )
//		{
//			AppEng.proxy.preinit();
//		}
//
//		IntegrationRegistry.INSTANCE.preInit();
//
//		if( versionCheckerConfig.isVersionCheckingEnabled() )
//		{
//			final VersionChecker versionChecker = new VersionChecker( versionCheckerConfig );
//			final Thread versionCheckerThread = new Thread( versionChecker );
//
//			this.startService( "AE2 VersionChecker", versionCheckerThread );
//		}
//
//		AELog.info( "Pre Initialization ( ended after " + watch.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
//
//		// Instantiate all Plugins
//		List<Object> injectables = Lists.newArrayList(
//				Api.INSTANCE );
//		new PluginLoader().loadPlugins( injectables, event.getAsmData() );
//	}
//
//	private void startService( final String serviceName, final Thread thread )
//	{
//		thread.setName( serviceName );
//		thread.setPriority( Thread.MIN_PRIORITY );
//
//		AELog.info( "Starting " + serviceName );
//		thread.start();
//	}
//
//	@EventHandler
//	private void init( final FMLCommonSetupEvent event )
//	{
//		final Stopwatch start = Stopwatch.createStarted();
//		AELog.info( "Initialization ( started )" );
//
//		AppEng.proxy.init();
//
//		if( this.exportConfig.isExportingItemNamesEnabled() )
//		{
//			if( FMLCommonHandler.instance().getSide().isClient() )
//			{
//				final ExportProcess process = new ExportProcess( this.configDirectory, this.exportConfig );
//				final Thread exportProcessThread = new Thread( process );
//
//				this.startService( "AE2 CSV Export", exportProcessThread );
//			}
//			else
//			{
//				AELog.info( "Disabling item.csv export for custom recipes, since creative tab information is only available on the client." );
//			}
//		}
//
//		this.registration.initialize( event, this.configDirectory );
//		IntegrationRegistry.INSTANCE.init();
//
//		AELog.info( "Initialization ( ended after " + start.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
//	}
//
//	@EventHandler
//	private void postInit( final FMLPostInitializationEvent event )
//	{
//		final Stopwatch start = Stopwatch.createStarted();
//		AELog.info( "Post Initialization ( started )" );
//
//		this.registration.postInit( event );
//		IntegrationRegistry.INSTANCE.postInit();
//		CrashReportExtender.registerCrashCallable( new IntegrationCrashEnhancement() );
//
//		AppEng.proxy.postInit();
//		AEConfig.instance().save();
//
//		NetworkRegistry.INSTANCE.registerGuiHandler( this, GuiBridge.GUI_Handler );
//		NetworkHandler.init( "AE2" );
//
//		AELog.info( "Post Initialization ( ended after " + start.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
//	}
//
//	@EventHandler
//	private void handleIMCEvent( final FMLInterModComms.IMCEvent event )
//	{
//		final IMCHandler imcHandler = new IMCHandler();
//
//		imcHandler.handleIMCEvent( event );
//	}

	private void serverAboutToStart( final FMLServerStartedEvent evt )
	{
		WorldData.onServerAboutToStart( evt.getServer() );
	}

	private void serverStopping( final FMLServerStoppingEvent event )
	{
		WorldData.instance().onServerStopping();
	}

	private void serverStopped( final FMLServerStoppedEvent event )
	{
		WorldData.instance().onServerStoppped();
		TickHandler.INSTANCE.shutdown();
	}

//	@EventHandler
//	private void serverStarting( final FMLServerStartingEvent evt )
//	{
//		evt.registerServerCommand( new AECommand( evt.getServer() ) );
//	}
}
