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

package appeng.me.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMaps;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;

/**
 * Manages all available {@link MEStorage} on the network.
 */
public class NetworkStorage implements MEStorage {
    private static final Comparator<Integer> PRIORITY_SORTER = (o1, o2) -> Integer.compare(o2, o1);

    // This flag prevents both concurrent modifications of the mounted storage while
    // they're being iterated, and recursive extract/insert/list operations.
    private boolean mountsInUse;

    private final Int2ObjectAVLTreeMap<List<MEStorage>> priorityInventory;
    private final List<MEStorage> secondPassInventories = new ArrayList<>();

    // Queued mount/unmount operations that occurred while an insert/extract was ongoing
    // Is only non-null if something is queued
    @Nullable
    private List<QueuedOperation> queuedOperations;

    public NetworkStorage() {
        this.priorityInventory = new Int2ObjectAVLTreeMap<>(PRIORITY_SORTER);
    }

    public void mount(int priority, MEStorage inventory) {
        if (mountsInUse) {
            if (queuedOperations == null) {
                queuedOperations = new ArrayList<>();
            }
            queuedOperations.add(new MountOperation(priority, inventory));
        } else {
            this.priorityInventory.computeIfAbsent(priority, k -> new ArrayList<>())
                    .add(inventory);
        }
    }

    public void unmount(MEStorage inventory) {
        if (mountsInUse) {
            if (queuedOperations == null) {
                queuedOperations = new ArrayList<>();
            }
            queuedOperations.add(new UnmountOperation(inventory));
        } else {
            var prioIt = Int2ObjectSortedMaps.fastIterator(this.priorityInventory);
            while (prioIt.hasNext()) {
                var prioEntry = prioIt.next();

                var inventories = prioEntry.getValue();
                if (inventories.remove(inventory) && inventories.isEmpty()) {
                    prioIt.remove();
                }
            }
        }
    }

    public long insert(AEKey what, long amount, Actionable type, IActionSource src) {
        if (mountsInUse) {
            return 0; // Prevent recursive use
        }

        var remaining = amount;

        mountsInUse = true;
        try {
            for (var invList : this.priorityInventory.values()) {
                secondPassInventories.clear();

                // First give every inventory a chance to accept the item if it's preferential storage for the given
                // stack
                var ii = invList.iterator();
                while (ii.hasNext() && remaining > 0) {
                    var inv = ii.next();

                    if (isQueuedForRemoval(inv)) {
                        continue;
                    }

                    if (inv.isPreferredStorageFor(what, src)) {
                        remaining -= inv.insert(what, remaining, type, src);
                    } else {
                        secondPassInventories.add(inv);
                    }
                }

                // Then give every remaining inventory a chance
                for (var inv : secondPassInventories) {
                    if (remaining <= 0) {
                        break;
                    }

                    if (isQueuedForRemoval(inv)) {
                        continue;
                    }

                    remaining -= inv.insert(what, remaining, type, src);
                }
            }

        } finally {
            mountsInUse = false;
        }

        flushQueuedOperations();

        return amount - remaining;
    }

    private void flushQueuedOperations() {
        Preconditions.checkState(!this.mountsInUse);
        var queuedOperations = this.queuedOperations;
        if (queuedOperations != null) {
            this.queuedOperations = null;
            for (var op : queuedOperations) {
                if (op instanceof MountOperation mountOp) {
                    mount(mountOp.priority, mountOp.storage);
                } else if (op instanceof UnmountOperation unmountOp) {
                    unmount(unmountOp.storage);
                } else {
                    throw new IllegalStateException("Unknown operation: " + op);
                }
            }
        }
    }

    private boolean isQueuedForRemoval(MEStorage inv) {
        if (queuedOperations != null) {
            for (var queuedOperation : queuedOperations) {
                if (queuedOperation instanceof UnmountOperation unmountOperation && unmountOperation.storage == inv) {
                    return true;
                }
            }
        }
        return false;
    }

    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (mountsInUse) {
            return 0; // Prevent recursive use
        }

        var extracted = 0L;

        mountsInUse = true;
        try {
            var priorityInventory = this.priorityInventory;
            var entrySet = priorityInventory.int2ObjectEntrySet();
            var last = entrySet.last();
            var iterator = entrySet.iterator(last);
            while (iterator.hasPrevious()) {
                var invList = iterator.previous().getValue();
                var ii = invList.iterator();
                while (ii.hasNext() && extracted < amount) {
                    var inv = ii.next();

                    if (isQueuedForRemoval(inv)) {
                        continue;
                    }

                    extracted += inv.extract(what, amount - extracted, mode, source);
                }
            }
        } finally {
            mountsInUse = false;
        }

        flushQueuedOperations();

        return extracted;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (mountsInUse) {
            return; // Prevent recursive use
        }

        mountsInUse = true;
        try {
            for (var i : this.priorityInventory.values()) {
                for (var j : i) {
                    j.getAvailableStacks(out);
                }
            }
        } finally {
            mountsInUse = false;
        }
    }

    @Override
    public Component getDescription() {
        return GuiText.MENetworkStorage.text();
    }

    sealed interface QueuedOperation permits MountOperation, UnmountOperation {
    }

    private record MountOperation(int priority, MEStorage storage) implements QueuedOperation {
    }

    private record UnmountOperation(MEStorage storage) implements QueuedOperation {
    }
}
