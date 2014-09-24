package appeng.client.me;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;

public class SlotME extends Slot
{

	public InternalSlotME mySlot;

	public SlotME(InternalSlotME me) {
		super( null, 0, me.xPos, me.yPos );
		mySlot = me;
	}

	@Override
	public ItemStack getStack()
	{
		if ( mySlot.hasPower() )
			return mySlot.getStack();
		return null;
	}

	public IAEItemStack getAEStack()
	{
		if ( mySlot.hasPower() )
			return mySlot.getAEStack();
		return null;
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	@Override
	public ItemStack decrStackSize(int par1)
	{
		return null;
	}

	@Override
	public void putStack(ItemStack par1ItemStack)
	{

	}

	@Override
	public boolean getHasStack()
	{
		if ( mySlot.hasPower() )
			return getStack() != null;
		return false;
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

	@Override
	public int getSlotStackLimit()
	{
		return 0;
	}

	@Override
	public boolean isSlotInInventory(IInventory par1iInventory, int par2)
	{
		return false;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
	}

}
