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
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;

import appeng.api.parts.PartHelper;
import appeng.api.util.AEAxisAlignedBB;
import appeng.parts.misc.CableAnchorPart;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadClamper;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadCornerKicker;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadFaceStripper;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadReInterpolator;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadTinter;
import appeng.thirdparty.fabric.Mesh;
import appeng.thirdparty.fabric.MeshBuilder;
import appeng.thirdparty.fabric.MeshBuilderImpl;
import appeng.thirdparty.fabric.ModelHelper;
import appeng.thirdparty.fabric.QuadEmitter;
import appeng.thirdparty.fabric.RenderContext;
import appeng.thirdparty.fabric.Renderer;

/**
 * The FacadeBuilder builds for facades..
 *
 * @author covers1624
 */
public class FacadeBuilder {

    private final Renderer renderer = Renderer.getInstance();

    public static final double THIN_THICKNESS = 1D / 16D;
    public static final double THICK_THICKNESS = 2D / 16D;

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
    private final Map<Direction, Mesh> transparentFacadeQuads;

    private final Map<Direction, Mesh> cableAnchorStilts;

    public FacadeBuilder(ModelBaker modelLoader, @Nullable BakedModel transparentFacadeModel) {
        cableAnchorStilts = buildCableAnchorStems(modelLoader);

        // Pre-rotate the transparent facade model to all possible sides so that we can
        // add it quicker later.
        this.transparentFacadeQuads = new EnumMap<>(Direction.class);
        // This can be null for item models.
        if (transparentFacadeModel != null) {
            List<BakedQuad> partQuads = transparentFacadeModel.getQuads(null, null, RandomSource.create());

            for (Direction facing : Direction.values()) {
                MeshBuilder meshBuilder = new MeshBuilderImpl();
                QuadEmitter emitter = meshBuilder.getEmitter();

                // Rotate quads accordingly
                RenderContext.QuadTransform rotator = QuadRotator.get(facing, 0);

                for (BakedQuad quad : partQuads) {
                    emitter.fromVanilla(quad.getVertices(), 0, false);
                    emitter.cullFace(null);
                    emitter.nominalFace(quad.getDirection());
                    if (!rotator.transform(emitter)) {
                        continue;
                    }
                    emitter.emit();
                }

                this.transparentFacadeQuads.put(facing, meshBuilder.build());
            }
        } else {
            // This constructor is used for item models where transparent facades are not a
            // concern
            for (Direction facing : Direction.values()) {
                this.transparentFacadeQuads.put(facing, new MeshBuilderImpl().build());
            }
        }
    }

