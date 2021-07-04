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

package appeng.parts.p2p;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;

public class FluidP2PTunnelPart extends P2PTunnelPart<FluidP2PTunnelPart> {

    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_fluids");
    private static final IFluidHandler NULL_FLUID_HANDLER = new NullFluidHandler();

    private final IFluidHandler inputHandler = new InputFluidHandler();
    private final IFluidHandler outputHandler = new OutputFluidHandler();

    public FluidP2PTunnelPart(final ItemStack is) {
        super(is);
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 2.0f;
    }

    @Override
    public void onTunnelNetworkChange() {
    }

    @Override
    public void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor) {
        if (this.isOutput()) {
            final FluidP2PTunnelPart in = this.getInput();
            if (in != null) {
                in.onTunnelNetworkChange();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capabilityClass) {
        if (capabilityClass == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (this.isOutput()) {
                return (LazyOptional<T>) LazyOptional.of(() -> this.outputHandler);
            }
            return (LazyOptional<T>) LazyOptional.of(() -> this.inputHandler);
        }

        return super.getCapability(capabilityClass);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private IFluidHandler getAttachedFluidHandler() {
        LazyOptional<IFluidHandler> fluidHandler = LazyOptional.empty();
        if (this.isActive()) {
            final TileEntity self = this.getTile();
            final TileEntity te = self.getWorld().getTileEntity(self.getPos().offset(this.getSide().getDirection()));

            if (te != null) {
                fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                        this.getSide().getOpposite().getDirection());
            }
        }
        return fluidHandler.orElse(NULL_FLUID_HANDLER);
    }

    private class InputFluidHandler implements IFluidHandler {

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        @Nonnull
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int total = 0;

            try {
                final int outputTunnels = FluidP2PTunnelPart.this.getOutputs().size();
                final int amount = resource.getAmount();

                if (outputTunnels == 0 || amount == 0) {
                    return 0;
                }

                final int amountPerOutput = Math.max(1, amount / outputTunnels);
                int overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;

                for (FluidP2PTunnelPart target : FluidP2PTunnelPart.this.getOutputs()) {
                    final IFluidHandler output = target.getAttachedFluidHandler();
                    final int toSend = amountPerOutput + overflow;
                    final FluidStack fillWithFluidStack = resource.copy();
                    fillWithFluidStack.setAmount(toSend);

                    final int received = output.fill(fillWithFluidStack, action);

                    overflow = toSend - received;
                    total += received;
                }

                if (action == FluidAction.EXECUTE) {
                    FluidP2PTunnelPart.this.queueTunnelDrain(PowerUnits.RF, total);
                }
            } catch (GridAccessException ignored) {
            }

            return total;
        }

        @Override
        @Nonnull
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        @Nonnull
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }

    }

    private class OutputFluidHandler implements IFluidHandler {

        @Override
        public int getTanks() {
            return FluidP2PTunnelPart.this.getInput().getAttachedFluidHandler().getTanks();
        }

        @Override
        @Nonnull
        public FluidStack getFluidInTank(int tank) {
            return FluidP2PTunnelPart.this.getInput().getAttachedFluidHandler().getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return FluidP2PTunnelPart.this.getInput().getAttachedFluidHandler().getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return FluidP2PTunnelPart.this.getInput().getAttachedFluidHandler().isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        @Nonnull
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidP2PTunnelPart.this.getInput().getAttachedFluidHandler().drain(resource, action);
        }

        @Override
        @Nonnull
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidP2PTunnelPart.this.getInput().getAttachedFluidHandler().drain(maxDrain, action);
        }
    }

    private static class NullFluidHandler implements IFluidHandler {

        @Override
        public int getTanks() {
            return 0;
        }

        @Override
        @Nonnull
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        @Nonnull
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        @Nonnull
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

}
