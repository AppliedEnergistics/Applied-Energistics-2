package appeng.util.item;

import java.util.Iterator;

import appeng.api.storage.data.IAEStack;

public class MeaningfulIterator<StackType extends IAEStack> implements Iterator<StackType>
{

	private final Iterator<StackType> parent;
	private StackType next;

	public MeaningfulIterator(Iterator<StackType> iterator)
	{
		this.parent = iterator;
	}

	@Override
	public boolean hasNext()
	{
		while (this.parent.hasNext())
		{
			this.next = this.parent.next();
			if ( this.next.isMeaningful() )
			{
				return true;
			}
			else
			{
				this.parent.remove(); // self cleaning :3
			}
		}

		return false;
	}

	@Override
	public StackType next()
	{
		return this.next;
	}

	@Override
	public void remove()
	{
		parent.remove();
	}

}
