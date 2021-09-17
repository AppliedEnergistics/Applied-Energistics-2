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

package appeng.items.misc;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

/**
 * Dummy item to display the fluid Icon
 *
 * @author DrummerMC
 * @version rv6 - 2018-01-22
 * @since rv6 2018-01-22
 */
public class FluidDummyItem extends AEBaseItem {
    private static final String NBT_DISPLAY_AMOUNT = "DisplayAmount";

    public static ItemStack fromFluidStack(FluidStack fs, boolean displayAmount) {
        var item = AEItems.DUMMY_FLUID_ITEM.asItem();
        var stack = new ItemStack(item);
        item.setFluidStack(stack, fs);
        item.setDisplayAmount(stack, displayAmount);
        return stack;
    }

    public FluidDummyItem(Item.Properties properties) {
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
            CompoundTag tag = is.getTag();
            return FluidStack.loadFluidStackFromNBT(tag);
        }
        return FluidStack.EMPTY;
    }

    public void setFluidStack(ItemStack is, FluidStack fs) {
        if (fs.isEmpty()) {
            is.setTag(null);
        } else {
            CompoundTag tag = new CompoundTag();
            fs.writeToNBT(tag);
            is.setTag(tag);
        }
    }

    public void setDisplayAmount(ItemStack is, boolean displayAmount) {
        if (displayAmount) {
            is.getOrCreateTag().put(NBT_DISPLAY_AMOUNT, new CompoundTag());
        } else {
            is.removeTagKey(NBT_DISPLAY_AMOUNT);
        }
    }

    public boolean getDisplayAmount(ItemStack is) {
        return is.hasTag() && is.getTag().contains(NBT_DISPLAY_AMOUNT);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        if (getDisplayAmount(stack)) {
            lines.add(new TextComponent(Platform.formatFluidAmount(getFluidStack(stack).getAmount())));
        }
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        // Don't show this item in CreativeTabs
    }
}
