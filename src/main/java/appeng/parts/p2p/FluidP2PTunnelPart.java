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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;

public class FluidP2PTunnelPart extends P2PTunnelPart<FluidP2PTunnelPart> implements IFluidHandler {

    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_fluids");

    private static final ThreadLocal<Deque<FluidP2PTunnelPart>> DEPTH = new ThreadLocal<>();;

    private LazyOptional<IFluidHandler> cachedTank;
    private int tmpUsed;

    public FluidP2PTunnelPart(final ItemStack is) {
        super(is);
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public float getPowerDrainPerTick() {
        return 2.0f;
    }

    @Override
    public void onTunnelNetworkChange() {
        this.cachedTank = null;
    }

    @Override
    public void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor) {
        this.cachedTank = null;

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
            return (LazyOptional<T>) LazyOptional.of(() -> this);
        }

        return super.getCapability(capabilityClass);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

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
        return 1000;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {

        final Deque<FluidP2PTunnelPart> stack = this.getDepth();

        for (final FluidP2PTunnelPart t : stack) {
            if (t == this) {
                return 0;
            }
        }

        stack.push(this);

        final List<FluidP2PTunnelPart> list = this.getOutputs(resource.getFluid());
        int requestTotal = 0;

        Iterator<FluidP2PTunnelPart> i = list.iterator();

        while (i.hasNext()) {
            final FluidP2PTunnelPart l = i.next();
            final IFluidHandler tank = l.getTarget().orElse(null);
            if (tank != null) {
                l.tmpUsed = tank.fill(resource.copy(), FluidAction.SIMULATE);
            } else {
                l.tmpUsed = 0;
            }

            if (l.tmpUsed <= 0) {
                i.remove();
            } else {
                requestTotal += l.tmpUsed;
            }
        }

        if (requestTotal <= 0) {
            if (stack.pop() != this) {
                throw new IllegalStateException("Invalid Recursion detected.");
            }

            return 0;
        }

        if (action != FluidAction.EXECUTE) {
            if (stack.pop() != this) {
                throw new IllegalStateException("Invalid Recursion detected.");
            }

            return Math.min(resource.getAmount(), requestTotal);
        }

        int available = resource.getAmount();

        i = list.iterator();
        int used = 0;

        while (i.hasNext() && available > 0) {
            final FluidP2PTunnelPart l = i.next();

            final FluidStack insert = resource.copy();
            insert.setAmount((int) Math.ceil(insert.getAmount() * ((double) l.tmpUsed / (double) requestTotal)));
            if (insert.getAmount() > available) {
                insert.setAmount(available);
            }

            final IFluidHandler tank = l.getTarget().orElse(null);
            if (tank != null) {
                l.tmpUsed = tank.fill(insert.copy(), action);
            } else {
                l.tmpUsed = 0;
            }

            available -= insert.getAmount();
            used += l.tmpUsed;
        }

        if (stack.pop() != this) {
            throw new IllegalStateException("Invalid Recursion detected.");
        }

        return used;
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

    private Deque<FluidP2PTunnelPart> getDepth() {
        Deque<FluidP2PTunnelPart> s = DEPTH.get();

        if (s == null) {
            DEPTH.set(s = new ArrayDeque<>());
        }

        return s;
    }

    private List<FluidP2PTunnelPart> getOutputs(final Fluid input) {
        final List<FluidP2PTunnelPart> outs = new ArrayList<>();

        try {
            for (final FluidP2PTunnelPart l : this.getOutputs()) {
                final IFluidHandler handler = l.getTarget().orElse(null);

                if (handler != null) {
                    outs.add(l);
                }
            }
        } catch (final GridAccessException e) {
            // :P
        }

        return outs;
    }

    private LazyOptional<IFluidHandler> getTarget() {
        if (!this.getProxy().isActive()) {
            return null;
        }

        if (this.cachedTank != null) {
            return this.cachedTank;
        }

        final TileEntity te = this.getTile().getWorld()
                .getTileEntity(this.getTile().getPos().offset(this.getSide().getFacing()));

        if (te != null && te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                this.getSide().getFacing().getOpposite()).isPresent()) {
            return this.cachedTank = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    this.getSide().getFacing().getOpposite());
        }

        return null;
    }

}
