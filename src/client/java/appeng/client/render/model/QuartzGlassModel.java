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

package appeng.client.render.model;

import java.util.List;
import java.util.stream.IntStream;

import com.google.common.base.Strings;
import com.mojang.serialization.MapCodec;

import net.minecraft.data.AtlasIds;
import org.joml.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.neoforged.neoforge.model.data.ModelProperty;

import appeng.core.AppEng;
import appeng.decorative.solid.GlassState;
import appeng.decorative.solid.QuartzGlassBlock;

/**
 * Model class for the connected texture glass model.
 */
public class QuartzGlassModel implements DynamicBlockStateModel {
    // This unlisted property is used to determine the actual block that should be
    // rendered
    public static final ModelProperty<GlassState> GLASS_STATE = new ModelProperty<>();

    // Alternating textures based on position
    static final Material TEXTURE_A = new Material(AtlasIds.BLOCKS,
            ResourceLocation.parse("ae2:block/glass/quartz_glass_a"));
    static final Material TEXTURE_B = new Material(AtlasIds.BLOCKS,
            ResourceLocation.parse("ae2:block/glass/quartz_glass_b"));
    static final Material TEXTURE_C = new Material(AtlasIds.BLOCKS,
            ResourceLocation.parse("ae2:block/glass/quartz_glass_c"));
    static final Material TEXTURE_D = new Material(AtlasIds.BLOCKS,
            ResourceLocation.parse("ae2:block/glass/quartz_glass_d"));

    // Frame texture
    static final Material[] TEXTURES_FRAME = generateTexturesFrame();

    // Generates the required textures for the frame
    private static Material[] generateTexturesFrame() {
        return IntStream.range(1, 16).mapToObj(Integer::toBinaryString).map(s -> Strings.padStart(s, 4, '0'))
                .map(s -> ResourceLocation.parse("ae2:block/glass/quartz_glass_frame" + s))
                .map(rl -> new Material(AtlasIds.BLOCKS, rl)).toArray(Material[]::new);
    }

    private final TextureAtlasSprite[] glassTextures;

    private final TextureAtlasSprite[] frameTextures;

    private QuartzGlassModel(SpriteGetter bakedTextureGetter) {
        ModelDebugName debugName = getClass()::toString;
        this.glassTextures = new TextureAtlasSprite[] { bakedTextureGetter.get(TEXTURE_A, debugName),
                bakedTextureGetter.get(TEXTURE_B, debugName), bakedTextureGetter.get(TEXTURE_C, debugName),
                bakedTextureGetter.get(TEXTURE_D, debugName) };

        // The first frame texture would be empty, so we simply leave it set to null
        // here
        this.frameTextures = new TextureAtlasSprite[16];
        for (int i = 0; i < TEXTURES_FRAME.length; i++) {
            this.frameTextures[1 + i] = bakedTextureGetter.get(TEXTURES_FRAME[i], debugName);
        }
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
            List<BlockModelPart> parts) {
        var glassState = getGlassState(level, state, pos);

        var quads = new QuadCollection.Builder();

        for (var side : Direction.values()) {
            int randomOffset = random.nextInt(4);
            var u = randomOffset / 16f;
            var v = random.nextInt(4) / 16f;

            int texIdx = (randomOffset + random.nextInt(4)) % 4;

            if (texIdx < 2) {
                u /= 2;
                v /= 2;
            }

            var glassTexture = this.glassTextures[texIdx];

            // Render the glass side
            // But skip sides that are connected to another glass block
            if (!glassState.hasAdjacentGlassBlock(side)) {
                final List<Vector3f> corners = RenderHelper.getFaceCorners(side);
                quads.addCulledFace(side, this.createQuad(side, corners, glassTexture, u, v));

                final int edgeBitmask = glassState.getMask(side);
                final TextureAtlasSprite sideSprite = this.frameTextures[edgeBitmask];

                if (sideSprite != null) {
                    quads.addCulledFace(side, this.createQuad(side, corners, sideSprite, 0, 0));
                }
            }
        }

        parts.add(new SimpleModelWrapper(quads.build(), false, particleIcon(), ChunkSectionLayer.CUTOUT));
    }

    /**
     * Creates the bitmask that indicates, in which directions (in terms of u,v space) a border should be drawn.
     */
    private static int makeBitmask(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction side) {
        return switch (side) {
            case DOWN -> makeBitmask(level, state, pos, side, Direction.SOUTH, Direction.EAST, Direction.NORTH,
                    Direction.WEST);
            case UP -> makeBitmask(level, state, pos, side, Direction.SOUTH, Direction.WEST, Direction.NORTH,
                    Direction.EAST);
            case NORTH -> makeBitmask(level, state, pos, side, Direction.UP, Direction.WEST, Direction.DOWN,
                    Direction.EAST);
            case SOUTH -> makeBitmask(level, state, pos, side, Direction.UP, Direction.EAST, Direction.DOWN,
                    Direction.WEST);
            case WEST -> makeBitmask(level, state, pos, side, Direction.UP, Direction.SOUTH, Direction.DOWN,
                    Direction.NORTH);
            case EAST -> makeBitmask(level, state, pos, side, Direction.UP, Direction.NORTH, Direction.DOWN,
                    Direction.SOUTH);
        };
    }

