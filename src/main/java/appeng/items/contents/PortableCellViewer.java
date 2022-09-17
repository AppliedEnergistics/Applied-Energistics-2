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


import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.me.helpers.MEMonitorHandler;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;


public class PortableCellViewer extends MEMonitorHandler<IAEItemStack> implements IPortableCell, IInventorySlotAware {

    private final ItemStack target;
    private final IAEItemPowerStorage ips;
    private final int inventorySlot;

    public PortableCellViewer(final ItemStack is, final int slot) {
        super(AEApi.instance().registries().cell().getCellInventory(is, null, AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)));
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
    public IAEItemStack injectItems(IAEItemStack input, Actionable mode, IActionSource src) {
        final long size = input.getStackSize();

        final IAEItemStack injected = super.injectItems(input, mode, src);

        if (mode == Actionable.MODULATE && (injected == null || injected.getStackSize() != size)) {
            this.notifyListenersOfChange(Collections.singletonList(input.copy().setStackSize(input.getStackSize() - (injected == null ? 0 : injected.getStackSize()))), null);
        }

        return injected;
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack request, Actionable mode, IActionSource src) {
        final IAEItemStack extractable = super.extractItems(request, mode, src);

        if (mode == Actionable.MODULATE && extractable != null) {
            this.notifyListenersOfChange(Collections.singletonList(request.copy().setStackSize(-extractable.getStackSize())), null);
        }

        return extractable;
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        if (channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
            return (IMEMonitor<T>) this;
        }
        return null;
    }

    @Override
    public IConfigManager getConfigManager() {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) ->
        {
            final NBTTagCompound data = Platform.openNbtData(PortableCellViewer.this.target);
            manager.writeToNBT(data);
        });

        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(Platform.openNbtData(this.target).copy());
        return out;
    }
}
