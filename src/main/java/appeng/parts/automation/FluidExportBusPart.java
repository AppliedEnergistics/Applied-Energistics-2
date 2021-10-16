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
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.menu.implementations.FluidIOBusMenu;
import appeng.parts.PartModel;

/**
 * @author BrockWS
 * @version rv6 - 30/04/2018
 * @since rv6 30/04/2018
 */
public class FluidExportBusPart extends SharedFluidBusPart {
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/fluid_export_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_export_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_export_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_export_bus_has_channel"));

    private final IActionSource source;

    public FluidExportBusPart(ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.CRAFT_ONLY, YesNo.NO);
        this.getConfigManager().registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
        this.source = new MachineSource(this);
    }

    @Override
    protected MenuType<?> getMenuType() {
        return FluidIOBusMenu.EXPORT_TYPE;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.FluidExportBus.getMin(), TickRates.FluidExportBus.getMax(),
                this.isSleeping(), false);
    }

    @Override
    protected TickRateModulation doBusWork() {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        var fh = this.getConnectedTE();
        var grid = getMainNode().getGrid();
        if (fh == null || grid == null) {
            return TickRateModulation.SLEEP;
        }

        var inv = grid.getStorageService().getInventory(this.getChannel());

        for (int i = 0; i < this.getConfig().getSlots(); i++) {
            IAEFluidStack fluid = this.getConfig().getFluidInSlot(i);
            if (fluid != null) {
                final IAEFluidStack toExtract = fluid.copy();

                toExtract.setStackSize(this.calculateAmountToSend());

                var out = inv.extractItems(toExtract, Actionable.SIMULATE, this.source);
                if (out != null) {
                    try (var tx = Transaction.openOuter()) {
                        long wasInserted = fh.insert(out.getFluid(), out.getStackSize(), tx);

                        if (wasInserted > 0) {
                            toExtract.setStackSize(wasInserted);
                            inv.extractItems(toExtract, Actionable.MODULATE, this.source);
                            tx.commit();

                            return TickRateModulation.FASTER;
                        }
                    }
                }
            }
        }

        return TickRateModulation.SLOWER;

    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
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
