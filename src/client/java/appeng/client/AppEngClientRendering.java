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

import appeng.api.parts.CableRenderMode;
import appeng.api.parts.PartModelsInternal;
import appeng.api.util.AEColor;
import appeng.block.networking.CableBusColor;
import appeng.block.qnb.QnbFormedModel;
import appeng.client.model.PaintSplotchesModel;
import appeng.client.model.SpatialPylonModel;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.cablebus.CableBusModel;
import appeng.client.render.cablebus.P2PTunnelFrequencyModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.MeteoriteCompassModel;
import appeng.client.renderer.blockentity.CableBusRenderer;
import appeng.client.areaoverlay.AreaOverlayRenderer;
import appeng.client.block.cablebus.CableBusBlockClientExtensions;
import appeng.client.hooks.RenderBlockOutlineHook;
import appeng.client.item.ColorApplicatorItemModel;
import appeng.client.item.EnergyFillLevelProperty;
import appeng.client.item.PortableCellColorTintSource;
import appeng.client.item.StorageCellStateTintSource;
import appeng.client.render.AERenderPipelines;
import appeng.client.render.ColorableBlockEntityBlockColor;
import appeng.client.render.StaticBlockColor;
import appeng.client.renderer.blockentity.CraftingMonitorRenderer;
import appeng.client.renderer.blockentity.DriveLedRenderer;
import appeng.client.renderer.blockentity.InscriberRenderer;
import appeng.client.renderer.blockentity.MEChestRenderer;
import appeng.client.renderer.blockentity.MolecularAssemblerRenderer;
import appeng.client.render.effects.CraftingFx;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.MatterCannonFX;
import appeng.client.render.effects.VibrantFX;
import appeng.client.render.model.BuiltInModelLoader;
import appeng.client.render.model.QuartzGlassModel;
import appeng.client.render.model.MemoryCardItemModel;
import appeng.client.renderer.blockentity.ChargerRenderer;
import appeng.client.renderer.blockentity.CrankRenderer;
import appeng.client.renderer.blockentity.SkyStoneChestRenderer;
import appeng.client.renderer.SpatialStorageSkyProperties;
import appeng.client.renderer.blockentity.SkyStoneTankRenderer;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.particles.ParticleTypes;
import appeng.entity.TinyTNTPrimedRenderer;
import appeng.hooks.BuiltInModelHooks;
import appeng.client.model.PlaneModel;
import appeng.spatial.SpatialStorageDimensionIds;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Client-specific functionality.
 */
@Mod(value = AppEng.MOD_ID, dist = Dist.CLIENT)
public class AppEngClientRendering {
    private static final Logger LOG = LoggerFactory.getLogger(AppEngClientRendering.class);

    /**
     * Last known cable render mode. Used to update all rendered blocks once at the end of the tick when the mode is
     * changed.
     */
    private CableRenderMode prevCableRenderMode = CableRenderMode.STANDARD;

    public AppEngClientRendering(IEventBus modEventBus, ModContainer container) {
        // TODO InitBuiltInModels.init();

        modEventBus.addListener(this::registerEntityRenderers);
        modEventBus.addListener(this::registerEntityLayerDefinitions);
        modEventBus.addListener(this::registerModelLoader);
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
    }

    private void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(AERenderPipelines.LINES_BEHIND_BLOCK);
    }

    private void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(AppEng.makeId("block/cable_bus"), CableBusModel.Unbaked.MAP_CODEC);
        event.registerModel(QuartzGlassModel.Unbaked.ID, QuartzGlassModel.Unbaked.MAP_CODEC);
        event.registerModel(AppEng.makeId("block/drive"), DriveModel.Unbaked.MAP_CODEC);
        event.registerModel(AppEng.makeId("block/spatial_pylon"), SpatialPylonModel.Unbaked.MAP_CODEC);
        event.registerModel(AppEng.makeId("block/paint"), PaintSplotchesModel.Unbaked.MAP_CODEC);
        event.registerModel(AppEng.makeId("block/qnb_formed"), QnbFormedModel.Unbaked.MAP_CODEC);
        event.registerModel(AppEng.makeId("part/p2p/p2p_tunnel_frequency"), P2PTunnelFrequencyModel.Unbaked.MAP_CODEC);
        event.registerModel(AppEng.makeId("plane"), PlaneModel.Unbaked.MAP_CODEC);

        // Fabric doesn't have model-loaders, so we register the models by hand instead
 //  TODO 1.21.5 Datagen       addPlaneModel("part/annihilation_plane", "part/annihilation_plane");
 //  TODO 1.21.5 Datagen       addPlaneModel("part/annihilation_plane_on", "part/annihilation_plane_on");
 //  TODO 1.21.5 Datagen       addPlaneModel("part/identity_annihilation_plane", "part/identity_annihilation_plane");
 //  TODO 1.21.5 Datagen       addPlaneModel("part/identity_annihilation_plane_on", "part/identity_annihilation_plane_on");
 //  TODO 1.21.5 Datagen       addPlaneModel("part/formation_plane", "part/formation_plane");
 //  TODO 1.21.5 Datagen       addPlaneModel("part/formation_plane_on", "part/formation_plane_on");

