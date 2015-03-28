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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

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
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IMouseWheelItem;

public class PacketValueConfig extends AppEngPacket
{

	final public String Name;
	final public String Value;

	// automatic.
	public PacketValueConfig(ByteBuf stream) throws IOException {
		DataInputStream dis = new DataInputStream( new ByteArrayInputStream( stream.array(), stream.readerIndex(), stream.readableBytes() ) );
		this.Name = dis.readUTF();
		this.Value = dis.readUTF();
		// dis.close();
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		Container c = player.openContainer;

		if ( this.Name.equals( "Item" ) && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IMouseWheelItem )
		{
			ItemStack is = player.getHeldItem();
			IMouseWheelItem si = (IMouseWheelItem) is.getItem();
			si.onWheel( is, this.Value.equals( "WheelUp" ) );
		}
		else if ( this.Name.equals( "Terminal.Cpu" ) && c instanceof ContainerCraftingStatus )
		{
			ContainerCraftingStatus qk = (ContainerCraftingStatus) c;
			qk.cycleCpu( this.Value.equals( "Next" ) );
		}
		else if ( this.Name.equals( "Terminal.Cpu" ) && c instanceof ContainerCraftConfirm )
		{
			ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
			qk.cycleCpu( this.Value.equals( "Next" ) );
		}
		else if ( this.Name.equals( "Terminal.Start" ) && c instanceof ContainerCraftConfirm )
		{
			ContainerCraftConfirm qk = (ContainerCraftConfirm) c;
			qk.startJob();
		}
		else if ( this.Name.equals( "TileCrafting.Cancel" ) && c instanceof ContainerCraftingCPU )
		{
			ContainerCraftingCPU qk = (ContainerCraftingCPU) c;
			qk.cancelCrafting();
		}
		else if ( this.Name.equals( "QuartzKnife.Name" ) && c instanceof ContainerQuartzKnife )
		{
			ContainerQuartzKnife qk = (ContainerQuartzKnife) c;
			qk.setName( this.Value );
		}
		else if ( this.Name.equals( "TileSecurity.ToggleOption" ) && c instanceof ContainerSecurity )
		{
			ContainerSecurity sc = (ContainerSecurity) c;
			sc.toggleSetting( this.Value, player );
		}
		else if ( this.Name.equals( "PriorityHost.Priority" ) && c instanceof ContainerPriority )
		{
			ContainerPriority pc = (ContainerPriority) c;
			pc.setPriority( Integer.parseInt( this.Value ), player );
		}
		else if ( this.Name.equals( "LevelEmitter.Value" ) && c instanceof ContainerLevelEmitter )
		{
			ContainerLevelEmitter lvc = (ContainerLevelEmitter) c;
			lvc.setLevel( Long.parseLong( this.Value ), player );
		}
		else if ( this.Name.startsWith( "PatternTerminal." ) && c instanceof ContainerPatternTerm )
		{
			ContainerPatternTerm cpt = (ContainerPatternTerm) c;
			if ( this.Name.equals( "PatternTerminal.CraftMode" ) )
			{
				cpt.ct.setCraftingRecipe( this.Value.equals( "1" ) );
			}
			else if ( this.Name.equals( "PatternTerminal.Encode" ) )
			{
				cpt.encode();
			}
			else if ( this.Name.equals( "PatternTerminal.Clear" ) )
			{
				cpt.clear();
			}
		}
		else if ( this.Name.startsWith( "StorageBus." ) && c instanceof ContainerStorageBus )
		{
			ContainerStorageBus ccw = (ContainerStorageBus) c;
			if ( this.Name.equals( "StorageBus.Action" ) )
			{
				if ( this.Value.equals( "Partition" ) )
				{
					ccw.partition();
				}
				else if ( this.Value.equals( "Clear" ) )
				{
					ccw.clear();
				}
			}
		}
		else if ( this.Name.startsWith( "CellWorkbench." ) && c instanceof ContainerCellWorkbench )
		{
			ContainerCellWorkbench ccw = (ContainerCellWorkbench) c;
			if ( this.Name.equals( "CellWorkbench.Action" ) )
			{
				if ( this.Value.equals( "CopyMode" ) )
				{
					ccw.nextCopyMode();
				}
				else if ( this.Value.equals( "Partition" ) )
				{
					ccw.partition();
				}
				else if ( this.Value.equals( "Clear" ) )
				{
					ccw.clear();
				}
			}
			else if ( this.Name.equals( "CellWorkbench.Fuzzy" ) )
			{
				ccw.setFuzzy( FuzzyMode.valueOf( this.Value ) );
			}
		}
		else if ( c instanceof ContainerNetworkTool )
		{
			if ( this.Name.equals( "NetworkTool" ) && this.Value.equals( "Toggle" ) )
			{
				((ContainerNetworkTool) c).toggleFacadeMode();
			}
		}
		else if ( c instanceof IConfigurableObject )
		{
			IConfigManager cm = ((IConfigurableObject) c).getConfigManager();

			for (Settings e : cm.getSettings())
			{
				if ( e.name().equals( this.Name ) )
				{
					Enum<?> def = cm.getSetting( e );

					try
					{
						cm.putSetting( e, Enum.valueOf( def.getClass(), this.Value ) );
					}
					catch (IllegalArgumentException err)
					{
						// :P
					}

					break;
				}
			}
		}

	}

	@Override
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		Container c = player.openContainer;

		if ( this.Name.equals( "CustomName" ) && c instanceof AEBaseContainer )
		{
			((AEBaseContainer) c).customName = this.Value;
		}
		else if ( this.Name.startsWith( "SyncDat." ) )
		{
			((AEBaseContainer) c).stringSync( Integer.parseInt( this.Name.substring( 8 ) ), this.Value );
		}
		else if ( this.Name.equals( "CraftingStatus" ) && this.Value.equals( "Clear" ) )
		{
			GuiScreen gs = Minecraft.getMinecraft().currentScreen;
			if ( gs instanceof GuiCraftingCPU )
				((GuiCraftingCPU) gs).clearItems();
		}
		else if ( c instanceof IConfigurableObject )
		{
			IConfigManager cm = ((IConfigurableObject) c).getConfigManager();

			for (Settings e : cm.getSettings())
			{
				if ( e.name().equals( this.Name ) )
				{
					Enum<?> def = cm.getSetting( e );

					try
					{
						cm.putSetting( e, Enum.valueOf( def.getClass(), this.Value ) );
					}
					catch (IllegalArgumentException err)
					{
						// :P
					}

					break;
				}
			}
		}

	}

	// api
	public PacketValueConfig(String Name, String Value) throws IOException {
		this.Name = Name;
		this.Value = Value;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( bos );
		dos.writeUTF( Name );
		dos.writeUTF( Value );
		// dos.close();

		data.writeBytes( bos.toByteArray() );

		this.configureWrite( data );
	}
}
