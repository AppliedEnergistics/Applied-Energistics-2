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

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

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
    private static final String NBT_FLUID_VARIANT = "FluidVariant";
    private static final String NBT_AMOUNT = "Amount";

    public static ItemStack fromFluidStack(ResourceAmount<FluidVariant> fs, boolean displayAmount) {
        var item = AEItems.DUMMY_FLUID_ITEM.asItem();
        var stack = new ItemStack(item);
        item.setFluid(stack, fs.resource());
        item.setAmount(stack, fs.amount());
        item.setDisplayAmount(stack, displayAmount);
        return stack;
    }

    public FluidDummyItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return Platform.getDescriptionId(this.getFluid(stack));
    }

    public FluidVariant getFluid(ItemStack is) {
        if (is.hasTag()) {
            return FluidVariant.fromNbt(is.getTag().getCompound(NBT_FLUID_VARIANT));
        }
        return FluidVariant.blank();
    }

    public long getAmount(ItemStack is) {
        if (is.hasTag()) {
            return is.getTag().getLong(NBT_AMOUNT);
        }
        return 0;
    }

    public ResourceAmount<FluidVariant> getFluidStack(ItemStack is) {
        return new ResourceAmount<>(getFluid(is), getAmount(is));
    }

    public void setAmount(ItemStack is, long amount) {
        if (amount == 0) {
            is.removeTagKey(NBT_AMOUNT);
        } else {
            is.getOrCreateTag().putLong(NBT_AMOUNT, amount);
        }
    }

    public void setFluid(ItemStack is, FluidVariant fluid) {
        if (fluid.isBlank()) {
            is.removeTagKey(NBT_FLUID_VARIANT);
        } else {
            is.getOrCreateTag().put(NBT_FLUID_VARIANT, fluid.toNbt());
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
            lines.add(new TextComponent(Platform.formatFluidAmount(getAmount(stack))));
        }
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        // Don't show this item in CreativeTabs
    }
}
