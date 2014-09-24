package appeng.me.pathfinding;

import appeng.api.networking.IGridConnectionVisitor;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.me.GridConnection;
import appeng.me.GridNode;

public class ControllerChannelUpdater implements IGridConnectionVisitor
{

	@Override
	public boolean visitNode(IGridNode n)
	{
		GridNode gn = (GridNode) n;
		gn.finalizeChannels();
		return true;
	}

	@Override
	public void visitConnection(IGridConnection gcc)
	{
		GridConnection gc = (GridConnection) gcc;
		gc.finalizeChannels();
	}
}
