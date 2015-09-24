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


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.client.gui.implementations.GuiInterfaceTerminal;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;


public class PacketCompressedNBT implements AppEngPacket, AppEngPacketHandler<PacketCompressedNBT, AppEngPacket>
{

	// input.
	private NBTTagCompound in;
	// output...
	private ByteBuf data;
	private GZIPOutputStream compressFrame;

	// automatic.
	public PacketCompressedNBT()
	{
	}

	// api
	public PacketCompressedNBT( NBTTagCompound din )
	{
		this.in = din;
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{
		this.data = null;
		this.compressFrame = null;

		final GZIPInputStream gzReader;
		try
		{
			gzReader = new GZIPInputStream( new InputStream()
			{

				@Override
				public int read() throws IOException
				{
					if( buf.readableBytes() <= 0 )
					{
						return -1;
					}

					return buf.readByte() & 0xff;
				}
			} );

			final DataInputStream inStream = new DataInputStream( gzReader );
			this.in = CompressedStreamTools.read( inStream );
			inStream.close();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		try
		{
			this.compressFrame = new GZIPOutputStream( new OutputStream()
			{

				@Override
				public void write( int value ) throws IOException
				{
					PacketCompressedNBT.this.data.writeByte( value );
				}
			} );
			CompressedStreamTools.write( this.in, new DataOutputStream( this.compressFrame ) );
			this.compressFrame.close();
		}
		catch( IOException e )
		{
		}
	}

	@Override
	public AppEngPacket onMessage( PacketCompressedNBT message, MessageContext ctx )
	{
		final GuiScreen gs = Minecraft.getMinecraft().currentScreen;

		if( gs instanceof GuiInterfaceTerminal )
		{
			( (GuiInterfaceTerminal) gs ).postUpdate( message.in );
		}
		return null;
	}
}
