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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import appeng.client.render.DelegateBakedModel;
import appeng.client.render.FacingToRotation;
import appeng.client.render.cablebus.QuadRotator;
import appeng.thirdparty.fabric.ModelHelper;
import appeng.thirdparty.fabric.QuadEmitter;
import appeng.thirdparty.fabric.Renderer;

public class AutoRotatingBakedModel extends DelegateBakedModel {

    // List of all directions and null
    private static final Direction[] CULL_FACES = Stream.concat(
            Arrays.stream(Direction.values()),
            Stream.of((Direction) null)).toArray(Direction[]::new);

    private final BakedModel parent;
    // Value is indexed by direction ordinal where index 6 is direction==null,
    // the direction of the cull-face / which is passed to getQuads
    private final LoadingCache<AutoRotatingCacheKey, List<BakedQuad>[]> quadCache;

    public AutoRotatingBakedModel(BakedModel parent) {
        super(parent);
        this.parent = parent;
        this.quadCache = CacheBuilder.newBuilder()
                // 36 variations of forward/up (some are redundant)
                .maximumSize(200)
                .build(new CacheLoader<>() {
                    @Override
                    public List<BakedQuad>[] load(AutoRotatingCacheKey key) {
                        return AutoRotatingBakedModel.this.getRotatedModel(key.getBlockState(), key.getModelData());
                    }
                });
    }

    private List<BakedQuad>[] getRotatedModel(BlockState state, ModelData modelData) {
        FacingToRotation f2r = FacingToRotation.get(AEModelData.getForward(modelData),
                AEModelData.getUp(modelData));

        var rand = RandomSource.create(0);

        // For redundant rotations, we don't actually compute a rotated version
        if (f2r.isRedundant()) {
            @SuppressWarnings("unchecked")
            List<BakedQuad>[] result = new List[7];
            for (var value : CULL_FACES) {
                int idx = value == null ? 6 : value.ordinal();
                result[idx] = parent.getQuads(state, value, rand, modelData, null);
            }
            return result;
        }

        var r = Renderer.getInstance();
        var meshBuilder = r.meshBuilder();
        var quadEmitter = meshBuilder.getEmitter();

        for (var cullFace : CULL_FACES) {
            rotateQuadsFromSide(state, cullFace, rand, modelData, f2r, quadEmitter);
        }

        return ModelHelper.toQuadLists(meshBuilder.build());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        if (state == null) {
            return getBaseModel().getQuads(state, side, rand);
        }
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData modelData, RenderType renderType) {
        if (state == null) {
            return getBaseModel().getQuads(state, side, rand, modelData, renderType);
        }

        var uncachable = modelData.get(AEModelData.SKIP_CACHE);
        if (Boolean.TRUE.equals(uncachable)) {
            var f2r = FacingToRotation.get(AEModelData.getForward(modelData),
                    AEModelData.getUp(modelData));

            var r = Renderer.getInstance();
            var meshBuilder = r.meshBuilder();
            var quadEmitter = meshBuilder.getEmitter();

            rotateQuadsFromSide(state, side, rand, modelData, f2r, quadEmitter);

            return ModelHelper.toQuadLists(meshBuilder.build())[side == null ? 6 : side.ordinal()];
        }

        var sideIdx = side == null ? 6 : side.ordinal();
        return quadCache.getUnchecked(new AutoRotatingCacheKey(state, modelData))[sideIdx];
    }

    private void rotateQuadsFromSide(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData modelData, FacingToRotation f2r, QuadEmitter quadEmitter) {
        var original = this.parent.getQuads(state, f2r.resultingRotate(side), rand,
                modelData, null);

        for (var quad : original) {
            quadEmitter.fromVanilla(quad, side);

            var quadRotator = QuadRotator.get(f2r);
            quadRotator.transform(quadEmitter);
            quadEmitter.emit();
        }
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state,
            ModelData modelData) {
        return this.parent.getModelData(level, pos, state, modelData);
    }

}
