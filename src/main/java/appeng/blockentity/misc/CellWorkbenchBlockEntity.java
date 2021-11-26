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

package appeng.blockentity.misc;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.CopyMode;
import appeng.api.config.Settings;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.iface.GenericStackInv;
import appeng.parts.automation.EmptyUpgradeInventory;
import appeng.util.ConfigInventory;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public class CellWorkbenchBlockEntity extends AEBaseBlockEntity
        implements IConfigurableObject, IUpgradeableObject, InternalInventoryHost, IConfigInvHost {

    private final AppEngInternalInventory cell = new AppEngInternalInventory(this, 1);
    private final GenericStackInv config = new GenericStackInv(this::configChanged, GenericStackInv.Mode.CONFIG_TYPES,
            63);
    private final ConfigManager manager = new ConfigManager();

    private IUpgradeInventory cacheUpgrades = null;
    private ConfigInventory cacheConfig = null;
    private boolean locked = false;

    public CellWorkbenchBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.manager.registerSetting(Settings.COPY_MODE, CopyMode.CLEAR_ON_REMOVE);
        this.cell.setEnableClientEvents(true);
    }

    public ICellWorkbenchItem getCell() {
        if (this.cell.getStackInSlot(0).isEmpty()) {
            return null;
        }

        if (this.cell.getStackInSlot(0).getItem() instanceof ICellWorkbenchItem) {
            return (ICellWorkbenchItem) this.cell.getStackInSlot(0).getItem();
        }

        return null;
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.cell.writeToNBT(data, "cell");
        this.config.writeToChildTag(data, "config");
        this.manager.writeToNBT(data);
    }

    @Override
    public void loadTag(final CompoundTag data) {
        super.loadTag(data);
        this.cell.readFromNBT(data, "cell");
        this.config.readFromChildTag(data, "config");
        this.manager.readFromNBT(data);
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.CELLS)) {
            return this.cell;
        }

        return super.getSubInventory(id);
    }

    @Override
    public void onChangeInventory(InternalInventory inv, final int slot, final ItemStack removedStack,
            final ItemStack newStack) {
        if (inv == this.cell && !this.locked) {
            this.locked = true;
            try {
                this.cacheUpgrades = null;
                this.cacheConfig = null;

                var configInventory = this.getCellConfigInventory();
                if (configInventory != null) {
                    if (!configInventory.isEmpty()) {
                        // Copy cell -> config inventory
                        copy(configInventory, this.config);
                    } else {
                        // Copy config inventory -> cell, when cell's config is empty
                        copy(this.config, configInventory);
                        // Copy items back. The cell may change the items on insert, for example if a fluid tank gets
                        // turned
                        // into a dummy fluid item.
                        copy(configInventory, this.config);
                    }
                } else if (this.manager.getSetting(Settings.COPY_MODE) == CopyMode.CLEAR_ON_REMOVE) {
                    this.config.clear();
                    this.saveChanges();
                }
            } finally {
                this.locked = false;
            }
        }
    }

    private void configChanged() {
        if (locked) {
            return;
        }

        this.locked = true;
        try {
            var c = this.getCellConfigInventory();
            if (c != null) {
                copy(this.config, c);
                // Copy items back. The cell may change the items on insert, for example if a fluid tank gets turned
                // into a dummy fluid item.
                copy(c, this.config);
            }
        } finally {
            this.locked = false;
        }
    }

    public static void copy(GenericStackInv from, GenericStackInv to) {
        for (int i = 0; i < Math.min(from.size(), to.size()); ++i) {
            to.setStack(i, from.getStack(i));
        }
        for (int i = from.size(); i < to.size(); i++) {
            to.setStack(i, null);
        }
    }

    private ConfigInventory getCellConfigInventory() {
        if (this.cacheConfig == null) {
            var cell = this.getCell();
            if (cell == null) {
                return null;
            }

            var is = this.cell.getStackInSlot(0);
            if (is.isEmpty()) {
                return null;
            }

            var inv = cell.getConfigInventory(is);
            if (inv == null) {
                return null;
            }

            this.cacheConfig = inv;
        }
        return this.cacheConfig;
    }

    @Override
    public void getDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        super.getDrops(level, pos, drops);

        if (!this.cell.getStackInSlot(0).isEmpty()) {
            drops.add(this.cell.getStackInSlot(0));
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.manager;
    }

    @Override
    public GenericStackInv getConfig() {
        return config;
    }

    @Nonnull
    @Override
    public IUpgradeInventory getUpgrades() {
        if (this.cacheUpgrades == null) {
            final ICellWorkbenchItem cell = this.getCell();
            if (cell == null) {
                return EmptyUpgradeInventory.INSTANCE;
            }

            final ItemStack is = this.cell.getStackInSlot(0);
            if (is.isEmpty()) {
                return EmptyUpgradeInventory.INSTANCE;
            }

            var inv = cell.getUpgradesInventory(is);
            if (inv == null) {
                return EmptyUpgradeInventory.INSTANCE;
            }

            return this.cacheUpgrades = inv;
        }
        return this.cacheUpgrades;
    }
}
