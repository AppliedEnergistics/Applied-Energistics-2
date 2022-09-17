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


import appeng.items.AEBaseItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;


/**
 * Dummy item to display the fluid Icon
 *
 * @author DrummerMC
 * @version rv6 - 2018-01-22
 * @since rv6 2018-01-22
 */
public class FluidDummyItem extends AEBaseItem {
    @Override
    public String getItemStackDisplayName(ItemStack stack) {

        FluidStack fluidStack = this.getFluidStack(stack);
        if (fluidStack == null) {
            fluidStack = new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
        }
        return fluidStack.getLocalizedName();
    }

    public FluidStack getFluidStack(ItemStack is) {
        if (is.hasTagCompound()) {
            NBTTagCompound tag = is.getTagCompound();
            return FluidStack.loadFluidStackFromNBT(tag);
        }
        return null;
    }

    public void setFluidStack(ItemStack is, FluidStack fs) {
        if (fs == null) {
            is.setTagCompound(null);
        } else {
            NBTTagCompound tag = new NBTTagCompound();
            fs.writeToNBT(tag);
            is.setTagCompound(tag);
        }
    }

    @Override
    protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
        // Don't show this item in CreativeTabs
    }
}
