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
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;

public class WirelessRegistry implements IWirelessTermRegistry
{

	final List<IWirelessTermHandler> handlers;

	public WirelessRegistry() {
		handlers = new ArrayList<IWirelessTermHandler>();
	}

	@Override
	public void registerWirelessHandler(IWirelessTermHandler handler)
	{
		if ( handler != null )
			handlers.add( handler );
	}

	@Override
	public boolean isWirelessTerminal(ItemStack is)
	{
		for (IWirelessTermHandler h : handlers)
		{
			if ( h.canHandle( is ) )
				return true;
		}
		return false;
	}

	@Override
	public IWirelessTermHandler getWirelessTerminalHandler(ItemStack is)
	{
		for (IWirelessTermHandler h : handlers)
		{
			if ( h.canHandle( is ) )
				return h;
		}
		return null;
	}

	@Override
	public void openWirelessTerminalGui(ItemStack item, World w, EntityPlayer player)
	{
		if ( Platform.isClient() )
			return;

		IWirelessTermHandler handler = getWirelessTerminalHandler( item );
		if ( handler == null )
		{
			player.addChatMessage( new ChatComponentText( "Item is not a wireless terminal." ) );
			return;
		}

		if ( handler.hasPower( player, 0.5, item ) )
		{
			Platform.openGUI( player, null, null, GuiBridge.GUI_WIRELESS_TERM );
		}
		else
			player.addChatMessage( PlayerMessages.DeviceNotPowered.get() );

	}

}
