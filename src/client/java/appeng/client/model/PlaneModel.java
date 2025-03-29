/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import appeng.client.render.CubeBuilder;
import appeng.core.AppEng;
import appeng.parts.automation.PlaneConnections;
import appeng.parts.automation.PlaneModelData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneModel implements DynamicBlockStateModel {

    private static final PlaneConnections DEFAULT_PERMUTATION = PlaneConnections.of(false, false, false, false);

    private final Map<PlaneConnections, SimpleModelWrapper> parts;

    private final TextureAtlasSprite frontSprite;

    public PlaneModel(TextureAtlasSprite frontSprite, TextureAtlasSprite sidesSprite, TextureAtlasSprite backSprite) {
        this.frontSprite = frontSprite;

        parts = new HashMap<>(PlaneConnections.PERMUTATIONS.size());
        // Create all possible permutations (16)
        for (var permutation : PlaneConnections.PERMUTATIONS) {
            var quads = new QuadCollection.Builder();

            CubeBuilder builder = new CubeBuilder(quads::addUnculledFace);

            builder.setTextures(sidesSprite, sidesSprite, frontSprite, backSprite, sidesSprite, sidesSprite);

            // Keep the orientation of the X axis in mind here. When looking at a quad
            // facing north from the front,
            // The X-axis points left
            int minX = permutation.isRight() ? 0 : 1;
            int maxX = permutation.isLeft() ? 16 : 15;
            int minY = permutation.isDown() ? 0 : 1;
            int maxY = permutation.isUp() ? 16 : 15;

            builder.addCube(minX, minY, 0, maxX, maxY, 1);

            this.parts.put(permutation, new SimpleModelWrapper(
                    quads.build(),
                    true,
                    frontSprite,
                    RenderType.solid()
            ));
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return frontSprite;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        var modelData = level.getModelData(pos);
        var connections = DEFAULT_PERMUTATION;
        if (modelData.has(PlaneModelData.CONNECTIONS)) {
            connections = modelData.get(PlaneModelData.CONNECTIONS);
        }
        parts.add(this.parts.get(connections));
    }

    public record Unbaked(ResourceLocation frontTexture, ResourceLocation sidesTexture,
                   ResourceLocation backTexture) implements CustomUnbakedBlockStateModel {
        public static final ResourceLocation ID = AppEng.makeId("plane");

        public static MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ResourceLocation.CODEC.fieldOf("front").forGetter(Unbaked::frontTexture),
                ResourceLocation.CODEC.fieldOf("sides").forGetter(Unbaked::frontTexture),
                ResourceLocation.CODEC.fieldOf("back").forGetter(Unbaked::frontTexture)
        ).apply(builder, Unbaked::new));

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            ModelDebugName debugName = getClass()::toString;

            var frontSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, frontTexture), debugName);
            var sidesSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, sidesTexture), debugName);
            var backSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, backTexture), debugName);

            return new PlaneModel(frontSprite, sidesSprite, backSprite);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }
    }

}
