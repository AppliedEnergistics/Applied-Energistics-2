package appeng.util.inv;


import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraftforge.items.IItemHandler;


class ItemHandlerIterator implements Iterator<ItemSlot>
{

	private final IItemHandler itemHandler;

	private final ItemSlot itemSlot = new ItemSlot();

	private int slot = 0;

	ItemHandlerIterator( IItemHandler itemHandler )
	{
		this.itemHandler = itemHandler;
	}

	@Override
	public boolean hasNext()
	{
		return slot < itemHandler.getSlots();
	}

	@Override
	public ItemSlot next()
	{
		if( slot >= itemHandler.getSlots() )
		{
			throw new NoSuchElementException();
		}
		itemSlot.setExtractable( itemHandler.extractItem( slot, 1, true ) != null );
		itemSlot.setItemStack( itemHandler.getStackInSlot( slot ) );
		itemSlot.setSlot( slot );
		slot++;
		return itemSlot;
	}

}
