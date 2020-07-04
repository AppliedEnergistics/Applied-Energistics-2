package appeng.client;

import appeng.api.parts.CableRenderMode;
import appeng.bootstrap.ModelsReloadCallback;
import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.IItemColorRegistrationComponent;
import appeng.bootstrap.components.IModelBakeComponent;
import appeng.bootstrap.components.ITileEntityRegistrationComponent;
import appeng.client.gui.implementations.*;
import appeng.client.render.cablebus.CableBusModelLoader;
import appeng.client.render.effects.*;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.container.implementations.*;
import appeng.core.Api;
import appeng.core.ApiDefinitions;
import appeng.core.AppEng;
import appeng.core.AppEngBase;
import appeng.core.features.registries.PartModels;
import appeng.core.sync.network.ClientNetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.entity.*;
import appeng.hooks.ClientTickHandler;
import appeng.util.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public final class AppEngClient extends AppEngBase {

    private final MinecraftClient client;

    private final ClientNetworkHandler networkHandler;

    private final ClientTickHandler tickHandler;

    private final EnumMap<ActionKey, KeyBinding> bindings = new EnumMap<>(ActionKey.class);

    public static AppEngClient instance() {
        return (AppEngClient) AppEng.instance();
    }

    public AppEngClient() {
        super();

        client = MinecraftClient.getInstance();
        networkHandler = new ClientNetworkHandler();
        tickHandler = new ClientTickHandler();

        ModelsReloadCallback.EVENT.register(this::onModelsReloaded);

        callDeferredBootstrapComponents(IClientSetupComponent.class,
                IClientSetupComponent::setup);
        registerModelProviders();
        registerParticleRenderers();
        registerEntityRenderers();
        registerItemColors();
        registerTextures();
        registerScreens();

        // On the client, we'll register for server startup/shutdown to properly setup WorldData
        // each time the integrated server starts&stops
        ServerLifecycleEvents.SERVER_STARTED.register(WorldData::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> WorldData.instance().onServerStopping());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> WorldData.instance().onServerStoppped());
    }

    @Override
    public MinecraftServer getServer() {
        IntegratedServer server = client.getServer();
        if (server != null) {
            return server;
        }

        throw new IllegalStateException("No server is currently running.");
    }

    @Override
    public Stream<? extends PlayerEntity> getPlayers() {
        return Stream.empty();
    }

    @Override
    public void spawnEffect(EffectType effect, World world, double posX, double posY, double posZ, Object extra) {

    }

    @Override
    public boolean shouldAddParticles(Random r) {
        return false;
    }

    @Override
    public HitResult getRTR() {
        return client.crosshairTarget;
    }

    @Override
    public void postInit() {

    }

    @Override
    public CableRenderMode getRenderMode() {
        if (Platform.isServer()) {
            return super.getRenderMode();
        }

        final MinecraftClient mc = MinecraftClient.getInstance();
        final PlayerEntity player = mc.player;

        return this.renderModeForPlayer(player);
    }

    public void triggerUpdates() {
        if (client.player == null || client.world == null) {
            return;
        }

        final PlayerEntity player = client.player;

        final int x = (int) player.getX();
        final int y = (int) player.getY();
        final int z = (int) player.getZ();

        final int range = 16 * 16;

        client.worldRenderer.scheduleBlockRenders(x - range, y - range, z - range, x + range, y + range,
                z + range);
    }

    @Override
    public void updateRenderMode(PlayerEntity player) {

    }

    @Override
    public boolean isActionKey(@Nonnull ActionKey key, int keyCode, int scanCode) {
        return this.bindings.get(key).matchesKey(keyCode, scanCode);
    }

    protected void registerParticleRenderers() {
        ParticleFactoryRegistry particles = ParticleFactoryRegistry.getInstance();
        particles.register(ParticleTypes.CHARGED_ORE, ChargedOreFX.Factory::new);
        particles.register(ParticleTypes.CRAFTING, CraftingFx.Factory::new);
        particles.register(ParticleTypes.ENERGY, EnergyFx.Factory::new);
        particles.register(ParticleTypes.LIGHTNING_ARC, LightningArcFX.Factory::new);
        particles.register(ParticleTypes.LIGHTNING, LightningFX.Factory::new);
        particles.register(ParticleTypes.MATTER_CANNON, MatterCannonFX.Factory::new);
        particles.register(ParticleTypes.VIBRANT, VibrantFX.Factory::new);
    }

    protected void registerEntityRenderers() {
        EntityRendererRegistry registry = EntityRendererRegistry.INSTANCE;

        registry.register(TinyTNTPrimedEntity.TYPE, (dispatcher, context) -> new TinyTNTPrimedRenderer(dispatcher));

        EntityRendererRegistry.Factory itemEntityFactory = (dispatcher, context) -> new ItemEntityRenderer(dispatcher, context.getItemRenderer());
        registry.register(SingularityEntity.TYPE, itemEntityFactory);
        registry.register(GrowingCrystalEntity.TYPE, itemEntityFactory);
        registry.register(ChargedQuartzEntity.TYPE, itemEntityFactory);
    }

    protected void registerItemColors() {
        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(IItemColorRegistrationComponent.class)
                .forEachRemaining(IItemColorRegistrationComponent::register);
    }

    protected void onModelsReloaded(Map<Identifier, BakedModel> loadedModels) {
        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(IModelBakeComponent.class)
                .forEachRemaining(c -> c.onModelsReloaded(loadedModels));
    }

    public void registerTextures() {
        Stream<Collection<SpriteIdentifier>> sprites = Stream.of(
                SkyChestTESR.SPRITES,
                InscriberTESR.SPRITES
        );

        // Group every needed sprite by atlas, since every atlas has their own event
        Map<Identifier, List<SpriteIdentifier>> groupedByAtlas = sprites.flatMap(Collection::stream)
                .collect(Collectors.groupingBy(SpriteIdentifier::getAtlasId));

        // Register to the stitch event for each atlas
        for (Map.Entry<Identifier, List<SpriteIdentifier>> entry : groupedByAtlas.entrySet()) {
            ClientSpriteRegistryCallback.event(entry.getKey())
                    .register((spriteAtlasTexture, registry) -> {
                        for (SpriteIdentifier spriteIdentifier : entry.getValue()) {
                            registry.register(spriteIdentifier.getTextureId());
                        }
                    });
        }
    }

    private void registerModelProviders() {

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new CableBusModelLoader((PartModels) Api.INSTANCE.registries().partModels()));