    /**
     * Build a map of pre-rotated cable anchor stilts, which are the shortened cable anchors that will still be visible
     * for facades attached to a cable.
     */
    private Map<Direction, Mesh> buildCableAnchorStems(ModelBaker modelLoader) {
        Map<Direction, Mesh> stems = new EnumMap<>(Direction.class);

        List<BakedModel> cableAnchorParts = new ArrayList<>();
        for (ResourceLocation model : CableAnchorPart.FACADE_MODELS.getModels()) {
            BakedModel cableAnchor = modelLoader.bake(model, BlockModelRotation.X0_Y0);
            cableAnchorParts.add(cableAnchor);
        }

        // Create pre-rotated variants of the cable anchor stems
        for (Direction side : Direction.values()) {
            RenderContext.QuadTransform rotator = QuadRotator.get(side, 0);

            MeshBuilder meshBuilder = renderer.meshBuilder();
            QuadEmitter emitter = meshBuilder.getEmitter();

            for (BakedModel model : cableAnchorParts) {
                for (int cullFaceIdx = 0; cullFaceIdx <= ModelHelper.NULL_FACE_ID; cullFaceIdx++) {
                    Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);
                    List<BakedQuad> quads = model.getQuads(null, cullFace, RandomSource.create());
                    for (BakedQuad quad : quads) {
                        emitter.fromVanilla(quad.getVertices(), 0, false);
                        emitter.cullFace(cullFace);
                        emitter.nominalFace(quad.getDirection());
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

    public Mesh getFacadeMesh(CableBusRenderState renderState, Supplier<RandomSource> rand,
            BlockAndTintGetter level, EnumMap<Direction, ModelData> facadeModelData, @Nullable RenderType renderType) {
        boolean transparent = PartHelper.getCableRenderMode().transparentFacades;
        Map<Direction, FacadeRenderState> facadeStates = renderState.getFacades();
        List<AABB> partBoxes = renderState.getBoundingBoxes();
        Set<Direction> sidesWithParts = renderState.getAttachments().keySet();
        BlockPos pos = renderState.getPos();
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        boolean thinFacades = isUseThinFacades(partBoxes);

        MeshBuilder meshBuilder = renderer.meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();

        for (Entry<Direction, FacadeRenderState> entry : facadeStates.entrySet()) {
            Direction side = entry.getKey();
            int sideIndex = side.ordinal();
            FacadeRenderState facadeRenderState = entry.getValue();
            boolean renderStilt = !sidesWithParts.contains(side);
            if (renderStilt) {
                cableAnchorStilts.get(side).forEach(quad -> {
                    quad.copyTo(emitter);
                    emitter.emit();
                });
            }

            // When we're forcing transparent facades, add a "border" model that indicates
            // where the facade is,
            // But otherwise skip the rest.
            if (transparent) {
                transparentFacadeQuads.get(side).forEach(quad -> {
                    quad.copyTo(emitter);
                    emitter.emit();
                });
                continue;
            }

            BlockState blockState = facadeRenderState.getSourceBlock();
            var dispatcher = Minecraft.getInstance().getBlockRenderer();
            var model = dispatcher.getBlockModel(blockState);
            var modelData = Objects.requireNonNullElse(facadeModelData.get(side), ModelData.EMPTY);

            // If we aren't forcing transparency let the model decide if it should render.
            if (renderType != null && !model.getRenderTypes(blockState, rand.get(), modelData).contains(renderType)) {
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
            List<AABB> holeStrips = getBoxes(facadeBox, cutOutBox, side.getAxis());
            var facadeAccess = new FacadeBlockAccess(level, pos, side, blockState);

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
                List<BakedQuad> quads = model.getQuads(blockState, cullFace, rand.get(), modelData, renderType);

                // Ignore quad if it's not supposed to connect to the adjacent block.
                if (cullFace != null) {
                    BlockPos adjPos = pos.relative(cullFace);
                    BlockState adjState = level.getBlockState(adjPos).getAppearance(level, adjPos,
                            cullFace.getOpposite(), blockState, pos);

                    if (blockState.skipRendering(adjState, cullFace)) {
                        continue;
                    }
                }

                for (BakedQuad quad : quads) {
                    QuadTinter quadTinter = null;

                    // Prebake the color tint into the quad
                    if (quad.getTintIndex() != -1) {
                        quadTinter = new QuadTinter(
                                blockColors.getColor(blockState, facadeAccess, pos, quad.getTintIndex()));
                    }

                    for (AABB box : holeStrips) {
                        emitter.fromVanilla(quad.getVertices(), 0, false);
                        // Keep the cull-face for faces that are flush with the outer block-face on the
                        // side the facade is attached to, but clear it for anything that faces inwards
                        emitter.cullFace(cullFace == side ? side : null);
                        emitter.nominalFace(quad.getDirection());
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

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(textureItem, null,
                null, 0);

        QuadReInterpolator interpolator = new QuadReInterpolator();

        var itemColors = Minecraft.getInstance().getItemColors();
        QuadClamper clamper = new QuadClamper(THICK_FACADE_BOXES[side.ordinal()]);

        for (int cullFaceIdx = 0; cullFaceIdx <= ModelHelper.NULL_FACE_ID; cullFaceIdx++) {
            Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);
            List<BakedQuad> quads = model.getQuads(null, cullFace, RandomSource.create());

            for (BakedQuad quad : quads) {
                QuadTinter quadTinter = null;

                // Prebake the color tint into the quad
                if (quad.getTintIndex() != -1) {
                    quadTinter = new QuadTinter(itemColors.getColor(textureItem, quad.getTintIndex()));
                }

                emitter.fromVanilla(quad.getVertices(), 0, false);
                emitter.cullFace(cullFace);
                emitter.nominalFace(quad.getDirection());
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

        return meshBuilder.build();
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
            case Y:
                boxes.add(new AABB(fb.minX, fb.minY, fb.minZ, hole.minX, fb.maxY, fb.maxZ));
                boxes.add(new AABB(hole.maxX, fb.minY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));

                boxes.add(new AABB(hole.minX, fb.minY, fb.minZ, hole.maxX, fb.maxY, hole.minZ));
                boxes.add(new AABB(hole.minX, fb.minY, hole.maxZ, hole.maxX, fb.maxY, fb.maxZ));

                break;
            case Z:
                boxes.add(new AABB(fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ));
                boxes.add(new AABB(fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));

                boxes.add(new AABB(fb.minX, hole.minY, fb.minZ, hole.minX, hole.maxY, fb.maxZ));
                boxes.add(new AABB(hole.maxX, hole.minY, fb.minZ, fb.maxX, hole.maxY, fb.maxZ));

                break;
            case X:
                boxes.add(new AABB(fb.minX, fb.minY, fb.minZ, fb.maxX, hole.minY, fb.maxZ));
                boxes.add(new AABB(fb.minX, hole.maxY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));

                boxes.add(new AABB(fb.minX, hole.minY, fb.minZ, fb.maxX, hole.maxY, hole.minZ));
                boxes.add(new AABB(fb.minX, hole.minY, hole.maxZ, fb.maxX, hole.maxY, fb.maxZ));
                break;
            default:
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
