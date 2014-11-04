package appeng.debug;

import java.util.EnumSet;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.IGridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.grid.AENetworkTile;

public class TilePhantomNode extends AENetworkTile
{

	protected AENetworkProxy proxy = null;
	boolean crashMode = false;

	@Override
	public void onReady()
	{
		super.onReady();
		proxy = createProxy();
		proxy.onReady();
		crashMode = true;
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir)
	{
		if ( !crashMode )
			return super.getGridNode( dir );

		return proxy.getNode();
	}

	public void BOOM()
	{
		if ( proxy != null )
		{
			crashMode = true;
			proxy.setValidSides( EnumSet.allOf( ForgeDirection.class ) );
		}
	}
}
