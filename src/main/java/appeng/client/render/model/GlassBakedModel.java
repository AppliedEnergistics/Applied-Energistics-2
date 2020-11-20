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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Strings;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;

import appeng.decorative.solid.GlassState;
import appeng.decorative.solid.QuartzGlassBlock;

class GlassBakedModel implements BakedModel, FabricBakedModel {

    private static final byte[][][] OFFSETS = generateOffsets();

    // Alternating textures based on position
    static final SpriteIdentifier TEXTURE_A = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier("appliedenergistics2:block/glass/quartz_glass_a"));
    static final SpriteIdentifier TEXTURE_B = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier("appliedenergistics2:block/glass/quartz_glass_b"));
    static final SpriteIdentifier TEXTURE_C = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier("appliedenergistics2:block/glass/quartz_glass_c"));
    static final SpriteIdentifier TEXTURE_D = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier("appliedenergistics2:block/glass/quartz_glass_d"));

    // Frame texture
    static final SpriteIdentifier[] TEXTURES_FRAME = generateTexturesFrame();
    private final RenderMaterial material = RendererAccess.INSTANCE.getRenderer().materialFinder()
            .disableDiffuse(0, true).disableAo(0, true).disableColorIndex(0, true).blendMode(0, BlendMode.TRANSLUCENT)
            .find();

    // Generates the required textures for the frame
    private static SpriteIdentifier[] generateTexturesFrame() {
        return IntStream.range(1, 16).mapToObj(Integer::toBinaryString).map(s -> Strings.padStart(s, 4, '0'))
                .map(s -> new Identifier("appliedenergistics2:block/glass/quartz_glass_frame" + s))
                .map(rl -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, rl))
                .toArray(SpriteIdentifier[]::new);
    }

    private final Sprite[] glassTextures;

    private final Sprite[] frameTextures;

    public GlassBakedModel(Function<SpriteIdentifier, Sprite> bakedTextureGetter) {
        this.glassTextures = new Sprite[] { bakedTextureGetter.apply(TEXTURE_A), bakedTextureGetter.apply(TEXTURE_B),
                bakedTextureGetter.apply(TEXTURE_C), bakedTextureGetter.apply(TEXTURE_D) };

        // The first frame texture would be empty, so we simply leave it set to null
        // here
        this.frameTextures = new Sprite[16];
        for (int i = 0; i < TEXTURES_FRAME.length; i++) {
            this.frameTextures[1 + i] = bakedTextureGetter.apply(TEXTURES_FRAME[i]);
        }
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos,
            Supplier<Random> randomSupplier, RenderContext context) {
        final GlassState glassState = getGlassState(blockView, pos);

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

        final Sprite glassTexture = this.glassTextures[texIdx];

        QuadEmitter emitter = context.getEmitter();

        // Render the glass side
        for (Direction side : Direction.values()) {
            final List<Vector3f> corners = RenderHelper.getFaceCorners(side);
            this.emitQuad(emitter, side, corners, glassTexture, u, v);

            /*
             * This needs some explanation: The bit-field contains 4-bits, one for each direction that a frame may be
             * drawn. Converted to a number, the bit-field is then used as an index into the list of frame textures,
             * which have been created in such a way that their filenames indicate, in which directions they contain
             * borders. i.e. bitmask = 0101 means a border should be drawn up and down (in terms of u,v space).
             * Converted to a number, this bitmask is 5. So the texture at index 5 is used. That texture had "0101" in
             * its filename to indicate this.
             */
            final int edgeBitmask = makeBitmask(glassState, side);
            final Sprite sideSprite = this.frameTextures[edgeBitmask];

            if (sideSprite != null) {
                this.emitQuad(emitter, side, corners, sideSprite, 0, 0);
            }
        }

    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelTransformation.NONE;
    }

    @Override
    public boolean isSideLit() {
        return false; // Irrelvant because not used as item model
    }

    /**
     * Creates the bitmask that indicates, in which directions (in terms of u,v space) a border should be drawn.
     */
    private static int makeBitmask(GlassState state, Direction side) {
        switch (side) {
            case DOWN:
                return makeBitmask(state, Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST);
            case UP:
                return makeBitmask(state, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST);
            case NORTH:
                return makeBitmask(state, Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST);
            case SOUTH:
                return makeBitmask(state, Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST);
            case WEST:
                return makeBitmask(state, Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH);
            case EAST:
                return makeBitmask(state, Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH);
            default:
                throw new IllegalArgumentException("Unsupported side!");
        }
    }

    private static int makeBitmask(GlassState state, Direction up, Direction right, Direction down, Direction left) {

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

    private void emitQuad(QuadEmitter emitter, Direction side, List<Vector3f> corners, Sprite sprite, float uOffset,
            float vOffset) {
        this.emitQuad(emitter, side, corners.get(0), corners.get(1), corners.get(2), corners.get(3), sprite, uOffset,
                vOffset);
    }

    private void emitQuad(QuadEmitter emitter, Direction side, Vector3f c1, Vector3f c2, Vector3f c3, Vector3f c4,
            Sprite sprite, float uOffset, float vOffset) {

        // Apply the u,v shift.
        // This mirrors the logic from OffsetIcon from 1.7
        float u1 = sprite.getFrameU(MathHelper.clamp(0 - uOffset, 0, 16));
        float u2 = sprite.getFrameU(MathHelper.clamp(16 - uOffset, 0, 16));
        float v1 = sprite.getFrameV(MathHelper.clamp(0 - vOffset, 0, 16));
        float v2 = sprite.getFrameV(MathHelper.clamp(16 - vOffset, 0, 16));

        emitter.nominalFace(side);
        emitter.cullFace(side);
        emitter.material(material);
        emitter.pos(0, c1).sprite(0, 0, u1, v1);
        emitter.pos(1, c2).sprite(1, 0, u1, v2);
        emitter.pos(2, c3).sprite(2, 0, u2, v2);
        emitter.pos(3, c4).sprite(3, 0, u2, v1);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.emit();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
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
    private static GlassState getGlassState(BlockRenderView world, BlockPos pos) {
        EnumSet<Direction> flushWith = EnumSet.noneOf(Direction.class);
        // Test every direction for another glass block
        for (Direction facing : Direction.values()) {
            if (isGlassBlock(world, pos, facing)) {
                flushWith.add(facing);
            }
        }

        return new GlassState(pos.getX(), pos.getY(), pos.getZ(), flushWith);
    }

    private static boolean isGlassBlock(BlockView world, BlockPos pos, Direction facing) {
        return world.getBlockState(pos.offset(facing)).getBlock() instanceof QuartzGlassBlock;
    }

}
