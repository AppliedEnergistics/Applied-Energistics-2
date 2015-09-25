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
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Future;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.core.AELog;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;
import appeng.util.Platform;

import com.google.common.collect.ImmutableSet;


public class ContainerCraftConfirm extends AEBaseContainer
{

	public final ArrayList<CraftingCPURecord> cpus = new ArrayList<CraftingCPURecord>();
	final ITerminalHost priHost;
	public Future<ICraftingJob> job;
	public ICraftingJob result;
	@GuiSync( 0 )
	public long bytesUsed;
	@GuiSync( 1 )
	public long cpuBytesAvail;
	@GuiSync( 2 )
	public int cpuCoProcessors;
	@GuiSync( 3 )
	public boolean autoStart = false;
	@GuiSync( 4 )
	public boolean simulation = true;
	@GuiSync( 5 )
	public int selectedCpu = -1;
	@GuiSync( 6 )
	public boolean noCPU = true;
	@GuiSync( 7 )
	public String myName = "";
	protected long cpuIdx = Long.MIN_VALUE;

	public ContainerCraftConfirm( InventoryPlayer ip, ITerminalHost te )
	{
		super( ip, te );
		this.priHost = te;
	}

	public void cycleCpu( boolean next )
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

		if( this.selectedCpu == -1 )
		{
			this.cpuBytesAvail = 0;
			this.cpuCoProcessors = 0;
			this.myName = "";
		}
		else
		{
			this.myName = this.cpus.get( this.selectedCpu ).myName;
			this.cpuBytesAvail = this.cpus.get( this.selectedCpu ).size;
			this.cpuCoProcessors = this.cpus.get( this.selectedCpu ).processors;
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		if( Platform.isClient() )
		{
			return;
		}

		ICraftingGrid cc = this.getGrid().getCache( ICraftingGrid.class );
		ImmutableSet<ICraftingCPU> cpuSet = cc.getCpus();

		int matches = 0;
		boolean changed = false;
		for( ICraftingCPU c : cpuSet )
		{
			boolean found = false;
			for( CraftingCPURecord ccr : this.cpus )
			{
				if( ccr.cpu == c )
				{
					found = true;
				}
			}

			boolean matched = this.cpuMatches( c );

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
			for( ICraftingCPU c : cpuSet )
			{
				if( this.cpuMatches( c ) )
				{
					this.cpus.add( new CraftingCPURecord( c.getAvailableStorage(), c.getCoProcessors(), c ) );
				}
			}

			this.sendCPUs();
		}

		this.noCPU = this.cpus.isEmpty();

		super.detectAndSendChanges();

		if( this.job != null && this.job.isDone() )
		{
			try
			{
				this.result = this.job.get();

				if( !this.result.isSimulation() )
				{
					this.simulation = false;
					if( this.autoStart )
					{
						this.startJob();
						return;
					}
				}
				else
				{
					this.simulation = true;
				}

				try
				{
					PacketMEInventoryUpdate a = new PacketMEInventoryUpdate( (byte) 0 );
					PacketMEInventoryUpdate b = new PacketMEInventoryUpdate( (byte) 1 );
					PacketMEInventoryUpdate c = this.result.isSimulation() ? new PacketMEInventoryUpdate( (byte) 2 ) : null;

					IItemList<IAEItemStack> plan = AEApi.instance().storage().createItemList();
					this.result.populatePlan( plan );

					this.bytesUsed = this.result.getByteTotal();

					for( IAEItemStack out : plan )
					{
						IAEItemStack m = null;

						IAEItemStack o = out.copy();
						o.reset();
						o.setStackSize( out.getStackSize() );

						IAEItemStack p = out.copy();
						p.reset();
						p.setStackSize( out.getCountRequestable() );

						IStorageGrid sg = this.getGrid().getCache( IStorageGrid.class );
						IMEInventory<IAEItemStack> items = sg.getItemInventory();

						if( c != null && this.result.isSimulation() )
						{
							m = o.copy();
							o = items.extractItems( o, Actionable.SIMULATE, this.mySrc );

							if( o == null )
							{
								o = m.copy();
								o.setStackSize( 0 );
							}

							m.setStackSize( m.getStackSize() - o.getStackSize() );
						}

						if( o.getStackSize() > 0 )
						{
							a.appendItem( o );
						}

						if( p.getStackSize() > 0 )
						{
							b.appendItem( p );
						}

						if( c != null && m != null && m.getStackSize() > 0 )
						{
							c.appendItem( m );
						}
					}

					for( Object g : this.crafters )
					{
						if( g instanceof EntityPlayer )
						{
							NetworkHandler.instance.sendTo( a, (EntityPlayerMP) g );
							NetworkHandler.instance.sendTo( b, (EntityPlayerMP) g );
							if( c != null )
							{
								NetworkHandler.instance.sendTo( c, (EntityPlayerMP) g );
							}
						}
					}
				}
				catch( IOException e )
				{
					// :P
				}
			}
			catch( Throwable e )
			{
				this.getPlayerInv().player.addChatMessage( new ChatComponentText( "Error: " + e.toString() ) );
				AELog.error( e );
				this.isContainerValid = false;
				this.result = null;
			}

			this.job = null;
		}
		this.verifyPermissions( SecurityPermissions.CRAFT, false );
	}

