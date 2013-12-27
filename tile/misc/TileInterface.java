package appeng.tile.misc;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.implementations.IStorageMonitorable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.storage.MEMonitorPassthu;
import appeng.me.storage.NullInventory;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

public class TileInterface extends AENetworkInvTile implements IGridTickable, IStorageMonitorable
{

	final int sides[] = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };
	AppEngInternalInventory inv = new AppEngInternalInventory( this, 16 + 9 );

	MEMonitorPassthu<IAEItemStack> items = new MEMonitorPassthu<IAEItemStack>( new NullInventory() );
	MEMonitorPassthu<IAEFluidStack> fluids = new MEMonitorPassthu<IAEFluidStack>( new NullInventory() );

	@Override
	public void gridChanged()
	{
		try
		{
			items.setInternal( gridProxy.getStorage().getItemInventory() );
			fluids.setInternal( gridProxy.getStorage().getFluidInventory() );
		}
		catch (GridAccessException gae)
		{
			items.setInternal( new NullInventory() );
			fluids.setInternal( new NullInventory() );
		}

		worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord, 0 );
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
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( slot >= 16 )
		{
			// gridProxy.getCrafting()
		}
		else
			updateStorage();
	}

	public boolean hasWorkToDo()
	{
		// TODO Auto-generated method stub
		return false;
	}

	private boolean updateStorage()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasConfig()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sides;
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( 5, 120, !hasWorkToDo(), false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		boolean couldDoWork = updateStorage();
		return hasWorkToDo() ? (couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER) : TickRateModulation.SLEEP;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		if ( hasConfig() )
			return null;

		return items;
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		if ( hasConfig() )
			return null;

		return fluids;
	}

}
