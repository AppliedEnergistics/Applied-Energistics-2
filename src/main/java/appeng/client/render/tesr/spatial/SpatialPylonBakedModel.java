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

package appeng.client.render.tesr.spatial;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import org.jetbrains.annotations.Nullable;

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
import net.minecraft.util.RandomSource;
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
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState blockState, BlockPos pos,
            Supplier<RandomSource> randomSupplier, RenderContext context) {
        var state = getState(blockView, pos);

        CubeBuilder builder = new CubeBuilder(context.getEmitter());

        if (state.axisPosition() != SpatialPylonBlockEntity.AxisPosition.NONE) {
            Direction ori = null;
            var displayAxis = state.axis();
            var axisPos = state.axisPosition();
            if (displayAxis == Direction.Axis.X) {
                ori = Direction.EAST;

                if (axisPos == SpatialPylonBlockEntity.AxisPosition.END) {
                    builder.setUvRotation(Direction.SOUTH, 1);
                    builder.setUvRotation(Direction.NORTH, 1);
                    builder.setUvRotation(Direction.UP, 2);
                    builder.setUvRotation(Direction.DOWN, 2);
                } else {
                    if (axisPos == SpatialPylonBlockEntity.AxisPosition.START) {
                        builder.setUvRotation(Direction.SOUTH, 2);
                        builder.setUvRotation(Direction.NORTH, 2);
                    } else {
                        builder.setUvRotation(Direction.SOUTH, 1);
                        builder.setUvRotation(Direction.NORTH, 1);
                    }
                    builder.setUvRotation(Direction.UP, 1);
                    builder.setUvRotation(Direction.DOWN, 1);
                }
            } else if (displayAxis == Direction.Axis.Y) {
                ori = Direction.UP;
                if (axisPos == SpatialPylonBlockEntity.AxisPosition.END) {
                    builder.setUvRotation(Direction.NORTH, 3);
                    builder.setUvRotation(Direction.SOUTH, 3);
                    builder.setUvRotation(Direction.EAST, 3);
                    builder.setUvRotation(Direction.WEST, 3);
                }
            } else if (displayAxis == Direction.Axis.Z) {
                ori = Direction.NORTH;
                if (axisPos == SpatialPylonBlockEntity.AxisPosition.END) {
                    builder.setUvRotation(Direction.EAST, 2);
                    builder.setUvRotation(Direction.WEST, 1);
                } else if (axisPos == SpatialPylonBlockEntity.AxisPosition.START) {
                    builder.setUvRotation(Direction.EAST, 1);
                    builder.setUvRotation(Direction.WEST, 2);
                    builder.setUvRotation(Direction.UP, 3);
                    builder.setUvRotation(Direction.DOWN, 3);
                } else {
                    builder.setUvRotation(Direction.EAST, 1);
                    builder.setUvRotation(Direction.WEST, 2);
                }
            }

            builder.setTextures(this.textures.get(getTextureTypeFromSideOutside(state, ori, Direction.UP)),
                    this.textures.get(getTextureTypeFromSideOutside(state, ori, Direction.DOWN)),
                    this.textures.get(getTextureTypeFromSideOutside(state, ori, Direction.NORTH)),
                    this.textures.get(getTextureTypeFromSideOutside(state, ori, Direction.SOUTH)),
                    this.textures.get(getTextureTypeFromSideOutside(state, ori, Direction.EAST)),
                    this.textures.get(getTextureTypeFromSideOutside(state, ori, Direction.WEST)));
            builder.addCube(0, 0, 0, 16, 16, 16);

            if (state.powered()) {
                builder.setEmissiveMaterial(true);
            }

            builder.setTextures(this.textures.get(getTextureTypeFromSideInside(state, ori, Direction.UP)),
                    this.textures.get(getTextureTypeFromSideInside(state, ori, Direction.DOWN)),
                    this.textures.get(getTextureTypeFromSideInside(state, ori, Direction.NORTH)),
                    this.textures.get(getTextureTypeFromSideInside(state, ori, Direction.SOUTH)),
                    this.textures.get(getTextureTypeFromSideInside(state, ori, Direction.EAST)),
                    this.textures.get(getTextureTypeFromSideInside(state, ori, Direction.WEST)));
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
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        // Not intended to be used as an item model.
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        // Can only sensibly render using the new API
        return Collections.emptyList();
    }

    private SpatialPylonBlockEntity.ClientState getState(BlockAndTintGetter blockRenderView, BlockPos pos) {
        if (blockRenderView instanceof RenderAttachedBlockView renderAttachedBlockView) {
            Object attachment = renderAttachedBlockView.getBlockEntityRenderAttachment(pos);
            if (attachment instanceof SpatialPylonBlockEntity.ClientState state) {
                return state;
            }
        }
        return SpatialPylonBlockEntity.ClientState.DEFAULT;
    }

    private static SpatialPylonTextureType getTextureTypeFromSideOutside(SpatialPylonBlockEntity.ClientState state,
            Direction ori, Direction dir) {
        if (ori == dir || ori.getOpposite() == dir) {
            return SpatialPylonTextureType.BASE;
        }

        if (state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.MIDDLE) {
            return SpatialPylonTextureType.BASE_SPANNED;
        } else if (state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.START
                || state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.END) {
            return SpatialPylonTextureType.BASE_END;
        }

        return SpatialPylonTextureType.BASE;
    }

    private static SpatialPylonTextureType getTextureTypeFromSideInside(SpatialPylonBlockEntity.ClientState state,
            Direction ori, Direction dir) {
        final boolean good = state.online();

        if (ori == dir || ori.getOpposite() == dir) {
            return good ? SpatialPylonTextureType.DIM : SpatialPylonTextureType.RED;
        }

        if (state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.MIDDLE) {
            return good ? SpatialPylonTextureType.DIM_SPANNED : SpatialPylonTextureType.RED_SPANNED;
        } else if (state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.START
                || state.axisPosition() == SpatialPylonBlockEntity.AxisPosition.END) {
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
