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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;
import appeng.me.service.SecurityService;

/**
 * Manages all available {@link MEStorage} on the network.
 */
public class NetworkStorage implements MEStorage {
    private static final ThreadLocal<Deque<NetworkStorage>> DEPTH_MOD = new ThreadLocal<>();
    private static final ThreadLocal<Deque<NetworkStorage>> DEPTH_SIM = new ThreadLocal<>();
    private static final Comparator<Integer> PRIORITY_SORTER = (o1, o2) -> Integer.compare(o2, o1);

    private boolean mountsInUse;

    private static int currentPass = 0;

    private final SecurityService security;
    private final NavigableMap<Integer, List<MEStorage>> priorityInventory;
    private final List<MEStorage> secondPassInventories = new ArrayList<>();
    private int myPass = 0;
    // Queued mount/unmount operations that occurred while an insert/extract was ongoing
    // Is only non-null if something is queued
    @Nullable
    private List<QueuedOperation> queuedOperations;

    public NetworkStorage(SecurityService security) {
        this.security = security;
        this.priorityInventory = new TreeMap<>(PRIORITY_SORTER);
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
            var prioIt = this.priorityInventory.entrySet().iterator();
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
        if (this.diveList(type)) {
            return 0;
        }

        if (this.testPermission(src, SecurityPermissions.INJECT)) {
            this.surface(type);
            return 0;
        }

        var remaining = amount;

        this.mountsInUse = true;
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
            this.mountsInUse = false;
        }

        this.surface(type);

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

    private boolean diveList(Actionable type) {
        var cDepth = this.getDepth(type);
        if (cDepth.contains(this)) {
            return true;
        }

        cDepth.push(this);
        return false;
    }

    private boolean testPermission(IActionSource src, SecurityPermissions permission) {
        if (src.player().isPresent()) {
            if (!this.security.hasPermission(src.player().get(), permission)) {
                return true;
            }
        } else if (src.machine().isPresent() && this.security.isAvailable()) {
            var n = src.machine().get().getActionableNode();
            if (n == null) {
                return true;
            }

            var gn = n.getGrid();
            if (gn != this.security.getGrid()) {

                var sg = gn.getSecurityService();
                var playerID = sg.getOwner();

                if (!this.security.hasPermission(playerID, permission)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void surface(Actionable type) {
        if (getDepth(type).pop() != this) {
            throw new IllegalStateException("Invalid Access to Networked Storage API detected.");
        }
    }

    private Deque<NetworkStorage> getDepth(Actionable type) {
        var depth = type == Actionable.MODULATE ? DEPTH_MOD : DEPTH_SIM;

        var s = depth.get();

        if (s == null) {
            depth.set(s = new ArrayDeque<>());
        }

        return s;
    }

    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (this.diveList(mode)) {
            return 0;
        }

        if (this.testPermission(source, SecurityPermissions.EXTRACT)) {
            this.surface(mode);
            return 0;
        }

        var extracted = 0L;

        this.mountsInUse = true;
        try {
            for (var invList : this.priorityInventory.descendingMap().values()) {
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
            this.mountsInUse = false;
        }

        this.surface(mode);

        flushQueuedOperations();

        return extracted;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (diveIteration(Actionable.SIMULATE)) {
            return;
        }

        for (var i : this.priorityInventory.values()) {
            for (var j : i) {
                j.getAvailableStacks(out);
            }
        }

        this.surface(Actionable.SIMULATE);
    }

    private boolean diveIteration(Actionable type) {
        var cDepth = this.getDepth(type);
        if (cDepth.isEmpty()) {
            currentPass++;
        } else if (currentPass == this.myPass) {
            return true;
        }
        this.myPass = currentPass;

        cDepth.push(this);
        return false;
    }

    @Override
    public Component getDescription() {
        return GuiText.MENetworkStorage.text();
    }

    sealed interface QueuedOperation permits MountOperation,UnmountOperation {
    }

    private record MountOperation(int priority, MEStorage storage) implements QueuedOperation {
    }

    private record UnmountOperation(MEStorage storage) implements QueuedOperation {
    }
}
