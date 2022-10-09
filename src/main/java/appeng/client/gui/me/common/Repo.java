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

package appeng.client.gui.me.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.stacks.AEKey;
import appeng.client.gui.me.search.RepoSearch;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AELog;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.util.prioritylist.IPartitionList;

/**
 * For showing the network content of a storage channel, this class will maintain a client-side copy of the current
 * server-side storage, which is continuously synchronized to the client while it is open.
 */
public class Repo implements IClientRepo {

    public static final Comparator<GridInventoryEntry> AMOUNT_ASC = Comparator
            .comparingDouble((GridInventoryEntry entry) -> ((double) entry.getStoredAmount())
                    / ((double) entry.getWhat().getAmountPerUnit()));

    public static final Comparator<GridInventoryEntry> AMOUNT_DESC = AMOUNT_ASC.reversed();

    private static final Comparator<GridInventoryEntry> PINNED_ROW_COMPARATOR = Comparator.comparing(entry -> {
        var pinInfo = PinnedKeys.getPinInfo(entry.getWhat());
        return pinInfo != null ? pinInfo.since : Instant.MAX;
    });

    private int rowSize = 9;

    private boolean hasPower;

    private final BiMap<Long, GridInventoryEntry> entries = HashBiMap.create();
    private final ArrayList<GridInventoryEntry> view = new ArrayList<>();
    private final ArrayList<GridInventoryEntry> pinnedRow = new ArrayList<>();
    private final RepoSearch search = new RepoSearch();
    private IPartitionList partitionList;
    private Runnable updateViewListener;

    private final IScrollSource src;
    private final ISortSource sortSrc;
    private boolean paused;

    public Repo(IScrollSource src, ISortSource sortSrc) {
        this.src = src;
        this.sortSrc = sortSrc;
    }

    public void setPartitionList(IPartitionList partitionList) {
        if (partitionList != this.partitionList) {
            this.partitionList = partitionList;
            this.updateView();
        }
    }

    @Override
    public final void handleUpdate(boolean fullUpdate, List<GridInventoryEntry> entries) {
        if (fullUpdate) {
            clear();
        }

        for (var entry : entries) {
            handleUpdate(entry);
        }

        updateView();
    }

    private void handleUpdate(GridInventoryEntry serverEntry) {

        var localEntry = entries.get(serverEntry.getSerial());
        if (localEntry == null) {
            // First time we're seeing this serial -> create new entry
            if (serverEntry.getWhat() == null) {
                AELog.warn("First time seeing serial %s, but incomplete info received", serverEntry.getSerial());
                return;
            }
            if (serverEntry.isMeaningful()) {
                entries.put(serverEntry.getSerial(), serverEntry);
            }
            return;
        }

        // Update the local entry
        if (!serverEntry.isMeaningful()) {
            entries.remove(serverEntry.getSerial());
        } else if (serverEntry.getWhat() == null) {
            entries.put(serverEntry.getSerial(), new GridInventoryEntry(
                    serverEntry.getSerial(),
                    localEntry.getWhat(),
                    serverEntry.getStoredAmount(),
                    serverEntry.getRequestableAmount(),
                    serverEntry.isCraftable()));
        } else {
            entries.put(serverEntry.getSerial(), serverEntry);
        }
    }

    public final void updateView() {
        // While the view is paused, we try to only append to the view list in order to avoid mis-clicks by the
        // player due to items shifting under their mouse cursor.
        if (isPaused()) {
            // First pass -> detect and update
            var visibleSerials = new LongOpenHashSet(this.view.size());
            updateEntriesWhilePaused(pinnedRow, visibleSerials);
            updateEntriesWhilePaused(view, visibleSerials);

            var entriesToAdd = new ArrayList<GridInventoryEntry>();

            // Determine what to do with server entries that are not currently being shown
            for (var serverEntry : entries.values()) {
                if (visibleSerials.contains(serverEntry.getSerial())) {
                    continue; // Entry is already visible
                }

                // First, try to find an empty/meaningless slot in the view that is visually indistinguishable
                // and fill it
                if (takeOverSlotOccupiedByRemovedItem(serverEntry, pinnedRow)
                        || takeOverSlotOccupiedByRemovedItem(serverEntry, view)) {
                    continue;
                }

                // if we couldn't take over an existing slot, just append it
                entriesToAdd.add(serverEntry);
            }

            addEntriesToView(entriesToAdd);
        } else {
            this.view.clear();
            this.pinnedRow.clear();

            this.view.ensureCapacity(this.entries.size());
            this.pinnedRow.ensureCapacity(rowSize);

            addEntriesToView(this.entries.values());
        }

        // Don't re-sort while being paused
        if (!isPaused()) {
            // Sort older entries first in the pinned row
            pinnedRow.sort(PINNED_ROW_COMPARATOR);

            var sortOrder = this.sortSrc.getSortBy();
            var sortDir = this.sortSrc.getSortDir();

            this.view.sort(getComparator(sortOrder, sortDir));
        }

        if (this.updateViewListener != null) {
            this.updateViewListener.run();
        }
    }

