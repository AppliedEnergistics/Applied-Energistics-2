/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.client.render.cablebus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import appeng.api.parts.PartHelper;
import appeng.api.util.AEAxisAlignedBB;
import appeng.parts.misc.CableAnchorPart;
import appeng.thirdparty.codechicken.lib.model.CachedFormat;
import appeng.thirdparty.codechicken.lib.model.Quad;
import appeng.thirdparty.codechicken.lib.model.pipeline.BakedPipeline;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadAlphaOverride;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadClamper;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadCornerKicker;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadFaceStripper;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadReInterpolator;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadTinter;

/**
 * The FacadeBuilder builds for facades..
 *
 * @author covers1624
 */
public class FacadeBuilder {

    public static final double THICK_THICKNESS = 2D / 16D;
    public static final double THIN_THICKNESS = 1D / 16D;

    public static final AABB[] THICK_FACADE_BOXES = new AABB[] {
            new AABB(0.0, 0.0, 0.0, 1.0, THICK_THICKNESS, 1.0),
            new AABB(0.0, 1.0 - THICK_THICKNESS, 0.0, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, 1.0, 1.0, THICK_THICKNESS),
            new AABB(0.0, 0.0, 1.0 - THICK_THICKNESS, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, THICK_THICKNESS, 1.0, 1.0),
            new AABB(1.0 - THICK_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0) };

    public static final AABB[] THIN_FACADE_BOXES = new AABB[] {
            new AABB(0.0, 0.0, 0.0, 1.0, THIN_THICKNESS, 1.0),
            new AABB(0.0, 1.0 - THIN_THICKNESS, 0.0, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, 1.0, 1.0, THIN_THICKNESS),
            new AABB(0.0, 0.0, 1.0 - THIN_THICKNESS, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, THIN_THICKNESS, 1.0, 1.0),
            new AABB(1.0 - THIN_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0) };

    // Pre-rotated transparent facade quads
    private final Map<Direction, List<BakedQuad>> transparentFacadeQuads;

    private final ThreadLocal<BakedPipeline> pipelines = ThreadLocal.withInitial(() -> BakedPipeline.builder()
            // Clamper is responsible for clamping the vertex to the bounds specified.
            .addElement("clamper", QuadClamper.FACTORY)
            // Strips faces if they match a mask.
            .addElement("face_stripper", QuadFaceStripper.FACTORY)
            // Kicks the edge inner corners in, solves Z fighting
            .addElement("corner_kicker", QuadCornerKicker.FACTORY)
            // Re-Interpolates the UV's for the quad.
            .addElement("interp", QuadReInterpolator.FACTORY)
            // Tints the quad if we need it to. Disabled by default.
            .addElement("tinter", QuadTinter.FACTORY, false)
            // Overrides the quad's alpha if we are forcing transparent facades.
            .addElement("transparent", QuadAlphaOverride.FACTORY, false, e -> e.setAlphaOverride(0x4C / 255F)).build()//
    );
    private final ThreadLocal<Quad> collectors = ThreadLocal.withInitial(Quad::new);

