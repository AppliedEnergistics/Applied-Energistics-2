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

import appeng.api.parts.CableRenderMode;
import appeng.block.AEBaseBlock;
import appeng.client.ActionKey;
import appeng.client.EffectType;
import appeng.core.sync.BasePacket;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public interface AppEng {

    String MOD_NAME = "Applied Energistics 2";

    String MOD_ID = "appliedenergistics2";

    static AppEng instance() {
        return AppEngHolder.INSTANCE;
    }

    static Identifier makeId(String id) {
        return new Identifier(MOD_ID, id);
    }

    void bindTileEntitySpecialRenderer(Class<? extends BlockEntity> tile, AEBaseBlock blk);

    List<? extends PlayerEntity> getPlayers();

    void sendToAllNearExcept(PlayerEntity p, double x, double y, double z, double dist, World w,
                             BasePacket packet);

    void spawnEffect(EffectType effect, World world, double posX, double posY, double posZ,
                     Object extra);

    boolean shouldAddParticles(Random r);

    HitResult getRTR();

    void postInit();

    CableRenderMode getRenderMode();

    void triggerUpdates();

    void updateRenderMode(PlayerEntity player);

    boolean isActionKey(@Nonnull final ActionKey key, InputUtil.Key input);

//
//    private final Registration registration;
//
//    public AppEng() {
//        INSTANCE = this;
//
//        CrashReportExtender.registerCrashCallable(new ModCrashEnhancement());
//
//        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
//        registration = new Registration();
//        modEventBus.addGenericListener(Block.class, registration::registerBlocks);
//        modEventBus.addGenericListener(Item.class, registration::registerItems);
//        modEventBus.addGenericListener(EntityType.class, registration::registerEntities);
//        modEventBus.addGenericListener(ParticleType.class, registration::registerParticleTypes);
//        modEventBus.addGenericListener(BlockEntityType.class, registration::registerTileEntities);
//        modEventBus.addGenericListener(ScreenHandlerType.class, registration::registerContainerTypes);
//        modEventBus.addGenericListener(RecipeSerializer.class, registration::registerRecipeSerializers);
//        modEventBus.addGenericListener(Feature.class, registration::registerWorldGen);
//        modEventBus.addGenericListener(Biome.class, registration::registerBiomes);
//        modEventBus.addGenericListener(ModDimension.class, registration::registerModDimension);
//
//        modEventBus.addListener(Integrations::enqueueIMC);
//
//        modEventBus.addListener(this::commonSetup);
//
//        // Register client-only events
//        DistExecutor.runWhenOn(EnvType.CLIENT, () -> registration::registerClientEvents);
//        DistExecutor.runWhenOn(EnvType.CLIENT, () -> () -> modEventBus.addListener(this::clientSetup));
//
//        MinecraftForge.EVENT_BUS.addListener(TickHandler.INSTANCE::unloadWorld);
//        MinecraftForge.EVENT_BUS.addListener(TickHandler.INSTANCE::onTick);
//        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
//        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
//        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
//        MinecraftForge.EVENT_BUS.addListener(registration::registerCommands);
//
//        MinecraftForge.EVENT_BUS.register(new PartPlacement());
//    }
//
//    private void commonSetup(FMLCommonSetupEvent event) {
//
//        ApiDefinitions definitions = Api.INSTANCE.definitions();
//        definitions.getRegistry().getBootstrapComponents(IInitComponent.class)
//                .forEachRemaining(IInitComponent::initialize);
//        definitions.getRegistry().getBootstrapComponents(IPostInitComponent.class)
//                .forEachRemaining(IPostInitComponent::postInitialize);
//
//        Capabilities.register();
//        Registration.setupInternalRegistries();
//        Registration.postInit();
//
//        registerNetworkHandler();
//
//    }
//
//    @Environment(EnvType.CLIENT)
//    private void clientSetup(FMLClientSetupEvent event) {
//
//        ((ClientHelper) proxy).clientInit();
//
//        // TODO: Do not use the internal API
//        final ApiDefinitions definitions = Api.INSTANCE.definitions();
//        definitions.getRegistry().getBootstrapComponents(IClientSetupComponent.class)
//                .forEachRemaining(IClientSetupComponent::setup);
//
//        addBuiltInModel("glass", GlassModel::new);
//        addBuiltInModel("sky_compass", SkyCompassModel::new);
//        addBuiltInModel("dummy_fluid_item", DummyFluidItemModel::new);
//        addBuiltInModel("memory_card", MemoryCardModel::new);
//        addBuiltInModel("biometric_card", BiometricCardModel::new);
//        addBuiltInModel("drive", DriveModel::new);
//        addBuiltInModel("color_applicator", ColorApplicatorModel::new);
//        addBuiltInModel("spatial_pylon", SpatialPylonModel::new);
//        addBuiltInModel("paint_splotches", PaintSplotchesModel::new);
//        addBuiltInModel("quantum_bridge_formed", QnbFormedModel::new);
//        addBuiltInModel("p2p_tunnel_frequency", P2PTunnelFrequencyModel::new);
//        addBuiltInModel("facade", FacadeItemModel::new);
//        ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "encoded_pattern"),
//                EncodedPatternModelLoader.INSTANCE);
//        ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "part_plane"),
//                PlaneModelLoader.INSTANCE);
//        ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "crafting_cube"),
//                CraftingCubeModelLoader.INSTANCE);
//        ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "uvlightmap"), UVLModelLoader.INSTANCE);
//        ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "cable_bus"),
//                new CableBusModelLoader((PartModels) Api.INSTANCE.registries().partModels()));
//
//    }
//
//    @Environment(EnvType.CLIENT)
//    private static <T extends IModelGeometry<T>> void addBuiltInModel(String id, Supplier<T> modelFactory) {
//        ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, id),
//                new SimpleModelLoader<>(modelFactory));
//    }
//
//    @Nonnull
//    public static AppEng instance() {
//        if (INSTANCE == null) {
//            throw new IllegalStateException();
//        }
//        return INSTANCE;
//    }
//
//    public AdvancementTriggers getAdvancementTriggers() {
//        return this.registration.advancementTriggers;
//    }
//
////	@EventHandler
////	private void preInit( final FMLPreInitializationEvent event )
////	{
////		final Stopwatch watch = Stopwatch.createStarted();
////		this.configDirectory = new File( event.getModConfigurationDirectory().getPath(), "AppliedEnergistics2" );
////
////		final File configFile = new File( this.configDirectory, "AppliedEnergistics2.cfg" );
////		final File facadeFile = new File( this.configDirectory, "Facades.cfg" );
////		final File versionFile = new File( this.configDirectory, "VersionChecker.cfg" );
////		final File recipeFile = new File( this.configDirectory, "CustomRecipes.cfg" );
////		final Configuration recipeConfiguration = new Configuration( recipeFile );
////
////		AEConfig.init( configFile );
////		FacadeConfig.init( facadeFile );
////
////		AELog.info( "Pre Initialization ( started )" );
////
////
////		for( final IntegrationType type : IntegrationType.values() )
////		{
////			IntegrationRegistry.INSTANCE.add( type );
////		}
////
////		this.registration.preInitialize( event );
////
////		if( Platform.isClient() )
////		{
////			AppEng.proxy.preinit();
////		}
////
////		IntegrationRegistry.INSTANCE.preInit();
////
////		AELog.info( "Pre Initialization ( ended after " + watch.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
////
////		// Instantiate all Plugins
////		List<Object> injectables = Lists.newArrayList(
////				AEApi.instance() );
////		new PluginLoader().loadPlugins( injectables, event.getAsmData() );
////	}
//
//    private void startService(final String serviceName, final Thread thread) {
//        thread.setName(serviceName);
//        thread.setPriority(Thread.MIN_PRIORITY);
//
//        AELog.info("Starting " + serviceName);
//        thread.start();
//    }
//
//    private void registerNetworkHandler() {
//        final Stopwatch start = Stopwatch.createStarted();
//        AELog.info("Post Initialization ( started )");
//
//        // FIXME IntegrationRegistry.INSTANCE.postInit();
//        // FIXME CrashReportExtender.registerCrashCallable( new
//        // IntegrationCrashEnhancement() );
//
//        AppEng.proxy.postInit();
//        AEConfig.instance().save();
//
//        NetworkHandler.init(new Identifier(MOD_ID, "main"));
//
//        AELog.info("Post Initialization ( ended after " + start.elapsed(TimeUnit.MILLISECONDS) + "ms )");
//    }
//
//    private void onServerAboutToStart(final FMLServerAboutToStartEvent evt) {
//        WorldData.onServerStarting(evt.getServer());
//    }
//
//    private void serverStopping(final FMLServerStoppingEvent event) {
//        WorldData.instance().onServerStopping();
//    }
//
//    private void serverStopped(final FMLServerStoppedEvent event) {
//        WorldData.instance().onServerStoppped();
//        TickHandler.INSTANCE.shutdown();
//    }

}
