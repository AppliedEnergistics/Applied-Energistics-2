package appeng.util.item;

import java.util.Iterator;

import appeng.api.storage.data.IAEStack;

public class MeaningfulIterator<StackType extends IAEStack> implements Iterator
{

	final Iterator<StackType> parent;
	private StackType next;

	public MeaningfulIterator(Iterator<StackType> iterator) {
		parent = iterator;
	}

	@Override
	public boolean hasNext()
	{
		while (parent.hasNext())
		{
			next = parent.next();
			if ( next.isMeaningful() )
				return true;
			else
				parent.remove(); // self cleaning :3
		}

		return false;
	}

	@Override
	public Object next()
	{
		return next;
	}

	@Override
	public void remove()
	{
		parent.remove();
	}

}
