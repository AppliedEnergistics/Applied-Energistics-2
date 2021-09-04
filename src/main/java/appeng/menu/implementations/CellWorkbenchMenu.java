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

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.blockentity.misc.CellWorkbenchBlockEntity;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.FakeTypeOnlySlot;
import appeng.menu.slot.OptionalRestrictedInputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.EnumCycler;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.SupplierInternalInventory;

/**
 * @see appeng.client.gui.implementations.CellWorkbenchScreen
 */
public class CellWorkbenchMenu extends UpgradeableMenu<CellWorkbenchBlockEntity> {

    public static final String ACTION_NEXT_COPYMODE = "nextCopyMode";
    public static final String ACTION_PARTITION = "partition";
    public static final String ACTION_CLEAR = "clear";
    public static final String ACTION_SET_FUZZY_MODE = "setFuzzyMode";

    public static final MenuType<CellWorkbenchMenu> TYPE = MenuTypeBuilder
            .create(CellWorkbenchMenu::new, CellWorkbenchBlockEntity.class)
            .build("cellworkbench");

    @GuiSync(2)
    public CopyMode copyMode = CopyMode.CLEAR_ON_REMOVE;

    public CellWorkbenchMenu(int id, Inventory ip, CellWorkbenchBlockEntity te) {
        super(TYPE, id, ip, te);

        registerClientAction(ACTION_NEXT_COPYMODE, this::nextWorkBenchCopyMode);
        registerClientAction(ACTION_PARTITION, this::partition);
        registerClientAction(ACTION_CLEAR, this::clear);
        registerClientAction(ACTION_SET_FUZZY_MODE, FuzzyMode.class, this::setCellFuzzyMode);
    }

    public void setCellFuzzyMode(FuzzyMode fuzzyMode) {
        if (isClient()) {
            sendClientAction(ACTION_SET_FUZZY_MODE, fuzzyMode);
            return;
        }

        var cwi = getHost().getCell();
        if (cwi != null) {
            cwi.setFuzzyMode(getWorkbenchItem(), fuzzyMode);
        }
    }

    public void nextWorkBenchCopyMode() {
        if (isClient()) {
            sendClientAction(ACTION_NEXT_COPYMODE);
        } else {
            getHost().getConfigManager().putSetting(Settings.COPY_MODE, EnumCycler.next(this.getWorkBenchCopyMode()));
        }
    }

    private CopyMode getWorkBenchCopyMode() {
        return getHost().getConfigManager().getSetting(Settings.COPY_MODE);
    }

    @Override
    protected void setupConfig() {
        var cell = this.getHost().getSubInventory(ISegmentedInventory.CELLS);
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.WORKBENCH_CELL, cell, 0),
                SlotSemantic.STORAGE_CELL);

        var inv = getConfigInventory();

        for (int i = 0; i < 7 * 9; i++) {
            this.addSlot(new FakeTypeOnlySlot(inv, i), SlotSemantic.CONFIG);
        }

        // We support up to 8 upgrade slots, see ICellWorkbenchItem, but we need to pre-create all slots here
        // while the active number of slots changes depending on the item inserted
        var upgradeInventory = new SupplierInternalInventory(this::getUpgrades);
        for (int i = 0; i < 8; i++) {
            OptionalRestrictedInputSlot slot = new OptionalRestrictedInputSlot(
                    RestrictedInputSlot.PlacableItemType.UPGRADES,
                    upgradeInventory, this, i, i, this.getPlayerInventory());
            this.addSlot(slot, SlotSemantic.UPGRADE);
        }
    }

    public ItemStack getWorkbenchItem() {
        var cells = Objects.requireNonNull(getHost().getSubInventory(ISegmentedInventory.CELLS));
        return cells.getStackInSlot(0);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setCopyMode(this.getWorkBenchCopyMode());
        this.setFuzzyMode(this.getWorkBenchFuzzyMode());
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        return idx < getUpgrades().size();
    }

    @Override
    public void onServerDataSync() {
        super.onServerDataSync();

        getHost().getConfigManager().putSetting(Settings.COPY_MODE, this.getCopyMode());
    }

    public void clear() {
        if (isClient()) {
            sendClientAction(ACTION_CLEAR);
        } else {
            ItemHandlerUtil.clear(getConfigInventory());
            this.broadcastChanges();
        }
    }

    private FuzzyMode getWorkBenchFuzzyMode() {
        final ICellWorkbenchItem cwi = getHost().getCell();
        if (cwi != null) {
            return cwi.getFuzzyMode(getWorkbenchItem());
        }
        return FuzzyMode.IGNORE_ALL;
    }

    public void partition() {
        if (isClient()) {
            sendClientAction(ACTION_PARTITION);
            return;
        }

        var inv = getConfigInventory();

        final ItemStack is = getWorkbenchItem();
        final IStorageChannel<?> channel = is.getItem() instanceof IStorageCell
                ? ((IStorageCell<?>) is.getItem()).getChannel()
                : StorageChannels.items();

        Iterator<? extends IAEStack<?>> i = iterateCellItems(is, channel);

        for (int x = 0; x < inv.size(); x++) {
            if (i.hasNext()) {
                var g = i.next().asItemStackRepresentation();
                inv.setItemDirect(x, g);
            } else {
                inv.setItemDirect(x, ItemStack.EMPTY);
            }
        }

        this.broadcastChanges();
    }

    @Nonnull
    private InternalInventory getConfigInventory() {
        return Objects.requireNonNull(this.getHost().getSubInventory(ISegmentedInventory.CONFIG));
    }

    private <T extends IAEStack<T>> Iterator<? extends IAEStack<T>> iterateCellItems(ItemStack is,
            IStorageChannel<T> channel) {
        final IMEInventory<T> cellInv = StorageCells.getCellInventory(is, null, channel);
        if (cellInv != null) {
            return cellInv.getAvailableItems().iterator();
        } else {
            return Collections.emptyIterator();
        }
    }

    public CopyMode getCopyMode() {
        return this.copyMode;
    }

    private void setCopyMode(final CopyMode copyMode) {
        this.copyMode = copyMode;
    }
}
