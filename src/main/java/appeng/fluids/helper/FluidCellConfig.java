/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.helper;


import appeng.core.Api;
import appeng.fluids.items.FluidDummyItem;
import appeng.items.contents.CellConfig;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;


/**
 * @author DrummerMC
 * @version rv6 - 2018-01-22
 * @since rv6 2018-01-22
 */
public class FluidCellConfig extends CellConfig {
    public FluidCellConfig(ItemStack is) {
        super(is);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || stack.getItem() instanceof FluidDummyItem) {
            super.insertItem(slot, stack, simulate);
        }
        FluidStack fluid = FluidUtil.getFluidContained(stack);
        if (fluid == null || !Api.INSTANCE.definitions().items().dummyFluidItem().maybeStack(1).isPresent()) {
            return stack;
        }

        fluid.amount = Fluid.BUCKET_VOLUME;
        ItemStack is = Api.INSTANCE.definitions().items().dummyFluidItem().maybeStack(1).get();
        FluidDummyItem item = (FluidDummyItem) is.getItem();
        item.setFluidStack(is, fluid);
        return super.insertItem(slot, is, simulate);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof FluidDummyItem) {
            super.setStackInSlot(slot, stack);
        }
        FluidStack fluid = FluidUtil.getFluidContained(stack);
        if (fluid == null || !Api.INSTANCE.definitions().items().dummyFluidItem().maybeStack(1).isPresent()) {
            return;
        }

        fluid.amount = Fluid.BUCKET_VOLUME;
        ItemStack is = Api.INSTANCE.definitions().items().dummyFluidItem().maybeStack(1).get();
        FluidDummyItem item = (FluidDummyItem) is.getItem();
        item.setFluidStack(is, fluid);
        super.setStackInSlot(slot, is);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof FluidDummyItem) {
            super.isItemValid(slot, stack);
        }
        FluidStack fluid = FluidUtil.getFluidContained(stack);
        if (fluid == null || !Api.INSTANCE.definitions().items().dummyFluidItem().maybeStack(1).isPresent()) {
            return false;
        }
        fluid.amount = Fluid.BUCKET_VOLUME;
        ItemStack is = Api.INSTANCE.definitions().items().dummyFluidItem().maybeStack(1).get();
        FluidDummyItem item = (FluidDummyItem) is.getItem();
        item.setFluidStack(is, fluid);
        return super.isItemValid(slot, is);
    }

}
