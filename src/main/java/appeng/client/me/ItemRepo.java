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


import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AEConfig;
import appeng.integration.Integrations;
import appeng.integration.modules.bogosorter.InventoryBogoSortModule;
import appeng.items.storage.ItemViewCell;
import appeng.util.ItemSorters;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;


public class ItemRepo {

    private final IItemList<IAEItemStack> list = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
    private final List<IAEItemStack> view;
    private final IScrollSource src;
    private final ISortSource sortSrc;

    private int rowSize = 9;

    private volatile String searchString = "";
    private IPartitionList<IAEItemStack> myPartitionList;
    private String innerSearch = "";
    private boolean hasPower;

    public ItemRepo(final IScrollSource src, final ISortSource sortSrc) {
        this.src = src;
        this.sortSrc = sortSrc;

        this.view = Collections.synchronizedList(new ArrayList<>());
        list.forEach(this.view::add);
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

    public long getItemCount(final IAEItemStack is) {
        IAEItemStack st = this.list.findPrecise(is);
        return st == null ? 0 : st.getStackSize();
    }

    public void setViewCell(final ItemStack[] list) {
        this.myPartitionList = ItemViewCell.createFilter(list);
        this.updateView();
    }

    private CompletableFuture<Void> searchTask = null;

    public void updateView() {
        if (searchTask != null) {
            return;
        }

        // Since sortSrc is final, so we can safely call it inside lambda
        searchTask = CompletableFuture.supplyAsync(() -> {
            IItemList<IAEItemStack> list = this.list.clone();
            List<IAEItemStack> view = new ArrayList<>(list.size());
            Enum viewMode = this.sortSrc.getSortDisplay();

            boolean needsZeroCopy = viewMode == ViewItems.CRAFTABLE;

            boolean terminalSearchToolTips = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_TOOLTIPS) != YesNo.NO;

            boolean searchMod = false;

            String innerSearch = searchString.toLowerCase();
            if (innerSearch.startsWith("@")) {
                searchMod = true;
                innerSearch = innerSearch.substring(1);
            }

            Pattern m = null;
            try {
                m = Pattern.compile(innerSearch, Pattern.CASE_INSENSITIVE);
            } catch (final Throwable ignore) {
                try {
                    m = Pattern.compile(Pattern.quote(innerSearch), Pattern.CASE_INSENSITIVE);
                } catch (final Throwable __) {
                    return Collections.<IAEItemStack>emptyList();
                }
            }


            for (IAEItemStack is : list) {
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

                final String dspName = (searchMod ? Platform.getModId(is) : Platform.getItemDisplayName(is)).toLowerCase();
                boolean foundMatchingItemStack = true;

                for (String term : innerSearch.split(" ")) {
                    if (term.length() > 1 && (term.startsWith("-") || term.startsWith("!"))) {
                        term = term.substring(1);
                        if (dspName.contains(term)) {
                            foundMatchingItemStack = false;
                            break;
                        }
                    } else if (!dspName.contains(term)) {
                        foundMatchingItemStack = false;
                        break;
                    }
                }

                if (terminalSearchToolTips && !foundMatchingItemStack) {
                    final List<String> tooltip = Platform.getTooltip(is);
                    for (final String line : tooltip) {
                        if (m.matcher(line).find()) {
                            foundMatchingItemStack = true;
                            break;
                        }
                    }
                }

                if (foundMatchingItemStack) {
                    if (needsZeroCopy) {
                        is = is.copy();
                        is.setStackSize(0);
                    }

                    view.add(is);
                }
            }

            final Enum SortBy = this.sortSrc.getSortBy();
            final Enum SortDir = this.sortSrc.getSortDir();

            ItemSorters.setDirection((appeng.api.config.SortDir) SortDir);
            ItemSorters.init();

            if (SortBy == SortOrder.MOD) {
                view.sort(ItemSorters.CONFIG_BASED_SORT_BY_MOD);
            } else if (SortBy == SortOrder.AMOUNT) {
                view.sort(ItemSorters.CONFIG_BASED_SORT_BY_SIZE);
            } else if (SortBy == SortOrder.INVTWEAKS) {
                if (InventoryBogoSortModule.isLoaded()) {
                    Collections.sort(this.view, InventoryBogoSortModule.COMPARATOR);
                } else {
                    Collections.sort(this.view, ItemSorters.CONFIG_BASED_SORT_BY_INV_TWEAKS);
                }
            } else {
                view.sort(ItemSorters.CONFIG_BASED_SORT_BY_NAME);
            }

            return view;
        }).thenAcceptAsync(view -> {
            this.view.clear();
            this.view.addAll(view);
        }).thenRunAsync(() -> {
            this.searchTask = null; // Prevent redundant cancellation
        });


    }

    private void updateJEI(String filter) {
        Integrations.jei().setSearchText(filter);
    }

    public int size() {
        return this.view.size();
    }

    public void clear() {
        if (searchTask != null) {
            searchTask.cancel(true);
            searchTask = null;
        }
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

        if (searchTask != null) {
            searchTask.cancel(true);
            searchTask = null;
        }

        // Passive JEI auto search
        final Enum<?> searchMode = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_MODE);
        if (searchMode == SearchBoxMode.JEI_AUTOSEARCH || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH || searchMode == SearchBoxMode.JEI_AUTOSEARCH_KEEP || searchMode == SearchBoxMode.JEI_MANUAL_SEARCH_KEEP) {
            this.updateJEI(this.searchString);
        }
    }

    public IItemList<IAEItemStack> getList() {
        return list;
    }
}
