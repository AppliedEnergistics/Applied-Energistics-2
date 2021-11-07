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

import java.util.Objects;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IConfigurableMEInventory;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.util.prioritylist.DefaultPriorityList;
import appeng.util.prioritylist.IPartitionList;

public class MEInventoryHandler<T extends IAEStack> implements IConfigurableMEInventory<T> {

    private IMEInventory<T> inventory;
    private int priority;
    private IPartitionList<T> partitionList = new DefaultPriorityList<>();
    private IncludeExclude partitionListMode = IncludeExclude.WHITELIST;
    private boolean filterOnExtraction;
    private boolean filterAvailableContents;

    private AccessRestriction maxAccess = AccessRestriction.READ_WRITE;
    private AccessRestriction effectiveAccess;
    private boolean allowsExtraction;
    private boolean allowsInsertion;

    private boolean gettingAvailableContent = false;

    public MEInventoryHandler(IMEInventory<T> inventory) {
        setInventory(Objects.requireNonNull(inventory));
    }

    /**
     * Changes which inventory is wrapped by this handler.
     */
    public void setInventory(IMEInventory<T> inventory) {
        this.inventory = inventory;

        // Update access restrictions, since the inventory could allow a different level of access
        setMaxAccess(maxAccess);
    }

    /**
     * @return The inventory this handler is wrapping.
     */
    public IMEInventory<T> getInventory() {
        return this.inventory;
    }

    protected IncludeExclude getWhitelist() {
        return this.partitionListMode;
    }

    public void setWhitelist(final IncludeExclude myWhitelist) {
        this.partitionListMode = myWhitelist;
    }

    public AccessRestriction getMaxAccess() {
        return maxAccess;
    }

    /**
     * Sets the maximum access this handler will allow to the underlying inventory. It might allow even less if the
     * delegated {@link #getInventory()} is an {@link IConfigurableMEInventory} itself, and allows even less access.
     */
    public void setMaxAccess(AccessRestriction maxAccess) {
        this.maxAccess = maxAccess;

        AccessRestriction inventoryAccess;
        if (inventory instanceof NullInventory) {
            // This enables a fast-path of sorts that disables insert/extract for null inventories
            inventoryAccess = AccessRestriction.NO_ACCESS;
        } else if (inventory instanceof IConfigurableMEInventory<T>handler) {
            // If the delegate inventory is itself a handler, we will respect its reported access
            inventoryAccess = handler.getAccess();
        } else {
            inventoryAccess = AccessRestriction.READ_WRITE;
        }

        this.effectiveAccess = maxAccess.restrictPermissions(inventoryAccess);
        this.allowsExtraction = this.effectiveAccess.hasPermission(AccessRestriction.READ);
        this.allowsInsertion = this.effectiveAccess.hasPermission(AccessRestriction.WRITE);
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
        if (!this.canAccept(input)) {
            return input;
        }

        return this.inventory.injectItems(input, type, src);
    }

    @Override
    public T extractItems(final T request, final Actionable type, final IActionSource src) {
        if (this.filterOnExtraction && !canExtract(request)) {
            return null;
        }

        return this.inventory.extractItems(request, type, src);
    }

    @Override
    public IAEStackList<T> getAvailableStacks(final IAEStackList<T> out) {
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
                return this.inventory.getAvailableStacks(out);
            } else {
                if (!this.allowsExtraction) {
                    return out;
                }
                // Don't try to check use the network storage list if this.delegate is an MEMonitor!
                // The storage list doesn't properly handle recursion!
                for (var stack : this.inventory.getAvailableStacks()) {
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
        return this.inventory.getChannel();
    }

    @Override
    public AccessRestriction getAccess() {
        return this.effectiveAccess;
    }

    @Override
    public boolean isPreferredStorageFor(T input, IActionSource source) {
        if (this.partitionListMode == IncludeExclude.WHITELIST) {
            if (this.partitionList.isListed(input)) {
                return true;
            }

            // If the delegate inventory is itself a handler, it might also prioritize the input
            if (this.inventory instanceof IConfigurableMEInventory<T>handler) {
                return handler.isPreferredStorageFor(input, source);
            }
        }

        // Inventories that already contain some equal stack are also preferred
        // we use a copy of size 1 here to prevent inventories from attempting to query multiple sub-inventories
        var extractTest = input;
        if (extractTest.getStackSize() != 1) {
            extractTest = IAEStack.copy(extractTest, 1);
        }
        if (this.inventory.extractItems(extractTest, Actionable.SIMULATE, source) != null) {
            return true;
        }

        return false;
    }

    protected boolean canExtract(T request) {
        return allowsExtraction && passesBlackOrWhitelist(request);
    }

    @Override
    public boolean canAccept(final T input) {
        if (!this.allowsInsertion || !passesBlackOrWhitelist(input)) {
            return false;
        }

        // If the delegate inventory is a handler itself, allow it to reject the input
        if (this.inventory instanceof IConfigurableMEInventory<T>handler) {
            return handler.canAccept(input);
        }
        return true;
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

    @Override
    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
