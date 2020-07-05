package appeng.client;

import appeng.api.parts.CableRenderMode;
import appeng.bootstrap.ModelsReloadCallback;
import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.IItemColorRegistrationComponent;
import appeng.bootstrap.components.IModelBakeComponent;
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
import appeng.fluids.client.gui.*;
import appeng.fluids.container.*;
import appeng.hooks.ClientTickHandler;
import appeng.util.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
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

    private final static String KEY_CATEGORY = "key.appliedenergistics2.category";

    private final MinecraftClient client;

    private final ClientNetworkHandler networkHandler;

    private final ClientTickHandler tickHandler;

    private final EnumMap<ActionKey, KeyBinding> bindings;

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

        this.bindings = new EnumMap<>(ActionKey.class);
        for (ActionKey key : ActionKey.values()) {
            final KeyBinding binding = new KeyBinding(key.getTranslationKey(), key.getDefaultKey(), KEY_CATEGORY);
            KeyBindingHelper.registerKeyBinding(binding);
            this.bindings.put(key, binding);
        }
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
       ScreenRegistry.<MEMonitorableContainer, MEMonitorableScreen<MEMonitorableContainer>>register(
               MEMonitorableContainer.TYPE, MEMonitorableScreen::new);
       ScreenRegistry.register(MEPortableCellContainer.TYPE, MEPortableCellScreen::new);
       ScreenRegistry.register(WirelessTermContainer.TYPE, WirelessTermScreen::new);
       ScreenRegistry.register(NetworkStatusContainer.TYPE, NetworkStatusScreen::new);
       ScreenRegistry.<CraftingCPUContainer, CraftingCPUScreen<CraftingCPUContainer>>register(
               CraftingCPUContainer.TYPE, CraftingCPUScreen::new);
       ScreenRegistry.register(NetworkToolContainer.TYPE, NetworkToolScreen::new);
       ScreenRegistry.register(QuartzKnifeContainer.TYPE, QuartzKnifeScreen::new);
       ScreenRegistry.register(DriveContainer.TYPE, DriveScreen::new);
       ScreenRegistry.register(VibrationChamberContainer.TYPE, VibrationChamberScreen::new);
       ScreenRegistry.register(CondenserContainer.TYPE, CondenserScreen::new);
       ScreenRegistry.register(InterfaceContainer.TYPE, InterfaceScreen::new);
       ScreenRegistry.register(FluidInterfaceContainer.TYPE, FluidInterfaceScreen::new);
       ScreenRegistry.<UpgradeableContainer, UpgradeableScreen<UpgradeableContainer>>register(
               UpgradeableContainer.TYPE, UpgradeableScreen::new);
       ScreenRegistry.register(FluidIOContainer.TYPE, FluidIOScreen::new);
       ScreenRegistry.register(IOPortContainer.TYPE, IOPortScreen::new);
       ScreenRegistry.register(StorageBusContainer.TYPE, StorageBusScreen::new);
       ScreenRegistry.register(FluidStorageBusContainer.TYPE, FluidStorageBusScreen::new);
       ScreenRegistry.register(FormationPlaneContainer.TYPE, FormationPlaneScreen::new);
       ScreenRegistry.register(FluidFormationPlaneContainer.TYPE, FluidFormationPlaneScreen::new);
       ScreenRegistry.register(PriorityContainer.TYPE, PriorityScreen::new);
       ScreenRegistry.register(SecurityStationContainer.TYPE, SecurityStationScreen::new);
       ScreenRegistry.register(CraftingTermContainer.TYPE, CraftingTermScreen::new);
       ScreenRegistry.register(PatternTermContainer.TYPE, PatternTermScreen::new);
       ScreenRegistry.register(FluidTerminalContainer.TYPE, FluidTerminalScreen::new);
       ScreenRegistry.register(LevelEmitterContainer.TYPE, LevelEmitterScreen::new);
       ScreenRegistry.register(FluidLevelEmitterContainer.TYPE, FluidLevelEmitterScreen::new);
       ScreenRegistry.register(SpatialIOPortContainer.TYPE, SpatialIOPortScreen::new);
       ScreenRegistry.register(InscriberContainer.TYPE, InscriberScreen::new);
       ScreenRegistry.register(CellWorkbenchContainer.TYPE, CellWorkbenchScreen::new);
       ScreenRegistry.register(MolecularAssemblerContainer.TYPE, MolecularAssemblerScreen::new);
       ScreenRegistry.register(CraftAmountContainer.TYPE, CraftAmountScreen::new);
       ScreenRegistry.register(CraftConfirmContainer.TYPE, CraftConfirmScreen::new);
       ScreenRegistry.register(InterfaceTerminalContainer.TYPE, InterfaceTerminalScreen::new);
       ScreenRegistry.register(CraftingStatusContainer.TYPE, CraftingStatusScreen::new);
    }

}
