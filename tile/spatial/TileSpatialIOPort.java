package appeng.tile.spatial;

import java.util.concurrent.Callable;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.TransitionResult;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.hooks.TickHandler;
import appeng.items.storage.ItemSpatialStorageCell;
import appeng.me.cache.SpatialPylonCache;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class TileSpatialIOPort extends AENetworkInvTile implements Callable
{

	final int sides[] = { 0, 1 };
	AppEngInternalInventory inv = new AppEngInternalInventory( this, 2 );
	boolean lastRedstoneState = false;

	public TileSpatialIOPort() {
		gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
	}

	public void updateRedstoneState()
	{
		boolean currentState = worldObj.isBlockIndirectlyGettingPowered( xCoord, yCoord, zCoord );
		if ( lastRedstoneState != currentState )
		{
			lastRedstoneState = currentState;
			if ( currentState )
			{
				triggerTransition();
			}
		}
	}

	private void triggerTransition()
	{
		if ( Platform.isServer() )
		{
			ItemStack cell = getStackInSlot( 0 );
			if ( isSpatialCell( cell ) )
			{
				TickHandler.instance.addCallable( null, this );// this needs to be cross world sycned.
			}
		}
	}

	@Override
	public Object call() throws Exception
	{

		ItemStack cell = getStackInSlot( 0 );
		if ( isSpatialCell( cell ) && getStackInSlot( 1 ) == null )
		{
			IGrid gi = gridProxy.getGrid();
			IEnergyGrid energy = gridProxy.getEnergy();

			ItemSpatialStorageCell sc = (ItemSpatialStorageCell) cell.getItem();

			SpatialPylonCache spc = (SpatialPylonCache) gi.getCache( ISpatialCache.class );
			if ( spc.hasRegion() && spc.isValidRegion() )
			{
				double req = spc.requiredPower();
				double pr = energy.extractAEPower( req, Actionable.SIMULATE, PowerMultiplier.CONFIG );
				if ( Math.abs( pr - req ) < req * 0.001 )
				{
					TransitionResult tr = sc.doSpatialTransition( cell, worldObj, spc.getMin(), spc.getMax(), true );
					if ( tr.success )
					{
						energy.extractAEPower( req, Actionable.MODULATE, PowerMultiplier.CONFIG );
						setInventorySlotContents( 0, null );
						setInventorySlotContents( 1, cell );
					}
				}
			}
		}

		return null;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.SMART;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return inv;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return (i == 0 ? isSpatialCell( itemstack ) : false);
	}

	private boolean isSpatialCell(ItemStack cell)
	{
		if ( cell != null && cell.getItem() instanceof ISpatialStorageCell )
		{
			ISpatialStorageCell sc = (ISpatialStorageCell) cell.getItem();
			return sc != null && sc.isSpatialStorage( cell );
		}
		return false;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return isItemValidForSlot( i, itemstack );
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i == 1;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{

	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return sides;
	}

}
