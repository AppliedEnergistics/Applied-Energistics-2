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


import appeng.block.AEBaseTileBlock;
import appeng.client.render.FacingToRotation;
import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;


public class AutoRotatingModel implements IBakedModel {

    private final IBakedModel parent;
    private final LoadingCache<AutoRotatingCacheKey, List<BakedQuad>> quadCache;

    public AutoRotatingModel(IBakedModel parent) {
        this.parent = parent;
        // 6 (DUNSWE) * 6 (DUNSWE) * 7 (DUNSWE + null) = 252
        this.quadCache = CacheBuilder.newBuilder().maximumSize(252).build(new CacheLoader<AutoRotatingCacheKey, List<BakedQuad>>() {
            @Override
            public List<BakedQuad> load(AutoRotatingCacheKey key) throws Exception {
                return AutoRotatingModel.this.getRotatedModel(key.getBlockState(), key.getSide(), key.getForward(), key.getUp());
            }
        });
    }

    private List<BakedQuad> getRotatedModel(IBlockState state, EnumFacing side, EnumFacing forward, EnumFacing up) {
        FacingToRotation f2r = FacingToRotation.get(forward, up);
        List<BakedQuad> original = AutoRotatingModel.this.parent.getQuads(state, f2r.resultingRotate(side), 0);
        List<BakedQuad> rotated = new ArrayList<>(original.size());
        for (BakedQuad quad : original) {
            VertexFormat format = quad.getFormat();
            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
            VertexRotator rot = new VertexRotator(f2r, quad.getFace());
            rot.setParent(builder);
            quad.pipe(rot);
            if (quad.getFace() != null) {
                builder.setQuadOrientation(f2r.rotate(quad.getFace()));
            } else {
                builder.setQuadOrientation(null);

            }
            BakedQuad unpackedQuad = builder.build();

            // Make a copy of it to resolve the vertex data and throw away the unpacked stuff
            // This also fixes a bug in Forge's UnpackedBakedQuad, which unpacks a byte-based normal like 0,0,-1
            // to 0,0,-0.99607843. We replace these normals with the proper 0,0,-1 when rotation, which
            // causes a bug in the AO lighter, if an unpacked quad pipes this value back to it.
            // Packing it back to the vanilla vertex format will fix this inconsistency because it converts
            // the normal back to a byte-based format, which then re-applies Forge's own bug when piping it
            // to the AO lighter, thus fixing our problem.
            BakedQuad packedQuad = new BakedQuad(unpackedQuad.getVertexData(), quad.getTintIndex(), unpackedQuad.getFace(), quad.getSprite(), quad
                    .shouldApplyDiffuseLighting(), quad.getFormat());
            rotated.add(packedQuad);
        }
        return rotated;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return this.parent.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.parent.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return this.parent.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.parent.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.parent.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.parent.getOverrides();
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (!(state instanceof IExtendedBlockState)) {
            return this.parent.getQuads(state, side, rand);
        }

        IExtendedBlockState extState = (IExtendedBlockState) state;

        EnumFacing forward = extState.getValue(AEBaseTileBlock.FORWARD);
        EnumFacing up = extState.getValue(AEBaseTileBlock.UP);

        if (forward == null || up == null) {
            return this.parent.getQuads(state, side, rand);
        }

        // The model has other properties than just forward/up, so it would cause our cache to inadvertendly also cache
        // these
        // additional states, possibly leading to huge isseus if the other extended state properties do not implement
        // equals/hashCode correctly
        if (extState.getUnlistedProperties().size() != 2) {
            return this.getRotatedModel(extState, side, forward, up);
        }

        AutoRotatingCacheKey key = new AutoRotatingCacheKey(extState.getClean(), forward, up, side);

        return this.quadCache.getUnchecked(key);
    }

    public static class VertexRotator extends QuadGatheringTransformer {
        private final FacingToRotation f2r;
        private final EnumFacing face;

        public VertexRotator(FacingToRotation f2r, EnumFacing face) {
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
            int count = format.getElementCount();

            for (int v = 0; v < 4; v++) {
                for (int e = 0; e < count; e++) {
                    VertexFormatElement element = format.getElement(e);
                    if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION) {
                        this.parent.put(e, this.transform(this.quadData[e][v]));
                    } else if (element.getUsage() == VertexFormatElement.EnumUsage.NORMAL) {
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
                    Vector3f vec = new Vector3f(fs[0], fs[1], fs[2]);
                    vec.x -= 0.5f;
                    vec.y -= 0.5f;
                    vec.z -= 0.5f;
                    this.f2r.getMat().transform(vec);
                    vec.x += 0.5f;
                    vec.y += 0.5f;
                    vec.z += 0.5f;
                    return new float[]{vec.x, vec.y, vec.z
                    };
                case 4:
                    Vector4f vecc = new Vector4f(fs[0], fs[1], fs[2], fs[3]);
                    vecc.x -= 0.5f;
                    vecc.y -= 0.5f;
                    vecc.z -= 0.5f;
                    this.f2r.getMat().transform(vecc);
                    vecc.x += 0.5f;
                    vecc.y += 0.5f;
                    vecc.z += 0.5f;
                    return new float[]{vecc.x, vecc.y, vecc.z, vecc.w
                    };

                default:
                    return fs;
            }
        }

        private float[] transformNormal(float[] fs) {
            if (this.face == null) {
                switch (fs.length) {
                    case 3:
                        Vector3f vec = new Vector3f(fs);
                        this.f2r.getMat().transform(vec);
                        return new float[]{
                                vec.getX(),
                                vec.getY(),
                                vec.getZ()
                        };
                    case 4:
                        Vector4f vec4 = new Vector4f(fs);
                        this.f2r.getMat().transform(vec4);
                        return new float[]{
                                vec4.getX(),
                                vec4.getY(),
                                vec4.getZ(),
                                0
                        };

                    default:
                        return fs;
                }
            } else {
                switch (fs.length) {
                    case 3:
                        Vec3i vec = this.f2r.rotate(this.face).getDirectionVec();
                        return new float[]{
                                vec.getX(),
                                vec.getY(),
                                vec.getZ()
                        };
                    case 4:
                        Vector4f veccc = new Vector4f(fs[0], fs[1], fs[2], fs[3]);
                        Vec3i vecc = this.f2r.rotate(this.face).getDirectionVec();
                        return new float[]{
                                vecc.getX(),
                                vecc.getY(),
                                vecc.getZ(),
                                veccc.w
                        };

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
        public void setQuadOrientation(EnumFacing orientation) {
            this.parent.setQuadOrientation(orientation);
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
