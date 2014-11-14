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

import net.minecraft.util.IIcon;

public class FlippableIcon implements IIcon
{

	protected IIcon original;
	boolean flip_u;
	boolean flip_v;

	public FlippableIcon(IIcon o) {

		if ( o == null )
			throw new RuntimeException( "Cannot create a wrapper icon with a null icon." );

		original = o;
		flip_u = false;
		flip_v = false;
	}

	@Override
	public int getIconWidth()
	{
		return original.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return original.getIconHeight();
	}

	@Override
	public float getMinU()
	{
		if ( flip_u )
			return original.getMaxU();
		return original.getMinU();
	}

	@Override
	public float getMaxU()
	{
		if ( flip_u )
			return original.getMinU();
		return original.getMaxU();
	}

	@Override
	public float getInterpolatedU(double px)
	{
		if ( flip_u )
			return original.getInterpolatedU( 16 - px );
		return original.getInterpolatedU( px );
	}

	@Override
	public float getMinV()
	{
		if ( flip_v )
			return original.getMaxV();
		return original.getMinV();
	}

	@Override
	public float getMaxV()
	{
		if ( flip_v )
			return original.getMinV();
		return original.getMaxV();
	}

	@Override
	public float getInterpolatedV(double px)
	{
		if ( flip_v )
			return original.getInterpolatedV( 16 - px );
		return original.getInterpolatedV( px );
	}

	@Override
	public String getIconName()
	{
		return original.getIconName();
	}

	public IIcon getOriginal()
	{
		return original;
	}

	public void setFlip(boolean u, boolean v)
	{
		flip_u = u;
		flip_v = v;
	}

	public int setFlip(int orientation)
	{
		flip_u = (orientation & 8) == 8;
		flip_v = (orientation & 16) == 16;
		return orientation & 7;
	}

}
