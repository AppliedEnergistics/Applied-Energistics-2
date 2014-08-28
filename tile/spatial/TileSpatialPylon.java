package appeng.tile.spatial;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.EnumSet;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.me.GridAccessException;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.SpatialPylonCalculator;
import appeng.me.cluster.implementations.SpatialPylonCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.AENetworkProxyMultiblock;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;

public class TileSpatialPylon extends AENetworkTile implements IAEMultiBlock
{

	public final int DISPLAY_ENDMIN = 0x01;
	public final int DISPLAY_ENDMAX = 0x02;
	public final int DISPLAY_MIDDLE = 0x01 + 0x02;
	public final int DISPLAY_X = 0x04;
	public final int DISPLAY_Y = 0x08;
	public final int DISPLAY_Z = 0x04 + 0x08;
	public final int MB_STATUS = 0x01 + 0x02 + 0x04 + 0x08;

	public final int DISPLAY_ENABLED = 0x10;
	public final int DISPLAY_POWEREDENABLED = 0x20;
	public final int NET_STATUS = 0x10 + 0x20;

	int displayBits = 0;
	SpatialPylonCluster clust;
	final SpatialPylonCalculator calc = new SpatialPylonCalculator( this );

	boolean didHaveLight = false;

	@Override
	protected AENetworkProxy createProxy()
	{
		return new AENetworkProxyMultiblock( this, "proxy", getItemFromTile( this ), true );
	}

	@Override
	public boolean canBeRotated()
	{
		return false;
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileSpatialPylon(ByteBuf data) throws IOException
	{
		int old = displayBits;
		displayBits = data.readByte();
		return old != displayBits;
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileSpatialPylon(ByteBuf data) throws IOException
	{
		data.writeByte( displayBits );
	}

	public TileSpatialPylon() {
		gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL, GridFlags.MULTIBLOCK );
		gridProxy.setIdlePowerUsage( 0.5 );
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
	}

	@Override
	public void onReady()
	{
		super.onReady();
		onNeighborBlockChange();
	}

	@Override
	public void markForUpdate()
	{
		super.markForUpdate();
		boolean hasLight = getLightValue() > 0;
		if ( hasLight != didHaveLight )
		{
			didHaveLight = hasLight;
			worldObj.func_147451_t( xCoord, yCoord, zCoord );
			// worldObj.updateAllLightTypes( xCoord, yCoord, zCoord );
		}
	}

	public int getLightValue()
	{
		if ( (displayBits & DISPLAY_POWEREDENABLED) == DISPLAY_POWEREDENABLED )
		{
			return 8;
		}
		return 0;
	}

	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		recalculateDisplay();
	}

	@MENetworkEventSubscribe
	public void activeRender(MENetworkChannelsChanged c)
	{
		recalculateDisplay();
	}

	@Override
	public void invalidate()
	{
		disconnect( false );
		super.invalidate();
	}

	@Override
	public void onChunkUnload()
	{
		disconnect( false );
		super.onChunkUnload();
	}

	public void onNeighborBlockChange()
	{
		calc.calculateMultiblock( worldObj, getLocation() );
	}

	@Override
	public SpatialPylonCluster getCluster()
	{
		return clust;
	}

	public void recalculateDisplay()
	{
		int oldBits = displayBits;

		displayBits = 0;

		if ( clust != null )
		{
			if ( clust.min.equals( getLocation() ) )
				displayBits = DISPLAY_ENDMIN;
			else if ( clust.max.equals( getLocation() ) )
				displayBits = DISPLAY_ENDMAX;
			else
				displayBits = DISPLAY_MIDDLE;

			switch (clust.currentAxis)
			{
			case X:
				displayBits |= DISPLAY_X;
				break;
			case Y:
				displayBits |= DISPLAY_Y;
				break;
			case Z:
				displayBits |= DISPLAY_Z;
				break;
			default:
				displayBits = 0;
				break;
			}

			try
			{
				if ( gridProxy.getEnergy().isNetworkPowered() )
					displayBits |= DISPLAY_POWEREDENABLED;

				if ( clust.isValid && gridProxy.isActive() )
					displayBits |= DISPLAY_ENABLED;
			}
			catch (GridAccessException e)
			{
				// nothing?
			}

		}

		if ( oldBits != displayBits )
			markForUpdate();
	}

	public void updateStatus(SpatialPylonCluster c)
	{
		clust = c;
		gridProxy.setValidSides( c == null ? EnumSet.noneOf( ForgeDirection.class ) : EnumSet.allOf( ForgeDirection.class ) );
		recalculateDisplay();
	}

	@Override
	public void disconnect(boolean b)
	{
		if ( clust != null )
		{
			clust.destroy();
			updateStatus( null );
		}
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	public int getDisplayBits()
	{
		return displayBits;
	}

}
