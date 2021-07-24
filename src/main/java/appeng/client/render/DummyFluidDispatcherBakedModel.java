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

package appeng.client.render;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import com.mojang.math.Transformation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import appeng.fluids.items.FluidDummyItem;

/**
 * This baked model class is used as a dispatcher to redirect the renderer to the *real* model that should be used based
 * on the item stack. A custom Item Override List is used to accomplish this.
 */
public class DummyFluidDispatcherBakedModel extends DelegateBakedModel {
    private final Function<Material, TextureAtlasSprite> bakedTextureGetter;

    public DummyFluidDispatcherBakedModel(BakedModel baseModel,
                                          Function<Material, TextureAtlasSprite> bakedTextureGetter) {
        super(baseModel);
        this.bakedTextureGetter = bakedTextureGetter;
    }

    // This is never used. See the item override list below.
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return Collections.emptyList();
    }

    @Override
    public boolean isGui3d() {
        return this.getBaseModel().isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return getBaseModel().usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel originalModel, ItemStack stack, ClientLevel world,
                                      LivingEntity entity, int seed) {
                if (!(stack.getItem() instanceof FluidDummyItem)) {
                    return originalModel;
                }

                FluidDummyItem itemFacade = (FluidDummyItem) stack.getItem();

                FluidStack fluidStack = itemFacade.getFluidStack(stack);
                if (fluidStack.isEmpty()) {
                    fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
                }

                FluidAttributes attributes = fluidStack.getFluid().getAttributes();
                ResourceLocation stillTexture = attributes.getStillTexture(fluidStack);
                Material stillMaterial = new Material(TextureAtlas.LOCATION_BLOCKS, stillTexture);
                TextureAtlasSprite sprite = DummyFluidDispatcherBakedModel.this.bakedTextureGetter.apply(stillMaterial);
                if (sprite == null) {
                    return new DummyFluidBakedModel(ImmutableList.of());
                }

                return new DummyFluidBakedModel(
                        ItemLayerModel.getQuadsForSprite(0, sprite, Transformation.identity()));
            }
        };
    }
}
