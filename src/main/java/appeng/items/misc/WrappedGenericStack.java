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

import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.Actionable;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;

/**
 * Wraps a {@link GenericStack} in an {@link ItemStack}. Even stacks that actually represent vanilla {@link Item items}
 * will be wrapped in this item, to allow items with amount 0 to be represented as itemstacks without becoming the empty
 * item.
 */
public class WrappedGenericStack extends AEBaseItem {
    private static final Logger LOG = LoggerFactory.getLogger(WrappedGenericStack.class);

    public static ItemStack wrap(GenericStack stack) {
        Objects.requireNonNull(stack, "stack");
        var item = AEItems.WRAPPED_GENERIC_STACK.asItem();
        var result = new ItemStack(item);
        result.set(AEComponents.WRAPPED_STACK, stack);
        return result;
    }

    public static ItemStack wrap(AEKey what, long amount) {
        Objects.requireNonNull(what, "what");

        return wrap(new GenericStack(what, amount));
    }

    public WrappedGenericStack(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Nullable
    public AEKey unwrapWhat(ItemStack stack) {
        if (stack.getItem() != this) {
            return null;
        }

        var wrapped = stack.get(AEComponents.WRAPPED_STACK);

        if (wrapped == null) {
            return null;
        }

        return wrapped.what();
    }

    public long unwrapAmount(ItemStack stack) {
        if (stack.getItem() != this) {
            return 0;
        }

        var wrapped = stack.get(AEComponents.WRAPPED_STACK);

        if (wrapped == null) {
            return 0;
        }

        return wrapped.amount();
    }

    /**
     * Allows picking up the contained fluid with a bucket.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack itemInSlot, ItemStack otherStack, Slot slot,
            ClickAction clickAction, Player player, SlotAccess access) {
        if (player.containerMenu == null) {
            // We need the opened menu since we're ignoring slotAccess due to no helper being available for it in the
            // transfer API
            return true;
        }

        // When trying to stack onto degenerate wrapped stacks, delete them
        var what = unwrapWhat(itemInSlot);
        if (what == null && slot.getItem() == itemInSlot) {
            LOG.error("Removing a broken wrapped generic stack from player {} slot {}", player, slot.getSlotIndex());
            slot.setByPlayer(ItemStack.EMPTY, itemInSlot);
            return true;
        }

        // Allow picking up fluids items with a fluid container, this is a special case for fluids
        if (clickAction == ClickAction.PRIMARY) {
            var heldContainer = ContainerItemStrategies.findCarriedContextForKey(what, player, player.containerMenu);
            if (heldContainer != null) {
                long amount = unwrapAmount(itemInSlot);
                long inserted = heldContainer.insert(what, amount, Actionable.MODULATE);

                // Check client to avoid duplicate sounds in singleplayer
                if (player.level().isClientSide()) {
                    heldContainer.playFillSound(player, what);
                }

                if (inserted >= amount) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.set(wrap(what, amount - inserted));
                }
            }
        }

        // Generally disallow picking this up
        return true;
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        // Don't show this item in CreativeTabs
    }
}
