package appeng.client;

import appeng.api.parts.CableRenderMode;
import appeng.bootstrap.ModelsReloadCallback;
import appeng.bootstrap.components.IItemColorRegistrationComponent;
import appeng.bootstrap.components.IModelBakeComponent;
import appeng.client.render.cablebus.CableBusModelLoader;
import appeng.client.render.effects.*;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.core.Api;
import appeng.core.ApiDefinitions;
import appeng.core.AppEng;
import appeng.core.AppEngBase;
import appeng.core.features.registries.PartModels;
import appeng.core.sync.network.ClientNetworkHandler;
import appeng.entity.*;
import appeng.hooks.ClientTickHandler;
import appeng.util.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public final class AppEngClient extends AppEngBase {

    private final MinecraftClient client;

    private final ClientNetworkHandler networkHandler;

    private final ClientTickHandler tickHandler;

    public static AppEngClient instance() {
        return (AppEngClient) AppEng.instance();
    }

    public AppEngClient() {
        super();

        client = MinecraftClient.getInstance();
        networkHandler = new ClientNetworkHandler();
        tickHandler = new ClientTickHandler();

        ModelsReloadCallback.EVENT.register(this::onModelsReloaded);

        registerModelProviders();
        registerParticleRenderers();
        registerEntityRenderers();
        registerItemColors();
        registerTextures();
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
    public boolean isActionKey(@Nonnull ActionKey key, InputUtil.Key input) {
        return false;
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
        // FIXME FABRIC InscriberTESR.registerTexture();
        Stream<Collection<SpriteIdentifier>> sprites = Stream.of(
                SkyChestTESR.SPRITES
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

}
