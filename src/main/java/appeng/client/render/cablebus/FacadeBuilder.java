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
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.BlockRenderView;

import appeng.api.util.AEAxisAlignedBB;
import appeng.core.Api;
import appeng.mixins.MinecraftClientAccessor;
import appeng.parts.misc.CableAnchorPart;
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

    private final Renderer renderer = RendererAccess.INSTANCE.getRenderer();

    public static final double THICK_THICKNESS = 2D / 16D;
    public static final double THIN_THICKNESS = 1D / 16D;

    public static final Box[] THICK_FACADE_BOXES = new Box[] { new Box(0.0, 0.0, 0.0, 1.0, THICK_THICKNESS, 1.0),
            new Box(0.0, 1.0 - THICK_THICKNESS, 0.0, 1.0, 1.0, 1.0), new Box(0.0, 0.0, 0.0, 1.0, 1.0, THICK_THICKNESS),
            new Box(0.0, 0.0, 1.0 - THICK_THICKNESS, 1.0, 1.0, 1.0), new Box(0.0, 0.0, 0.0, THICK_THICKNESS, 1.0, 1.0),
            new Box(1.0 - THICK_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0) };

    public static final Box[] THIN_FACADE_BOXES = new Box[] { new Box(0.0, 0.0, 0.0, 1.0, THIN_THICKNESS, 1.0),
            new Box(0.0, 1.0 - THIN_THICKNESS, 0.0, 1.0, 1.0, 1.0), new Box(0.0, 0.0, 0.0, 1.0, 1.0, THIN_THICKNESS),
            new Box(0.0, 0.0, 1.0 - THIN_THICKNESS, 1.0, 1.0, 1.0), new Box(0.0, 0.0, 0.0, THIN_THICKNESS, 1.0, 1.0),
            new Box(1.0 - THIN_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0) };

    private final Map<Direction, Mesh> cableAnchorStilts;

    public FacadeBuilder(ModelLoader modelLoader) {
        cableAnchorStilts = buildCableAnchorStems(modelLoader);
    }

    /**
     * Build a map of pre-rotated cable anchor stilts, which are the shortened cable
     * anchors that will still be visible for facades attached to a cable.
     */
    private Map<Direction, Mesh> buildCableAnchorStems(ModelLoader modelLoader) {
        Map<Direction, Mesh> stems = new EnumMap<>(Direction.class);

        List<BakedModel> cableAnchorParts = new ArrayList<>();
        for (Identifier model : CableAnchorPart.FACADE_MODELS.getModels()) {
            BakedModel cableAnchor = modelLoader.bake(model, ModelRotation.X0_Y0);
            cableAnchorParts.add(cableAnchor);
        }

        // Create pre-rotated variants of the cable anchor stems
        for (Direction side : Direction.values()) {
            RenderContext.QuadTransform rotator = QuadRotator.get(side, Direction.UP);

            MeshBuilder meshBuilder = renderer.meshBuilder();
            QuadEmitter emitter = meshBuilder.getEmitter();

            for (BakedModel model : cableAnchorParts) {
                for (int cullFaceIdx = 0; cullFaceIdx <= ModelHelper.NULL_FACE_ID; cullFaceIdx++) {
                    Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);
                    List<BakedQuad> quads = model.getQuads(null, cullFace, new Random());
                    for (BakedQuad quad : quads) {
                        emitter.fromVanilla(quad.getVertexData(), 0, false);
                        emitter.cullFace(cullFace);
                        emitter.nominalFace(quad.getFace());
                        if (!rotator.transform(emitter)) {
                            continue;
                        }
                        emitter.emit();
                    }
                }
            }

            stems.put(side, meshBuilder.build());
        }

        return stems;
    }

    public Mesh getFacadeMesh(CableBusRenderState renderState, Supplier<Random> rand,
            Function<Identifier, BakedModel> modelLookup) {
        boolean transparent = Api.instance().partHelper().getCableRenderMode().transparentFacades;
        Map<Direction, FacadeRenderState> facadeStates = renderState.getFacades();
        List<Box> partBoxes = renderState.getBoundingBoxes();
        Set<Direction> sidesWithParts = renderState.getAttachments().keySet();
        BlockRenderView parentWorld = renderState.getWorld();
        BlockPos pos = renderState.getPos();
        BlockColors blockColors = MinecraftClient.getInstance().getBlockColors();
        boolean thinFacades = isUseThinFacades(partBoxes);

        MeshBuilder meshBuilder = renderer.meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();

        for (Entry<Direction, FacadeRenderState> entry : facadeStates.entrySet()) {
            Direction side = entry.getKey();
            int sideIndex = side.ordinal();
            FacadeRenderState facadeRenderState = entry.getValue();
            boolean renderStilt = !sidesWithParts.contains(side);
            if (renderStilt) {
                cableAnchorStilts.get(entry.getKey()).forEach(quad -> {
                    quad.copyTo(emitter);
                    emitter.emit();
                });
            }

            BlockState blockState = facadeRenderState.getSourceBlock();
            // If we aren't forcing transparency let the block decide if it should render.
// FIXME FABRIC            if (!transparent && layer != null) {
// FIXME FABRIC only one layer per block
// FIXME FABRIC               if (!RenderLayers.canRenderInLayer(blockState, layer)) {
// FIXME FABRIC                   continue;
// FIXME FABRIC               }
// FIXME FABRIC            }

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
                                    throw new RuntimeException("Switch falloff. " + face);
                            }
                        }
                    }
                }
                if (tmpBB != null) {
                    facadeBox = tmpBB.getBoundingBox();
                }
            }

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

            AEAxisAlignedBB cutOutBox = getCutOutBox(facadeBox, partBoxes);
            List<Box> holeStrips = getBoxes(facadeBox, cutOutBox, side.getAxis());
            BlockRenderView facadeAccess = new FacadeBlockAccess(parentWorld, pos, side, blockState);

            BlockRenderManager dispatcher = MinecraftClient.getInstance().getBlockRenderManager();
            BakedModel model = dispatcher.getModel(blockState);

            QuadFaceStripper faceStripper = new QuadFaceStripper(fullBounds, facadeMask);
            // Setup the kicker.
            QuadCornerKicker kicker = new QuadCornerKicker();
            kicker.setSide(sideIndex);
            kicker.setFacadeMask(facadeMask);
            kicker.setBox(fullBounds);
            kicker.setThickness(thinFacades ? THIN_THICKNESS : THICK_THICKNESS);

            QuadReInterpolator interpolator = new QuadReInterpolator();

            for (int cullFaceIdx = 0; cullFaceIdx <= ModelHelper.NULL_FACE_ID; cullFaceIdx++) {
                Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);
                List<BakedQuad> quads = model.getQuads(blockState, cullFace, rand.get());

                for (BakedQuad quad : quads) {
                    QuadTinter quadTinter = null;

                    // Prebake the color tint into the quad
                    if (quad.getColorIndex() != -1) {
                        quadTinter = new QuadTinter(
                                blockColors.getColor(blockState, facadeAccess, pos, quad.getColorIndex()));
                    }

                    for (Box box : holeStrips) {
                        emitter.fromVanilla(quad.getVertexData(), 0, false);
                        // Keep the cull-face for faces that are flush with the outer block-face on the
                        // side the facade is attached to, but clear it for anything that faces inwards
                        emitter.cullFace(cullFace == side ? side : null);
                        emitter.nominalFace(quad.getFace());
                        interpolator.setInputQuad(emitter);

                        QuadClamper clamper = new QuadClamper(box);
                        if (!clamper.transform(emitter)) {
                            continue;
                        }

                        // Strips faces if they match a mask.
                        if (!faceStripper.transform(emitter)) {
                            continue;
                        }

                        // Kicks the edge inner corners in, solves Z fighting
                        if (!kicker.transform(emitter)) {
                            continue;
                        }

                        interpolator.transform(emitter);

                        // Tints the quad if we need it to. Disabled by default.
                        if (quadTinter != null) {
                            quadTinter.transform(emitter);
                        }

//                // Overrides the quad's alpha if we are forcing transparent facades.
//                .addElement("transparent", QuadAlphaOverride.FACTORY, false, e -> e.setAlphaOverride(0x4C / 255F)).build()//

                        emitter.emit();
                    }
                }
            }
        }

        return meshBuilder.build();

    }

    /**
     * This is slow, so should be cached.
     *
     * @return The model.
     */
    public Mesh buildFacadeItemQuads(ItemStack textureItem, Direction side) {

        MeshBuilder meshBuilder = renderer.meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();

        BakedModel model = MinecraftClient.getInstance().getItemRenderer().getHeldItemModel(textureItem, null, null);
        List<BakedQuad> modelQuads = model.getQuads(null, null, new Random());

        // FIXME BakedPipeline pipeline = this.pipelines.get();
//FIXME          Quad collectorQuad = this.collectors.get();

        // Grab pipeline elements.
        // FIXME QuadClamper clamper = pipeline.getElement("clamper",
        // QuadClamper.class);
        // FIXME QuadTinter tinter = pipeline.getElement("tinter", QuadTinter.class);

        QuadReInterpolator interpolator = new QuadReInterpolator();

        ItemColors itemColors = ((MinecraftClientAccessor) MinecraftClient.getInstance()).getItemColors();
        QuadClamper clamper = new QuadClamper(THICK_FACADE_BOXES[side.ordinal()]);

        for (int cullFaceIdx = 0; cullFaceIdx <= ModelHelper.NULL_FACE_ID; cullFaceIdx++) {
            Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);
            List<BakedQuad> quads = model.getQuads(null, cullFace, new Random());

            for (BakedQuad quad : quads) {
                QuadTinter quadTinter = null;

                // Prebake the color tint into the quad
                if (quad.getColorIndex() != -1) {
                    quadTinter = new QuadTinter(itemColors.getColorMultiplier(textureItem, quad.getColorIndex()));
                }

                emitter.fromVanilla(quad.getVertexData(), 0, false);
                emitter.cullFace(cullFace);
                emitter.nominalFace(quad.getFace());
                interpolator.setInputQuad(emitter);

                if (!clamper.transform(emitter)) {
                    continue;
                }

                interpolator.transform(emitter);

                // Tints the quad if we need it to. Disabled by default.
                if (quadTinter != null) {
                    quadTinter.transform(emitter);
                }

                emitter.emit();
            }
        }

        for (BakedQuad quad : modelQuads) {

            // Lookup the CachedFormat for this quads format.
            // FIXME CachedFormat format =
            // CachedFormat.lookup(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            // Reset the pipeline.
            // FIXME pipeline.reset(format);
            // Reset the collector.
            // FIXME collectorQuad.reset(format);
            // If we have a tint index, setup the tinter and enable it.
            // FIXME if (quad.hasTintIndex()) {
            // FIXME
            // tinter.setTint(MinecraftClient.getInstance().getItemColors().getColor(textureItem,
            // quad.getColorIndex()));
            // FIXME pipeline.enableElement("tinter");
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
            // FIXME facadeQuads.add(collectorQuad.bake());
            // FIXME }
        }
        return meshBuilder.build();
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
