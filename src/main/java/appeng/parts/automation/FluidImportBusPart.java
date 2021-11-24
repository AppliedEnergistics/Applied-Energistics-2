/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

import javax.annotation.Nonnull;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartModel;
import appeng.api.storage.AEKeySpace;
import appeng.api.storage.data.AEFluidKey;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.menu.implementations.IOBusMenu;
import appeng.parts.PartModel;

public class FluidImportBusPart extends ImportBusPart<AEFluidKey, Storage<FluidVariant>> {
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/fluid_import_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_import_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_import_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_import_bus_has_channel"));

    public FluidImportBusPart(ItemStack is) {
        super(TickRates.FluidImportBus, is, FluidStorage.SIDED, AEFluidKey.filter());
    }

    @Override
    protected AEKeySpace getChannel() {
        return AEKeySpace.fluids();
    }

    @Override
    protected MenuType<?> getMenuType() {
        return IOBusMenu.FLUID_IMPORT_TYPE;
    }

    @Override
    protected TickRateModulation doBusWork() {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        var grid = getMainNode().getGrid();
        var adjacentStorage = adjacentExternalApi.find();
        if (adjacentStorage == null || grid == null) {
            return TickRateModulation.SLEEP;
        }

        long remainingTransferAmount = calculateAmountPerTick();

        var inv = grid.getStorageService().getInventory();
        try (var tx = Transaction.openOuter()) {

            // Try to find an extractable resource that fits our filter, and if we've found at least one,
            // continue until we've filled the desired amount per transfer
            AEFluidKey extractable = null;
            long extractableAmount = 0;
            for (var view : adjacentStorage.iterable(tx)) {
                var resource = view.getResource();
                if (resource.isBlank()
                        // After the first extractable resource, we're just trying to get enough to fill our
                        // transfer quota.
                        || extractable != null && !extractable.matches(resource)
                        // Regard a filter that is set on the bus
                        || this.filterEnabled() && !this.isInFilter(resource)) {
                    continue;
                }

                // Check how much of *this* resource we can actually insert into the network, it might be 0
                // if the cells are partitioned or there's not enough types left, etc.
                var amountForThisResource = inv.insert(AEFluidKey.of(resource), remainingTransferAmount,
                        Actionable.SIMULATE,
                        this.source);

                // Try to extract it
                var amount = view.extract(resource, amountForThisResource, tx);
                if (amount > 0) {
                    if (extractable != null) {
                        extractableAmount += amount;
                    } else {
                        extractable = AEFluidKey.of(resource);
                        extractableAmount += amount;
                    }
                    remainingTransferAmount -= amount;
                    if (remainingTransferAmount >= 0) {
                        // We got enough to fill our transfer quota
                        break;
                    }
                }
            }

            // We might have found nothing to extract
            if (extractable == null) {
                return TickRateModulation.SLOWER;
            }

            var inserted = inv.insert(extractable, extractableAmount, Actionable.MODULATE, this.source);

            if (inserted < extractableAmount) {
                // Be nice and try to give the overflow back
                AELog.warn("Extracted %dx%s from adjacent tank and voided it because network refused insert",
                        extractableAmount - inserted, extractable);
            }

            tx.commit();
            return TickRateModulation.FASTER;
        }
    }

    private boolean isInFilter(FluidVariant fluid) {
        for (var i = 0; i < this.getConfig().size(); i++) {
            var what = this.getConfig().getKey(i);
            if (AEFluidKey.matches(what, fluid)) {
                return true;
            }
        }
        return false;
    }

    private boolean filterEnabled() {
        return !getConfig().isEmpty();
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }
}
