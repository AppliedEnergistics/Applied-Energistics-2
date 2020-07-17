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

import java.util.Collections;
import java.util.List;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.SingleItemSlot;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.PlayerSource;
import appeng.parts.PartModel;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class ConversionMonitorPart extends AbstractMonitorPart {

    @PartModels
    public static final Identifier MODEL_OFF = new Identifier(AppEng.MOD_ID, "part/conversion_monitor_off");
    @PartModels
    public static final Identifier MODEL_ON = new Identifier(AppEng.MOD_ID, "part/conversion_monitor_on");
    @PartModels
    public static final Identifier MODEL_LOCKED_OFF = new Identifier(AppEng.MOD_ID,
            "part/conversion_monitor_locked_off");
    @PartModels
    public static final Identifier MODEL_LOCKED_ON = new Identifier(AppEng.MOD_ID,
            "part/conversion_monitor_locked_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);
    public static final IPartModel MODELS_LOCKED_OFF = new PartModel(MODEL_BASE, MODEL_LOCKED_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_LOCKED_ON = new PartModel(MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_LOCKED_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_LOCKED_ON,
            MODEL_STATUS_HAS_CHANNEL);


    public ConversionMonitorPart(final ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartActivate(PlayerEntity player, Hand hand, Vec3d pos) {
        if (Platform.isClient()) {
            return true;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getLocation(), player)) {
            return false;
        }

        final ItemStack eq = player.getStackInHand(hand);
        if (this.isLocked()) {
            if (eq.isEmpty()) {
                this.insertItem(player, hand, true);
            } else if (Platform.isWrench(player, eq, this.getLocation().getPos())
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
    public boolean onClicked(PlayerEntity player, Hand hand, Vec3d pos) {
        if (Platform.isClient()) {
            return true;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getLocation(), player)) {
            return false;
        }

        if (this.getDisplayed() != null) {
            this.extractItem(player, this.getDisplayed().getDefinition().getMaxCount());
        }

        return true;
    }

    @Override
    public boolean onShiftClicked(PlayerEntity player, Hand hand, Vec3d pos) {
        if (Platform.isClient()) {
            return true;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getLocation(), player)) {
            return false;
        }

        if (this.getDisplayed() != null) {
            this.extractItem(player, 1);
        }

        return true;
    }

    private void insertItem(final PlayerEntity player, final Hand hand, final boolean allItems) {
        try {
            final IEnergySource energy = this.getProxy().getEnergy();
            final IMEMonitor<IAEItemStack> cell = this.getProxy().getStorage()
                    .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));

            if (allItems) {
                if (this.getDisplayed() != null) {
                    final IAEItemStack input = this.getDisplayed().copy();
                    FixedItemInv inv = new FixedInventoryVanillaWrapper(player.inventory);

                    for (int x = 0; x < inv.getSlotCount(); x++) {
                        final ItemStack targetStack = inv.getInvStack(x);
                        if (input.equals(targetStack)) {
                            SingleItemSlot slot = inv.getSlot(x);
                            final ItemStack canExtract = slot.attemptAnyExtraction(targetStack.getCount(), Simulation.SIMULATE);
                            if (!canExtract.isEmpty()) {
                                input.setStackSize(canExtract.getCount());
                                final IAEItemStack failedToInsert = Platform.poweredInsert(energy, cell, input,
                                        new PlayerSource(player, this));
                                slot.extract(failedToInsert == null ? canExtract.getCount()
                                        : canExtract.getCount() - (int) failedToInsert.getStackSize());
                            }
                        }
                    }
                }
            } else {
                final IAEItemStack input = AEItemStack.fromItemStack(player.getStackInHand(hand));
                final IAEItemStack failedToInsert = Platform.poweredInsert(energy, cell, input,
                        new PlayerSource(player, this));
                player.setStackInHand(hand, failedToInsert == null ? ItemStack.EMPTY : failedToInsert.createItemStack());
            }
        } catch (final GridAccessException e) {
            // :P
        }
    }

    private void extractItem(final PlayerEntity player, int count) {
        final IAEItemStack input = this.getDisplayed();
        if (input != null) {
            try {
                if (!this.getProxy().isActive()) {
                    return;
                }

                final IEnergySource energy = this.getProxy().getEnergy();
                final IMEMonitor<IAEItemStack> cell = this.getProxy().getStorage()
                        .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));

                input.setStackSize(count);

                final IAEItemStack retrieved = Platform.poweredExtraction(energy, cell, input,
                        new PlayerSource(player, this));
                if (retrieved != null) {
                    ItemStack newItems = retrieved.createItemStack();
                    final InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor(player);
                    newItems = adaptor.addItems(newItems);
                    if (!newItems.isEmpty()) {
                        final BlockEntity te = this.getTile();
                        final List<ItemStack> list = Collections.singletonList(newItems);
                        Platform.spawnDrops(player.world, te.getPos().offset(this.getSide().getFacing()), list);
                    }

                    if (player.currentScreenHandler != null) {
                        player.currentScreenHandler.sendContentUpdates();
                    }
                }
            } catch (final GridAccessException e) {
                // :P
            }
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL, MODELS_LOCKED_OFF, MODELS_LOCKED_ON,
                MODELS_LOCKED_HAS_CHANNEL);
    }

}
