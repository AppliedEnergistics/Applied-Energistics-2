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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
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
import appeng.core.sync.AppEngPacketHandler;
import appeng.helpers.Reflected;
import appeng.util.item.AEItemStack;


public class PacketMEInventoryUpdate implements AppEngPacket, AppEngPacketHandler<PacketMEInventoryUpdate, AppEngPacket>
{
	private static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 16 * 1024 * 1024;
	private static final int OPERATION_BYTE_LIMIT = 2 * 1024;
	private static final int TEMP_BUFFER_SIZE = 1024;
	private static final int STREAM_MASK = 0xff;

	// input.
	@Nullable
	private final List<IAEItemStack> list = new LinkedList<IAEItemStack>();
	// output...
	private byte ref;

	private int writtenBytes = 0;
	private boolean empty = true;

	// automatic.
	@Reflected
	public PacketMEInventoryUpdate()
	{
		this( (byte) 0 );
	}

	// api
	public PacketMEInventoryUpdate( final byte ref )
	{
		this.ref = ref;
	}

	public void appendItem( final IAEItemStack is ) throws IOException, BufferOverflowException
	{
		// is.writeToPacket( this.data );
		this.list.add( is.copy() );
	}

	public int getLength()
	{
		return this.writtenBytes;
	}

	public boolean isEmpty()
	{
		return this.empty;
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{
		this.ref = buf.readByte();

		try
		{
			final GZIPInputStream gzReader = new GZIPInputStream( new InputStream()
			{
				@Override
				public int read() throws IOException
				{
					if( buf.readableBytes() <= 0 )
					{
						return -1;
					}

					return buf.readByte() & STREAM_MASK;
				}
			} );

			final ByteBuf uncompressed = Unpooled.buffer( buf.readableBytes() );
			final byte[] tmp = new byte[TEMP_BUFFER_SIZE];

			while( gzReader.available() != 0 )
			{
				final int bytes = gzReader.read( tmp );
				if( bytes > 0 )
				{
					uncompressed.writeBytes( tmp, 0, bytes );
				}
			}

			gzReader.close();

			while( uncompressed.readableBytes() > 0 )
			{
				this.list.add( AEItemStack.loadItemStackFromPacket( uncompressed ) );
			}
		}
		catch( final IOException e )
		{
		}

		this.empty = this.list.isEmpty();
	}

	@Override
	public void toBytes( final ByteBuf buf )
	{
		buf.writeByte( this.ref );

		OutputStream compressFrame = null;

		try
		{
			compressFrame = new GZIPOutputStream( new OutputStream()
			{
				@Override
				public void write( final int value ) throws IOException
				{
					buf.writeByte( value );
				}
			} );

			for( final IAEItemStack iaeItemStack : this.list )
			{
				final ByteBuf tmp = Unpooled.buffer( OPERATION_BYTE_LIMIT );

				iaeItemStack.writeToPacket( tmp );

				if( this.writtenBytes + tmp.readableBytes() > UNCOMPRESSED_PACKET_BYTE_LIMIT )
				{
					throw new BufferOverflowException();
				}
				else
				{
					this.writtenBytes += tmp.readableBytes();
					compressFrame.write( tmp.array(), 0, tmp.readableBytes() );
					this.empty = false;
				}

			}
			compressFrame.flush();
		}
		catch( final IOException e1 )
		{
		}
		finally
		{
			if( compressFrame != null )
			{
				try
				{
					compressFrame.close();
				}
				catch( final IOException e )
				{
				}
			}
		}
	}

	@Override
	public AppEngPacket onMessage( final PacketMEInventoryUpdate message, final MessageContext ctx )
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