// FIXME FABRIC       addBuiltInModel("glass", GlassModel::new);
// FIXME FABRIC       addBuiltInModel("sky_compass", SkyCompassModel::new);
// FIXME FABRIC       addBuiltInModel("dummy_fluid_item", DummyFluidItemModel::new);
// FIXME FABRIC       addBuiltInModel("memory_card", MemoryCardModel::new);
// FIXME FABRIC       addBuiltInModel("biometric_card", BiometricCardModel::new);
// FIXME FABRIC       addBuiltInModel("drive", DriveModel::new);
// FIXME FABRIC       addBuiltInModel("color_applicator", ColorApplicatorModel::new);
// FIXME FABRIC       addBuiltInModel("spatial_pylon", SpatialPylonModel::new);
// FIXME FABRIC       addBuiltInModel("paint_splotches", PaintSplotchesModel::new);
// FIXME FABRIC       addBuiltInModel("quantum_bridge_formed", QnbFormedModel::new);
// FIXME FABRIC       addBuiltInModel("p2p_tunnel_frequency", P2PTunnelFrequencyModel::new);
// FIXME FABRIC       addBuiltInModel("facade", FacadeItemModel::new);
// FIXME FABRIC       ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "encoded_pattern"),
// FIXME FABRIC               EncodedPatternModelLoader.INSTANCE);
// FIXME FABRIC       ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "part_plane"),
// FIXME FABRIC               PlaneModelLoader.INSTANCE);
// FIXME FABRIC       ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "crafting_cube"),
// FIXME FABRIC               CraftingCubeModelLoader.INSTANCE);
// FIXME FABRIC       ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "uvlightmap"), UVLModelLoader.INSTANCE);
// FIXME FABRIC       ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "cable_bus"),
// FIXME FABRIC               new CableBusModelLoader());
    }

    private void registerScreens() {
        ScreenRegistry.register(GrinderContainer.TYPE, GrinderScreen::new);
        ScreenRegistry.register(QNBContainer.TYPE, QNBScreen::new);
        ScreenRegistry.register(SkyChestContainer.TYPE, SkyChestScreen::new);
        ScreenRegistry.register(ChestContainer.TYPE, ChestScreen::new);
        ScreenRegistry.register(WirelessContainer.TYPE, WirelessScreen::new);
// FIXME FABRIC       ScreenRegistry.<MEMonitorableContainer, MEMonitorableScreen<MEMonitorableContainer>>register(
// FIXME FABRIC               MEMonitorableContainer.TYPE, MEMonitorableScreen::new);
// FIXME FABRIC       ScreenRegistry.register(MEPortableCellContainer.TYPE, MEPortableCellScreen::new);
// FIXME FABRIC       ScreenRegistry.register(WirelessTermContainer.TYPE, WirelessTermScreen::new);
// FIXME FABRIC       ScreenRegistry.register(NetworkStatusContainer.TYPE, NetworkStatusScreen::new);
// FIXME FABRIC       ScreenRegistry.<CraftingCPUContainer, CraftingCPUScreen<CraftingCPUContainer>>register(
// FIXME FABRIC               CraftingCPUContainer.TYPE, CraftingCPUScreen::new);
// FIXME FABRIC       ScreenRegistry.register(NetworkToolContainer.TYPE, NetworkToolScreen::new);
// FIXME FABRIC       ScreenRegistry.register(QuartzKnifeContainer.TYPE, QuartzKnifeScreen::new);
// FIXME FABRIC       ScreenRegistry.register(DriveContainer.TYPE, DriveScreen::new);
// FIXME FABRIC       ScreenRegistry.register(VibrationChamberContainer.TYPE, VibrationChamberScreen::new);
// FIXME FABRIC       ScreenRegistry.register(CondenserContainer.TYPE, CondenserScreen::new);
// FIXME FABRIC       ScreenRegistry.register(InterfaceContainer.TYPE, InterfaceScreen::new);
// FIXME FABRIC       ScreenRegistry.register(FluidInterfaceContainer.TYPE, FluidInterfaceScreen::new);
// FIXME FABRIC       ScreenRegistry.<UpgradeableContainer, UpgradeableScreen<UpgradeableContainer>>register(
// FIXME FABRIC               UpgradeableContainer.TYPE, UpgradeableScreen::new);
// FIXME FABRIC       ScreenRegistry.register(FluidIOContainer.TYPE, FluidIOScreen::new);
// FIXME FABRIC       ScreenRegistry.register(IOPortContainer.TYPE, IOPortScreen::new);
// FIXME FABRIC       ScreenRegistry.register(StorageBusContainer.TYPE, StorageBusScreen::new);
// FIXME FABRIC       ScreenRegistry.register(FluidStorageBusContainer.TYPE, FluidStorageBusScreen::new);
// FIXME FABRIC       ScreenRegistry.register(FormationPlaneContainer.TYPE, FormationPlaneScreen::new);
// FIXME FABRIC       ScreenRegistry.register(FluidFormationPlaneContainer.TYPE, FluidFormationPlaneScreen::new);
// FIXME FABRIC       ScreenRegistry.register(PriorityContainer.TYPE, PriorityScreen::new);
// FIXME FABRIC       ScreenRegistry.register(SecurityStationContainer.TYPE, SecurityStationScreen::new);
// FIXME FABRIC       ScreenRegistry.register(CraftingTermContainer.TYPE, CraftingTermScreen::new);
// FIXME FABRIC       ScreenRegistry.register(PatternTermContainer.TYPE, PatternTermScreen::new);
// FIXME FABRIC       ScreenRegistry.register(FluidTerminalContainer.TYPE, FluidTerminalScreen::new);
// FIXME FABRIC       ScreenRegistry.register(LevelEmitterContainer.TYPE, LevelEmitterScreen::new);
// FIXME FABRIC       ScreenRegistry.register(FluidLevelEmitterContainer.TYPE, FluidLevelEmitterScreen::new);
// FIXME FABRIC       ScreenRegistry.register(SpatialIOPortContainer.TYPE, SpatialIOPortScreen::new);
// FIXME FABRIC       ScreenRegistry.register(InscriberContainer.TYPE, InscriberScreen::new);
// FIXME FABRIC       ScreenRegistry.register(CellWorkbenchContainer.TYPE, CellWorkbenchScreen::new);
// FIXME FABRIC       ScreenRegistry.register(MolecularAssemblerContainer.TYPE, MolecularAssemblerScreen::new);
// FIXME FABRIC       ScreenRegistry.register(CraftAmountContainer.TYPE, CraftAmountScreen::new);
// FIXME FABRIC       ScreenRegistry.register(CraftConfirmContainer.TYPE, CraftConfirmScreen::new);
// FIXME FABRIC       ScreenRegistry.register(InterfaceTerminalContainer.TYPE, InterfaceTerminalScreen::new);
// FIXME FABRIC       ScreenRegistry.register(CraftingStatusContainer.TYPE, CraftingStatusScreen::new);
    }

}
