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

package appeng.container.implementations;

import java.util.Iterator;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.FakeTypeOnlySlot;
import appeng.container.slot.OptionalTypeOnlyFakeSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.Api;
import appeng.parts.misc.StorageBusPart;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.iterators.NullIterator;

public class StorageBusContainer extends UpgradeableContainer {

    public static ContainerType<StorageBusContainer> TYPE;

    private static final ContainerHelper<StorageBusContainer, StorageBusPart> helper = new ContainerHelper<>(
            StorageBusContainer::new, StorageBusPart.class, SecurityPermissions.BUILD);

    public static StorageBusContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final StorageBusPart storageBus;

    @GuiSync(3)
    public AccessRestriction rwMode = AccessRestriction.READ_WRITE;

    @GuiSync(4)
    public StorageFilter storageFilter = StorageFilter.EXTRACTABLE_ONLY;

    public StorageBusContainer(int id, final PlayerInventory ip, final StorageBusPart te) {
        super(TYPE, id, ip, te);
        this.storageBus = te;
    }

    @Override
    protected int getHeight() {
        return 251;
    }

    @Override
    protected void setupConfig() {
        final int xo = 8;
        final int yo = 23 + 6;

        final IItemHandler config = this.getUpgradeable().getInventoryByName("config");
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                if (y < 2) {
                    this.addSlot(new FakeTypeOnlySlot(config, y * 9 + x, xo + x * 18, yo + y * 18));
                } else {
                    this.addSlot(new OptionalTypeOnlyFakeSlot(config, this, y * 9 + x, xo, yo, x, y, y - 2));
                }
            }
        }

        final IItemHandler upgrades = this.getUpgradeable().getInventoryByName("upgrades");
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 0, 187, 8,
                this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18,
                this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 2, 187,
                8 + 18 * 2, this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 3, 187,
                8 + 18 * 3, this.getPlayerInventory())).setNotDraggable());
        this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, upgrades, 4, 187,
                8 + 18 * 4, this.getPlayerInventory())).setNotDraggable());
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
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
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
        this.detectAndSendChanges();
    }

    public void partition() {
        final IItemHandler inv = this.getUpgradeable().getInventoryByName("config");

        final IMEInventory<IAEItemStack> cellInv = this.storageBus.getInternalHandler();

        Iterator<IAEItemStack> i = new NullIterator<>();
        if (cellInv != null) {
            final IItemList<IAEItemStack> list = cellInv.getAvailableItems(
                    Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList());
            i = list.iterator();
        }

        for (int x = 0; x < inv.getSlots(); x++) {
            if (i.hasNext() && this.isSlotEnabled((x / 9) - 2)) {
                // TODO: check if ok
                final ItemStack g = i.next().asItemStackRepresentation();
                ItemHandlerUtil.setStackInSlot(inv, x, g);
            } else {
                ItemHandlerUtil.setStackInSlot(inv, x, ItemStack.EMPTY);
            }
        }

        this.detectAndSendChanges();
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
