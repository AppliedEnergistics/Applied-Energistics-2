package appeng.tile.grid;

import net.minecraftforge.common.ForgeDirection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.powersink.AEBasePoweredTile;

public abstract class AENetworkPowerTile extends AEBasePoweredTile implements IGridHost, IGridProxyable
{

	protected AENetworkProxy gridProxy = new AENetworkProxy( this, "proxy", true );

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.SMART;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

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
