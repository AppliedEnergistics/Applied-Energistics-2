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

import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;

import appeng.core.Api;
import appeng.fluids.items.FluidDummyItem;
import appeng.items.contents.CellConfig;

/**
 * @author DrummerMC
 * @version rv6 - 2018-01-22
 * @since rv6 2018-01-22
 */
public class FluidCellConfig extends CellConfig {
    public FluidCellConfig(ItemStack is) {
        super(is);
    }

    private static ItemStack tryConvertToFluidDummy(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof FluidDummyItem) {
            return stack;
        }

        // Try to auto-convert any fluid-containing item into a dummy item before
        // insertion
        FluidExtractable fluidExtractable = FluidAttributes.EXTRACTABLE.getFirstOrNull(stack);
        if (fluidExtractable == null) {
            return ItemStack.EMPTY;
        }

        FluidVolume fluid = fluidExtractable.attemptAnyExtraction(FluidAmount.MAX_VALUE, Simulation.SIMULATE);
        if (fluid.isEmpty()) {
            return ItemStack.EMPTY;
        }

        fluid = fluid.withAmount(FluidAmount.BUCKET);

        ItemStack is = Api.instance().definitions().items().dummyFluidItem().stack(1);
        FluidDummyItem item = (FluidDummyItem) is.getItem();
        item.setFluidStack(is, fluid);
        return is;
    }

    @Override
    public ItemStack attemptInsertion(ItemStack stack, Simulation simulation) {
        stack = tryConvertToFluidDummy(stack);
        return super.attemptInsertion(stack, simulation);
    }

    @Override
    public boolean setInvStack(int slot, ItemStack to, Simulation simulation) {
        to = tryConvertToFluidDummy(to);
        return super.setInvStack(slot, to, simulation);
    }

    @Override
    public ItemFilter getFilterForSlot(int slot) {
        return stack -> !tryConvertToFluidDummy(stack).isEmpty();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return !tryConvertToFluidDummy(stack).isEmpty();
    }

}
