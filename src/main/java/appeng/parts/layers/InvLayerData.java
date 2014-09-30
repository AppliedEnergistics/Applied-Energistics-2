package appeng.parts.layers;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public class InvLayerData
{

	// a simple empty array for empty stuff..
	private final static int[] nullSides = new int[] {};

	// cache of inventory state.
	final private int sides[][];
	final private List<ISidedInventory> inventories;
	final private List<InvSot> slots;

	public InvLayerData(int a[][], List<ISidedInventory> b, List<InvSot> c) {
		sides = a;
		inventories = b;
		slots = c;
	}

	/**
	 * check if a slot index is valid, prevent crashes from bad code :)
	 * 
	 * @param slot slot index
	 * @return true, if the slot exists.
	 */
	boolean isSlotValid(int slot)
	{
		return slots != null && slot >= 0 && slot < slots.size();
	}

	public ItemStack decreaseStackSize(int slot, int amount)
	{
		if ( isSlotValid( slot ) )
			return slots.get( slot ).decreaseStackSize( amount );

		return null;
	}

	public int getSizeInventory()
	{
		if ( slots == null )
			return 0;

		return slots.size();
	}

	public ItemStack getStackInSlot(int slot)
	{
		if ( isSlotValid( slot ) )
			return slots.get( slot ).getStackInSlot();

		return null;
	}

	public boolean isItemValidForSlot(int slot, ItemStack itemstack)
	{
		if ( isSlotValid( slot ) )
			return slots.get( slot ).isItemValidForSlot( itemstack );

		return false;
	}

	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		if ( isSlotValid( slot ) )
			slots.get( slot ).setInventorySlotContents( itemstack );
	}

	public boolean canExtractItem(int slot, ItemStack itemstack, int side)
	{
		if ( isSlotValid( slot ) )
			return slots.get( slot ).canExtractItem( itemstack, side );

		return false;
	}

	public boolean canInsertItem(int slot, ItemStack itemstack, int side)
	{
		if ( isSlotValid( slot ) )
			return slots.get( slot ).canInsertItem( itemstack, side );

		return false;
	}

	public void markDirty()
	{
		if ( inventories != null )
		{
			for (IInventory inv : inventories)
				inv.markDirty();
		}
	}

	public int[] getAccessibleSlotsFromSide(int side)
	{
		if ( sides == null || side < 0 || side > 5 )
			return nullSides;
		return sides[side];
	}

}
