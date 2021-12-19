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
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.collect.Iterators;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.util.IConfigManager;
import appeng.blockentity.misc.CellWorkbenchBlockEntity;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.OptionalRestrictedInputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigMenuInventory;
import appeng.util.EnumCycler;
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
                SlotSemantics.STORAGE_CELL);

        ConfigMenuInventory configInv = getConfigInventory().createMenuWrapper();

        for (int i = 0; i < 7 * 9; i++) {
            this.addSlot(new FakeSlot(configInv, i), SlotSemantics.CONFIG);
        }

        // We support up to 8 upgrade slots, see ICellWorkbenchItem, but we need to pre-create all slots here
        // while the active number of slots changes depending on the item inserted
        var upgradeInventory = new SupplierInternalInventory(this::getUpgrades);
        for (int i = 0; i < 8; i++) {
            OptionalRestrictedInputSlot slot = new OptionalRestrictedInputSlot(
                    RestrictedInputSlot.PlacableItemType.UPGRADES,
                    upgradeInventory, this, i, i, this.getPlayerInventory());
            this.addSlot(slot, SlotSemantics.UPGRADE);
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
            getConfigInventory().clear();
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
        var is = getWorkbenchItem();

        var it = iterateCellStacks(is);

        for (int x = 0; x < inv.size(); x++) {
            if (it.hasNext()) {
                inv.setStack(x, new GenericStack(it.next(), 0));
            } else {
                inv.setStack(x, null);
            }
        }

        this.broadcastChanges();
    }

    @Nonnull
    private GenericStackInv getConfigInventory() {
        return Objects.requireNonNull(this.getHost().getConfig());
    }

    @NotNull
    private Iterator<? extends AEKey> iterateCellStacks(ItemStack is) {
        var cellInv = StorageCells.getCellInventory(is, null);
        Iterator<? extends AEKey> i;
        if (cellInv != null) {
            i = Iterators.transform(cellInv.getAvailableStacks().iterator(), Map.Entry::getKey);
        } else {
            i = Collections.emptyIterator();
        }
        return i;
    }

    public CopyMode getCopyMode() {
        return this.copyMode;
    }

    private void setCopyMode(final CopyMode copyMode) {
        this.copyMode = copyMode;
    }
}
