/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;

import appeng.api.stacks.GenericStack;
import appeng.client.gui.AEBaseScreen;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;

/**
 * JEI allows ingredients to be dragged from a JEI panel onto compatible slots to set filters and the like without
 * having the actual item in hand.
 */
@SuppressWarnings("rawtypes")
class GhostIngredientHandler implements IGhostIngredientHandler<AEBaseScreen> {
    @Override
    public <I> List<Target<I>> getTargets(AEBaseScreen gui, I ingredient, boolean doStart) {
        var wrapped = wrapDraggedItem(ingredient);
        if (wrapped == null) {
            return Collections.emptyList();
        }

        List<Target<I>> targets = new ArrayList<>();

        addItemStackTargets(gui, targets, wrapped);

        return targets;
    }

    @Nullable
    private static ItemStack wrapDraggedItem(Object ingredient) {
        if (ingredient instanceof ItemStack itemStack) {
            return itemStack;
        } else {
            var genericStack = GenericEntryStackHelper.of(ingredient);
            if (genericStack != null) {
                return GenericStack.wrapInItemStack(genericStack);
            }
        }
        return null;
    }

    /**
     * Returns possible drop-targets for ghost items.
     */
    @SuppressWarnings("unchecked")
    private static <I> void addItemStackTargets(AEBaseScreen<?> gui, List<Target<I>> targets, ItemStack draggedStack) {
        for (Slot slot : gui.getMenu().slots) {
            if (!(slot instanceof AppEngSlot appEngSlot)) {
                continue;
            }

            if (!appEngSlot.isSlotEnabled()) {
                continue;
            }

            if (appEngSlot instanceof FakeSlot fakeSlot) {
                // Use the standard inventory function to test if the dragged stack would in theory be accepted
                if (!fakeSlot.canSetFilterTo(draggedStack)) {
                    continue;
                }

                targets.add((Target<I>) new ItemSlotTarget(gui, appEngSlot));
            }
        }
    }

    @Override
    public void onComplete() {
    }

    private static class ItemSlotTarget implements Target<Object> {
        private final AppEngSlot slot;
        private final Rect2i area;

        public ItemSlotTarget(AEBaseScreen<?> screen, AppEngSlot slot) {
            this.slot = slot;
            this.area = new Rect2i(screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, 16, 16);
        }

        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(Object ingredient) {
            var wrapped = wrapDraggedItem(ingredient);

            if (wrapped != null) {
                NetworkHandler.instance().sendToServer(new InventoryActionPacket(InventoryAction.SET_FILTER,
                        slot.index, wrapped));
            }
        }
    }

}
