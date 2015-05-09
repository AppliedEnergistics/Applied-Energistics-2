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

import javax.annotation.Nullable;

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
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.hooks.TickHandler;
import appeng.integration.IntegrationType;
import appeng.me.GridAccessException;
import appeng.transformer.annotations.Integration.Interface;
import appeng.transformer.annotations.Integration.InterfaceList;


@InterfaceList(value = { @Interface(iface = "li.cil.oc.api.network.Environment", iname = "OpenComputers"), @Interface(iface = "li.cil.oc.api.network.SidedEnvironment", iname = "OpenComputers") })
public final class PartP2POpenComputers extends PartP2PTunnel<PartP2POpenComputers> implements IGridTickable, Environment, SidedEnvironment
{
	@Nullable
	private final Node node;

	private final Callable<Void> updateCallback;

	public PartP2POpenComputers(ItemStack is)
	{
		super( is );

		if ( !AppEng.instance.isIntegrationEnabled( IntegrationType.OpenComputers ) )
		{
			throw new RuntimeException( "OpenComputers is not installed!" );
		}

		// Avoid NPE when called in pre-init phase (part population).
		if ( API.network != null )
		{
			this.node = Network.newNode( this, Visibility.None ).create();
		}
		else
		{
			this.node = null; // to satisfy final
		}

		this.updateCallback = new UpdateCallback();
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
		if ( this.node != null)
		{
			this.node.remove();
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
		if ( this.node != null)
		{
			this.node.load( data );
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );
		if ( this.node != null)
		{
			this.node.save( data );
		}
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		return new TickingRequest( TickRates.OpenComputersTunnel.min, TickRates.OpenComputersTunnel.max, true, false );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int ticksSinceLastCall )
	{
		try
		{
			if( !this.proxy.getPath().isNetworkBooting() )
			{
				if ( this.node() != null ) // Client side doesn't have nodes.
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
		if ( this.proxy.isPowered() && this.proxy.isActive() )
		{
			// Make sure we're connected to existing OC nodes in the world.
			Network.joinOrCreateNetwork( this.getTile() );

			if ( this.output )
			{
				if ( this.getInput() != null && this.node != null )
				{
					Network.joinOrCreateNetwork( this.getInput().getTile() );
					this.node.connect( this.getInput().node() );
				}
			}
			else
			{
				try
				{
					for ( PartP2POpenComputers output : this.getOutputs() )
					{
						if ( this.node != null )
						{
							Network.joinOrCreateNetwork( output.getTile() );
							this.node.connect( output.node() );
						}
					}
				}
				catch ( GridAccessException e )
				{
					AELog.error( e );
				}
			}
		}
		else if ( this.node != null )
		{
			this.node.remove();
		}
	}

	@Nullable
	@Override
	public Node node()
	{
		return this.node;
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

	@Nullable
	@Override
	public Node sidedNode(ForgeDirection side)
	{
		return side == this.side ? this.node : null;
	}

	@Override
	public boolean canConnect(ForgeDirection side)
	{
		return side == this.side;
	}

	private final class UpdateCallback implements Callable<Void>
	{
		@Nullable
		@Override
		public Void call() throws Exception
		{
			PartP2POpenComputers.this.updateConnections();

			return null;
		}
	}
}
