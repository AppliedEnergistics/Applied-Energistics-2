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

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.inventories.InternalInventory;
import appeng.api.parts.IPartItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.definitions.AEItems;
import appeng.parts.BasicStatePart;
import appeng.util.ConfigManager;

public abstract class UpgradeablePart extends BasicStatePart
        implements IConfigurableObject, IUpgradeableObject {
    private final IConfigManager config;
    private final IUpgradeInventory upgrades;

    public UpgradeablePart(IPartItem<?> partItem) {
        super(partItem);
        this.upgrades = UpgradeInventories.forMachine(partItem.asItem(), this.getUpgradeSlots(),
                this::onUpgradesChanged);
        this.config = new ConfigManager(this::onSettingChanged);
    }

    private void onUpgradesChanged() {
        getHost().markForSave();
        upgradesChanged();
    }

    protected int getUpgradeSlots() {
        return 4;
    }

    public void upgradesChanged() {

    }

    protected boolean isSleeping() {
        if (upgrades.isInstalled(AEItems.REDSTONE_CARD)) {
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
        return this.upgrades.getMaxInstalled(AEItems.REDSTONE_CARD) > 0;
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        this.config.readFromNBT(extra);
        this.upgrades.readFromNBT(extra, "upgrades");
    }

    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        this.config.writeToNBT(extra);
        this.upgrades.writeToNBT(extra, "upgrades");
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        for (var is : this.upgrades) {
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

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public RedstoneMode getRSMode() {
        return null;
    }

    protected void onSettingChanged(IConfigManager manager, Setting<?> setting) {
    }

}
