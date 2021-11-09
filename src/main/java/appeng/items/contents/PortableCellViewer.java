/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.items.contents;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.me.helpers.MEMonitorHandler;
import appeng.me.storage.NullInventory;
import appeng.menu.interfaces.IInventorySlotAware;
import appeng.util.ConfigManager;

public class PortableCellViewer implements IPortableCell, IInventorySlotAware {
    private final MEMonitorHandler<?> cellMonitor;
    private final ItemStack target;
    private final IAEItemPowerStorage ips;
    private final int inventorySlot;

    public PortableCellViewer(ItemStack is, int slot) {
        IMEInventory<?> inv = StorageCells.getCellInventory(is, null);
        if (inv == null) {
            inv = NullInventory.of(StorageChannels.items());
        }
        this.cellMonitor = new MEMonitorHandler<>(inv);
        this.ips = (IAEItemPowerStorage) is.getItem();
        this.target = is;
        this.inventorySlot = slot;
    }

    @Override
    public int getInventorySlot() {
        return this.inventorySlot;
    }

    @Override
    public ItemStack getItemStack() {
        return this.target;
    }

    @Override
    public double extractAEPower(double amt, final Actionable mode, final PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);

        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.ips.getAECurrentPower(this.target)));
        }

        return usePowerMultiplier.divide(this.ips.extractAEPower(this.target, amt, Actionable.MODULATE));
    }

    @Override
    public <T extends IAEStack> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        return cellMonitor.cast(channel);
    }

    @Override
    public IConfigManager getConfigManager() {
        var out = new ConfigManager((manager, settingName) -> {
            manager.writeToNBT(this.target.getOrCreateTag());
        });

        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(this.target.getOrCreateTag().copy());
        return out;
    }
}
