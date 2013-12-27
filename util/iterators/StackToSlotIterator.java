package appeng.util.iterators;

import java.util.Iterator;

import net.minecraft.item.ItemStack;
import appeng.util.inv.ItemSlot;

public class StackToSlotIterator implements Iterator<ItemSlot>
{

	int x = 0;
	final ItemSlot iss = new ItemSlot();
	final Iterator<ItemStack> is;

	public StackToSlotIterator(Iterator<ItemStack> is) {
		this.is = is;
	}

	@Override
	public boolean hasNext()
	{
		return is.hasNext();
	}

	@Override
	public ItemSlot next()
	{
		iss.slot = x++;
		iss.itemStack = is.next();
		return iss;
	}

	@Override
	public void remove()
	{
		// uhh no.
	}

}
