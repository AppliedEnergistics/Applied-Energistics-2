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

package appeng.util.inv;


import appeng.api.config.Actionable;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import net.minecraft.item.ItemStack;


public class IMEInventoryDestination implements IInventoryDestination {

    private final IMEInventory<IAEItemStack> me;

    public IMEInventoryDestination(final IMEInventory<IAEItemStack> o) {
        this.me = o;
    }

    @Override
    public boolean canInsert(final ItemStack stack) {

        if (stack.isEmpty()) {
            return false;
        }

        final IAEItemStack failed = this.me.injectItems(AEItemStack.fromItemStack(stack), Actionable.SIMULATE, null);

        if (failed == null) {
            return true;
        }
        return failed.getStackSize() != stack.getCount();
    }
}
