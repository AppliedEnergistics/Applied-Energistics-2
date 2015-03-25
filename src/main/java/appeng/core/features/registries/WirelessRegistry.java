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

package appeng.core.features.registries;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;


public final class WirelessRegistry implements IWirelessTermRegistry
{
	private final List<IWirelessTermHandler> handlers;

	public WirelessRegistry()
	{
		this.handlers = new ArrayList<IWirelessTermHandler>();
	}

	@Override
	public void registerWirelessHandler( IWirelessTermHandler handler )
	{
		if( handler != null )
			this.handlers.add( handler );
	}

	@Override
	public boolean isWirelessTerminal( ItemStack is )
	{
		for( IWirelessTermHandler h : this.handlers )
		{
			if( h.canHandle( is ) )
				return true;
		}
		return false;
	}

	@Override
	public IWirelessTermHandler getWirelessTerminalHandler( ItemStack is )
	{
		for( IWirelessTermHandler h : this.handlers )
		{
			if( h.canHandle( is ) )
				return h;
		}
		return null;
	}

	@Override
	public void openWirelessTerminalGui( ItemStack item, World w, EntityPlayer player )
	{
		if( Platform.isClient() )
			return;

		if( !this.isWirelessTerminal( item ) )
		{
			player.addChatMessage( PlayerMessages.DeviceNotWirelessTerminal.get() );
			return;
		}

		final IWirelessTermHandler handler = this.getWirelessTerminalHandler( item );
		final String unparsedKey = handler.getEncryptionKey( item );
		if( unparsedKey.length() == 0 )
		{
			player.addChatMessage( PlayerMessages.DeviceNotLinked.get() );
			return;
		}

		final long parsedKey = Long.parseLong( unparsedKey );
		final ILocatable securityStation = AEApi.instance().registries().locatable().getLocatableBy( parsedKey );
		if( securityStation == null )
		{
			player.addChatMessage( PlayerMessages.StationCanNotBeLocated.get() );
			return;
		}

		if( handler.hasPower( player, 0.5, item ) )
		{
			Platform.openGUI( player, null, null, GuiBridge.GUI_WIRELESS_TERM );
		}
		else
			player.addChatMessage( PlayerMessages.DeviceNotPowered.get() );
	}
}
