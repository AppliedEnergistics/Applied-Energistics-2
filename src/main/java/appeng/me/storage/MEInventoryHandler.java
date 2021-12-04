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
import appeng.api.storage.MEStorage;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.util.prioritylist.DefaultPriorityList;
import appeng.util.prioritylist.IPartitionList;

public class MEInventoryHandler extends DelegatingMEInventory {

    private IPartitionList partitionList = DefaultPriorityList.INSTANCE;
    private IncludeExclude partitionListMode = IncludeExclude.WHITELIST;
    private boolean filterOnExtraction;
    private boolean filterAvailableContents;
    private boolean allowExtraction = true;
    private boolean allowInsertion = true;

    private boolean gettingAvailableContent = false;

    public MEInventoryHandler(MEStorage inventory) {
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

    protected IPartitionList getPartitionList() {
        return this.partitionList;
    }

    public void setPartitionList(final IPartitionList myPartitionList) {
        this.partitionList = myPartitionList;
    }

    public void setExtractFiltering(boolean filterOnExtraction, boolean filterAvailableContents) {
        this.filterOnExtraction = filterOnExtraction;
        this.filterAvailableContents = filterAvailableContents;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!this.allowInsertion || !passesBlackOrWhitelist(what)) {
            return 0;
        }

        return super.insert(what, amount, mode, source);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (this.filterOnExtraction && !canExtract(what)) {
            return 0;
        }

        return super.extract(what, amount, mode, source);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (this.gettingAvailableContent) {
            // Prevent recursion in case the internal inventory somehow calls this when the available items are queried.
            // This is handled by the NetworkInventoryHandler when the initial query is coming from the network.
            // However, this function might be called from the storage bus code directly,
            // so we have to do this check manually.
            return;
        }

        this.gettingAvailableContent = true;
        try {
            if (!this.filterAvailableContents) {
                super.getAvailableStacks(out);
            } else {
                if (!this.allowExtraction) {
                    return;
                }

                for (var entry : getDelegate().getAvailableStacks()) {
                    if (canExtract(entry.getKey())) {
                        out.add(entry.getKey(), entry.getLongValue());
                    }
                }
            }
        } finally {
            this.gettingAvailableContent = false;
        }
    }

    @Override
    public boolean isPreferredStorageFor(AEKey input, IActionSource source) {
        if (this.partitionListMode == IncludeExclude.WHITELIST) {
            if (this.partitionList.isListed(input)) {
                return true;
            }
        }

        // Inventories that already contain some equal stack are also preferred
        // we use a copy of size 1 here to prevent inventories from attempting to query multiple sub-inventories
        if (super.extract(input, 1, Actionable.SIMULATE, source) > 0) {
            return true;
        }

        return super.isPreferredStorageFor(input, source);
    }

    protected boolean canExtract(AEKey request) {
        return allowExtraction && passesBlackOrWhitelist(request);
    }

    // Applies the black/whitelist, but only if any item is listed at all
    private boolean passesBlackOrWhitelist(AEKey input) {
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
