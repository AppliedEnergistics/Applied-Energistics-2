/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts.automation;


import appeng.api.config.Upgrades;
import appeng.util.inv.IAEAppEngInventory;
import net.minecraft.item.ItemStack;


public class StackUpgradeInventory extends UpgradeInventory {
    private final ItemStack stack;

    public StackUpgradeInventory(final ItemStack stack, final IAEAppEngInventory inventory, final int s) {
        super(inventory, s);
        this.stack = stack;
    }

    @Override
    public int getMaxInstalled(final Upgrades upgrades) {
        int max = 0;

        for (final ItemStack is : upgrades.getSupported().keySet()) {
            if (ItemStack.areItemsEqual(this.stack, is)) {
                max = upgrades.getSupported().get(is);
                break;
            }
        }

        return max;
    }
}