	public IGrid getGrid()
	{
		IActionHost h = ( (IActionHost) this.getTarget() );
		return h.getActionableNode().getGrid();
	}

	private boolean cpuMatches( ICraftingCPU c )
	{
		return c.getAvailableStorage() >= this.bytesUsed && !c.isBusy();
	}

	private void sendCPUs()
	{
		Collections.sort( this.cpus );

		if( this.selectedCpu >= this.cpus.size() )
		{
			this.selectedCpu = -1;
			this.cpuBytesAvail = 0;
			this.cpuCoProcessors = 0;
			this.myName = "";
		}
		else if( this.selectedCpu != -1 )
		{
			this.myName = this.cpus.get( this.selectedCpu ).myName;
			this.cpuBytesAvail = this.cpus.get( this.selectedCpu ).size;
			this.cpuCoProcessors = this.cpus.get( this.selectedCpu ).processors;
		}
	}

	public void startJob()
	{
		GuiBridge originalGui = null;

		IActionHost ah = this.getActionHost();
		if( ah instanceof WirelessTerminalGuiObject )
		{
			originalGui = GuiBridge.GUI_WIRELESS_TERM;
		}

		if( ah instanceof PartTerminal )
		{
			originalGui = GuiBridge.GUI_ME;
		}

		if( ah instanceof PartCraftingTerminal )
		{
			originalGui = GuiBridge.GUI_CRAFTING_TERMINAL;
		}

		if( ah instanceof PartPatternTerminal )
		{
			originalGui = GuiBridge.GUI_PATTERN_TERMINAL;
		}

		if( this.result != null && !this.simulation )
		{
			ICraftingGrid cc = this.getGrid().getCache( ICraftingGrid.class );
			ICraftingLink g = cc.submitJob( this.result, null, this.selectedCpu == -1 ? null : this.cpus.get( this.selectedCpu ).cpu, true, this.getActionSrc() );
			this.autoStart = false;
			if( g != null && originalGui != null && this.openContext != null )
			{
				NetworkHandler.instance.sendTo( new PacketSwitchGuis( originalGui ), (EntityPlayerMP) this.invPlayer.player );

				TileEntity te = this.openContext.getTile();
				Platform.openGUI( this.invPlayer.player, te, this.openContext.side, originalGui );
			}
		}
	}

	public BaseActionSource getActionSrc()
	{
		return new PlayerSource( this.getPlayerInv().player, (IActionHost) this.getTarget() );
	}

	@Override
	public void removeCraftingFromCrafters( ICrafting c )
	{
		super.removeCraftingFromCrafters( c );
		if( this.job != null )
		{
			this.job.cancel( true );
			this.job = null;
		}
	}

	@Override
	public void onContainerClosed( EntityPlayer par1EntityPlayer )
	{
		super.onContainerClosed( par1EntityPlayer );
		if( this.job != null )
		{
			this.job.cancel( true );
			this.job = null;
		}
	}

	public World getWorld()
	{
		return this.getPlayerInv().player.worldObj;
	}
}
