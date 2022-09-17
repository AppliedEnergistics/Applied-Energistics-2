package appeng.fluids.util;


import appeng.api.storage.data.IAEFluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;


public interface IAEFluidTank extends IFluidHandler {
    void setFluidInSlot(final int slot, final IAEFluidStack fluid);

    IAEFluidStack getFluidInSlot(final int slot);

    int getSlots();

}
