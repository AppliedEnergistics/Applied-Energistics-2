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

package appeng.util.item;

import java.util.Collection;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;

/**
 * This variant list is optimized for items that cannot be damaged and thus do not support querying durability ranges
 * via {@link #findFuzzy(IAEItemStack, FuzzyMode)}.
 */
class NormalItemVariantList extends ItemVariantList {

    private final Reference2ObjectMap<AESharedItemStack, IAEItemStack> records = new Reference2ObjectOpenHashMap<>();

    @Override
    Map<AESharedItemStack, IAEItemStack> getRecords() {
        return this.records;
    }

    /**
     * For items that do not support durability, we just return all variants to a fuzzy search.
     */
    @Override
    public Collection<IAEItemStack> findFuzzy(IAEItemStack filter, FuzzyMode fuzzy) {
        return this.getRecords().values();
    }

}
