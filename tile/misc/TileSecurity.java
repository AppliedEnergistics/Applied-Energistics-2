package appeng.tile.misc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;

public class TileSecurity extends AENetworkTile
{

	private static int diffrence = 0;
	private boolean isActive = false;

	public long securityKey;

	@Override
	public void onReady()
	{
		super.onReady();
		if ( Platform.isServer() )
			isActive = true;
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		isActive = false;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		isActive = false;
	}

	class SecurityHandler extends AETileEventHandler
	{

		public SecurityHandler() {
			super( TileEventType.WORLD_NBT, TileEventType.NETWORK );
		}

		@Override
		public boolean readFromStream(DataInputStream data) throws IOException
		{
			boolean wasActive = isActive;
			isActive = data.readBoolean();

			return wasActive != isActive;
		}

		@Override
		public void writeToStream(DataOutputStream data) throws IOException
		{
			data.writeBoolean( gridProxy.isActive() );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			data.setLong( "securityKey", securityKey );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			securityKey = data.getLong( "securityKey" );
		}
	};

	public void readPermissions(HashMap<Integer, EnumSet<SecurityPermissions>> playerPerms)
	{
		playerPerms.put( gridProxy.getNode().getPlayerID(), EnumSet.allOf( SecurityPermissions.class ) );
	}

	@MENetworkEventSubscribe
	public void bootUpdate(MENetworkChannelsChanged changed)
	{
		markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerUpdate(MENetworkPowerStatusChange changed)
	{
		markForUpdate();
	}

	public boolean isSecurityEnabled()
	{
		return isActive && gridProxy.isActive();
	}

	public void updateNodeCount(int nodes)
	{
		gridProxy.setIdlePowerUsage( 2.0 + ((double) nodes / 0.033) );
	}

	public TileSecurity() {
		addNewHandler( new SecurityHandler() );
		gridProxy.setFlags( GridFlags.REQURE_CHANNEL );
		gridProxy.setIdlePowerUsage( 2.0 );
		diffrence++;
		securityKey = System.currentTimeMillis() * 10 + diffrence;
		if ( diffrence > 10 )
			diffrence = 0;
	}

	public int getOwner()
	{
		return gridProxy.getNode().getPlayerID();
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

	public boolean isActive()
	{
		return isActive;
	}

}
