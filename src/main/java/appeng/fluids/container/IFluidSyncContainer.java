package appeng.fluids.container;

import java.util.Map;

import appeng.api.storage.data.IAEFluidStack;

public interface IFluidSyncContainer {
    void receiveFluidSlots(final Map<Integer, IAEFluidStack> fluids);
}
