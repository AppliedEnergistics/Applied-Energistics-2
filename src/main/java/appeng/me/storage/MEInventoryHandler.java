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

import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.util.prioritylist.DefaultPriorityList;
import appeng.util.prioritylist.IPartitionList;

public class MEInventoryHandler<T extends IAEStack> extends DelegatingMEInventory<T> {

    private IPartitionList<T> partitionList = new DefaultPriorityList<>();
    private IncludeExclude partitionListMode = IncludeExclude.WHITELIST;
    private boolean filterOnExtraction;
    private boolean filterAvailableContents;
    private boolean allowExtraction = true;
    private boolean allowInsertion = true;

    private boolean gettingAvailableContent = false;

    public MEInventoryHandler(IMEInventory<T> inventory) {
        super(inventory);
    }

    public void setAllowExtraction(boolean allowExtraction) {
        this.allowExtraction = allowExtraction;
    }

    public void setAllowInsertion(boolean allowInsertion) {
        this.allowInsertion = allowInsertion;
    }

    protected IncludeExclude getWhitelist() {
        return this.partitionListMode;
    }

    public void setWhitelist(final IncludeExclude myWhitelist) {
        this.partitionListMode = myWhitelist;
    }

    protected IPartitionList<T> getPartitionList() {
        return this.partitionList;
    }

    public void setPartitionList(final IPartitionList<T> myPartitionList) {
        this.partitionList = myPartitionList;
    }

    public void setExtractFiltering(boolean filterOnExtraction, boolean filterAvailableContents) {
        this.filterOnExtraction = filterOnExtraction;
        this.filterAvailableContents = filterAvailableContents;
    }

    @Override
    public T injectItems(final T input, final Actionable type, final IActionSource src) {
        if (!this.allowInsertion || !passesBlackOrWhitelist(input)) {
            return input;
        }

        return super.injectItems(input, type, src);
    }

    @Override
    public T extractItems(final T request, final Actionable type, final IActionSource src) {
        if (this.filterOnExtraction && !canExtract(request)) {
            return null;
        }

        return super.extractItems(request, type, src);
    }

    @Override
    public IAEStackList<T> getAvailableStacks(IAEStackList<T> out) {
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
                return super.getAvailableStacks(out);
            } else {
                if (!this.allowExtraction) {
                    return out;
                }
                // Don't try to check use the network storage list if this.delegate is an MEMonitor!
                // The storage list doesn't properly handle recursion!
                for (var stack : getDelegate().getAvailableStacks()) {
                    if (canExtract(stack)) {
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
    public boolean isPreferredStorageFor(T input, IActionSource source) {
        if (this.partitionListMode == IncludeExclude.WHITELIST) {
            if (this.partitionList.isListed(input)) {
                return true;
            }
        }

        // Inventories that already contain some equal stack are also preferred
        // we use a copy of size 1 here to prevent inventories from attempting to query multiple sub-inventories
        var extractTest = input;
        if (extractTest.getStackSize() != 1) {
            extractTest = IAEStack.copy(extractTest, 1);
        }
        if (super.extractItems(extractTest, Actionable.SIMULATE, source) != null) {
            return true;
        }

        return super.isPreferredStorageFor(input, source);
    }

    protected boolean canExtract(T request) {
        return allowExtraction && passesBlackOrWhitelist(request);
    }

    // Applies the black/whitelist, but only if any item is listed at all
    private boolean passesBlackOrWhitelist(T input) {
        if (!this.partitionList.isEmpty()) {
            switch (this.partitionListMode) {
                case WHITELIST -> {
                    if (!this.partitionList.isListed(input)) {
                        return false;
                    }
                }
                case BLACKLIST -> {
                    if (this.partitionList.isListed(input)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
