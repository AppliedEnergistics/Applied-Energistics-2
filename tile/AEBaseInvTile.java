package appeng.tile;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public abstract class AEBaseInvTile extends AEBaseTile implements ISidedInventory, IAEAppEngInventory
{

	public AEBaseInvTile() {
		addNewHandler( new AETileEventHandler( EnumSet.of( TileEventType.WORLD_NBT ) ) {

			@Override
			public void readFromNBT(net.minecraft.nbt.NBTTagCompound data)
			{
				IInventory inv = getInternalInventory();
				NBTTagCompound opt = data.getCompoundTag( "inv" );
				for (int x = 0; x < inv.getSizeInventory(); x++)
				{
					NBTTagCompound item = opt.getCompoundTag( "item" + x );
					inv.setInventorySlotContents( x, ItemStack.loadItemStackFromNBT( item ) );
				}
			}

			@Override
			public void writeToNBT(net.minecraft.nbt.NBTTagCompound data)
			{
				IInventory inv = getInternalInventory();
				NBTTagCompound opt = new NBTTagCompound();
				for (int x = 0; x < inv.getSizeInventory(); x++)
				{
					NBTTagCompound item = new NBTTagCompound();
					ItemStack is = getStackInSlot( x );
					if ( is != null )
						is.writeToNBT( item );
					opt.setCompoundTag( "item" + x, item );
				}
				data.setCompoundTag( "inv", opt );
			}

		} );
	}

	@Override
	public int getSizeInventory()
	{
		return getInternalInventory().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return getInternalInventory().getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return getInternalInventory().decrStackSize( i, j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		getInternalInventory().setInventorySlotContents( i, itemstack );
	}

	@Override
	public String getInvName()
	{
		return getClass().getSimpleName();
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer p)
	{
		return this.worldObj.getBlockTileEntity( this.xCoord, this.yCoord, this.zCoord ) != this ? false : p.getDistanceSq( (double) this.xCoord + 0.5D,
				(double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D ) <= 32.0D;
	}

	@Override
	public void openChest()
	{
	}

	@Override
	public void closeChest()
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return isItemValidForSlot( i, itemstack );
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return true;
	}

	@Override
	public abstract IInventory getInternalInventory();

	@Override
	public abstract void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added);

	@Override
	public abstract int[] getAccessibleSlotsFromSide(int side);

}
