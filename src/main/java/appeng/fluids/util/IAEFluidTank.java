
package appeng.fluids.util;


import appeng.api.storage.data.IAEFluidStack;


public interface IAEFluidTank
{
	void setFluidInSlot( final int slot, final IAEFluidStack fluid );

	IAEFluidStack getFluidInSlot( final int slot );

	int getSlots();

}
