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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;


import appeng.api.util.AEAxisAlignedBB;
import appeng.core.Api;
import appeng.parts.misc.CableAnchorPart;
/**
 * The FacadeBuilder builds for facades..
 *
 * @author covers1624
 */
public class FacadeBuilder {

    public static final double THICK_THICKNESS = 2D / 16D;
    public static final double THIN_THICKNESS = 1D / 16D;

    public static final Box[] THICK_FACADE_BOXES = new Box[] {
            new Box(0.0, 0.0, 0.0, 1.0, THICK_THICKNESS, 1.0),
            new Box(0.0, 1.0 - THICK_THICKNESS, 0.0, 1.0, 1.0, 1.0),
            new Box(0.0, 0.0, 0.0, 1.0, 1.0, THICK_THICKNESS),
            new Box(0.0, 0.0, 1.0 - THICK_THICKNESS, 1.0, 1.0, 1.0),
            new Box(0.0, 0.0, 0.0, THICK_THICKNESS, 1.0, 1.0),
            new Box(1.0 - THICK_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0) };

    public static final Box[] THIN_FACADE_BOXES = new Box[] {
            new Box(0.0, 0.0, 0.0, 1.0, THIN_THICKNESS, 1.0),
            new Box(0.0, 1.0 - THIN_THICKNESS, 0.0, 1.0, 1.0, 1.0),
            new Box(0.0, 0.0, 0.0, 1.0, 1.0, THIN_THICKNESS),
            new Box(0.0, 0.0, 1.0 - THIN_THICKNESS, 1.0, 1.0, 1.0),
            new Box(0.0, 0.0, 0.0, THIN_THICKNESS, 1.0, 1.0),
            new Box(1.0 - THIN_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0) };

//FIXME    private final ThreadLocal<BakedPipeline> pipelines = ThreadLocal.withInitial(() -> BakedPipeline.builder()
//FIXME            // Clamper is responsible for clamping the vertex to the bounds specified.
//FIXME            .addElement("clamper", QuadClamper.FACTORY)
//FIXME            // Strips faces if they match a mask.
//FIXME            .addElement("face_stripper", QuadFaceStripper.FACTORY)
//FIXME            // Kicks the edge inner corners in, solves Z fighting
//FIXME            .addElement("corner_kicker", QuadCornerKicker.FACTORY)
//FIXME            // Re-Interpolates the UV's for the quad.
//FIXME            .addElement("interp", QuadReInterpolator.FACTORY)
//FIXME            // Tints the quad if we need it to. Disabled by default.
//FIXME            .addElement("tinter", QuadTinter.FACTORY, false)
//FIXME            // Overrides the quad's alpha if we are forcing transparent facades.
//FIXME            .addElement("transparent", QuadAlphaOverride.FACTORY, false, e -> e.setAlphaOverride(0x4C / 255F)).build()//
//FIXME    );
//FIXME      private final ThreadLocal<Quad> collectors = ThreadLocal.withInitial(Quad::new);

