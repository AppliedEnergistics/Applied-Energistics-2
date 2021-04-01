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

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

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
    public String getTranslationKey(ItemStack stack) {
        FluidVolume fluidStack = this.getFluidStack(stack);
        if (fluidStack.isEmpty()) {
            fluidStack = FluidKeys.WATER.withAmount(FluidAmount.BUCKET);
        }
        return fluidStack.getName().getString();
    }

    public FluidVolume getFluidStack(ItemStack is) {
        CompoundNBT tag = is.getTag();
        if (tag != null) {
            return FluidVolume.fromTag(tag);
        }
        return FluidVolumeUtil.EMPTY;
    }

    public void setFluidStack(ItemStack is, FluidVolume fs) {
        if (fs.isEmpty()) {
            is.setTag(null);
        } else {
            CompoundNBT tag = new CompoundNBT();
            fs.toTag(tag);
            is.setTag(tag);
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        // Don't show this item in CreativeTabs
    }
}
