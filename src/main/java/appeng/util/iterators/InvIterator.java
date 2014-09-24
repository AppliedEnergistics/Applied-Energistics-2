package appeng.util.iterators;

import java.util.Iterator;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InvIterator implements Iterator<ItemStack>
{

	final IInventory inv;
	final int size;

	int x = 0;

	public InvIterator(IInventory i) {
		inv = i;
		size = inv.getSizeInventory();
	}

	@Override
	public boolean hasNext()
	{
		return x < size;
	}

	@Override
	public ItemStack next()
	{
		return inv.getStackInSlot( x++ );
	}

	@Override
	public void remove()
	{
		throw new RuntimeException( "no..." );
	}

}
