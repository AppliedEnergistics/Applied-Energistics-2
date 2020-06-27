
package appeng.fluids.util;

import java.util.Objects;

import javax.annotation.Nonnull;

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import net.minecraft.nbt.CompoundTag;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.util.Platform;

public class AEFluidInventory implements IAEFluidTank {
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
                if (fluid != null && fluid.getStackSize() != this.fluids[slot].getStackSize()) {
                    this.fluids[slot].setStackSize(Math.min(fluid.getStackSize(), this.capacity));
                    this.onContentChanged(slot);
                }
            } else {
                if (fluid == null) {
                    this.fluids[slot] = null;
                } else {
                    this.fluids[slot] = fluid.copy();
                    this.fluids[slot].setStackSize(Math.min(fluid.getStackSize(), this.capacity));
                }

                this.onContentChanged(slot);
            }
        }
    }

    private void onContentChanged(final int slot) {
        if (this.handler != null && Platform.isServer()) {
            this.handler.onFluidInventoryChanged(this, slot);
        }
    }

    @Override
    public IAEFluidStack getFluidInSlot(final int slot) {
        if (slot >= 0 && slot < this.getSlots()) {
            return this.fluids[slot];
        }
        return null;
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
    public FluidVolume getFluidInTank(int tank) {
        if (tank < 0 || tank >= fluids.length) {
            return FluidVolume.EMPTY;
        }
        return fluids[tank] == null ? FluidVolume.EMPTY : fluids[tank].getFluidStack();
    }

    @Override
    public int getTankCapacity(int tank) {
        return Math.min(this.capacity, Integer.MAX_VALUE);
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidVolume stack) {
        return stack != FluidVolume.EMPTY;
    }

    public int fill(final int slot, final FluidVolume resource, final boolean doFill) {
        if (resource.isEmpty() || resource.getAmount() <= 0) {
            return 0;
        }

        final IAEFluidStack fluid = this.fluids[slot];

        if (fluid != null && !fluid.getFluidStack().equals(resource)) {
            return 0;
        }

        int amountToStore = this.capacity;

        if (fluid != null) {
            amountToStore -= fluid.getStackSize();
        }

        amountToStore = Math.min(amountToStore, resource.getAmount());

        if (doFill) {
            if (fluid == null) {
                this.setFluidInSlot(slot, AEFluidStack.fromFluidStack(resource));
            } else {
                fluid.setStackSize(fluid.getStackSize() + amountToStore);
                this.onContentChanged(slot);
            }
        }

        return amountToStore;
    }

    @Override
    public int fill(FluidVolume resource, FluidAction action) {
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

        if (fluid != null) {
            amountToStore -= fluid.getStackSize();
        }

        amountToStore = Math.min(amountToStore, resource.getAmount());

        if (action == FluidAction.EXECUTE) {
            if (fluid == null) {
                this.setFluidInSlot(slot, AEFluidStack.fromFluidStack(resource));
            } else {
                fluid.setStackSize(fluid.getStackSize() + amountToStore);
                this.onContentChanged(slot);
            }
        }

        return amountToStore;
    }

    @Override
    public FluidVolume drain(final FluidVolume fluid, final FluidAction action) {
        if (fluid.isEmpty() || fluid.getAmount() <= 0) {
            return FluidVolume.EMPTY;
        }

        final FluidVolume resource = fluid.copy();

        FluidVolume totalDrained = FluidVolume.EMPTY;
        for (int slot = 0; slot < this.getSlots(); ++slot) {
            FluidVolume drain = this.drain(slot, resource, action == FluidAction.EXECUTE);
            if (drain != null) {
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
    public FluidVolume drain(final int maxDrain, final FluidAction action) {
        if (maxDrain == 0) {
            return FluidVolume.EMPTY;
        }

        FluidVolume totalDrained = FluidVolume.EMPTY;
        int toDrain = maxDrain;

        for (int slot = 0; slot < this.getSlots(); ++slot) {
            if (totalDrained.isEmpty()) {
                totalDrained = this.drain(slot, toDrain, action == FluidAction.EXECUTE);
                if (totalDrained.isEmpty()) {
                    toDrain -= totalDrained.getAmount();
                }
            } else {
                FluidVolume copy = totalDrained.copy();
                copy.setAmount(toDrain);
                FluidVolume drain = this.drain(slot, copy, action == FluidAction.EXECUTE);
                if (drain != null) {
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

    private int indexOfFluid(FluidVolume resource) {
        for (int slot = 0; slot < fluids.length; slot++) {
            if (fluids[slot] != null && fluids[slot].getFluidStack().isFluidEqual(resource)) {
                return slot;
            }
        }
        return -1;
    }

    private int indexOfEmptySlot() {
        for (int slot = 0; slot < fluids.length; slot++) {
            if (fluids[slot] == null) {
                return slot;
            }
        }
        return -1;
    }

    public FluidVolume drain(final int slot, final FluidVolume resource, final boolean doDrain) {
        final IAEFluidStack fluid = this.fluids[slot];
        if (resource.isEmpty() || fluid == null || !fluid.getFluidStack().equals(resource)) {
            return null;
        }
        return this.drain(slot, resource.getAmount(), doDrain);
    }

    public FluidVolume drain(final int slot, final int maxDrain, boolean doDrain) {
        final IAEFluidStack fluid = this.fluids[slot];
        if (fluid == null || maxDrain <= 0) {
            return null;
        }

        int drained = maxDrain;
        if (fluid.getStackSize() < drained) {
            drained = (int) fluid.getStackSize();
        }

        FluidVolume stack = new FluidVolume(fluid.getFluid(), drained);
        if (doDrain) {
            fluid.setStackSize(fluid.getStackSize() - drained);
            if (fluid.getStackSize() <= 0) {
                this.fluids[slot] = null;
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

                if (this.fluids[x] != null) {
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
