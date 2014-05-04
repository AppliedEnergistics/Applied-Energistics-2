package appeng.util.inv;

import java.util.Iterator;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class IMEAdaptorIterator implements Iterator<ItemSlot>
{

	Iterator<IAEItemStack> stack;
	ItemSlot slot = new ItemSlot();
	int offset = 0;

	public IMEAdaptorIterator(IItemList<IAEItemStack> availableItems) {
		stack = availableItems.iterator();
	}

	@Override
	public boolean hasNext()
	{
		return stack.hasNext();
	}

	@Override
	public ItemSlot next()
	{
		IAEItemStack item = stack.next();
		slot.setAEItemStack( item );
		slot.slot = offset++;
		return slot;
	}

	@Override
	public void remove()
	{
		throw new RuntimeException( "Not Implemented!" );
	}
}
