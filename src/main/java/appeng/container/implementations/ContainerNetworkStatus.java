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
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class ContainerNetworkStatus extends AEBaseContainer
{

	@GuiSync( 0 )
	public long avgAddition;
	@GuiSync( 1 )
	public long powerUsage;
	@GuiSync( 2 )
	public long currentPower;
	@GuiSync( 3 )
	public long maxPower;
	IGrid network;
	int delay = 40;

	public ContainerNetworkStatus( InventoryPlayer ip, INetworkTool te )
	{
		super( ip, null, null );
		IGridHost host = te.getGridHost();

		if( host != null )
		{
			this.findNode( host, ForgeDirection.UNKNOWN );
			for( ForgeDirection d : ForgeDirection.VALID_DIRECTIONS )
			{
				this.findNode( host, d );
			}
		}

		if( this.network == null && Platform.isServer() )
		{
			this.isContainerValid = false;
		}
	}

	private void findNode( IGridHost host, ForgeDirection d )
	{
		if( this.network == null )
		{
			IGridNode node = host.getGridNode( d );
			if( node != null )
			{
				this.network = node.getGrid();
			}
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		this.delay++;
		if( Platform.isServer() && this.delay > 15 && this.network != null )
		{
			this.delay = 0;

			IEnergyGrid eg = this.network.getCache( IEnergyGrid.class );
			if( eg != null )
			{
				this.avgAddition = (long) ( 100.0 * eg.getAvgPowerInjection() );
				this.powerUsage = (long) ( 100.0 * eg.getAvgPowerUsage() );
				this.currentPower = (long) ( 100.0 * eg.getStoredPower() );
				this.maxPower = (long) ( 100.0 * eg.getMaxStoredPower() );
			}

			try
			{
				PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();

				for( Class<? extends IGridHost> machineClass : this.network.getMachinesClasses() )
				{
					IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
					for( IGridNode machine : this.network.getMachines( machineClass ) )
					{
						IGridBlock blk = machine.getGridBlock();
						ItemStack is = blk.getMachineRepresentation();
						if( is != null && is.getItem() != null )
						{
							IAEItemStack ais = AEItemStack.create( is );
							ais.setStackSize( 1 );
							ais.setCountRequestable( (long) ( blk.getIdlePowerUsage() * 100.0 ) );
							list.add( ais );
						}
					}

					for( IAEItemStack ais : list )
					{
						piu.appendItem( ais );
					}
				}

				for( Object c : this.crafters )
				{
					if( c instanceof EntityPlayer )
					{
						NetworkHandler.instance.sendTo( piu, (EntityPlayerMP) c );
					}
				}
			}
			catch( IOException e )
			{
				// :P
			}
		}
		super.detectAndSendChanges();
	}
}
