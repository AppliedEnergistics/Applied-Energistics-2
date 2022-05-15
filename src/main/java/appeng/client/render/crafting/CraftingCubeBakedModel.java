/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.render.crafting;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import appeng.blockentity.crafting.CraftingCubeModelData;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.util.Platform;

/**
 * The base model for baked models used by components of the crafting cube multi-block in it's formed state. Primarily
 * this base class handles adding the "ring" that frames the multi-block structure and delegates rendering of the
 * "inner" part of each block to the subclasses of this class.
 */
abstract class CraftingCubeBakedModel implements BakedModel, FabricBakedModel {

    private final TextureAtlasSprite ringCorner;

    private final TextureAtlasSprite ringHor;

    private final TextureAtlasSprite ringVer;

    CraftingCubeBakedModel(TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor, TextureAtlasSprite ringVer) {
        this.ringCorner = ringCorner;
        this.ringHor = ringHor;
        this.ringVer = ringVer;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
                               Supplier<RandomSource> RandomSourceSupplier, RenderContext context) {
        var modelData = getModelData(blockView, pos);
        var connections = modelData != null ? modelData.getConnections()
                : EnumSet.noneOf(Direction.class);

        CubeBuilder builder = new CubeBuilder(context.getEmitter());

        for (Direction side : Direction.values()) {
            builder.setDrawFaces(EnumSet.of(side));

            // Add the quads for the ring that frames the entire multi-block structure
            this.addRing(builder, side, connections);

            // Calculate the bounds of the "inner" block that is framed by the border drawn
            // above
            float x2 = connections.contains(Direction.EAST) ? 16 : 13.01f;
            float x1 = connections.contains(Direction.WEST) ? 0 : 2.99f;

            float y2 = connections.contains(Direction.UP) ? 16 : 13.01f;
            float y1 = connections.contains(Direction.DOWN) ? 0 : 2.99f;

            float z2 = connections.contains(Direction.SOUTH) ? 16 : 13.01f;
            float z1 = connections.contains(Direction.NORTH) ? 0 : 2.99f;

            // On the axis of the side that we're currently drawing, extend the dimensions
            // out to the outer face of the block
            switch (side) {
                case DOWN:
                case UP:
                    y1 = 0;
                    y2 = 16;
                    break;
                case NORTH:
                case SOUTH:
                    z1 = 0;
                    z2 = 16;
                    break;
                case WEST:
                case EAST:
                    x1 = 0;
                    x2 = 16;
                    break;
            }

            this.addInnerCube(side, state, modelData, builder, x1, y1, z1, x2, y2, z2);
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> RandomSourceSupplier, RenderContext context) {

    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource RandomSource) {
        return Collections.emptyList();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    private void addRing(CubeBuilder builder, Direction side, EnumSet<Direction> connections) {
        // Fill in the corners
        builder.setTexture(this.ringCorner);
        this.addCornerCap(builder, connections, side, Direction.UP, Direction.EAST, Direction.NORTH);
        this.addCornerCap(builder, connections, side, Direction.UP, Direction.EAST, Direction.SOUTH);
        this.addCornerCap(builder, connections, side, Direction.UP, Direction.WEST, Direction.NORTH);
        this.addCornerCap(builder, connections, side, Direction.UP, Direction.WEST, Direction.SOUTH);
        this.addCornerCap(builder, connections, side, Direction.DOWN, Direction.EAST, Direction.NORTH);
        this.addCornerCap(builder, connections, side, Direction.DOWN, Direction.EAST, Direction.SOUTH);
        this.addCornerCap(builder, connections, side, Direction.DOWN, Direction.WEST, Direction.NORTH);
        this.addCornerCap(builder, connections, side, Direction.DOWN, Direction.WEST, Direction.SOUTH);

        // Fill in the remaining stripes of the face
        for (Direction a : Direction.values()) {
            if (a == side || a == side.getOpposite()) {
                continue;
            }

            // Select the horizontal or vertical ring texture depending on which side we're
            // filling in
            if (side.getAxis() != Direction.Axis.Y
                    && (a == Direction.NORTH || a == Direction.EAST || a == Direction.WEST || a == Direction.SOUTH)) {
                builder.setTexture(this.ringVer);
            } else if (side.getAxis() == Direction.Axis.Y && (a == Direction.EAST || a == Direction.WEST)) {
                builder.setTexture(this.ringVer);
            } else {
                builder.setTexture(this.ringHor);
            }

            // If there's an adjacent crafting cube block on side a, then the core of the
            // block already extends
            // fully to this side. So only bother drawing the stripe, if there's no
            // connection.
            if (!connections.contains(a)) {
                // Note that since we're drawing something that "looks" 2-dimensional,
                // two of the following will always be 0 and 16.
                float x1 = 0, y1 = 0, z1 = 0, x2 = 16, y2 = 16, z2 = 16;

                switch (a) {
                    case DOWN:
                        y1 = 0;
                        y2 = 3;
                        break;
                    case UP:
                        y1 = 13.0f;
                        y2 = 16;
                        break;
                    case WEST:
                        x1 = 0;
                        x2 = 3;
                        break;
                    case EAST:
                        x1 = 13;
                        x2 = 16;
                        break;
                    case NORTH:
                        z1 = 0;
                        z2 = 3;
                        break;
                    case SOUTH:
                        z1 = 13;
                        z2 = 16;
                        break;
                }

                // Constraint the stripe in the two directions perpendicular to a in case there
                // has been a corner
                // drawn in those directions. Since a corner is drawn if the three touching
                // faces dont have adjacent
                // crafting cube blocks, we'd have to check for a, side, and the perpendicular
                // direction. But in this
                // block, we've already checked for side (due to face culling) and a (see
                // above).
                Direction perpendicular = Platform.rotateAround(a, side);
                for (Direction cornerCandidate : EnumSet.of(perpendicular, perpendicular.getOpposite())) {
                    if (!connections.contains(cornerCandidate)) {
                        // There's a cap in this direction
                        switch (cornerCandidate) {
                            case DOWN:
                                y1 = 3;
                                break;
                            case UP:
                                y2 = 13;
                                break;
                            case NORTH:
                                z1 = 3;
                                break;
                            case SOUTH:
                                z2 = 13;
                                break;
                            case WEST:
                                x1 = 3;
                                break;
                            case EAST:
                                x2 = 13;
                                break;
                        }
                    }
                }

                builder.addCube(x1, y1, z1, x2, y2, z2);
            }
        }
    }

    /**
     * Adds a 3x3x3 corner cap to the cube builder if there are no adjacent crafting cubes on that corner.
     */
    private void addCornerCap(CubeBuilder builder, EnumSet<Direction> connections, Direction side, Direction down,
            Direction west, Direction north) {
        if (connections.contains(down) || connections.contains(west) || connections.contains(north)) {
            return;
        }

        // Only add faces for sides that can actually be seen (the outside of the cube)
        if (side != down && side != west && side != north) {
            return;
        }

        float x1 = west == Direction.WEST ? 0 : 13;
        float y1 = down == Direction.DOWN ? 0 : 13;
        float z1 = north == Direction.NORTH ? 0 : 13;
        float x2 = west == Direction.WEST ? 3 : 16;
        float y2 = down == Direction.DOWN ? 3 : 16;
        float z2 = north == Direction.NORTH ? 3 : 16;
        builder.addCube(x1, y1, z1, x2, y2, z2);
    }

    // Retrieve the cube connection state from the block state
    // If none is present, just assume there are no adjacent crafting cube blocks
    private static CraftingCubeModelData getModelData(BlockAndTintGetter blockRenderView, BlockPos pos) {
        if (!(blockRenderView instanceof RenderAttachedBlockView)) {
            return null;
        }
        Object attached = ((RenderAttachedBlockView) blockRenderView).getBlockEntityRenderAttachment(pos);
        if (attached instanceof CraftingCubeModelData) {
            return (CraftingCubeModelData) attached;
        }
        return null;
    }

    protected abstract void addInnerCube(Direction facing, BlockState state, CraftingCubeModelData modelData,
            CubeBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2);

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.ringCorner;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

}
