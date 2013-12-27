package appeng.util.prioitylist;

import java.util.ArrayList;
import java.util.List;

import appeng.api.storage.data.IAEStack;

public class DefaultPriorityList<T extends IAEStack<T>> implements IPartitionList<T>
{

	final static List nullList = new ArrayList();

	public boolean isListed(T input)
	{
		return false;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public Iterable<T> getItems()
	{
		return (Iterable<T>) nullList;
	}

}
