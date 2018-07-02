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


import java.util.Collections;
import java.util.Map;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.parts.PartSharedFluidBus;
import appeng.util.Platform;


/**
 * @author BrockWS
 * @version rv5 - 1/05/2018
 * @since rv5 1/05/2018
 */
public class ContainerFluidIO extends ContainerUpgradeable implements IFluidSyncContainer
{
	private final PartSharedFluidBus bus;
	private final FluidSyncHelper configSync;

	public ContainerFluidIO( InventoryPlayer ip, PartSharedFluidBus te )
	{
		super( ip, te );
		this.bus = te;
		this.configSync = new FluidSyncHelper( this.bus.getConfig(), 0 );
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
			this.configSync.sendDiff( this.listeners );
		}

		super.detectAndSendChanges();
	}

	@Override
	public void addListener( IContainerListener listener )
	{
		super.addListener( listener );
		this.configSync.sendFull( Collections.singleton( listener ) );
	}

	@Override
	public void receiveFluidSlots( Map<Integer, IAEFluidStack> fluids )
	{
		this.configSync.readPacket( fluids );
	}
}
