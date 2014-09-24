package appeng.me;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import appeng.api.networking.IGridNode;
import appeng.api.util.IReadOnlyCollection;

public class NodeIterable<T> implements IReadOnlyCollection<T>
{

	final private HashMap<Class, Set<IGridNode>> Machines;

	public NodeIterable(HashMap<Class, Set<IGridNode>> Machines) {
		this.Machines = Machines;
	}

	@Override
	public Iterator<T> iterator()
	{
		return new NodeIterator( Machines );
	}

	@Override
	public int size()
	{
		int size = 0;

		for (Set<IGridNode> o : Machines.values())
			size += o.size();

		return size;
	}

	@Override
	public boolean isEmpty()
	{
		for (Set<IGridNode> o : Machines.values())
			if ( !o.isEmpty() )
				return false;

		return true;
	}

	@Override
	public boolean contains(Object node)
	{
		Class c = ((IGridNode) node).getMachine().getClass();

		Set<IGridNode> p = Machines.get( c );
		if ( p != null )
			return p.contains( node );

		return false;
	}
}
