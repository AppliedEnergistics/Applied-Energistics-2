/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.parts.p2p;

import java.util.concurrent.Callable;

import appeng.api.networking.GridFlags;
import appeng.core.AELog;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.oc.api.API;
import li.cil.oc.api.Items;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import li.cil.oc.api.network.Visibility;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.hooks.TickHandler;
import appeng.integration.IntegrationType;
import appeng.me.GridAccessException;
import appeng.transformer.annotations.Integration.Interface;
import appeng.transformer.annotations.Integration.InterfaceList;

@InterfaceList(value = { @Interface(iface = "li.cil.oc.api.network.Environment", iname = "OpenComputers"), @Interface(iface = "li.cil.oc.api.network.SidedEnvironment", iname = "OpenComputers") })
public class PartP2POpenComputers extends PartP2PTunnel<PartP2POpenComputers> implements IGridTickable, Environment, SidedEnvironment
{
	private final Node node;

	private final Callable updateCallback;

	public PartP2POpenComputers(ItemStack is)
	{
		super( is );
		this.proxy.setFlags( GridFlags.REQUIRE_CHANNEL, GridFlags.COMPRESSED_CHANNEL );

		if ( !AppEng.instance.isIntegrationEnabled( IntegrationType.OpenComputers ) )
		{
			throw new RuntimeException( "OpenComputers is not installed!" );
		}

		// Avoid NPE when called in pre-init phase (part population).
		if ( API.network != null )
		{
			node = Network.newNode( this, Visibility.None ).create();
		}
		else
		{
			node = null; // to satisfy final
		}

		updateCallback = new Callable()
		{
			@Override
			public Object call() throws Exception
			{
				updateConnections();
				return null;
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Items.get( "adapter" ).block().getBlockTextureFromSide( 2 );
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		if (node() != null)
		{
			node().remove();
		}
	}

	@Override
	public void onTunnelNetworkChange()
	{
		super.onTunnelNetworkChange();
		try
		{
			this.proxy.getTick().wakeDevice( this.proxy.getNode() );
		}
		catch( GridAccessException e )
		{
			// ignore
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );
		if (node() != null)
		{
			node().load( data );
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );
		if (node() != null)
		{
			node().save( data );
		}
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		return new TickingRequest( TickRates.OpenComputersTunnel.min, TickRates.OpenComputersTunnel.max, true, false );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int TicksSinceLastCall )
	{
		try
		{
			if( !this.proxy.getPath().isNetworkBooting() )
			{
				if ( node() != null ) // Client side doesn't have nodes.
				{
					TickHandler.INSTANCE.addCallable( this.tile.getWorldObj(), this.updateCallback );
				}

				return TickRateModulation.SLEEP;
			}
		}
		catch( GridAccessException e )
		{
			// ignore
		}

		return TickRateModulation.IDLE;
	}

	private void updateConnections()
	{
		if ( proxy.isPowered() && proxy.isActive() )
		{
			// Make sure we're connected to existing OC nodes in the world.
			Network.joinOrCreateNetwork( getTile() );

			if ( output )
			{
				if ( getInput() != null )
				{
					Network.joinOrCreateNetwork( getInput().getTile() );
					node().connect( getInput().node() );
				}
			}
			else
			{
				try
				{
					for ( PartP2POpenComputers output : getOutputs() )
					{
						Network.joinOrCreateNetwork( output.getTile() );
						node().connect( output.node() );
					}
				}
				catch ( GridAccessException e )
				{
					AELog.error( e );
				}
			}
		}
		else
		{
			node().remove();
		}
	}

	@Override
	public Node node()
	{
		return node;
	}

	@Override
	public void onConnect(Node node) {
	}

	@Override
	public void onDisconnect(Node node) {
	}

	@Override
	public void onMessage(Message message) {
	}

	@Override
	public Node sidedNode(ForgeDirection side)
	{
		return side == this.side ? node() : null;
	}

	@Override
	public boolean canConnect(ForgeDirection side)
	{
		return side == this.side;
	}
}
