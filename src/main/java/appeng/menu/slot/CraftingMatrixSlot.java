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

package appeng.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.menu.AEBaseMenu;

public class CraftingMatrixSlot extends AppEngSlot {
    private final AEBaseMenu c;
    private final Container wrappedInventory;

    public CraftingMatrixSlot(final AEBaseMenu c, final InternalInventory inv, final int invSlot) {
        super(inv, invSlot);
        this.c = c;
        this.wrappedInventory = inv.toContainer();
    }

    @Override
    public void clearStack() {
        super.clearStack();
        this.c.slotsChanged(this.wrappedInventory);
    }

    @Override
    public void set(final ItemStack par1ItemStack) {
        super.set(par1ItemStack);
        this.c.slotsChanged(this.wrappedInventory);
    }

    @Override
    public ItemStack remove(final int par1) {
        final ItemStack is = super.remove(par1);
        this.c.slotsChanged(this.wrappedInventory);
        return is;
    }
}
