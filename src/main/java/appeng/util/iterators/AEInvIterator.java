package appeng.util.iterators;

import java.util.Iterator;

import appeng.api.storage.data.IAEItemStack;
import appeng.tile.inventory.AppEngInternalAEInventory;

public class AEInvIterator implements Iterator<IAEItemStack>
{

	final AppEngInternalAEInventory inv;
	final int size;

	int x = 0;

	public AEInvIterator(AppEngInternalAEInventory i) {
		inv = i;
		size = inv.getSizeInventory();
	}

	@Override
	public boolean hasNext()
	{
		return x < size;
	}

	@Override
	public IAEItemStack next()
	{
		return inv.getAEStackInSlot( x++ );
	}

	@Override
	public void remove()
	{
		throw new RuntimeException( "no..." );
	}

}
