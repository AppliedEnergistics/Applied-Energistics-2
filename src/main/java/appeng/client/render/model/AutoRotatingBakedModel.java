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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;

import appeng.client.render.DelegateBakedModel;
import appeng.client.render.FacingToRotation;

public class AutoRotatingBakedModel extends DelegateBakedModel {

    private final IBakedModel parent;
    private final LoadingCache<AutoRotatingCacheKey, List<BakedQuad>> quadCache;

    public AutoRotatingBakedModel(IBakedModel parent) {
        super(parent);
        this.parent = parent;
        // 6 (DUNSWE) * 6 (DUNSWE) * 7 (DUNSWE + null) = 252
        this.quadCache = CacheBuilder.newBuilder().maximumSize(252)
                .build(new CacheLoader<AutoRotatingCacheKey, List<BakedQuad>>() {
                    @Override
                    public List<BakedQuad> load(AutoRotatingCacheKey key) {
                        return AutoRotatingBakedModel.this.getRotatedModel(key.getBlockState(), key.getSide(),
                                new Random(0), key.getModelData());
                    }
                });
    }

    private List<BakedQuad> getRotatedModel(BlockState state, Direction side, Random rand, AEModelData modelData) {
        FacingToRotation f2r = FacingToRotation.get(modelData.getForward(), modelData.getUp());

        if (f2r.isRedundant()) {
            return AutoRotatingBakedModel.this.parent.getQuads(state, side, rand, modelData);
        }

        List<BakedQuad> original = AutoRotatingBakedModel.this.parent.getQuads(state, f2r.resultingRotate(side), rand,
                modelData);
        List<BakedQuad> rotated = new ArrayList<>(original.size());
        for (BakedQuad quad : original) {
            BakedQuadBuilder builder = new BakedQuadBuilder();
            VertexRotator rot = new VertexRotator(f2r, quad.getFace());
            rot.setParent(builder);
            quad.pipe(rot);
            if (quad.getFace() != null) {
                builder.setQuadOrientation(f2r.rotate(quad.getFace()));
            } else {
                builder.setQuadOrientation(null);

            }
            BakedQuad unpackedQuad = builder.build();

            // Make a copy of it to resolve the vertex data and throw away the unpacked
            // stuff
            // This also fixes a bug in Forge's UnpackedBakedQuad, which unpacks a
            // byte-based normal like 0,0,-1
            // to 0,0,-0.99607843. We replace these normals with the proper 0,0,-1 when
            // rotation, which
            // causes a bug in the AO lighter, if an unpacked quad pipes this value back to
            // it.
            // Packing it back to the vanilla vertex format will fix this inconsistency
            // because it converts
            // the normal back to a byte-based format, which then re-applies Forge's own bug
            // when piping it
            // to the AO lighter, thus fixing our problem.
            BakedQuad packedQuad = new BakedQuad(unpackedQuad.getVertexData(), quad.getTintIndex(),
                    unpackedQuad.getFace(), quad.getSprite(), quad.applyDiffuseLighting());
            rotated.add(packedQuad);
        }
        return rotated;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return getQuads(state, side, rand, EmptyModelData.INSTANCE);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand,
            @Nonnull IModelData extraData) {

        if (!(extraData instanceof AEModelData)) {
            return this.parent.getQuads(state, side, rand, extraData);
        }

        AEModelData aeModelData = (AEModelData) extraData;

        if (aeModelData.isCacheable()) {
            return quadCache.getUnchecked(new AutoRotatingCacheKey(state, aeModelData, side));
        } else {
            return this.getRotatedModel(state, side, rand, aeModelData);
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state,
            @Nonnull IModelData tileData) {
        return this.parent.getModelData(world, pos, state, tileData);
    }

    public static class VertexRotator extends QuadGatheringTransformer {
        private final FacingToRotation f2r;
        private final Direction face;

        public VertexRotator(FacingToRotation f2r, Direction face) {
            this.f2r = f2r;
            this.face = face;
        }

        @Override
        public void setParent(IVertexConsumer parent) {
            super.setParent(parent);
            if (Objects.equal(this.getVertexFormat(), parent.getVertexFormat())) {
                return;
            }
            this.setVertexFormat(parent.getVertexFormat());
        }

        @Override
        protected void processQuad() {
            VertexFormat format = this.parent.getVertexFormat();
            ImmutableList<VertexFormatElement> elements = format.getElements();

            for (int v = 0; v < 4; v++) {
                for (int e = 0; e < elements.size(); e++) {
                    VertexFormatElement element = elements.get(e);
                    if (element.getUsage() == VertexFormatElement.Usage.POSITION) {
                        this.parent.put(e, this.transform(this.quadData[e][v]));
                    } else if (element.getUsage() == VertexFormatElement.Usage.NORMAL) {
                        this.parent.put(e, this.transformNormal(this.quadData[e][v]));
                    } else {
                        this.parent.put(e, this.quadData[e][v]);
                    }
                }
            }
        }

        private float[] transform(float[] fs) {
            switch (fs.length) {
                case 3:
                    Vector4f vec = new Vector4f(fs[0], fs[1], fs[2], 1);
                    vec.setX(vec.getX() - 0.5f);
                    vec.setY(vec.getY() - 0.5f);
                    vec.setZ(vec.getZ() - 0.5f);
                    vec.transform(this.f2r.getMat());
                    vec.setX(vec.getX() + 0.5f);
                    vec.setY(vec.getY() + 0.5f);
                    vec.setZ(vec.getZ() + 0.5f);
                    return new float[] { snap(vec.getX()), snap(vec.getY()), snap(vec.getZ()) };
                case 4:
                    Vector4f vecc = new Vector4f(fs[0], fs[1], fs[2], fs[3]);
                    vecc.setX(vecc.getX() - 0.5f);
                    vecc.setY(vecc.getY() - 0.5f);
                    vecc.setZ(vecc.getZ() - 0.5f);
                    vecc.transform(this.f2r.getMat());
                    vecc.setX(vecc.getX() + 0.5f);
                    vecc.setY(vecc.getY() + 0.5f);
                    vecc.setZ(vecc.getZ() + 0.5f);
                    return new float[] { snap(vecc.getX()), snap(vecc.getY()), snap(vecc.getZ()), snap(vecc.getW()) };

                default:
                    return fs;
            }
        }

        /**
         * This is the same value used by Vanilla's AO lighter.
         */
        private static final float EPS = 0.0001f;

        /**
         * This tries to snap the coordinate to the edges of the block frame, because Vanilla uses direct equals
         * comparisons to 0 and 1 for checking if a face extends fully towards the edge. This is used primarily for AO
         * calculations.
         */
        private static float snap(float x) {
            if (Math.abs(x) <= EPS) {
                return 0f;
            } else if (Math.abs(x - 1) <= EPS) {
                return 1f;
            }
            return x;
        }

        private float[] transformNormal(float[] fs) {
            if (this.face == null) {
                switch (fs.length) {
                    case 3:
                        Vector4f vec = new Vector4f(fs[0], fs[1], fs[2], 0);
                        vec.transform(this.f2r.getMat());
                        return new float[] { vec.getX(), vec.getY(), vec.getZ() };
                    case 4:
                        Vector4f vec4 = new Vector4f(fs[0], fs[1], fs[2], fs[3]);
                        vec4.transform(this.f2r.getMat());
                        return new float[] { snap(vec4.getX()), snap(vec4.getY()), snap(vec4.getZ()), 0 };

                    default:
                        return fs;
                }
            } else {
                switch (fs.length) {
                    case 3:
                        Vector3i vec = this.f2r.rotate(this.face).getDirectionVec();
                        return new float[] { vec.getX(), vec.getY(), vec.getZ() };
                    case 4:
                        Vector4f veccc = new Vector4f(fs[0], fs[1], fs[2], fs[3]);
                        Vector3i vecc = this.f2r.rotate(this.face).getDirectionVec();
                        return new float[] { vecc.getX(), vecc.getY(), vecc.getZ(), veccc.getW() };

                    default:
                        return fs;
                }
            }
        }

        @Override
        public void setQuadTint(int tint) {
            this.parent.setQuadTint(tint);
        }

        @Override
        public void setQuadOrientation(Direction orientation) {
            this.parent.setQuadOrientation(f2r.rotate(orientation));
        }

        @Override
        public void setApplyDiffuseLighting(boolean diffuse) {
            this.parent.setApplyDiffuseLighting(diffuse);
        }

        @Override
        public void setTexture(TextureAtlasSprite texture) {
            this.parent.setTexture(texture);
        }
    }
}
