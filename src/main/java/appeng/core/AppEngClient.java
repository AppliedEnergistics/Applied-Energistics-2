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

import java.util.Objects;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import appeng.api.parts.CableRenderMode;
import appeng.client.EffectType;
import appeng.client.Hotkeys;
import appeng.client.commands.ClientCommands;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.style.StyleManager;
import appeng.client.guidebook.Guide;
import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.command.GuidebookStructureCommands;
import appeng.client.guidebook.compiler.TagCompiler;
import appeng.client.guidebook.extensions.ConfigValueTagExtension;
import appeng.client.guidebook.hotkey.OpenGuideHotkey;
import appeng.client.guidebook.scene.ImplicitAnnotationStrategy;
import appeng.client.guidebook.scene.PartAnnotationStrategy;
import appeng.client.guidebook.screen.GlobalInMemoryHistory;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.client.render.StorageCellClientTooltipComponent;
import appeng.client.render.effects.EnergyParticleData;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.overlay.OverlayManager;
import appeng.core.definitions.AEBlocks;
import appeng.core.network.NetworkHandler;
import appeng.core.network.serverbound.MouseWheelPacket;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.BlockAttackHook;
import appeng.hooks.RenderBlockOutlineHook;
import appeng.init.client.InitAdditionalModels;
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
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStorageSkyProperties;
import appeng.util.Platform;

/**
 * Client-specific functionality.
 */
