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


import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.cablebus.CubeBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;


/**
 * The base model for baked models used by components of the crafting cube multi-block in it's formed state.
 * Primarily this base class handles adding the "ring" that frames the multi-block structure and delegates
 * rendering of the "inner" part of each block to the subclasses of this class.
 */
abstract class CraftingCubeBakedModel implements IBakedModel {

    private final VertexFormat format;

    private final TextureAtlasSprite ringCorner;

    private final TextureAtlasSprite ringHor;

    private final TextureAtlasSprite ringVer;

    CraftingCubeBakedModel(VertexFormat format, TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor, TextureAtlasSprite ringVer) {
        this.format = format;
        this.ringCorner = ringCorner;
        this.ringHor = ringHor;
        this.ringVer = ringVer;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

        if (side == null) {
            return Collections.emptyList(); // No generic quads for this model
        }

        EnumSet<EnumFacing> connections = getConnections(state);

        List<BakedQuad> quads = new ArrayList<>();
        CubeBuilder builder = new CubeBuilder(this.format, quads);

        builder.setDrawFaces(EnumSet.of(side));

        // Add the quads for the ring that frames the entire multi-block structure
        this.addRing(builder, side, connections);

        // Calculate the bounds of the "inner" block that is framed by the border drawn above
        float x2 = connections.contains(EnumFacing.EAST) ? 16 : 13.01f;
        float x1 = connections.contains(EnumFacing.WEST) ? 0 : 2.99f;

        float y2 = connections.contains(EnumFacing.UP) ? 16 : 13.01f;
        float y1 = connections.contains(EnumFacing.DOWN) ? 0 : 2.99f;

        float z2 = connections.contains(EnumFacing.SOUTH) ? 16 : 13.01f;
        float z1 = connections.contains(EnumFacing.NORTH) ? 0 : 2.99f;

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

        this.addInnerCube(side, state, builder, x1, y1, z1, x2, y2, z2);

        return quads;
    }

    private void addRing(CubeBuilder builder, @Nullable EnumFacing side, EnumSet<EnumFacing> connections) {
        // Fill in the corners
        builder.setTexture(this.ringCorner);
        this.addCornerCap(builder, connections, side, EnumFacing.UP, EnumFacing.EAST, EnumFacing.NORTH);
        this.addCornerCap(builder, connections, side, EnumFacing.UP, EnumFacing.EAST, EnumFacing.SOUTH);
        this.addCornerCap(builder, connections, side, EnumFacing.UP, EnumFacing.WEST, EnumFacing.NORTH);
        this.addCornerCap(builder, connections, side, EnumFacing.UP, EnumFacing.WEST, EnumFacing.SOUTH);
        this.addCornerCap(builder, connections, side, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.NORTH);
        this.addCornerCap(builder, connections, side, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.SOUTH);
        this.addCornerCap(builder, connections, side, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.NORTH);
        this.addCornerCap(builder, connections, side, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.SOUTH);

        // Fill in the remaining stripes of the face
        for (EnumFacing a : EnumFacing.values()) {
            if (a == side || a == side.getOpposite()) {
                continue;
            }

            // Select the horizontal or vertical ring texture depending on which side we're filling in
            if ((side.getAxis() != EnumFacing.Axis.Y) && (a == EnumFacing.NORTH || a == EnumFacing.EAST || a == EnumFacing.WEST || a == EnumFacing.SOUTH)) {
                builder.setTexture(this.ringVer);
            } else if (side.getAxis() == EnumFacing.Axis.Y && (a == EnumFacing.EAST || a == EnumFacing.WEST)) {
                builder.setTexture(this.ringVer);
            } else {
                builder.setTexture(this.ringHor);
            }

            // If there's an adjacent crafting cube block on side a, then the core of the block already extends
            // fully to this side. So only bother drawing the stripe, if there's no connection.
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

                // Constraint the stripe in the two directions perpendicular to a in case there has been a corner
                // drawn in those directions. Since a corner is drawn if the three touching faces dont have adjacent
                // crafting cube blocks, we'd have to check for a, side, and the perpendicular direction. But in this
                // block, we've already checked for side (due to face culling) and a (see above).
                EnumFacing perpendicular = a.rotateAround(side.getAxis());
                for (EnumFacing cornerCandidate : EnumSet.of(perpendicular, perpendicular.getOpposite())) {
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
    private void addCornerCap(CubeBuilder builder, EnumSet<EnumFacing> connections, EnumFacing side, EnumFacing down, EnumFacing west, EnumFacing north) {
        if (connections.contains(down) || connections.contains(west) || connections.contains(north)) {
            return;
        }

        // Only add faces for sides that can actually be seen (the outside of the cube)
        if (side != down && side != west && side != north) {
            return;
        }

        float x1 = (west == EnumFacing.WEST ? 0 : 13);
        float y1 = (down == EnumFacing.DOWN ? 0 : 13);
        float z1 = (north == EnumFacing.NORTH ? 0 : 13);
        float x2 = (west == EnumFacing.WEST ? 3 : 16);
        float y2 = (down == EnumFacing.DOWN ? 3 : 16);
        float z2 = (north == EnumFacing.NORTH ? 3 : 16);
        builder.addCube(x1, y1, z1, x2, y2, z2);
    }

    // Retrieve the cube connection state from the block state
    // If none is present, just assume there are no adjacent crafting cube blocks
    private static EnumSet<EnumFacing> getConnections(@Nullable IBlockState state) {
        if (!(state instanceof IExtendedBlockState)) {
            return EnumSet.noneOf(EnumFacing.class);
        }

        IExtendedBlockState extState = (IExtendedBlockState) state;
        CraftingCubeState cubeState = extState.getValue(BlockCraftingUnit.STATE);
        if (cubeState == null) {
            return EnumSet.noneOf(EnumFacing.class);
        }

        return cubeState.getConnections();
    }

    protected abstract void addInnerCube(EnumFacing facing, IBlockState state, CubeBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2);

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.ringCorner;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
