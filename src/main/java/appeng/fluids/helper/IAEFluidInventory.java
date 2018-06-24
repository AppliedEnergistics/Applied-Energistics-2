
package appeng.fluids.helper;


import net.minecraftforge.fluids.capability.IFluidHandler;


@FunctionalInterface
public interface IAEFluidInventory
{
	void onFluidInventoryChanged( IFluidHandler inventory );
}
