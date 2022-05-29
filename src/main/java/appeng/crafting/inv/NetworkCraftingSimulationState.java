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

package appeng.crafting.inv;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Iterables;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.core.AEConfig;

/**
 * Currently, extracts the whole network contents when the job starts. Lazily extracting is unfortunately not possible
 * as long as the crafting simulation operates from a separate thread: any world access from this thread will deadlock
 * the server.
 */
public class NetworkCraftingSimulationState extends CraftingSimulationState {
    private final KeyCounter list = new KeyCounter();

    public NetworkCraftingSimulationState(IStorageService storage, @Nullable IActionSource src) {
        // Take care of the edge case where ICraftingSimulationRequester#getActionSource() returns null.
        if (src == null) {
            return;
        }

        for (var stack : storage.getCachedInventory()) {
            long networkAmount = AEConfig.instance().isCraftingSimulatedExtraction()
                    ? storage.getInventory().extract(stack.getKey(), stack.getLongValue(), Actionable.SIMULATE, src)
                    : stack.getLongValue();
            if (networkAmount > 0) {
                this.list.add(stack.getKey(), networkAmount);
            }
        }
    }

    @Override
    protected long simulateExtractParent(AEKey what, long amount) {
        return Math.min(list.get(what), amount);
    }

    @Override
    protected Iterable<AEKey> findFuzzyParent(AEKey input) {
        return Iterables.transform(list.findFuzzy(input, FuzzyMode.IGNORE_ALL), Map.Entry::getKey);
    }
}