    private static int makeBitmask(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction face,
            Direction up, Direction right, Direction down, Direction left) {

        int bitmask = 0;

        if (!isGlassBlock(level, state, pos, face, up, face)) {
            bitmask |= 1;
        }
        if (!isGlassBlock(level, state, pos, face, right, face)) {
            bitmask |= 2;
        }
        if (!isGlassBlock(level, state, pos, face, down, face)) {
            bitmask |= 4;
        }
        if (!isGlassBlock(level, state, pos, face, left, face)) {
            bitmask |= 8;
        }
        return bitmask;
    }

    private BakedQuad createQuad(Direction side, List<Vector3f> corners, TextureAtlasSprite sprite, float uOffset,
            float vOffset) {
        return this.createQuad(side, corners.get(0), corners.get(1), corners.get(2), corners.get(3), sprite, uOffset,
                vOffset);
    }

    private BakedQuad createQuad(Direction side, Vector3f c1, Vector3f c2, Vector3f c3, Vector3f c4,
            TextureAtlasSprite sprite, float uOffset, float vOffset) {
        Vec3 normal = side.getUnitVec3();

        // Apply the u,v shift.
        // This mirrors the logic from OffsetIcon from 1.7
        float u1 = Mth.clamp(0 - uOffset, 0, 1);
        float u2 = Mth.clamp(1 - uOffset, 0, 1);
        float v1 = Mth.clamp(0 - vOffset, 0, 1);
        float v2 = Mth.clamp(1 - vOffset, 0, 1);

        var builder = new QuadBakingVertexConsumer();
        builder.setSprite(sprite);
        builder.setDirection(side);
        this.putVertex(builder, normal, c1.x(), c1.y(), c1.z(), sprite, u1, v1);
        this.putVertex(builder, normal, c2.x(), c2.y(), c2.z(), sprite, u1, v2);
        this.putVertex(builder, normal, c3.x(), c3.y(), c3.z(), sprite, u2, v2);
        this.putVertex(builder, normal, c4.x(), c4.y(), c4.z(), sprite, u2, v1);
        return builder.bakeQuad();
    }

    /*
     * This method is as complicated as it is, because the order in which we push data into the vertexbuffer actually
     * has to be precisely the order in which the vertex elements had been declared in the vertex format.
     */
    private void putVertex(QuadBakingVertexConsumer builder, Vec3 normal, float x, float y, float z,
            TextureAtlasSprite sprite, float u, float v) {
        builder.addVertex(x, y, z);
        builder.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        builder.setNormal((float) normal.x, (float) normal.y, (float) normal.z);
        u = sprite.getU(u);
        v = sprite.getV(v);
        builder.setUv(u, v);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return this.frameTextures[this.frameTextures.length - 1];
    }

    private static GlassState getGlassState(BlockAndTintGetter level, BlockState state, BlockPos pos) {
        /*
         * This needs some explanation: The bit-field contains 4-bits, one for each direction that a frame may be drawn.
         * Converted to a number, the bit-field is then used as an index into the list of frame textures, which have
         * been created in such a way that their filenames indicate, in which directions they contain borders. i.e.
         * bitmask = 0101 means a border should be drawn up and down (in terms of u,v space). Converted to a number,
         * this bitmask is 5. So the texture at index 5 is used. That texture had "0101" in its filename to indicate
         * this.
         */
        int[] masks = new int[6];
        for (Direction facing : Direction.values()) {
            masks[facing.get3DDataValue()] = makeBitmask(level, state, pos, facing);
        }
        boolean[] adjacentGlassBlocks = new boolean[6];
        for (Direction facing : Direction.values()) {
            adjacentGlassBlocks[facing.get3DDataValue()] = isGlassBlock(level, state, pos, facing,
                    facing, facing.getOpposite());
        }
        return new GlassState(masks, adjacentGlassBlocks);
    }

    /**
     * Checks if the given block is a glass block.
     *
     * @param queryingFace Face of the glass that is currently performing the check.
     * @param adjFace      Face of the glass that we are currently checking for.
     * @param adjDir       Direction in which to check.
     */
    private static boolean isGlassBlock(BlockAndTintGetter level, BlockState state, BlockPos pos,
            Direction queryingFace, Direction adjDir, Direction adjFace) {
        var adjacentPos = pos.relative(adjDir);
        var adjacentState = level.getBlockState(adjacentPos);
        // Checks that the adjacent block is indeed glass
        if (!(adjacentState.getAppearance(level, adjacentPos, adjFace, state, pos)
                .getBlock() instanceof QuartzGlassBlock)) {
            return false;
        }
        // Checks that the current block is also glass, in other words that the adjacent block would connect to us.
        // This ensures consistency between this block and the adjacent block deciding to connect or not.
        // This is important for advanced use cases such as FramedBlocks.
        return state.getAppearance(level, pos, queryingFace, adjacentState, adjacentPos)
                .getBlock() instanceof QuartzGlassBlock;
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final ResourceLocation ID = AppEng.makeId("quartz_glass");
        public static final MapCodec<QuartzGlassModel.Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new QuartzGlassModel(baker.sprites());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }

        @Override
        public MapCodec<QuartzGlassModel.Unbaked> codec() {
            return MAP_CODEC;
        }
    }
}
