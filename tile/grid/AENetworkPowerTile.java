package appeng.tile.grid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.powersink.AEBasePoweredTile;

public abstract class AENetworkPowerTile extends AEBasePoweredTile implements IActionHost, IGridProxyable
{

	class AENetworkPowerTileHandler extends AETileEventHandler
	{

		public AENetworkPowerTileHandler() {
			super( TileEventType.WORLD_NBT );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			gridProxy.readFromNBT( data );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			gridProxy.writeToNBT( data );
		}

	};

	public AENetworkPowerTile() {
		addNewHandler( new AENetworkPowerTileHandler() );
	}

	protected AENetworkProxy gridProxy = new AENetworkProxy( this, "proxy", getItemFromTile( this ), true );

	@Override
	public AENetworkProxy getProxy()
	{
		return gridProxy;
	}

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

	@Override
	public IGridNode getActionableNode()
	{
		return gridProxy.getNode();
	}
}
