package appeng.client;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import appeng.api.parts.CableRenderMode;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.paint.PaintSplotchesModel;
import appeng.block.qnb.QnbFormedModel;
import appeng.bootstrap.ModelsReloadCallback;
import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.IItemColorRegistrationComponent;
import appeng.bootstrap.components.IModelBakeComponent;
import appeng.client.gui.implementations.CellWorkbenchScreen;
import appeng.client.gui.implementations.ChestScreen;
import appeng.client.gui.implementations.CondenserScreen;
import appeng.client.gui.implementations.CraftAmountScreen;
import appeng.client.gui.implementations.CraftConfirmScreen;
import appeng.client.gui.implementations.CraftingCPUScreen;
import appeng.client.gui.implementations.CraftingStatusScreen;
import appeng.client.gui.implementations.CraftingTermScreen;
import appeng.client.gui.implementations.DriveScreen;
import appeng.client.gui.implementations.FormationPlaneScreen;
import appeng.client.gui.implementations.GrinderScreen;
import appeng.client.gui.implementations.IOPortScreen;
import appeng.client.gui.implementations.InscriberScreen;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.implementations.InterfaceTerminalScreen;
import appeng.client.gui.implementations.LevelEmitterScreen;
import appeng.client.gui.implementations.MEMonitorableScreen;
import appeng.client.gui.implementations.MEPortableCellScreen;
import appeng.client.gui.implementations.MolecularAssemblerScreen;
import appeng.client.gui.implementations.NetworkStatusScreen;
import appeng.client.gui.implementations.NetworkToolScreen;
import appeng.client.gui.implementations.PatternTermScreen;
import appeng.client.gui.implementations.PriorityScreen;
import appeng.client.gui.implementations.QNBScreen;
import appeng.client.gui.implementations.QuartzKnifeScreen;
import appeng.client.gui.implementations.SecurityStationScreen;
import appeng.client.gui.implementations.SkyChestScreen;
import appeng.client.gui.implementations.SpatialIOPortScreen;
import appeng.client.gui.implementations.StorageBusScreen;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.implementations.VibrationChamberScreen;
import appeng.client.gui.implementations.WirelessScreen;
import appeng.client.gui.implementations.WirelessTermScreen;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.SimpleModelLoader;
import appeng.client.render.cablebus.CableBusModelLoader;
import appeng.client.render.cablebus.P2PTunnelFrequencyModel;
import appeng.client.render.crafting.CraftingCubeModel;
import appeng.client.render.effects.ChargedOreFX;
import appeng.client.render.effects.CraftingFx;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.MatterCannonFX;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.effects.VibrantFX;
import appeng.client.render.model.BiometricCardModel;
import appeng.client.render.model.ColorApplicatorModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.GlassModel;
import appeng.client.render.model.MemoryCardModel;
import appeng.client.render.model.SkyCompassModel;
import appeng.client.render.spatial.SpatialPylonModel;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.container.implementations.CellWorkbenchContainer;
import appeng.container.implementations.ChestContainer;
import appeng.container.implementations.CondenserContainer;
import appeng.container.implementations.CraftAmountContainer;
import appeng.container.implementations.CraftConfirmContainer;
import appeng.container.implementations.CraftingCPUContainer;
import appeng.container.implementations.CraftingStatusContainer;
import appeng.container.implementations.CraftingTermContainer;
import appeng.container.implementations.DriveContainer;
import appeng.container.implementations.FormationPlaneContainer;
import appeng.container.implementations.GrinderContainer;
import appeng.container.implementations.IOPortContainer;
import appeng.container.implementations.InscriberContainer;
import appeng.container.implementations.InterfaceContainer;
import appeng.container.implementations.InterfaceTerminalContainer;
import appeng.container.implementations.LevelEmitterContainer;
import appeng.container.implementations.MEMonitorableContainer;
import appeng.container.implementations.MEPortableCellContainer;
import appeng.container.implementations.MolecularAssemblerContainer;
import appeng.container.implementations.NetworkStatusContainer;
import appeng.container.implementations.NetworkToolContainer;
import appeng.container.implementations.PatternTermContainer;
import appeng.container.implementations.PriorityContainer;
import appeng.container.implementations.QNBContainer;
import appeng.container.implementations.QuartzKnifeContainer;
import appeng.container.implementations.SecurityStationContainer;
import appeng.container.implementations.SkyChestContainer;
import appeng.container.implementations.SpatialIOPortContainer;
import appeng.container.implementations.StorageBusContainer;
import appeng.container.implementations.UpgradeableContainer;
import appeng.container.implementations.VibrationChamberContainer;
import appeng.container.implementations.WirelessContainer;
import appeng.container.implementations.WirelessTermContainer;
import appeng.core.Api;
import appeng.core.ApiDefinitions;
import appeng.core.AppEng;
import appeng.core.AppEngBase;
import appeng.core.features.registries.PartModels;
import appeng.core.sync.network.ClientNetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.entity.ChargedQuartzEntity;
import appeng.entity.GrowingCrystalEntity;
import appeng.entity.SingularityEntity;
import appeng.entity.TinyTNTPrimedEntity;
import appeng.entity.TinyTNTPrimedRenderer;
import appeng.fluids.client.gui.FluidFormationPlaneScreen;
import appeng.fluids.client.gui.FluidIOScreen;
import appeng.fluids.client.gui.FluidInterfaceScreen;
import appeng.fluids.client.gui.FluidLevelEmitterScreen;
import appeng.fluids.client.gui.FluidStorageBusScreen;
import appeng.fluids.client.gui.FluidTerminalScreen;
import appeng.fluids.container.FluidFormationPlaneContainer;
import appeng.fluids.container.FluidIOContainer;
import appeng.fluids.container.FluidInterfaceContainer;
import appeng.fluids.container.FluidLevelEmitterContainer;
import appeng.fluids.container.FluidStorageBusContainer;
import appeng.fluids.container.FluidTerminalContainer;
import appeng.hooks.ClientTickHandler;
import appeng.parts.automation.PlaneModel;
import appeng.tile.crafting.MolecularAssemblerRenderer;
import appeng.util.Platform;

