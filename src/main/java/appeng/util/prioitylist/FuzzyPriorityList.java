package appeng.util.prioitylist;

import java.util.Collection;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public class FuzzyPriorityList<T extends IAEStack<T>> implements IPartitionList<T>
{

	final IItemList<T> list;
	final FuzzyMode mode;

	public FuzzyPriorityList(IItemList<T> in, FuzzyMode mode) {
		list = in;
		this.mode = mode;
	}

	public boolean isListed(T input)
	{
		Collection<T> out = list.findFuzzy( input, mode );
		return out != null && !out.isEmpty();
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
