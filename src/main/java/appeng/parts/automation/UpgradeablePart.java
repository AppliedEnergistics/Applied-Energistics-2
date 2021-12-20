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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.inventories.InternalInventory;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.items.parts.PartItem;
import appeng.parts.BasicStatePart;
import appeng.util.ConfigManager;
import appeng.util.inv.InternalInventoryHost;

public abstract class UpgradeablePart extends BasicStatePart
        implements InternalInventoryHost, IConfigurableObject, IUpgradeableObject {
    private final IConfigManager config;
    private final UpgradeInventory upgrades;

    public UpgradeablePart(PartItem<?> is) {
        super(is);
        this.upgrades = new StackUpgradeInventory(getPartItem(), this, this.getUpgradeSlots());
        this.config = new ConfigManager(this::onSettingChanged);
    }

    protected int getUpgradeSlots() {
        return 4;
    }

    @Override
    public void saveChanges() {
        getHost().markForSave();
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.upgrades) {
            this.upgradesChanged();
        }
    }

    public void upgradesChanged() {

    }

    protected boolean isSleeping() {
        if (upgrades.getInstalledUpgrades(Upgrades.REDSTONE) > 0) {
            return switch (this.getRSMode()) {
                case IGNORE -> false;
                case HIGH_SIGNAL -> !this.getHost().hasRedstone();
                case LOW_SIGNAL -> this.getHost().hasRedstone();
                case SIGNAL_PULSE -> true;
            };
        }

        return false;
    }

    @Override
    public boolean canConnectRedstone() {
        return this.upgrades.getMaxInstalled(Upgrades.REDSTONE) > 0;
    }

    @Override
    public void readFromNBT(final CompoundTag extra) {
        super.readFromNBT(extra);
        this.config.readFromNBT(extra);
        this.upgrades.readFromNBT(extra, "upgrades");
    }

    @Override
    public void writeToNBT(final CompoundTag extra) {
        super.writeToNBT(extra);
        this.config.writeToNBT(extra);
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
        return this.config;
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(UPGRADES)) {
            return upgrades;
        } else {
            return super.getSubInventory(id);
        }
    }

    @Nonnull
    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    protected final int getInstalledUpgrades(Upgrades u) {
        return upgrades.getInstalledUpgrades(u);
    }

    public RedstoneMode getRSMode() {
        return null;
    }

    protected void onSettingChanged(IConfigManager manager, Setting<?> setting) {
    }

}
