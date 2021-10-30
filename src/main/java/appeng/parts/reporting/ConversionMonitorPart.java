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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.helpers.PlayerSource;
import appeng.parts.PartModel;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.inv.PlayerInternalInventory;
import appeng.util.item.AEItemStack;

public class ConversionMonitorPart extends AbstractMonitorPart {

    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/item_conversion_monitor_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/item_conversion_monitor_on");
    @PartModels
    public static final ResourceLocation MODEL_LOCKED_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/item_conversion_monitor_locked_off");
    @PartModels
    public static final ResourceLocation MODEL_LOCKED_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/item_conversion_monitor_locked_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);
    public static final IPartModel MODELS_LOCKED_OFF = new PartModel(MODEL_BASE, MODEL_LOCKED_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_LOCKED_ON = new PartModel(MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_LOCKED_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_LOCKED_ON,
            MODEL_STATUS_HAS_CHANNEL);

    public ConversionMonitorPart(final ItemStack is) {
        super(is, true);
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (isRemote()) {
            return true;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getHost().getLocation(), player)) {
            return false;
        }

        final ItemStack eq = player.getItemInHand(hand);
        if (this.isLocked()) {
            if (eq.isEmpty()) {
                this.insertItem(player, hand, true);
            } else if (InteractionUtil.canWrenchRotate(eq)
                    && (this.getDisplayed() == null || !this.getDisplayed().equals(eq))) {
                // wrench it
                return super.onPartActivate(player, hand, pos);
            } else {
                this.insertItem(player, hand, false);
            }
        } else if (this.getDisplayed() != null && this.getDisplayed().equals(eq)) {
            this.insertItem(player, hand, false);
        } else {
            return super.onPartActivate(player, hand, pos);
        }

        return true;
    }

    @Override
    public boolean onClicked(Player player, InteractionHand hand, Vec3 pos) {
        if (isRemote()) {
            return true;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getHost().getLocation(), player)) {
            return false;
        }

        if (this.getDisplayed() != null) {
            this.extractItem(player, this.getDisplayed().getDefinition().getMaxStackSize());
        }

        return true;
    }

    @Override
    public boolean onShiftClicked(Player player, InteractionHand hand, Vec3 pos) {
        if (isRemote()) {
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

    private void insertItem(final Player player, final InteractionHand hand, final boolean allItems) {
        getMainNode().ifPresent(grid -> {
            var energy = grid.getEnergyService();
            var cell = grid.getStorageService().getInventory(StorageChannels.items());

            if (allItems) {
                if (this.getDisplayed() != null) {
                    var input = this.getDisplayed().copy();
                    var inv = new PlayerInternalInventory(player.getInventory());

                    for (int x = 0; x < inv.size(); x++) {
                        var targetStack = inv.getStackInSlot(x);
                        if (input.equals(targetStack)) {
                            var canExtract = inv.extractItem(x, targetStack.getCount(), true);
                            if (!canExtract.isEmpty()) {
                                input.setStackSize(canExtract.getCount());
                                final IAEItemStack failedToInsert = StorageHelper.poweredInsert(energy, cell, input,
                                        new PlayerSource(player, this));
                                inv.extractItem(x, failedToInsert == null ? canExtract.getCount()
                                        : canExtract.getCount() - (int) failedToInsert.getStackSize(), false);
                            }
                        }
                    }
                }
            } else {
                final IAEItemStack input = AEItemStack.fromItemStack(player.getItemInHand(hand));
                final IAEItemStack failedToInsert = StorageHelper.poweredInsert(energy, cell, input,
                        new PlayerSource(player, this));
                player.setItemInHand(hand, failedToInsert == null ? ItemStack.EMPTY : failedToInsert.createItemStack());
            }
        });
    }

    private void extractItem(final Player player, int count) {
        final IAEItemStack input = this.getDisplayed();
        if (input == null) {
            return;
        }

        if (!this.getMainNode().isActive()) {
            return;
        }

        getMainNode().ifPresent(grid -> {
            final IEnergySource energy = grid.getEnergyService();
            final IMEMonitor<IAEItemStack> cell = grid.getStorageService()
                    .getInventory(StorageChannels.items());

            input.setStackSize(count);

            final IAEItemStack retrieved = StorageHelper.poweredExtraction(energy, cell, input,
                    new PlayerSource(player, this));
            if (retrieved != null) {
                ItemStack newItems = retrieved.createItemStack();
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
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL, MODELS_LOCKED_OFF, MODELS_LOCKED_ON,
                MODELS_LOCKED_HAS_CHANNEL);
    }

}
