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

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.me.service.SecurityService;

/**
 * Manages all available {@link IConfigurableMEInventory} on the network.
 */
public class NetworkStorage<T extends AEKey> implements IMEInventory<T> {

    private static final ThreadLocal<Deque<NetworkStorage<?>>> DEPTH_MOD = new ThreadLocal<>();
    private static final ThreadLocal<Deque<NetworkStorage<?>>> DEPTH_SIM = new ThreadLocal<>();
    private static final Comparator<Integer> PRIORITY_SORTER = (o1, o2) -> Integer.compare(o2, o1);

    private static int currentPass = 0;

    private final IStorageChannel<T> channel;
    private final SecurityService security;
    private final NavigableMap<Integer, List<IMEInventory<T>>> priorityInventory;
    private final List<IMEInventory<T>> secondPassInventories = new ArrayList<>();
    private int myPass = 0;

    public NetworkStorage(IStorageChannel<T> channel, SecurityService security) {
        this.channel = channel;
        this.security = security;
        this.priorityInventory = new TreeMap<>(PRIORITY_SORTER);
    }

    public void mount(int priority, IMEInventory<T> inventory) {
        this.priorityInventory.computeIfAbsent(priority, k -> new ArrayList<>())
                .add(inventory);
    }

    public void unmount(IMEInventory<T> inventory) {
        var prioIt = this.priorityInventory.entrySet().iterator();
        while (prioIt.hasNext()) {
            var prioEntry = prioIt.next();

            var inventories = prioEntry.getValue();
            if (inventories.remove(inventory) && inventories.isEmpty()) {
                prioIt.remove();
            }
        }
    }

    public long insert(T what, long amount, final Actionable type, final IActionSource src) {
        if (this.diveList(type)) {
            return 0;
        }

        if (this.testPermission(src, SecurityPermissions.INJECT)) {
            return 0;
        }

        var remaining = amount;
        for (var invList : this.priorityInventory.values()) {
            secondPassInventories.clear();

            // First give every inventory a chance to accept the item if it's preferential storage for the given stack
            var ii = invList.iterator();
            while (ii.hasNext() && remaining > 0) {
                var inv = ii.next();

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

                remaining -= inv.insert(what, remaining, type, src);
            }
        }

        this.surface(type);

        return amount - remaining;
    }

    private boolean diveList(Actionable type) {
        var cDepth = this.getDepth(type);
        if (cDepth.contains(this)) {
            return true;
        }

        cDepth.push(this);
        return false;
    }

    private boolean testPermission(final IActionSource src, final SecurityPermissions permission) {
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

    private Deque<NetworkStorage<?>> getDepth(Actionable type) {
        var depth = type == Actionable.MODULATE ? DEPTH_MOD : DEPTH_SIM;

        var s = depth.get();

        if (s == null) {
            depth.set(s = new ArrayDeque<>());
        }

        return s;
    }

    public long extract(T what, long amount, final Actionable mode, final IActionSource source) {
        if (this.diveList(mode)) {
            return 0;
        }

        if (this.testPermission(source, SecurityPermissions.EXTRACT)) {
            this.surface(mode);
            return 0;
        }

        var i = this.priorityInventory.descendingMap().values().iterator();

        var extracted = 0L;
        while (i.hasNext()) {
            var invList = i.next();

            var ii = invList.iterator();
            while (ii.hasNext() && extracted < amount) {
                var inv = ii.next();

                extracted += inv.extract(what, amount - extracted, mode, source);
            }
        }

        this.surface(mode);

        return extracted;
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return channel;
    }

    @Override
    public void getAvailableStacks(KeyCounter<T> out) {
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
}
