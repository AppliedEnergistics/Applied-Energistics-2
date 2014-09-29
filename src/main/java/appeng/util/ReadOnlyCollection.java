package appeng.util;

import java.util.Collection;
import java.util.Iterator;

import appeng.api.util.IReadOnlyCollection;

public class ReadOnlyCollection<T> implements IReadOnlyCollection<T>
{

	private final Collection<T> c;

	public ReadOnlyCollection(Collection<T> in) {
		c = in;
	}

	@Override
	public Iterator<T> iterator()
	{
		return c.iterator();
	}

	@Override
	public int size()
	{
		return c.size();
	}

	@Override
	public boolean isEmpty()
	{
		return c.isEmpty();
	}

	@Override
	public boolean contains(Object node)
	{
		return c.contains( node );
	}

}
