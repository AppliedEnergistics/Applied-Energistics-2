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

package appeng.crafting;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import appeng.util.inv.ItemListIgnoreCrafting;

import javax.annotation.Nullable;

/**
 * An inventory with infinite capacity that can optionally log insertions and extractions, and attempt to apply them to
 * another inventory later by using {@link #commit}.
 * <p>
 * It also completely ignores the {@link IAEStack#isCraftable()} information by using {@link ItemListIgnoreCrafting},
 * because apparently that was necessary to
 * fix <a href="https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/2530">an issue with
 * CraftingCPUClusters using isEmpty() during their cleanup, but that check returning false due to some items being
 * craftable </a> (commit id: a9c5019). This is really sketchy, and needs to be looked at carefully in the future.
 */
public class MECraftingInventory implements IMEInventory<IAEItemStack> {

    /**
     * Actual storage.
     */
    private final IItemList<IAEItemStack> localCache;

    private final boolean logExtracted;
    /**
     * List of extracted items if {@link #logExtracted} is true. Can be applied using {@link #commit}.
     */
    private final IItemList<IAEItemStack> extractedCache;

    private final boolean logInjections;
    /**
     * List of inserted items if {@link #logInjections} is true. Can be applied using {@link #commit}.
     */
    private final IItemList<IAEItemStack> injectedCache;

    /**
     * Internal constructor used to build the various caches.
     */
    private MECraftingInventory(final boolean logExtracted, final boolean logInjections) {
        this.logExtracted = logExtracted;
        this.logInjections = logInjections;

        if (logExtracted) {
            this.extractedCache = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
        } else {
            this.extractedCache = null;
        }

        if (logInjections) {
            this.injectedCache = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();
        } else {
            this.injectedCache = null;
        }

        // Note: we ignore crafting status, this is sketchy as explained above in the class javadoc.
        this.localCache = new ItemListIgnoreCrafting<>(
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList());
    }

    // FIXME CRAFTING This is only called by the cpu cluster during the crafting it seems
    public MECraftingInventory() {
        this(false, false);
    }

    // FIXME CRAFTING This is only called in the constructor of a crafting job tree to copy the stacks from the grid
    public MECraftingInventory(final IMEMonitor<IAEItemStack> target, final IActionSource src,
                               final boolean logExtracted, final boolean logInjections) {
        this(logExtracted, logInjections);

        // Populate initial cache by copying from the target monitor.
        for (final IAEItemStack is : target.getStorageList()) {
            this.localCache.add(target.extractItems(is, Actionable.SIMULATE, src));
        }
    }

    // FIXME CRAFTING This is called by the crafting job at the root, and by crafting tree nodes
    public MECraftingInventory(final IMEInventory<IAEItemStack> target, final boolean logExtracted,
                               final boolean logInjections) {
        this(logExtracted, logInjections);

        // Populate initial cache by copying from the target inventory.
        target.getAvailableItems(this.localCache);
    }

    @Override
    public IAEItemStack injectItems(final IAEItemStack input, final Actionable mode, final IActionSource src) {
        if (input == null) {
            return null;
        }

        if (mode == Actionable.MODULATE) {
            if (this.logInjections) {
                this.injectedCache.add(input);
            }
            this.localCache.add(input);
        }

        return null;
    }

    @Override
    public IAEItemStack extractItems(final IAEItemStack request, final Actionable mode, final IActionSource src) {
        if (request == null) {
            return null;
        }

        final IAEItemStack list = this.localCache.findPrecise(request);
        if (list == null || list.getStackSize() == 0) {
            return null;
        }

        if (list.getStackSize() >= request.getStackSize()) {
            if (mode == Actionable.MODULATE) {
                list.decStackSize(request.getStackSize());
                if (this.logExtracted) {
                    this.extractedCache.add(request);
                }
            }

            return request;
        }

        final IAEItemStack ret = request.copy();
        ret.setStackSize(list.getStackSize());

        if (mode == Actionable.MODULATE) {
            list.reset();
            if (this.logExtracted) {
                this.extractedCache.add(ret);
            }
        }

        return ret;
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(final IItemList<IAEItemStack> out) {
        for (final IAEItemStack is : this.localCache) {
            out.add(is);
        }

        return out;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    public IItemList<IAEItemStack> getItemList() {
        return this.localCache;
    }

    public boolean commit(final IMEInventory<IAEItemStack> target, final IActionSource src) {
        final IItemList<IAEItemStack> added = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                .createList();
        final IItemList<IAEItemStack> pulled = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                .createList();
        boolean failed = false;

        if (this.logInjections) {
            for (final IAEItemStack inject : this.injectedCache) {
                IAEItemStack result = null;
                added.add(result = target.injectItems(inject, Actionable.MODULATE, src));

                if (result != null) {
                    failed = true;
                    break;
                }
            }
        }

        if (failed) {
            for (final IAEItemStack is : added) {
                target.extractItems(is, Actionable.MODULATE, src);
            }

            return false;
        }

        if (this.logExtracted) {
            for (final IAEItemStack extra : this.extractedCache) {
                IAEItemStack result = null;
                pulled.add(result = target.extractItems(extra, Actionable.MODULATE, src));

                if (result == null || result.getStackSize() != extra.getStackSize()) {
                    failed = true;
                    break;
                }
            }
        }

        if (failed) {
            for (final IAEItemStack is : added) {
                target.extractItems(is, Actionable.MODULATE, src);
            }

            for (final IAEItemStack is : pulled) {
                target.injectItems(is, Actionable.MODULATE, src);
            }

            return false;
        }

        return true;
    }

    /**
     * Set the stack size of the target stack to 0.
     */
    void resetStackSize(final IAEItemStack what) {
        final IAEItemStack list = this.localCache.findPrecise(what);
        if (list != null) {
            list.setStackSize(0);
        }
    }
}
