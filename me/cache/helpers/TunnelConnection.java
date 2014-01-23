package appeng.me.cache.helpers;

import appeng.api.networking.IGridConnection;
import appeng.parts.p2p.PartP2PTunnelME;

public class TunnelConnection
{

	final public PartP2PTunnelME tunnel;
	final public IGridConnection c;

	public TunnelConnection(PartP2PTunnelME t, IGridConnection con) {
		tunnel = t;
		c = con;
	}
}