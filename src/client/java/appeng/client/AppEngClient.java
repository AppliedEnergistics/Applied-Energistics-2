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

package appeng.client;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InitializeClientRegistriesEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.resources.VanillaClientListeners;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;

import guideme.Guide;
import guideme.compiler.TagCompiler;
import guideme.scene.ImplicitAnnotationStrategy;
import guideme.siteexport.AdditionalResourceExporter;
import guideme.siteexport.RecipeExporter;

import appeng.api.client.StorageCellModels;
import appeng.api.parts.CableRenderMode;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.util.AEColor;
import appeng.block.qnb.QnbFormedModel;
import appeng.client.api.AEKeyRendering;
import appeng.client.api.model.parts.CompositePartModel;
import appeng.client.api.model.parts.RegisterPartModelsEvent;
import appeng.client.api.model.parts.StaticPartModel;
import appeng.client.api.renderer.parts.RegisterPartRendererEvent;
import appeng.client.areaoverlay.AreaOverlayRenderer;
import appeng.client.block.cablebus.CableBusBlockClientExtensions;
import appeng.client.block.cablebus.CableBusColor;
import appeng.client.commands.ClientCommands;
import appeng.client.gui.me.common.PendingCraftingJobs;
import appeng.client.gui.me.common.PinnedKeys;
import appeng.client.gui.style.StyleManager;
import appeng.client.guidebook.AEAdditionalExportData;
import appeng.client.guidebook.AERecipeExporter;
import appeng.client.guidebook.ConfigValueTagExtension;
import appeng.client.guidebook.PartAnnotationStrategy;
import appeng.client.hooks.BlockAttackHook;
import appeng.client.hooks.RenderBlockOutlineHook;
import appeng.client.item.ColorApplicatorItemModel;
import appeng.client.item.EnergyFillLevelProperty;
import appeng.client.item.PortableCellColorTintSource;
import appeng.client.item.StorageCellStateTintSource;
import appeng.client.model.CableAnchorPartModel;
import appeng.client.model.LevelEmitterPartModel;
import appeng.client.model.LockableMonitorPartModel;
import appeng.client.model.P2PFrequencyPartModel;
import appeng.client.model.PaintSplotchesModel;
import appeng.client.model.PartModels;
import appeng.client.model.PlanePartModel;
import appeng.client.model.SpatialPylonModel;
import appeng.client.model.StatusIndicatorPartModel;
import appeng.client.render.AEColorItemTintSource;
import appeng.client.render.AERenderPipelines;
import appeng.client.render.ColorableBlockEntityBlockColor;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.StaticBlockColor;
import appeng.client.render.StorageCellClientTooltipComponent;
import appeng.client.render.cablebus.CableBusModel;
import appeng.client.render.crafting.CraftingCubeModel;
import appeng.client.render.effects.CraftingParticle;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.MatterCannonFX;
import appeng.client.render.effects.VibrantFX;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.MemoryCardItemModel;
import appeng.client.render.model.MeteoriteCompassModel;
import appeng.client.render.model.QuartzGlassModel;
import appeng.client.render.model.SingleSpinnableVariant;
import appeng.client.renderer.SpatialStorageSkyProperties;
import appeng.client.renderer.blockentity.CableBusRenderer;
import appeng.client.renderer.blockentity.ChargerRenderer;
import appeng.client.renderer.blockentity.CraftingMonitorRenderer;
import appeng.client.renderer.blockentity.CrankRenderer;
import appeng.client.renderer.blockentity.DriveRenderer;
import appeng.client.renderer.blockentity.InscriberRenderer;
import appeng.client.renderer.blockentity.MEChestRenderer;
import appeng.client.renderer.blockentity.MolecularAssemblerRenderer;
import appeng.client.renderer.blockentity.SkyStoneChestModel;
import appeng.client.renderer.blockentity.SkyStoneChestRenderer;
import appeng.client.renderer.blockentity.SkyStoneTankRenderer;
import appeng.client.renderer.entity.TinyTNTPrimedRenderer;
import appeng.client.renderer.keytypes.FluidKeyRenderer;
import appeng.client.renderer.keytypes.ItemKeyRenderer;
import appeng.client.renderer.part.MonitorRenderer;
import appeng.client.renderer.parts.PartRendererDispatcher;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.AppEngBase;
import appeng.core.definitions.AEAttachmentTypes;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.definitions.AEItems;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.MouseWheelPacket;
import appeng.core.network.serverbound.UpdateHoldingCtrlPacket;
import appeng.core.particles.ParticleTypes;
import appeng.helpers.IMouseWheelItem;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.parts.reporting.ConversionMonitorPart;
import appeng.parts.reporting.StorageMonitorPart;
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.util.Platform;

