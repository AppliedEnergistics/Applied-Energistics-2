package appeng.util.prioitylist;

import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public class PrecisePriorityList<T extends IAEStack<T>> implements IPartitionList<T>
{

	final IItemList<T> list;

	public PrecisePriorityList(IItemList<T> in) {
		list = in;
	}

	public boolean isListed(T input)
	{
		return list.findPrecise( input ) != null;
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public Iterable<T> getItems()
	{
		return list;
	}

}
