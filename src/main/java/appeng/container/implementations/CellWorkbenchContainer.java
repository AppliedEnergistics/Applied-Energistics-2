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

import java.util.Iterator;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;


import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.impl.EmptyFixedItemInv;

import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.container.SlotSemantic;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.FakeTypeOnlySlot;
import appeng.container.slot.OptionalRestrictedInputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.Api;
import appeng.tile.misc.CellWorkbenchTileEntity;
import appeng.util.EnumCycler;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperSupplierItemHandler;
import appeng.util.iterators.NullIterator;

/**
 * @see appeng.client.gui.implementations.CellWorkbenchScreen
 */
public class CellWorkbenchContainer extends UpgradeableContainer {

    public static final ContainerType<CellWorkbenchContainer> TYPE = ContainerTypeBuilder
            .create(CellWorkbenchContainer::new, CellWorkbenchTileEntity.class)
            .build("cellworkbench");

    private final CellWorkbenchTileEntity workBench;
    @GuiSync(2)
    public CopyMode copyMode = CopyMode.CLEAR_ON_REMOVE;
    private ItemStack prevStack = ItemStack.EMPTY;
    private int lastUpgrades = 0;

    public CellWorkbenchContainer(int id, final PlayerInventory ip, final CellWorkbenchTileEntity te) {
        super(TYPE, id, ip, te);
        this.workBench = te;
    }

    public void setFuzzy(final FuzzyMode valueOf) {
        final ICellWorkbenchItem cwi = this.workBench.getCell();
        if (cwi != null) {
            cwi.setFuzzyMode(getWorkbenchItem(), valueOf);
        }
    }

    public void nextWorkBenchCopyMode() {
        this.workBench.getConfigManager().putSetting(Settings.COPY_MODE, EnumCycler.next(this.getWorkBenchCopyMode()));
    }

    private CopyMode getWorkBenchCopyMode() {
        return (CopyMode) this.workBench.getConfigManager().getSetting(Settings.COPY_MODE);
    }

    @Override
    protected void setupConfig() {
        final FixedItemInv cell = this.getUpgradeable().getInventoryByName("cell");
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.WORKBENCH_CELL, cell, 0),
                SlotSemantic.STORAGE_CELL);

        final FixedItemInv inv = this.getUpgradeable().getInventoryByName("config");
        final WrapperSupplierItemHandler upgradeInventory = new WrapperSupplierItemHandler(
                this::getCellUpgradeInventory);

        for (int i = 0; i < 7 * 9; i++) {
            this.addSlot(new FakeTypeOnlySlot(inv, i), SlotSemantic.CONFIG);
        }

        // We support up to 24 upgrade slots, see ICellWorkbenchItem, but we need to pre-create all slots here
        // while the active number of slots changes depending on the item inserted
        for (int i = 0; i < 24; i++) {
            OptionalRestrictedInputSlot slot = new OptionalRestrictedInputSlot(
                    RestrictedInputSlot.PlacableItemType.UPGRADES,
                    upgradeInventory, this, i, i, this.getPlayerInventory());
            this.addSlot(slot, SlotSemantic.UPGRADE);
        }
    }

    @Override
    public int availableUpgrades() {
        final ItemStack is = getWorkbenchItem();
        if (this.prevStack != is) {
            this.prevStack = is;
            this.lastUpgrades = this.getCellUpgradeInventory().getSlotCount();
        }
        return this.lastUpgrades;
    }

    public ItemStack getWorkbenchItem() {
        return this.workBench.getInventoryByName("cell").getStackInSlot(0);
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack is = getWorkbenchItem();
        if (isServer()) {
            for (final IContainerListener listener : this.listeners) {
                if (this.prevStack != is) {
                    // if the bars changed an item was probably made, so just send shit!
                    for (final Slot s : this.inventorySlots) {
                        if (s instanceof OptionalRestrictedInputSlot) {
                            final OptionalRestrictedInputSlot sri = (OptionalRestrictedInputSlot) s;
                            listener.sendSlotContents(this, sri.slotNumber, sri.getStack());
                        }
                    }

                    if (listener instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) listener).isChangingQuantityOnly = false;
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

    public FixedItemInv getCellUpgradeInventory() {
        final FixedItemInv upgradeInventory = this.workBench.getCellUpgradeInventory();

        return upgradeInventory == null ? EmptyFixedItemInv.INSTANCE : upgradeInventory;
    }

    @Override
    public void onServerDataSync() {
        super.onServerDataSync();

        this.workBench.getConfigManager().putSetting(Settings.COPY_MODE, this.getCopyMode());
    }

    public void clear() {
        ItemHandlerUtil.clear(this.getUpgradeable().getInventoryByName("config"));
        this.detectAndSendChanges();
    }

    private FuzzyMode getWorkBenchFuzzyMode() {
        final ICellWorkbenchItem cwi = this.workBench.getCell();
        if (cwi != null) {
            return cwi.getFuzzyMode(getWorkbenchItem());
        }
        return FuzzyMode.IGNORE_ALL;
    }

    public void partition() {

        final FixedItemInv inv = this.getUpgradeable().getInventoryByName("config");

        final ItemStack is = getWorkbenchItem();
        final IStorageChannel<?> channel = is.getItem() instanceof IStorageCell
                ? ((IStorageCell<?>) is.getItem()).getChannel()
                : Api.instance().storage().getStorageChannel(IItemStorageChannel.class);

        Iterator<? extends IAEStack<?>> i = iterateCellItems(is, channel);

        for (int x = 0; x < inv.getSlotCount(); x++) {
            if (i.hasNext()) {
                final ItemStack g = i.next().asItemStackRepresentation();
                ItemHandlerUtil.setStackInSlot(inv, x, g);
            } else {
                ItemHandlerUtil.setStackInSlot(inv, x, ItemStack.EMPTY);
            }
        }

        this.detectAndSendChanges();
    }

    private <T extends IAEStack<T>> Iterator<? extends IAEStack<T>> iterateCellItems(ItemStack is,
            IStorageChannel<T> channel) {
        final IMEInventory<T> cellInv = Api.instance().registries().cell().getCellInventory(is, null, channel);
        if (cellInv != null) {
            final IItemList<T> list = cellInv.getAvailableItems(channel.createList());
            return list.iterator();
        } else {
            return new NullIterator<>();
        }
    }

    public CopyMode getCopyMode() {
        return this.copyMode;
    }

    private void setCopyMode(final CopyMode copyMode) {
        this.copyMode = copyMode;
    }
}
