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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.phys.AABB;

import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import appeng.api.util.AEAxisAlignedBB;
import appeng.block.networking.CableBusBlock;
import appeng.block.networking.CableBusRenderState;
import appeng.client.model.FacingModelState;
import appeng.core.AppEng;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadClamper;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadCornerKicker;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadFaceStripper;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadReInterpolator;
import appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadTinter;
import appeng.thirdparty.fabric.MeshBuilder;
import appeng.thirdparty.fabric.ModelHelper;
import appeng.thirdparty.fabric.QuadEmitter;
import appeng.thirdparty.fabric.Renderer;

/**
 * The FacadeBuilder builds for facades..
 *
 * @author covers1624
 */
public class FacadeBuilder {

    public static final ModelBaker.SharedOperationKey<FacadeBuilder> SHARED_KEY = FacadeBuilder::new;

    private static final ResourceLocation ANCHOR_STILT = AppEng.makeId("part/cable_anchor_short");;
    private static final ResourceLocation TRANSLUCENT_FACADE_MODEL = AppEng.makeId("part/translucent_facade");

    private final Renderer renderer = Renderer.getInstance();

    // Slightly smaller than a pixel to never show the beginning of the second row of pixels of the block's texture.
    public static final double THIN_THICKNESS = 1D / 16D - 2e-3;

    public static final AABB[] THIN_FACADE_BOXES = new AABB[] {
            new AABB(0.0, 0.0, 0.0, 1.0, THIN_THICKNESS, 1.0),
            new AABB(0.0, 1.0 - THIN_THICKNESS, 0.0, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, 1.0, 1.0, THIN_THICKNESS),
            new AABB(0.0, 0.0, 1.0 - THIN_THICKNESS, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, THIN_THICKNESS, 1.0, 1.0),
            new AABB(1.0 - THIN_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0) };

    // Pre-rotated transparent facade quads
    private final Map<Direction, SimpleModelWrapper> transparentFacadeModels;

    private final Map<Direction, SimpleModelWrapper> cableAnchorStilts;

    public FacadeBuilder(ModelBaker baker) {
        cableAnchorStilts = new EnumMap<>(Direction.class);
        transparentFacadeModels = new EnumMap<>(Direction.class);
        for (var facing : Direction.values()) {
            cableAnchorStilts.put(facing,
                    SimpleModelWrapper.bake(baker, ANCHOR_STILT, FacingModelState.fromFacing(facing)));
            transparentFacadeModels.put(facing,
                    SimpleModelWrapper.bake(baker, TRANSLUCENT_FACADE_MODEL, FacingModelState.fromFacing(facing)));
        }
    }

    public void collectFacadeParts(CableBusRenderState renderState,
            BlockAndTintGetter level,
            Consumer<BlockModelPart> partConsumer) {
        boolean transparent = PartHelper.getCableRenderMode().transparentFacades;

        collectFacadePartsInternal(
                transparent,
                renderState::getFacade,
                level,
                renderState.getBoundingBoxes(),
                renderState.getAttachments().keySet(),
                renderState.getPos(),
                partConsumer);
    }

