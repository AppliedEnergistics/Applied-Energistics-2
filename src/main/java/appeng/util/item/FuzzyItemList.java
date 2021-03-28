/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2020, AlgorithmX2, All rights reserved.
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

package appeng.util.item;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AESharedItemStack.Bounds;

class FuzzyItemList extends AbstractItemList {
    private final Object2ObjectSortedMap<AESharedItemStack, IAEItemStack> records = new Object2ObjectAVLTreeMap<>();

    @Override
    public Collection<IAEItemStack> findFuzzy(final IAEItemStack filter, final FuzzyMode fuzzy) {
        if (filter == null) {
            return Collections.emptyList();
        }

        return this.findFuzzyDamage(filter, fuzzy);
    }

    @Override
    Map<AESharedItemStack, IAEItemStack> getRecords() {
        return this.records;
    }

    private Collection<IAEItemStack> findFuzzyDamage(final IAEItemStack filter, final FuzzyMode fuzzy) {
        final AEItemStack itemStack = (AEItemStack) filter;
        final Bounds bounds = itemStack.getSharedStack().getBounds(fuzzy);

        return this.records.subMap(bounds.lower(), bounds.upper()).values();
    }

}
