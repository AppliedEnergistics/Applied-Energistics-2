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

package appeng.container.implementations;


import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.ICustomNameObject;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;


public class ContainerCraftingCPU extends AEBaseContainer implements IMEMonitorHandlerReceiver<IAEItemStack>, ICustomNameObject
{

	final IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
	protected IGrid network;
	CraftingCPUCluster monitor = null;
	String cpuName = null;
	int delay = 40;

	@GuiSync( 0 )
	public long eta = -1;

	public ContainerCraftingCPU( final InventoryPlayer ip, final Object te )
	{
		super( ip, te );
		final IGridHost host = (IGridHost) ( te instanceof IGridHost ? te : null );

		if( host != null )
		{
			this.findNode( host, AEPartLocation.INTERNAL );
			for( final AEPartLocation d : AEPartLocation.SIDE_LOCATIONS )
			{
				this.findNode( host, d );
			}
		}

		if( te instanceof TileCraftingTile )
		{
			this.setCPU( (ICraftingCPU) ( (IAEMultiBlock) te ).getCluster() );
		}

		if( this.network == null && Platform.isServer() )
		{
			this.isContainerValid = false;
		}
	}

	private void findNode( final IGridHost host, final AEPartLocation d )
	{
		if( this.network == null )
		{
			final IGridNode node = host.getGridNode( d );
			if( node != null )
			{
				this.network = node.getGrid();
			}
		}
	}

	protected void setCPU( final ICraftingCPU c )
	{
		if( c == this.monitor )
		{
			return;
		}

		if( this.monitor != null )
		{
			this.monitor.removeListener( this );
		}

		for( final Object g : this.crafters )
		{
			if( g instanceof EntityPlayer )
			{
				try
				{
					NetworkHandler.instance.sendTo( new PacketValueConfig( "CraftingStatus", "Clear" ), (EntityPlayerMP) g );
				}
				catch( final IOException e )
				{
					AELog.error( e );
				}
			}
		}

		if( c instanceof CraftingCPUCluster )
		{
			this.cpuName = c.getName();
			this.monitor = (CraftingCPUCluster) c;
			this.list.resetStatus();
			this.monitor.getListOfItem( this.list, CraftingItemList.ALL );
			this.monitor.addListener( this, null );
			this.eta = 0;
		}
		else
		{
			this.monitor = null;
			this.cpuName = "";
			this.eta = -1;
		}
	}

	public void cancelCrafting()
	{
		if( this.monitor != null )
		{
			this.monitor.cancel();
		}
		this.eta = -1;
	}

	@Override
	public void removeCraftingFromCrafters( final ICrafting c )
	{
		super.removeCraftingFromCrafters( c );

		if( this.crafters.isEmpty() && this.monitor != null )
		{
			this.monitor.removeListener( this );
		}
	}

	@Override
	public void onContainerClosed( final EntityPlayer player )
	{
		super.onContainerClosed( player );
		if( this.monitor != null )
		{
			this.monitor.removeListener( this );
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		if( Platform.isServer() && this.monitor != null && !this.list.isEmpty() )
		{
			try
			{
				if( this.eta >= 0 )
				{
					final long elapsedTime = this.monitor.getElapsedTime();
					final double remainingItems = this.monitor.getRemainingItemCount();
					final double startItems = this.monitor.getStartItemCount();
					final long eta = (long) ( elapsedTime / Math.max( 1d, ( startItems - remainingItems ) ) * remainingItems );
					this.eta = eta;
				}

				final PacketMEInventoryUpdate a = new PacketMEInventoryUpdate( (byte) 0 );
				final PacketMEInventoryUpdate b = new PacketMEInventoryUpdate( (byte) 1 );
				final PacketMEInventoryUpdate c = new PacketMEInventoryUpdate( (byte) 2 );

				for( final IAEItemStack out : this.list )
				{
					a.appendItem( this.monitor.getItemStack( out, CraftingItemList.STORAGE ) );
					b.appendItem( this.monitor.getItemStack( out, CraftingItemList.ACTIVE ) );
					c.appendItem( this.monitor.getItemStack( out, CraftingItemList.PENDING ) );
				}

				this.list.resetStatus();

				for( final Object g : this.crafters )
				{
					if( g instanceof EntityPlayer )
					{
						if( !a.isEmpty() )
						{
							NetworkHandler.instance.sendTo( a, (EntityPlayerMP) g );
						}

						if( !b.isEmpty() )
						{
							NetworkHandler.instance.sendTo( b, (EntityPlayerMP) g );
						}

						if( !c.isEmpty() )
						{
							NetworkHandler.instance.sendTo( c, (EntityPlayerMP) g );
						}
					}
				}
			}
			catch( final IOException e )
			{
				// :P
			}
		}
		super.detectAndSendChanges();
	}

	@Override
	public boolean isValid( final Object verificationToken )
	{
		return true;
	}

	@Override
	public void postChange( final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change, final BaseActionSource actionSource )
	{
		for( IAEItemStack is : change )
		{
			is = is.copy();
			is.setStackSize( 1 );
			this.list.add( is );
		}
	}

	@Override
	public void onListUpdate()
	{

	}

	@Override
	public String getCustomName()
	{
		return this.cpuName;
	}

	@Override
	public boolean hasCustomName()
	{
		return this.cpuName != null && this.cpuName.length() > 0;
	}
}
