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

package appeng.container.implementations;


import net.minecraft.entity.player.InventoryPlayer;

import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.Platform;


public class ContainerWirelessTerm extends ContainerMEPortableCell
{

	final WirelessTerminalGuiObject wirelessTerminalGUIObject;

	public ContainerWirelessTerm( InventoryPlayer ip, WirelessTerminalGuiObject wirelessTerminalGUIObject )
	{
		super( ip, wirelessTerminalGUIObject );
		this.wirelessTerminalGUIObject = wirelessTerminalGUIObject;
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		if( !this.wirelessTerminalGUIObject.rangeCheck() )
		{
			if( Platform.isServer() && this.isContainerValid )
				this.getPlayerInv().player.addChatMessage( PlayerMessages.OutOfRange.get() );

			this.isContainerValid = false;
		}
		else
		{
			this.powerMultiplier = AEConfig.instance.wireless_getDrainRate( this.wirelessTerminalGUIObject.getRange() );
		}
	}
}
