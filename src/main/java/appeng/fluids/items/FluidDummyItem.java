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

package appeng.fluids.items;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import appeng.items.AEBaseItem;

/**
 * Dummy item to display the fluid Icon
 *
 * @author DrummerMC
 * @version rv6 - 2018-01-22
 * @since rv6 2018-01-22
 */
public class FluidDummyItem extends AEBaseItem {

    public FluidDummyItem(Properties properties) {
        super(properties);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        FluidStack fluidStack = this.getFluidStack(stack);
        if (fluidStack.isEmpty()) {
            fluidStack = new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
        }
        return fluidStack.getTranslationKey();
    }

    public FluidStack getFluidStack(ItemStack is) {
        if (is.hasTag()) {
            CompoundNBT tag = is.getTag();
            return FluidStack.loadFluidStackFromNBT(tag);
        }
        return FluidStack.EMPTY;
    }

    public void setFluidStack(ItemStack is, FluidStack fs) {
        if (fs.isEmpty()) {
            is.setTag(null);
        } else {
            CompoundNBT tag = new CompoundNBT();
            fs.writeToNBT(tag);
            is.setTag(tag);
        }
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        // Don't show this item in CreativeTabs
    }
}
