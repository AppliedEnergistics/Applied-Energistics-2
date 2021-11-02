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
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityService;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.me.service.SecurityService;

/**
 * Manages all available {@link IMEInventoryHandler} on the network.
 */
public class NetworkInventory<T extends IAEStack> {

    private static final ThreadLocal<Deque<NetworkInventory<?>>> DEPTH_MOD = new ThreadLocal<>();
    private static final ThreadLocal<Deque<NetworkInventory<?>>> DEPTH_SIM = new ThreadLocal<>();
    private static final Comparator<Integer> PRIORITY_SORTER = (o1, o2) -> Integer.compare(o2, o1);

    private static int currentPass = 0;
    private final SecurityService security;
    private final NavigableMap<Integer, List<IMEInventoryHandler<T>>> priorityInventory;
    private int myPass = 0;

    public NetworkInventory(SecurityService security) {
        this.security = security;
        this.priorityInventory = new TreeMap<>(PRIORITY_SORTER);
    }

    public void addNewStorage(final IMEInventoryHandler<T> h) {
        this.priorityInventory.computeIfAbsent(h.getPriority(), k -> new ArrayList<>()).add(h);
    }

    public T injectItems(T input, final Actionable type, final IActionSource src) {
        if (this.diveList(type)) {
            return input;
        }

        if (this.testPermission(src, SecurityPermissions.INJECT)) {
            this.surface(type);
            return input;
        }

        for (final List<IMEInventoryHandler<T>> invList : this.priorityInventory.values()) {
            Iterator<IMEInventoryHandler<T>> ii = invList.iterator();
            while (ii.hasNext() && input != null) {
                final IMEInventoryHandler<T> inv = ii.next();

                if (inv.validForPass(1) && inv.canAccept(input)
                        && (inv.isPrioritized(input) || inv.extractItems(input, Actionable.SIMULATE, src) != null)) {
                    input = inv.injectItems(input, type, src);
                }
            }

            // We need to ignore prioritized inventories in the second pass. If they were
            // not able to store everything
            // during the first pass, they will do so in the second, but as this is
            // stateless we will just report twice
            // the amount of storable items.
            // ignores craftingcache on the second pass.
            ii = invList.iterator();
            while (ii.hasNext() && input != null) {
                final IMEInventoryHandler<T> inv = ii.next();

                if (inv.validForPass(2) && inv.canAccept(input) && !inv.isPrioritized(input)) {
                    input = inv.injectItems(input, type, src);
                }
            }
        }

        this.surface(type);

        return input;
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
            final IGridNode n = src.machine().get().getActionableNode();
            if (n == null) {
                return true;
            }

            final IGrid gn = n.getGrid();
            if (gn != this.security.getGrid()) {

                final ISecurityService sg = gn.getService(ISecurityService.class);
                final int playerID = sg.getOwner();

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

    private Deque<NetworkInventory<?>> getDepth(final Actionable type) {
        var depth = type == Actionable.MODULATE ? DEPTH_MOD : DEPTH_SIM;

        var s = depth.get();

        if (s == null) {
            depth.set(s = new ArrayDeque<>());
        }

        return s;
    }

    public T extractItems(T request, final Actionable mode, final IActionSource src) {
        if (this.diveList(mode)) {
            return null;
        }

        if (this.testPermission(src, SecurityPermissions.EXTRACT)) {
            this.surface(mode);
            return null;
        }

        final Iterator<List<IMEInventoryHandler<T>>> i = this.priorityInventory.descendingMap().values().iterator();

        final T output = IAEStack.copy(request);
        request = IAEStack.copy(request);
        output.setStackSize(0);
        final long req = request.getStackSize();

        while (i.hasNext()) {
            final List<IMEInventoryHandler<T>> invList = i.next();

            final Iterator<IMEInventoryHandler<T>> ii = invList.iterator();
            while (ii.hasNext() && output.getStackSize() < req) {
                final IMEInventoryHandler<T> inv = ii.next();

                request.setStackSize(req - output.getStackSize());
                IAEStack.add(output, inv.extractItems(request, mode, src));
            }
        }

        this.surface(mode);

        if (output.getStackSize() <= 0) {
            return null;
        }

        return output;
    }

    public IAEStackList<T> getAvailableItems(IAEStackList<T> out) {
        if (diveIteration(Actionable.SIMULATE)) {
            return out;
        }

        for (var i : this.priorityInventory.values()) {
            for (final IMEInventoryHandler<T> j : i) {
                out = j.getAvailableStacks(out);
            }
        }

        this.surface(Actionable.SIMULATE);

        return out;
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
