/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.fluids.util;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;

public class AEFluidInventory implements IAEFluidTank {

    /**
     * While this may seem redundant, it helps since this class heavily mixes AE fluids stacks, which use null to
     * represent "nothing", and Minecraft's FluidStack, which uses #isEmpty() to represent nothing.
     */
    private static final IAEFluidStack EMPTY_AE_FLUIDSTACK = null;

    private final IAEFluidStack[] fluids;
    private final IAEFluidInventory handler;
    private final int capacity;

    public AEFluidInventory(final IAEFluidInventory handler, final int slots, final int capacity) {
        this.fluids = new IAEFluidStack[slots];
        this.handler = handler;
        this.capacity = capacity;
    }

    public AEFluidInventory(final IAEFluidInventory handler, final int slots) {
        this(handler, slots, Integer.MAX_VALUE);
    }

    @Override
    public void setFluidInSlot(final int slot, final IAEFluidStack fluid) {
        if (slot >= 0 && slot < this.getSlots()) {
            if (Objects.equals(this.fluids[slot], fluid)) {
                if (fluid != EMPTY_AE_FLUIDSTACK && fluid.getStackSize() != this.fluids[slot].getStackSize()) {
                    this.fluids[slot].setStackSize(Math.min(fluid.getStackSize(), this.capacity));
                    this.onContentChanged(slot);
                }
            } else {
                if (fluid == EMPTY_AE_FLUIDSTACK) {
                    this.fluids[slot] = EMPTY_AE_FLUIDSTACK;
                } else {
                    this.fluids[slot] = fluid.copy();
                    this.fluids[slot].setStackSize(Math.min(fluid.getStackSize(), this.capacity));
                }

                this.onContentChanged(slot);
            }
        }
    }

    private void onContentChanged(final int slot) {
        if (this.handler != null && !this.handler.isRemote()) {
            this.handler.onFluidInventoryChanged(this, slot);
        }
    }

    @Override
    public IAEFluidStack getFluidInSlot(final int slot) {
        if (slot >= 0 && slot < this.getSlots()) {
            return this.fluids[slot];
        }
        return EMPTY_AE_FLUIDSTACK;
    }

    @Override
    public int getSlots() {
        return this.fluids.length;
    }

