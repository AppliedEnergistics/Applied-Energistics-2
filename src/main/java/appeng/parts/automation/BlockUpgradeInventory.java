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
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;


public class BlockUpgradeInventory extends UpgradeInventory {
    private final Block block;

    public BlockUpgradeInventory(final Block block, final IAEAppEngInventory parent, final int s) {
        super(parent, s);
        this.block = block;
    }

    @Override
    public int getMaxInstalled(final Upgrades upgrades) {
        int max = 0;

        for (final ItemStack is : upgrades.getSupported().keySet()) {
            final Item encodedItem = is.getItem();

            if (encodedItem instanceof ItemBlock && Block.getBlockFromItem(encodedItem) == this.block) {
                max = upgrades.getSupported().get(is);
                break;
            }
        }

        return max;
    }
}
