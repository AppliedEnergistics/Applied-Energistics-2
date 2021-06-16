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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import appeng.core.api.definitions.ApiBlocks;
import appeng.core.features.ItemDefinition;

public class AEItemGroup extends ItemGroup {

    private final List<ItemDefinition> itemDefs = new ArrayList<>();

    public AEItemGroup(String label) {
        super(label);
    }

    @Override
    public ItemStack createIcon() {
        return ApiBlocks.CONTROLLER.stack();
    }

    public void add(ItemDefinition itemDef) {
        this.itemDefs.add(itemDef);
    }

    @Override
    public void fill(NonNullList<ItemStack> items) {
        for (ItemDefinition itemDef : this.itemDefs) {
            itemDef.item().fillItemGroup(this, items);
        }
    }

}
