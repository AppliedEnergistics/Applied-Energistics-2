package appeng.debug;

import java.util.EnumSet;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.IGridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.grid.AENetworkTile;

public class TilePhantomNode extends AENetworkTile
{

	protected AENetworkProxy RWAR = null;
	boolean crashMode = false;

	@Override
	public void onReady()
	{
		super.onReady();
		RWAR = createProxy();
		RWAR.onReady();
		crashMode = true;
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir)
	{
		if ( !crashMode )
			return super.getGridNode( dir );

		return RWAR.getNode();
	}

	public void BOOM()
	{
		if ( RWAR != null )
		{
			crashMode = true;
			RWAR.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
		}
	}
}
