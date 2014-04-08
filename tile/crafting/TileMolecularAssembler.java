package appeng.tile.crafting;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class TileMolecularAssembler extends AENetworkInvTile implements IAEAppEngInventory, ISidedInventory
{

	final int[] sides = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	boolean hasPattern = false;
	AppEngInternalInventory inv = new AppEngInternalInventory( this, 9 + 2 );

	private class TileMolecularAssemblerHandler extends AETileEventHandler
	{

		public TileMolecularAssemblerHandler() {
			super( TileEventType.WORLD_NBT );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{

		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{

		}

	};

	public TileMolecularAssembler() {
		addNewHandler( new TileMolecularAssemblerHandler() );
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if ( i >= 9 )
			return false;

		if ( hasPattern )
		{

		}

		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i == 9;
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
	public int[] getAccessibleSlotsBySide(ForgeDirection whichSide)
	{
		return sides;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

}
