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

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import appeng.api.parts.CableRenderMode;
import appeng.block.AEBaseBlock;
import appeng.client.ActionKey;
import appeng.client.EffectType;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.sync.BasePacket;

public interface AppEng {

    String MOD_NAME = "Applied Energistics 2";

    String MOD_ID = "appliedenergistics2";

    static AppEng instance() {
        return AppEngHolder.INSTANCE;
    }

    static Identifier makeId(String id) {
        return new Identifier(MOD_ID, id);
    }

    AdvancementTriggers getAdvancementTriggers();

    Stream<? extends PlayerEntity> getPlayers();

    void sendToAllNearExcept(PlayerEntity p, double x, double y, double z, double dist, World w, BasePacket packet);

    void spawnEffect(EffectType effect, World world, double posX, double posY, double posZ, Object extra);

    boolean shouldAddParticles(Random r);

    HitResult getRTR();

    void postInit();

    CableRenderMode getCableRenderMode();

    /**
     * Sets the player that is currently interacting with a cable or part attached to a cable. This will return that
     * player's cable render mode from calls to {@link #getCableRenderMode()}, until another player or null is set.
     * @param player Null to revert to the default cable render mode.
     */
    void setPartInteractionPlayer(PlayerEntity player);

    boolean isActionKey(@Nonnull final ActionKey key, int keyCode, int scanCode);

    /**
     * Get the currently running server. On the client-side this may throw if no
     * server is currently running or a remote server has been joined.
     *
     * @return The current server. Never null.
     */
    MinecraftServer getServer();

    /**
     * Checks whether the current thread is the main server thread.
     */
    boolean isOnServerThread();

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
////			AppEng.instance().preinit();
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
//        AppEng.instance().postInit();
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
