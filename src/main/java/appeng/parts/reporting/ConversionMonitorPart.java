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

package appeng.parts.reporting;

import appeng.parts.automation.PartModelData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.ISubMenuHost;
import appeng.api.storage.StorageHelper;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.helpers.PlayerSource;
import appeng.menu.ISubMenu;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.parts.PartModel;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.inv.PlayerInternalInventory;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class ConversionMonitorPart extends AbstractMonitorPart implements ISubMenuHost {
    public ConversionMonitorPart(IPartItem<?> partItem) {
        super(partItem, true);
    }

    @Override
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        if (isClientSide()) {
            return true;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        if (this.isLocked() && !InteractionUtil.isInAlternateUseMode(player)) {
            if (InteractionUtil.canWrenchRotate(heldItem)
                    && (this.getDisplayed() == null || !AEItemKey.matches(getDisplayed(), heldItem))) {
                // wrench it
                return super.onUseWithoutItem(player, pos);
            } else if (!heldItem.isEmpty()) {
                this.insertItem(player, heldItem);
                return true;
            }
        } else if (this.getDisplayed() != null && AEItemKey.matches(getDisplayed(), heldItem)) {
            this.insertItem(player, heldItem);
            return true;
        }

        return super.onUseItemOn(heldItem, player, hand, pos);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (this.isLocked() && !InteractionUtil.isInAlternateUseMode(player)) {
            if (isClientSide()) {
                return true;
            }
            if (!this.getMainNode().isActive()) {
                return false;
            }
            this.insertAllItem(player);
            return true;
        }

        return super.onUseWithoutItem(player, pos);
    }

    @Override
    public boolean onClicked(Player player, Vec3 pos) {
        if (isClientSide()) {
            return true;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getHost().getLocation(), player)) {
            return false;
        }

        if (this.getDisplayed() instanceof AEItemKey itemKey) {
            this.extractItem(player, itemKey.getMaxStackSize());
        }

        return true;
    }

    @Override
    public boolean onShiftClicked(Player player, Vec3 pos) {
        if (isClientSide()) {
            return true;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getHost().getLocation(), player)) {
            return false;
        }

        if (this.getDisplayed() != null) {
            this.extractItem(player, 1);
        }

        return true;
    }

    private void insertAllItem(Player player) {
        getMainNode().ifPresent(grid -> {
            var energy = grid.getEnergyService();
            var cell = grid.getStorageService().getInventory();

            if (getDisplayed() instanceof AEItemKey itemKey) {
                var inv = new PlayerInternalInventory(player.getInventory());

                for (int x = 0; x < inv.size(); x++) {
                    var targetStack = inv.getStackInSlot(x);
                    if (itemKey.matches(targetStack)) {
                        var canExtract = inv.extractItem(x, targetStack.getCount(), true);
                        if (!canExtract.isEmpty()) {
                            var inserted = StorageHelper.poweredInsert(energy, cell, itemKey, canExtract.getCount(),
                                    new PlayerSource(player, this));
                            inv.extractItem(x, (int) inserted, false);
                        }
                    }
                }
            }
        });
    }

    private void insertItem(Player player, ItemStack heldItem) {
        getMainNode().ifPresent(grid -> {
            var energy = grid.getEnergyService();
            var cell = grid.getStorageService().getInventory();

            var inserted = StorageHelper.poweredInsert(energy, cell, AEItemKey.of(heldItem), heldItem.getCount(),
                    new PlayerSource(player, this));
            heldItem.shrink((int) inserted);
        });
    }

    private void extractItem(Player player, int count) {
        if (!(this.getDisplayed() instanceof AEItemKey itemKey)) {
            return;
        }

        if (!this.getMainNode().isActive()) {
            return;
        }

        if (getAmount() == 0 && canCraft()) {
            CraftAmountMenu.open((ServerPlayer) player, MenuLocators.forPart(this), itemKey,
                    itemKey.getAmountPerUnit());
            return;
        }

        getMainNode().ifPresent(grid -> {
            var energy = grid.getEnergyService();
            var cell = grid.getStorageService().getInventory();

            var retrieved = StorageHelper.poweredExtraction(energy, cell, itemKey, count,
                    new PlayerSource(player, this));
            if (retrieved != 0) {
                var newItems = itemKey.toStack((int) retrieved);
                if (!player.getInventory().add(newItems)) {
                    player.drop(newItems, false);
                }

                if (player.containerMenu != null) {
                    player.containerMenu.broadcastChanges();
                }
            }
        });
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        player.closeContainer();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getPartItem());
    }
}
