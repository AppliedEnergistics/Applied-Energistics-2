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
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.util.prioritylist.DefaultPriorityList;
import appeng.util.prioritylist.IPartitionList;

public class MEInventoryHandler<T extends IAEStack> implements IMEInventoryHandler<T> {

    private final IStorageChannel<T> channel;
    private IMEInventory<T> internal;
    private IMEInventoryHandler<T> delegate;
    private int myPriority;
    private IncludeExclude myWhitelist;
    private AccessRestriction myAccess;
    private IPartitionList<T> myPartitionList;
    private boolean filterOnExtraction;
    private boolean filterAvailableContents;

    private AccessRestriction cachedAccessRestriction;
    private boolean hasReadAccess;
    private boolean hasWriteAccess;

    private boolean gettingAvailableContent = false;

    public MEInventoryHandler(final IMEInventory<T> i) {
        this.channel = i.getChannel();
        this.setInternal(i);
        this.reset();
    }

    public void setInternal(IMEInventory<T> i) {
        if (i instanceof IMEInventoryHandler) {
            this.delegate = (IMEInventoryHandler<T>) i;
        } else {
            this.delegate = new MEPassThrough<>(i, channel);
        }
        this.internal = i;

        if (cachedAccessRestriction != null) {
            // Update cached access restriction
            setBaseAccess(this.myAccess);
        }
    }

    public IMEInventory<T> getInternal() {
        return this.internal;
    }

    public void reset() {
        this.myPriority = 0;
        this.myWhitelist = IncludeExclude.WHITELIST;
        this.setBaseAccess(AccessRestriction.READ_WRITE);
        this.myPartitionList = new DefaultPriorityList<>();
    }

    protected IncludeExclude getWhitelist() {
        return this.myWhitelist;
    }

    public void setWhitelist(final IncludeExclude myWhitelist) {
        this.myWhitelist = myWhitelist;
    }

    public AccessRestriction getBaseAccess() {
        return this.myAccess;
    }

    public void setBaseAccess(final AccessRestriction myAccess) {
        this.myAccess = myAccess;
        this.cachedAccessRestriction = this.myAccess.restrictPermissions(this.delegate.getAccess());
        this.hasReadAccess = this.cachedAccessRestriction.hasPermission(AccessRestriction.READ);
        this.hasWriteAccess = this.cachedAccessRestriction.hasPermission(AccessRestriction.WRITE);
    }

    protected IPartitionList<T> getPartitionList() {
        return this.myPartitionList;
    }

    public void setPartitionList(final IPartitionList<T> myPartitionList) {
        this.myPartitionList = myPartitionList;
    }

    public void setExtractFiltering(boolean filterOnExtraction, boolean filterAvailableContents) {
        this.filterOnExtraction = filterOnExtraction;
        this.filterAvailableContents = filterAvailableContents;
    }

    protected boolean canExtract(T request) {
        if (!this.hasReadAccess) {
            return false;
        }
        if (this.myWhitelist == IncludeExclude.WHITELIST
                && (this.myPartitionList.isEmpty() || this.myPartitionList.isListed(request))) {
            return true;
        } else if (this.myWhitelist == IncludeExclude.BLACKLIST && !this.myPartitionList.isListed(request)) {
            return true;
        }
        return false;
    }

    @Override
    public T injectItems(final T input, final Actionable type, final IActionSource src) {
        if (!this.canAccept(input)) {
            return input;
        }

        return this.delegate.injectItems(input, type, src);
    }

    @Override
    public T extractItems(final T request, final Actionable type, final IActionSource src) {
        if (this.filterOnExtraction && !canExtract(request)) {
            return null;
        }

        return this.delegate.extractItems(request, type, src);
    }

    @Override
    public IAEStackList<T> getAvailableItems(final IAEStackList<T> out) {
        if (this.gettingAvailableContent) {
            // Prevent recursion in case the internal inventory somehow calls this when the available items are queried.
            // This is handled by the NetworkInventoryHandler when the initial query is coming from the network.
            // However, this function might be called from the storage bus code directly,
            // so we have to do this check manually.
            return out;
        }

        this.gettingAvailableContent = true;
        try {
            if (!this.filterAvailableContents) {
                return this.delegate.getAvailableItems(out);
            } else {
                if (!this.hasReadAccess) {
                    return out;
                }
                // Don't try to check use the network storage list if this.delegate is an MEMonitor!
                // The storage list doesn't properly handle recursion!
                for (var stack : this.delegate.getAvailableItems()) {
                    if (canExtract(stack)) {
                        // We use addStorage because MEMonitorPassThrough#getStorageList() does not filter craftable
                        // items!
                        out.addStorage(stack);
                    }
                }
                return out;
            }
        } finally {
            this.gettingAvailableContent = false;
        }
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return this.delegate.getChannel();
    }

    @Override
    public AccessRestriction getAccess() {
        return this.cachedAccessRestriction;
    }

    @Override
    public boolean isPrioritized(final T input) {
        if (this.myWhitelist == IncludeExclude.WHITELIST) {
            return this.myPartitionList.isListed(input) || this.delegate.isPrioritized(input);
        }
        return false;
    }

    @Override
    public boolean canAccept(final T input) {
        if (!this.hasWriteAccess) {
            return false;
        }

        if (this.myWhitelist == IncludeExclude.BLACKLIST && this.myPartitionList.isListed(input)) {
            return false;
        }
        if (this.myPartitionList.isEmpty() || this.myWhitelist == IncludeExclude.BLACKLIST) {
            return this.delegate.canAccept(input);
        }
        return this.myPartitionList.isListed(input) && this.delegate.canAccept(input);
    }

    @Override
    public int getPriority() {
        return this.myPriority;
    }

    public void setPriority(final int myPriority) {
        this.myPriority = myPriority;
    }
}
