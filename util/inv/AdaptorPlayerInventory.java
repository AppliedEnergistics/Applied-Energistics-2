package appeng.util.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class AdaptorPlayerInventory implements IInventory
{

	private InventoryPlayer src;
	private int min;
	private int size;

	public AdaptorPlayerInventory(InventoryPlayer a) {
		src = a;
		min = 0; // a.getStartInventorySide( d );
		size = 36; // a.getSizeInventorySide( d );
	}

	@Override
	public int getSizeInventory()
	{
		return size;
	}

	@Override
	public ItemStack getStackInSlot(int var1)
	{
		return src.getStackInSlot( var1 + min );
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2)
	{
		return src.decrStackSize( min + var1, var2 );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return src.getStackInSlotOnClosing( min + var1 );
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2)
	{
		src.setInventorySlotContents( var1 + min, var2 );
	}

	@Override
	public String getInvName()
	{
		return src.getInvName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return src.getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged()
	{
		src.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1)
	{
		return src.isUseableByPlayer( var1 );
	}

	@Override
	public void openChest()
	{
		src.openChest();
	}

	@Override
	public void closeChest()
	{
		src.closeChest();
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

}
