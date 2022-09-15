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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

import appeng.decorative.solid.GlassState;
import appeng.decorative.solid.QuartzGlassBlock;

class GlassBakedModel implements IDynamicBakedModel {

    // This unlisted property is used to determine the actual block that should be
    // rendered
    public static final ModelProperty<GlassState> GLASS_STATE = new ModelProperty<>();

    private static final byte[][][] OFFSETS = generateOffsets();

    // Alternating textures based on position
    static final Material TEXTURE_A = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation("ae2:block/glass/quartz_glass_a"));
    static final Material TEXTURE_B = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation("ae2:block/glass/quartz_glass_b"));
    static final Material TEXTURE_C = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation("ae2:block/glass/quartz_glass_c"));
    static final Material TEXTURE_D = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation("ae2:block/glass/quartz_glass_d"));

    // Frame texture
    static final Material[] TEXTURES_FRAME = generateTexturesFrame();

    // Generates the required textures for the frame
    private static Material[] generateTexturesFrame() {
        return IntStream.range(1, 16).mapToObj(Integer::toBinaryString).map(s -> Strings.padStart(s, 4, '0'))
                .map(s -> new ResourceLocation("ae2:block/glass/quartz_glass_frame" + s))
                .map(rl -> new Material(TextureAtlas.LOCATION_BLOCKS, rl)).toArray(Material[]::new);
    }

    private final TextureAtlasSprite[] glassTextures;

    private final TextureAtlasSprite[] frameTextures;

    public GlassBakedModel(Function<Material, TextureAtlasSprite> bakedTextureGetter) {
        this.glassTextures = new TextureAtlasSprite[] { bakedTextureGetter.apply(TEXTURE_A),
                bakedTextureGetter.apply(TEXTURE_B), bakedTextureGetter.apply(TEXTURE_C),
                bakedTextureGetter.apply(TEXTURE_D) };

        // The first frame texture would be empty, so we simply leave it set to null
        // here
        this.frameTextures = new TextureAtlasSprite[16];
        for (int i = 0; i < TEXTURES_FRAME.length; i++) {
            this.frameTextures[1 + i] = bakedTextureGetter.apply(TEXTURES_FRAME[i]);
        }
    }

    @Override
    public @NotNull ModelData getModelData(BlockAndTintGetter blockView, @NotNull BlockPos pos,
            @NotNull BlockState state, @NotNull ModelData modelData) {
        final GlassState glassState = getGlassState(blockView, state, pos);
        return modelData.derive().with(GLASS_STATE, glassState).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData extraData, RenderType renderType) {
        if (side == null) {
            return Collections.emptyList();
        }

        final GlassState glassState = extraData.get(GLASS_STATE);

        if (glassState == null) {
            return Collections.emptyList();
        }

        // TODO: This could just use the Random instance we're given...
        final int cx = Math.abs(glassState.getX() % 10);
        final int cy = Math.abs(glassState.getY() % 10);
        final int cz = Math.abs(glassState.getZ() % 10);

        int u = OFFSETS[cx][cy][cz] % 4;
        int v = OFFSETS[9 - cx][9 - cy][9 - cz] % 4;

        int texIdx = Math.abs((OFFSETS[cx][cy][cz] + (glassState.getX() + glassState.getY() + glassState.getZ())) % 4);

        if (texIdx < 2) {
            u /= 2;
            v /= 2;
        }

        final TextureAtlasSprite glassTexture = this.glassTextures[texIdx];

        // Render the glass side
        if (glassState.hasAdjacentGlassBlock(side)) { // Skip sides that are connected to another glass block
            return Collections.emptyList();
        }

        final List<BakedQuad> quads = new ArrayList<>(5); // At most 5

        final List<Vector3f> corners = RenderHelper.getFaceCorners(side);
        quads.add(this.createQuad(side, corners, glassTexture, u, v));

        final int edgeBitmask = glassState.getMask(side);
        final TextureAtlasSprite sideSprite = this.frameTextures[edgeBitmask];

        if (sideSprite != null) {
            quads.add(this.createQuad(side, corners, sideSprite, 0, 0));
        }

        return quads;
    }

    @Override
    public boolean usesBlockLight() {
        // TODO: Forge: Auto-generated method stub
        return false;
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

        if (!isGlassBlock(level, state, pos, face, up)) {
            bitmask |= 1;
        }
        if (!isGlassBlock(level, state, pos, face, right)) {
            bitmask |= 2;
        }
        if (!isGlassBlock(level, state, pos, face, down)) {
            bitmask |= 4;
        }
        if (!isGlassBlock(level, state, pos, face, left)) {
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
        Vec3 normal = new Vec3(side.getNormal().getX(), side.getNormal().getY(),
                side.getNormal().getZ());

        // Apply the u,v shift.
        // This mirrors the logic from OffsetIcon from 1.7
        float u1 = Mth.clamp(0 - uOffset, 0, 16);
        float u2 = Mth.clamp(16 - uOffset, 0, 16);
        float v1 = Mth.clamp(0 - vOffset, 0, 16);
        float v2 = Mth.clamp(16 - vOffset, 0, 16);

        var result = new MutableObject<BakedQuad>();
        var builder = new QuadBakingVertexConsumer(result::setValue);
        builder.setSprite(sprite);
        builder.setDirection(side);
        this.putVertex(builder, normal, c1.x(), c1.y(), c1.z(), sprite, u1, v1);
        this.putVertex(builder, normal, c2.x(), c2.y(), c2.z(), sprite, u1, v2);
        this.putVertex(builder, normal, c3.x(), c3.y(), c3.z(), sprite, u2, v2);
        this.putVertex(builder, normal, c4.x(), c4.y(), c4.z(), sprite, u2, v1);
        return result.getValue();
    }

    /*
     * This method is as complicated as it is, because the order in which we push data into the vertexbuffer actually
     * has to be precisely the order in which the vertex elements had been declared in the vertex format.
     */
    private void putVertex(QuadBakingVertexConsumer builder, Vec3 normal, double x, double y, double z,
            TextureAtlasSprite sprite, float u, float v) {
        builder.vertex(x, y, z);
        builder.color(1.0f, 1.0f, 1.0f, 1.0f);
        builder.normal((float) normal.x, (float) normal.y, (float) normal.z);
        u = sprite.getU(u);
        v = sprite.getV(v);
        builder.uv(u, v);
        builder.endVertex();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

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
        return this.frameTextures[this.frameTextures.length - 1];
    }

    private static byte[][][] generateOffsets() {
        final Random r = new Random(924);
        final byte[][][] offset = new byte[10][10][10];

        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                r.nextBytes(offset[x][y]);
            }
        }

        return offset;
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
            adjacentGlassBlocks[facing.get3DDataValue()] = isGlassBlock(level, state, pos, facing.getOpposite(),
                    facing);
        }
        return new GlassState(pos.getX(), pos.getY(), pos.getZ(), masks, adjacentGlassBlocks);
    }

    /**
     * Checks if the given block is a glass block.
     *
     * @param face   Face of the glass that we are currently checking for.
     * @param adjDir Direction in which to check.
     */
    private static boolean isGlassBlock(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction face,
            Direction adjDir) {
        var adjacentPos = pos.relative(adjDir);
        return level.getBlockState(adjacentPos).getAppearance(level, adjacentPos, face, state, pos)
                .getBlock() instanceof QuartzGlassBlock;
    }

}