    public void collectFacadePartsInternal(
            boolean transparent,
            Function<Direction, BlockState> facadeGetter,
            BlockAndTintGetter level,
            List<AABB> partBoxes,
            Set<Direction> sidesWithParts,
            BlockPos pos,
            Consumer<BlockModelPart> partConsumer) {
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();

        for (var side : IPart.ATTACHMENT_POINTS) {
            var blockState = facadeGetter.apply(side);
            if (blockState == null) {
                continue;
            }

            int sideIndex = side.ordinal();
            boolean renderStilt = !sidesWithParts.contains(side);
            if (renderStilt) {
                partConsumer.accept(cableAnchorStilts.get(side));
            }

            // When we're forcing transparent facades, add a "border" model that indicates
            // where the facade is,
            // But otherwise skip the rest.
            if (transparent) {
                partConsumer.accept(transparentFacadeModels.get(side));
                continue;
            }

            var dispatcher = Minecraft.getInstance().getBlockRenderer();
            var model = dispatcher.getBlockModel(blockState);

            AABB fullBounds = THIN_FACADE_BOXES[sideIndex];
            AABB facadeBox = fullBounds;
            // If we are a transparent facade, we need to modify out BB.
            if (!blockState.isSolidRender()) {
                double offset = THIN_THICKNESS;
                AEAxisAlignedBB tmpBB = null;
                for (Direction face : Direction.values()) {
                    // Only faces that aren't on our axis
                    if (face.getAxis() != side.getAxis()) {
                        var otherState = facadeGetter.apply(face);
                        if (otherState != null && otherState.isSolidRender()) {
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
            for (var otherSide : IPart.ATTACHMENT_POINTS) {
                if (otherSide.getAxis() != side.getAxis()) {
                    var otherState = facadeGetter.apply(otherSide);
                    if (otherState != null && otherState.isSolidRender()) {
                        facadeMask |= 1 << otherSide.ordinal();
                    }
                }
            }

            AEAxisAlignedBB cutOutBox = getCutOutBox(facadeBox, partBoxes);
            List<AABB> holeStrips = getBoxes(facadeBox, cutOutBox, side.getAxis());

            QuadFaceStripper faceStripper = new QuadFaceStripper(fullBounds, facadeMask);
            // Setup the kicker.
            QuadCornerKicker kicker = new QuadCornerKicker();
            kicker.setSide(sideIndex);
            kicker.setFacadeMask(facadeMask);
            kicker.setBox(fullBounds);
            kicker.setThickness(THIN_THICKNESS);

            QuadReInterpolator interpolator = new QuadReInterpolator();

            var random = new SingleThreadedRandomSource(blockState.getSeed(pos));
            var parts = model.collectParts(level, pos, blockState, random);

            // Transform each part emitted by the block model
            for (var part : parts) {
                MeshBuilder meshBuilder = renderer.meshBuilder();
                QuadEmitter emitter = meshBuilder.getEmitter();

                for (int cullFaceIdx = 0; cullFaceIdx <= ModelHelper.NULL_FACE_ID; cullFaceIdx++) {
                    Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);

                    // Ignore quad if it's not supposed to connect to the adjacent block.
                    if (cullFace != null) {
                        BlockPos adjPos = pos.relative(cullFace);
                        CableBusBlock.RENDERING_FACADE_DIRECTION.set(side);
                        BlockState adjState = level.getBlockState(adjPos).getAppearance(level, adjPos,
                                cullFace.getOpposite(), blockState, pos);
                        CableBusBlock.RENDERING_FACADE_DIRECTION.remove();

                        if (blockState.skipRendering(adjState, cullFace)) {
                            continue;
                        }
                    }

                    for (BakedQuad quad : part.getQuads(cullFace)) {
                        QuadTinter quadTinter = null;

                        // Prebake the color tint into the quad
                        if (quad.tintIndex() != -1) {
                            quadTinter = new QuadTinter(
                                    blockColors.getColor(blockState, level, pos, quad.tintIndex()));
                        }

                        for (AABB box : holeStrips) {
                            emitter.fromVanilla(quad.vertices(), 0);
                            // Keep the cull-face for faces that are flush with the outer block-face on the
                            // side the facade is attached to, but clear it for anything that faces inwards
                            emitter.cullFace(cullFace == side ? side : null);
                            emitter.nominalFace(quad.direction());
                            emitter.shade(quad.shade());
                            emitter.ambientOcclusion(quad.hasAmbientOcclusion());
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

                // Build a new quad collection
                var unculledQuads = new ArrayList<>(meshBuilder.build().toBakedBlockQuads());
                if (!unculledQuads.isEmpty()) {
                    partConsumer.accept(new BlockModelPart() {
                        @Override
                        public List<BakedQuad> getQuads(@Nullable Direction side) {
                            return side == null ? unculledQuads : List.of();
                        }

                        @Override
                        public boolean useAmbientOcclusion() {
                            return part.useAmbientOcclusion();
                        }

                        @Override
                        public TriState ambientOcclusion() {
                            return part.ambientOcclusion();
                        }

                        @Override
                        public TextureAtlasSprite particleIcon() {
                            return part.particleIcon();
                        }

                        @Override
                        public RenderType getRenderType(BlockState state) {
                            return part.getRenderType(state);
                        }
                    });
                }
            }
        }

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
        }

        return boxes;
    }

    public static void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(TRANSLUCENT_FACADE_MODEL);
        resolver.markDependency(ANCHOR_STILT);
    }
}
