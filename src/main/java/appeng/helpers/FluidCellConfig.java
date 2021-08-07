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

package appeng.helpers;

import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.core.definitions.AEItems;
import appeng.items.contents.CellConfig;
import appeng.items.misc.FluidDummyItem;

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
        Optional<FluidStack> fluidOpt = FluidUtil.getFluidContained(stack);
        if (!fluidOpt.isPresent()) {
            return stack;
        }
        FluidStack fluid = fluidOpt.orElse(null);

        fluid.setAmount(FluidAttributes.BUCKET_VOLUME);
        ItemStack is = AEItems.DUMMY_FLUID_ITEM.stack();
        FluidDummyItem item = (FluidDummyItem) is.getItem();
        item.setFluidStack(is, fluid);
        return super.insertItem(slot, is, simulate);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof FluidDummyItem) {
            super.setStackInSlot(slot, stack);
        }
        Optional<FluidStack> fluidOpt = FluidUtil.getFluidContained(stack);
        if (!fluidOpt.isPresent()) {
            return;
        }
        FluidStack fluid = fluidOpt.orElse(null);

        fluid.setAmount(FluidAttributes.BUCKET_VOLUME);
        ItemStack is = AEItems.DUMMY_FLUID_ITEM.stack();
        FluidDummyItem item = (FluidDummyItem) is.getItem();
        item.setFluidStack(is, fluid);
        super.setStackInSlot(slot, is);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof FluidDummyItem) {
            super.isItemValid(slot, stack);
        }
        Optional<FluidStack> fluidOpt = FluidUtil.getFluidContained(stack);
        if (!fluidOpt.isPresent()) {
            return false;
        }
        FluidStack fluid = fluidOpt.orElse(null);

        fluid.setAmount(FluidAttributes.BUCKET_VOLUME);
        ItemStack is = AEItems.DUMMY_FLUID_ITEM.stack();
        FluidDummyItem item = (FluidDummyItem) is.getItem();
        item.setFluidStack(is, fluid);
        return super.isItemValid(slot, is);
    }

}
