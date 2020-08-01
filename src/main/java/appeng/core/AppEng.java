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

import javax.annotation.Nonnull;

import com.google.common.base.Stopwatch;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.block.paint.PaintSplotchesModel;
import appeng.block.qnb.QnbFormedModel;
import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.IInitComponent;
import appeng.bootstrap.components.IPostInitComponent;
import appeng.capabilities.Capabilities;
import appeng.client.ClientHelper;
import appeng.client.render.DummyFluidItemModel;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.SimpleModelLoader;
import appeng.client.render.cablebus.CableBusModelLoader;
import appeng.client.render.cablebus.P2PTunnelFrequencyModel;
import appeng.client.render.crafting.CraftingCubeModelLoader;
import appeng.client.render.crafting.EncodedPatternModelLoader;
import appeng.client.render.model.BiometricCardModel;
import appeng.client.render.model.ColorApplicatorModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.GlassModel;
import appeng.client.render.model.MemoryCardModel;
import appeng.client.render.model.SkyCompassModel;
import appeng.client.render.model.UVLModelLoader;
import appeng.client.render.spatial.SpatialPylonModel;
import appeng.core.features.registries.PartModels;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.entity.ChargedQuartzEntity;
import appeng.entity.GrowingCrystalEntity;
import appeng.entity.SingularityEntity;
import appeng.entity.TinyTNTPrimedEntity;
import appeng.entity.TinyTNTPrimedRenderer;
import appeng.hooks.TickHandler;
import appeng.integration.Integrations;
import appeng.parts.PartPlacement;
import appeng.parts.automation.PlaneModelLoader;
import appeng.server.ServerHelper;

@Mod(AppEng.MOD_ID)
public final class AppEng {
    public static CommonHelper proxy;

    public static final String MOD_ID = "appliedenergistics2";
    public static final String MOD_NAME = "Applied Energistics 2";

    private static AppEng INSTANCE;

    public static ResourceLocation makeId(String id) {
        return new ResourceLocation(MOD_ID, id);
    }

    private final Registration registration;

    public AppEng() {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AEConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AEConfig.COMMON_SPEC);

        proxy = DistExecutor.unsafeRunForDist(() -> ClientHelper::new, () -> ServerHelper::new);

        CreativeTab.init();
        new FacadeItemGroup(); // This call has a side-effect (adding it to the creative screen)

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        registration = new Registration();
        modEventBus.addGenericListener(Block.class, registration::registerBlocks);
        modEventBus.addGenericListener(Item.class, registration::registerItems);
        modEventBus.addGenericListener(EntityType.class, registration::registerEntities);
        modEventBus.addGenericListener(ParticleType.class, registration::registerParticleTypes);
        modEventBus.addGenericListener(TileEntityType.class, registration::registerTileEntities);
        modEventBus.addGenericListener(ContainerType.class, registration::registerContainerTypes);
        modEventBus.addGenericListener(IRecipeSerializer.class, registration::registerRecipeSerializers);
        modEventBus.addGenericListener(Feature.class, registration::registerFeatures);
        modEventBus.addGenericListener(Structure.class, registration::registerStructures);
        modEventBus.addGenericListener(Biome.class, registration::registerBiomes);

        modEventBus.addListener(Integrations::enqueueIMC);
        modEventBus.addListener(this::commonSetup);

        // Register client-only events
        DistExecutor.runWhenOn(Dist.CLIENT, () -> registration::registerClientEvents);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(this::clientSetup));

        TickHandler.setup(MinecraftForge.EVENT_BUS);

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
        MinecraftForge.EVENT_BUS.addListener(registration::registerCommands);

        MinecraftForge.EVENT_BUS.register(new PartPlacement());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(IInitComponent.class)
                .forEachRemaining(IInitComponent::initialize);
        definitions.getRegistry().getBootstrapComponents(IPostInitComponent.class)
                .forEachRemaining(IPostInitComponent::postInitialize);

        Capabilities.register();
        Registration.setupInternalRegistries();
        Registration.postInit();

        registerNetworkHandler();

        AddonLoader.loadAddons(Api.INSTANCE);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(FMLClientSetupEvent event) {

        ((ClientHelper) proxy).clientInit();

        RenderingRegistry.registerEntityRenderingHandler(TinyTNTPrimedEntity.TYPE, TinyTNTPrimedRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SingularityEntity.TYPE,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(GrowingCrystalEntity.TYPE,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ChargedQuartzEntity.TYPE,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));

        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(IClientSetupComponent.class)
                .forEachRemaining(IClientSetupComponent::setup);

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
        addBuiltInModel("facade", FacadeItemModel::new);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "encoded_pattern"),
                EncodedPatternModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "part_plane"),
                PlaneModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "crafting_cube"),
                CraftingCubeModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "uvlightmap"), UVLModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "cable_bus"),
                new CableBusModelLoader((PartModels) Api.INSTANCE.registries().partModels()));

    }

    @OnlyIn(Dist.CLIENT)
    private static <T extends IModelGeometry<T>> void addBuiltInModel(String id, Supplier<T> modelFactory) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, id),
                new SimpleModelLoader<>(modelFactory));
    }

    @Nonnull
    public static AppEng instance() {
        if (INSTANCE == null) {
            throw new IllegalStateException();
        }
        return INSTANCE;
    }

    public AdvancementTriggers getAdvancementTriggers() {
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
//	}

    private void startService(final String serviceName, final Thread thread) {
        thread.setName(serviceName);
        thread.setPriority(Thread.MIN_PRIORITY);

        AELog.info("Starting " + serviceName);
        thread.start();
    }

    private void registerNetworkHandler() {
        final Stopwatch start = Stopwatch.createStarted();
        AELog.info("Post Initialization ( started )");

        // FIXME IntegrationRegistry.INSTANCE.postInit();
        // FIXME CrashReportExtender.registerCrashCallable( new
        // IntegrationCrashEnhancement() );

        AppEng.proxy.postInit();
        AEConfig.instance().save();

        NetworkHandler.init(new ResourceLocation(MOD_ID, "main"));

        AELog.info("Post Initialization ( ended after " + start.elapsed(TimeUnit.MILLISECONDS) + "ms )");
    }

    private void onServerAboutToStart(final FMLServerAboutToStartEvent evt) {
        WorldData.onServerStarting(evt.getServer());
    }

    private void serverStopping(final FMLServerStoppingEvent event) {
        WorldData.instance().onServerStopping();
    }

    private void serverStopped(final FMLServerStoppedEvent event) {
        WorldData.instance().onServerStoppped();
        TickHandler.instance().shutdown();
    }

}