    private void addEntriesToView(Collection<GridInventoryEntry> entries) {
        var viewMode = this.sortSrc.getSortDisplay();
        var typeFilter = this.sortSrc.getTypeFilter().getFilter();

        var hasPinnedRow = !PinnedKeys.isEmpty();

        for (var entry : entries) {
            // Pinned keys ignore all filters & search
            if (hasPinnedRow && pinnedRow.size() < rowSize && PinnedKeys.isPinned(entry.getWhat())) {
                pinnedRow.add(entry);
                continue;
            }

            if (this.partitionList != null && !this.partitionList.isListed(entry.getWhat())) {
                continue;
            }

            if (viewMode == ViewItems.CRAFTABLE && !entry.isCraftable()) {
                continue;
            }

            if (viewMode == ViewItems.STORED && entry.getStoredAmount() == 0) {
                continue;
            }

            if (!typeFilter.matches(entry.getWhat())) {
                continue;
            }

            if (search.matches(entry)) {
                this.view.add(entry);
            }
        }

        // Any pinned entry that has not yet been added to the pinned row will be represented by a fake
        // entry. Pinned crafting jobs are excluded from this because they *should* have a grid-entry
        // with craftable=true if they're craftable on this grid.
        if (hasPinnedRow) {
            for (var pinnedKey : PinnedKeys.getPinnedKeys()) {
                var info = PinnedKeys.getPinInfo(pinnedKey);
                if (info.reason != PinnedKeys.PinReason.CRAFTING
                        && pinnedRow.stream().noneMatch(r -> pinnedKey.equals(r.getWhat()))) {
                    this.pinnedRow.add(new GridInventoryEntry(
                            -1, pinnedKey, 0, 0, false));
                }
            }
        }
    }

    private void updateEntriesWhilePaused(List<GridInventoryEntry> shownEntries, LongSet visibleSerials) {
        for (int i = 0; i < shownEntries.size(); i++) {
            var entry = shownEntries.get(i);

            // Update entries with data from server, which doesn't move them
            var serverEntry = entries.get(entry.getSerial());
            if (serverEntry == null) {
                // The server has removed the entry. Let's replace it with an entry that is not meaningful, but shows
                // amount 0
                entry = new GridInventoryEntry(
                        entry.getSerial(),
                        entry.getWhat(),
                        0,
                        0,
                        false);
            } else {
                entry = serverEntry;
            }

            visibleSerials.add(entry.getSerial());
            shownEntries.set(i, entry);
        }
    }

    private boolean takeOverSlotOccupiedByRemovedItem(GridInventoryEntry serverEntry, List<GridInventoryEntry> slots) {
        for (int i = 0; i < slots.size(); i++) {
            var entry = slots.get(i);
            if (!entries.containsKey(entry.getSerial()) && entry.getWhat().equals(serverEntry.getWhat())) {
                slots.set(i, serverEntry);
                return true;
            }
        }
        return false;
    }

    private Comparator<? super GridInventoryEntry> getComparator(SortOrder sortOrder, SortDir sortDir) {
        if (sortOrder == SortOrder.AMOUNT) {
            return sortDir == SortDir.ASCENDING ? AMOUNT_ASC : AMOUNT_DESC;
        }

        return Comparator.comparing(GridInventoryEntry::getWhat, getKeyComparator(sortOrder, sortDir));
    }

    public List<GridInventoryEntry> getPinnedEntries() {
        return Collections.unmodifiableList(this.pinnedRow);
    }

    @Nullable
    public final GridInventoryEntry get(int idx) {
        if (!this.pinnedRow.isEmpty()) {
            // First row of slots is reserved for pinned keys
            if (idx < this.rowSize) {
                if (idx < this.pinnedRow.size()) {
                    return this.pinnedRow.get(idx);
                }
                return null;
            }
            idx -= this.rowSize;
        }

        idx += this.src.getCurrentScroll() * this.rowSize;

        if (idx >= this.view.size()) {
            return null;
        }
        return this.view.get(idx);
    }

    public final int size() {
        return this.view.size() + this.pinnedRow.size();
    }

    public final void clear() {
        this.entries.clear();
        this.view.clear();
        this.pinnedRow.clear();
    }

    public final boolean hasPinnedRow() {
        return !this.pinnedRow.isEmpty();
    }

    public final boolean hasPower() {
        return this.hasPower;
    }

    public final void setPower(boolean hasPower) {
        this.hasPower = hasPower;
    }

    public final int getRowSize() {
        return this.rowSize;
    }

    public final void setRowSize(int rowSize) {
        this.rowSize = rowSize;
    }

    public final String getSearchString() {
        return this.search.getSearchString();
    }

    public final void setSearchString(String searchString) {
        this.search.setSearchString(searchString);
    }

    private Comparator<AEKey> getKeyComparator(SortOrder sortBy, SortDir sortDir) {
        return KeySorters.getComparator(sortBy, sortDir);
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        if (this.paused != paused) {
            this.paused = paused;
            AELog.debug("Toggling client-repo pause mode to %s", this.paused);
            if (!paused) {
                updateView(); // resort on unpause
            }
        }
    }

    @Override
    public Set<GridInventoryEntry> getAllEntries() {
        return entries.values();
    }

    public final void setUpdateViewListener(Runnable updateViewListener) {
        this.updateViewListener = updateViewListener;
    }

    /**
     * Checks if the repo knows that the given key can be crafted.
     */
    public boolean isCraftable(AEKey what) {
        for (var entry : entries.values()) {
            if (entry.isCraftable() && what.equals(entry.getWhat())) {
                return true;
            }
        }
        return false;
    }
}
