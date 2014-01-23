package appeng.tile.grid;

import net.minecraftforge.common.ForgeDirection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.AEBaseInvTile;

public abstract class AENetworkInvTile extends AEBaseInvTile implements IGridHost, IGridProxyable
{

	protected AENetworkProxy gridProxy = new AENetworkProxy( this, "proxy", getItemFromTile( this ), true );

	@Override
	public IGridNode getGridNode(ForgeDirection dir)
	{
		return gridProxy.getNode();
	}

	@Override
	public void onReady()
	{
		super.onReady();
		gridProxy.onReady();
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		gridProxy.onChunkUnload();
	}

	@Override
	public void validate()
	{
		super.validate();
		gridProxy.validate();
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		gridProxy.invalidate();
	}

	@Override
	public void gridChanged()
	{

	}
}
