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

package appeng.core;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.ItemDefinition;

public class AEItemGroup extends CreativeModeTab {

    private final List<ItemDefinition<?>> itemDefs;

    public AEItemGroup(String label, List<ItemDefinition<?>> itemDefs) {
        super(label);
        this.itemDefs = itemDefs;
    }

    @Override
    public ItemStack makeIcon() {
        return AEBlocks.CONTROLLER.stack();
    }

    public void add(ItemDefinition<?> itemDef) {
        this.itemDefs.add(itemDef);
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> items) {
        for (ItemDefinition<?> itemDef : this.itemDefs) {
            itemDef.asItem().fillItemCategory(this, items);
        }
    }

}
