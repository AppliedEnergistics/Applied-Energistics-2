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

import com.google.common.collect.Iterables;

import org.jetbrains.annotations.Nullable;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;

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

        // We choose to re-query the available stacks every time a crafting simulation is started by a player.
        // Using getCachedInventory causes issues with our "CTRL+click to craft" integration with EMI, which submits a
        // job and then immediately starts a new simulation. We want that simulation to see the state of the network
        // after the previous job was submitted in case of overlap between the recipes. More generally, having to replan
        // is annoying, and we want to minimize the risk of that for player-started calculations.
        // For non-player sources, it is fine to use the cached inventory: they will submit a new request eventually if
        // this simulation or job fails.
        var inventory = src.player().isEmpty() ? storage.getCachedInventory()
                : storage.getInventory().getAvailableStacks();
        for (var stack : inventory) {
            long networkAmount = stack.getLongValue();
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