    public FacadeBuilder() {
        // This constructor is used for item models where transparent facades are not a
        // concern
        this.transparentFacadeQuads = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.values()) {
            this.transparentFacadeQuads.put(facing, Collections.emptyList());
        }
    }

    public FacadeBuilder(BakedModel transparentFacadeModel) {
        // Pre-rotate the transparent facade model to all possible sides so that we can
        // add it quicker later
        List<BakedQuad> partQuads = transparentFacadeModel.getQuads(null, null, new Random(), EmptyModelData.INSTANCE);
        this.transparentFacadeQuads = new EnumMap<>(Direction.class);

        for (Direction facing : Direction.values()) {
            // Rotate quads accordingly
            QuadRotator rotator = new QuadRotator();
            List<BakedQuad> rotated = rotator.rotateQuads(partQuads, facing, Direction.UP);
            this.transparentFacadeQuads.put(facing, ImmutableList.copyOf(rotated));
        }
    }

    public void buildFacadeQuads(RenderType layer, CableBusRenderState renderState, Random rand, List<BakedQuad> quads,
            Function<ResourceLocation, BakedModel> modelLookup) {
        BakedPipeline pipeline = this.pipelines.get();
        Quad collectorQuad = this.collectors.get();
        boolean transparent = PartHelper.getCableRenderMode().transparentFacades;
        Map<Direction, FacadeRenderState> facadeStates = renderState.getFacades();
        List<AABB> partBoxes = renderState.getBoundingBoxes();
        Set<Direction> sidesWithParts = renderState.getAttachments().keySet();
        BlockAndTintGetter parentLevel = renderState.getLevel();
        BlockPos pos = renderState.getPos();
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        boolean thinFacades = isUseThinFacades(partBoxes);

        for (Entry<Direction, FacadeRenderState> entry : facadeStates.entrySet()) {
            Direction side = entry.getKey();
            int sideIndex = side.ordinal();
            FacadeRenderState facadeRenderState = entry.getValue();
            boolean renderStilt = !sidesWithParts.contains(side);
            if (layer == RenderType.cutout() && renderStilt) {
                for (ResourceLocation part : CableAnchorPart.FACADE_MODELS.getModels()) {
                    BakedModel partModel = modelLookup.apply(part);
                    QuadRotator rotator = new QuadRotator();
                    quads.addAll(rotator.rotateQuads(gatherQuads(partModel, null, rand, EmptyModelData.INSTANCE), side,
                            Direction.UP));
                }
            }

            // When we're forcing transparent facades, add a "border" model that indicates
            // where the facade is,
            // But otherwise skip the rest.
            if (transparent) {
                if (layer != RenderType.cutout()) {
                    quads.addAll(transparentFacadeQuads.get(side));
                }
                continue;
            }

            BlockState blockState = facadeRenderState.getSourceBlock();
            // If we aren't forcing transparency let the block decide if it should render.
            if (layer != null && !ItemBlockRenderTypes.canRenderInLayer(blockState, layer)) {
                continue;
            }

            AABB fullBounds = thinFacades ? THIN_FACADE_BOXES[sideIndex] : THICK_FACADE_BOXES[sideIndex];
            AABB facadeBox = fullBounds;
            // If we are a transparent facade, we need to modify out BB.
            if (facadeRenderState.isTransparent()) {
                double offset = thinFacades ? THIN_THICKNESS : THICK_THICKNESS;
                AEAxisAlignedBB tmpBB = null;
                for (Direction face : Direction.values()) {
                    // Only faces that aren't on our axis
                    if (face.getAxis() != side.getAxis()) {
                        FacadeRenderState otherState = facadeStates.get(face);
                        if (otherState != null && !otherState.isTransparent()) {
                            if (tmpBB == null) {
                                tmpBB = AEAxisAlignedBB.fromBounds(facadeBox);
                            }
                            switch (face) {
                                case DOWN -> tmpBB.minY += offset;
                                case UP -> tmpBB.maxY -= offset;
                                case NORTH -> tmpBB.minZ += offset;
                                case SOUTH -> tmpBB.maxZ -= offset;
                                case WEST -> tmpBB.minX += offset;
                                case EAST -> tmpBB.maxX -= offset;
                                default -> throw new RuntimeException("Switch falloff. " + String.valueOf(face));
                            }
                        }
                    }
                }
                if (tmpBB != null) {
                    facadeBox = tmpBB.getBoundingBox();
                }
            }

            AEAxisAlignedBB cutOutBox = getCutOutBox(facadeBox, partBoxes);
            List<AABB> holeStrips = getBoxes(facadeBox, cutOutBox, side.getAxis());
            BlockAndTintGetter facadeAccess = new FacadeBlockAccess(parentLevel, pos, side, blockState);

            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            BakedModel model = dispatcher.getBlockModel(blockState);
            IModelData modelData = model.getModelData(facadeAccess, pos, blockState, EmptyModelData.INSTANCE);

            List<BakedQuad> modelQuads = new ArrayList<>();
            // If we are forcing transparent facades, fake the render layer, and grab all
            // quads.
            if (layer == null) {
                for (RenderType forcedLayer : RenderType.chunkBufferLayers()) {
                    // Check if the block renders on the layer we want to force.
                    if (ItemBlockRenderTypes.canRenderInLayer(blockState, forcedLayer)) {
                        // Force the layer and gather quads.
                        ForgeHooksClient.setRenderType(forcedLayer);
                        modelQuads.addAll(gatherQuads(model, blockState, rand, modelData));
                    }
                }

                // Reset.
                ForgeHooksClient.setRenderType(layer);
            } else {
                modelQuads.addAll(gatherQuads(model, blockState, rand, modelData));
            }

            // No quads.. Cool, next!
            if (modelQuads.isEmpty()) {
                continue;
            }

            // Grab out pipeline elements.
            QuadClamper clamper = pipeline.getElement("clamper", QuadClamper.class);
            QuadFaceStripper edgeStripper = pipeline.getElement("face_stripper", QuadFaceStripper.class);
            QuadTinter tinter = pipeline.getElement("tinter", QuadTinter.class);
            QuadCornerKicker kicker = pipeline.getElement("corner_kicker", QuadCornerKicker.class);

            // Set global element states.

            // calculate the side mask.
            int facadeMask = 0;
            for (Entry<Direction, FacadeRenderState> ent : facadeStates.entrySet()) {
                Direction s = ent.getKey();
                if (s.getAxis() != side.getAxis()) {
                    FacadeRenderState otherState = ent.getValue();
                    if (!otherState.isTransparent()) {
                        facadeMask |= 1 << s.ordinal();
                    }
                }
            }
            // Setup the edge stripper.
            edgeStripper.setBounds(fullBounds);
            edgeStripper.setMask(facadeMask);

            // Setup the kicker.
            kicker.setSide(sideIndex);
            kicker.setFacadeMask(facadeMask);
            kicker.setBox(fullBounds);
            kicker.setThickness(thinFacades ? THIN_THICKNESS : THICK_THICKNESS);

            for (BakedQuad quad : modelQuads) {
                // lookup the format in CachedFormat.
                CachedFormat format = CachedFormat.lookup(DefaultVertexFormat.BLOCK);
                // If this quad has a tint index, setup the tinter.
                if (quad.isTinted()) {
                    tinter.setTint(blockColors.getColor(blockState, facadeAccess, pos, quad.getTintIndex()));
                }
                for (AABB box : holeStrips) {
                    // setup the clamper for this box
                    clamper.setClampBounds(box);
                    // Reset the pipeline, clears all enabled/disabled states.
                    pipeline.reset(format);
                    // Reset out collector.
                    collectorQuad.reset(format);
                    // Enable / disable the optional elements
                    pipeline.setElementState("tinter", quad.isTinted());
                    pipeline.setElementState("transparent", transparent);
                    // Prepare the pipeline for a quad.
                    pipeline.prepare(collectorQuad);

                    // Pipe our quad into the pipeline.
                    quad.pipe(pipeline);
                    // Check if the collector got any data.
                    if (collectorQuad.full) {
                        // Add the result.
                        quads.add(collectorQuad.bake());
                    }
                }
            }
        }
    }

    /**
     * This is slow, so should be cached.
     *
     * @return The model.
     */
    public List<BakedQuad> buildFacadeItemQuads(ItemStack textureItem, Direction side) {
        List<BakedQuad> facadeQuads = new ArrayList<>();
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(textureItem, null,
                null, 0);
        List<BakedQuad> modelQuads = gatherQuads(model, null, new Random(), EmptyModelData.INSTANCE);

        BakedPipeline pipeline = this.pipelines.get();
        Quad collectorQuad = this.collectors.get();

        // Grab pipeline elements.
        QuadClamper clamper = pipeline.getElement("clamper", QuadClamper.class);
        QuadTinter tinter = pipeline.getElement("tinter", QuadTinter.class);

        for (BakedQuad quad : modelQuads) {
            // Lookup the CachedFormat for this quads format.
            CachedFormat format = CachedFormat.lookup(DefaultVertexFormat.BLOCK);
            // Reset the pipeline.
            pipeline.reset(format);
            // Reset the collector.
            collectorQuad.reset(format);
            // If we have a tint index, setup the tinter and enable it.
            if (quad.isTinted()) {
                tinter.setTint(Minecraft.getInstance().getItemColors().getColor(textureItem, quad.getTintIndex()));
                pipeline.enableElement("tinter");
            }
            // Disable elements we don't need for items.
            pipeline.disableElement("face_stripper");
            pipeline.disableElement("corner_kicker");
            // Setup the clamper
            clamper.setClampBounds(THICK_FACADE_BOXES[side.ordinal()]);
            // Prepare the pipeline.
            pipeline.prepare(collectorQuad);
            // Pipe our quad into the pipeline.
            quad.pipe(pipeline);
            // Check the collector for data and add the quad if there was.
            if (collectorQuad.full) {
                facadeQuads.add(collectorQuad.bake());
            }
        }
        return facadeQuads;
    }

    // Helper to gather all quads from a model into a list.
    private static List<BakedQuad> gatherQuads(BakedModel model, BlockState state, Random rand, IModelData data) {
        List<BakedQuad> modelQuads = new ArrayList<>();
        for (Direction face : Direction.values()) {
            modelQuads.addAll(model.getQuads(state, face, rand, data));
        }
        modelQuads.addAll(model.getQuads(state, null, rand, data));
        return modelQuads;
    }

    /**
     * Given the actual facade bounding box, and the bounding boxes of all parts, determine the biggest union of AABB
     * that intersect with the facade's bounding box. This AABB will need to be "cut out" when the facade is rendered.
     */
    @Nullable
    private static AEAxisAlignedBB getCutOutBox(AABB facadeBox, List<AABB> partBoxes) {
        AEAxisAlignedBB b = null;
        for (AABB bb : partBoxes) {
            if (bb.intersects(facadeBox)) {
                if (b == null) {
                    b = AEAxisAlignedBB.fromBounds(bb);
                } else {
                    b.maxX = Math.max(b.maxX, bb.maxX);
                    b.maxY = Math.max(b.maxY, bb.maxY);
                    b.maxZ = Math.max(b.maxZ, bb.maxZ);
                    b.minX = Math.min(b.minX, bb.minX);
                    b.minY = Math.min(b.minY, bb.minY);
                    b.minZ = Math.min(b.minZ, bb.minZ);
                }
            }
        }
        return b;
    }

    /**
     * Generates the box segments around the specified hole. If the specified hole is null, a Singleton of the Facade
     * box is returned.
     *
     * @param fb   The Facade's box.
     * @param hole The hole to 'cut'.
     * @param axis The axis the facade is on.
     * @return The box segments.
     */
    private static List<AABB> getBoxes(AABB fb, AEAxisAlignedBB hole, Axis axis) {
        if (hole == null) {
            return Collections.singletonList(fb);
        }
        List<AABB> boxes = new ArrayList<>();
        switch (axis) {
            case Y -> {
                boxes.add(new AABB(fb.minX, fb.minY, fb.minZ, hole.minX, fb.maxY, fb.maxZ));
                boxes.add(new AABB(hole.maxX, fb.minY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));
                boxes.add(new AABB(hole.minX, fb.minY, fb.minZ, hole.maxX, fb.maxY, hole.minZ));
                boxes.add(new AABB(hole.minX, fb.minY, hole.maxZ, hole.maxX, fb.maxY, fb.maxZ));
            }
            case Z -> {
                boxes.add(new AABB(fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ));
                boxes.add(new AABB(fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));
                boxes.add(new AABB(fb.minX, hole.minY, fb.minZ, hole.minX, hole.maxY, fb.maxZ));
                boxes.add(new AABB(hole.maxX, hole.minY, fb.minZ, fb.maxX, hole.maxY, fb.maxZ));
            }
            case X -> {
                boxes.add(new AABB(fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ));
                boxes.add(new AABB(fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));
                boxes.add(new AABB(fb.minX, hole.minY, fb.minZ, fb.maxX, hole.maxY, hole.minZ));
                boxes.add(new AABB(fb.minX, hole.minY, hole.maxZ, fb.maxX, hole.maxY, fb.maxZ));
            }
            default ->
                // should never happen.
                throw new RuntimeException("switch falloff. " + String.valueOf(axis));
        }

        return boxes;
    }

    /**
     * Determines if any of the part's bounding boxes intersects with the outside 2 voxel wide layer. If so, we should
     * use thinner facades (1 voxel deep).
     */
    private static boolean isUseThinFacades(List<AABB> partBoxes) {
        final double min = 2.0 / 16.0;
        final double max = 14.0 / 16.0;

        for (AABB bb : partBoxes) {
            int o = 0;
            o += bb.maxX > max ? 1 : 0;
            o += bb.maxY > max ? 1 : 0;
            o += bb.maxZ > max ? 1 : 0;
            o += bb.minX < min ? 1 : 0;
            o += bb.minY < min ? 1 : 0;
            o += bb.minZ < min ? 1 : 0;

            if (o >= 2) {
                return true;
            }
        }
        return false;
    }
}
