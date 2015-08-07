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


import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.implementations.GuiCraftingCPU;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.implementations.ContainerCraftingCPU;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.container.implementations.ContainerLevelEmitter;
import appeng.container.implementations.ContainerNetworkTool;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.implementations.ContainerPriority;
import appeng.container.implementations.ContainerQuartzKnife;
import appeng.container.implementations.ContainerSecurity;
import appeng.container.implementations.ContainerStorageBus;
import appeng.core.sync.AppEngPacket;
import appeng.helpers.IMouseWheelItem;


public class PacketValueConfig extends AppEngPacket<PacketValueConfig>
{

	private String name;
	private String value;

	// automatic.
	public PacketValueConfig()
	{
	}

	// api
	public PacketValueConfig( final String name, final String value ) throws IOException
	{
		this.name = name;
		this.value = value;
	}

	@Override
	public PacketValueConfig onMessage( PacketValueConfig message, MessageContext ctx )
	{
		if( ctx.side == Side.CLIENT )
		{
			this.clientHandling( message, ctx );
		}
		else
		{
			this.serverHandling( message, ctx );
		}
		return null;
	}

	public void serverHandling( PacketValueConfig message, MessageContext ctx )
	{
		final EntityPlayer player = ctx.getServerHandler().playerEntity;
		final Container c = player.openContainer;

		if( message.name.equals( "Item" ) && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof
				IMouseWheelItem )
		{
			final ItemStack is = player.getHeldItem();
			final IMouseWheelItem si = (IMouseWheelItem) is.getItem();
			si.onWheel( is, message.value.equals( "WheelUp" ) );
		}
		else if( message.name.equals( "Terminal.Cpu" ) && c instanceof ContainerCraftingStatus )
		{
			final ContainerCraftingStatus qk = (ContainerCraftingStatus) c;
			qk.cycleCpu( message.value.equals( "Next" ) );
		}
		else if( message.name.equals( "Terminal.Cpu" ) && c instanceof ContainerCraftConfirm )
		{
			final ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
			qk.cycleCpu( message.value.equals( "Next" ) );
		}
		else if( message.name.equals( "Terminal.Start" ) && c instanceof ContainerCraftConfirm )
		{
			final ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
			qk.startJob();
		}
		else if( message.name.equals( "TileCrafting.Cancel" ) && c instanceof ContainerCraftingCPU )
		{
			final ContainerCraftingCPU qk = (ContainerCraftingCPU) c;
			qk.cancelCrafting();
		}
		else if( message.name.equals( "QuartzKnife.Name" ) && c instanceof ContainerQuartzKnife )
		{
			final ContainerQuartzKnife qk = (ContainerQuartzKnife) c;
			qk.setName( message.value );
		}
		else if( message.name.equals( "TileSecurity.ToggleOption" ) && c instanceof ContainerSecurity )
		{
			final ContainerSecurity sc = (ContainerSecurity) c;
			sc.toggleSetting( message.value, player );
		}
		else if( message.name.equals( "PriorityHost.Priority" ) && c instanceof ContainerPriority )
		{
			final ContainerPriority pc = (ContainerPriority) c;
			pc.setPriority( Integer.parseInt( message.value ), player );
		}
		else if( message.name.equals( "LevelEmitter.Value" ) && c instanceof ContainerLevelEmitter )
		{
			final ContainerLevelEmitter lvc = (ContainerLevelEmitter) c;
			lvc.setLevel( Long.parseLong( message.value ), player );
		}
		else if( message.name.startsWith( "PatternTerminal." ) && c instanceof ContainerPatternTerm )
		{
			final ContainerPatternTerm cpt = (ContainerPatternTerm) c;
			if( message.name.equals( "PatternTerminal.CraftMode" ) )
			{
				cpt.getPatternTerminal().setCraftingRecipe( message.value.equals( "1" ) );
			}
			else if( message.name.equals( "PatternTerminal.Encode" ) )
			{
				cpt.encode();
			}
			else if( message.name.equals( "PatternTerminal.Clear" ) )
			{
				cpt.clear();
			}
			else if( message.name.equals( "PatternTerminal.Substitute" ) )
			{
				cpt.getPatternTerminal().setSubstitution( message.value.equals( "1" ) );
			}
		}
		else if( message.name.startsWith( "StorageBus." ) && c instanceof ContainerStorageBus )
		{
			final ContainerStorageBus ccw = (ContainerStorageBus) c;
			if( message.name.equals( "StorageBus.Action" ) )
			{
				if( message.value.equals( "Partition" ) )
				{
					ccw.partition();
				}
				else if( message.value.equals( "Clear" ) )
				{
					ccw.clear();
				}
			}
		}
		else if( message.name.startsWith( "CellWorkbench." ) && c instanceof ContainerCellWorkbench )
		{
			final ContainerCellWorkbench ccw = (ContainerCellWorkbench) c;
			if( message.name.equals( "CellWorkbench.Action" ) )
			{
				if( message.value.equals( "CopyMode" ) )
				{
					ccw.nextWorkBenchCopyMode();
				}
				else if( message.value.equals( "Partition" ) )
				{
					ccw.partition();
				}
				else if( message.value.equals( "Clear" ) )
				{
					ccw.clear();
				}
			}
			else if( message.name.equals( "CellWorkbench.Fuzzy" ) )
			{
				ccw.setFuzzy( FuzzyMode.valueOf( message.value ) );
			}
		}
		else if( c instanceof ContainerNetworkTool )
		{
			if( message.name.equals( "NetworkTool" ) && message.value.equals( "Toggle" ) )
			{
				( (ContainerNetworkTool) c ).toggleFacadeMode();
			}
		}
		else if( c instanceof IConfigurableObject )
		{
			final IConfigManager cm = ( (IConfigurableObject) c ).getConfigManager();

			for( final Settings e : cm.getSettings() )
			{
				if( e.name().equals( message.name ) )
				{
					final Enum<?> def = cm.getSetting( e );

					try
					{
						cm.putSetting( e, Enum.valueOf( def.getClass(), message.value ) );
					}
					catch( final IllegalArgumentException err )
					{
						// :P
					}

					break;
				}
			}
		}
	}

