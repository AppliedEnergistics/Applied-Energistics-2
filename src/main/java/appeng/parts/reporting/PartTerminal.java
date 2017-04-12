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
import net.minecraft.item.ItemStack;


public class PartTerminal extends AbstractPartTerminal
{

	public PartTerminal( final ItemStack is )
	{
		super( is );
	}

	private static final CableBusTextures FRONT_BRIGHT_ICON = CableBusTextures.PartTerminal_Bright;
	private static final CableBusTextures FRONT_DARK_ICON = CableBusTextures.PartTerminal_Dark;
	private static final CableBusTextures FRONT_COLORED_ICON = CableBusTextures.PartTerminal_Colored;

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
