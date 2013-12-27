package appeng.me;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridVisitor;

public class GridPropagator implements IGridVisitor
{

	final private Grid g;

	public GridPropagator(Grid g) {
		this.g = g;
	}

	@Override
	public boolean visitNode(IGridNode n)
	{
		GridNode gn = (GridNode) n;
		if ( gn.myGrid != g || g.pivot == n )
		{
			gn.setGrid( g );
			return true;
		}
		return false;
	}

}
