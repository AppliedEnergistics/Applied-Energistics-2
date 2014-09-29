package appeng.me.cache.helpers;

import java.util.Collection;
import java.util.Iterator;

import appeng.parts.p2p.PartP2PTunnel;
import appeng.util.iterators.NullIterator;

public class TunnelCollection<T extends PartP2PTunnel> implements Iterable<T>
{

	final Class clz;
	Collection<T> tunnelSources;

	public TunnelCollection(Collection<T> src, Class c) {
		tunnelSources = src;
		clz = c;
	}

	@Override
	public Iterator<T> iterator()
	{
		if ( tunnelSources == null )
			return new NullIterator<T>();
		return new TunnelIterator<T>( tunnelSources, clz );
	}

	public void setSource(Collection<T> c)
	{
		tunnelSources = c;
	}

	public boolean isEmpty()
	{
		return !iterator().hasNext();
	}

	public boolean matches(Class<? extends PartP2PTunnel> c)
	{
		return clz == c;
	}

	public Class<? extends PartP2PTunnel> getClz()
	{
		return clz;
	}
}
