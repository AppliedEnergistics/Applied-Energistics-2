package appeng.tile.grid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.AEBaseInvTile;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;

public abstract class AENetworkInvTile extends AEBaseInvTile implements IActionHost, IGridProxyable
{

	class AENetworkInvTileHandler extends AETileEventHandler
	{

		public AENetworkInvTileHandler() {
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

	public AENetworkInvTile() {
		addNewHandler( new AENetworkInvTileHandler() );
	}

	protected AENetworkProxy gridProxy = new AENetworkProxy( this, "proxy", getItemFromTile( this ), true );

	@Override
	public AENetworkProxy getProxy()
	{
		return gridProxy;
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
