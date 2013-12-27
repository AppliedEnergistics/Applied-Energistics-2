package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.util.Platform;

public class SlotFake extends AppEngSlot
{

	int invSlot;

	public SlotFake(IInventory inv, int idx, int x, int y) {
		super( inv, idx, x, y );
		invSlot = idx;
	}

	public void addToInv(ItemStack is)
	{
		if ( is != null )
		{
			ItemStack current = this.inventory.getStackInSlot( invSlot );

			if ( current != null && Platform.isSameItem( current, is ) )
			{
				current.stackSize += is.stackSize;

				if ( current.stackSize > inventory.getInventoryStackLimit() )
					current.stackSize = inventory.getInventoryStackLimit();
			} else
			{
				current = is.copy();

				if ( current.stackSize > inventory.getInventoryStackLimit() )
					current.stackSize = inventory.getInventoryStackLimit();

				this.inventory.setInventorySlotContents( invSlot, current );
			}
		} else
		{
			this.inventory.setInventorySlotContents( invSlot, null );
		}
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	@Override
	public ItemStack decrStackSize(int par1)
	{
		ItemStack current = this.inventory.getStackInSlot( invSlot );

		if ( current != null )
		{
			current.stackSize--;

			if ( current.stackSize <= 0 )
			{
				this.inventory.setInventorySlotContents( invSlot, null );
			}
		}

		return null;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
	}

	@Override
	public void putStack(ItemStack par1ItemStack)
	{
		this.inventory.setInventorySlotContents( invSlot, par1ItemStack );
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return true;
	}
}
