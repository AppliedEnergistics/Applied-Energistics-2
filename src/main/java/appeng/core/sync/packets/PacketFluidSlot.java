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


import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.fluids.container.IFluidSyncContainer;
import appeng.fluids.util.AEFluidStack;
import net.minecraftforge.fml.network.NetworkEvent;


public class PacketFluidSlot extends AppEngPacket
{
	private final Map<Integer, IAEFluidStack> list;

	public PacketFluidSlot( final ByteBuf stream )
	{
		this.list = new HashMap<>();
		final PacketBuffer data = new PacketBuffer(stream);

		CompoundNBT tag = data.readCompoundTag();

		for( final String key : tag.keySet() )
		{
			this.list.put( Integer.parseInt( key ), AEFluidStack.fromNBT( tag.getCompound( key ) ) );
		}
	}

	// api
	public PacketFluidSlot( final Map<Integer, IAEFluidStack> list )
	{
		this.list = list;
		final CompoundNBT sendTag = new CompoundNBT();
		for( Map.Entry<Integer, IAEFluidStack> fs : list.entrySet() )
		{
			final CompoundNBT tag = new CompoundNBT();
			if( fs.getValue() != null )
			{
				fs.getValue().write( tag );
			}
			sendTag.put( fs.getKey().toString(), tag );
		}

		final PacketBuffer data = new PacketBuffer(Unpooled.buffer());
		data.writeCompoundTag( sendTag );
		this.configureWrite( data );
	}

	@Override
	public void clientPacketData( final INetworkInfo manager, final AppEngPacket packet, final PlayerEntity player, NetworkEvent.Context ctx )
	{
		final Container c = player.openContainer;
		if( c instanceof IFluidSyncContainer )
		{
			( (IFluidSyncContainer) c ).receiveFluidSlots( this.list );
		}
	}

	@Override
	public void serverPacketData( INetworkInfo manager, AppEngPacket packet, PlayerEntity player, NetworkEvent.Context ctx )
	{
		final Container c = player.openContainer;
		if( c instanceof IFluidSyncContainer )
		{
			( (IFluidSyncContainer) c ).receiveFluidSlots( this.list );
		}
	}
}