	public void clientHandling( PacketValueConfig message, MessageContext ctx )
	{
		final Container c = Minecraft.getMinecraft().thePlayer.openContainer;

		if( message.name.equals( "CustomName" ) && c instanceof AEBaseContainer )
		{
			( (AEBaseContainer) c ).setCustomName( message.value );
		}
		else if( message.name.startsWith( "SyncDat." ) )
		{
			( (AEBaseContainer) c ).stringSync( Integer.parseInt( message.name.substring( 8 ) ), message.value );
		}
		else if( message.name.equals( "CraftingStatus" ) && message.value.equals( "Clear" ) )
		{
			final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
			if( gs instanceof GuiCraftingCPU )
			{
				( (GuiCraftingCPU) gs ).clearItems();
			}
		}
		else if( c instanceof IConfigurableObject )
		{
			final IConfigManager cm = ( (IConfigurableObject) c ).getConfigManager();

			for( final Settings e : cm.getSettings() )
			{
				if( e.name().equals( message.name ) )
				{
					final Enum<?> def = cm.getSetting( e );

					try
					{
						cm.putSetting( e, Enum.valueOf( def.getClass(), message.value ) );
					}
					catch( final IllegalArgumentException err )
					{
						// :P
					}

					break;
				}
			}
		}
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.name = ByteBufUtils.readUTF8String( buf );
		this.value = ByteBufUtils.readUTF8String( buf );
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		ByteBufUtils.writeUTF8String( buf, this.name );
		ByteBufUtils.writeUTF8String( buf, this.value );
	}
}
