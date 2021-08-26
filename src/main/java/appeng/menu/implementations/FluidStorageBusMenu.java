/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.menu.implementations;

import java.util.Collections;
import java.util.Iterator;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.menu.guisync.GuiSync;
import appeng.parts.misc.FluidStorageBusPart;
import appeng.util.fluid.IAEFluidTank;

/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class FluidStorageBusMenu extends FluidConfigurableMenu<FluidStorageBusPart> {

    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_PARTITION = "partition";

    public static final MenuType<FluidStorageBusMenu> TYPE = MenuTypeBuilder
            .create(FluidStorageBusMenu::new, FluidStorageBusPart.class)
            .build("fluid_storage_bus");

    @GuiSync(3)
    public AccessRestriction rwMode = AccessRestriction.READ_WRITE;

    @GuiSync(4)
    public StorageFilter storageFilter = StorageFilter.EXTRACTABLE_ONLY;

    public FluidStorageBusMenu(int id, Inventory ip, FluidStorageBusPart host) {
        super(TYPE, id, ip, host);

        registerClientAction(ACTION_CLEAR, this::clear);
        registerClientAction(ACTION_PARTITION, this::partition);
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }

    @Override
    protected boolean isValidForConfig(int slot, IAEFluidStack fs) {
        if (this.supportCapacity()) {
            final int upgrades = getUpgrades().getInstalledUpgrades(Upgrades.CAPACITY);

            final int y = slot / 9;

            if (y >= upgrades + 2) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean supportCapacity() {
        return true;
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        this.setReadWriteMode(cm.getSetting(Settings.ACCESS));
        this.setStorageFilter(cm.getSetting(Settings.STORAGE_FILTER));
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        final int upgrades = getUpgrades().getInstalledUpgrades(Upgrades.CAPACITY);

        return upgrades > idx;
    }

    public void clear() {
        if (isClient()) {
            sendClientAction(ACTION_CLEAR);
            return;
        }

        IAEFluidTank h = getHost().getConfig();
        for (int i = 0; i < h.getSlots(); ++i) {
            h.setFluidInSlot(i, null);
        }
        this.broadcastChanges();
    }

    public void partition() {
        if (isClient()) {
            sendClientAction(ACTION_PARTITION);
            return;
        }

        IAEFluidTank h = getHost().getConfig();

        final IMEInventory<IAEFluidStack> cellInv = getHost().getInternalHandler();

        Iterator<IAEFluidStack> i = Collections.emptyIterator();
        if (cellInv != null) {
            i = cellInv.getAvailableItems().iterator();
        }

        for (int x = 0; x < h.getSlots(); x++) {
            if (i.hasNext() && this.isSlotEnabled(x / 9 - 2)) {
                h.setFluidInSlot(x, i.next());
            } else {
                h.setFluidInSlot(x, null);
            }
        }
        this.broadcastChanges();
    }

    public AccessRestriction getReadWriteMode() {
        return this.rwMode;
    }

    private void setReadWriteMode(final AccessRestriction rwMode) {
        this.rwMode = rwMode;
    }

    public StorageFilter getStorageFilter() {
        return this.storageFilter;
    }

    private void setStorageFilter(final StorageFilter storageFilter) {
        this.storageFilter = storageFilter;
    }

    @Override
    public IAEFluidTank getFluidConfigInventory() {
        return getHost().getConfig();
    }
}
