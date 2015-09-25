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

package appeng.client.texture;


import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;


public class TmpFlippableIcon extends FlippableIcon
{

	private static final IIcon NULL_ICON = new MissingIcon( Blocks.diamond_block );

	public TmpFlippableIcon()
	{
		super( NULL_ICON );
	}

	public void setOriginal( IIcon i )
	{
		this.setFlip( false, false );

		while( i instanceof FlippableIcon )
		{
			final FlippableIcon fi = (FlippableIcon) i;
			if( fi.flip_u )
			{
				this.flip_u = !this.flip_u;
			}

			if( fi.flip_v )
			{
				this.flip_v = !this.flip_v;
			}

			i = fi.getOriginal();
		}

		if( i == null )
		{
			this.original = NULL_ICON;
		}
		else
		{
			this.original = i;
		}
	}
}
