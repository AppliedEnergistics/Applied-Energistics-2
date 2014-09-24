package appeng.me;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridVisitor;

class GridSplitDetector implements IGridVisitor
{

	final IGridNode pivot;
	boolean pivotFound;

	public GridSplitDetector(IGridNode pivot) {
		this.pivot = pivot;
	}

	@Override
	public boolean visitNode(IGridNode n)
	{
		if ( n == pivot )
			pivotFound = true;

		return !pivotFound;
	}
};
