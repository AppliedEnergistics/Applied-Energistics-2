package appeng.parts.p2p;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;
import appeng.core.AELog;
import appeng.core.settings.TickRates;
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.Connections;
import appeng.me.cache.helpers.TunnelConnection;
import appeng.me.helpers.AENetworkProxy;

public class PartP2PTunnelME extends PartP2PTunnel<PartP2PTunnelME> implements IGridTickable
{

	@Override
	public TunnelType getTunnelType()
	{
		return TunnelType.ME;
	}

	final AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", null, true );
	public final Connections connection = new Connections( this );

	public PartP2PTunnelME(ItemStack is) {
		super( is );
		proxy.setFlags( GridFlags.REQUIRE_CHANNEL, GridFlags.COMPRESSED_CHANNEL );
		outerProxy.setFlags( GridFlags.DENSE_CAPACITY, GridFlags.CANNOT_CARRY_COMPRESSED );
	}

	@Override
	public void setPartHostInfo(ForgeDirection side, IPartHost host, TileEntity tile)
	{
		super.setPartHostInfo( side, host, tile );
		outerProxy.setValidSides( EnumSet.of( side ) );
	}

	@Override
	public void readFromNBT(NBTTagCompound extra)
	{
		super.readFromNBT( extra );
		outerProxy.readFromNBT( extra );
	}

	@Override
	public void writeToNBT(NBTTagCompound extra)
	{
		super.writeToNBT( extra );
		outerProxy.writeToNBT( extra );
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		outerProxy.onReady();
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		outerProxy.invalidate();
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return outerProxy.getNode();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.DENSE;
	}

	@Override
	public void onTunnelNetworkChange()
	{
		super.onTunnelNetworkChange();
		if ( !output )
		{
			try
			{
				proxy.getTick().wakeDevice( proxy.getNode() );
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.METunnel.min, TickRates.METunnel.max, true, false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		// just move on...
		try
		{
			if ( !proxy.getPath().isNetworkBooting() )
			{
				if ( !proxy.getEnergy().isNetworkPowered() )
				{
					connection.markDestroy();
					TickHandler.instance.addCallable( tile.getWorldObj(), connection );
				}
				else
				{
					if ( proxy.isActive() )
					{
						connection.markCreate();
						TickHandler.instance.addCallable( tile.getWorldObj(), connection );
					}
					else
					{
						connection.markDestroy();
						TickHandler.instance.addCallable( tile.getWorldObj(), connection );
					}
				}

				return TickRateModulation.SLEEP;
			}
		}
		catch (GridAccessException e)
		{
			// meh?
		}

		return TickRateModulation.IDLE;
	}

	@Override
	public void onPlacement(EntityPlayer player, ItemStack held, ForgeDirection side)
	{
		super.onPlacement( player, held, side );
		outerProxy.setOwner( player );
	}

	public void updateConnections(Connections connections)
	{
		if ( connections.destroy )
		{
			for (TunnelConnection cw : connection.connections.values())
				cw.c.destroy();

			connection.connections.clear();
		}
		else if ( connections.create )
		{

			Iterator<TunnelConnection> i = connection.connections.values().iterator();
			while (i.hasNext())
			{
				TunnelConnection cw = i.next();
				try
				{
					if ( cw.tunnel.proxy.getGrid() != proxy.getGrid() )
					{
						cw.c.destroy();
						i.remove();
					}
					else if ( !cw.tunnel.proxy.isActive() )
					{
						cw.c.destroy();
						i.remove();
					}
				}
				catch (GridAccessException e)
				{
					// :P
				}
			}

			LinkedList<PartP2PTunnelME> newSides = new LinkedList<PartP2PTunnelME>();
			try
			{
				for (PartP2PTunnelME me : getOutputs())
				{
					if ( me.proxy.isActive() && connections.connections.get( me.getGridNode() ) == null )
					{
						newSides.add( me );
					}
				}

				for (PartP2PTunnelME me : newSides)
				{
					try
					{
						connections.connections.put( me.getGridNode(),
								new TunnelConnection( me, AEApi.instance().createGridConnection( outerProxy.getNode(), me.outerProxy.getNode() ) ) );
					}
					catch (FailedConnection e)
					{
						AELog.error( e );
						// :(
					}
				}
			}
			catch (GridAccessException e)
			{
				AELog.error( e );
			}
		}
	}
}
