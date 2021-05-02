/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.gui.me.items;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.ActionItems;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.container.SlotSemantic;
import appeng.container.me.items.CraftingTermContainer;
import appeng.util.Platform;

/**
 * This screen extends the item terminal with a crafting grid. The content of the crafting grid is stored server-side in
 * the crafting terminal itself.
 */
public class CraftingTermScreen extends ItemTerminalScreen<CraftingTermContainer> {

    public CraftingTermScreen(CraftingTermContainer container, PlayerInventory playerInventory,
            ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);

        ActionButton clearBtn = new ActionButton(ActionItems.STASH, btn -> container.clearCraftingGrid());
        clearBtn.setHalfSize(true);
        widgets.add("clearCraftingGrid", clearBtn);
    }

    @Override
    public boolean hasItemType(ItemStack itemStack, int amount) {
        // In addition to the base item repo, also check the crafting grid if it
        // already contains some of the needed items
        for (Slot slot : container.getSlots(SlotSemantic.CRAFTING_GRID)) {
            ItemStack stackInSlot = slot.getStack();
            if (!stackInSlot.isEmpty()) {
                if (Platform.itemComparisons().isSameItem(itemStack, stackInSlot)) {
                    if (itemStack.getCount() >= amount) {
                        return true;
                    }
                    amount -= itemStack.getCount();
                }
            }

        }

        return super.hasItemType(itemStack, amount);
    }

}
