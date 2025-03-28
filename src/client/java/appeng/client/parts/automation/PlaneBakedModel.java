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

package appeng.client.parts.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import appeng.block.paint.PlaneBakedModel;
import appeng.parts.automation.PlaneConnections;
import appeng.parts.automation.PlaneModelData;
import com.google.common.collect.ImmutableList;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import appeng.client.render.cablebus.CubeBuilder;

/**
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneBakedModel implements DynamicBlockStateModel {

    private static final PlaneConnections DEFAULT_PERMUTATION = PlaneConnections.of(false, false, false, false);

    private final TextureAtlasSprite frontTexture;

    private final Map<PlaneConnections, List<BakedQuad>> quads;

    private final ItemTransforms transforms;

    PlaneBakedModel(TextureAtlasSprite frontTexture, TextureAtlasSprite sidesTexture, TextureAtlasSprite backTexture,
            ItemTransforms transforms) {
        this.frontTexture = frontTexture;
        this.transforms = transforms;

        quads = new HashMap<>(PlaneConnections.PERMUTATIONS.size());
        // Create all possible permutations (16)
        for (PlaneConnections permutation : PlaneConnections.PERMUTATIONS) {
            List<BakedQuad> quads = new ArrayList<>(4 * 6);

            CubeBuilder builder = new CubeBuilder(quads);

            builder.setTextures(sidesTexture, sidesTexture, frontTexture, backTexture, sidesTexture, sidesTexture);

            // Keep the orientation of the X axis in mind here. When looking at a quad
            // facing north from the front,
            // The X-axis points left
            int minX = permutation.isRight() ? 0 : 1;
            int maxX = permutation.isLeft() ? 16 : 15;
            int minY = permutation.isDown() ? 0 : 1;
            int maxY = permutation.isUp() ? 16 : 15;

            builder.addCube(minX, minY, 0, maxX, maxY, 1);

            this.quads.put(permutation, ImmutableList.copyOf(quads));
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData modelData, RenderType renderType) {
        if (side == null) {
            PlaneConnections connections = DEFAULT_PERMUTATION;
            if (modelData.has(PlaneModelData.CONNECTIONS)) {
                connections = modelData.get(PlaneModelData.CONNECTIONS);
            }
            return this.quads.get(connections);
        } else {
            return Collections.emptyList();
        }
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
    public boolean usesBlockLight() {
        return false;// TODO
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.frontTexture;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final MapCodec<PlaneBakedModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group()
                .apply(instance, PlaneBakedModel.Unbaked::new));

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new PlaneBakedModel();
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }

        @Override
        public MapCodec<PlaneBakedModel.Unbaked> codec() {
            return MAP_CODEC;
        }
    }    
}
