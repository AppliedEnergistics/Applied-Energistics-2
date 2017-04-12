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
import appeng.helpers.Reflected;
import net.minecraft.item.ItemStack;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class PartStorageMonitor extends AbstractPartMonitor
{
	private static final CableBusTextures FRONT_BRIGHT_ICON = CableBusTextures.PartStorageMonitor_Bright;
	private static final CableBusTextures FRONT_DARK_ICON = CableBusTextures.PartStorageMonitor_Dark;
	private static final CableBusTextures FRONT_COLORED_ICON = CableBusTextures.PartStorageMonitor_Colored;
	private static final CableBusTextures FRONT_COLORED_ICON_LOCKED = CableBusTextures.PartStorageMonitor_Colored_Locked;

	@Reflected
	public PartStorageMonitor( final ItemStack is )
	{
		super( is );
	}

	@Override
	public CableBusTextures getFrontBright()
	{
		return FRONT_BRIGHT_ICON;
	}

	@Override
	public CableBusTextures getFrontColored()
	{
		return this.isLocked() ? FRONT_COLORED_ICON_LOCKED : FRONT_COLORED_ICON;
	}

	@Override
	public CableBusTextures getFrontDark()
	{
		return FRONT_DARK_ICON;
	}
}
