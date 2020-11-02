package appeng.fluids.util;

import java.math.RoundingMode;
import java.util.Objects;

import net.minecraft.nbt.CompoundTag;

import alexiil.mc.lib.attributes.ListenerRemovalToken;
import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidInvTankChangeListener;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.util.Platform;

public class AEFluidInventory implements IAEFluidTank {

    /**
     * While this may seem redundant, it helps since this class heavily mixes AE
     * fluids stacks, which use null to represent "nothing", and Minecraft's
     * FluidStack, which uses #isEmpty() to represent nothing.
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
    public boolean setInvFluid(int tank, FluidVolume to, Simulation simulation) {
        if (tank >= 0 && tank < this.getSlots()) {
            if (simulation == Simulation.ACTION) {
                setFluidInSlot(tank, AEFluidStack.fromFluidVolume(to, RoundingMode.DOWN));
            }
            return true;
        }
        return false;
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
        if (this.handler != null && Platform.isServer()) {
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
    public int getTankCount() {
        return this.fluids.length;
    }

    @Override
    public FluidAmount getMaxAmount_F(int tank) {
        return FluidAmount.of(Math.min(this.capacity, Integer.MAX_VALUE), 1000);
    }

    @Override
    public FluidVolume getInvFluid(int tank) {
        if (tank < 0 || tank >= fluids.length) {
            return FluidVolumeUtil.EMPTY;
        }
        return fluids[tank] == EMPTY_AE_FLUIDSTACK ? FluidVolumeUtil.EMPTY : fluids[tank].getFluidStack();
    }

    @Override
    public boolean isFluidValidForTank(int tank, FluidKey fluid) {
        return !fluid.isEmpty();
    }

    @Override
    public ListenerToken addListener(FluidInvTankChangeListener listener, ListenerRemovalToken removalToken) {
        return null;
    }

    public int fill(final int slot, final FluidVolume resource, final boolean doFill) {
        if (resource.isEmpty() || resource.getAmount() <= 0) {
            return 0;
        }

        final IAEFluidStack fluid = this.fluids[slot];

        if (fluid != EMPTY_AE_FLUIDSTACK && !FluidVolume.areEqualExceptAmounts(fluid.getFluidStack(), resource)) {
            return 0;
        }

        int amountToStore = this.capacity;

        if (fluid != EMPTY_AE_FLUIDSTACK) {
            amountToStore -= fluid.getStackSize();
        }

        amountToStore = Math.min(amountToStore, (int) resource.getAmount_F().asLong(1000, RoundingMode.DOWN));

        if (doFill) {
            if (fluid == null) {
                this.setFluidInSlot(slot, AEFluidStack.fromFluidVolume(resource, RoundingMode.DOWN));
            } else {
                fluid.setStackSize(fluid.getStackSize() + amountToStore);
                this.onContentChanged(slot);
            }
        }

        return amountToStore;
    }

    public FluidAmount drain(final int slot, final FluidVolume resource, final boolean doDrain) {
        final IAEFluidStack fluid = this.fluids[slot];
        if (resource.isEmpty() || fluid == EMPTY_AE_FLUIDSTACK
                || !FluidVolume.areEqualExceptAmounts(fluid.getFluidStack(), resource)) {
            return FluidAmount.ZERO;
        }
        int toDrain = (int) resource.getAmount_F().asLong(1000, RoundingMode.DOWN);
        return this.drain(slot, toDrain, doDrain);
    }

    public FluidAmount drain(final int slot, final int maxDrain, boolean doDrain) {
        final IAEFluidStack fluid = this.fluids[slot];
        if (fluid == EMPTY_AE_FLUIDSTACK || maxDrain <= 0) {
            return FluidAmount.ZERO;
        }

        int drained = maxDrain;
        if (fluid.getStackSize() < drained) {
            drained = (int) fluid.getStackSize();
        }

        FluidAmount amount = FluidAmount.of(drained, 1000);
        if (doDrain) {
            fluid.setStackSize(fluid.getStackSize() - drained);
            if (fluid.getStackSize() <= 0) {
                this.fluids[slot] = EMPTY_AE_FLUIDSTACK;
            }
            this.onContentChanged(slot);
        }
        return amount;
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
