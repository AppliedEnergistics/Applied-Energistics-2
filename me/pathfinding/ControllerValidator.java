package appeng.me.pathfinding;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridVisitor;
import appeng.tile.networking.TileController;

public class ControllerValidator implements IGridVisitor
{

	int minX;
	int minY;
	int minZ;

	int maxX;
	int maxY;
	int maxZ;

	public boolean isValid = true;
	public int found = 0;

	public ControllerValidator(int x, int y, int z) {
		minX = x;
		minY = y;
		minZ = z;
		maxX = x;
		maxY = y;
		maxZ = z;
	}

	@Override
	public boolean visitNode(IGridNode n)
	{
		IGridHost host = n.getMachine();
		if ( isValid && host instanceof TileController )
		{
			TileController c = (TileController) host;

			minX = Math.min( c.xCoord, minX );
			maxX = Math.max( c.xCoord, maxX );
			minY = Math.min( c.yCoord, minY );
			maxY = Math.max( c.yCoord, maxY );
			minZ = Math.min( c.zCoord, minZ );
			maxZ = Math.max( c.zCoord, maxZ );

			if ( maxX - minX < 7 && maxY - minY < 7 && maxZ - minZ < 7 )
			{
				found++;
				return true;
			}

			isValid = false;
		}
		else
			return false;

		return isValid;
	}
}
