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
import appeng.parts.automation.EmptyUpgradeInventory;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalAEInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public class CellWorkbenchBlockEntity extends AEBaseBlockEntity
        implements IConfigurableObject, IUpgradeableObject, InternalInventoryHost {

    private final AppEngInternalInventory cell = new AppEngInternalInventory(this, 1);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 63);
    private final ConfigManager manager = new ConfigManager();

    private IUpgradeInventory cacheUpgrades = null;
    private InternalInventory cacheConfig = null;
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
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        this.cell.writeToNBT(data, "cell");
        this.config.writeToNBT(data, "config");
        this.manager.writeToNBT(data);
        return data;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        this.cell.readFromNBT(data, "cell");
        this.config.readFromNBT(data, "config");
        this.manager.readFromNBT(data);
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.CONFIG)) {
            return this.config;
        } else if (id.equals(ISegmentedInventory.CELLS)) {
            return this.cell;
        }

        return super.getSubInventory(id);
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.cell && !this.locked) {
            this.locked = true;

            this.cacheUpgrades = null;
            this.cacheConfig = null;

            var configInventory = this.getCellConfigInventory();
            if (configInventory != null) {
                if (!configInventory.isEmpty()) {
                    // Copy cell -> config inventory
                    for (int x = 0; x < this.config.size(); x++) {
                        this.config.setItemDirect(x, configInventory.getStackInSlot(x));
                    }
                } else {
                    // Copy config inventory -> cell, when cell's config is empty
                    copy(this.config, configInventory);
                }
            } else if (this.manager.getSetting(Settings.COPY_MODE) == CopyMode.CLEAR_ON_REMOVE) {
                for (int x = 0; x < this.config.size(); x++) {
                    this.config.setItemDirect(x, ItemStack.EMPTY);
                }

                this.saveChanges();
            }

            this.locked = false;
        } else if (inv == this.config && !this.locked) {
            this.locked = true;
            var c = this.getCellConfigInventory();
            if (c != null) {
                copy(this.config, c);
                // copy items back. The ConfigInventory may changed the items on insert
                copy(c, this.config);
            }
            this.locked = false;
        }
    }

    public static void copy(InternalInventory from, InternalInventory to) {
        for (int i = 0; i < Math.min(from.size(), to.size()); ++i) {
            to.setItemDirect(i, from.getStackInSlot(i));
        }
    }

    private InternalInventory getCellConfigInventory() {
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
