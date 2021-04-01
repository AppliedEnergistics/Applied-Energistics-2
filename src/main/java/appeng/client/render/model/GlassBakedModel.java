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
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import appeng.decorative.solid.GlassState;
import appeng.decorative.solid.QuartzGlassBlock;

class GlassBakedModel implements IBakedModel, FabricBakedModel {

    private static final byte[][][] OFFSETS = generateOffsets();

    // Alternating textures based on position
    static final net.minecraft.client.renderer.model.RenderMaterial TEXTURE_A = new net.minecraft.client.renderer.model.RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation("appliedenergistics2:block/glass/quartz_glass_a"));
    static final net.minecraft.client.renderer.model.RenderMaterial TEXTURE_B = new net.minecraft.client.renderer.model.RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation("appliedenergistics2:block/glass/quartz_glass_b"));
    static final net.minecraft.client.renderer.model.RenderMaterial TEXTURE_C = new net.minecraft.client.renderer.model.RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation("appliedenergistics2:block/glass/quartz_glass_c"));
    static final net.minecraft.client.renderer.model.RenderMaterial TEXTURE_D = new net.minecraft.client.renderer.model.RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation("appliedenergistics2:block/glass/quartz_glass_d"));

    // Frame texture
    static final net.minecraft.client.renderer.model.RenderMaterial[] TEXTURES_FRAME = generateTexturesFrame();
    private final RenderMaterial material = RendererAccess.INSTANCE.getRenderer().materialFinder()
            .disableDiffuse(0, true).disableAo(0, true).disableColorIndex(0, true).blendMode(0, BlendMode.TRANSLUCENT)
            .find();

    // Generates the required textures for the frame
    private static net.minecraft.client.renderer.model.RenderMaterial[] generateTexturesFrame() {
        return IntStream.range(1, 16).mapToObj(Integer::toBinaryString).map(s -> Strings.padStart(s, 4, '0'))
                .map(s -> new ResourceLocation("appliedenergistics2:block/glass/quartz_glass_frame" + s))
                .map(rl -> new net.minecraft.client.renderer.model.RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, rl))
                .toArray(net.minecraft.client.renderer.model.RenderMaterial[]::new);
    }

    private final TextureAtlasSprite[] glassTextures;

    private final TextureAtlasSprite[] frameTextures;

    public GlassBakedModel(Function<net.minecraft.client.renderer.model.RenderMaterial, TextureAtlasSprite> bakedTextureGetter) {
        this.glassTextures = new TextureAtlasSprite[] { bakedTextureGetter.apply(TEXTURE_A), bakedTextureGetter.apply(TEXTURE_B),
                bakedTextureGetter.apply(TEXTURE_C), bakedTextureGetter.apply(TEXTURE_D) };

        // The first frame texture would be empty, so we simply leave it set to null
        // here
        this.frameTextures = new TextureAtlasSprite[16];
        for (int i = 0; i < TEXTURES_FRAME.length; i++) {
            this.frameTextures[1 + i] = bakedTextureGetter.apply(TEXTURES_FRAME[i]);
        }
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(IBlockDisplayReader blockView, BlockState state, BlockPos pos,
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

        final TextureAtlasSprite glassTexture = this.glassTextures[texIdx];

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
            final TextureAtlasSprite sideSprite = this.frameTextures[edgeBitmask];

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
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
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
            case field_11033:
                return makeBitmask(state, Direction.field_11035, Direction.field_11034, Direction.field_11043, Direction.field_11039);
            case field_11036:
                return makeBitmask(state, Direction.field_11035, Direction.field_11039, Direction.field_11043, Direction.field_11034);
            case field_11043:
                return makeBitmask(state, Direction.field_11036, Direction.field_11039, Direction.field_11033, Direction.field_11034);
            case field_11035:
                return makeBitmask(state, Direction.field_11036, Direction.field_11034, Direction.field_11033, Direction.field_11039);
            case field_11039:
                return makeBitmask(state, Direction.field_11036, Direction.field_11035, Direction.field_11033, Direction.field_11043);
            case field_11034:
                return makeBitmask(state, Direction.field_11036, Direction.field_11043, Direction.field_11033, Direction.field_11035);
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

    private void emitQuad(QuadEmitter emitter, Direction side, List<Vector3f> corners, TextureAtlasSprite sprite, float uOffset,
            float vOffset) {
        this.emitQuad(emitter, side, corners.get(0), corners.get(1), corners.get(2), corners.get(3), sprite, uOffset,
                vOffset);
    }

    private void emitQuad(QuadEmitter emitter, Direction side, Vector3f c1, Vector3f c2, Vector3f c3, Vector3f c4,
            TextureAtlasSprite sprite, float uOffset, float vOffset) {

        // Apply the u,v shift.
        // This mirrors the logic from OffsetIcon from 1.7
        float u1 = sprite.getInterpolatedU(MathHelper.clamp(0 - uOffset, 0, 16));
        float u2 = sprite.getInterpolatedU(MathHelper.clamp(16 - uOffset, 0, 16));
        float v1 = sprite.getInterpolatedV(MathHelper.clamp(0 - vOffset, 0, 16));
        float v2 = sprite.getInterpolatedV(MathHelper.clamp(16 - vOffset, 0, 16));

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
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

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
    private static GlassState getGlassState(IBlockDisplayReader world, BlockPos pos) {
        EnumSet<Direction> flushWith = EnumSet.noneOf(Direction.class);
        // Test every direction for another glass block
        for (Direction facing : Direction.values()) {
            if (isGlassBlock(world, pos, facing)) {
                flushWith.add(facing);
            }
        }

        return new GlassState(pos.getX(), pos.getY(), pos.getZ(), flushWith);
    }

    private static boolean isGlassBlock(IBlockReader world, BlockPos pos, Direction facing) {
        return world.getBlockState(pos.offset(facing)).getBlock() instanceof QuartzGlassBlock;
    }

}
