package appeng.me.pathfinding;

import appeng.api.networking.IGridConnecitonVisitor;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.me.GridConnection;
import appeng.me.GridNode;

public class AdHocChannelUpdater implements IGridConnecitonVisitor
{

	final private int usedChannels;

	public AdHocChannelUpdater(int used) {
		usedChannels = used;
	}

	@Override
	public boolean visitNode(IGridNode n)
	{
		GridNode gn = (GridNode) n;
		gn.setControllerRoute( null, true );
		gn.incrementChannelCount( usedChannels );
		gn.finalizeChannels();
		return true;
	}

	@Override
	public void visitConnection(IGridConnection gcc)
	{
		GridConnection gc = (GridConnection) gcc;
		gc.setControllerRoute( null, true );
		gc.incrementChannelCount( usedChannels );
		gc.finalizeChannels();
	}
}
