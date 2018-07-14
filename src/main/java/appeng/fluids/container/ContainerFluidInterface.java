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
import appeng.api.util.IConfigManager;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;


public class ContainerFluidInterface extends ContainerFluidConfigurable
{
	private final DualityFluidInterface myDuality;
	private final FluidSyncHelper tankSync;

	public ContainerFluidInterface( final InventoryPlayer ip, final IFluidInterfaceHost te )
	{
		super( ip, te.getDualityFluidInterface().getHost() );

		this.myDuality = te.getDualityFluidInterface();
		this.tankSync = new FluidSyncHelper( this.myDuality.getTanks(), DualityFluidInterface.NUMBER_OF_TANKS );
	}

	@Override
	protected int getHeight()
	{
		return 231;
	}

	@Override
	public IAEFluidTank getFluidConfigInventory()
	{
		return this.myDuality.getConfig();
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );

		if( Platform.isServer() )
		{
			this.tankSync.sendDiff( this.listeners );
		}

		super.detectAndSendChanges();
	}

	@Override
	protected void setupConfig()
	{
	}

	@Override
	protected void loadSettingsFromHost( final IConfigManager cm )
	{
	}

	@Override
	public void addListener( IContainerListener listener )
	{
		super.addListener( listener );
		this.tankSync.sendFull( Collections.singleton( listener ) );
	}

	@Override
	public void receiveFluidSlots( Map<Integer, IAEFluidStack> fluids )
	{
		super.receiveFluidSlots( fluids );
		this.tankSync.readPacket( fluids );
	}

	protected boolean supportCapacity()
	{
		return false;
	}

	public int availableUpgrades()
	{
		return 0;
	}

	@Override
	public boolean hasToolbox()
	{
		return false;
	}
}
