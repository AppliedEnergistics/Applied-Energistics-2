/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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
import appeng.api.parts.PartHelper;
import appeng.client.EffectType;
import appeng.client.Hotkeys;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.style.StyleManager;
import appeng.client.guidebook.GuidebookManager;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.client.render.StorageCellClientTooltipComponent;
import appeng.client.render.effects.EnergyParticleData;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.overlay.OverlayManager;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.core.sync.network.ClientNetworkHandler;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.MouseWheelPacket;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.BlockAttackHook;
import appeng.hooks.ICustomPickBlock;
import appeng.hooks.MouseWheelScrolled;
import appeng.hooks.RenderBlockOutlineHook;
import appeng.hotkeys.HotkeyActions;
import appeng.init.client.InitAdditionalModels;
import appeng.init.client.InitAutoRotatingModel;
import appeng.init.client.InitBlockColors;
import appeng.init.client.InitBlockEntityRenderers;
import appeng.init.client.InitBuiltInModels;
import appeng.init.client.InitEntityLayerDefinitions;
import appeng.init.client.InitEntityRendering;
import appeng.init.client.InitItemColors;
import appeng.init.client.InitItemModelsProperties;
import appeng.init.client.InitParticleFactories;
import appeng.init.client.InitRenderTypes;
import appeng.init.client.InitScreens;
import appeng.init.client.InitStackRenderHandlers;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.siteexport.SiteExporter;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Client-specific functionality.
 */
@Environment(EnvType.CLIENT)
public class AppEngClient extends AppEngBase {
    private static AppEngClient INSTANCE;

    /**
     * Last known cable render mode. Used to update all rendered blocks once at the end of the tick when the mode is
     * changed.
     */
    private CableRenderMode prevCableRenderMode = CableRenderMode.STANDARD;

