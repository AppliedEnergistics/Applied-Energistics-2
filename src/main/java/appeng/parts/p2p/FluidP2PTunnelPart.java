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

import alexiil.mc.lib.attributes.Attribute;
import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.impl.EmptyFluidExtractable;
import alexiil.mc.lib.attributes.fluid.impl.RejectingFluidInsertable;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;

public class FluidP2PTunnelPart extends P2PTunnelPart<FluidP2PTunnelPart> {

    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_fluids");

    private final FluidInsertable inputHandler = new InputFluidHandler();
    private final FluidExtractable outputHandler = new OutputFluidHandler();

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
    public void onNeighborUpdate(BlockView w, BlockPos pos, BlockPos neighbor) {
        if (this.isOutput()) {
            final FluidP2PTunnelPart in = this.getInput();
            if (in != null) {
                in.onTunnelNetworkChange();
            }
        }
    }

    @Override
    public void addAllAttributes(AttributeList<?> to) {
        if (this.isOutput()) {
            to.offer(this.outputHandler);
        } else {
            to.offer(this.inputHandler);
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    // Gets the given attribute (or the defaultValue) of the block that this part attaches to
    private <T> T getAttachedAttribute(Attribute<T> attribute, T defaultValue) {
        T result = null;
        if (this.isActive()) {
            final BlockEntity self = this.getTile();
            Direction direction = this.getSide().getFacing();
            BlockPos targetPos = self.getPos().offset(direction);
            World world = self.getWorld();
            result = attribute.getFirstOrNull(world, targetPos, SearchOptions.inDirection(direction));
        }
        return result != null ? result : defaultValue;
    }

    private FluidInsertable getAttachedOutput() {
        return getAttachedAttribute(FluidAttributes.INSERTABLE, RejectingFluidInsertable.NULL);
    }

    private FluidExtractable getAttachedInput() {
        return getAttachedAttribute(FluidAttributes.EXTRACTABLE, EmptyFluidExtractable.NULL);
    }

    private class InputFluidHandler implements FluidInsertable {

        @Override
        public FluidVolume attemptInsertion(FluidVolume fluidVolume, Simulation simulation) {
            FluidAmount overflowAmount = FluidAmount.ZERO;

            try {
                final int outputTunnels = FluidP2PTunnelPart.this.getOutputs().size();
                final FluidAmount amount = fluidVolume.amount();

                if (outputTunnels == 0 || amount.isZero()) {
                    return fluidVolume;
                }

                final FluidAmount amountPerOutput = amount.div(outputTunnels);

                for (FluidP2PTunnelPart target : FluidP2PTunnelPart.this.getOutputs()) {
                    final FluidInsertable output = target.getAttachedOutput();
                    final FluidVolume fillWithFluidStack = fluidVolume.withAmount(amountPerOutput.add(overflowAmount));

                    overflowAmount = output.attemptInsertion(fillWithFluidStack, simulation).amount();
                }

                if (simulation.isAction()) {
                    FluidP2PTunnelPart.this.queueTunnelDrain(PowerUnits.RF, amount.sub(overflowAmount).asInexactDouble() * 1000);
                }
            } catch (GridAccessException ignored) {
            }

            return fluidVolume.withAmount(overflowAmount);
        }

    }

    private class OutputFluidHandler implements FluidExtractable {

        @Override
        public FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxAmount, Simulation simulation) {
            return getAttachedInput().attemptExtraction(filter, maxAmount, simulation);
        }

        @Override
        public FluidVolume attemptAnyExtraction(FluidAmount maxAmount, Simulation simulation) {
            return getAttachedInput().attemptAnyExtraction(maxAmount, simulation);
        }

        @Override
        public FluidVolume extract(FluidFilter filter, FluidAmount maxAmount) {
            return getAttachedInput().extract(filter, maxAmount);
        }

        @Override
        public FluidVolume extract(FluidKey filter, FluidAmount maxAmount) {
            return getAttachedInput().extract(filter, maxAmount);
        }

        @Override
        public FluidVolume extract(FluidAmount maxAmount) {
            return getAttachedInput().extract(maxAmount);
        }

        @Override
        public boolean couldExtractAnything() {
            return getAttachedInput().couldExtractAnything();
        }

        @Override
        public FluidExtractable filtered(FluidFilter filter) {
            return getAttachedInput().filtered(filter);
        }

        @Override
        public FluidExtractable getPureExtractable() {
            return getAttachedInput().getPureExtractable();
        }

    }

}
