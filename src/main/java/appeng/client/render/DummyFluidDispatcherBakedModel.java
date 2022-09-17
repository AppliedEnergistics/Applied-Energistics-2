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


import appeng.fluids.items.FluidDummyItem;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;


/**
 * This baked model class is used as a dispatcher to redirect the renderer to the *real* model that should be used based
 * on the item stack.
 * A custom Item Override List is used to accomplish this.
 */
public class DummyFluidDispatcherBakedModel extends DelegateBakedModel {
    private final VertexFormat format;
    private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;

    public DummyFluidDispatcherBakedModel(IBakedModel baseModel, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        super(baseModel);
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
    }

    // This is never used. See the item override list below.
    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return Collections.emptyList();
    }

    @Override
    public boolean isGui3d() {
        return this.getBaseModel().isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ItemOverrideList(Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
                if (!(stack.getItem() instanceof FluidDummyItem)) {
                    return originalModel;
                }

                FluidDummyItem itemFacade = (FluidDummyItem) stack.getItem();

                FluidStack fluidStack = itemFacade.getFluidStack(stack);
                if (fluidStack == null) {
                    fluidStack = new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
                }

                TextureAtlasSprite sprite = DummyFluidDispatcherBakedModel.this.bakedTextureGetter.apply(fluidStack.getFluid().getStill(fluidStack));
                if (sprite == null) {
                    return new DummyFluidBakedModel(ImmutableList.of());
                }

                return new DummyFluidBakedModel(ItemLayerModel.getQuadsForSprite(0, sprite, DummyFluidDispatcherBakedModel.this.format, Optional.empty()));
            }
        };
    }
}
