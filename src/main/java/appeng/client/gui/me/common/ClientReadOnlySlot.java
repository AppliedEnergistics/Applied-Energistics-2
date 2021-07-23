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

package appeng.client.gui.me.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

/**
 * Base class for virtual client-side only slots that do not allow Vanilla to directly interact with the contained item
 * (since it purely exists client-side).
 */
public class ClientReadOnlySlot extends Slot {
    /**
     * We use this fake/empty inventory to prevent other mods from attempting to interact with anything based on this
     * slot's inventory/slot index.
     */
    private static final IInventory EMPTY_INVENTORY = new Inventory(0);

    public ClientReadOnlySlot(int xPosition, int yPosition) {
        super(EMPTY_INVENTORY, 0, xPosition, yPosition);
    }

    @Override
    public final boolean mayPlace(final ItemStack stack) {
        return false;
    }

    @Override
    public final void set(final ItemStack stack) {
    }

    @Override
    public final int getMaxStackSize() {
        return 0;
    }

    @Override
    public final ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public final boolean mayPickup(PlayerEntity player) {
        return false;
    }
}
