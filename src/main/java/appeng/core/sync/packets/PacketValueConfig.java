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

package appeng.core.sync.packets;


import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;


public class PacketValueConfig extends AppEngPacket
{

	private final String Name;
	private final String Value;

	public PacketValueConfig( final PacketBuffer stream )
	{
		this.Name = stream.readString();
		this.Value = stream.readString();
		// dis.close();
	}

	// api
	public PacketValueConfig( final String name, final String value ) throws IOException
	{
		this.Name = name;
		this.Value = value;

		final PacketBuffer data = new PacketBuffer( Unpooled.buffer() );

		data.writeInt( this.getPacketID() );

		data.writeString( name );
		data.writeString( value );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final PlayerEntity player )
	{
		final Container c = player.openContainer;

// FIXME		if( this.Name.equals( "Item" ) && ( ( !player.getHeldItem( Hand.MAIN_HAND ).isEmpty() && player.getHeldItem( Hand.MAIN_HAND ).getItem() instanceof IMouseWheelItem ) || ( !player.getHeldItem( Hand.OFF_HAND ).isEmpty() && player.getHeldItem( Hand.OFF_HAND ).getItem() instanceof IMouseWheelItem ) ) )
// FIXME		{
// FIXME			final Hand hand;
// FIXME			if( !player.getHeldItem( Hand.MAIN_HAND ).isEmpty() && player.getHeldItem( Hand.MAIN_HAND ).getItem() instanceof IMouseWheelItem )
// FIXME			{
// FIXME				hand = Hand.MAIN_HAND;
// FIXME			}
// FIXME			else if( !player.getHeldItem( Hand.OFF_HAND ).isEmpty() && player.getHeldItem( Hand.OFF_HAND ).getItem() instanceof IMouseWheelItem )
// FIXME			{
// FIXME				hand = Hand.OFF_HAND;
// FIXME			}
// FIXME			else
// FIXME			{
// FIXME				return;
// FIXME			}
// FIXME
// FIXME			final ItemStack is = player.getHeldItem( hand );
// FIXME			final IMouseWheelItem si = (IMouseWheelItem) is.getItem();
// FIXME			si.onWheel( is, this.Value.equals( "WheelUp" ) );
// FIXME		}
// FIXME		else if( this.Name.equals( "Terminal.Cpu" ) && c instanceof ContainerCraftingStatus )
// FIXME		{
// FIXME			final ContainerCraftingStatus qk = (ContainerCraftingStatus) c;
// FIXME			qk.cycleCpu( this.Value.equals( "Next" ) );
// FIXME		}
// FIXME		else if( this.Name.equals( "Terminal.Cpu" ) && c instanceof ContainerCraftConfirm )
// FIXME		{
// FIXME			final ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
// FIXME			qk.cycleCpu( this.Value.equals( "Next" ) );
// FIXME		}
// FIXME		else if( this.Name.equals( "Terminal.Start" ) && c instanceof ContainerCraftConfirm )
// FIXME		{
// FIXME			final ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
// FIXME			qk.startJob();
// FIXME		}
// FIXME		else if( this.Name.equals( "TileCrafting.Cancel" ) && c instanceof ContainerCraftingCPU )
// FIXME		{
// FIXME			final ContainerCraftingCPU qk = (ContainerCraftingCPU) c;
// FIXME			qk.cancelCrafting();
// FIXME		}
// FIXME		else if( this.Name.equals( "QuartzKnife.Name" ) && c instanceof ContainerQuartzKnife )
// FIXME		{
// FIXME			final ContainerQuartzKnife qk = (ContainerQuartzKnife) c;
// FIXME			qk.setName( this.Value );
// FIXME		}
// FIXME		else if( this.Name.equals( "TileSecurityStation.ToggleOption" ) && c instanceof ContainerSecurityStation )
// FIXME		{
// FIXME			final ContainerSecurityStation sc = (ContainerSecurityStation) c;
// FIXME			sc.toggleSetting( this.Value, player );
// FIXME		}
// FIXME		else if( this.Name.equals( "PriorityHost.Priority" ) && c instanceof ContainerPriority )
// FIXME		{
// FIXME			final ContainerPriority pc = (ContainerPriority) c;
// FIXME			pc.setPriority( Integer.parseInt( this.Value ), player );
// FIXME		}
// FIXME		else if( this.Name.equals( "LevelEmitter.Value" ) && c instanceof ContainerLevelEmitter )
// FIXME		{
// FIXME			final ContainerLevelEmitter lvc = (ContainerLevelEmitter) c;
// FIXME			lvc.setLevel( Long.parseLong( this.Value ), player );
// FIXME		}
// FIXME		else if( this.Name.equals( "FluidLevelEmitter.Value" ) && c instanceof ContainerFluidLevelEmitter )
// FIXME		{
// FIXME			final ContainerFluidLevelEmitter lvc = (ContainerFluidLevelEmitter) c;
// FIXME			lvc.setLevel( Long.parseLong( this.Value ), player );
// FIXME		}
// FIXME		else if( this.Name.startsWith( "PatternTerminal." ) && c instanceof ContainerPatternTerm )
// FIXME		{
// FIXME			final ContainerPatternTerm cpt = (ContainerPatternTerm) c;
// FIXME			if( this.Name.equals( "PatternTerminal.CraftMode" ) )
// FIXME			{
// FIXME				cpt.getPatternTerminal().setCraftingRecipe( this.Value.equals( "1" ) );
// FIXME			}
// FIXME			else if( this.Name.equals( "PatternTerminal.Encode" ) )
// FIXME			{
// FIXME				cpt.encode();
// FIXME			}
// FIXME			else if( this.Name.equals( "PatternTerminal.Clear" ) )
// FIXME			{
// FIXME				cpt.clear();
// FIXME			}
// FIXME			else if( this.Name.equals( "PatternTerminal.Substitute" ) )
// FIXME			{
// FIXME				cpt.getPatternTerminal().setSubstitution( this.Value.equals( "1" ) );
// FIXME			}
// FIXME		}
// FIXME		else if( this.Name.startsWith( "StorageBus." ) )
// FIXME		{
// FIXME			if( this.Name.equals( "StorageBus.Action" ) )
// FIXME			{
// FIXME				if( this.Value.equals( "Partition" ) )
// FIXME				{
// FIXME					if( c instanceof ContainerStorageBus )
// FIXME					{
// FIXME						( (ContainerStorageBus) c ).partition();
// FIXME					}
// FIXME					else if( c instanceof ContainerFluidStorageBus )
// FIXME					{
// FIXME						( (ContainerFluidStorageBus) c ).partition();
// FIXME					}
// FIXME				}
// FIXME				else if( this.Value.equals( "Clear" ) )
// FIXME				{
// FIXME					if( c instanceof ContainerStorageBus )
// FIXME					{
// FIXME						( (ContainerStorageBus) c ).clear();
// FIXME					}
// FIXME					else if( c instanceof ContainerFluidStorageBus )
// FIXME					{
// FIXME						( (ContainerFluidStorageBus) c ).clear();
// FIXME					}
// FIXME				}
// FIXME			}
// FIXME		}
// FIXME		else if( this.Name.startsWith( "CellWorkbench." ) && c instanceof ContainerCellWorkbench )
// FIXME		{
// FIXME			final ContainerCellWorkbench ccw = (ContainerCellWorkbench) c;
// FIXME			if( this.Name.equals( "CellWorkbench.Action" ) )
// FIXME			{
// FIXME				if( this.Value.equals( "CopyMode" ) )
// FIXME				{
// FIXME					ccw.nextWorkBenchCopyMode();
// FIXME				}
// FIXME				else if( this.Value.equals( "Partition" ) )
// FIXME				{
// FIXME					ccw.partition();
// FIXME				}
// FIXME				else if( this.Value.equals( "Clear" ) )
// FIXME				{
// FIXME					ccw.clear();
// FIXME				}
// FIXME			}
// FIXME			else if( this.Name.equals( "CellWorkbench.Fuzzy" ) )
// FIXME			{
// FIXME				ccw.setFuzzy( FuzzyMode.valueOf( this.Value ) );
// FIXME			}
// FIXME		}
// FIXME		else if( c instanceof ContainerNetworkTool )
// FIXME		{
// FIXME			if( this.Name.equals( "NetworkTool" ) && this.Value.equals( "Toggle" ) )
// FIXME			{
// FIXME				( (ContainerNetworkTool) c ).toggleFacadeMode();
// FIXME			}
// FIXME		}
// FIXME		else if( c instanceof IConfigurableObject )
// FIXME		{
// FIXME			final IConfigManager cm = ( (IConfigurableObject) c ).getConfigManager();
// FIXME
// FIXME			for( final Settings e : cm.getSettings() )
// FIXME			{
// FIXME				if( e.name().equals( this.Name ) )
// FIXME				{
// FIXME					final Enum<?> def = cm.getSetting( e );
// FIXME
// FIXME					try
// FIXME					{
// FIXME						cm.putSetting( e, Enum.valueOf( def.getClass(), this.Value ) );
// FIXME					}
// FIXME					catch( final IllegalArgumentException err )
// FIXME					{
// FIXME						// :P
// FIXME					}
// FIXME
// FIXME					break;
// FIXME				}
// FIXME			}
// FIXME		}
	}

