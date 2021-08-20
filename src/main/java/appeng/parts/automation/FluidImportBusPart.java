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

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.menu.implementations.FluidIOBusMenu;
import appeng.parts.PartModel;
import appeng.util.fluid.AEFluidStack;

/**
 * @author BrockWS
 * @version rv6 - 30/04/2018
 * @since rv6 30/04/2018
 */
public class FluidImportBusPart extends SharedFluidBusPart {
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

    private final IActionSource source;

    public FluidImportBusPart(ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.CRAFT_ONLY, YesNo.NO);
        this.getConfigManager().registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
        this.source = new MachineSource(this);
    }

    @Override
    protected MenuType<?> getMenuType() {
        return FluidIOBusMenu.IMPORT_TYPE;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.FluidImportBus.getMin(), TickRates.FluidImportBus.getMax(),
                this.isSleeping(), false);
    }

    @Override
    protected TickRateModulation doBusWork() {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        var grid = getMainNode().getGrid();
        var adjacentStorage = this.getConnectedTE();
        if (adjacentStorage == null || grid == null) {
            return TickRateModulation.SLEEP;
        }

        long remainingTransferAmount = calculateAmountToSend();

        var inv = grid.getStorageService().getInventory(this.getChannel());
        try (var tx = Transaction.openOuter()) {

            // Try to find an extractable resource that fits our filter, and if we've found at least one,
            // continue until we've filled the desired amount per transfer
            IAEFluidStack extractable = null;
            for (StorageView<FluidVariant> view : adjacentStorage.iterable(tx)) {
                var resource = view.getResource();
                if (resource.isBlank()
                        // After the first extractable resource, we're just trying to get enough to fill our
                        // transfer quota.
                        || extractable != null && !extractable.getFluid().equals(resource)
                        // Regard a filter that is set on the bus
                        || this.filterEnabled() && !this.isInFilter(resource)) {
                    continue;
                }

                // Check how much of *this* resource we can actually insert into the network, it might be 0
                // if the cells are partitioned or there's not enough types left, etc.
                long amountForThisResource = remainingTransferAmount;
                var overflow = inv.injectItems(AEFluidStack.of(resource, amountForThisResource), Actionable.SIMULATE,
                        this.source);
                if (overflow != null) {
                    amountForThisResource -= overflow.getStackSize();
                }

                // Try to extract it
                var amount = view.extract(resource, amountForThisResource, tx);
                if (amount > 0) {
                    if (extractable != null) {
                        extractable.incStackSize(amount);
                    } else {
                        extractable = AEFluidStack.of(resource, amount);
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

            var notInserted = inv.injectItems(extractable, Actionable.MODULATE, this.source);

            if (notInserted != null && notInserted.getStackSize() > 0) {
                // Be nice and try to give the overflow back
                AELog.warn("Extracted %s from adjacent tank and voided it because network refused insert",
                        extractable);
            }

            tx.commit();
            return TickRateModulation.FASTER;
        }
    }

    private boolean isInFilter(FluidVariant fluid) {
        for (int i = 0; i < this.getConfig().getSlots(); i++) {
            final IAEFluidStack stack = this.getConfig().getFluidInSlot(i);
            if (stack != null && stack.getFluid().equals(fluid)) {
                return true;
            }
        }
        return false;
    }

    private boolean filterEnabled() {
        for (int i = 0; i < this.getConfig().getSlots(); i++) {
            final IAEFluidStack stack = this.getConfig().getFluidInSlot(i);
            if (stack != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RedstoneMode getRSMode() {
        return this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
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
