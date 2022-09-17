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


import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.items.contents.CellConfig;
import appeng.util.item.AEItemStack;
import net.minecraft.item.ItemStack;


public class CreativeCellInventory implements IMEInventoryHandler<IAEItemStack> {

    private final IItemList<IAEItemStack> itemListCache = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();

    protected CreativeCellInventory(final ItemStack o) {
        final CellConfig cc = new CellConfig(o);
        for (final ItemStack is : cc) {
            if (!is.isEmpty()) {
                final IAEItemStack i = AEItemStack.fromItemStack(is);
                i.setStackSize(Integer.MAX_VALUE);
                this.itemListCache.add(i);
            }
        }
    }

    public static ICellInventoryHandler getCell(final ItemStack o) {
        return new BasicCellInventoryHandler(new CreativeCellInventory(o), AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
    }

    @Override
    public IAEItemStack injectItems(final IAEItemStack input, final Actionable mode, final IActionSource src) {
        final IAEItemStack local = this.itemListCache.findPrecise(input);
        if (local == null) {
            return input;
        }

        return null;
    }

    @Override
    public IAEItemStack extractItems(final IAEItemStack request, final Actionable mode, final IActionSource src) {
        final IAEItemStack local = this.itemListCache.findPrecise(request);
        if (local == null) {
            return null;
        }

        return request.copy();
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(final IItemList out) {
        for (final IAEItemStack ais : this.itemListCache) {
            out.add(ais);
        }
        return out;
    }

    @Override
    public IStorageChannel getChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(final IAEItemStack input) {
        return this.itemListCache.findPrecise(input) != null;
    }

    @Override
    public boolean canAccept(final IAEItemStack input) {
        return this.itemListCache.findPrecise(input) != null;
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
