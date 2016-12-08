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


import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.util.Platform;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.InventoryPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ContainerCraftingStatus extends ContainerCraftingCPU
{

	private final List<CraftingCPURecord> cpus = new ArrayList<CraftingCPURecord>();
	@GuiSync( 5 )
	public int selectedCpu = -1;
	@GuiSync( 6 )
	public boolean noCPU = true;
	@GuiSync( 7 )
	public String myName = "";

	public ContainerCraftingStatus( final InventoryPlayer ip, final ITerminalHost te )
	{
		super( ip, te );
	}

	@Override
	public void detectAndSendChanges()
	{
		if( Platform.isServer() && this.getNetwork() != null )
		{
			final ICraftingGrid cc = this.getNetwork().getCache( ICraftingGrid.class );
			final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

			int matches = 0;
			boolean changed = false;
			for( final ICraftingCPU c : cpuSet )
			{
				boolean found = false;
				for( final CraftingCPURecord ccr : this.cpus )
				{
					if( ccr.getCpu() == c )
					{
						found = true;
					}
				}

				final boolean matched = this.cpuMatches( c );

				if( matched )
				{
					matches++;
				}

				if( found == !matched )
				{
					changed = true;
				}
			}

			if( changed || this.cpus.size() != matches )
			{
				this.cpus.clear();
				for( final ICraftingCPU c : cpuSet )
				{
					if( this.cpuMatches( c ) )
					{
						this.cpus.add( new CraftingCPURecord( c.getAvailableStorage(), c.getCoProcessors(), c ) );
					}
				}

				this.sendCPUs();
			}

			this.noCPU = this.cpus.isEmpty();
		}

		super.detectAndSendChanges();
	}

	private boolean cpuMatches( final ICraftingCPU c )
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
			this.myName = this.cpus.get( this.selectedCpu ).getName();
		}

		if( this.selectedCpu == -1 && this.cpus.size() > 0 )
		{
			this.selectedCpu = 0;
		}

		if( this.selectedCpu != -1 )
		{
			if( this.cpus.get( this.selectedCpu ).getCpu() != this.getMonitor() )
			{
				this.setCPU( this.cpus.get( this.selectedCpu ).getCpu() );
			}
		}
		else
		{
			this.setCPU( null );
		}
	}

	public void cycleCpu( final boolean next )
	{
		if( next )
		{
			this.selectedCpu++;
		}
		else
		{
			this.selectedCpu--;
		}

		if( this.selectedCpu < -1 )
		{
			this.selectedCpu = this.cpus.size() - 1;
		}
		else if( this.selectedCpu >= this.cpus.size() )
		{
			this.selectedCpu = -1;
		}

		if( this.selectedCpu == -1 && this.cpus.size() > 0 )
		{
			this.selectedCpu = 0;
		}

		if( this.selectedCpu == -1 )
		{
			this.myName = "";
			this.setCPU( null );
		}
		else
		{
			this.myName = this.cpus.get( this.selectedCpu ).getName();
			this.setCPU( this.cpus.get( this.selectedCpu ).getCpu() );
		}
	}
}
