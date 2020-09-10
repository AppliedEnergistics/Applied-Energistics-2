
package appeng.container.interfaces;

import java.util.Map;

import appeng.api.storage.data.IAEFluidStack;

public interface IFluidSyncContainer {
    void receiveFluidSlots(final Map<Integer, IAEFluidStack> fluids);
}
