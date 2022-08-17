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
import java.util.*;

import appeng.api.networking.IGrid;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCraftingCPUsUpdate;
import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.util.Platform;


public class ContainerCraftingStatus extends ContainerCraftingCPU
{

	private ImmutableSet<ICraftingCPU> lastCpuSet = null;
	private List<CraftingCPUStatus> cpus = new ArrayList<CraftingCPUStatus>();
	private final WeakHashMap<ICraftingCPU, Integer> cpuSerialMap = new WeakHashMap<>();
	private int nextCpuSerial = 1;
	private int lastUpdate = 0;
	@GuiSync(5)
	public int selectedCpuSerial = -1;

	public ContainerCraftingStatus( final InventoryPlayer ip, final ITerminalHost te )
	{
		super( ip, te );
	}

	@Override
	public void detectAndSendChanges()
	{
		IGrid network = this.getNetwork();
		if( Platform.isServer() && network != null )
		{
			final ICraftingGrid cc = network.getCache( ICraftingGrid.class );
			final ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

			/*int matches = 0;
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

			this.noCPU = this.cpus.isEmpty(); */

			// Update at least once a second
			++lastUpdate;
			if (!cpuSet.equals( lastCpuSet ) || lastUpdate > 20) {
				lastUpdate = 0;
				lastCpuSet = cpuSet;
				updateCpuList();
				sendCPUs();
			}
		}


		// Clear selection if CPU is no longer in list
		if (selectedCpuSerial != -1) {
			if (cpus.stream().noneMatch(c -> c.getSerial() == selectedCpuSerial)) {
				selectCPU(-1);
			}
		}

		// Select a suitable CPU if none is selected
		if (selectedCpuSerial == -1) {
			// Try busy CPUs first
			for (CraftingCPUStatus cpu : cpus) {
				if (cpu.getRemainingItems() > 0) {
					selectCPU(cpu.getSerial());
					break;
				}
			}
			// If we couldn't find a busy one, just select the first
			if (selectedCpuSerial == -1 && !cpus.isEmpty()) {
				selectCPU(cpus.get(0).getSerial());
			}
		}

		super.detectAndSendChanges();
	}

	private static final Comparator<CraftingCPUStatus> CPU_COMPARATOR = Comparator
			.comparing((CraftingCPUStatus e) -> e.getName() == null || e.getName().isEmpty())
			.thenComparing(e -> e.getName() != null ? e.getName() : "")
			.thenComparingInt(CraftingCPUStatus::getSerial);

	private void updateCpuList()
	{
		this.cpus.clear();
		for (ICraftingCPU cpu : lastCpuSet)
		{
			int serial = getOrAssignCpuSerial(cpu);
			this.cpus.add( new CraftingCPUStatus( cpu, serial ) );
		}
		this.cpus.sort(CPU_COMPARATOR);
	}

	private int getOrAssignCpuSerial( ICraftingCPU cpu )
	{
		return cpuSerialMap.computeIfAbsent( cpu, unused -> nextCpuSerial++ );
	}

	private boolean cpuMatches( final ICraftingCPU c )
	{
		return c.isBusy();
	}

	private void sendCPUs()
	{
		final PacketCraftingCPUsUpdate update;
		for( final Object player : this.listeners )
		{
			if( player instanceof EntityPlayerMP)
			{
				try
				{
					NetworkHandler.instance.sendTo( new PacketCraftingCPUsUpdate( this.cpus ), (EntityPlayerMP) player );
				}
				catch( IOException e )
				{
					AELog.debug( e );
				}
			}
		}
		/*Collections.sort( this.cpus );

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
		}*/
	}


	public void selectCPU( int serial )
	{
		if (Platform.isServer())
		{
			if( serial < -1 )
			{
				serial = -1;
			}

			final int searchedSerial = serial;
			if( serial > -1 && cpus.stream().noneMatch(c -> c.getSerial() == searchedSerial) )
			{
				serial = -1;
			}

			ICraftingCPU newSelectedCpu = null;
			if( serial != -1 )
			{
				for( ICraftingCPU cpu : lastCpuSet )
				{
					if( cpuSerialMap.getOrDefault( cpu, -1 ) == serial )
					{
						newSelectedCpu = cpu;
						break;
					}
				}
			}

			if( newSelectedCpu != getMonitor() )
			{
				this.selectedCpuSerial = serial;
				setCPU( newSelectedCpu );
			}
		}
	}

	/*public void cycleCpu( final boolean next )
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
	} */

	public List<CraftingCPUStatus> getCPUs()
	{
		return Collections.unmodifiableList( cpus );
	}

	public void postCPUUpdate( CraftingCPUStatus[] cpus )
	{
		this.cpus = Arrays.asList( cpus );
	}
}
