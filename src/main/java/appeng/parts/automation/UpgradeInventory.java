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

package appeng.parts.automation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;

public abstract class UpgradeInventory extends AppEngInternalInventory
        implements InternalInventoryHost, IUpgradeInventory {
    private final InternalInventoryHost parent;

    private boolean cached = false;
    private int fuzzyUpgrades = 0;
    private int speedUpgrades = 0;
    private int redstoneUpgrades = 0;
    private int capacityUpgrades = 0;
    private int inverterUpgrades = 0;
    private int craftingUpgrades = 0;

    public UpgradeInventory(InternalInventoryHost parent, int slots) {
        super(null, slots, 1);
        this.setHost(this);
        this.parent = parent;
        this.setFilter(new UpgradeInvFilter());
    }

    @Override
    public boolean isClientSide() {
        return this.parent == null || this.parent.isClientSide();
    }

    @Override
    protected boolean eventsEnabled() {
        return true;
    }

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        if (!this.cached) {
            this.updateUpgradeInfo();
        }

        return switch (u) {
            case CAPACITY -> this.capacityUpgrades;
            case FUZZY -> this.fuzzyUpgrades;
            case REDSTONE -> this.redstoneUpgrades;
            case SPEED -> this.speedUpgrades;
            case INVERTER -> this.inverterUpgrades;
            case CRAFTING -> this.craftingUpgrades;
            default -> 0;
        };
    }

    @Override
    public abstract int getMaxInstalled(Upgrades upgrades);

    private void updateUpgradeInfo() {
        this.cached = true;
        this.inverterUpgrades = this.capacityUpgrades = this.redstoneUpgrades = this.speedUpgrades = this.fuzzyUpgrades = this.craftingUpgrades = 0;

        for (var is : this) {
            var upgradeType = IUpgradeModule.getTypeFromStack(is);
            if (upgradeType == null) {
                continue;
            }

            switch (upgradeType) {
                case CAPACITY:
                    this.capacityUpgrades++;
                    break;
                case FUZZY:
                    this.fuzzyUpgrades++;
                    break;
                case REDSTONE:
                    this.redstoneUpgrades++;
                    break;
                case SPEED:
                    this.speedUpgrades++;
                    break;
                case INVERTER:
                    this.inverterUpgrades++;
                    break;
                case CRAFTING:
                    this.craftingUpgrades++;
                    break;
                default:
                    break;
            }
        }

        this.capacityUpgrades = Math.min(this.capacityUpgrades, this.getMaxInstalled(Upgrades.CAPACITY));
        this.fuzzyUpgrades = Math.min(this.fuzzyUpgrades, this.getMaxInstalled(Upgrades.FUZZY));
        this.redstoneUpgrades = Math.min(this.redstoneUpgrades, this.getMaxInstalled(Upgrades.REDSTONE));
        this.speedUpgrades = Math.min(this.speedUpgrades, this.getMaxInstalled(Upgrades.SPEED));
        this.inverterUpgrades = Math.min(this.inverterUpgrades, this.getMaxInstalled(Upgrades.INVERTER));
        this.craftingUpgrades = Math.min(this.craftingUpgrades, this.getMaxInstalled(Upgrades.CRAFTING));
    }

    @Override
    public void readFromNBT(CompoundTag data, String name) {
        super.readFromNBT(data, name);
        this.updateUpgradeInfo();
    }

    @Override
    public void saveChanges() {
        if (this.parent != null) {
            this.parent.saveChanges();
        }
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.cached = false;
        if (!isClientSide()) {
            this.parent.onChangeInventory(inv, slot);
        }
    }

    @Override
    public void sendChangeNotification(int slot) {
        this.cached = false;
        super.sendChangeNotification(slot);
    }

    private class UpgradeInvFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack itemstack) {
            var u = IUpgradeModule.getTypeFromStack(itemstack);
            if (u != null) {
                return UpgradeInventory.this.getInstalledUpgrades(u) < UpgradeInventory.this.getMaxInstalled(u);
            }
            return false;
        }
    }
}
