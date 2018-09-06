/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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


import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import li.cil.oc.api.Items;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import li.cil.oc.api.network.Visibility;

import appeng.api.parts.IPartModel;
import appeng.capabilities.Capabilities;
import appeng.items.parts.PartModels;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.util.Platform;


public final class PartP2POpenComputers extends PartP2PTunnel<PartP2POpenComputers>
{
	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_open_computers" );

	private OCSidedEnvironment OCEnvironment;

	public PartP2POpenComputers( final ItemStack is )
	{
		super( is );
	}

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
	}

	@Override
	public IPartModel getStaticModels()
	{
		return MODELS.getModel( this.isPowered(), this.isActive() );
	}

	@MENetworkEventSubscribe
	public void changeStateA( final MENetworkBootingStatusChange bs )
	{
		this.initEnvironment();
	}

	@MENetworkEventSubscribe
	public void changeStateB( final MENetworkChannelsChanged bs )
	{
		this.initEnvironment();
	}

	@MENetworkEventSubscribe
	public void changeStateC( final MENetworkPowerStatusChange bs )
	{
		this.initEnvironment();
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		this.initEnvironment();
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		if( this.OCEnvironment != null )
		{
			this.OCEnvironment.removeFromWorld();
		}
	}

	@Override
	public void onTunnelNetworkChange()
	{
		this.initEnvironment();
	}

	@Override
	public void onTunnelConfigChange()
	{
		this.initEnvironment();
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.getNode().load( data );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.getNode().save( data );
	}

	private void initEnvironment()
	{
		if( this.OCEnvironment == null )
		{
			this.OCEnvironment = new OCSidedEnvironment( this );
			this.OCEnvironment.updateConnections();
		}
	}

	@Nullable
	public Node getNode()
	{
		if( this.OCEnvironment == null )
		{
			this.initEnvironment();
		}

		return this.OCEnvironment.node();
	}

	@Override
	public boolean hasCapability( @Nonnull Capability<?> capability )
	{
		if( capability == Capabilities.OC_ENVIRONMENT ||
			capability == Capabilities.OC_SIDED_ENVIRONMENT )
		{
			return true;
		}
		return super.hasCapability( capability );
	}

	@Nullable
	public <T> T getCapability( @Nonnull Capability<T> capability )
	{
		if( capability == Capabilities.OC_ENVIRONMENT ||
				capability == Capabilities.OC_SIDED_ENVIRONMENT )
		{
			if( this.OCEnvironment == null )
			{
				this.initEnvironment();
			}

			return (T) this.OCEnvironment;
		}
		return super.getCapability( capability );
	}


	private final class OCSidedEnvironment implements Environment, SidedEnvironment
	{
		@Nullable
		private final Node node;

		private PartP2POpenComputers parent;

		OCSidedEnvironment( PartP2POpenComputers par )
		{
			this.parent = par;

			if( Platform.isServer() )
			{
				this.node = Network.newNode( this, Visibility.None ).create();
			}
			else
			{
				this.node = null;
			}
		}

		public void removeFromWorld()
		{
			if( this.node != null )
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

		@Nullable
		@Override
		public Node sidedNode( final EnumFacing side )
		{
			return side == this.parent.getSide().getFacing() ? this.node : null;
		}

		@Override
		public boolean canConnect( final EnumFacing side )
		{
			return side == this.parent.getSide().getFacing();
		}

		@Override
		public void onConnect( final Node node )
		{
		}

		@Override
		public void onDisconnect( final Node node )
		{
		}

		@Override
		public void onMessage( final Message message )
		{
		}

		public void updateConnections()
		{
			if( this.parent.getProxy().isPowered() && this.parent.getProxy().isActive() )
			{
				// Make sure we're connected to existing OC nodes in the world.
				Network.joinOrCreateNetwork( this.parent.getTile() );

				if( this.parent.isOutput() && this.parent.getInput() != null &&
						this.node != null )
				{
					Network.joinOrCreateNetwork( this.parent.getInput().getTile() );
					this.node.connect( this.parent.getInput().getNode() );
				}
			}
			else if( this.node != null )
			{
				this.node.remove();
			}
		}
	}
}
