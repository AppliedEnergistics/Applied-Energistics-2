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


import net.minecraft.item.ItemStack;


/**
 * A more sophisticated part overlapping all 3 textures.
 *
 * Subclass this if you need want a new part and need all 3 textures.
 * For more concrete implementations, the direct abstract subclasses might be a better alternative.
 *
 * @author AlgorithmX2
 * @author yueh
 * @version rv3
 * @since rv3
 */
public abstract class AbstractPartDisplay extends AbstractPartReporting
{

	public AbstractPartDisplay( final ItemStack is )
	{
		super( is, true );
	}

	@Override
	public boolean isLightSource()
	{
		return false;
	}

}
