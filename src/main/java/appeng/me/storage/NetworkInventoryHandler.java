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


import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cache.SecurityCache;

import java.util.*;


public class NetworkInventoryHandler<T extends IAEStack<T>> implements IMEInventoryHandler<T> {

    private static final ThreadLocal<Deque> DEPTH_MOD = new ThreadLocal<>();
    private static final ThreadLocal<Deque> DEPTH_SIM = new ThreadLocal<>();
    private static final Comparator<Integer> PRIORITY_SORTER = (o1, o2) -> Integer.compare(o2, o1);

    private static int currentPass = 0;
    private final IStorageChannel<T> myChannel;
    private final SecurityCache security;
    private final NavigableMap<Integer, List<IMEInventoryHandler<T>>> priorityInventory;
    private int myPass = 0;

    public NetworkInventoryHandler(final IStorageChannel<T> chan, final SecurityCache security) {
        this.myChannel = chan;
        this.security = security;
        this.priorityInventory = new TreeMap<>(PRIORITY_SORTER);
    }

    public void addNewStorage(final IMEInventoryHandler<T> h) {
        final int priority = h.getPriority();
        List<IMEInventoryHandler<T>> list = this.priorityInventory.get(priority);
        if (list == null) {
            this.priorityInventory.put(priority, list = new ArrayList<>());
        }

        list.add(h);
    }

    @Override
    public T injectItems(T input, final Actionable type, final IActionSource src) {
        if (this.diveList(this, type)) {
            return input;
        }

        if (this.testPermission(src, SecurityPermissions.INJECT)) {
            this.surface(this, type);
            return input;
        }

        for (final List<IMEInventoryHandler<T>> invList : this.priorityInventory.values()) {
            Iterator<IMEInventoryHandler<T>> ii = invList.iterator();
            while (ii.hasNext() && input != null) {
                final IMEInventoryHandler<T> inv = ii.next();

                if (inv.validForPass(1) && inv
                        .canAccept(input) && (inv.isPrioritized(input) || inv.extractItems(input, Actionable.SIMULATE, src) != null)) {
                    input = inv.injectItems(input, type, src);
                }
            }

            // We need to ignore prioritized inventories in the second pass. If they were not able to store everything
            // during the first pass, they will do so in the second, but as this is stateless we will just report twice
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

        this.surface(this, type);

        return input;
    }

    private boolean diveList(final NetworkInventoryHandler<T> networkInventoryHandler, final Actionable type) {
        final Deque cDepth = this.getDepth(type);
        if (cDepth.contains(networkInventoryHandler)) {
            return true;
        }

        cDepth.push(this);
        return false;
    }

    private boolean testPermission(final IActionSource src, final SecurityPermissions permission) {
        if (src.player().isPresent()) {
            return !this.security.hasPermission(src.player().get(), permission);
        } else if (src.machine().isPresent()) {
            if (this.security.isAvailable()) {
                final IGridNode n = src.machine().get().getActionableNode();
                if (n == null) {
                    return true;
                }

                final IGrid gn = n.getGrid();
                if (gn != this.security.getGrid()) {

                    final ISecurityGrid sg = gn.getCache(ISecurityGrid.class);
                    final int playerID = sg.getOwner();

                    return !this.security.hasPermission(playerID, permission);
                }
            }
        }

        return false;
    }

    private void surface(final NetworkInventoryHandler<T> networkInventoryHandler, final Actionable type) {
        if (this.getDepth(type).pop() != this) {
            throw new IllegalStateException("Invalid Access to Networked Storage API detected.");
        }
    }

    private Deque getDepth(final Actionable type) {
        final ThreadLocal<Deque> depth = type == Actionable.MODULATE ? DEPTH_MOD : DEPTH_SIM;

        Deque s = depth.get();

        if (s == null) {
            depth.set(s = new ArrayDeque<>());
        }

        return s;
    }

    @Override
    public T extractItems(T request, final Actionable mode, final IActionSource src) {
        if (this.diveList(this, mode)) {
            return null;
        }

        if (this.testPermission(src, SecurityPermissions.EXTRACT)) {
            this.surface(this, mode);
            return null;
        }

        final Iterator<List<IMEInventoryHandler<T>>> i = this.priorityInventory.descendingMap().values().iterator();// priorityInventory.asMap().descendingMap().entrySet().iterator();

        final T output = request.copy();
        request = request.copy();
        output.setStackSize(0);
        final long req = request.getStackSize();

        while (i.hasNext()) {
            final List<IMEInventoryHandler<T>> invList = i.next();

            final Iterator<IMEInventoryHandler<T>> ii = invList.iterator();
            while (ii.hasNext() && output.getStackSize() < req) {
                final IMEInventoryHandler<T> inv = ii.next();

                request.setStackSize(req - output.getStackSize());
                output.add(inv.extractItems(request, mode, src));
            }
        }

        this.surface(this, mode);

        if (output.getStackSize() <= 0) {
            return null;
        }

        return output;
    }

    @Override
    public IItemList<T> getAvailableItems(IItemList<T> out) {
        if (this.diveIteration(this, Actionable.SIMULATE)) {
            return out;
        }

        // for (Entry<Integer, IMEInventoryHandler<T>> h : priorityInventory.entries())
        for (final List<IMEInventoryHandler<T>> i : this.priorityInventory.values()) {
            for (final IMEInventoryHandler<T> j : i) {
                out = j.getAvailableItems(out);
            }
        }

        this.surface(this, Actionable.SIMULATE);

        return out;
    }

    private boolean diveIteration(final NetworkInventoryHandler<T> networkInventoryHandler, final Actionable type) {
        final Deque cDepth = this.getDepth(type);
        if (cDepth.isEmpty()) {
            currentPass++;
            this.myPass = currentPass;
        } else {
            if (currentPass == this.myPass) {
                return true;
            } else {
                this.myPass = currentPass;
            }
        }

        cDepth.push(this);
        return false;
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return this.myChannel;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(final T input) {
        return false;
    }

    @Override
    public boolean canAccept(final T input) {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(final int i) {
        return true;
    }
}
