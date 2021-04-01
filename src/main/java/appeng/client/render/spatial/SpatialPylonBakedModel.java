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
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.tile.spatial.SpatialPylonBlockEntity;

/**
 * The baked model that will be used for rendering the spatial pylon.
 */
class SpatialPylonBakedModel implements IBakedModel, FabricBakedModel {

    private final Map<SpatialPylonTextureType, TextureAtlasSprite> textures;

    SpatialPylonBakedModel(Map<SpatialPylonTextureType, TextureAtlasSprite> textures) {
        this.textures = ImmutableMap.copyOf(textures);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(IBlockDisplayReader blockView, BlockState state, BlockPos pos,
            Supplier<Random> randomSupplier, RenderContext context) {
        int flags = getFlags(blockView, pos);

        CubeBuilder builder = new CubeBuilder(context.getEmitter());

        if (flags != 0) {
            Direction ori = null;
            int displayAxis = flags & SpatialPylonBlockEntity.DISPLAY_Z;
            if (displayAxis == SpatialPylonBlockEntity.DISPLAY_X) {
                ori = Direction.field_11034;

                if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MAX) {
                    builder.setUvRotation(Direction.field_11035, 1);
                    builder.setUvRotation(Direction.field_11043, 1);
                    builder.setUvRotation(Direction.field_11036, 2);
                    builder.setUvRotation(Direction.field_11033, 2);
                } else if ((flags
                        & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MIN) {
                    builder.setUvRotation(Direction.field_11035, 2);
                    builder.setUvRotation(Direction.field_11043, 2);
                    builder.setUvRotation(Direction.field_11036, 1);
                    builder.setUvRotation(Direction.field_11033, 1);
                } else {
                    builder.setUvRotation(Direction.field_11035, 1);
                    builder.setUvRotation(Direction.field_11043, 1);
                    builder.setUvRotation(Direction.field_11036, 1);
                    builder.setUvRotation(Direction.field_11033, 1);
                }
            } else if (displayAxis == SpatialPylonBlockEntity.DISPLAY_Y) {
                ori = Direction.field_11036;
                if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MAX) {
                    builder.setUvRotation(Direction.field_11043, 3);
                    builder.setUvRotation(Direction.field_11035, 3);
                    builder.setUvRotation(Direction.field_11034, 3);
                    builder.setUvRotation(Direction.field_11039, 3);
                }
            } else if (displayAxis == SpatialPylonBlockEntity.DISPLAY_Z) {
                ori = Direction.field_11043;
                if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MAX) {
                    builder.setUvRotation(Direction.field_11034, 2);
                    builder.setUvRotation(Direction.field_11039, 1);
                } else if ((flags
                        & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MIN) {
                    builder.setUvRotation(Direction.field_11034, 1);
                    builder.setUvRotation(Direction.field_11039, 2);
                    builder.setUvRotation(Direction.field_11036, 3);
                    builder.setUvRotation(Direction.field_11033, 3);
                } else {
                    builder.setUvRotation(Direction.field_11034, 1);
                    builder.setUvRotation(Direction.field_11039, 2);
                }
            }

            builder.setTextures(this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.field_11036)),
                    this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.field_11033)),
                    this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.field_11043)),
                    this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.field_11035)),
                    this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.field_11034)),
                    this.textures.get(getTextureTypeFromSideOutside(flags, ori, Direction.field_11039)));
            builder.addCube(0, 0, 0, 16, 16, 16);

            if ((flags
                    & SpatialPylonBlockEntity.DISPLAY_POWERED_ENABLED) == SpatialPylonBlockEntity.DISPLAY_POWERED_ENABLED) {
                builder.setEmissiveMaterial(true);
            }

            builder.setTextures(this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.field_11036)),
                    this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.field_11033)),
                    this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.field_11043)),
                    this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.field_11035)),
                    this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.field_11034)),
                    this.textures.get(getTextureTypeFromSideInside(flags, ori, Direction.field_11039)));
            builder.addCube(0, 0, 0, 16, 16, 16);
        } else {
            builder.setTexture(this.textures.get(SpatialPylonTextureType.BASE));
            builder.addCube(0, 0, 0, 16, 16, 16);

            builder.setTexture(this.textures.get(SpatialPylonTextureType.DIM));
            builder.addCube(0, 0, 0, 16, 16, 16);
        }

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

    private int getFlags(IBlockDisplayReader blockRenderView, BlockPos pos) {
        if (!(blockRenderView instanceof RenderAttachedBlockView)) {
            return 0;
        }

        Object attachment = ((RenderAttachedBlockView) blockRenderView).getBlockEntityRenderAttachment(pos);
        if (!(attachment instanceof Integer)) {
            return 0;
        }

        return (Integer) attachment;
    }

    private static SpatialPylonTextureType getTextureTypeFromSideOutside(int flags, Direction ori, Direction dir) {
        if (ori == dir || ori.getOpposite() == dir) {
            return SpatialPylonTextureType.BASE;
        }

        if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_MIDDLE) {
            return SpatialPylonTextureType.BASE_SPANNED;
        } else if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MIN) {
            return SpatialPylonTextureType.BASE_END;
        } else if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MAX) {
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
        } else if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MIN) {
            return good ? SpatialPylonTextureType.DIM_END : SpatialPylonTextureType.RED_END;
        } else if ((flags & SpatialPylonBlockEntity.DISPLAY_MIDDLE) == SpatialPylonBlockEntity.DISPLAY_END_MAX) {
            return good ? SpatialPylonTextureType.DIM_END : SpatialPylonTextureType.RED_END;
        }

        return SpatialPylonTextureType.BASE;
    }

    @Override
    public boolean isSideLit() {
        return false;
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
        return this.textures.get(SpatialPylonTextureType.DIM);
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}
