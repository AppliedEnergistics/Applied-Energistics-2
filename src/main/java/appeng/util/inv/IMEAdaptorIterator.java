package appeng.util.inv;

import java.util.Iterator;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class IMEAdaptorIterator implements Iterator<ItemSlot>
{

	final Iterator<IAEItemStack> stack;
	final ItemSlot slot = new ItemSlot();
	int offset = 0;
	boolean hasNext;

	final IMEAdaptor parent;
	final int containerSize;

	public IMEAdaptorIterator(IMEAdaptor parent, IItemList<IAEItemStack> availableItems) {
		stack = availableItems.iterator();
		containerSize = parent.maxSlots;
		this.parent = parent;
	}

	@Override
	public boolean hasNext()
	{
		hasNext = stack.hasNext();
		return offset < containerSize || hasNext;
	}

	@Override
	public ItemSlot next()
	{
		slot.slot = offset++;
		slot.isExtractable=true;

		if ( parent.maxSlots < offset )
			parent.maxSlots = offset;

		if ( hasNext )
		{
			IAEItemStack item = stack.next();
			slot.setAEItemStack( item );
			return slot;
		}

		slot.setItemStack( null );
		return slot;
	}

	@Override
	public void remove()
	{
		throw new RuntimeException( "Not Implemented!" );
	}
}
