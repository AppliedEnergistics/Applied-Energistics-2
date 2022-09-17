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


import appeng.api.config.RedstoneMode;
import appeng.api.config.Upgrades;
import appeng.api.util.IConfigManager;
import appeng.parts.PartBasicState;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.List;


public abstract class PartUpgradeable extends PartBasicState implements IAEAppEngInventory, IConfigManagerHost {
    private final IConfigManager manager;
    private final UpgradeInventory upgrades;

    public PartUpgradeable(final ItemStack is) {
        super(is);
        this.upgrades = new StackUpgradeInventory(this.getItemStack(), this, this.getUpgradeSlots());
        this.manager = new ConfigManager(this);
    }

    protected int getUpgradeSlots() {
        return 4;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {

    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.upgrades) {
            this.upgradesChanged();
        }
    }

    public void upgradesChanged() {

    }

    protected boolean isSleeping() {
        if (this.getInstalledUpgrades(Upgrades.REDSTONE) > 0) {
            switch (this.getRSMode()) {
                case IGNORE:
                    return false;

                case HIGH_SIGNAL:
                    if (this.getHost().hasRedstone(this.getSide())) {
                        return false;
                    }

                    break;

                case LOW_SIGNAL:
                    if (!this.getHost().hasRedstone(this.getSide())) {
                        return false;
                    }

                    break;

                case SIGNAL_PULSE:
                default:
                    break;
            }

            return true;
        }

        return false;
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return this.upgrades.getInstalledUpgrades(u);
    }

    @Override
    public boolean canConnectRedstone() {
        return this.upgrades.getMaxInstalled(Upgrades.REDSTONE) > 0;
    }

    @Override
    public void readFromNBT(final net.minecraft.nbt.NBTTagCompound extra) {
        super.readFromNBT(extra);
        this.manager.readFromNBT(extra);
        this.upgrades.readFromNBT(extra, "upgrades");
    }

    @Override
    public void writeToNBT(final net.minecraft.nbt.NBTTagCompound extra) {
        super.writeToNBT(extra);
        this.manager.writeToNBT(extra);
        this.upgrades.writeToNBT(extra, "upgrades");
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        for (final ItemStack is : this.upgrades) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.manager;
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("upgrades")) {
            return this.upgrades;
        }

        return null;
    }

    public RedstoneMode getRSMode() {
        return null;
    }
}