//  TODO 1.21.5       addBuiltInModel("block/crafting/1k_storage_formed",
//  TODO 1.21.5               () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_1K)));
//  TODO 1.21.5       addBuiltInModel("block/crafting/4k_storage_formed",
//  TODO 1.21.5               () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_4K)));
//  TODO 1.21.5       addBuiltInModel("block/crafting/16k_storage_formed",
//  TODO 1.21.5               () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_16K)));
//  TODO 1.21.5       addBuiltInModel("block/crafting/64k_storage_formed",
//  TODO 1.21.5               () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_64K)));
//  TODO 1.21.5       addBuiltInModel("block/crafting/256k_storage_formed",
//  TODO 1.21.5               () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_256K)));
//  TODO 1.21.5       addBuiltInModel("block/crafting/accelerator_formed",
//  TODO 1.21.5               () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.ACCELERATOR)));
//  TODO 1.21.5       addBuiltInModel("block/crafting/monitor_formed",
//  TODO 1.21.5               () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.MONITOR)));
//  TODO 1.21.5       addBuiltInModel("block/crafting/unit_formed",
//  TODO 1.21.5               () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.UNIT)));
    }

// TODO 1.21.5 -> Move to datagen   private static void addPlaneModel(String planeName,
// TODO 1.21.5 -> Move to datagen                                     String frontTexture) {
// TODO 1.21.5 -> Move to datagen       ResourceLocation frontTextureId = AppEng.makeId(frontTexture);
// TODO 1.21.5 -> Move to datagen       ResourceLocation sidesTextureId = AppEng.makeId("part/plane_sides");
// TODO 1.21.5 -> Move to datagen       ResourceLocation backTextureId = AppEng.makeId("part/transition_plane_back");
// TODO 1.21.5 -> Move to datagen       addBuiltInModel(planeName, () -> new PlaneModel(frontTextureId, sidesTextureId, backTextureId));
// TODO 1.21.5 -> Move to datagen   }

    private static <T extends UnbakedModel> void addBuiltInModel(String id,
                                                                 Supplier<T> modelFactory) {
        BuiltInModelHooks.addBuiltInModel(AppEng.makeId(id), modelFactory.get());
    }

    private void registerModelLoader(ModelEvent.RegisterLoaders event) {
        event.register(BuiltInModelLoader.ID, new BuiltInModelLoader());
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(new CableBusBlockClientExtensions(AEBlocks.CABLE_BUS.block()), AEBlocks.CABLE_BUS.block());
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AEEntities.TINY_TNT_PRIMED.get(), TinyTNTPrimedRenderer::new);

        event.registerBlockEntityRenderer(AEBlockEntities.CRANK.get(), CrankRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.INSCRIBER.get(), InscriberRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.SKY_CHEST.get(), SkyStoneChestRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CHARGER.get(), ChargerRenderer.FACTORY);
        event.registerBlockEntityRenderer(AEBlockEntities.DRIVE.get(), DriveLedRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.ME_CHEST.get(), MEChestRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CRAFTING_MONITOR.get(), CraftingMonitorRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.MOLECULAR_ASSEMBLER.get(), MolecularAssemblerRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.CABLE_BUS.get(), CableBusRenderer::new);
        event.registerBlockEntityRenderer(AEBlockEntities.SKY_STONE_TANK.get(), SkyStoneTankRenderer::new);
    }

    private void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SkyStoneChestRenderer.MODEL_LAYER, SkyStoneChestRenderer::createSingleBodyLayer);
    }

    public void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleTypes.CRAFTING, CraftingFx.Factory::new);
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

    private SimpleModelWrapper bakeSimpleWrapper(ResolvedModel resolvedmodel, ModelBaker baker) {
        var modelState = BlockModelRotation.X0_Y0;
        TextureSlots textureslots = resolvedmodel.getTopTextureSlots();
        boolean flag = resolvedmodel.getTopAmbientOcclusion();
        TextureAtlasSprite textureatlassprite = resolvedmodel.resolveParticleSprite(textureslots, baker);
        QuadCollection quadcollection = resolvedmodel.bakeTopGeometry(textureslots, baker, modelState);
        var renderTypeGroup = resolvedmodel.getTopAdditionalProperties().getOptional(net.neoforged.neoforge.client.model.NeoForgeModelProperties.RENDER_TYPE);
        var renderTypes = renderTypeGroup == null || renderTypeGroup.isEmpty() ? null : renderTypeGroup.block();
        return new SimpleModelWrapper(quadcollection, flag, textureatlassprite, renderTypes);
    }

    private void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        event.register(MolecularAssemblerRenderer.LIGHTS_MODEL, this::bakeSimpleWrapper);
        event.register(CrankRenderer.BASE_MODEL, this::bakeSimpleWrapper);
        event.register(CrankRenderer.HANDLE_MODEL, this::bakeSimpleWrapper);
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
    }

    public void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(new StaticBlockColor(AEColor.TRANSPARENT), AEBlocks.WIRELESS_ACCESS_POINT.block());
        event.register(new CableBusColor(), AEBlocks.CABLE_BUS.block());
        event.register(ColorableBlockEntityBlockColor.INSTANCE, AEBlocks.ME_CHEST.block());
    }
}