    public void buildFacadeQuads(RenderLayer layer, CableBusRenderState renderState, Supplier<Random> rand,
                                 RenderContext context, Function<Identifier, BakedModel> modelLookup) {
//FIXME        BakedPipeline pipeline = this.pipelines.get();
//FIXME          Quad collectorQuad = this.collectors.get();
        boolean transparent = Api.instance().partHelper().getCableRenderMode().transparentFacades;
        Map<Direction, FacadeRenderState> facadeStates = renderState.getFacades();
        List<Box> partBoxes = renderState.getBoundingBoxes();
        Set<Direction> sidesWithParts = renderState.getAttachments().keySet();
        BlockRenderView parentWorld = renderState.getWorld();
        BlockPos pos = renderState.getPos();
        BlockColors blockColors = MinecraftClient.getInstance().getBlockColors();
        boolean thinFacades = isUseThinFacades(partBoxes);

        for (Entry<Direction, FacadeRenderState> entry : facadeStates.entrySet()) {
            Direction side = entry.getKey();
            int sideIndex = side.ordinal();
            FacadeRenderState facadeRenderState = entry.getValue();
            boolean renderStilt = !sidesWithParts.contains(side);
            if (layer == RenderLayer.getCutout() && renderStilt) {
                context.pushTransform(QuadRotator.get(side, Direction.UP));
                for (Identifier part : CableAnchorPart.FACADE_MODELS.getModels()) {
                    BakedModel partModel = modelLookup.apply(part);
                    context.fallbackConsumer().accept(partModel);
                }
                context.popTransform();
            }
            // If we are forcing transparency and this isn't the Translucent layer.
            if (transparent && layer != RenderLayer.getTranslucent()) {
                continue;
            }

            BlockState blockState = facadeRenderState.getSourceBlock();
            // If we aren't forcing transparency let the block decide if it should render.
            if (!transparent && layer != null) {
// FIXME FABRIC only one layer per block
// FIXME FABRIC               if (!RenderLayers.canRenderInLayer(blockState, layer)) {
// FIXME FABRIC                   continue;
// FIXME FABRIC               }
            }

            Box fullBounds = thinFacades ? THIN_FACADE_BOXES[sideIndex] : THICK_FACADE_BOXES[sideIndex];
            Box facadeBox = fullBounds;
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
                                case DOWN:
                                    tmpBB.minY += offset;
                                    break;
                                case UP:
                                    tmpBB.maxY -= offset;
                                    break;
                                case NORTH:
                                    tmpBB.minZ += offset;
                                    break;
                                case SOUTH:
                                    tmpBB.maxZ -= offset;
                                    break;
                                case WEST:
                                    tmpBB.minX += offset;
                                    break;
                                case EAST:
                                    tmpBB.maxX -= offset;
                                    break;
                                default:
                                    throw new RuntimeException("Switch falloff. " + String.valueOf(face));
                            }
                        }
                    }
                }
                if (tmpBB != null) {
                    facadeBox = tmpBB.getBoundingBox();
                }
            }

            AEAxisAlignedBB cutOutBox = getCutOutBox(facadeBox, partBoxes);
            List<Box> holeStrips = getBoxes(facadeBox, cutOutBox, side.getAxis());
