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

package appeng.client.model;

import java.util.List;

import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BlockModelPart;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import appeng.block.paint.PaintSplotches;
import appeng.blockentity.misc.PaintSplotchesBlockEntity;
import appeng.client.render.CubeBuilder;
import appeng.core.AppEng;
import appeng.helpers.Splotch;

public class PaintSplotchesModel implements DynamicBlockStateModel {
    private static final Material TEXTURE_PAINT1 = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/paint1"));
    private static final Material TEXTURE_PAINT2 = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/paint2"));
    private static final Material TEXTURE_PAINT3 = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/paint3"));

    private final TextureAtlasSprite[] textures;

    public PaintSplotchesModel(SpriteGetter sprites) {
        ModelDebugName debugName = getClass()::toString;
        this.textures = new TextureAtlasSprite[] { sprites.get(TEXTURE_PAINT1, debugName),
                sprites.get(TEXTURE_PAINT2, debugName), sprites.get(TEXTURE_PAINT3, debugName) };
    }

    @Override
    public void collectParts(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, BlockState blockState,
            RandomSource randomSource, List<BlockModelPart> list) {
        var modelData = blockAndTintGetter.getModelData(blockPos);
        var quadListBuilder = new QuadCollection.Builder();
        getQuads(quadListBuilder, modelData);
        list.add(new SimpleModelWrapper(
                quadListBuilder.build(),
                false,
                textures[0],
                ChunkSectionLayer.CUTOUT));
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return textures[0];
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
            RandomSource random) {
        return level.getModelData(pos);
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        public static final Identifier ID = AppEng.makeId("paint_splotches");

        @Override
        public MapCodec<Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public PaintSplotchesModel bake(ModelBaker baker) {
            return new PaintSplotchesModel(baker.sprites());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }
    }

    private void getQuads(QuadCollection.Builder quadListBuilder, ModelData extraData) {
        PaintSplotches splotchesState = extraData.get(PaintSplotchesBlockEntity.SPLOTCHES);

        if (splotchesState == null) {
            // This is the inventory model which should usually not be used other than in
            // special cases
            CubeBuilder builder = new CubeBuilder(quadListBuilder::addUnculledFace);
            builder.setTexture(this.textures[0]);
            builder.addCube(0, 0, 0, 16, 16, 16);
            return;
        }

        List<Splotch> splotches = splotchesState.splotches();

        CubeBuilder builder = new CubeBuilder(quadListBuilder::addUnculledFace);

        float offsetConstant = 0.001f;
        for (Splotch s : splotches) {

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
            builder.setCustomUv(s.getSide().getOpposite(), 0, 0, 1, 1);

            switch (s.getSide()) {
                case Direction.UP:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.DOWN, pos_x - buffer, offset, pos_y - buffer, pos_x + buffer, offset,
                            pos_y + buffer);
                    break;

                case Direction.DOWN:
                    builder.addQuad(Direction.UP, pos_x - buffer, offset, pos_y - buffer, pos_x + buffer, offset,
                            pos_y + buffer);
                    break;

                case Direction.EAST:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.WEST, offset, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer);
                    break;

                case Direction.WEST:
                    builder.addQuad(Direction.EAST, offset, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer);
                    break;

                case Direction.SOUTH:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.NORTH, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer, offset);
                    break;

                case Direction.NORTH:
                    builder.addQuad(Direction.SOUTH, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer, offset);
                    break;

                default:
            }
        }
    }
}
