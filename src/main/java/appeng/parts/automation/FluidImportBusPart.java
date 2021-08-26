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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

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
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
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

        final BlockEntity te = this.getConnectedTE();
        if (te != null) {
            LazyOptional<IFluidHandler> fhOpt = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    this.getSide().getOpposite());

            if (fhOpt.isPresent()) {
                var grid = getMainNode().getGrid();
                if (grid != null) {
                    final IFluidHandler fh = fhOpt.orElseThrow(IllegalStateException::new);
                    final IMEMonitor<IAEFluidStack> inv = grid.getStorageService()
                            .getInventory(this.getChannel());

                    final FluidStack fluidStack = fh.drain(this.calculateAmountToSend(), FluidAction.SIMULATE);

                    if (this.filterEnabled() && !this.isInFilter(fluidStack)) {
                        return TickRateModulation.SLOWER;
                    }

                    final AEFluidStack aeFluidStack = AEFluidStack.fromFluidStack(fluidStack);

                    if (aeFluidStack != null) {
                        final IAEFluidStack notInserted = inv.injectItems(aeFluidStack, Actionable.MODULATE,
                                this.source);

                        if (notInserted != null && notInserted.getStackSize() > 0) {
                            aeFluidStack.decStackSize(notInserted.getStackSize());
                        }

                        fh.drain(aeFluidStack.getFluidStack(), FluidAction.EXECUTE);

                        return TickRateModulation.FASTER;
                    }

                    return TickRateModulation.IDLE;
                }
            }
        }
        return TickRateModulation.SLEEP;
    }

    private boolean isInFilter(FluidStack fluid) {
        for (int i = 0; i < this.getConfig().getSlots(); i++) {
            final IAEFluidStack stack = this.getConfig().getFluidInSlot(i);
            if (stack != null && stack.equals(fluid)) {
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
