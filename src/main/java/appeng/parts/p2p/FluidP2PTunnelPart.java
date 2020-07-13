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

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.*;

public class FluidP2PTunnelPart extends P2PTunnelPart<FluidP2PTunnelPart> implements FluidInsertable {

    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_fluids");

    private static final ThreadLocal<Deque<FluidP2PTunnelPart>> DEPTH = new ThreadLocal<>();;

    private FluidInsertable cachedTank;

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
    public void onneighborUpdate(BlockView w, BlockPos pos, BlockPos neighbor) {
        this.cachedTank = null;

        if (this.isOutput()) {
            final FluidP2PTunnelPart in = this.getInput();
            if (in != null) {
                in.onTunnelNetworkChange();
            }
        }
    }

    @Override
    public void addAllAttributes(AttributeList<?> to) {
        to.offer(this);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public FluidVolume attemptInsertion(FluidVolume fluid, Simulation simulation) {

        final Deque<FluidP2PTunnelPart> stack = this.getDepth();

        for (final FluidP2PTunnelPart t : stack) {
            if (t == this) {
                return fluid;
            }
        }

        stack.push(this);

        final List<FluidP2PTunnelPart> list = this.getOutputs(fluid.getFluidKey());
        FluidAmount requestTotal = FluidAmount.ZERO;

        Iterator<FluidP2PTunnelPart> i = list.iterator();

        while (i.hasNext()) {
            final FluidP2PTunnelPart l = i.next();
            final FluidInsertable tank = l.getTarget();
            FluidAmount inserted;
            if (tank != null) {
                inserted = fluid.amount().sub(tank.attemptInsertion(fluid.copy(), Simulation.SIMULATE).amount());
            } else {
                inserted = FluidAmount.ZERO;
            }

            if (inserted.isZero()) {
                i.remove();
            } else {
                requestTotal = requestTotal.add(inserted);
            }
        }

        if (requestTotal.isZero()) {
            if (stack.pop() != this) {
                throw new IllegalStateException("Invalid Recursion detected.");
            }

            return fluid;
        }

        if (simulation != Simulation.ACTION) {
            if (stack.pop() != this) {
                throw new IllegalStateException("Invalid Recursion detected.");
            }

            return fluid.copy().split(fluid.amount().min(requestTotal));
        }

        FluidAmount remaining = fluid.amount();

        i = list.iterator();

        while (i.hasNext() && !remaining.isZero()) {
            final FluidP2PTunnelPart l = i.next();

            FluidVolume insert = fluid.withAmount(
                    fluid.amount().div(list.size())
            );
            if (insert.amount().isGreaterThan(remaining)) {
                insert = insert.withAmount(remaining);
            }

            FluidInsertable tank = l.getTarget();
            if (tank != null) {
                 remaining = remaining.sub(insert.amount().sub(tank.attemptInsertion(insert, Simulation.ACTION).amount()));
            }
        }

        if (stack.pop() != this) {
            throw new IllegalStateException("Invalid Recursion detected.");
        }

        return remaining.isZero() ? FluidVolumeUtil.EMPTY : fluid.withAmount(remaining);
    }

    private Deque<FluidP2PTunnelPart> getDepth() {
        Deque<FluidP2PTunnelPart> s = DEPTH.get();

        if (s == null) {
            DEPTH.set(s = new ArrayDeque<>());
        }

        return s;
    }

    private List<FluidP2PTunnelPart> getOutputs(final FluidKey input) {
        final List<FluidP2PTunnelPart> outs = new ArrayList<>();

        try {
            for (final FluidP2PTunnelPart l : this.getOutputs()) {
                final FluidInsertable handler = l.getTarget();

                if (handler != null) {
                    outs.add(l);
                }
            }
        } catch (final GridAccessException e) {
            // :P
        }

        return outs;
    }

    private FluidInsertable getTarget() {
        if (!this.getProxy().isActive()) {
            return null;
        }

        if (this.cachedTank != null) {
            return this.cachedTank;
        }

        return this.cachedTank = FluidAttributes.INSERTABLE.getFirstOrNullFromNeighbour(this.getTile(), this.getSide().getFacing());
    }

}
