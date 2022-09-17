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

package appeng.core.features.registries;


import appeng.api.features.IItemComparison;
import appeng.api.features.IItemComparisonProvider;
import appeng.api.features.ISpecialComparisonRegistry;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class SpecialComparisonRegistry implements ISpecialComparisonRegistry {

    private final List<IItemComparisonProvider> CompRegistry;

    public SpecialComparisonRegistry() {
        this.CompRegistry = new ArrayList<>();
    }

    @Override
    public IItemComparison getSpecialComparison(final ItemStack stack) {
        for (final IItemComparisonProvider i : this.CompRegistry) {
            final IItemComparison comp = i.getComparison(stack);
            if (comp != null) {
                return comp;
            }
        }

        return null;
    }

    @Override
    public void addComparisonProvider(final IItemComparisonProvider prov) {
        this.CompRegistry.add(prov);
    }
}
