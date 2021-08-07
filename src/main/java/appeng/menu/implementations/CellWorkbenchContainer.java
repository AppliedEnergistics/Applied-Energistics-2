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

package appeng.menu.implementations;

import java.util.Iterator;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

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
import appeng.blockentity.misc.CellWorkbenchBlockEntity;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.FakeTypeOnlySlot;
import appeng.menu.slot.OptionalRestrictedInputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.core.Api;
import appeng.util.EnumCycler;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperSupplierItemHandler;
import appeng.util.iterators.NullIterator;

/**
 * @see appeng.client.gui.implementations.CellWorkbenchScreen
 */
public class CellWorkbenchContainer extends UpgradeableContainer {

    public static final MenuType<CellWorkbenchContainer> TYPE = ContainerTypeBuilder
            .create(CellWorkbenchContainer::new, CellWorkbenchBlockEntity.class)
            .build("cellworkbench");

    private final CellWorkbenchBlockEntity workBench;
    @GuiSync(2)
    public CopyMode copyMode = CopyMode.CLEAR_ON_REMOVE;
    private ItemStack prevStack = ItemStack.EMPTY;
    private int lastUpgrades = 0;

    public CellWorkbenchContainer(int id, final Inventory ip, final CellWorkbenchBlockEntity te) {
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
        final IItemHandler cell = this.getUpgradeable().getInventoryByName("cell");
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.WORKBENCH_CELL, cell, 0),
                SlotSemantic.STORAGE_CELL);

        final IItemHandler inv = this.getUpgradeable().getInventoryByName("config");
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
            this.lastUpgrades = this.getCellUpgradeInventory().getSlots();
        }
        return this.lastUpgrades;
    }

    public ItemStack getWorkbenchItem() {
        return this.workBench.getInventoryByName("cell").getStackInSlot(0);
    }

    @Override
    public void broadcastChanges() {
        final ItemStack is = getWorkbenchItem();
        if (isServer()) {
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
    public void onServerDataSync() {
        super.onServerDataSync();

        this.workBench.getConfigManager().putSetting(Settings.COPY_MODE, this.getCopyMode());
    }

    public void clear() {
        ItemHandlerUtil.clear(this.getUpgradeable().getInventoryByName("config"));
        this.broadcastChanges();
    }

    private FuzzyMode getWorkBenchFuzzyMode() {
        final ICellWorkbenchItem cwi = this.workBench.getCell();
        if (cwi != null) {
            return cwi.getFuzzyMode(getWorkbenchItem());
        }
        return FuzzyMode.IGNORE_ALL;
    }

    public void partition() {

        final IItemHandler inv = this.getUpgradeable().getInventoryByName("config");

        final ItemStack is = getWorkbenchItem();
        final IStorageChannel<?> channel = is.getItem() instanceof IStorageCell
                ? ((IStorageCell<?>) is.getItem()).getChannel()
                : Api.instance().storage().getStorageChannel(IItemStorageChannel.class);

        Iterator<? extends IAEStack<?>> i = iterateCellItems(is, channel);

        for (int x = 0; x < inv.getSlots(); x++) {
            if (i.hasNext()) {
                final ItemStack g = i.next().asItemStackRepresentation();
                ItemHandlerUtil.setStackInSlot(inv, x, g);
            } else {
                ItemHandlerUtil.setStackInSlot(inv, x, ItemStack.EMPTY);
            }
        }

        this.broadcastChanges();
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
