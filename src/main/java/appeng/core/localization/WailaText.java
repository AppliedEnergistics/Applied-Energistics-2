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

package appeng.core.localization;


import net.minecraft.util.text.translation.I18n;


public enum WailaText
{
	Crafting,

	DeviceOnline,
	DeviceOffline,
	DeviceMissingChannel,

	P2PUnlinked,
	P2P_INPUT_ONE_OUTPUT,
	P2P_INPUT_MANY_OUTPUTS,
	P2P_OUTPUT_ONE_INPUT,
	P2P_OUTPUT_MANY_INPUTS,
	P2POutput,

	Locked,
	Unlocked,
	Showing,

	Contains,
	Channels;

	private final String root;

	WailaText()
	{
		this.root = "waila.appliedenergistics2";
	}

	WailaText( final String r )
	{
		this.root = r;
	}

	public String getLocal()
	{
		return I18n.translateToLocal( this.getUnlocalized() );
	}

	public String getUnlocalized()
	{
		return this.root + '.' + this.toString();
	}

}
