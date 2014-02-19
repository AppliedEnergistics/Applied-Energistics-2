package appeng.me;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class NetworkList implements Collection<Grid>
{

	private List<Grid> networks = new LinkedList();

	@Override
	public boolean add(Grid e)
	{
		copy();
		return networks.add( e );
	}

	@Override
	public boolean addAll(Collection<? extends Grid> c)
	{
		copy();
		return networks.addAll( c );
	}

	@Override
	public void clear()
	{
		networks = new LinkedList<Grid>();
	}

	@Override
	public boolean contains(Object o)
	{
		return networks.contains( o );
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return networks.containsAll( c );
	}

	@Override
	public boolean isEmpty()
	{
		return networks.isEmpty();
	}

	@Override
	public Iterator<Grid> iterator()
	{
		return networks.iterator();
	}

	@Override
	public boolean remove(Object o)
	{
		copy();
		return networks.remove( o );
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		copy();
		return networks.removeAll( c );
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		copy();
		return networks.retainAll( c );
	}

	private void copy()
	{
		List<Grid> old = networks;
		networks = new LinkedList<Grid>();
		networks.addAll( old );
	}

	@Override
	public int size()
	{
		return networks.size();
	}

	@Override
	public Object[] toArray()
	{
		return networks.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return networks.toArray( a );
	}

}
