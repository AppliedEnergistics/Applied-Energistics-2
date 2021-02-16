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

package appeng.tile.misc;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.CopyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.util.IConfigManager;
import appeng.tile.AEBaseBlockEntity;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;

public class CellWorkbenchBlockEntity extends AEBaseBlockEntity
        implements IUpgradeableHost, IAEAppEngInventory, IConfigManagerHost {

    private final AppEngInternalInventory cell = new AppEngInternalInventory(this, 1);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 63);
    private final ConfigManager manager = new ConfigManager(this);

    private FixedItemInv cacheUpgrades = null;
    private FixedItemInv cacheConfig = null;
    private boolean locked = false;

    public CellWorkbenchBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
        this.manager.registerSetting(Settings.COPY_MODE, CopyMode.CLEAR_ON_REMOVE);
        this.cell.setEnableClientEvents(true);
    }

    public FixedItemInv getCellUpgradeInventory() {
        if (this.cacheUpgrades == null) {
            final ICellWorkbenchItem cell = this.getCell();
            if (cell == null) {
                return null;
            }

            final ItemStack is = this.cell.getInvStack(0);
            if (is.isEmpty()) {
                return null;
            }

            final FixedItemInv inv = cell.getUpgradesInventory(is);
            if (inv == null) {
                return null;
            }

            return this.cacheUpgrades = inv;
        }
        return this.cacheUpgrades;
    }

    public ICellWorkbenchItem getCell() {
        if (this.cell.getInvStack(0).isEmpty()) {
            return null;
        }

        if (this.cell.getInvStack(0).getItem() instanceof ICellWorkbenchItem) {
            return ((ICellWorkbenchItem) this.cell.getInvStack(0).getItem());
        }

        return null;
    }

    @Override
    public CompoundTag toTag(final CompoundTag data) {
        super.toTag(data);
        this.cell.writeToNBT(data, "cell");
        this.config.writeToNBT(data, "config");
        this.manager.writeToNBT(data);
        return data;
    }

    @Override
    public void fromTag(final CompoundTag data) {
        super.fromTag(data);
        this.cell.readFromNBT(data, "cell");
        this.config.readFromNBT(data, "config");
        this.manager.readFromNBT(data);
    }

    @Override
    public FixedItemInv getInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }

        if (name.equals("cell")) {
            return this.cell;
        }

        return null;
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return 0;
    }

    @Override
    public void onChangeInventory(final FixedItemInv inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.cell && !this.locked) {
            this.locked = true;

            this.cacheUpgrades = null;
            this.cacheConfig = null;

            final FixedItemInv configInventory = this.getCellConfigInventory();
            if (configInventory != null) {
                boolean cellHasConfig = false;
                for (int x = 0; x < configInventory.getSlotCount(); x++) {
                    if (!configInventory.getInvStack(x).isEmpty()) {
                        cellHasConfig = true;
                        break;
                    }
                }

                if (cellHasConfig) {
                    for (int x = 0; x < this.config.getSlotCount(); x++) {
                        this.config.forceSetInvStack(x, configInventory.getInvStack(x));
                    }
                } else {
                    ItemHandlerUtil.copy(this.config, configInventory, false);
                }
            } else if (this.manager.getSetting(Settings.COPY_MODE) == CopyMode.CLEAR_ON_REMOVE) {
                for (int x = 0; x < this.config.getSlotCount(); x++) {
                    this.config.forceSetInvStack(x, ItemStack.EMPTY);
                }

                this.saveChanges();
            }

            this.locked = false;
        } else if (inv == this.config && !this.locked) {
            this.locked = true;
            final FixedItemInv c = this.getCellConfigInventory();
            if (c != null) {
                ItemHandlerUtil.copy(this.config, c, false);
                // copy items back. The ConfigInventory may changed the items on insert
                ItemHandlerUtil.copy(c, this.config, false);
            }
            this.locked = false;
        }
    }

    private FixedItemInv getCellConfigInventory() {
        if (this.cacheConfig == null) {
            final ICellWorkbenchItem cell = this.getCell();
            if (cell == null) {
                return null;
            }

            final ItemStack is = this.cell.getInvStack(0);
            if (is.isEmpty()) {
                return null;
            }

            final FixedItemInv inv = cell.getConfigInventory(is);
            if (inv == null) {
                return null;
            }

            this.cacheConfig = inv;
        }
        return this.cacheConfig;
    }

    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        super.getDrops(w, pos, drops);

        if (this.cell.getInvStack(0) != null) {
            drops.add(this.cell.getInvStack(0));
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.manager;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
        // nothing here..
    }
}
