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

package appeng.menu.implementations;

import java.util.Iterator;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.ItemStorageBusScreen;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.FakeTypeOnlySlot;
import appeng.menu.slot.OptionalTypeOnlyFakeSlot;
import appeng.parts.misc.ItemStorageBusPart;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.iterators.NullIterator;

/**
 * @see ItemStorageBusScreen
 */
public class ItemStorageBusMenu extends UpgradeableMenu {

    public static final MenuType<ItemStorageBusMenu> TYPE = MenuTypeBuilder
            .create(ItemStorageBusMenu::new, ItemStorageBusPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("storagebus");

    private final ItemStorageBusPart storageBus;

    @GuiSync(3)
    public AccessRestriction rwMode = AccessRestriction.READ_WRITE;

    @GuiSync(4)
    public StorageFilter storageFilter = StorageFilter.EXTRACTABLE_ONLY;

    public ItemStorageBusMenu(int id, final Inventory ip, final ItemStorageBusPart te) {
        super(TYPE, id, ip, te);
        this.storageBus = te;
    }

    @Override
    protected void setupConfig() {
        final IItemHandler config = this.getUpgradeable().getInventoryByName("config");
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                int invSlot = y * 9 + x;
                if (y < 2) {
                    this.addSlot(new FakeTypeOnlySlot(config, invSlot), SlotSemantic.CONFIG);
                } else {
                    this.addSlot(new OptionalTypeOnlyFakeSlot(config, this, invSlot, y - 2), SlotSemantic.CONFIG);
                }
            }
        }

        this.setupUpgrades();
    }

    @Override
    protected boolean supportCapacity() {
        return true;
    }

    @Override
    public int availableUpgrades() {
        return 5;
    }

    @Override
    public void broadcastChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            this.setFuzzyMode((FuzzyMode) this.getUpgradeable().getConfigManager().getSetting(Settings.FUZZY_MODE));
            this.setReadWriteMode(
                    (AccessRestriction) this.getUpgradeable().getConfigManager().getSetting(Settings.ACCESS));
            this.setStorageFilter(
                    (StorageFilter) this.getUpgradeable().getConfigManager().getSetting(Settings.STORAGE_FILTER));
        }

        this.standardDetectAndSendChanges();
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

        return upgrades > idx;
    }

    public void clear() {
        ItemHandlerUtil.clear(this.getUpgradeable().getInventoryByName("config"));
        this.broadcastChanges();
    }

    public void partition() {
        final IItemHandler inv = this.getUpgradeable().getInventoryByName("config");

        final IMEInventory<IAEItemStack> cellInv = this.storageBus.getInternalHandler();

        Iterator<IAEItemStack> i = new NullIterator<>();
        if (cellInv != null) {
            i = cellInv.getAvailableItems().iterator();
        }

        for (int x = 0; x < inv.getSlots(); x++) {
            if (i.hasNext() && this.isSlotEnabled(x / 9 - 2)) {
                // TODO: check if ok
                final ItemStack g = i.next().asItemStackRepresentation();
                ItemHandlerUtil.setStackInSlot(inv, x, g);
            } else {
                ItemHandlerUtil.setStackInSlot(inv, x, ItemStack.EMPTY);
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
}
