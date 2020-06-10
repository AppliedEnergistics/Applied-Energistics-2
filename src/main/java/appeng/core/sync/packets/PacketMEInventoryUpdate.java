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


import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingCPU;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiNetworkStatus;
import appeng.core.sync.AppEngCompressedPacket;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;


public class PacketMEInventoryUpdate extends AppEngCompressedPacket
{
	// input.
	@Nullable
	private final List<IAEItemStack> list;

	protected boolean empty = true;

	// automatic.
	public PacketMEInventoryUpdate( final ByteBuf stream ) throws IOException
	{
		super(stream);
		this.list = new ArrayList<>();

		if( uncompressed != null )
		{
			while( uncompressed.readableBytes() > 0 )
			{
				this.list.add( AEItemStack.fromPacket( uncompressed ) );
			}
		}

		this.empty = this.list.isEmpty();

	}

	// api
	public PacketMEInventoryUpdate() throws IOException
	{
		this( (byte) 0 );
	}

	// api
	public PacketMEInventoryUpdate( final byte ref ) throws IOException
	{
		super(ref);

		this.list = null;
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void clientPacketData( final INetworkInfo network, final AppEngPacket packet, final PlayerEntity player, NetworkEvent.Context ctx )
	{
		final Screen gs = Minecraft.getInstance().currentScreen;

		if( gs instanceof GuiCraftConfirm )
		{
			( (GuiCraftConfirm) gs ).postUpdate( this.list, this.ref );
		}

		if( gs instanceof GuiCraftingCPU )
		{
			( (GuiCraftingCPU) gs ).postUpdate( this.list, this.ref );
		}

		if( gs instanceof GuiMEMonitorable )
		{
			( (GuiMEMonitorable) gs ).postUpdate( this.list );
		}

		if( gs instanceof GuiNetworkStatus )
		{
			( (GuiNetworkStatus) gs ).postUpdate( this.list );
		}
	}

	public void appendItem( final IAEItemStack is ) throws IOException, BufferOverflowException
	{
		final ByteBuf tmp = Unpooled.buffer( OPERATION_BYTE_LIMIT );
		is.writeToPacket( tmp );

		super.append( tmp );
		this.empty = false;
	}

	public boolean isEmpty()
	{
		return this.empty;
	}
}
