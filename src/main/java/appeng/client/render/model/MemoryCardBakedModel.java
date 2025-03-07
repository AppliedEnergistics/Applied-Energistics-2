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
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AELog;

class MemoryCardBakedModel implements BakedModel {
    private final BakedModel baseModel;

    private final TextureAtlasSprite texture;

    private final MemoryCardColors colors;

    private final Cache<MemoryCardColors, MemoryCardBakedModel> modelCache;

    private final ImmutableList<BakedQuad> generalQuads;

    MemoryCardBakedModel(BakedModel baseModel, TextureAtlasSprite texture) {
        this(baseModel, texture, MemoryCardColors.DEFAULT, createCache());
    }

    private MemoryCardBakedModel(BakedModel baseModel, TextureAtlasSprite texture, MemoryCardColors colors,
            Cache<MemoryCardColors, MemoryCardBakedModel> modelCache) {
        this.baseModel = baseModel;
        this.texture = texture;
        this.colors = colors;
        this.generalQuads = ImmutableList.copyOf(this.buildGeneralQuads());
        this.modelCache = modelCache;
    }

    private static Cache<MemoryCardColors, MemoryCardBakedModel> createCache() {
        return CacheBuilder.newBuilder().maximumSize(100).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {

        List<BakedQuad> quads = this.baseModel.getQuads(state, side, rand, ModelData.EMPTY, null);

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
                final AEColor color = this.colors.get(x, y);

                builder.setColorRGB(color.mediumVariant);
                builder.addCube(8 + x, 8 + 1 - y, 7.5f, 8 + x + 1, 8 + 1 - y + 1, 8.5f);
            }
        }

        return builder.getOutput();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return false;// TODO
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.baseModel.getTransforms();
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack) {
        try {
            if (stack.getItem() instanceof IMemoryCard memoryCard) {
                var colors = stack.getOrDefault(AEComponents.MEMORY_CARD_COLORS, MemoryCardColors.DEFAULT);

                return List.of(MemoryCardBakedModel.this.modelCache.get(colors,
                        () -> new MemoryCardBakedModel(MemoryCardBakedModel.this.baseModel,
                                MemoryCardBakedModel.this.texture, colors,
                                MemoryCardBakedModel.this.modelCache)));
            }
        } catch (ExecutionException e) {
            AELog.error(e);
        }

        return List.of(MemoryCardBakedModel.this);
    }

    @Override
    public void applyTransform(ItemDisplayContext transformType, PoseStack poseStack,
            boolean applyLeftHandTransform) {
        baseModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

}
