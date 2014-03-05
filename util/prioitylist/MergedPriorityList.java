package appeng.util.prioitylist;

import java.util.ArrayList;
import java.util.List;

import appeng.api.storage.data.IAEStack;

public class MergedPriorityList<T extends IAEStack<T>> implements IPartitionList<T>
{

	final List<IPartitionList<T>> positive = new ArrayList();
	final List<IPartitionList<T>> negative = new ArrayList();

	public void addNewList(IPartitionList<T> list, boolean isWhitelist)
	{
		if ( isWhitelist )
			positive.add( list );
		else
			negative.add( list );
	}

	public boolean isListed(T input)
	{
		for (IPartitionList<T> l : negative)
			if ( l.isListed( input ) )
				return false;

		if ( !positive.isEmpty() )
		{
			for (IPartitionList<T> l : positive)
				if ( l.isListed( input ) )
					return true;

			return false;
		}

		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return positive.isEmpty() && negative.isEmpty();
	}

	@Override
	public Iterable<T> getItems()
	{
		throw new RuntimeException( "Not Implemented" );
	}

}