/**
 * Client-specific functionality.
 */
@Mod(value = AppEng.MOD_ID, dist = Dist.CLIENT)
public class AppEngClient extends AppEngBase {
    private static final Logger LOG = LoggerFactory.getLogger(AppEngClient.class);
    public static final ResourceLocation MODEL_CELL_ITEMS_1K = ResourceLocation.parse(
            "ae2:block/drive_1k_item_cell");
    public static final ResourceLocation MODEL_CELL_ITEMS_4K = ResourceLocation.parse(
            "ae2:block/drive_4k_item_cell");
    public static final ResourceLocation MODEL_CELL_ITEMS_16K = ResourceLocation.parse(
            "ae2:block/drive_16k_item_cell");
    public static final ResourceLocation MODEL_CELL_ITEMS_64K = ResourceLocation.parse(
            "ae2:block/drive_64k_item_cell");
    public static final ResourceLocation MODEL_CELL_ITEMS_256K = ResourceLocation.parse(
            "ae2:block/drive_256k_item_cell");
    public static final ResourceLocation MODEL_CELL_FLUIDS_1K = ResourceLocation.parse(
            "ae2:block/drive_1k_fluid_cell");
    public static final ResourceLocation MODEL_CELL_FLUIDS_4K = ResourceLocation.parse(
            "ae2:block/drive_4k_fluid_cell");
    public static final ResourceLocation MODEL_CELL_FLUIDS_16K = ResourceLocation.parse(
            "ae2:block/drive_16k_fluid_cell");
    public static final ResourceLocation MODEL_CELL_FLUIDS_64K = ResourceLocation.parse(
            "ae2:block/drive_64k_fluid_cell");
    public static final ResourceLocation MODEL_CELL_FLUIDS_256K = ResourceLocation.parse(
            "ae2:block/drive_256k_fluid_cell");
    public static final ResourceLocation MODEL_CELL_CREATIVE = ResourceLocation.parse(
            "ae2:block/drive_creative_cell");

    /**
     * This modifier key has to be held to activate mouse wheel items.
     */
    private static final KeyMapping MOUSE_WHEEL_ITEM_MODIFIER = new KeyMapping(
            "key.ae2.mouse_wheel_item_modifier", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT, Hotkeys.CATEGORY);

    private static final KeyMapping PART_PLACEMENT_OPPOSITE = new KeyMapping(
            "key.ae2.part_placement_opposite", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
            InputConstants.KEY_LCONTROL, Hotkeys.CATEGORY);

    private static AppEngClient INSTANCE;

    /**
     * Last known cable render mode. Used to update all rendered blocks once at the end of the tick when the mode is
     * changed.
     */
    private CableRenderMode prevCableRenderMode = CableRenderMode.STANDARD;

    private final PartRendererDispatcher partRendererDispatcher = new PartRendererDispatcher();

    private PartModels partModels;

    // Recipes synchronized from the server
    private RecipeMap recipeMap = RecipeMap.EMPTY;
    private final Set<RecipeType<?>> knownRecipeTypes = Collections.newSetFromMap(new IdentityHashMap<>());

    private final Guide guide;

    public AppEngClient(IEventBus modEventBus, ModContainer container) {
        super(modEventBus, container);

        INSTANCE = this;

        this.registerClientCommands();

        modEventBus.addListener(this::registerClientTooltipComponents);
        modEventBus.addListener(this::registerHotkeys);
        modEventBus.addListener(InitScreens::init);
        modEventBus.addListener(this::registerReloadListeners);

        BlockAttackHook.install();
        guide = createGuide();

        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (ClientTickEvent.Pre e) -> {
            updateCableRenderMode();
        });

