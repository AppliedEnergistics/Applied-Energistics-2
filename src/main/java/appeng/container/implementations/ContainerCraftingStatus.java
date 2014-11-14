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
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;

import com.google.common.collect.ImmutableSet;

public class ContainerCraftingStatus extends ContainerCraftingCPU
{

	@GuiSync(5)
	public int selectedCpu = -1;

	@GuiSync(6)
	public boolean noCPU = true;

	@GuiSync(7)
	public String myName = "";

	public final ArrayList<CraftingCPURecord> cpus = new ArrayList<CraftingCPURecord>();

	private void sendCPUs()
	{
		Collections.sort( cpus );

		if ( selectedCpu >= cpus.size() )
		{
			selectedCpu = -1;
			myName = "";
		}
		else if ( selectedCpu != -1 )
		{
			myName = cpus.get( selectedCpu ).myName;
		}

		if ( selectedCpu == -1 && cpus.size() > 0 )
			selectedCpu = 0;

		if ( selectedCpu != -1 )
		{
			if ( cpus.get( selectedCpu ).cpu != monitor )
				setCPU( cpus.get( selectedCpu ).cpu );
		}
		else
			setCPU( null );
	}

	@Override
	public void detectAndSendChanges()
	{
		ICraftingGrid cc = network.getCache( ICraftingGrid.class );
		ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

		int matches = 0;
		boolean changed = false;
		for (ICraftingCPU c : cpuSet)
		{
			boolean found = false;
			for (CraftingCPURecord ccr : cpus)
				if ( ccr.cpu == c )
					found = true;

			boolean matched = cpuMatches( c );

			if ( matched )
				matches++;

			if ( found == !matched )
				changed = true;
		}

		if ( changed || cpus.size() != matches )
		{
			cpus.clear();
			for (ICraftingCPU c : cpuSet)
			{
				if ( cpuMatches( c ) )
					cpus.add( new CraftingCPURecord( c.getAvailableStorage(), c.getCoProcessors(), c ) );
			}

			sendCPUs();
		}

		noCPU = cpus.size() == 0;

		super.detectAndSendChanges();
	}

	private boolean cpuMatches(ICraftingCPU c)
	{
		return c.isBusy();
	}

	public ContainerCraftingStatus(InventoryPlayer ip, ITerminalHost te) {
		super( ip, te );
	}

	public void cycleCpu(boolean next)
	{
		if ( next )
			selectedCpu++;
		else
			selectedCpu--;

		if ( selectedCpu < -1 )
			selectedCpu = cpus.size() - 1;
		else if ( selectedCpu >= cpus.size() )
			selectedCpu = -1;

		if ( selectedCpu == -1 && cpus.size() > 0 )
			selectedCpu = 0;

		if ( selectedCpu == -1 )
		{
			myName = "";
			setCPU( null );
		}
		else
		{
			myName = cpus.get( selectedCpu ).myName;
			setCPU( cpus.get( selectedCpu ).cpu );
		}
	}

}
