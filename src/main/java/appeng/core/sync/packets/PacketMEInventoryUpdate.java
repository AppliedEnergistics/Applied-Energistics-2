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
import java.nio.BufferOverflowException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingCPU;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiNetworkStatus;
import appeng.core.sync.AppEngPacket;
import appeng.util.item.AEItemStack;


public class PacketMEInventoryUpdate extends AppEngPacket<PacketMEInventoryUpdate>
{
	private static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 16 * 1024 * 1024;
	private static final int OPERATION_BYTE_LIMIT = 2 * 1024;
	private static final int TEMP_BUFFER_SIZE = 1024;
	private static final int STREAM_MASK = 0xff;

	// input.
	@Nullable
	private List<IAEItemStack> list;
	// output...
	private byte ref;

	@Nullable
	private ByteBuf data = Unpooled.buffer( OPERATION_BYTE_LIMIT );
	@Nullable
	private GZIPOutputStream compressFrame;

	private int writtenBytes = 0;
	private boolean empty = true;

	// automatic.
	public PacketMEInventoryUpdate()
	{
		this( (byte) 0 );
	}

	// api
	public PacketMEInventoryUpdate( byte ref )
	{
		this.ref = ref;
	}

	public void appendItem( IAEItemStack is ) throws IOException, BufferOverflowException
	{
		is.writeToPacket( this.data );
	}

	public int getLength()
	{
		return this.data.readableBytes();
	}

	public boolean isEmpty()
	{
		return this.data.readableBytes() == 0;
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{
		this.list = new LinkedList<IAEItemStack>();
		this.ref = buf.readByte();

		while( buf.readableBytes() > 0 )
		{
			try
			{
				this.list.add( AEItemStack.loadItemStackFromPacket( buf ) );
			}
			catch( IOException e )
			{
			}
		}

		this.empty = this.list.isEmpty();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeByte( this.ref );
		buf.writeBytes( this.data );
	}

	@Override
	public PacketMEInventoryUpdate onMessage( PacketMEInventoryUpdate message, MessageContext ctx )
	{
		final GuiScreen gs = Minecraft.getMinecraft().currentScreen;

		if( gs instanceof GuiCraftConfirm )
		{
			( (GuiCraftConfirm) gs ).postUpdate( message.list, message.ref );
		}

		if( gs instanceof GuiCraftingCPU )
		{
			( (GuiCraftingCPU) gs ).postUpdate( message.list, message.ref );
		}

		if( gs instanceof GuiMEMonitorable )
		{
			( (GuiMEMonitorable) gs ).postUpdate( message.list );
		}

		if( gs instanceof GuiNetworkStatus )
		{
			( (GuiNetworkStatus) gs ).postUpdate( message.list );
		}
		return null;
	}
}
