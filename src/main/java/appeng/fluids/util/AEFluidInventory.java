package appeng.fluids.util;


import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.util.Platform;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.Objects;


public class AEFluidInventory implements IAEFluidTank {
    private final IAEFluidStack[] fluids;
    private final IAEFluidInventory handler;
    private int capacity;
    private IFluidTankProperties[] props = null;

    public AEFluidInventory(final IAEFluidInventory handler, final int slots, final int capcity) {
        this.fluids = new IAEFluidStack[slots];
        this.handler = handler;
        this.capacity = capcity;
    }

    public void setCapacity(int capacity) {
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
                    this.fluids[slot].setStackSize(fluid.getStackSize());
                    this.onContentChanged(slot);
                }
            } else {
                if (fluid == null) {
                    this.fluids[slot] = null;
                } else {
                    this.fluids[slot] = fluid.copy();
                    this.fluids[slot].setStackSize(fluid.getStackSize());
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
    public IFluidTankProperties[] getTankProperties() {
        if (this.props == null) {
            this.props = new IFluidTankProperties[this.getSlots()];
            for (int i = 0; i < this.getSlots(); ++i) {
                this.props[i] = new FluidTankPropertiesWrapper(i);
            }

        }
        return this.props;
    }

    public int fill(final int slot, final FluidStack resource, final boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }

        final IAEFluidStack fluid = this.fluids[slot];

        if (fluid != null && !fluid.equals(resource)) {
            return 0;
        }

        int amountToStore = this.capacity;

        if (fluid != null) {
            amountToStore -= fluid.getStackSize();
        }

        amountToStore = Math.min(amountToStore, resource.amount);

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

    public FluidStack drain(final int slot, final FluidStack resource, final boolean doDrain) {
        final IAEFluidStack fluid = this.fluids[slot];
        if (resource == null || fluid == null || !fluid.equals(resource)) {
            return null;
        }
        return this.drain(slot, resource.amount, doDrain);
    }

    public FluidStack drain(final int slot, final int maxDrain, boolean doDrain) {
        final IAEFluidStack fluid = this.fluids[slot];
        if (fluid == null || maxDrain <= 0) {
            return null;
        }

        int drained = maxDrain;
        if (fluid.getStackSize() < drained) {
            drained = (int) fluid.getStackSize();
        }

        FluidStack stack = new FluidStack(fluid.getFluid(), drained);
        if (doDrain) {
            fluid.setStackSize(fluid.getStackSize() - drained);
            if (fluid.getStackSize() <= 0) {
                this.fluids[slot] = null;
            }
            this.onContentChanged(slot);
        }
        return stack;
    }

    @Override
    public int fill(final FluidStack fluid, final boolean doFill) {
        if (fluid == null || fluid.amount <= 0) {
            return 0;
        }

        final FluidStack insert = fluid.copy();

        int totalFillAmount = 0;
        for (int slot = 0; slot < this.getSlots(); ++slot) {
            int fillAmount = this.fill(slot, insert, doFill);
            totalFillAmount += fillAmount;
            insert.amount -= fillAmount;
            if (insert.amount <= 0) {
                break;
            }
        }
        return totalFillAmount;
    }

    @Override
    public FluidStack drain(final FluidStack fluid, final boolean doDrain) {
        if (fluid == null || fluid.amount <= 0) {
            return null;
        }

        final FluidStack resource = fluid.copy();

        FluidStack totalDrained = null;
        for (int slot = 0; slot < this.getSlots(); ++slot) {
            FluidStack drain = this.drain(slot, resource, doDrain);
            if (drain != null) {
                if (totalDrained == null) {
                    totalDrained = drain;
                } else {
                    totalDrained.amount += drain.amount;
                }

                resource.amount -= drain.amount;
                if (resource.amount <= 0) {
                    break;
                }
            }
        }
        return totalDrained;
    }

    @Override
    public FluidStack drain(final int maxDrain, final boolean doDrain) {
        if (maxDrain == 0) {
            return null;
        }

        FluidStack totalDrained = null;
        int toDrain = maxDrain;

        for (int slot = 0; slot < this.getSlots(); ++slot) {
            if (totalDrained == null) {
                totalDrained = this.drain(slot, toDrain, doDrain);
                if (totalDrained != null) {
                    toDrain -= totalDrained.amount;
                }
            } else {
                FluidStack copy = totalDrained.copy();
                copy.amount = toDrain;
                FluidStack drain = this.drain(slot, copy, doDrain);
                if (drain != null) {
                    totalDrained.amount += drain.amount;
                    toDrain -= drain.amount;
                }
            }

            if (toDrain <= 0) {
                break;
            }
        }
        return totalDrained;
    }

    public void writeToNBT(final NBTTagCompound data, final String name) {
        final NBTTagCompound c = new NBTTagCompound();
        this.writeToNBT(c);
        data.setTag(name, c);
    }

    private void writeToNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.fluids.length; x++) {
            try {
                final NBTTagCompound c = new NBTTagCompound();

                if (this.fluids[x] != null) {
                    this.fluids[x].writeToNBT(c);
                }

                target.setTag("#" + x, c);
            } catch (final Exception ignored) {
            }
        }
    }

    public void readFromNBT(final NBTTagCompound data, final String name) {
        final NBTTagCompound c = data.getCompoundTag(name);
        if (c != null) {
            this.readFromNBT(c);
        }
    }

    private void readFromNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.fluids.length; x++) {
            try {
                final NBTTagCompound c = target.getCompoundTag("#" + x);

                if (c != null) {
                    this.fluids[x] = AEFluidStack.fromNBT(c);
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    private class FluidTankPropertiesWrapper implements IFluidTankProperties {
        private final int slot;

        public FluidTankPropertiesWrapper(final int slot) {
            this.slot = slot;
        }

        @Override
        public FluidStack getContents() {
            return AEFluidInventory.this.fluids[this.slot] == null ? null : AEFluidInventory.this.fluids[this.slot].getFluidStack();
        }

        @Override
        public int getCapacity() {
            return Math.min(AEFluidInventory.this.capacity, Integer.MAX_VALUE);
        }

        @Override
        public boolean canFill() {
            return true;
        }

        @Override
        public boolean canDrain() {
            return true;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            return true;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return fluidStack != null;
        }
    }
}
