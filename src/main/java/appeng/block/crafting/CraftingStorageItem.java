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

package appeng.block.crafting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.block.AEBaseBlockItem;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;

public class CraftingStorageItem extends AEBaseBlockItem {

    public CraftingStorageItem(Block id, Item.Properties props) {
        super(id, props);
    }

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack) {
        return AEBlocks.CRAFTING_UNIT.stack();
    }

    @Override
    public boolean hasContainerItem(final ItemStack stack) {
        return AEConfig.instance().isDisassemblyCraftingEnabled();
    }
}