	@Override
	public void clientPacketData( final INetworkInfo network, final PlayerEntity player )
	{
		final Container c = player.openContainer;

// FIXME		if( this.Name.equals( "CustomName" ) && c instanceof AEBaseContainer )
// FIXME		{
// FIXME			( (AEBaseContainer) c ).setCustomName( this.Value );
// FIXME		}
// FIXME		else if( this.Name.startsWith( "SyncDat." ) )
// FIXME		{
// FIXME			( (AEBaseContainer) c ).stringSync( Integer.parseInt( this.Name.substring( 8 ) ), this.Value );
// FIXME		}
// FIXME		else if( this.Name.equals( "CraftingStatus" ) && this.Value.equals( "Clear" ) )
// FIXME		{
// FIXME			final Screen gs = Minecraft.getInstance().currentScreen;
// FIXME			if( gs instanceof GuiCraftingCPU )
// FIXME			{
// FIXME				( (GuiCraftingCPU) gs ).clearItems();
// FIXME			}
// FIXME		}
// FIXME		else if( c instanceof IConfigurableObject )
// FIXME		{
// FIXME			final IConfigManager cm = ( (IConfigurableObject) c ).getConfigManager();
// FIXME
// FIXME			for( final Settings e : cm.getSettings() )
// FIXME			{
// FIXME				if( e.name().equals( this.Name ) )
// FIXME				{
// FIXME					final Enum<?> def = cm.getSetting( e );
// FIXME
// FIXME					try
// FIXME					{
// FIXME						cm.putSetting( e, Enum.valueOf( def.getClass(), this.Value ) );
// FIXME					}
// FIXME					catch( final IllegalArgumentException err )
// FIXME					{
// FIXME						// :P
// FIXME					}
// FIXME
// FIXME					break;
// FIXME				}
// FIXME			}
// FIXME		}
	}
}
