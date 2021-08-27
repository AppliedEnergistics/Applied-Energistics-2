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
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.CopyMode;
import appeng.api.config.Settings;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.implementations.blockentities.ISegmentedInventory;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.inventory.AppEngInternalAEInventory;
import appeng.blockentity.inventory.AppEngInternalInventory;
import appeng.parts.automation.EmptyUpgradeInventory;
import appeng.util.ConfigManager;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;

public class CellWorkbenchBlockEntity extends AEBaseBlockEntity
        implements IConfigurableObject, IUpgradeableObject, IAEAppEngInventory {

    private final AppEngInternalInventory cell = new AppEngInternalInventory(this, 1);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 63);
    private final ConfigManager manager = new ConfigManager();

    private IUpgradeInventory cacheUpgrades = null;
    private IItemHandler cacheConfig = null;
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
    public IItemHandler getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.CONFIG)) {
            return this.config;
        } else if (id.equals(ISegmentedInventory.CELLS)) {
            return this.cell;
        }

        return super.getSubInventory(id);
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.cell && !this.locked) {
            this.locked = true;

            this.cacheUpgrades = null;
            this.cacheConfig = null;

            final IItemHandler configInventory = this.getCellConfigInventory();
            if (configInventory != null) {
                boolean cellHasConfig = false;
                for (int x = 0; x < configInventory.getSlots(); x++) {
                    if (!configInventory.getStackInSlot(x).isEmpty()) {
                        cellHasConfig = true;
                        break;
                    }
                }

                if (cellHasConfig) {
                    for (int x = 0; x < this.config.getSlots(); x++) {
                        this.config.setStackInSlot(x, configInventory.getStackInSlot(x));
                    }
                } else {
                    ItemHandlerUtil.copy(this.config, configInventory, false);
                }
            } else if (this.manager.getSetting(Settings.COPY_MODE) == CopyMode.CLEAR_ON_REMOVE) {
                for (int x = 0; x < this.config.getSlots(); x++) {
                    this.config.setStackInSlot(x, ItemStack.EMPTY);
                }

                this.saveChanges();
            }

            this.locked = false;
        } else if (inv == this.config && !this.locked) {
            this.locked = true;
            final IItemHandler c = this.getCellConfigInventory();
            if (c != null) {
                ItemHandlerUtil.copy(this.config, c, false);
                // copy items back. The ConfigInventory may changed the items on insert
                ItemHandlerUtil.copy(c, this.config, false);
            }
            this.locked = false;
        }
    }

    private IItemHandler getCellConfigInventory() {
        if (this.cacheConfig == null) {
            final ICellWorkbenchItem cell = this.getCell();
            if (cell == null) {
                return null;
            }

            final ItemStack is = this.cell.getStackInSlot(0);
            if (is.isEmpty()) {
                return null;
            }

            final IItemHandler inv = cell.getConfigInventory(is);
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