@Environment(EnvType.CLIENT)
public final class AppEngClient extends AppEngBase {

    private final static String KEY_CATEGORY = "key.appliedenergistics2.category";

    private final Minecraft client;

    private IntegratedServer server = null;

    private final ClientNetworkHandler networkHandler;

    private final ClientTickHandler tickHandler;

    private final EnumMap<ActionKey, KeyBinding> bindings;

    public static AppEngClient instance() {
        return (AppEngClient) AppEng.instance();
    }

    public AppEngClient() {
        super();

        client = Minecraft.getInstance();
        networkHandler = new ClientNetworkHandler();
        tickHandler = new ClientTickHandler();

        ModelsReloadCallback.EVENT.register(this::onModelsReloaded);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            callDeferredBootstrapComponents(IClientSetupComponent.class, IClientSetupComponent::setup);
        });
        registerModelProviders();
        registerParticleRenderers();
        registerEntityRenderers();
        registerItemColors();
        registerTextures();
        registerScreens();

        // On the client, we'll register for server startup/shutdown to properly setup
        // WorldData
        // each time the integrated server starts&stops
        ServerLifecycleEvents.SERVER_STARTING.register(WorldData::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(WorldData::onServerStoppped);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> this.server = (IntegratedServer) server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> this.server = null);

        this.bindings = new EnumMap<>(ActionKey.class);
        for (ActionKey key : ActionKey.values()) {
            final KeyBinding binding = new KeyBinding(key.getTranslationKey(), key.getDefaultKey(), KEY_CATEGORY);
            KeyBindingHelper.registerKeyBinding(binding);
            this.bindings.put(key, binding);
        }
    }

    @Override
    public MinecraftServer getServer() {
        if (server != null) {
            return server;
        }

        throw new IllegalStateException("No server is currently running.");
    }

    @Override
    public boolean isOnServerThread() {
        return server != null && server.isOnExecutionThread();
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
        switch (client.gameSettings.particles) {
            default:
            case field_18197:
                return true;
            case field_18198:
                return r.nextBoolean();
            case field_18199:
                return false;
        }
    }

    @Override
    public RayTraceResult getRTR() {
        return client.objectMouseOver;
    }

    @Override
    public void postInit() {

    }

    @Override
    public CableRenderMode getCableRenderMode() {
        if (Platform.isServer()) {
            return super.getCableRenderMode();
        }

        final Minecraft mc = Minecraft.getInstance();
        final PlayerEntity player = mc.player;

        return this.getCableRenderModeForPlayer(player);
    }

    public void triggerUpdates() {
        if (client.player == null || client.world == null) {
            return;
        }

        final PlayerEntity player = client.player;

        final int x = (int) player.getPosX();
        final int y = (int) player.getPosY();
        final int z = (int) player.getPosZ();

        final int range = 16 * 16;

        client.worldRenderer.markBlockRangeForRenderUpdate(x - range, y - range, z - range, x + range, y + range, z + range);
    }

    @Override
    public void setPartInteractionPlayer(PlayerEntity player) {

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

        EntityRendererRegistry.Factory itemEntityFactory = (dispatcher, context) -> new ItemRenderer(dispatcher,
                context.getItemRenderer());
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

    protected void onModelsReloaded(Map<ResourceLocation, IBakedModel> loadedModels) {
        // TODO: Do not use the internal API
        final ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(IModelBakeComponent.class)
                .forEachRemaining(c -> c.onModelsReloaded(loadedModels));
    }

    public void registerTextures() {
        Stream<Collection<RenderMaterial>> sprites = Stream.of(SkyChestTESR.SPRITES, InscriberTESR.SPRITES);

        // Group every needed sprite by atlas, since every atlas has their own event
        Map<ResourceLocation, List<RenderMaterial>> groupedByAtlas = sprites.flatMap(Collection::stream)
                .collect(Collectors.groupingBy(RenderMaterial::getAtlasLocation));

        // Register to the stitch event for each atlas
        for (Map.Entry<ResourceLocation, List<RenderMaterial>> entry : groupedByAtlas.entrySet()) {
            ClientSpriteRegistryCallback.event(entry.getKey()).register((spriteAtlasTexture, registry) -> {
                for (RenderMaterial spriteIdentifier : entry.getValue()) {
                    registry.register(spriteIdentifier.getTextureLocation());
                }
            });
        }
    }

    private void registerModelProviders() {

        ModelLoadingRegistry.INSTANCE.registerAppender((resourceManager, consumer) -> {
            consumer.accept(MolecularAssemblerRenderer.LIGHTS_MODEL);
        });
        ModelLoadingRegistry.INSTANCE.registerVariantProvider((resourceManager) -> {
            return (modelIdentifier, modelProviderContext) -> {
                if (MolecularAssemblerRenderer.LIGHTS_MODEL.equals(modelIdentifier)) {
                    return modelProviderContext
                            .loadModel(new ResourceLocation(modelIdentifier.getNamespace(), modelIdentifier.getPath()));
                }
                return null;
            };
        });

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(
                rm -> new CableBusModelLoader((PartModels) Api.INSTANCE.registries().partModels()));

        addBuiltInModel("block/quartz_glass", GlassModel::new);
        addBuiltInModel("block/sky_compass", SkyCompassModel::new);
        addBuiltInModel("item/sky_compass", SkyCompassModel::new);
// FIXME FABRIC       addBuiltInModel("item/dummy_fluid_item", DummyFluidItemModel::new);
        addBuiltInModel("item/memory_card", MemoryCardModel::new);
        addBuiltInModel("item/biometric_card", BiometricCardModel::new);
        addBuiltInModel("block/drive", DriveModel::new);
        addBuiltInModel("color_applicator", ColorApplicatorModel::new); // FIXME need to wire this up (this might just
        // not be needed)
        addBuiltInModel("block/spatial_pylon", SpatialPylonModel::new);
        addBuiltInModel("block/paint", PaintSplotchesModel::new);
        addBuiltInModel("block/qnb/qnb_formed", QnbFormedModel::new);
        addBuiltInModel("part/p2p/p2p_tunnel_frequency", P2PTunnelFrequencyModel::new);
        addBuiltInModel("item/facade", FacadeItemModel::new);

        addPlaneModel("part/annihilation_plane", "part/annihilation_plane");
        addPlaneModel("part/annihilation_plane_on", "part/annihilation_plane_on");
        addPlaneModel("part/identity_annihilation_plane", "part/identity_annihilation_plane");
        addPlaneModel("part/identity_annihilation_plane_on", "part/identity_annihilation_plane_on");
        addPlaneModel("part/fluid_annihilation_plane", "part/fluid_annihilation_plane");
        addPlaneModel("part/fluid_annihilation_plane_on", "part/fluid_annihilation_plane_on");
        addPlaneModel("part/fluid_formation_plane", "part/fluid_formation_plane");
        addPlaneModel("part/fluid_formation_plane_on", "part/fluid_formation_plane_on");
        addPlaneModel("part/formation_plane", "part/formation_plane");
        addPlaneModel("part/formation_plane_on", "part/formation_plane_on");

        addBuiltInModel("block/crafting/1k_storage_formed",
                () -> new CraftingCubeModel(AbstractCraftingUnitBlock.CraftingUnitType.STORAGE_1K));
        addBuiltInModel("block/crafting/4k_storage_formed",
                () -> new CraftingCubeModel(AbstractCraftingUnitBlock.CraftingUnitType.STORAGE_4K));
        addBuiltInModel("block/crafting/16k_storage_formed",
                () -> new CraftingCubeModel(AbstractCraftingUnitBlock.CraftingUnitType.STORAGE_16K));
        addBuiltInModel("block/crafting/64k_storage_formed",
                () -> new CraftingCubeModel(AbstractCraftingUnitBlock.CraftingUnitType.STORAGE_64K));
        addBuiltInModel("block/crafting/accelerator_formed",
                () -> new CraftingCubeModel(AbstractCraftingUnitBlock.CraftingUnitType.ACCELERATOR));
        addBuiltInModel("block/crafting/monitor_formed",
                () -> new CraftingCubeModel(AbstractCraftingUnitBlock.CraftingUnitType.MONITOR));
        addBuiltInModel("block/crafting/unit_formed",
                () -> new CraftingCubeModel(AbstractCraftingUnitBlock.CraftingUnitType.UNIT));
// FIXME FABRIC       ModelLoaderRegistry.registerLoader(new Identifier(AppEng.MOD_ID, "uvlightmap"), UVLModelLoader.INSTANCE);
    }

    private static void addPlaneModel(String planeName, String frontTexture) {
        ResourceLocation frontTextureId = AppEng.makeId(frontTexture);
        ResourceLocation sidesTextureId = AppEng.makeId("part/plane_sides");
        ResourceLocation backTextureId = AppEng.makeId("part/transition_plane_back");
        addBuiltInModel(planeName, () -> new PlaneModel(frontTextureId, sidesTextureId, backTextureId));
    }

    private static <T extends IUnbakedModel> void addBuiltInModel(String id, Supplier<T> modelFactory) {
        ModelLoadingRegistry.INSTANCE
                .registerResourceProvider(resourceManager -> new SimpleModelLoader<>(AppEng.makeId(id), modelFactory));
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
