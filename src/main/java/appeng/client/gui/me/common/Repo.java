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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.api.stacks.AEKey;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.integration.abstraction.JEIFacade;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;

/**
 * For showing the network content of a storage channel, this class will maintain a client-side copy of the current
 * server-side storage, which is continuously synchronized to the client while it is open.
 */
public class Repo implements IClientRepo {

    public static final Comparator<GridInventoryEntry> AMOUNT_ASC = Comparator
            .comparingLong(GridInventoryEntry::getStoredAmount);

    public static final Comparator<GridInventoryEntry> AMOUNT_DESC = AMOUNT_ASC.reversed();

    private int rowSize = 9;

    private String searchString = "";
    private boolean hasPower;

    private final BiMap<Long, GridInventoryEntry> entries = HashBiMap.create();
    private final ArrayList<GridInventoryEntry> view = new ArrayList<>();
    private IPartitionList partitionList;
    private Runnable updateViewListener;

    private final IScrollSource src;
    private final ISortSource sortSrc;
    private boolean synchronizeWithJEI;

    public Repo(IScrollSource src, ISortSource sortSrc) {
        this.src = src;
        this.sortSrc = sortSrc;
        setSynchronizeWithJEI(true);
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

        for (GridInventoryEntry entry : entries) {
            handleUpdate(entry);
        }

        updateView();
    }

    private void handleUpdate(GridInventoryEntry serverEntry) {

        GridInventoryEntry localEntry = entries.get(serverEntry.getSerial());
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
        this.view.clear();

        this.view.ensureCapacity(this.entries.size());

        this.updateJEI(this.searchString);

        SearchMode searchMode = SearchMode.NAME;
        if (AEConfig.instance().getSearchTooltips() != YesNo.NO) {
            searchMode = SearchMode.NAME_OR_TOOLTIP;
        }

        String innerSearch = this.searchString;
        if (innerSearch.startsWith("@")) {
            searchMode = SearchMode.MOD;
            innerSearch = innerSearch.substring(1);
        }

        Pattern m;
        try {
            m = Pattern.compile(innerSearch.toLowerCase(),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException ignored) {
            m = Pattern.compile(Pattern.quote(innerSearch.toLowerCase()),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }

        ViewItems viewMode = this.sortSrc.getSortDisplay();

        for (GridInventoryEntry entry : this.entries.values()) {
            if (this.partitionList != null && !this.partitionList.isListed(entry.getWhat())) {
                continue;
            }

            if (viewMode == ViewItems.CRAFTABLE && !entry.isCraftable()) {
                continue;
            }

            if (viewMode == ViewItems.STORED && entry.getStoredAmount() == 0) {
                continue;
            }

            if (matchesSearch(searchMode, m, entry.getWhat())) {
                this.view.add(entry);
            }
        }

        SortOrder sortOrder = this.sortSrc.getSortBy();
        SortDir sortDir = this.sortSrc.getSortDir();

        this.view.sort(getComparator(sortOrder, sortDir));

        if (this.updateViewListener != null) {
            this.updateViewListener.run();
        }
    }

    private Comparator<? super GridInventoryEntry> getComparator(SortOrder sortOrder, SortDir sortDir) {
        if (sortOrder == SortOrder.AMOUNT) {
            return sortDir == SortDir.ASCENDING ? AMOUNT_ASC : AMOUNT_DESC;
        }

        return Comparator.comparing(GridInventoryEntry::getWhat, getKeyComparator(sortOrder, sortDir));
    }

    @Nullable
    public final GridInventoryEntry get(int idx) {
        idx += this.src.getCurrentScroll() * this.rowSize;

        if (idx >= this.view.size()) {
            return null;
        }
        return this.view.get(idx);
    }

    public final int size() {
        return this.view.size();
    }

    public final void clear() {
        this.entries.clear();
        this.view.clear();
    }

    public final boolean hasPower() {
        return this.hasPower;
    }

    public final void setPower(final boolean hasPower) {
        this.hasPower = hasPower;
    }

    public final int getRowSize() {
        return this.rowSize;
    }

    public final void setRowSize(final int rowSize) {
        this.rowSize = rowSize;
    }

    public final String getSearchString() {
        return this.searchString;
    }

    public final void setSearchString(@Nonnull final String searchString) {
        this.searchString = searchString;
    }

    private void updateJEI(String filter) {
        SearchBoxMode searchMode = AEConfig.instance().getTerminalSearchMode();
        if (synchronizeWithJEI && searchMode.isRequiresJei()) {
            JEIFacade.instance().setSearchText(filter);
        }
    }

    protected final void setSynchronizeWithJEI(boolean enable) {
        this.synchronizeWithJEI = enable;
    }

    protected boolean matchesSearch(SearchMode searchMode, Pattern searchPattern, AEKey what) {
        if (searchMode == SearchMode.MOD) {
            return searchPattern.matcher(what.getModId()).find();
        }

        String displayName = what.getDisplayName().getString();
        if (searchPattern.matcher(displayName).find()) {
            return true;
        }

        if (searchMode == SearchMode.NAME_OR_TOOLTIP) {
            var tooltip = Platform.getTooltip(what);

            for (var line : tooltip) {
                if (searchPattern.matcher(line.getString()).find()) {
                    return true;
                }
            }
        }

        return false;
    }

    private Comparator<AEKey> getKeyComparator(SortOrder sortBy, SortDir sortDir) {
        return KeySorters.getComparator(sortBy, sortDir);
    }

    @Override
    public Set<GridInventoryEntry> getAllEntries() {
        return entries.values();
    }

    public final void setUpdateViewListener(Runnable updateViewListener) {
        this.updateViewListener = updateViewListener;
    }

    protected enum SearchMode {
        MOD,
        NAME,
        NAME_OR_TOOLTIP
    }

}
