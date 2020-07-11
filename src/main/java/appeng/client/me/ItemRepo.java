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

package appeng.client.me;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.SearchBoxMode;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.integration.abstraction.JEIFacade;
import appeng.items.storage.ViewCellItem;
import appeng.util.ItemSorters;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;

public class ItemRepo {

    private final IItemList<IAEItemStack> list = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
            .createList();
    private final ArrayList<IAEItemStack> view = new ArrayList<>();
    private final IScrollSource src;
    private final ISortSource sortSrc;

    private int rowSize = 9;

    private String searchString = "";
    private IPartitionList<IAEItemStack> myPartitionList;
    private String innerSearch = "";
    private boolean hasPower;

    public ItemRepo(final IScrollSource src, final ISortSource sortSrc) {
        this.src = src;
        this.sortSrc = sortSrc;
    }

    public IAEItemStack getReferenceItem(int idx) {
        idx += this.src.getCurrentScroll() * this.rowSize;

        if (idx >= this.view.size()) {
            return null;
        }
        return this.view.get(idx);
    }

    void setSearch(final String search) {
        this.searchString = search == null ? "" : search;
    }

    public void postUpdate(final IAEItemStack is) {
        final IAEItemStack st = this.list.findPrecise(is);

        if (st != null) {
            st.reset();
            st.add(is);
        } else {
            this.list.add(is);
        }
    }

    public void setViewCell(final ItemStack[] list) {
        this.myPartitionList = ViewCellItem.createFilter(list);
        this.updateView();
    }

    public void updateView() {
        this.view.clear();

        this.view.ensureCapacity(this.list.size());

        ViewItems viewMode = this.sortSrc.getSortDisplay();
        SearchBoxMode searchMode = AEConfig.instance().getTerminalSearchMode();
        final boolean needsZeroCopy = viewMode == ViewItems.CRAFTABLE;

        if (searchMode == SearchBoxMode.JEI_AUTOSEARCH || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH
                || searchMode == SearchBoxMode.JEI_AUTOSEARCH_KEEP
                || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH_KEEP) {
            this.updateJEI(this.searchString);
        }

        this.innerSearch = this.searchString;
        final boolean terminalSearchToolTips = AEConfig.instance().getSearchTooltips() != YesNo.NO;

        boolean searchMod = false;
        if (this.innerSearch.startsWith("@")) {
            searchMod = true;
            this.innerSearch = this.innerSearch.substring(1);
        }

        Pattern m = null;
        try {
            m = Pattern.compile(this.innerSearch.toLowerCase(), Pattern.CASE_INSENSITIVE);
        } catch (final Throwable ignore) {
            try {
                m = Pattern.compile(Pattern.quote(this.innerSearch.toLowerCase()), Pattern.CASE_INSENSITIVE);
            } catch (final Throwable __) {
                return;
            }
        }

        boolean notDone = false;
        for (IAEItemStack is : this.list) {
            if (this.myPartitionList != null) {
                if (!this.myPartitionList.isListed(is)) {
                    continue;
                }
            }

            if (viewMode == ViewItems.CRAFTABLE && !is.isCraftable()) {
                continue;
            }

            if (viewMode == ViewItems.STORED && is.getStackSize() == 0) {
                continue;
            }

            final String dspName = searchMod ? Platform.getModId(is) : Platform.getItemDisplayName(is).getString();
            boolean foundMatchingItemStack = false;
            notDone = true;

            if (m.matcher(dspName.toLowerCase()).find()) {
                notDone = false;
                foundMatchingItemStack = true;
            }

            if (terminalSearchToolTips && notDone && !searchMod) {
                final List<ITextComponent> tooltip = Platform.getTooltip(is);

                for (final ITextComponent line : tooltip) {
                    if (m.matcher(line.getString()).find()) {
                        foundMatchingItemStack = true;
                        notDone = false;
                        break;
                    }
                }
            }

            if (foundMatchingItemStack) {
                if (needsZeroCopy) {
                    is = is.copy();
                    is.setStackSize(0);
                }

                this.view.add(is);
            }
        }

        final Enum SortBy = this.sortSrc.getSortBy();
        final Enum SortDir = this.sortSrc.getSortDir();

        ItemSorters.setDirection((appeng.api.config.SortDir) SortDir);

        if (SortBy == SortOrder.MOD) {
            Collections.sort(this.view, ItemSorters.CONFIG_BASED_SORT_BY_MOD);
        } else if (SortBy == SortOrder.AMOUNT) {
            Collections.sort(this.view, ItemSorters.CONFIG_BASED_SORT_BY_SIZE);
        } else {
            Collections.sort(this.view, ItemSorters.CONFIG_BASED_SORT_BY_NAME);
        }
    }

    private void updateJEI(String filter) {
        JEIFacade.instance().setSearchText(filter);
    }

    public int size() {
        return this.view.size();
    }

    public void clear() {
        this.list.resetStatus();
    }

    public boolean hasPower() {
        return this.hasPower;
    }

    public void setPower(final boolean hasPower) {
        this.hasPower = hasPower;
    }

    public int getRowSize() {
        return this.rowSize;
    }

    public void setRowSize(final int rowSize) {
        this.rowSize = rowSize;
    }

    public String getSearchString() {
        return this.searchString;
    }

    public void setSearchString(@Nonnull final String searchString) {
        this.searchString = searchString;
    }
}
