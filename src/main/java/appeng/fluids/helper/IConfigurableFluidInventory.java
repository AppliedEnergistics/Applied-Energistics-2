package appeng.fluids.helper;

import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IConfigurableFluidInventory
{
    default IFluidHandler getFluidInventoryByName( String name )
    {
        return null;
    }
}
