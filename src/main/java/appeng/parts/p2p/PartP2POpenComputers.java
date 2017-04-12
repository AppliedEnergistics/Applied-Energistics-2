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


import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.transformer.annotations.Integration.Interface;
import appeng.transformer.annotations.Integration.InterfaceList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.oc.api.API;
import li.cil.oc.api.Items;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nullable;


@InterfaceList( value = { @Interface( iface = "li.cil.oc.api.network.Environment", iname = IntegrationType.OpenComputers ), @Interface( iface = "li.cil.oc.api.network.SidedEnvironment", iname = IntegrationType.OpenComputers ) } )
public final class PartP2POpenComputers extends PartP2PTunnel<PartP2POpenComputers> implements Environment, SidedEnvironment
{
	@Nullable
	private final Node node;

	public PartP2POpenComputers( final ItemStack is )
	{
		super( is );

		if( !IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.OpenComputers ) )
		{
			throw new RuntimeException( "OpenComputers is not installed!" );
		}

		// Avoid NPE when called in pre-init phase (part population).
		if( API.network != null )
		{
			this.node = Network.newNode( this, Visibility.None ).create();
		}
		else
		{
			this.node = null; // to satisfy final
		}
	}

	@MENetworkEventSubscribe
	public void changeStateA( final MENetworkBootingStatusChange bs )
	{
		this.updateConnections();
	}

	@MENetworkEventSubscribe
	public void changeStateB( final MENetworkChannelsChanged bs )
	{
		this.updateConnections();
	}

	@MENetworkEventSubscribe
	public void changeStateC( final MENetworkPowerStatusChange bs )
	{
		this.updateConnections();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getTypeTexture()
	{
		return Items.get( "adapter" ).block().getBlockTextureFromSide( 2 );
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		if( this.node != null )
		{
			this.node.remove();
		}
	}

	@Override
	public void onTunnelNetworkChange()
	{
		this.updateConnections();
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		if( this.node != null )
		{
			this.node.load( data );
		}
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		if( this.node != null )
		{
			this.node.save( data );
		}
	}

	private void updateConnections()
	{
		if( this.getProxy().isPowered() && this.getProxy().isActive() )
		{
			// Make sure we're connected to existing OC nodes in the world.
			Network.joinOrCreateNetwork( this.getTile() );

			if( this.isOutput() && this.getInput() != null && this.node != null )
			{
				Network.joinOrCreateNetwork( this.getInput().getTile() );
				this.node.connect( this.getInput().node() );
			}
		}
		else if( this.node != null )
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

	@Nullable
	@Override
	public Node sidedNode( final ForgeDirection side )
	{
		return side == this.getSide() ? this.node : null;
	}

	@Override
	public boolean canConnect( final ForgeDirection side )
	{
		return side == this.getSide();
	}
}
