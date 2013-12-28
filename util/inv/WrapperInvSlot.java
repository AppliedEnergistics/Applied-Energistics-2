package appeng.util.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class WrapperInvSlot implements IInventory
{

	private final IInventory inv;
	private int slot = 0;

	public WrapperInvSlot(IInventory inv) {
		this.inv = inv;
	}

	public void setSlot(int slot)
	{
		this.slot = slot;
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return inv.getStackInSlot( slot );
	}

	@Override
	public ItemStack decrStackSize(int i, int num)
	{
		return inv.decrStackSize( slot, num );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return inv.getStackInSlotOnClosing( slot );
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		inv.setInventorySlotContents( slot, itemstack );
	}

	@Override
	public String getInvName()
	{
		return inv.getInvName();
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return inv.isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return inv.getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged()
	{
		inv.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return inv.isUseableByPlayer( entityplayer );
	}

	@Override
	public void openChest()
	{
		inv.openChest();
	}

	@Override
	public void closeChest()
	{
		inv.closeChest();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return inv.isItemValidForSlot( slot, itemstack );
	}

}