// FIXME            BlockRenderView facadeAccess = new FacadeBlockAccess(parentWorld, pos, side, blockState);

 // FIXME FABRIC            BlockRenderManager dispatcher = MinecraftClient.getInstance().getBlockRenderManager();
 // FIXME FABRIC            BakedModel model = dispatcher.getModel(blockState);
 // FIXME FABRIC            IModelData modelData = model.getModelData(facadeAccess, pos, blockState, EmptyModelData.INSTANCE);
 // FIXME FABRIC
 // FIXME FABRIC            List<BakedQuad> modelQuads = new ArrayList<>();
 // FIXME FABRIC            // If we are forcing transparent facades, fake the render layer, and grab all
 // FIXME FABRIC            // quads.
 // FIXME FABRIC            if (transparent || layer == null) {
 // FIXME FABRIC                for (RenderLayer forcedLayer : RenderLayer.getBlockRenderTypes()) {
 // FIXME FABRIC                    // Check if the block renders on the layer we want to force.
 // FIXME FABRIC                    if (RenderLayers.canRenderInLayer(blockState, forcedLayer)) {
 // FIXME FABRIC                        // Force the layer and gather quads.
 // FIXME FABRIC                        ForgeHooksClient.setRenderLayer(forcedLayer);
 // FIXME FABRIC                        modelQuads.addAll(gatherQuads(model, blockState, rand, modelData));
 // FIXME FABRIC                    }
 // FIXME FABRIC                }
 // FIXME FABRIC
 // FIXME FABRIC                // Reset.
 // FIXME FABRIC                ForgeHooksClient.setRenderLayer(layer);
 // FIXME FABRIC            } else {
 // FIXME FABRIC                modelQuads.addAll(gatherQuads(model, blockState, rand, modelData));
 // FIXME FABRIC            }
 // FIXME FABRIC
 // FIXME FABRIC            // No quads.. Cool, next!
 // FIXME FABRIC            if (modelQuads.isEmpty()) {
 // FIXME FABRIC                continue;
 // FIXME FABRIC            }
 // FIXME FABRIC
 // FIXME FABRIC            // Grab out pipeline elements.
 // FIXME FABRIC            QuadClamper clamper = pipeline.getElement("clamper", QuadClamper.class);
 // FIXME FABRIC            QuadFaceStripper edgeStripper = pipeline.getElement("face_stripper", QuadFaceStripper.class);
 // FIXME FABRIC            QuadTinter tinter = pipeline.getElement("tinter", QuadTinter.class);
 // FIXME FABRIC            QuadCornerKicker kicker = pipeline.getElement("corner_kicker", QuadCornerKicker.class);
 // FIXME FABRIC
 // FIXME FABRIC            // Set global element states.
 // FIXME FABRIC
 // FIXME FABRIC            // calculate the side mask.
 // FIXME FABRIC            int facadeMask = 0;
 // FIXME FABRIC            for (Entry<Direction, FacadeRenderState> ent : facadeStates.entrySet()) {
 // FIXME FABRIC                Direction s = ent.getKey();
 // FIXME FABRIC                if (s.getAxis() != side.getAxis()) {
 // FIXME FABRIC                    FacadeRenderState otherState = ent.getValue();
 // FIXME FABRIC                    if (!otherState.isTransparent()) {
 // FIXME FABRIC                        facadeMask |= 1 << s.ordinal();
 // FIXME FABRIC                    }
 // FIXME FABRIC                }
 // FIXME FABRIC            }
 // FIXME FABRIC            // Setup the edge stripper.
 // FIXME FABRIC            edgeStripper.setBounds(fullBounds);
 // FIXME FABRIC            edgeStripper.setMask(facadeMask);
 // FIXME FABRIC
 // FIXME FABRIC            // Setup the kicker.
 // FIXME FABRIC            kicker.setSide(sideIndex);
 // FIXME FABRIC            kicker.setFacadeMask(facadeMask);
 // FIXME FABRIC            kicker.setBox(fullBounds);
 // FIXME FABRIC            kicker.setThickness(thinFacades ? THIN_THICKNESS : THICK_THICKNESS);
 // FIXME FABRIC
 // FIXME FABRIC            for (BakedQuad quad : modelQuads) {
 // FIXME FABRIC                // lookup the format in CachedFormat.
 // FIXME FABRIC                CachedFormat format = CachedFormat.lookup(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
 // FIXME FABRIC                // If this quad has a tint index, setup the tinter.
 // FIXME FABRIC                if (quad.hasTintIndex()) {
 // FIXME FABRIC                    tinter.setTint(blockColors.getColor(blockState, facadeAccess, pos, quad.getColorIndex()));
 // FIXME FABRIC                }
 // FIXME FABRIC                for (Box box : holeStrips) {
 // FIXME FABRIC                    // setup the clamper for this box
 // FIXME FABRIC                    clamper.setClampBounds(box);
 // FIXME FABRIC                    // Reset the pipeline, clears all enabled/disabled states.
 // FIXME FABRIC                    pipeline.reset(format);
 // FIXME FABRIC                    // Reset out collector.
 // FIXME FABRIC                    collectorQuad.reset(format);
 // FIXME FABRIC                    // Enable / disable the optional elements
 // FIXME FABRIC                    pipeline.setElementState("tinter", quad.hasTintIndex());
 // FIXME FABRIC                    pipeline.setElementState("transparent", transparent);
 // FIXME FABRIC                    // Prepare the pipeline for a quad.
 // FIXME FABRIC                    pipeline.prepare(collectorQuad);
 // FIXME FABRIC
 // FIXME FABRIC                    // Pipe our quad into the pipeline.
 // FIXME FABRIC                    quad.pipe(pipeline);
 // FIXME FABRIC                    // Check if the collector got any data.
 // FIXME FABRIC                    if (collectorQuad.full) {
 // FIXME FABRIC                        // Add the result.
 // FIXME FABRIC                        quads.add(collectorQuad.bake());
 // FIXME FABRIC                    }
 // FIXME FABRIC                }
 // FIXME FABRIC            }
        }
    }

    /**
     * This is slow, so should be cached.
     *
     * @return The model.
     */
    public List<BakedQuad> buildFacadeItemQuads(ItemStack textureItem, Direction side) {
        List<BakedQuad> facadeQuads = new ArrayList<>();

        BakedModel model = MinecraftClient.getInstance().getItemRenderer().getHeldItemModel(textureItem, null,
                null);
        List<BakedQuad> modelQuads = gatherQuads(model, null, new Random());

        //FIXME       BakedPipeline pipeline = this.pipelines.get();
//FIXME          Quad collectorQuad = this.collectors.get();

        // Grab pipeline elements.
        // FIXME QuadClamper clamper = pipeline.getElement("clamper", QuadClamper.class);
        // FIXME QuadTinter tinter = pipeline.getElement("tinter", QuadTinter.class);

        for (BakedQuad quad : modelQuads) {
            // Lookup the CachedFormat for this quads format.
            // FIXME CachedFormat format = CachedFormat.lookup(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            // Reset the pipeline.
            // FIXME pipeline.reset(format);
            // Reset the collector.
            //FIXME             collectorQuad.reset(format);
            // If we have a tint index, setup the tinter and enable it.
            // FIXME if (quad.hasTintIndex()) {
            // FIXME     tinter.setTint(MinecraftClient.getInstance().getItemColors().getColor(textureItem, quad.getColorIndex()));
            // FIXME     pipeline.enableElement("tinter");
            // FIXME }
            // Disable elements we don't need for items.
            // FIXME pipeline.disableElement("face_stripper");
            // FIXME pipeline.disableElement("corner_kicker");
            // FIXME // Setup the clamper
            // FIXME clamper.setClampBounds(THICK_FACADE_BOXES[side.ordinal()]);
            // FIXME // Prepare the pipeline.
            // FIXME pipeline.prepare(collectorQuad);
            // FIXME // Pipe our quad into the pipeline.
            // FIXME quad.pipe(pipeline);
            // FIXME // Check the collector for data and add the quad if there was.
            // FIXME if (collectorQuad.full) {
            // FIXME     facadeQuads.add(collectorQuad.bake());
            // FIXME }
        }
        return facadeQuads;
    }

    // Helper to gather all quads from a model into a list.
    private static List<BakedQuad> gatherQuads(BakedModel model, BlockState state, Random rand) {
        List<BakedQuad> modelQuads = new ArrayList<>();
        for (Direction face : Direction.values()) {
            modelQuads.addAll(model.getQuads(state, face, rand));
        }
        modelQuads.addAll(model.getQuads(state, null, rand));
        return modelQuads;
    }

    /**
     * Given the actual facade bounding box, and the bounding boxes of all parts,
     * determine the biggest union of AABB that intersect with the facade's bounding
     * box. This AABB will need to be "cut out" when the facade is rendered.
     */
    @Nullable
    private static AEAxisAlignedBB getCutOutBox(Box facadeBox, List<Box> partBoxes) {
        AEAxisAlignedBB b = null;
        for (Box bb : partBoxes) {
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
     * Generates the box segments around the specified hole. If the specified hole
     * is null, a Singleton of the Facade box is returned.
     *
     * @param fb   The Facade's box.
     * @param hole The hole to 'cut'.
     * @param axis The axis the facade is on.
     *
     * @return The box segments.
     */
    private static List<Box> getBoxes(Box fb, AEAxisAlignedBB hole, Axis axis) {
        if (hole == null) {
            return Collections.singletonList(fb);
        }
        List<Box> boxes = new ArrayList<>();
        switch (axis) {
            case Y:
                boxes.add(new Box(fb.minX, fb.minY, fb.minZ, hole.minX, fb.maxY, fb.maxZ));
                boxes.add(new Box(hole.maxX, fb.minY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));

                boxes.add(new Box(hole.minX, fb.minY, fb.minZ, hole.maxX, fb.maxY, hole.minZ));
                boxes.add(new Box(hole.minX, fb.minY, hole.maxZ, hole.maxX, fb.maxY, fb.maxZ));

                break;
            case Z:
                boxes.add(new Box(fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ));
                boxes.add(new Box(fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));

                boxes.add(new Box(fb.minX, hole.minY, fb.minZ, hole.minX, hole.maxY, fb.maxZ));
                boxes.add(new Box(hole.maxX, hole.minY, fb.minZ, fb.maxX, hole.maxY, fb.maxZ));

                break;
            case X:
                boxes.add(new Box(fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ));
                boxes.add(new Box(fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));

                boxes.add(new Box(fb.minX, hole.minY, fb.minZ, fb.maxX, hole.maxY, hole.minZ));
                boxes.add(new Box(fb.minX, hole.minY, hole.maxZ, fb.maxX, hole.maxY, fb.maxZ));
                break;
            default:
                // should never happen.
                throw new RuntimeException("switch falloff. " + String.valueOf(axis));
        }

        return boxes;
    }

    /**
     * Determines if any of the part's bounding boxes intersects with the outside 2
     * voxel wide layer. If so, we should use thinner facades (1 voxel deep).
     */
    private static boolean isUseThinFacades(List<Box> partBoxes) {
        final double min = 2.0 / 16.0;
        final double max = 14.0 / 16.0;

        for (Box bb : partBoxes) {
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
