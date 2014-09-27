package appeng.me.cache.helpers;

import java.util.HashMap;
import java.util.concurrent.Callable;

import appeng.api.networking.IGridNode;
import appeng.parts.p2p.PartP2PTunnelME;

public class Connections implements Callable
{

	final private PartP2PTunnelME me;
	final public HashMap<IGridNode, TunnelConnection> connections = new HashMap();

	public boolean create = false;
	public boolean destroy = false;

	public Connections(PartP2PTunnelME o) {
		me = o;
	}

	@Override
	public Object call() throws Exception
	{
		me.updateConnections( this );

		return null;
	}

	public void markDestroy()
	{
		create = false;
		destroy = true;
	}

	public void markCreate()
	{
		create = true;
		destroy = false;
	}

}
