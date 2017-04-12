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

package appeng.parts.reporting;


import appeng.client.texture.CableBusTextures;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;


public class PartInterfaceTerminal extends AbstractPartDisplay
{
	private static final CableBusTextures FRONT_BRIGHT_ICON = CableBusTextures.PartInterfaceTerm_Bright;
	private static final CableBusTextures FRONT_DARK_ICON = CableBusTextures.PartInterfaceTerm_Dark;
	private static final CableBusTextures FRONT_COLORED_ICON = CableBusTextures.PartInterfaceTerm_Colored;

	public PartInterfaceTerminal( final ItemStack is )
	{
		super( is );
	}

	@Override
	public boolean onPartActivate( final EntityPlayer player, final Vec3 pos )
	{
		if( !super.onPartActivate( player, pos ) )
		{
			if( !player.isSneaking() )
			{
				if( Platform.isClient() )
				{
					return true;
				}

				Platform.openGUI( player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_INTERFACE_TERMINAL );

				return true;
			}
		}

		return false;
	}

	@Override
	public CableBusTextures getFrontBright()
	{
		return FRONT_BRIGHT_ICON;
	}

	@Override
	public CableBusTextures getFrontColored()
	{
		return FRONT_COLORED_ICON;
	}

	@Override
	public CableBusTextures getFrontDark()
	{
		return FRONT_DARK_ICON;
	}
}
