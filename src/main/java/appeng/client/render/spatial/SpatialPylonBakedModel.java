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

package appeng.client.render.spatial;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.client.render.cablebus.CubeBuilder;

/**
 * The baked model that will be used for rendering the spatial pylon.
 */
class SpatialPylonBakedModel implements BakedModel, FabricBakedModel {

    private final Map<SpatialPylonTextureType, TextureAtlasSprite> textures;

    SpatialPylonBakedModel(Map<SpatialPylonTextureType, TextureAtlasSprite> textures) {
        this.textures = ImmutableMap.copyOf(textures);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
            Supplier<Random> randomSupplier, RenderContext context) {
        int flags = getFlags(blockView, pos);

        CubeBuilder builder = new CubeBuilder(context.getEmitter());

        if (flags != 0) {
            Direction ori = null;
            int displayAxis = flags & SpatialPylonBlockEntity.DISPLAY_Z;
            if (displayAxis == SpatialPylonBlockEntity.DISPLAY_X) {
                ori = Direction.EAST;

                if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MAX) {
                    builder.setUvRotation(Direction.SOUTH, 1);
                    builder.setUvRotation(Direction.NORTH, 1);
                    builder.setUvRotation(Direction.UP, 2);
                    builder.setUvRotation(Direction.DOWN, 2);
                } else {
                    if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MIN) {
                        builder.setUvRotation(Direction.SOUTH, 2);
                        builder.setUvRotation(Direction.NORTH, 2);
                    } else {
                        builder.setUvRotation(Direction.SOUTH, 1);
                        builder.setUvRotation(Direction.NORTH, 1);
                    }
                    builder.setUvRotation(Direction.UP, 1);
                    builder.setUvRotation(Direction.DOWN, 1);
                }
            } else if (displayAxis == SpatialPylonBlockEntity.DISPLAY_Y) {
                ori = Direction.UP;
                if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MAX) {
                    builder.setUvRotation(Direction.NORTH, 3);
                    builder.setUvRotation(Direction.SOUTH, 3);
                    builder.setUvRotation(Direction.EAST, 3);
                    builder.setUvRotation(Direction.WEST, 3);
                }
            } else if (displayAxis == SpatialPylonBlockEntity.DISPLAY_Z) {
                ori = Direction.NORTH;
                if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MAX) {
                    builder.setUvRotation(Direction.EAST, 2);
                    builder.setUvRotation(Direction.WEST, 1);
                } else if ((flags
                        & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MIN) {
                    builder.setUvRotation(Direction.EAST, 1);
                    builder.setUvRotation(Direction.WEST, 2);
                    builder.setUvRotation(Direction.UP, 3);
                    builder.setUvRotation(Direction.DOWN, 3);
                } else {
                    builder.setUvRotation(Direction.EAST, 1);
                    builder.setUvRotation(Direction.WEST, 2);
                }
            }

            builder.setTextures(this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.UP)),
                    this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.DOWN)),
                    this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.NORTH)),
                    this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.SOUTH)),
                    this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.EAST)),
                    this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.WEST)));
            builder.addCube(0, 0, 0, 16, 16, 16);

            if ((flags
                    & SpatialPylonBlockEntity.DISPLAY_POWERED_ENABLED) == SpatialPylonBlockEntity.DISPLAY_POWERED_ENABLED) {
                builder.setEmissiveMaterial(true);
            }

            builder.setTextures(this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.UP)),
                    this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.DOWN)),
                    this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.NORTH)),
                    this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.SOUTH)),
                    this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.EAST)),
                    this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.WEST)));
        } else {
            builder.setTexture(this.textures.get(SpatialPylonTextureType.BASE));
            builder.addCube(0, 0, 0, 16, 16, 16);

            builder.setTexture(this.textures.get(SpatialPylonTextureType.DIM));
        }
        builder.addCube(0, 0, 0, 16, 16, 16);

        // Reset back to default
        builder.setEmissiveMaterial(false);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        // Not intended to be used as an item model.
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
        // Can only sensibly render using the new API
        return Collections.emptyList();
    }

    private int getFlags(BlockAndTintGetter blockRenderView, BlockPos pos) {
        if (blockRenderView instanceof RenderAttachedBlockView renderAttachedBlockView) {
            Object attachment = renderAttachedBlockView.getBlockEntityRenderAttachment(pos);
            if (attachment instanceof Integer flags) {
                return flags;
            }
        }
        return 0;
    }

    private static SpatialPylonTextureType getTextureTypeFromSideOutside(int flags, Direction ori, Direction dir) {
        if (ori == dir || ori.getOpposite() == dir) {
            return SpatialPylonTextureType.BASE;
        }

        if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_MIDDLE) {
            return SpatialPylonTextureType.BASE_SPANNED;
        } else if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MIN
                || (flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MAX) {
            return SpatialPylonTextureType.BASE_END;
        }

        return SpatialPylonTextureType.BASE;
    }

    private static SpatialPylonTextureType getTextureTypeFromSideInside(int flags, Direction ori, Direction dir) {
        final boolean good = (flags
                & SpatialPylonBlockEntity.DISPLAY_ENABLED) == SpatialPylonBlockEntity.DISPLAY_ENABLED;

        if (ori == dir || ori.getOpposite() == dir) {
            return good ? SpatialPylonTextureType.DIM : SpatialPylonTextureType.RED;
        }

        if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_MIDDLE) {
            return good ? SpatialPylonTextureType.DIM_SPANNED : SpatialPylonTextureType.RED_SPANNED;
        } else if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MIN
                || (flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MAX) {
            return good ? SpatialPylonTextureType.DIM_END : SpatialPylonTextureType.RED_END;
        }

        return SpatialPylonTextureType.BASE;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
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
        return this.textures.get(SpatialPylonTextureType.DIM);
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
