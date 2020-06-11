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


import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import appeng.block.paint.PaintSplotchesModel;
import appeng.block.qnb.QnbFormedModel;
import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.IInitComponent;
import appeng.bootstrap.components.IPostInitComponent;
import appeng.capabilities.Capabilities;
import appeng.client.ClientHelper;
import appeng.client.render.DummyFluidItemModel;
import appeng.client.render.SimpleModelLoader;
import appeng.client.render.cablebus.CableBusModel;
import appeng.client.render.cablebus.P2PTunnelFrequencyModel;
import appeng.client.render.crafting.CraftingCubeModelLoader;
import appeng.client.render.model.*;
import appeng.client.render.spatial.SpatialPylonModel;
import appeng.core.features.registries.PartModels;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.hooks.TickHandler;
import appeng.parts.PartPlacement;
import appeng.parts.automation.PlaneModelLoader;
import appeng.server.ServerHelper;
import com.google.common.base.Stopwatch;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.config.ModConfig;

import appeng.core.crash.ModCrashEnhancement;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.*;
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

	private final Registration registration;

	/**
	 * determined in pre-init but used in init
	 */

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
		registration = new Registration();
		modEventBus.addGenericListener(Block.class, registration::registerBlocks);
		modEventBus.addGenericListener(Item.class, registration::registerItems);
		modEventBus.addGenericListener(EntityType.class, registration::registerEntities);
		modEventBus.addGenericListener(ParticleType.class, registration::registerParticleTypes);
		modEventBus.addGenericListener(TileEntityType.class, registration::registerTileEntities);
		modEventBus.addGenericListener(ContainerType.class, registration::registerContainerTypes);
		modEventBus.addGenericListener(IRecipeSerializer.class, registration::registerRecipeSerializers);
		modEventBus.addGenericListener(Feature.class, registration::registerWorldGen);
		modEventBus.addListener(registration::registerParticleFactories);
		modEventBus.addListener(registration::registerTextures);
		modEventBus.addListener(registration::registerCommands);

		modEventBus.addListener(this::commonSetup);

		// Register client-only events
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(this::clientSetup));
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(registration::modelRegistryEvent));
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(registration::registerItemColors));
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(registration::handleModelBake));

		MinecraftForge.EVENT_BUS.addListener( TickHandler.INSTANCE::unloadWorld );
		MinecraftForge.EVENT_BUS.addListener( TickHandler.INSTANCE::onTick );
		MinecraftForge.EVENT_BUS.addListener( this::onServerAboutToStart );
		MinecraftForge.EVENT_BUS.addListener( this::serverStopped );
		MinecraftForge.EVENT_BUS.addListener( this::serverStopping );

		MinecraftForge.EVENT_BUS.register( new PartPlacement() );
	}

	private void commonSetup(FMLCommonSetupEvent event) {

		ApiDefinitions definitions = Api.INSTANCE.definitions();
		definitions.getRegistry().getBootstrapComponents( IInitComponent.class ).forEachRemaining(IInitComponent::initialize);
		definitions.getRegistry().getBootstrapComponents( IPostInitComponent.class ).forEachRemaining(IPostInitComponent::postInitialize);

		Capabilities.register();
		Registration.setupInternalRegistries();
		Registration.postInit();

		registerNetworkHandler();

	}

	@OnlyIn(Dist.CLIENT)
	private void clientSetup(FMLClientSetupEvent event) {

		((ClientHelper) proxy).clientInit();

		// Do not register the Fullbright hacks if Optifine is present or if the Forge lighting is disabled
		// FIXME if( !FMLClientHandler.instance().hasOptifine() && ForgeModContainer.forgeLightPipelineEnabled )
		// FIXME {
		// FIXME 	ModelLoaderRegistry.registerLoader( UVLModelLoader.INSTANCE );
		// FIXME }

		// FIXME RenderingRegistry.registerEntityRenderingHandler( EntityTinyTNTPrimed.class, manager -> new RenderTinyTNTPrimed( manager ) );
		// FIXME RenderingRegistry.registerEntityRenderingHandler( EntityFloatingItem.class, manager -> new RenderFloatingItem( manager ) );

		// TODO: Do not use the internal API
		final ApiDefinitions definitions = Api.INSTANCE.definitions();
		definitions.getRegistry().getBootstrapComponents( IClientSetupComponent.class ).forEachRemaining(IClientSetupComponent::setup);

		addBuiltInModel("glass", GlassModel::new);
		addBuiltInModel("sky_compass", SkyCompassModel::new);
		addBuiltInModel("dummy_fluid_item", DummyFluidItemModel::new);
		addBuiltInModel("memory_card", MemoryCardModel::new);
		addBuiltInModel("biometric_card", BiometricCardModel::new);
		addBuiltInModel("drive", DriveModel::new);
		addBuiltInModel("color_applicator", ColorApplicatorModel::new);
		addBuiltInModel("spatial_pylon", SpatialPylonModel::new);
		addBuiltInModel("paint_splotches", PaintSplotchesModel::new);
		addBuiltInModel("quantum_bridge_formed", QnbFormedModel::new);
		addBuiltInModel("p2p_tunnel_frequency", P2PTunnelFrequencyModel::new);
		ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "part_plane"), PlaneModelLoader.INSTANCE);
		ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "crafting_cube"), CraftingCubeModelLoader.INSTANCE);
		ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "uvlightmap"), UVLModelLoader.INSTANCE);
		addBuiltInModel("cable_bus", () -> new CableBusModel((PartModels) Api.INSTANCE.registries().partModels()));

	}

	private static <T extends IModelGeometry<T>> void addBuiltInModel(String id, Supplier<T> modelFactory) {
		ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, id), new SimpleModelLoader<T>(modelFactory));
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
		return this.registration.advancementTriggers;
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
//		AELog.info( "Pre Initialization ( ended after " + watch.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
//
//		// Instantiate all Plugins
//		List<Object> injectables = Lists.newArrayList(
//				AEApi.instance() );
//		new PluginLoader().loadPlugins( injectables, event.getAsmData() );
//	}

	private void startService( final String serviceName, final Thread thread )
	{
		thread.setName( serviceName );
		thread.setPriority( Thread.MIN_PRIORITY );

		AELog.info( "Starting " + serviceName );
		thread.start();
	}

	private void registerNetworkHandler()
	{
		final Stopwatch start = Stopwatch.createStarted();
		AELog.info( "Post Initialization ( started )" );

		// FIXME IntegrationRegistry.INSTANCE.postInit();
		// FIXME CrashReportExtender.registerCrashCallable( new IntegrationCrashEnhancement() );

		AppEng.proxy.postInit();
		AEConfig.instance().save();

		NetworkHandler.init( new ResourceLocation(MOD_ID, "main") );

		AELog.info( "Post Initialization ( ended after " + start.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
	}

	private void onServerAboutToStart(final FMLServerAboutToStartEvent evt)
	{
		WorldData.onServerStarting( evt.getServer() );
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

}
