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


import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;


public abstract class UpgradeInventory extends AppEngInternalInventory implements IAEAppEngInventory {
    private final IAEAppEngInventory parent;

    private boolean cached = false;
    private int fuzzyUpgrades = 0;
    private int speedUpgrades = 0;
    private int redstoneUpgrades = 0;
    private int capacityUpgrades = 0;
    private int inverterUpgrades = 0;
    private int craftingUpgrades = 0;
    private int patternExpansionUpgrades = 0;

    public UpgradeInventory(final IAEAppEngInventory parent, final int s) {
        super(null, s, 1);
        this.setTileEntity(this);
        this.parent = parent;
        this.setFilter(new UpgradeInvFilter());
    }

    @Override
    protected boolean eventsEnabled() {
        return true;
    }

    public int getInstalledUpgrades(final Upgrades u) {
        if (!this.cached) {
            this.updateUpgradeInfo();
        }

        switch (u) {
            case CAPACITY:
                return this.capacityUpgrades;
            case FUZZY:
                return this.fuzzyUpgrades;
            case REDSTONE:
                return this.redstoneUpgrades;
            case SPEED:
                return this.speedUpgrades;
            case INVERTER:
                return this.inverterUpgrades;
            case CRAFTING:
                return this.craftingUpgrades;
            case PATTERN_EXPANSION:
                return this.patternExpansionUpgrades;
            default:
                return 0;
        }
    }

    public abstract int getMaxInstalled(Upgrades upgrades);

    private void updateUpgradeInfo() {
        this.cached = true;
        this.patternExpansionUpgrades = this.inverterUpgrades = this.capacityUpgrades = this.redstoneUpgrades = this.speedUpgrades = this.fuzzyUpgrades = this.craftingUpgrades = 0;

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
                case PATTERN_EXPANSION:
                    this.patternExpansionUpgrades++;
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
        this.patternExpansionUpgrades = Math.min(this.patternExpansionUpgrades, this.getMaxInstalled(Upgrades.PATTERN_EXPANSION));
    }

    @Override
    public void readFromNBT(final NBTTagCompound target) {
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
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        this.cached = false;
        if (this.parent != null && Platform.isServer()) {
            this.parent.onChangeInventory(inv, slot, mc, removedStack, newStack);
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return super.extractItem(slot, amount, simulate);
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
