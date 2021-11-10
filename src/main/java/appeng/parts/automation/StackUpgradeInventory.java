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

import net.minecraft.world.item.ItemStack;

import appeng.api.config.Upgrades;
import appeng.util.inv.InternalInventoryHost;

/**
 * An upgrade inventory that limits which upgrades are supported by checking them against a part itemstack.
 */
public class StackUpgradeInventory extends UpgradeInventory {
    private final ItemStack stack;

    public StackUpgradeInventory(ItemStack stack, InternalInventoryHost inventory, int slots) {
        super(inventory, slots);
        this.stack = stack;
    }

    @Override
    public int getMaxInstalled(Upgrades upgrades) {
        for (Upgrades.Supported supported : upgrades.getSupported()) {
            if (supported.isSupported(stack.getItem())) {
                return supported.getMaxCount();
            }
        }

        return 0;
    }
}
