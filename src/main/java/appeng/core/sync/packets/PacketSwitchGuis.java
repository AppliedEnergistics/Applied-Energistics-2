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


import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

import appeng.client.gui.AEBaseGui;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;


public class PacketSwitchGuis extends AppEngPacket<PacketSwitchGuis>
{

	private GuiBridge newGui;

	// automatic.
	public PacketSwitchGuis()
	{
	}

	// api
	public PacketSwitchGuis( final GuiBridge newGui )
	{
		this.newGui = newGui;

		if( Platform.isClient() )
		{
			AEBaseGui.setSwitchingGuis( true );
		}
	}

	@Override
	public PacketSwitchGuis onMessage( PacketSwitchGuis message, MessageContext ctx )
	{
		if( ctx.side == Side.CLIENT )
		{
			AEBaseGui.setSwitchingGuis( true );
		}
		else
		{
			final Container c = ctx.getServerHandler().playerEntity.openContainer;
			if( c instanceof AEBaseContainer )
			{
				final AEBaseContainer bc = (AEBaseContainer) c;
				final ContainerOpenContext context = bc.getOpenContext();

				if( context != null )
				{
					final TileEntity te = context.getTile();
					Platform.openGUI( ctx.getServerHandler().playerEntity, te, context.getSide(), message.newGui );
				}
			}
		}
		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.newGui = GuiBridge.values()[buf.readInt()];
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		AEBaseGui.setSwitchingGuis( true );
		buf.writeInt( newGui.ordinal() );
	}
}
