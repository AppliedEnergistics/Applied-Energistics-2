package appeng.tile;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public abstract class AEBaseInvTile extends AEBaseTile implements ISidedInventory, IAEAppEngInventory
{

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_AEBaseInvTile(net.minecraft.nbt.NBTTagCompound data)
	{
		IInventory inv = getInternalInventory();
		NBTTagCompound opt = data.getCompoundTag( "inv" );
		for (int x = 0; x < inv.getSizeInventory(); x++)
		{
			NBTTagCompound item = opt.getCompoundTag( "item" + x );
			inv.setInventorySlotContents( x, ItemStack.loadItemStackFromNBT( item ) );
		}
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_AEBaseInvTile(net.minecraft.nbt.NBTTagCompound data)
	{
		IInventory inv = getInternalInventory();
		NBTTagCompound opt = new NBTTagCompound();
		for (int x = 0; x < inv.getSizeInventory(); x++)
		{
			NBTTagCompound item = new NBTTagCompound();
			ItemStack is = getStackInSlot( x );
			if ( is != null )
				is.writeToNBT( item );
			opt.setTag( "item" + x, item );
		}
		data.setTag( "inv", opt );
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
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer p)
	{
		return this.worldObj.getTileEntity( this.xCoord, this.yCoord, this.zCoord ) != this ? false : p.getDistanceSq( (double) this.xCoord + 0.5D,
				(double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D ) <= 32.0D;
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

	public abstract IInventory getInternalInventory();

	@Override
	public abstract void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added);

	public abstract int[] getAccessibleSlotsBySide(ForgeDirection whichSide);

	@Override
	final public int[] getAccessibleSlotsFromSide(int side)
	{
		Block blk = worldObj.getBlock( xCoord, yCoord, zCoord );
		if ( blk instanceof AEBaseBlock )
		{
			ForgeDirection mySide = ForgeDirection.getOrientation( side );
			return getAccessibleSlotsBySide( ((AEBaseBlock) blk).mapRotation( this, mySide ) );
		}
		return getAccessibleSlotsBySide( ForgeDirection.getOrientation( side ) );
	}

	/**
	 * Returns the name of the inventory
	 */
	@Override
	public String getInventoryName()
	{
		return getCustomName();
	}

	/**
	 * Returns if the inventory is named
	 */
	@Override
	public boolean hasCustomInventoryName()
	{
		return hasCustomName();
	}

}
