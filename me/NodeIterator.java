package appeng.me;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class NodeIterator<IGridNode> implements Iterator<IGridNode>
{

	boolean hasMore;
	Iterator lvl1;
	Iterator lvl2;

	boolean pull()
	{
		hasMore = lvl1.hasNext();
		if ( hasMore )
		{
			lvl2 = ((Collection) lvl1.next()).iterator();
			return true;
		}
		return false;
	}

	public NodeIterator(HashMap<Class, Set<IGridNode>> machines) {
		lvl1 = machines.values().iterator();
		pull();
	}

	@Override
	public boolean hasNext()
	{
		if ( lvl2.hasNext() )
			return true;
		if ( pull() )
			return hasNext();
		return hasMore;
	}

	@Override
	public IGridNode next()
	{
		return (IGridNode) lvl2.next();
	}

	@Override
	public void remove()
	{
		lvl2.remove();
	}

}
