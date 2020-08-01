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

package appeng.fluids.parts;

import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.ConstantFluidFilter;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.attributes.MEAttributes;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.fluids.util.AEFluidStack;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.PartModel;

/**
 * @author BrockWS
 * @version rv6 - 30/04/2018
 * @since rv6 30/04/2018
 */
public class FluidImportBusPart extends SharedFluidBusPart {
    public static final Identifier MODEL_BASE = new Identifier(AppEng.MOD_ID, "part/fluid_import_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new Identifier(AppEng.MOD_ID, "part/fluid_import_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new Identifier(AppEng.MOD_ID, "part/fluid_import_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new Identifier(AppEng.MOD_ID, "part/fluid_import_bus_has_channel"));

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
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.FluidImportBus.getMin(), TickRates.FluidImportBus.getMax(),
                this.isSleeping(), false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        return this.canDoBusWork() ? this.doBusWork() : TickRateModulation.IDLE;
    }

    @Override
    protected TickRateModulation doBusWork() {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        FluidExtractable extractable = MEAttributes.getAttributeInFrontOfPart(FluidAttributes.EXTRACTABLE, this);
        if (extractable != null) {
            try {
                IMEMonitor<IAEFluidStack> inv = this.getProxy().getStorage().getInventory(this.getChannel());

                // Ask the target for fluid matching our filter
                FluidFilter filter = getFilter();
                FluidVolume extractableVolume = extractable.attemptExtraction(filter,
                        FluidAmount.of(this.calculateAmountToSend(), 1000), Simulation.SIMULATE);

                // Also double check that it's valid w.r.t. our filter
                if (extractableVolume.isEmpty() || !filter.matches(extractableVolume.fluidKey)) {
                    return TickRateModulation.SLOWER;
                }

                // Round down when inserting fluids into the system to avoid duplication
                final AEFluidStack aeFluidStack = AEFluidStack.fromFluidVolume(extractableVolume, RoundingMode.DOWN);

                if (aeFluidStack != null) {
                    final IAEFluidStack notInserted = inv.injectItems(aeFluidStack, Actionable.MODULATE, this.source);

                    if (notInserted != null && notInserted.getStackSize() > 0) {
                        aeFluidStack.decStackSize(notInserted.getStackSize());
                    }

                    // Now we need to actually drain the fluid, and use the actual amount we just
                    // inserted
                    extractable.extract(filter, aeFluidStack.getAmount()); // FIXME: If there's a mismatch here, log?

                    return TickRateModulation.FASTER;
                }

                return TickRateModulation.IDLE;
            } catch (GridAccessException e) {
                e.printStackTrace();
            }
        }

        return TickRateModulation.SLEEP;
    }

    @Override
    protected boolean canDoBusWork() {
        return this.getProxy().isActive();
    }

    // Returns a filter for fluid extraction based on the configured filter for this
    // bus
    private FluidFilter getFilter() {
        Set<FluidKey> allowedFluids = null;

        for (int i = 0; i < this.getConfig().getSlots(); i++) {
            final IAEFluidStack stack = this.getConfig().getFluidInSlot(i);
            if (stack != null) {
                if (allowedFluids == null) {
                    allowedFluids = new HashSet<>();
                }
                allowedFluids.add(stack.getFluid());
            }
        }
        return allowedFluids == null ? ConstantFluidFilter.ANYTHING : allowedFluids::contains;
    }

    @Override
    public RedstoneMode getRSMode() {
        return (RedstoneMode) this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
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
