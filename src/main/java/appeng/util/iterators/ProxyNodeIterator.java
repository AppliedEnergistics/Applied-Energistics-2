package appeng.util.iterators;

import java.util.Iterator;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;

public class ProxyNodeIterator implements Iterator<IGridNode>
{

	final Iterator<IGridHost> hosts;

	public ProxyNodeIterator(Iterator<IGridHost> hosts) {
		this.hosts = hosts;
	}

	@Override
	public boolean hasNext()
	{
		return hosts.hasNext();
	}

	@Override
	public IGridNode next()
	{
		IGridHost host = hosts.next();
		return host.getGridNode( ForgeDirection.UNKNOWN );
	}

	@Override
	public void remove()
	{
		throw new RuntimeException( "Not implemented." );
	}

}
