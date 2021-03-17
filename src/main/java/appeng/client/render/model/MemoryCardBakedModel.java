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

package appeng.client.render.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AELog;

class MemoryCardBakedModel implements IBakedModel {
    private static final AEColor[] DEFAULT_COLOR_CODE = new AEColor[] { AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, };

    private final IBakedModel baseModel;

    private final TextureAtlasSprite texture;

    private final AEColor[] colorCode;

    private final Cache<CacheKey, MemoryCardBakedModel> modelCache;

    private final ImmutableList<BakedQuad> generalQuads;

    MemoryCardBakedModel(IBakedModel baseModel, TextureAtlasSprite texture) {
        this(baseModel, texture, DEFAULT_COLOR_CODE, createCache());
    }

    private MemoryCardBakedModel(IBakedModel baseModel, TextureAtlasSprite texture, AEColor[] hash,
            Cache<CacheKey, MemoryCardBakedModel> modelCache) {
        this.baseModel = baseModel;
        this.texture = texture;
        this.colorCode = hash;
        this.generalQuads = ImmutableList.copyOf(this.buildGeneralQuads());
        this.modelCache = modelCache;
    }

    private static Cache<CacheKey, MemoryCardBakedModel> createCache() {
        return CacheBuilder.newBuilder().maximumSize(100).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {

        List<BakedQuad> quads = this.baseModel.getQuads(state, side, rand, EmptyModelData.INSTANCE);

        if (side != null) {
            return quads;
        }

        List<BakedQuad> result = new ArrayList<>(quads.size() + this.generalQuads.size());
        result.addAll(quads);
        result.addAll(this.generalQuads);
        return result;
    }

    private List<BakedQuad> buildGeneralQuads() {
        CubeBuilder builder = new CubeBuilder();

        builder.setTexture(this.texture);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 2; y++) {
                final AEColor color = this.colorCode[x + y * 4];

                builder.setColorRGB(color.mediumVariant);
                builder.addCube(7 + x, 8 + (1 - y), 7.5f, 7 + x + 1, 8 + (1 - y) + 1, 8.5f);
            }
        }

        return builder.getOutput();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return this.baseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    @Override
    public boolean isSideLit() {
        return false;// TODO
    }

    @Override
    public boolean isBuiltInRenderer() {
        return this.baseModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.baseModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.baseModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ItemOverrideList() {
            @Override
            public IBakedModel getOverrideModel(IBakedModel originalModel, ItemStack stack, ClientWorld world,
                    LivingEntity entity) {
                try {
                    if (stack.getItem() instanceof IMemoryCard) {
                        final IMemoryCard memoryCard = (IMemoryCard) stack.getItem();
                        final AEColor[] colors = memoryCard.getColorCode(stack);

                        return MemoryCardBakedModel.this.modelCache.get(new CacheKey(colors),
                                () -> new MemoryCardBakedModel(MemoryCardBakedModel.this.baseModel,
                                        MemoryCardBakedModel.this.texture, colors,
                                        MemoryCardBakedModel.this.modelCache));
                    }
                } catch (ExecutionException e) {
                    AELog.error(e);
                }

                return MemoryCardBakedModel.this;
            }
        };
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
        baseModel.handlePerspective(cameraTransformType, mat);
        return this;
    }

    private static class CacheKey {
        private final AEColor[] key;

        CacheKey(AEColor[] key) {
            this.key = key;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.key);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            return Arrays.equals(this.key, other.key);
        }
    }
}
