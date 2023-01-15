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

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;

import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.client.render.cablebus.CubeBuilder;

/**
 * The baked model that will be used for rendering the spatial pylon.
 */
class SpatialPylonBakedModel implements IDynamicBakedModel {

    private final Map<SpatialPylonTextureType, TextureAtlasSprite> textures;

    SpatialPylonBakedModel(Map<SpatialPylonTextureType, TextureAtlasSprite> textures) {
        this.textures = ImmutableMap.copyOf(textures);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction side, RandomSource rand,
            ModelData extraData, RenderType renderType) {
        var state = getState(extraData);

        CubeBuilder builder = new CubeBuilder();

        if (state.axisPosition() != SpatialPylonBlockEntity.AxisPosition.NONE) {
            Direction ori = null;
            var displayAxis = state.axis();
            var axisPos = state.axisPosition();

            if (displayAxis == Direction.Axis.Y) {
                ori = Direction.UP;
                if (axisPos == SpatialPylonBlockEntity.AxisPosition.END) {
                    builder.setFlipV(Direction.NORTH, true);
                    builder.setFlipV(Direction.SOUTH, true);
                    builder.setFlipV(Direction.WEST, true);
                    builder.setFlipV(Direction.EAST, true);
                }
            } else if (displayAxis == Direction.Axis.X) {
                ori = Direction.EAST;

                builder.setUvRotation(Direction.NORTH, 1);
                builder.setUvRotation(Direction.SOUTH, 1);
                builder.setUvRotation(Direction.UP, 3);
                builder.setUvRotation(Direction.DOWN, 3);

                if (axisPos == SpatialPylonBlockEntity.AxisPosition.START) {
                    builder.setFlipV(Direction.UP, true);
                    builder.setFlipV(Direction.DOWN, true);
                    builder.setFlipV(Direction.NORTH, true);
                } else if (axisPos == SpatialPylonBlockEntity.AxisPosition.END) {
                    builder.setFlipV(Direction.SOUTH, true);
                }
            } else if (displayAxis == Direction.Axis.Z) {
                ori = Direction.NORTH;

                builder.setUvRotation(Direction.WEST, 1);
                builder.setUvRotation(Direction.EAST, 1);

                if (axisPos == SpatialPylonBlockEntity.AxisPosition.START) {
                    builder.setFlipV(Direction.UP, true);
                    builder.setFlipV(Direction.EAST, true);
                } else if (axisPos == SpatialPylonBlockEntity.AxisPosition.END) {
                    builder.setFlipV(Direction.DOWN, true);
                    builder.setFlipV(Direction.WEST, true);
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

        return builder.getOutput();
    }

    private SpatialPylonBlockEntity.ClientState getState(ModelData modelData) {
        var state = modelData.get(SpatialPylonBlockEntity.STATE);
        return state != null ? state : SpatialPylonBlockEntity.ClientState.DEFAULT;
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
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
