package appeng.me.cache.helpers;

import java.util.Collection;
import java.util.Iterator;

import appeng.parts.p2p.PartP2PTunnel;

public class TunnelIterator<T extends PartP2PTunnel> implements Iterator<T>
{

	Iterator<T> wrapped;
	Class targetType;
	T Next;

	private void findNext()
	{
		while (Next == null && wrapped.hasNext())
		{
			Next = wrapped.next();
			if ( !targetType.isInstance( Next ) )
				Next = null;
		}
	}

	public TunnelIterator(Collection<T> tunnelsource, Class clz) {
		wrapped = tunnelsource.iterator();
		targetType = clz;
		findNext();
	}

	@Override
	public boolean hasNext()
	{
		findNext();
		return Next != null;
	}

	@Override
	public T next()
	{
		T tmp = Next;
		Next = null;
		return tmp;
	}

	@Override
	public void remove()
	{
		// no.
	}

}