@OnlyIn(Dist.CLIENT)
public class AppEngClient extends AppEngBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppEngClient.class);

    private static AppEngClient INSTANCE;

    /**
     * Last known cable render mode. Used to update all rendered blocks once at the end of the tick when the mode is
     * changed.
     */
    private CableRenderMode prevCableRenderMode = CableRenderMode.STANDARD;

    /**
     * This modifier key has to be held to activate mouse wheel items.
     */
    private static final KeyMapping MOUSE_WHEEL_ITEM_MODIFIER = new KeyMapping(
            "key.ae2.mouse_wheel_item_modifier", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT, "key.ae2.category");

    private final Guide guide;

    public AppEngClient(IEventBus modEventBus) {
        super(modEventBus);
        InitBuiltInModels.init();

        this.registerClientCommands();

        modEventBus.addListener(this::registerClientTooltipComponents);
        modEventBus.addListener(this::registerParticleFactories);
        modEventBus.addListener(this::modelRegistryEventAdditionalModels);
        modEventBus.addListener(this::modelRegistryEvent);
        modEventBus.addListener(this::registerBlockColors);
        modEventBus.addListener(this::registerItemColors);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::registerEntityLayerDefinitions);
        modEventBus.addListener(this::registerHotkeys);
        modEventBus.addListener(this::registerDimensionSpecialEffects);

        BlockAttackHook.install();
        RenderBlockOutlineHook.install();
        guide = createGuide(modEventBus);
        OpenGuideHotkey.init();

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (TickEvent.ClientTickEvent e) -> {
            if (e.phase == TickEvent.Phase.START) {
                updateCableRenderMode();
            }
        });

        modEventBus.addListener(this::clientSetup);

        INSTANCE = this;

        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn evt) -> {
            PendingCraftingJobs.clearPendingJobs();
            PinnedKeys.clearPinnedKeys();
        });

        NeoForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
            if (e.phase == TickEvent.Phase.END) {
                tickPinnedKeys(Minecraft.getInstance());
                Hotkeys.checkHotkeys();
            }
        });
    }

    private void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                SpatialStorageDimensionIds.DIMENSION_TYPE_ID.location(),
                SpatialStorageSkyProperties.INSTANCE);
    }

    private void registerClientCommands() {
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent evt) -> {
            var dispatcher = evt.getDispatcher();

            LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("ae2client");
            if (AEConfig.instance().isDebugToolsEnabled()) {
                for (var commandBuilder : ClientCommands.DEBUG_COMMANDS) {
                    commandBuilder.build(builder);
                }
            }
            dispatcher.register(builder);
        });
    }

    private Guide createGuide(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent evt) -> {
            var server = evt.getServer();
            var dispatcher = server.getCommands().getDispatcher();
            GuidebookStructureCommands.register(dispatcher);
        });

        return Guide.builder(modEventBus, MOD_ID, "ae2guide")
                .extension(ImplicitAnnotationStrategy.EXTENSION_POINT, new PartAnnotationStrategy())
                .extension(TagCompiler.EXTENSION_POINT, new ConfigValueTagExtension())
                .build();
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

    private void registerHotkeys(RegisterKeyMappingsEvent e) {
        if (AEConfig.instance().isGuideHotkeyEnabled()) {
            e.register(OpenGuideHotkey.getHotkey());
        }
        e.register(MOUSE_WHEEL_ITEM_MODIFIER);
        Hotkeys.finalizeRegistration(e::register);
    }

    public static AppEngClient instance() {
        return Objects.requireNonNull(INSTANCE, "AppEngClient is not initialized");
    }

    public void registerParticleFactories(RegisterParticleProvidersEvent event) {
        InitParticleFactories.init();
    }

    public void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        InitBlockColors.init(event.getBlockColors());
    }

    public void registerItemColors(RegisterColorHandlersEvent.Item event) {
        InitItemColors.init(event.getItemColors());
    }

    private void registerClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(StorageCellTooltipComponent.class, StorageCellClientTooltipComponent::new);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            try {
                postClientSetup(minecraft);
            } catch (Throwable e) {
                LOGGER.error("AE2 failed postClientSetup", e);
                throw new RuntimeException(e);
            }
        });

        NeoForge.EVENT_BUS.addListener(this::wheelEvent);
        NeoForge.EVENT_BUS.register(OverlayManager.getInstance());
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        InitEntityRendering.init(event::registerEntityRenderer);
    }

    private void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        InitEntityLayerDefinitions.init((modelLayerLocation, layerDefinition) -> {
            event.registerLayerDefinition(modelLayerLocation, () -> layerDefinition);
        });
    }

    /**
     * Called when other mods have finished initializing and the client is now available.
     */
    private void postClientSetup(Minecraft minecraft) {
        StyleManager.initialize(minecraft.getResourceManager());
        InitScreens.init();
        InitStackRenderHandlers.init();
        InitRenderTypes.init();

        // Only activate the site exporter when we're not running a release version, since it'll
        // replace blocks around spawn.
        if (!FMLLoader.isProduction()) {
            SiteExporter.initialize();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void modelRegistryEventAdditionalModels(ModelEvent.RegisterAdditional event) {
        InitAdditionalModels.init(event);
    }

    @OnlyIn(Dist.CLIENT)
    public void modelRegistryEvent(RegisterGeometryLoaders event) {
        InitBlockEntityRenderers.init();
        InitItemModelsProperties.init();
    }

    private void wheelEvent(final InputEvent.MouseScrollingEvent me) {
        if (me.getScrollDeltaY() == 0) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;
        if (MOUSE_WHEEL_ITEM_MODIFIER.isDown()) {
            var mainHand = player.getItemInHand(InteractionHand.MAIN_HAND)
                    .getItem() instanceof IMouseWheelItem;
            var offHand = player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof IMouseWheelItem;

            if (mainHand || offHand) {
                NetworkHandler.instance().sendToServer(new MouseWheelPacket(me.getScrollDeltaY() > 0));
                me.setCanceled(true);
            }
        }
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
        if (AppEngClient.instance().shouldAddParticles(level.getRandom())) {
            final double d0 = (level.getRandom().nextFloat() - 0.5F) * 0.26D;
            final double d1 = (level.getRandom().nextFloat() - 0.5F) * 0.26D;
            final double d2 = (level.getRandom().nextFloat() - 0.5F) * 0.26D;

            Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.VIBRANT, x + d0, y + d1, z + d2, 0.0D,
                    0.0D,
                    0.0D);
        }
    }

    private void spawnEnergy(Level level, double posX, double posY, double posZ) {
        var random = level.getRandom();
        final float x = (float) (Math.abs(random.nextInt()) % 100 * 0.01 - 0.5) * 0.7f;
        final float y = (float) (Math.abs(random.nextInt()) % 100 * 0.01 - 0.5) * 0.7f;
        final float z = (float) (Math.abs(random.nextInt()) % 100 * 0.01 - 0.5) * 0.7f;

        Minecraft.getInstance().particleEngine.createParticle(EnergyParticleData.FOR_BLOCK, posX + x, posY + y,
                posZ + z,
                -x * 0.1, -y * 0.1, -z * 0.1);
    }

    private void spawnLightning(Level level, double posX, double posY, double posZ) {
        Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.LIGHTNING, posX, posY + 0.3f, posZ, 0.0f,
                0.0f,
                0.0f);
    }

    private void updateCableRenderMode() {
        var currentMode = getCableRenderMode();

        // Handle changes to the cable-rendering mode
        if (currentMode == this.prevCableRenderMode) {
            return;
        }

        this.prevCableRenderMode = currentMode;

        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Invalidate all sections that contain a cable bus within view distance
        // This should asynchronously update the chunk meshes and as part of that use the new facade render mode
        var viewDistance = (int) Math.ceil(mc.levelRenderer.getLastViewDistance());
        ChunkPos.rangeClosed(mc.player.chunkPosition(), viewDistance).forEach(chunkPos -> {
            var chunk = mc.level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk != null) {
                for (var i = 0; i < chunk.getSectionsCount(); i++) {
                    var section = chunk.getSection(i);
                    if (section.maybeHas(state -> state.is(AEBlocks.CABLE_BUS.block()))) {
                        mc.levelRenderer.setSectionDirty(chunkPos.x, chunk.getSectionYFromSectionIndex(i), chunkPos.z);
                    }
                }
            }
        });
    }

    @Override
    public CableRenderMode getCableRenderMode() {
        if (Platform.isServer()) {
            return super.getCableRenderMode();
        }

        var mc = Minecraft.getInstance();
        if (mc.player == null) {
            return CableRenderMode.STANDARD;
        }

        return this.getCableRenderModeForPlayer(mc.player);
    }

    @Override
    public void openGuideAtPreviousPage(ResourceLocation initialPage) {
        try {
            var screen = GuideScreen.openAtPreviousPage(guide, PageAnchor.page(initialPage),
                    GlobalInMemoryHistory.INSTANCE);

            openGuideScreen(screen);
        } catch (Exception e) {
            LOGGER.error("Failed to open guide.", e);
        }
    }

    @Override
    public void openGuideAtAnchor(PageAnchor anchor) {
        try {
            var screen = GuideScreen.openNew(guide, anchor, GlobalInMemoryHistory.INSTANCE);

            openGuideScreen(screen);
        } catch (Exception e) {
            LOGGER.error("Failed to open guide at {}.", anchor, e);
        }
    }

    private static void openGuideScreen(GuideScreen screen) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            screen.setReturnToOnClose(minecraft.screen);
        }

        minecraft.setScreen(screen);
    }

    public Guide getGuide() {
        return guide;
    }
}