    @Override
    public int getTanks() {
        return this.fluids.length;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank < 0 || tank >= fluids.length) {
            return FluidStack.EMPTY;
        }
        return fluids[tank] == EMPTY_AE_FLUIDSTACK ? FluidStack.EMPTY : fluids[tank].getFluidStack();
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.capacity;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return stack != FluidStack.EMPTY;
    }

    public int fill(final int slot, final FluidStack resource, final boolean doFill) {
        if (resource.isEmpty() || resource.getAmount() <= 0) {
            return 0;
        }

        final IAEFluidStack fluid = this.fluids[slot];

        if (fluid != EMPTY_AE_FLUIDSTACK && !fluid.getFluidStack().equals(resource)) {
            return 0;
        }

        int amountToStore = this.capacity;

        if (fluid != EMPTY_AE_FLUIDSTACK) {
            amountToStore -= fluid.getStackSize();
        }

        amountToStore = Math.min(amountToStore, resource.getAmount());

        if (doFill) {
            if (fluid == EMPTY_AE_FLUIDSTACK) {
                this.setFluidInSlot(slot, AEFluidStack.fromFluidStack(resource));
            } else {
                fluid.setStackSize(fluid.getStackSize() + amountToStore);
                this.onContentChanged(slot);
            }
        }

        return amountToStore;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || resource.getAmount() <= 0) {
            return 0;
        }

        // Find a suitable slot
        int slot = indexOfFluid(resource);
        if (slot == -1) {
            slot = indexOfEmptySlot();
            if (slot == -1) {
                return 0;
            }
        }

        final IAEFluidStack fluid = this.fluids[slot];

        int amountToStore = this.capacity;

        if (fluid != EMPTY_AE_FLUIDSTACK) {
            amountToStore -= fluid.getStackSize();
        }

        amountToStore = Math.min(amountToStore, resource.getAmount());

        if (action == FluidAction.EXECUTE) {
            if (fluid == EMPTY_AE_FLUIDSTACK) {
                this.setFluidInSlot(slot, AEFluidStack.fromFluidStack(resource));
            } else {
                fluid.setStackSize(fluid.getStackSize() + amountToStore);
                this.onContentChanged(slot);
            }
        }

        return amountToStore;
    }

    @Override
    public FluidStack drain(final FluidStack fluid, final FluidAction action) {
        if (fluid.isEmpty() || fluid.getAmount() <= 0) {
            return FluidStack.EMPTY;
        }

        final FluidStack resource = fluid.copy();

        FluidStack totalDrained = FluidStack.EMPTY;
        for (int slot = 0; slot < this.getSlots(); ++slot) {
            FluidStack drain = this.drain(slot, resource, action == FluidAction.EXECUTE);
            if (!drain.isEmpty()) {
                if (totalDrained.isEmpty()) {
                    totalDrained = drain;
                } else {
                    totalDrained.setAmount(totalDrained.getAmount() + drain.getAmount());
                }

                resource.setAmount(resource.getAmount() - drain.getAmount());
                if (resource.getAmount() <= 0) {
                    break;
                }
            }
        }
        return totalDrained;
    }

    @Override
    public FluidStack drain(final int maxDrain, final FluidAction action) {
        if (maxDrain == 0) {
            return FluidStack.EMPTY;
        }

        FluidStack totalDrained = FluidStack.EMPTY;
        int toDrain = maxDrain;

        for (int slot = 0; slot < this.getSlots(); ++slot) {
            if (totalDrained.isEmpty()) {
                totalDrained = this.drain(slot, toDrain, action == FluidAction.EXECUTE);
                if (totalDrained.isEmpty()) {
                    toDrain -= totalDrained.getAmount();
                }
            } else {
                FluidStack copy = totalDrained.copy();
                copy.setAmount(toDrain);
                FluidStack drain = this.drain(slot, copy, action == FluidAction.EXECUTE);
                if (!drain.isEmpty()) {
                    totalDrained.setAmount(totalDrained.getAmount() + drain.getAmount());
                    toDrain -= drain.getAmount();
                }
            }

            if (toDrain <= 0) {
                break;
            }
        }
        return totalDrained;
    }

    private int indexOfFluid(FluidStack resource) {
        for (int slot = 0; slot < fluids.length; slot++) {
            if (fluids[slot] != EMPTY_AE_FLUIDSTACK && fluids[slot].getFluidStack().isFluidEqual(resource)) {
                return slot;
            }
        }
        return -1;
    }

    private int indexOfEmptySlot() {
        for (int slot = 0; slot < fluids.length; slot++) {
            if (fluids[slot] == EMPTY_AE_FLUIDSTACK) {
                return slot;
            }
        }
        return -1;
    }

    public FluidStack drain(final int slot, final FluidStack resource, final boolean doDrain) {
        final IAEFluidStack fluid = this.fluids[slot];
        if (resource.isEmpty() || fluid == EMPTY_AE_FLUIDSTACK || !fluid.getFluidStack().equals(resource)) {
            return FluidStack.EMPTY;
        }
        return this.drain(slot, resource.getAmount(), doDrain);
    }

    public FluidStack drain(final int slot, final int maxDrain, boolean doDrain) {
        final IAEFluidStack fluid = this.fluids[slot];
        if (fluid == EMPTY_AE_FLUIDSTACK || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        int drained = maxDrain;
        if (fluid.getStackSize() < drained) {
            drained = (int) fluid.getStackSize();
        }

        FluidStack stack = new FluidStack(fluid.getFluid(), drained);
        if (doDrain) {
            fluid.setStackSize(fluid.getStackSize() - drained);
            if (fluid.getStackSize() <= 0) {
                this.fluids[slot] = EMPTY_AE_FLUIDSTACK;
            }
            this.onContentChanged(slot);
        }
        return stack;
    }

    public void writeToNBT(final CompoundTag data, final String name) {
        final CompoundTag c = new CompoundTag();
        this.writeToNBT(c);
        data.put(name, c);
    }

    private void writeToNBT(final CompoundTag target) {
        for (int x = 0; x < this.fluids.length; x++) {
            try {
                final CompoundTag c = new CompoundTag();

                if (this.fluids[x] != EMPTY_AE_FLUIDSTACK) {
                    this.fluids[x].writeToNBT(c);
                }

                target.put("#" + x, c);
            } catch (final Exception ignored) {
            }
        }
    }

    public void readFromNBT(final CompoundTag data, final String name) {
        final CompoundTag c = data.getCompound(name);
        if (!c.isEmpty()) {
            this.readFromNBT(c);
        }
    }

    private void readFromNBT(final CompoundTag target) {
        for (int x = 0; x < this.fluids.length; x++) {
            try {
                final CompoundTag c = target.getCompound("#" + x);

                if (!c.isEmpty()) {
                    this.fluids[x] = AEFluidStack.fromNBT(c);
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

}
