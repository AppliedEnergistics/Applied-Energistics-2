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

import com.google.common.base.Preconditions;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

/**
 * Wraps a {@link appeng.api.storage.data.IAEFluidStack} in an {@link ItemStack}.
 */
public class WrappedFluidStack extends AEBaseItem {
    private static final String NBT_FLUID = "f";
    private static final String NBT_AMOUNT = "a";

    public static ItemStack wrap(IAEFluidStack stack) {
        Preconditions.checkNotNull(stack, "stack");

        var item = AEItems.WRAPPED_FLUID_STACK.asItem();
        var result = new ItemStack(item);

        var tag = result.getOrCreateTag();
        var fluidTag = new CompoundTag();
        stack.getFluidStack().writeToNBT(fluidTag);
        tag.put(NBT_FLUID, fluidTag);
        if (stack.getStackSize() > 0) {
            tag.putLong(NBT_AMOUNT, stack.getStackSize());
        }
        return result;
    }

    public static boolean isWrapped(ItemStack stack) {
        return AEItems.WRAPPED_FLUID_STACK.isSameAs(stack);
    }

    @Nullable
    public static IAEFluidStack unwrap(ItemStack stack) {
        if (!AEItems.WRAPPED_FLUID_STACK.isSameAs(stack)) {
            return null;
        }

        var tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_FLUID, Tag.TAG_COMPOUND)) {
            return null;
        }

        var fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound(NBT_FLUID));
        if (fluid.isEmpty()) {
            return null;
        }

        return IAEFluidStack.of(fluid, tag.getLong(NBT_AMOUNT));
    }

    public WrappedFluidStack(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * Used to replace the mod id in the tooltip with the appropriate mod id of the wrapped fluid.
     */
    public static void modifyTooltip(ItemStack itemStack, List<Component> lines) {
        var fluidStack = WrappedFluidStack.unwrap(itemStack);
        for (int i = lines.size() - 1; i >= 0; i--) {
            var line = lines.get(i);
            // REI adds a blue formatting code at the start
            if (line.getString().equals("§9§o" + AppEng.MOD_NAME)) {
                var modId = Platform.formatModName(Platform.getModId(fluidStack));
                lines.set(i, new TextComponent(modId));
                break;
            }
        }
    }

    /**
     * Allows picking up the contained fluid with a bucket.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack itemInSlot, ItemStack otherStack, Slot slot,
            ClickAction clickAction, Player player, SlotAccess otherItemAccess) {
        // Allow picking up fluid dummy items with a fluid container
        var fluidStack = WrappedFluidStack.unwrap(itemInSlot);
        if (clickAction == ClickAction.PRIMARY && fluidStack != null) {
            // TODO: Getting the carried item is tricky
            var heldContainer = FluidUtil.getFluidHandler(otherStack).orElse(null);
            if (heldContainer != null) {
                var inserted = heldContainer.fill(fluidStack.getFluidStack(), IFluidHandler.FluidAction.EXECUTE);

                if (inserted >= fluidStack.getStackSize()) {
                    slot.set(ItemStack.EMPTY);
                    otherItemAccess.set(heldContainer.getContainer());
                } else if (inserted > 0) {
                    fluidStack.decStackSize(inserted);
                    slot.set(fluidStack.wrap());
                    otherItemAccess.set(heldContainer.getContainer());
                }
            }
        }

        // Generally disallow picking this up
        return true;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.getFluid(stack).getTranslationKey();
    }

    private FluidStack getFluid(ItemStack is) {
        if (is.hasTag()) {
            return FluidStack.loadFluidStackFromNBT(is.getTag().getCompound(NBT_FLUID));
        }
        return FluidStack.EMPTY;
    }

    public long getAmount(ItemStack is) {
        Preconditions.checkArgument(is.getItem() == this);
        if (is.hasTag()) {
            return is.getTag().getLong(NBT_AMOUNT);
        }
        return 0;
    }

    public void setAmount(ItemStack is, long amount) {
        Preconditions.checkArgument(is.getItem() == this);
        if (amount == 0) {
            is.removeTagKey(NBT_AMOUNT);
        } else {
            is.getOrCreateTag().putLong(NBT_AMOUNT, amount);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        long amount = getAmount(stack);
        if (amount > 0) {
            lines.add(new TextComponent(Platform.formatFluidAmount(amount)));
        }
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        // Don't show this item in CreativeTabs
    }
}
