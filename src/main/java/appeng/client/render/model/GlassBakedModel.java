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
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Strings;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import appeng.decorative.solid.GlassState;
import appeng.decorative.solid.QuartzGlassBlock;

class GlassBakedModel implements IDynamicBakedModel {

    // This unlisted property is used to determine the actual block that should be
    // rendered
    public static final ModelProperty<GlassState> GLASS_STATE = new ModelProperty<>();

    private static final byte[][][] OFFSETS = generateOffsets();

    // Alternating textures based on position
    static final Material TEXTURE_A = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation("appliedenergistics2:block/glass/quartz_glass_a"));
    static final Material TEXTURE_B = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation("appliedenergistics2:block/glass/quartz_glass_b"));
    static final Material TEXTURE_C = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation("appliedenergistics2:block/glass/quartz_glass_c"));
    static final Material TEXTURE_D = new Material(TextureAtlas.LOCATION_BLOCKS,
            new net.minecraft.resources.ResourceLocation("appliedenergistics2:block/glass/quartz_glass_d"));

    // Frame texture
    static final Material[] TEXTURES_FRAME = generateTexturesFrame();

    // Generates the required textures for the frame
    private static Material[] generateTexturesFrame() {
        return IntStream.range(1, 16).mapToObj(Integer::toBinaryString).map(s -> Strings.padStart(s, 4, '0'))
                .map(s -> new ResourceLocation("appliedenergistics2:block/glass/quartz_glass_frame" + s))
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
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand,
                                    IModelData extraData) {
        if (side == null) {
            return Collections.emptyList();
        }

        final GlassState glassState = extraData.getData(GLASS_STATE);

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
        final List<BakedQuad> quads = new ArrayList<>(5); // At most 5

        final List<Vec3> corners = RenderHelper.getFaceCorners(side);
        quads.add(this.createQuad(side, corners, glassTexture, u, v));

        /*
         * This needs some explanation: The bit-field contains 4-bits, one for each direction that a frame may be drawn.
         * Converted to a number, the bit-field is then used as an index into the list of frame textures, which have
         * been created in such a way that their filenames indicate, in which directions they contain borders. i.e.
         * bitmask = 0101 means a border should be drawn up and down (in terms of u,v space). Converted to a number,
         * this bitmask is 5. So the texture at index 5 is used. That texture had "0101" in its filename to indicate
         * this.
         */
        final int edgeBitmask = makeBitmask(glassState, side);
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
    private static int makeBitmask(GlassState state, net.minecraft.core.Direction side) {
        switch (side) {
            case DOWN:
                return makeBitmask(state, net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.EAST, Direction.NORTH, net.minecraft.core.Direction.WEST);
            case UP:
                return makeBitmask(state, net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.WEST, net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.EAST);
            case NORTH:
                return makeBitmask(state, net.minecraft.core.Direction.UP, net.minecraft.core.Direction.WEST, net.minecraft.core.Direction.DOWN, net.minecraft.core.Direction.EAST);
            case SOUTH:
                return makeBitmask(state, net.minecraft.core.Direction.UP, net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.DOWN, net.minecraft.core.Direction.WEST);
            case WEST:
                return makeBitmask(state, Direction.UP, net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.DOWN, net.minecraft.core.Direction.NORTH);
            case EAST:
                return makeBitmask(state, net.minecraft.core.Direction.UP, net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.DOWN, net.minecraft.core.Direction.SOUTH);
            default:
                throw new IllegalArgumentException("Unsupported side!");
        }
    }

    private static int makeBitmask(GlassState state, net.minecraft.core.Direction up, net.minecraft.core.Direction right, net.minecraft.core.Direction down, net.minecraft.core.Direction left) {

        int bitmask = 0;

        if (!state.isFlushWith(up)) {
            bitmask |= 1;
        }
        if (!state.isFlushWith(right)) {
            bitmask |= 2;
        }
        if (!state.isFlushWith(down)) {
            bitmask |= 4;
        }
        if (!state.isFlushWith(left)) {
            bitmask |= 8;
        }
        return bitmask;
    }

    private BakedQuad createQuad(net.minecraft.core.Direction side, List<Vec3> corners, TextureAtlasSprite sprite, float uOffset,
                                 float vOffset) {
        return this.createQuad(side, corners.get(0), corners.get(1), corners.get(2), corners.get(3), sprite, uOffset,
                vOffset);
    }

    private BakedQuad createQuad(net.minecraft.core.Direction side, Vec3 c1, Vec3 c2, Vec3 c3, Vec3 c4,
                                 TextureAtlasSprite sprite, float uOffset, float vOffset) {
        Vec3 normal = new Vec3(side.getNormal().getX(), side.getNormal().getY(),
                side.getNormal().getZ());

        // Apply the u,v shift.
        // This mirrors the logic from OffsetIcon from 1.7
        float u1 = Mth.clamp(0 - uOffset, 0, 16);
        float u2 = Mth.clamp(16 - uOffset, 0, 16);
        float v1 = Mth.clamp(0 - vOffset, 0, 16);
        float v2 = Mth.clamp(16 - vOffset, 0, 16);

        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(side);
        this.putVertex(builder, normal, c1.x, c1.y, c1.z, sprite, u1, v1);
        this.putVertex(builder, normal, c2.x, c2.y, c2.z, sprite, u1, v2);
        this.putVertex(builder, normal, c3.x, c3.y, c3.z, sprite, u2, v2);
        this.putVertex(builder, normal, c4.x, c4.y, c4.z, sprite, u2, v1);
        return builder.build();
    }

    /*
     * This method is as complicated as it is, because the order in which we push data into the vertexbuffer actually
     * has to be precisely the order in which the vertex elements had been declared in the vertex format.
     */
    private void putVertex(BakedQuadBuilder builder, Vec3 normal, double x, double y, double z,
                           TextureAtlasSprite sprite, float u, float v) {
        VertexFormat vertexFormat = builder.getVertexFormat();
        for (int e = 0; e < vertexFormat.getElements().size(); e++) {
            VertexFormatElement el = vertexFormat.getElements().get(e);
            switch (el.getUsage()) {
                case POSITION:
                    builder.put(e, (float) x, (float) y, (float) z, 1.0f);
                    break;
                case COLOR:
                    builder.put(e, 1.0f, 1.0f, 1.0f, 1.0f);
                    break;
                case NORMAL:
                    builder.put(e, (float) normal.x, (float) normal.y, (float) normal.z, 0f);
                    break;
                case UV:
                    if (el.getIndex() == 0) {
                        u = sprite.getU(u);
                        v = sprite.getV(v);
                        builder.put(e, u, v, 0f, 1f);
                        break;
                    }
                    // Important: Fall through for getIndex() != 0
                default:
                    builder.put(e);
                    break;
            }
        }
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

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull net.minecraft.core.BlockPos pos, @Nonnull net.minecraft.world.level.block.state.BlockState state,
                                   @Nonnull IModelData tileData) {

        EnumSet<net.minecraft.core.Direction> flushWith = EnumSet.noneOf(net.minecraft.core.Direction.class);
        // Test every direction for another glass block
        for (net.minecraft.core.Direction facing : net.minecraft.core.Direction.values()) {
            if (isGlassBlock(world, pos, facing)) {
                flushWith.add(facing);
            }
        }

        GlassState glassState = new GlassState(pos.getX(), pos.getY(), pos.getZ(), flushWith);
        return new ModelDataMap.Builder().withInitial(GLASS_STATE, glassState).build();

    }

    private static boolean isGlassBlock(BlockGetter world, net.minecraft.core.BlockPos pos, net.minecraft.core.Direction facing) {
        return world.getBlockState(pos.relative(facing)).getBlock() instanceof QuartzGlassBlock;
    }

}
