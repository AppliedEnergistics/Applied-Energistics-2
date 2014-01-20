package appeng.me.cache.helpers;

import java.util.Collection;
import java.util.Iterator;

import appeng.parts.p2p.PartP2PTunnel;
import appeng.util.iterators.NullIterator;

public class TunnelCollection<T extends PartP2PTunnel> implements Iterable<T>
{

	final Class clz;
	Collection<T> tunnelsource;

	public TunnelCollection(Collection<T> src, Class c) {
		tunnelsource = src;
		clz = c;
	}

	@Override
	public Iterator<T> iterator()
	{
		if ( tunnelsource == null )
			return new NullIterator();
		return new TunnelIterator( tunnelsource, clz );
	}

	public void setSource(Collection<T> c)
	{
		tunnelsource = c;
	}

	public boolean isEmpty()
	{
		return iterator().hasNext();
	}
}
