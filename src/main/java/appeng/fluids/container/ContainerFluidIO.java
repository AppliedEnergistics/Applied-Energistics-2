/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.container;


import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketFluidSlot;
import appeng.fluids.parts.PartSharedFluidBus;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;


/**
 * @author BrockWS
 * @version rv5 - 1/05/2018
 * @since rv5 1/05/2018
 */
public class ContainerFluidIO extends ContainerUpgradeable implements IFluidSyncContainer
{
	private final PartSharedFluidBus bus;

	public ContainerFluidIO( InventoryPlayer ip, PartSharedFluidBus te )
	{
		super( ip, te );
		this.bus = te;
	}

	@Override
	protected void setupConfig()
	{
		this.setupUpgrades();
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );

		if( Platform.isServer() )
		{
			this.sendFluidSlots( this.bus.getConfig() );
		}

		super.detectAndSendChanges();
	}

	@Override
	public void addListener( IContainerListener listener )
	{
		super.addListener( listener );
		this.sendFluidSlots( this.bus.getConfig() );
	}

	private void sendFluidSlots( IAEFluidTank fluids )
	{
		final Map<Integer, IAEFluidStack> sendMap = new HashMap<>();
		for( int i = 0; i < fluids.getSlots(); ++i )
		{
			sendMap.put( i, fluids.getFluidInSlot( i ) );
		}
		for( final IContainerListener listener : this.listeners )
		{
			if( listener instanceof EntityPlayerMP )
			{
				NetworkHandler.instance().sendTo( new PacketFluidSlot( sendMap ), (EntityPlayerMP) listener );
			}
		}
	}

	@Override
	public void receiveFluidSlots( Map<Integer, IAEFluidStack> fluids )
	{
		for( final Map.Entry<Integer, IAEFluidStack> entry : fluids.entrySet() )
		{
			( (PartSharedFluidBus) this.getUpgradeable() ).getConfig().setFluidInSlot( entry.getKey(), entry.getValue() );
		}
	}
}
