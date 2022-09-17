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

package appeng.container.implementations;


import appeng.api.AEApi;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.OptionalSlotRestrictedInput;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.TileCellWorkbench;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperSupplierItemHandler;
import appeng.util.iterators.NullIterator;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import java.util.Iterator;


public class ContainerCellWorkbench extends ContainerUpgradeable {
    private final TileCellWorkbench workBench;
    @GuiSync(2)
    public CopyMode copyMode = CopyMode.CLEAR_ON_REMOVE;
    private ItemStack prevStack = ItemStack.EMPTY;
    private int lastUpgrades = 0;

    public ContainerCellWorkbench(final InventoryPlayer ip, final TileCellWorkbench te) {
        super(ip, te);
        this.workBench = te;
    }

    public void setFuzzy(final FuzzyMode valueOf) {
        final ICellWorkbenchItem cwi = this.workBench.getCell();
        if (cwi != null) {
            cwi.setFuzzyMode(this.workBench.getInventoryByName("cell").getStackInSlot(0), valueOf);
        }
    }

    public void nextWorkBenchCopyMode() {
        this.workBench.getConfigManager().putSetting(Settings.COPY_MODE, Platform.nextEnum(this.getWorkBenchCopyMode()));
    }

    private CopyMode getWorkBenchCopyMode() {
        return (CopyMode) this.workBench.getConfigManager().getSetting(Settings.COPY_MODE);
    }

    @Override
    protected int getHeight() {
        return 251;
    }

    @Override
    protected void setupConfig() {
        final IItemHandler cell = this.getUpgradeable().getInventoryByName("cell");
        this.addSlotToContainer(new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.WORKBENCH_CELL, cell, 0, 152, 8, this.getPlayerInv()));

        final IItemHandler inv = this.getUpgradeable().getInventoryByName("config");
        final WrapperSupplierItemHandler upgradeInventory = new WrapperSupplierItemHandler(this::getCellUpgradeInventory);
        // null, 3 * 8 );

        int offset = 0;
        final int y = 29;
        final int x = 8;
        for (int w = 0; w < 7; w++) {
            for (int z = 0; z < 9; z++) {
                this.addSlotToContainer(new SlotFakeTypeOnly(inv, offset, x + z * 18, y + w * 18));
                offset++;
            }
        }

        for (int zz = 0; zz < 3; zz++) {
            for (int z = 0; z < 8; z++) {
                final int iSLot = zz * 8 + z;
                this.addSlotToContainer(
                        new OptionalSlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgradeInventory, this, iSLot, 187 + zz * 18, 8 + 18 * z, iSLot, this
                                .getInventoryPlayer()));
            }
        }
        /*
         * if ( supportCapacity() ) { for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new
         * OptionalSlotFakeTypeOnly( inv, this, offset++, x, y, z, w, 1 ) );
         * for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new OptionalSlotFakeTypeOnly(
         * inv, this, offset++, x, y, z, w + 2, 2 ) );
         * for (int w = 0; w < 2; w++) for (int z = 0; z < 9; z++) addSlotToContainer( new OptionalSlotFakeTypeOnly(
         * inv, this, offset++, x, y, z, w + 4, 3 ) ); }
         */
    }

    @Override
    public int availableUpgrades() {
        final ItemStack is = this.workBench.getInventoryByName("cell").getStackInSlot(0);
        if (this.prevStack != is) {
            this.prevStack = is;
            this.lastUpgrades = this.getCellUpgradeInventory().getSlots();
        }
        return this.lastUpgrades;
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack is = this.workBench.getInventoryByName("cell").getStackInSlot(0);
        if (Platform.isServer()) {
            for (final IContainerListener listener : this.listeners) {
                if (this.prevStack != is) {
                    // if the bars changed an item was probably made, so just send shit!
                    for (final Slot s : this.inventorySlots) {
                        if (s instanceof OptionalSlotRestrictedInput) {
                            final OptionalSlotRestrictedInput sri = (OptionalSlotRestrictedInput) s;
                            listener.sendSlotContents(this, sri.slotNumber, sri.getStack());
                        }
                    }

                    if (listener instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) listener).isChangingQuantityOnly = false;
                    }
                }
            }

            this.setCopyMode(this.getWorkBenchCopyMode());
            this.setFuzzyMode(this.getWorkBenchFuzzyMode());
        }

        this.prevStack = is;
        this.standardDetectAndSendChanges();
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        return idx < this.availableUpgrades();
    }

    public IItemHandler getCellUpgradeInventory() {
        final IItemHandler upgradeInventory = this.workBench.getCellUpgradeInventory();

        return upgradeInventory == null ? EmptyHandler.INSTANCE : upgradeInventory;
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("copyMode")) {
            this.workBench.getConfigManager().putSetting(Settings.COPY_MODE, this.getCopyMode());
        }

        super.onUpdate(field, oldValue, newValue);
    }

    public void clear() {
        ItemHandlerUtil.clear(this.getUpgradeable().getInventoryByName("config"));
        this.detectAndSendChanges();
    }

    private FuzzyMode getWorkBenchFuzzyMode() {
        final ICellWorkbenchItem cwi = this.workBench.getCell();
        if (cwi != null) {
            return cwi.getFuzzyMode(this.workBench.getInventoryByName("cell").getStackInSlot(0));
        }
        return FuzzyMode.IGNORE_ALL;
    }

    public void partition() {

        final IItemHandler inv = this.getUpgradeable().getInventoryByName("config");

        final ItemStack is = this.getUpgradeable().getInventoryByName("cell").getStackInSlot(0);
        final IStorageChannel channel = is.getItem() instanceof IStorageCell ? ((IStorageCell) is.getItem()).getChannel() : AEApi.instance()
                .storage()
                .getStorageChannel(IItemStorageChannel.class);

        final IMEInventory cellInv = AEApi.instance().registries().cell().getCellInventory(is, null, channel);

        Iterator<IAEStack> i = new NullIterator<>();
        if (cellInv != null) {
            final IItemList list = cellInv.getAvailableItems(channel.createList());
            i = list.iterator();
        }

        for (int x = 0; x < inv.getSlots(); x++) {
            if (i.hasNext()) {
                // TODO: check if ok
                final ItemStack g = i.next().asItemStackRepresentation();
                ItemHandlerUtil.setStackInSlot(inv, x, g);
            } else {
                ItemHandlerUtil.setStackInSlot(inv, x, ItemStack.EMPTY);
            }
        }

        this.detectAndSendChanges();
    }

    public CopyMode getCopyMode() {
        return this.copyMode;
    }

    private void setCopyMode(final CopyMode copyMode) {
        this.copyMode = copyMode;
    }
}