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
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
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

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

/**
 * Wraps a {@link GenericStack} in an {@link ItemStack}. Even stacks that actually represent vanilla {@link Item items}
 * will be wrapped in this item, to allow items with amount 0 to be represented as itemstacks without becoming the empty
 * item.
 */
public class WrappedGenericStack extends AEBaseItem {
    private static final String NBT_AMOUNT = "#";

    public static ItemStack wrap(GenericStack stack) {
        Objects.requireNonNull(stack, "stack");
        return wrap(stack.what(), stack.amount());
    }

    public static ItemStack wrap(AEKey what, long amount) {
        Objects.requireNonNull(what, "what");

        var item = AEItems.WRAPPED_GENERIC_STACK.asItem();
        var result = new ItemStack(item);

        var tag = what.toTagGeneric();
        if (amount != 0) {
            tag.putLong(NBT_AMOUNT, amount);
        }
        result.setTag(tag);
        return result;
    }

    public WrappedGenericStack(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Nullable
    public AEKey unwrapWhat(ItemStack stack) {
        if (stack.getItem() != this) {
            return null;
        }

        var tag = stack.getTag();
        if (tag == null) {
            return null;
        }

        return AEKey.fromTagGeneric(tag);
    }

    public long unwrapAmount(ItemStack stack) {
        if (stack.getItem() != this) {
            return 0;
        }

        long amount = 0;
        if (stack.getTag() != null && stack.getTag().contains(NBT_AMOUNT)) {
            amount = stack.getTag().getLong(NBT_AMOUNT);
        }

        return amount;
    }

    /**
     * Used to replace the mod id in the tooltip with the appropriate mod id of the wrapped fluid.
     */
    public static void modifyTooltip(ItemStack itemStack, List<Component> lines) {
        if (!(itemStack.getItem() instanceof WrappedGenericStack item)) {
            return;
        }

        var key = item.unwrapWhat(itemStack);
        if (key != null) {
            for (int i = lines.size() - 1; i >= 0; i--) {
                var line = lines.get(i);
                // REI adds a blue formatting code at the start
                if (line.getString().equals("§9§o" + AppEng.MOD_NAME)) {
                    var modId = Platform.formatModName(key.getModId());
                    lines.set(i, new TextComponent(modId));
                    break;
                }
            }
        }
    }

    /**
     * Allows picking up the contained fluid with a bucket.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack itemInSlot, ItemStack otherStack, Slot slot,
            ClickAction clickAction, Player player, SlotAccess otherItemAccess) {
        if (player.containerMenu == null) {
            // We need the opened menu since we're ignoring slotAccess due to no helper being available for it in the
            // transfer API
            return true;
        }

        // Allow picking up fluids items with a fluid container, this is a special case for fluids
        var what = unwrapWhat(itemInSlot);
        if (clickAction == ClickAction.PRIMARY && what instanceof AEFluidKey fluidKey) {
            // TODO: Getting the carried item is tricky
            var heldContainer = FluidUtil.getFluidHandler(player.containerMenu.getCarried())
                    .orElse(null);
            if (heldContainer != null) {
                long amount = unwrapAmount(itemInSlot);
                long inserted = heldContainer.fill(fluidKey.toStack(Ints.saturatedCast(amount)),
                        IFluidHandler.FluidAction.EXECUTE);

                if (inserted >= amount) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.set(wrap(what, amount - inserted));
                }

                player.containerMenu.setCarried(heldContainer.getContainer());
            }
        }

        // Generally disallow picking this up
        return true;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return Platform.getDescriptionId(this.getFluid(stack));
    }

    private FluidStack getFluid(ItemStack is) {
        if (is.hasTag()) {
            var key = AEFluidKey.fromTag(is.getTag());
            return key != null ? key.toStack(Ints.saturatedCast(getAmount(is))) : FluidStack.EMPTY;
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
