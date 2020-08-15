package appeng.fluids.util;

import alexiil.mc.lib.attributes.fluid.FixedFluidInv;

import appeng.api.storage.data.IAEFluidStack;

public interface IAEFluidTank extends FixedFluidInv {
    void setFluidInSlot(final int slot, final IAEFluidStack fluid);

    IAEFluidStack getFluidInSlot(final int slot);

    int getSlots();

}
