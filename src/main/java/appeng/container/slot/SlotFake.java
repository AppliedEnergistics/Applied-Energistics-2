package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotFake extends AppEngSlot
{

	final int invSlot;

	public SlotFake(IInventory inv, int idx, int x, int y) {
		super( inv, idx, x, y );
		invSlot = idx;
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
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
	}

	@Override
	public void putStack(ItemStack is)
	{
		if ( is != null )
			is = is.copy();

		super.putStack( is );
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

}
