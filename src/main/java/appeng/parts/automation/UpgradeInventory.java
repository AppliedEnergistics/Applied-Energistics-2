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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.blockentity.inventory.AppEngInternalInventory;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;

public abstract class UpgradeInventory extends AppEngInternalInventory
        implements IAEAppEngInventory, IUpgradeInventory {
    private final IAEAppEngInventory parent;

    private boolean cached = false;
    private int fuzzyUpgrades = 0;
    private int speedUpgrades = 0;
    private int redstoneUpgrades = 0;
    private int capacityUpgrades = 0;
    private int inverterUpgrades = 0;
    private int craftingUpgrades = 0;

    public UpgradeInventory(final IAEAppEngInventory parent, final int slots) {
        super(null, slots, 1);
        this.setBlockEntity(this);
        this.parent = parent;
        this.setFilter(new UpgradeInvFilter());
    }

    @Override
    public boolean isRemote() {
        return this.parent == null || this.parent.isRemote();
    }

    @Override
    protected boolean eventsEnabled() {
        return true;
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
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

        for (final ItemStack is : this) {
            if (is == null || is.getItem() == Items.AIR || !(is.getItem() instanceof IUpgradeModule)) {
                continue;
            }

            final Upgrades myUpgrade = ((IUpgradeModule) is.getItem()).getType(is);
            switch (myUpgrade) {
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
    public void readFromNBT(final CompoundTag target) {
        super.readFromNBT(target);
        this.updateUpgradeInfo();
    }

    @Override
    public void saveChanges() {
        if (this.parent != null) {
            this.parent.saveChanges();
        }
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        this.cached = false;
        if (!isRemote()) {
            this.parent.onChangeInventory(inv, slot, mc, removedStack, newStack);
        }
    }

    private class UpgradeInvFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack itemstack) {
            if (itemstack.isEmpty()) {
                return false;
            }
            final Item it = itemstack.getItem();
            if (it instanceof IUpgradeModule) {
                final Upgrades u = ((IUpgradeModule) it).getType(itemstack);
                if (u != null) {
                    return UpgradeInventory.this.getInstalledUpgrades(u) < UpgradeInventory.this.getMaxInstalled(u);
                }
            }
            return false;
        }
    }
}
