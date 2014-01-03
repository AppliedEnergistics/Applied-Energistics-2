package appeng.parts.automation;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
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

	@Override
	public void upgradesChanged()
	{
		updateState();
	}

	protected int availableSlots()
	{
		return Math.min( 1 + getInstalledUpgrades( Upgrades.CAPACITY ) * 4, config.getSizeInventory() );
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

		if ( adaptorHash == newAdaptorHash && newAdaptorHash != 0 )
			return adaptor;

		adaptorHash = newAdaptorHash;
		adaptor = InventoryAdaptor.getAdaptor( target, side.getOpposite() );
		cached = true;

		return adaptor;
	}

	boolean lastRedstone = false;

	abstract TickRateModulation doBusWork();

	@Override
	public void onNeighborChanged()
	{
		updateState();
		if ( lastRedstone != host.hasRedstone( side ) )
		{
			lastRedstone = !lastRedstone;
			if ( lastRedstone && getRSMode() == RedstoneMode.SIGNAL_PULSE )
				doBusWork();
		}
	}

	private void updateState()
	{
		cached = false;
		try
		{
			if ( !isSleeping() )
				proxy.getTick().wakeDevice( proxy.getNode() );
			else
				proxy.getTick().sleepDevice( proxy.getNode() );
		}
		catch (GridAccessException e)
		{
			// :P
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

}