    public AppEngClient() {
        this.registerParticleFactories();
        this.registerTextures();
        this.modelRegistryEvent();
        this.registerBlockColors();
        this.registerItemColors();
        this.registerEntityRenderers();
        this.registerEntityLayerDefinitions();
        this.registerClientTooltipComponents();

        ClientPickBlockGatherCallback.EVENT.register(this::onPickBlock);
        ClientTickEvents.START_CLIENT_TICK.register(this::updateCableRenderMode);

        InitAutoRotatingModel.init();
        BlockAttackHook.install();
        RenderBlockOutlineHook.install();
        GuidebookManager.init();

        ClientLifecycleEvents.CLIENT_STARTED.register(this::clientSetup);

        INSTANCE = this;
        notifyAddons("client");
        HotkeyActions.init();
        ClientTickEvents.END_CLIENT_TICK.register(c -> Hotkeys.checkHotkeys());

        ClientTickEvents.END_CLIENT_TICK.register(this::tickPinnedKeys);

        registerTests();

        // Only activate the site exporter when we're not running a release version, since it'll
        // replace blocks around spawn.
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            SiteExporter.initialize();
        }
    }

    private void tickPinnedKeys(Minecraft minecraft) {
        // Only prune pinned keys when no screen is currently open
        if (minecraft.screen == null) {
            PinnedKeys.prune();
        }
    }

    @Override
    public Level getClientLevel() {
        return Minecraft.getInstance().level;
    }

    @Override
    public void registerHotkey(String id) {
        Hotkeys.registerHotkey(id);
    }

    public static AppEngClient instance() {
        return Objects.requireNonNull(INSTANCE, "AppEngClient is not initialized");
    }

    public void registerParticleFactories() {
        InitParticleFactories.init();
    }

    public void registerTextures() {
        Stream<Collection<Material>> sprites = Stream.of(SkyChestTESR.SPRITES, InscriberTESR.SPRITES);

        // Group every needed sprite by atlas, since every atlas has their own event
        Map<ResourceLocation, List<Material>> groupedByAtlas = sprites.flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Material::atlasLocation));

        // Register to the stitch event for each atlas
        for (Map.Entry<ResourceLocation, List<Material>> entry : groupedByAtlas.entrySet()) {
            ClientSpriteRegistryCallback.event(entry.getKey()).register((spriteAtlasTexture, registry) -> {
                for (Material spriteIdentifier : entry.getValue()) {
                    registry.register(spriteIdentifier.texture());
                }
            });
        }
    }

    public void registerBlockColors() {
        InitBlockColors.init(ColorProviderRegistry.BLOCK::register);
    }

    public void registerItemColors() {
        InitItemColors.init(ColorProviderRegistry.ITEM::register);
    }

    private void registerClientTooltipComponents() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof StorageCellTooltipComponent cellTooltipComponent) {
                return new StorageCellClientTooltipComponent(cellTooltipComponent);
            }
            return null;
        });
    }

    private void clientSetup(Minecraft client) {
        postClientSetup(client);

        MouseWheelScrolled.EVENT.register(this::wheelEvent);
        WorldRenderEvents.LAST.register(OverlayManager.getInstance()::renderWorldLastEvent);

        if (client.getOverlay() instanceof LoadingOverlay loadingOverlay) {
            ReloadInstance reloadInstance;
            try {
                reloadInstance = (ReloadInstance) FieldUtils.readField(loadingOverlay, "reload", true);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            reloadInstance.done().thenRunAsync(() -> {
                var page = GuidebookManager.INSTANCE.getPage(AppEng.makeId("index.md"));
                client.setScreen(new GuideScreen(page));
            }, client)
                    .exceptionally(throwable -> {
                        AELog.error(throwable);
                        return null;
                    });
        } else {
            var page = GuidebookManager.INSTANCE.getPage(AppEng.makeId("index.md"));
            client.setScreen(new GuideScreen(page));
        }
    }

    private void registerEntityRenderers() {
        InitEntityRendering.init(EntityRendererRegistry::register);
    }

    private void registerEntityLayerDefinitions() {
        InitEntityLayerDefinitions.init((modelLayerLocation, layerDefinition) -> {
            EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, () -> layerDefinition);
        });
    }

    /**
     * Called when other mods have finished initializing and the client is now available.
     */
    private void postClientSetup(Minecraft minecraft) {
        StyleManager.initialize(minecraft.getResourceManager());
        InitScreens.init();
        InitStackRenderHandlers.init();
    }

    @Environment(EnvType.CLIENT)
    public void modelRegistryEvent() {
        InitAdditionalModels.init();
        InitBlockEntityRenderers.init();
        InitItemModelsProperties.init();
        InitRenderTypes.init();
        InitBuiltInModels.init();
    }

    private boolean wheelEvent(double verticalAmount) {
        if (verticalAmount == 0) {
            return false;
        }

        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;
        if (InteractionUtil.isInAlternateUseMode(player)) {
            final boolean mainHand = player.getItemInHand(InteractionHand.MAIN_HAND)
                    .getItem() instanceof IMouseWheelItem;
            final boolean offHand = player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof IMouseWheelItem;

            if (mainHand || offHand) {
                NetworkHandler.instance().sendToServer(new MouseWheelPacket(verticalAmount > 0));
                return true;
            }
        }

        return false;
    }

    public boolean shouldAddParticles(RandomSource r) {
        return switch (Minecraft.getInstance().options.particles().get()) {
            case ALL -> true;
            case DECREASED -> r.nextBoolean();
            case MINIMAL -> false;
        };
    }

    @Override
    public HitResult getCurrentMouseOver() {
        return Minecraft.getInstance().hitResult;
    }

    // FIXME: Instead of doing a custom packet and this dispatcher, we can use the
    // vanilla particle system
    @Override
    public void spawnEffect(EffectType effect, Level level, double posX, double posY,
                            double posZ, Object o) {
        if (AEConfig.instance().isEnableEffects()) {
            switch (effect) {
                case Vibrant:
                    this.spawnVibrant(level, posX, posY, posZ);
                    return;
                case Energy:
                    this.spawnEnergy(level, posX, posY, posZ);
                    return;
                case Lightning:
                    this.spawnLightning(level, posX, posY, posZ);
                    return;
                default:
            }
        }
    }

    private void spawnVibrant(Level level, double x, double y, double z) {
        if (AppEngClient.instance().shouldAddParticles(Platform.getRandom())) {
            final double d0 = (Platform.getRandomFloat() - 0.5F) * 0.26D;
            final double d1 = (Platform.getRandomFloat() - 0.5F) * 0.26D;
            final double d2 = (Platform.getRandomFloat() - 0.5F) * 0.26D;

            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.VIBRANT, x + d0, y + d1, z + d2, 0.0D,
                    0.0D,
                    0.0D);
        }
    }

    private void spawnEnergy(Level level, double posX, double posY, double posZ) {
        final float x = (float) (Platform.getRandomInt() % 100 * 0.01 - 0.5) * 0.7f;
        final float y = (float) (Platform.getRandomInt() % 100 * 0.01 - 0.5) * 0.7f;
        final float z = (float) (Platform.getRandomInt() % 100 * 0.01 - 0.5) * 0.7f;

        Minecraft.getInstance().particleEngine.createParticle(EnergyParticleData.FOR_BLOCK, posX + x, posY + y,
                posZ + z,
                -x * 0.1, -y * 0.1, -z * 0.1);
    }

    private void spawnLightning(Level level, double posX, double posY, double posZ) {
        Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.LIGHTNING, posX, posY + 0.3f, posZ, 0.0f,
                0.0f,
                0.0f);
    }

    private void updateCableRenderMode(Minecraft mc) {
        var currentMode = PartHelper.getCableRenderMode();

        // Handle changes to the cable-rendering mode
        if (currentMode == this.prevCableRenderMode) {
            return;
        }

        this.prevCableRenderMode = currentMode;

        if (mc.player == null || mc.level == null) {
            return;
        }

        final Player player = mc.player;

        final int x = (int) player.getX();
        final int y = (int) player.getY();
        final int z = (int) player.getZ();

        final int range = 16 * 16;

        mc.levelRenderer.setBlocksDirty(x - range, y - range, z - range, x + range, y + range,
                z + range);
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        if (Platform.isServer()) {
            return super.getCableRenderMode();
        }

        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;

        return this.getCableRenderModeForPlayer(player);
    }

    protected void initNetworkHandler() {
        new ClientNetworkHandler();
    }

    /**
     * Replaces a Forge-Hook that was done via a method in IForgeBlock.
     */
    private ItemStack onPickBlock(Player player, HitResult hitResult) {
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = player.level.getBlockState(blockPos);

            if (blockState.getBlock() instanceof ICustomPickBlock customPickBlock) {
                return customPickBlock.getPickBlock(blockState, hitResult, player.level, blockPos, player);
            }
        }
        return ItemStack.EMPTY;
    }
}
