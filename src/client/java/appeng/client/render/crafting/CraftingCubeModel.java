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

import java.util.EnumSet;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import appeng.block.crafting.CraftingUnitType;
import appeng.blockentity.crafting.CraftingCubeModelData;
import appeng.client.render.CubeBuilder;
import appeng.core.AppEng;
import appeng.thirdparty.fabric.ModelHelper;
import appeng.util.Platform;

/**
 * The base model for baked models used by components of the crafting cube multi-block in it's formed state. Primarily
 * this base class handles adding the "ring" that frames the multi-block structure and delegates rendering of the
 * "inner" part of each block to the subclasses of this class.
 */
public abstract class CraftingCubeModel implements DynamicBlockStateModel {
    private final TextureAtlasSprite ringCorner;

    private final TextureAtlasSprite ringHor;

    private final TextureAtlasSprite ringVer;

    CraftingCubeModel(TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor, TextureAtlasSprite ringVer) {
        this.ringCorner = ringCorner;
        this.ringHor = ringHor;
        this.ringVer = ringVer;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
            List<BlockModelPart> parts) {

        var extraData = level.getModelData(pos);

        EnumSet<Direction> connections = getConnections(extraData);

        // TODO: Redesign this by allowing a CubeBuilder to directly emit to a QuadCollection with correct cull faces
        var quadCollection = new QuadCollection.Builder();
        for (int cullFaceIdx = 0; cullFaceIdx < ModelHelper.NULL_FACE_ID; cullFaceIdx++) {
            Direction cullFace = ModelHelper.faceFromIndex(cullFaceIdx);

            CubeBuilder builder = new CubeBuilder(quad -> quadCollection.addCulledFace(cullFace, quad));

            builder.setDrawFaces(EnumSet.of(cullFace));

            // Add the quads for the ring that frames the entire multi-block structure
            this.addRing(builder, cullFace, connections);

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
            switch (cullFace) {
                case DOWN, UP -> {
                    y1 = 0;
                    y2 = 16;
                }
                case NORTH, SOUTH -> {
                    z1 = 0;
                    z2 = 16;
                }
                case WEST, EAST -> {
                    x1 = 0;
                    x2 = 16;
                }
            }

            this.addInnerCube(cullFace, state, extraData, builder, x1, y1, z1, x2, y2, z2);
        }

        parts.add(new SimpleModelWrapper(quadCollection.build(), false, ringCorner, RenderType.cutout()));
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
            if (side.getAxis() != Axis.Y
                    && (a == Direction.NORTH || a == Direction.EAST || a == Direction.WEST || a == Direction.SOUTH)) {
                builder.setTexture(this.ringVer);
            } else if (side.getAxis() == Axis.Y && (a == Direction.EAST || a == Direction.WEST)) {
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
                    case DOWN -> {
                        y1 = 0;
                        y2 = 3;
                    }
                    case UP -> {
                        y1 = 13.0f;
                        y2 = 16;
                    }
                    case WEST -> {
                        x1 = 0;
                        x2 = 3;
                    }
                    case EAST -> {
                        x1 = 13;
                        x2 = 16;
                    }
                    case NORTH -> {
                        z1 = 0;
                        z2 = 3;
                    }
                    case SOUTH -> {
                        z1 = 13;
                        z2 = 16;
                    }
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
                            case DOWN -> y1 = 3;
                            case UP -> y2 = 13;
                            case NORTH -> z1 = 3;
                            case SOUTH -> z2 = 13;
                            case WEST -> x1 = 3;
                            case EAST -> x2 = 13;
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
    private static EnumSet<Direction> getConnections(ModelData modelData) {
        if (modelData.has(CraftingCubeModelData.CONNECTIONS)) {
            return modelData.get(CraftingCubeModelData.CONNECTIONS);
        }
        return EnumSet.noneOf(Direction.class);
    }

    protected abstract void addInnerCube(Direction facing, BlockState state, ModelData modelData, CubeBuilder builder,
            float x1, float y1, float z1, float x2, float y2, float z2);

    @Override
    public TextureAtlasSprite particleIcon() {
        return ringCorner;
    }

    public record Unbaked(CraftingUnitType type) implements CustomUnbakedBlockStateModel {
        public static final ResourceLocation ID = AppEng.makeId("crafting_cube");
        public static final MapCodec<CraftingCubeModel.Unbaked> MAP_CODEC = RecordCodecBuilder
                .mapCodec(instance -> instance.group(
                        CraftingUnitType.CODEC.fieldOf("unit_type").forGetter(Unbaked::type))
                        .apply(instance, CraftingCubeModel.Unbaked::new));

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            var provider = new CraftingUnitModelProvider(type);
            return provider.bake(baker.sprites());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }

        @Override
        public MapCodec<CraftingCubeModel.Unbaked> codec() {
            return MAP_CODEC;
        }
    }
}
