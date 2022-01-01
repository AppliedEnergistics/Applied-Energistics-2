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

package appeng.api.upgrades;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;

abstract class UpgradeInventory extends AppEngInternalInventory implements InternalInventoryHost, IUpgradeInventory {
    private final Item item;

    // Cache of which upgrades are installed
    @Nullable
    private Reference2IntMap<Item> installed = null;

    public UpgradeInventory(Item item, int slots) {
        super(null, slots, 1);
        this.item = item;
        this.setHost(this);
        this.setFilter(new UpgradeInvFilter());
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    protected boolean eventsEnabled() {
        return true;
    }

    @Override
    public int getMaxInstalled(ItemLike upgradeCard) {
        return Upgrades.getMaxInstallable(upgradeCard, item);
    }

    @Override
    public ItemLike getUpgradableItem() {
        return item;
    }

    @Override
    public int getInstalledUpgrades(ItemLike upgradeCard) {
        if (installed == null) {
            this.updateUpgradeInfo();
        }

        return installed.getOrDefault(upgradeCard.asItem(), 0);
    }

    private void updateUpgradeInfo() {
        this.installed = new Reference2IntArrayMap<>(size());

        for (var is : this) {
            var maxInstalled = getMaxInstalled(is.getItem());
            if (maxInstalled > 0) {
                this.installed.merge(is.getItem(), 1, (a, b) -> Math.min(maxInstalled, a + b));
            }
        }
    }

    @Override
    public void readFromNBT(CompoundTag data, String name) {
        super.readFromNBT(data, name);
        this.updateUpgradeInfo();
    }

    @Override
    public void saveChanges() {
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        this.installed = null;
    }

    @Override
    public void sendChangeNotification(int slot) {
        this.installed = null;
        super.sendChangeNotification(slot);
    }

    private class UpgradeInvFilter implements IAEItemFilter {

        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack itemstack) {
            var cardItem = itemstack.getItem();
            return getInstalledUpgrades(cardItem) < getMaxInstalled(cardItem);
        }
    }
}
