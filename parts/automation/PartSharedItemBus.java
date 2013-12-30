package appeng.parts.automation;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickingRequest;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

public abstract class PartSharedItemBus extends PartUpgradeable implements IGridTickable
{

	public PartSharedItemBus(Class c, ItemStack is) {
		super( c, is );
	}

	public void writeToNBT(net.minecraft.nbt.NBTTagCompound extra)
	{
		super.writeToNBT( extra );
		config.writeToNBT( extra, "config" );
	}

	public void readFromNBT(net.minecraft.nbt.NBTTagCompound extra)
	{
		super.readFromNBT( extra );
		config.readFromNBT( extra, "config" );
	}

	AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 9 );

	boolean cached = false;
	int adaptorHash = 0;
	InventoryAdaptor adaptor;

	InventoryAdaptor getHandler()
	{
		if ( cached )
			return adaptor;

		cached = true;
		TileEntity self = getHost().getTile();
		TileEntity target = self.worldObj.getBlockTileEntity( self.xCoord + side.offsetX, self.yCoord + side.offsetY, self.zCoord + side.offsetZ );

		int newAdaptorHash = Platform.generateTileHash( target );

		if ( adaptorHash == newAdaptorHash )
			return adaptor;

		adaptorHash = newAdaptorHash;
		adaptor = InventoryAdaptor.getAdaptor( target, side.getOpposite() );
		cached = true;

		return adaptor;

	}

	@Override
	public void onNeighborChanged()
	{
		cached = false;
		if ( !isSleeping() )
		{
			try
			{
				proxy.getTick().wakeDevice( proxy.getNode() );
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "config" ) )
			return config;

		return super.getInventoryByName( name );
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( 5, 60, isSleeping(), false );
	}

	abstract protected boolean isSleeping();

}
