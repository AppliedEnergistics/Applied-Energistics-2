package appeng.api.parts.layers;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public class InvSot
{

	final public ISidedInventory partInv;
	final public int index;

	public InvSot(ISidedInventory part, int slot) {
		partInv = part;
		index = slot;
	}

	public ItemStack decrStackSize(int j)
	{
		return partInv.decrStackSize( index, j );
	}

	public ItemStack getStackInSlot()
	{
		return partInv.getStackInSlot( index );
	}

	public boolean isItemValidForSlot(ItemStack itemstack)
	{
		return partInv.isItemValidForSlot( index, itemstack );
	}

	public void setInventorySlotContents(ItemStack itemstack)
	{
		partInv.setInventorySlotContents( index, itemstack );
	}

	public boolean canExtractItem(ItemStack itemstack, int side)
	{
		return partInv.canExtractItem( index, itemstack, side );
	}

	public boolean canInsertItem(ItemStack itemstack, int side)
	{
		return partInv.canInsertItem( index, itemstack, side );
	}

}
