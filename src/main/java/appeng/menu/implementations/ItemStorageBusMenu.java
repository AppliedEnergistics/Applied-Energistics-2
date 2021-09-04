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

import java.util.Collections;
import java.util.Iterator;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.AccessRestriction;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.ItemStorageBusScreen;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.FakeTypeOnlySlot;
import appeng.menu.slot.OptionalTypeOnlyFakeSlot;
import appeng.parts.misc.ItemStorageBusPart;
import appeng.util.helpers.ItemHandlerUtil;

/**
 * @see ItemStorageBusScreen
 */
public class ItemStorageBusMenu extends UpgradeableMenu<ItemStorageBusPart> {

    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_PARTITION = "partition";

    public static final MenuType<ItemStorageBusMenu> TYPE = MenuTypeBuilder
            .create(ItemStorageBusMenu::new, ItemStorageBusPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("storagebus");

    @GuiSync(3)
    public AccessRestriction rwMode = AccessRestriction.READ_WRITE;

    @GuiSync(4)
    public StorageFilter storageFilter = StorageFilter.EXTRACTABLE_ONLY;

    public ItemStorageBusMenu(int id, final Inventory ip, final ItemStorageBusPart te) {
        super(TYPE, id, ip, te);

        registerClientAction(ACTION_CLEAR, this::clear);
        registerClientAction(ACTION_PARTITION, this::partition);
    }

    @Override
    protected void setupConfig() {
        var config = this.getHost().getSubInventory(ISegmentedInventory.CONFIG);
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

        ItemHandlerUtil.clear(this.getHost().getSubInventory(ISegmentedInventory.CONFIG));
        this.broadcastChanges();
    }

    public void partition() {
        if (isClient()) {
            sendClientAction(ACTION_PARTITION);
            return;
        }

        var inv = getHost().getSubInventory(ISegmentedInventory.CONFIG);

        var cellInv = getHost().getInternalHandler();

        Iterator<IAEItemStack> i = Collections.emptyIterator();
        if (cellInv != null) {
            i = cellInv.getAvailableItems().iterator();
        }

        for (int x = 0; x < inv.size(); x++) {
            if (i.hasNext() && this.isSlotEnabled(x / 9 - 2)) {
                // TODO: check if ok
                final ItemStack g = i.next().asItemStackRepresentation();
                inv.setItemDirect(x, g);
            } else {
                inv.setItemDirect(x, ItemStack.EMPTY);
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