        modEventBus.addListener(this::clientSetup);

        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn evt) -> {
            PendingCraftingJobs.clearPendingJobs();
            PinnedKeys.clearPinnedKeys();
        });

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> {
            tickPinnedKeys(Minecraft.getInstance());
            Hotkeys.checkHotkeys();
        });

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, parent) -> new ConfigurationScreen(container, parent));

        modEventBus.addListener(new AEClientboundPacketHandler()::register);
        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::registerEntityLayerDefinitions);
        modEventBus.addListener(this::registerClientExtensions);
        modEventBus.addListener(this::enqueueImcMessages);
        modEventBus.addListener(this::registerParticleFactories);
        modEventBus.addListener(this::registerBlockStateModels);
        modEventBus.addListener(this::registerStandaloneModels);
        modEventBus.addListener(this::registerRenderPipelines);
        modEventBus.addListener(this::registerItemModelProperties);
        modEventBus.addListener(this::registerItemModels);
        modEventBus.addListener(this::registerDimensionSpecialEffects);
        modEventBus.addListener(this::registerItemTintSources);
        modEventBus.addListener(this::registerBlockColors);

        RenderBlockOutlineHook.install();

        var areaOverlayRenderer = new AreaOverlayRenderer();
        NeoForge.EVENT_BUS.register(areaOverlayRenderer);

        modEventBus.addListener(this::registerReloadListener);
        modEventBus.addListener(this::registerPartRenderers);
        modEventBus.addListener(this::registerPartModelTypes);
        modEventBus.addListener(this::initCustomClientRegistries);
        NeoForge.EVENT_BUS.addListener(this::receiveRecipes);
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

    private Guide createGuide() {

        return Guide.builder(AppEng.makeId("guide"))
                .folder("ae2guide")
                .extension(ImplicitAnnotationStrategy.EXTENSION_POINT, new PartAnnotationStrategy())
                .extension(TagCompiler.EXTENSION_POINT, new ConfigValueTagExtension())
                .extension(RecipeExporter.EXTENSION_POINT, new AERecipeExporter())
                .extension(AdditionalResourceExporter.EXTENSION_POINT, new AEAdditionalExportData())
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
        e.registerCategory(Hotkeys.CATEGORY);
        e.register(MOUSE_WHEEL_ITEM_MODIFIER);
        e.register(PART_PLACEMENT_OPPOSITE);
        Hotkeys.finalizeRegistration(e::register);
    }

    public static AppEngClient instance() {
        return Objects.requireNonNull(INSTANCE, "AppEngClient is not initialized");
    }

    private void registerClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(StorageCellTooltipComponent.class, StorageCellClientTooltipComponent::new);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                AEKeyRendering.register(AEKeyType.items(), AEItemKey.class, new ItemKeyRenderer());
                AEKeyRendering.register(AEKeyType.fluids(), AEFluidKey.class, new FluidKeyRenderer());
            } catch (Throwable e) {
                LOG.error("AE2 failed postClientSetup", e);
                throw new RuntimeException(e);
            }
        });

        NeoForge.EVENT_BUS.addListener(this::wheelEvent);
        NeoForge.EVENT_BUS.addListener(this::ctrlEvent);
    }

    private void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(AppEng.makeId("styles"), StyleManager.getReloadListener());
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
                ServerboundPacket message = new MouseWheelPacket(me.getScrollDeltaY() > 0);
                ClientPacketDistributor.sendToServer(message);
                me.setCanceled(true);
            }
        }
    }

    private void ctrlEvent(InputEvent.Key event) {
        if (event.getKey() == PART_PLACEMENT_OPPOSITE.getKey().getValue()) {
            var player = Minecraft.getInstance().player;

            if (player != null) {
                var isDown = event.getAction() == InputConstants.PRESS || event.getAction() == InputConstants.REPEAT;
                var previousIsDown = player.getData(AEAttachmentTypes.HOLDING_CTRL);
                if (previousIsDown != isDown) {
                    player.setData(AEAttachmentTypes.HOLDING_CTRL, isDown);
                    ClientPacketDistributor.sendToServer(new UpdateHoldingCtrlPacket(isDown));
                }
            }
        }
    }

    @Override
    public HitResult getCurrentMouseOver() {
        return Minecraft.getInstance().hitResult;
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

    public Guide getGuide() {
        return guide;
    }

    @Override
    public void sendSystemMessage(Player player, Component text) {
        if (player == Minecraft.getInstance().player) {
            Minecraft.getInstance().gui.getChat().addMessage(text);
        }
        super.sendSystemMessage(player, text);
    }

    @Override
    public RecipeMap getRecipeMapForType(Level level, RecipeType<?> recipeType) {
        if (level instanceof ClientLevel) {
            if (!knownRecipeTypes.contains(recipeType)) {
                LOG.warn("Haven't received recipes of type {} from server yet.", recipeType);
                return RecipeMap.EMPTY;
            }

            return recipeMap;
        }

        return super.getRecipeMapForType(level, recipeType);
    }

    private void registerPartModelTypes(RegisterPartModelsEvent event) {
        event.registerModelType(StaticPartModel.Unbaked.ID, StaticPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(CompositePartModel.Unbaked.ID, CompositePartModel.Unbaked.MAP_CODEC);
        event.registerModelType(P2PFrequencyPartModel.Unbaked.ID, P2PFrequencyPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(StatusIndicatorPartModel.Unbaked.ID, StatusIndicatorPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(LevelEmitterPartModel.Unbaked.ID, LevelEmitterPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(CableAnchorPartModel.Unbaked.ID, CableAnchorPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(LockableMonitorPartModel.Unbaked.ID, LockableMonitorPartModel.Unbaked.MAP_CODEC);
        event.registerModelType(PlanePartModel.Unbaked.ID, PlanePartModel.Unbaked.MAP_CODEC);
    }

    private void initCustomClientRegistries(InitializeClientRegistriesEvent event) {
        partModels = new PartModels();

        StorageCellModels.registerModel(AEItems.ITEM_CELL_1K, AppEngClient.MODEL_CELL_ITEMS_1K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_4K, AppEngClient.MODEL_CELL_ITEMS_4K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_16K, AppEngClient.MODEL_CELL_ITEMS_16K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_64K, AppEngClient.MODEL_CELL_ITEMS_64K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_256K, AppEngClient.MODEL_CELL_ITEMS_256K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_1K, AppEngClient.MODEL_CELL_FLUIDS_1K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_4K, AppEngClient.MODEL_CELL_FLUIDS_4K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_16K, AppEngClient.MODEL_CELL_FLUIDS_16K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_64K, AppEngClient.MODEL_CELL_FLUIDS_64K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_256K, AppEngClient.MODEL_CELL_FLUIDS_256K);
        StorageCellModels.registerModel(AEItems.CREATIVE_CELL, AppEngClient.MODEL_CELL_CREATIVE);

        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL1K, AppEngClient.MODEL_CELL_ITEMS_1K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL4K, AppEngClient.MODEL_CELL_ITEMS_4K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL16K, AppEngClient.MODEL_CELL_ITEMS_16K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL64K, AppEngClient.MODEL_CELL_ITEMS_64K);
        StorageCellModels.registerModel(AEItems.PORTABLE_ITEM_CELL256K, AppEngClient.MODEL_CELL_ITEMS_256K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL1K, AppEngClient.MODEL_CELL_FLUIDS_1K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL4K, AppEngClient.MODEL_CELL_FLUIDS_4K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL16K, AppEngClient.MODEL_CELL_FLUIDS_16K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL64K, AppEngClient.MODEL_CELL_FLUIDS_64K);
        StorageCellModels.registerModel(AEItems.PORTABLE_FLUID_CELL256K, AppEngClient.MODEL_CELL_FLUIDS_256K);
    }

    private void registerPartRenderers(RegisterPartRendererEvent event) {
        event.register(ConversionMonitorPart.class, new MonitorRenderer());
        event.register(StorageMonitorPart.class, new MonitorRenderer());
    }

    public PartModels getPartModels() {
        if (partModels == null) {
            throw new IllegalStateException("Client registries have not been initialized yet");
        }
        return partModels;
    }

    public PartRendererDispatcher getPartRendererDispatcher() {
        return partRendererDispatcher;
    }

    private void registerReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(PartRendererDispatcher.ID, partRendererDispatcher);
        // The block entity render for parts needs access to the formed PartRendererDispatcher
        event.addDependency(VanillaClientListeners.BLOCK_ENTITY_RENDERER, PartRendererDispatcher.ID);
    }

    private void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(AERenderPipelines.LINES_BEHIND_BLOCK);
    }

    private void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(SingleSpinnableVariant.Unbaked.ID, SingleSpinnableVariant.Unbaked.MAP_CODEC);
        event.registerModel(CableBusModel.Unbaked.ID, CableBusModel.Unbaked.MAP_CODEC);
        event.registerModel(QuartzGlassModel.Unbaked.ID, QuartzGlassModel.Unbaked.MAP_CODEC);
        event.registerModel(AppEng.makeId("drive"), DriveModel.Unbaked.MAP_CODEC);
        event.registerModel(AppEng.makeId("spatial_pylon"), SpatialPylonModel.Unbaked.MAP_CODEC);
        event.registerModel(AppEng.makeId("paint"), PaintSplotchesModel.Unbaked.MAP_CODEC);
        event.registerModel(AppEng.makeId("qnb_formed"), QnbFormedModel.Unbaked.MAP_CODEC);
        event.registerModel(CraftingCubeModel.Unbaked.ID, CraftingCubeModel.Unbaked.MAP_CODEC);
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(new CableBusBlockClientExtensions(AEBlocks.CABLE_BUS.block()), AEBlocks.CABLE_BUS.block());
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AEEntities.TINY_TNT_PRIMED.get(), TinyTNTPrimedRenderer::new);

        event.registerBlockEntityRenderer(AEBlockEntities.CRANK.get(), CrankRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.INSCRIBER.get(), InscriberRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.SKY_CHEST.get(), SkyStoneChestRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CHARGER.get(), ChargerRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.DRIVE.get(), DriveRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.ME_CHEST.get(), MEChestRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CRAFTING_MONITOR.get(), CraftingMonitorRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.MOLECULAR_ASSEMBLER.get(), MolecularAssemblerRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CABLE_BUS.get(), CableBusRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.SKY_STONE_TANK.get(), SkyStoneTankRenderer::new);
    }

    private void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SkyStoneChestRenderer.MODEL_LAYER, SkyStoneChestModel::createSingleBodyLayer);
    }

    public void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleTypes.CRAFTING, CraftingParticle.Factory::new);
        event.registerSpriteSet(ParticleTypes.ENERGY, EnergyFx.Factory::new);
        event.registerSpriteSet(ParticleTypes.LIGHTNING_ARC, LightningArcFX.Factory::new);
        event.registerSpriteSet(ParticleTypes.LIGHTNING, LightningFX.Factory::new);
        event.registerSpriteSet(ParticleTypes.MATTER_CANNON, MatterCannonFX.Factory::new);
        event.registerSpriteSet(ParticleTypes.VIBRANT, VibrantFX.Factory::new);
    }

    private void enqueueImcMessages(InterModEnqueueEvent event) {
        // Our new light-mode UI doesn't play nice with darkmodeeverywhere
        InterModComms.sendTo("darkmodeeverywhere", "dme-shaderblacklist", () -> "appeng.");
        InterModComms.sendTo("framedblocks", "add_ct_property", () -> QuartzGlassModel.GLASS_STATE);
    }

    private void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        event.register(CrankRenderer.HANDLE_MODEL,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(CrankRenderer.HANDLE_MODEL_ID));

        // For rendering the ME chest we require the original storage cell models as standalone models
        for (var cellModelKey : StorageCellModels.standaloneModels().values()) {
            // TODO 1.21.8 Investigate what model debug name vs. model key vs. resource location is
            event.register(cellModelKey,
                    SimpleUnbakedStandaloneModel.simpleModelWrapper(ResourceLocation.parse(cellModelKey.getName())));
        }
        event.register(StorageCellModels.getDefaultStandaloneModel(), SimpleUnbakedStandaloneModel
                .simpleModelWrapper(ResourceLocation.parse(StorageCellModels.getDefaultStandaloneModel().getName())));
    }

    private void registerItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(EnergyFillLevelProperty.ID, EnergyFillLevelProperty.CODEC);
    }

    private void registerItemModels(RegisterItemModelsEvent event) {
        event.register(ColorApplicatorItemModel.Unbaked.ID, ColorApplicatorItemModel.Unbaked.MAP_CODEC);
        event.register(MemoryCardItemModel.Unbaked.ID, MemoryCardItemModel.Unbaked.MAP_CODEC);
        event.register(FacadeItemModel.Unbaked.ID, FacadeItemModel.Unbaked.MAP_CODEC);
        event.register(MeteoriteCompassModel.Unbaked.ID, MeteoriteCompassModel.Unbaked.MAP_CODEC);
    }

    private void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                SpatialStorageDimensionIds.DIMENSION_TYPE_ID.location(),
                SpatialStorageSkyProperties.INSTANCE);
    }

    private void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(PortableCellColorTintSource.ID, PortableCellColorTintSource.MAP_CODEC);
        event.register(StorageCellStateTintSource.ID, StorageCellStateTintSource.MAP_CODEC);
        event.register(AEColorItemTintSource.ID, AEColorItemTintSource.MAP_CODEC);
    }

    public void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(new StaticBlockColor(AEColor.TRANSPARENT), AEBlocks.WIRELESS_ACCESS_POINT.block());
        event.register(new CableBusColor(), AEBlocks.CABLE_BUS.block());
        event.register(ColorableBlockEntityBlockColor.INSTANCE, AEBlocks.ME_CHEST.block());
    }

    private void receiveRecipes(RecipesReceivedEvent event) {
        recipeMap = event.getRecipeMap();
        knownRecipeTypes.clear();
        knownRecipeTypes.addAll(event.getRecipeTypes());
    }
}
