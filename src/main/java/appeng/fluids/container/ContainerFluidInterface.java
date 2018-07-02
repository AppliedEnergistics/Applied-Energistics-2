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
import net.minecraft.tileentity.TileEntity;

import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.AEBaseContainer;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.util.Platform;


public class ContainerFluidInterface extends AEBaseContainer implements IFluidSyncContainer
{
	private final DualityFluidInterface myDuality;
	private final FluidSyncHelper tankSync;
	private final FluidSyncHelper configSync;

	public ContainerFluidInterface( final InventoryPlayer ip, final IFluidInterfaceHost te )
	{
		super( ip, (TileEntity) ( te instanceof TileEntity ? te : null ), (IPart) ( te instanceof IPart ? te : null ) );

		this.myDuality = te.getDualityFluidInterface();
		this.tankSync = new FluidSyncHelper( this.myDuality.getTanks(), 0 );
		this.configSync = new FluidSyncHelper( this.myDuality.getConfig(), DualityFluidInterface.NUMBER_OF_TANKS );

		this.bindPlayerInventory( ip, 0, 231 - /* height of player inventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );

		if( Platform.isServer() )
		{
			this.configSync.sendDiff( this.listeners );
			this.tankSync.sendDiff( this.listeners );
		}

		super.detectAndSendChanges();
	}

	@Override
	public void addListener( IContainerListener listener )
	{
		super.addListener( listener );
		this.configSync.sendFull( Collections.singleton( listener ) );
		this.tankSync.sendFull( Collections.singleton( listener ) );
	}

	@Override
	public void receiveFluidSlots( Map<Integer, IAEFluidStack> fluids )
	{
		this.configSync.readPacket( fluids );
		this.tankSync.readPacket( fluids );
	}
}
