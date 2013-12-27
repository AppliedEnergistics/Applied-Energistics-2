package appeng.tile.storage;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;

public class TileIOPort extends AENetworkInvTile
{

	final int sides[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
	AppEngInternalInventory inv = new AppEngInternalInventory( this, 12 );

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
		// TODO Auto-generated method stub
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return sides;
	}

}
