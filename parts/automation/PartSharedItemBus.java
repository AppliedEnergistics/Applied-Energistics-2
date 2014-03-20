package appeng.parts.automation;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

public abstract class PartSharedItemBus extends PartUpgradeable implements IGridTickable
{

	private TileEntity getTileEntity(TileEntity self, int x, int y, int z)
	{
		World w = self.getWorldObj();

		if ( w.getChunkProvider().chunkExists( x >> 4, z >> 4 ) )
		{
			TileEntity te = w.getTileEntity( x, y, z );
			return te;
		}

		return null;
	}

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

	int adaptorHash = 0;
	InventoryAdaptor adaptor;

	InventoryAdaptor getHandler()
	{
		TileEntity self = getHost().getTile();
		TileEntity target = getTileEntity( self, self.xCoord + side.offsetX, self.yCoord + side.offsetY, self.zCoord + side.offsetZ );

		int newAdaptorHash = Platform.generateTileHash( target );

		if ( adaptorHash == newAdaptorHash && newAdaptorHash != 0 )
			return adaptor;

		adaptorHash = newAdaptorHash;
		adaptor = InventoryAdaptor.getAdaptor( target, side.getOpposite() );

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

}
