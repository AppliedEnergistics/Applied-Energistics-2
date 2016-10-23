package appeng.util.inv;


import java.util.Iterator;

import net.minecraftforge.items.IItemHandler;


class ItemHandlerIterator implements Iterator<ItemSlot>
{

	private final IItemHandler itemHandler;

	private final ItemSlot itemSlot = new ItemSlot();

	private int slot = 0;

	public ItemHandlerIterator( IItemHandler itemHandler )
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
		itemSlot.setExtractable( itemHandler.extractItem( slot, 1, true ) != null );
		itemSlot.setItemStack( itemHandler.getStackInSlot( slot ) );
		itemSlot.setSlot( slot );
		slot++;
		return itemSlot;
	}

}
