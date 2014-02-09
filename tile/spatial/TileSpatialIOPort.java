package appeng.tile.spatial;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

public class TileSpatialIOPort extends AENetworkInvTile
{

	final int sides[] = { 0, 1 };
	AppEngInternalInventory inv = new AppEngInternalInventory( this, 2 );
	boolean lastRedstoneState = false;

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
		// TODO Auto-generated method stub

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

	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sides;
	}

}
