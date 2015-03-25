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


import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.entity.player.InventoryPlayer;

import com.google.common.collect.ImmutableSet;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;


public class ContainerCraftingStatus extends ContainerCraftingCPU
{

	public final ArrayList<CraftingCPURecord> cpus = new ArrayList<CraftingCPURecord>();
	@GuiSync( 5 )
	public int selectedCpu = -1;
	@GuiSync( 6 )
	public boolean noCPU = true;
	@GuiSync( 7 )
	public String myName = "";

	public ContainerCraftingStatus( InventoryPlayer ip, ITerminalHost te )
	{
		super( ip, te );
	}

	@Override
	public void detectAndSendChanges()
	{
		ICraftingGrid cc = this.network.getCache( ICraftingGrid.class );
		ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

		int matches = 0;
		boolean changed = false;
		for( ICraftingCPU c : cpuSet )
		{
			boolean found = false;
			for( CraftingCPURecord ccr : this.cpus )
				if( ccr.cpu == c )
					found = true;

			boolean matched = this.cpuMatches( c );

			if( matched )
				matches++;

			if( found == !matched )
				changed = true;
		}

		if( changed || this.cpus.size() != matches )
		{
			this.cpus.clear();
			for( ICraftingCPU c : cpuSet )
			{
				if( this.cpuMatches( c ) )
					this.cpus.add( new CraftingCPURecord( c.getAvailableStorage(), c.getCoProcessors(), c ) );
			}

			this.sendCPUs();
		}

		this.noCPU = this.cpus.size() == 0;

		super.detectAndSendChanges();
	}

	private boolean cpuMatches( ICraftingCPU c )
	{
		return c.isBusy();
	}

	private void sendCPUs()
	{
		Collections.sort( this.cpus );

		if( this.selectedCpu >= this.cpus.size() )
		{
			this.selectedCpu = -1;
			this.myName = "";
		}
		else if( this.selectedCpu != -1 )
		{
			this.myName = this.cpus.get( this.selectedCpu ).myName;
		}

		if( this.selectedCpu == -1 && this.cpus.size() > 0 )
			this.selectedCpu = 0;

		if( this.selectedCpu != -1 )
		{
			if( this.cpus.get( this.selectedCpu ).cpu != this.monitor )
				this.setCPU( this.cpus.get( this.selectedCpu ).cpu );
		}
		else
			this.setCPU( null );
	}

	public void cycleCpu( boolean next )
	{
		if( next )
			this.selectedCpu++;
		else
			this.selectedCpu--;

		if( this.selectedCpu < -1 )
			this.selectedCpu = this.cpus.size() - 1;
		else if( this.selectedCpu >= this.cpus.size() )
			this.selectedCpu = -1;

		if( this.selectedCpu == -1 && this.cpus.size() > 0 )
			this.selectedCpu = 0;

		if( this.selectedCpu == -1 )
		{
			this.myName = "";
			this.setCPU( null );
		}
		else
		{
			this.myName = this.cpus.get( this.selectedCpu ).myName;
			this.setCPU( this.cpus.get( this.selectedCpu ).cpu );
		}
	}
}
