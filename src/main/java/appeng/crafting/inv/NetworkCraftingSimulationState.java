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

import java.util.Collection;

import javax.annotation.Nullable;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedStackList;

/**
 * Currently, extracts the whole network contents when the job starts. Lazily extracting is unfortunately not possible
 * as long as the crafting simulation operates from a separate thread: any world access from this thread will deadlock
 * the server.
 */
public class NetworkCraftingSimulationState extends CraftingSimulationState {
    private final MixedStackList list = new MixedStackList();

    public NetworkCraftingSimulationState(IStorageMonitorable monitorable, @Nullable IActionSource src) {
        // Take care of the edge case where ICraftingSimulationRequester#getActionSource() returns null.
        if (src == null) {
            return;
        }

        for (var channel : StorageChannels.getAll()) {
            collectChannelContents(channel, monitorable, src);
        }
    }

    private <T extends IAEStack> void collectChannelContents(IStorageChannel<T> channel,
            IStorageMonitorable monitorable, IActionSource src) {
        var monitor = monitorable.getInventory(channel);
        for (var stack : monitor.getStorageList()) {
            this.list.addStorage(monitor.extractItems(stack, Actionable.SIMULATE, src));
        }
    }

    @Override
    protected IAEStack simulateExtractParent(IAEStack input) {
        var precise = list.findPrecise(input);
        if (precise == null)
            return null;
        else
            return IAEStack.copy(input, Math.min(input.getStackSize(), precise.getStackSize()));
    }

    @Override
    protected Collection<IAEStack> findFuzzyParent(IAEStack input) {
        return list.findFuzzy(input, FuzzyMode.IGNORE_ALL);
    }
}
