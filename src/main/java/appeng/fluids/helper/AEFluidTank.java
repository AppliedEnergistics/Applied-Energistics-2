
package appeng.fluids.helper;


import net.minecraftforge.fluids.FluidTank;


public class AEFluidTank extends FluidTank
{
	private final IAEFluidInventory host;

	public AEFluidTank( IAEFluidInventory host, int capacity )
	{
		super( capacity );
		this.host = host;
	}

	@Override
	protected void onContentsChanged()
	{
		if( host != null )
		{
			host.onFluidInventoryChanged( this );
		}
		super.onContentsChanged();
	}

}
