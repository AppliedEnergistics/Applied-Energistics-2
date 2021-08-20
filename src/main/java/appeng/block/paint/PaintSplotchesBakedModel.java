/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.block.paint;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AppEng;
import appeng.helpers.Splotch;

/**
 * Renders paint blocks, which render multiple "splotches" that have been applied to the sides of adjacent blocks using
 * a matter cannon with paint balls.
 */
class PaintSplotchesBakedModel implements BakedModel, FabricBakedModel {

    private static final Material TEXTURE_PAINT1 = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation(AppEng.MOD_ID, "block/paint1"));
    private static final Material TEXTURE_PAINT2 = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation(AppEng.MOD_ID, "block/paint2"));
    private static final Material TEXTURE_PAINT3 = new Material(TextureAtlas.LOCATION_BLOCKS,
            new ResourceLocation(AppEng.MOD_ID, "block/paint3"));

    private final TextureAtlasSprite[] textures;

    PaintSplotchesBakedModel(Function<Material, TextureAtlasSprite> bakedTextureGetter) {
        this.textures = new TextureAtlasSprite[] { bakedTextureGetter.apply(TEXTURE_PAINT1),
                bakedTextureGetter.apply(TEXTURE_PAINT2), bakedTextureGetter.apply(TEXTURE_PAINT3) };
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
            Supplier<Random> randomSupplier, RenderContext context) {

        Object renderAttachment = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
        if (!(renderAttachment instanceof PaintSplotches)) {
            return;
        }
        PaintSplotches splotchesState = (PaintSplotches) renderAttachment;

        List<Splotch> splotches = splotchesState.getSplotches();

        CubeBuilder builder = new CubeBuilder(context.getEmitter());

        float offsetConstant = 0.001f;
        for (final Splotch s : splotches) {

            if (s.isLumen()) {
                builder.setColorRGB(s.getColor().whiteVariant);
                builder.setEmissiveMaterial(true);
            } else {
                builder.setColorRGB(s.getColor().mediumVariant);
                builder.setEmissiveMaterial(false);
            }

            float offset = offsetConstant;
            offsetConstant += 0.001f;

            final float buffer = 0.1f;

            float pos_x = s.x();
            float pos_y = s.y();

            pos_x = Math.max(buffer, Math.min(1.0f - buffer, pos_x));
            pos_y = Math.max(buffer, Math.min(1.0f - buffer, pos_y));

            TextureAtlasSprite ico = this.textures[s.getSeed() % this.textures.length];
            builder.setTexture(ico);
            builder.setCustomUv(s.getSide().getOpposite(), 0, 0, 16, 16);

            switch (s.getSide()) {
                case UP:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.DOWN, pos_x - buffer, offset, pos_y - buffer, pos_x + buffer, offset,
                            pos_y + buffer);
                    break;

                case DOWN:
                    builder.addQuad(Direction.UP, pos_x - buffer, offset, pos_y - buffer, pos_x + buffer, offset,
                            pos_y + buffer);
                    break;

                case EAST:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.WEST, offset, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer);
                    break;

                case WEST:
                    builder.addQuad(Direction.EAST, offset, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer);
                    break;

                case SOUTH:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.NORTH, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer, offset);
                    break;

                case NORTH:
                    builder.addQuad(Direction.SOUTH, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer, offset);
                    break;

                default:
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
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.textures[0];
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    static List<Material> getRequiredTextures() {
        return ImmutableList.of(TEXTURE_PAINT1, TEXTURE_PAINT2, TEXTURE_PAINT3);
    }
}
