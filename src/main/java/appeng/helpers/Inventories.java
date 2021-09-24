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

package appeng.helpers;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;

public final class Inventories {

    private Inventories() {
    }

    public static void clear(InternalInventory inv) {
        for (int x = 0; x < inv.size(); x++) {
            inv.setItemDirect(x, ItemStack.EMPTY);
        }
    }

    public static void copy(CraftingContainer from, InternalInventory to, boolean deepCopy) {
        for (int i = 0; i < Math.min(from.getContainerSize(), to.size()); ++i) {
            to.setItemDirect(i, deepCopy ? from.getItem(i).copy() : from.getItem(i));
        }
    }

}
