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
import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.client.gui.implementations.GuiInterfaceTerminal;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;


public class PacketCompressedNBT extends AppEngPacket
{

	// input.
	final NBTTagCompound in;
	// output...
	final private ByteBuf data;
	final private GZIPOutputStream compressFrame;
	int writtenBytes = 0;
	boolean empty = true;

	// automatic.
	public PacketCompressedNBT( final ByteBuf stream ) throws IOException
	{
		this.data = null;
		this.compressFrame = null;

		GZIPInputStream gzReader = new GZIPInputStream( new InputStream()
		{

			@Override
			public int read() throws IOException
			{
				if( stream.readableBytes() <= 0 )
					return -1;

				return stream.readByte() & 0xff;
			}
		} );

		DataInputStream inStream = new DataInputStream( gzReader );
		this.in = CompressedStreamTools.read( inStream );
		inStream.close();
	}

	// api
	public PacketCompressedNBT( NBTTagCompound din ) throws IOException
	{

		this.data = Unpooled.buffer( 2048 );
		this.data.writeInt( this.getPacketID() );

		this.in = din;

		this.compressFrame = new GZIPOutputStream( new OutputStream()
		{

			@Override
			public void write( int value ) throws IOException
			{
				PacketCompressedNBT.this.data.writeByte( value );
			}
		} );

		CompressedStreamTools.write( din, new DataOutputStream( this.compressFrame ) );
		this.compressFrame.close();

		this.configureWrite( this.data );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void clientPacketData( INetworkInfo network, AppEngPacket packet, EntityPlayer player )
	{
		GuiScreen gs = Minecraft.getMinecraft().currentScreen;

		if( gs instanceof GuiInterfaceTerminal )
			( (GuiInterfaceTerminal) gs ).postUpdate( this.in );
	}
}
