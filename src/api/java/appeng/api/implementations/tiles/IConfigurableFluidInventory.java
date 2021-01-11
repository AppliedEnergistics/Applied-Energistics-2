package appeng.api.implementations.tiles;

import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IConfigurableFluidInventory {

    default IFluidHandler getFluidInventoryByName(String name) {
        return null;
    }
}
