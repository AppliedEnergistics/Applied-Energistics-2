/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.util.inv.filter;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.definitions.IItemDefinition;

public class AEItemDefinitionFilter implements IAEItemFilter {
    private final IItemDefinition definition;

    public AEItemDefinitionFilter(IItemDefinition definition) {
        this.definition = definition;
    }

    @Override
    public boolean allowExtract(IItemHandler inv, int slot, int amount) {
        return true;
    }

    @Override
    public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
        return this.definition.isSameAs(stack);
    }

}
