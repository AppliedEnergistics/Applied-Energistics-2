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

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.AppEngPacketHandler;
import appeng.helpers.Reflected;
import appeng.util.Platform;


public final class PacketConfigButton implements AppEngPacket, AppEngPacketHandler<PacketConfigButton, AppEngPacket>
{
	private Settings option;
	private boolean rotationDirection;

	@Reflected
	public PacketConfigButton()
	{
		// automatic.
	}

	// api
	public PacketConfigButton( final Settings option, final boolean rotationDirection )
	{
		this.option = option;
		this.rotationDirection = rotationDirection;
	}

	@Override
	public AppEngPacket onMessage( final PacketConfigButton message, final MessageContext ctx )
	{
		final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;

		if( sender.openContainer instanceof AEBaseContainer )
		{
			final AEBaseContainer baseContainer = (AEBaseContainer) sender.openContainer;
			if( baseContainer.getTarget() instanceof IConfigurableObject )
			{
				final IConfigManager cm = ( (IConfigurableObject) baseContainer.getTarget() ).getConfigManager();
				final Enum<?> newState = Platform.rotateEnum( cm.getSetting( message.option ), message.rotationDirection,
						message.option.getPossibleValues() );
				cm.putSetting( message.option, newState );
			}
		}
		return null;
	}

	@Override
	public void fromBytes( final ByteBuf buf )
	{
		this.option = Settings.values()[buf.readInt()];
		this.rotationDirection = buf.readBoolean();
	}

	@Override
	public void toBytes( final ByteBuf buf )
	{
		buf.writeInt( this.option.ordinal() );
		buf.writeBoolean( this.rotationDirection );
	}
}
