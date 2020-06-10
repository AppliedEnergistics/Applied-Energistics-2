/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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


import appeng.api.storage.data.IAEFluidStack;
import appeng.core.sync.AppEngCompressedPacket;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.fluids.client.gui.GuiFluidTerminal;
import appeng.fluids.util.AEFluidStack;
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


/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class PacketMEFluidInventoryUpdate extends AppEngCompressedPacket
{
	// input.
	@Nullable
	private final List<IAEFluidStack> list;

	protected boolean empty = true;

	// automatic.
	public PacketMEFluidInventoryUpdate( final ByteBuf stream ) throws IOException
	{
		super(stream);
		this.list = new ArrayList<>();

		if( uncompressed != null )
		{
			while( uncompressed.readableBytes() > 0 )
			{
				this.list.add( AEFluidStack.fromPacket( uncompressed ) );
			}
		}

		this.empty = this.list.isEmpty();
	}

	// api
	public PacketMEFluidInventoryUpdate() throws IOException
	{
		this( (byte) 0 );
	}

	// api
	public PacketMEFluidInventoryUpdate( final byte ref ) throws IOException
	{
		super(ref);

		this.list = null;
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void clientPacketData( final INetworkInfo network, final AppEngPacket packet, final PlayerEntity player, NetworkEvent.Context ctx )
	{
		final Screen gs = Minecraft.getInstance().currentScreen;

		if( gs instanceof GuiFluidTerminal )
		{
			( (GuiFluidTerminal) gs ).postUpdate( this.list );
		}
	}

	public void appendFluid( final IAEFluidStack fs ) throws IOException, BufferOverflowException
	{
		final ByteBuf tmp = Unpooled.buffer( OPERATION_BYTE_LIMIT );
		fs.writeToPacket( tmp );

		super.append( tmp );

		this.empty = false;
	}

	public boolean isEmpty()
	{
		return this.empty;
	}
}
