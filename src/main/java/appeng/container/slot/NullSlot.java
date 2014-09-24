package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class NullSlot extends Slot
{

	public NullSlot() {
		super( null, 0, 0, 0 );
	}

	@Override
	public void onSlotChange(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{

	}

	@Override
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{

	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

	@Override
	public ItemStack getStack()
	{
		return null;
	}

	@Override
	public void putStack(ItemStack par1ItemStack)
	{

	}

	@Override
	public void onSlotChanged()
	{

	}

	@Override
	public int getSlotStackLimit()
	{
		return 0;
	}

	@Override
	public ItemStack decrStackSize(int par1)
	{
		return null;
	}

	@Override
	public boolean isSlotInInventory(IInventory par1IInventory, int par2)
	{
		return false;
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	@Override
	public int getSlotIndex()
	{
		return 0;
	}
}
