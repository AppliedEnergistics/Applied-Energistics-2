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


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;


public class FullIcon implements IIcon
{

	private final IIcon p;

	public FullIcon( final IIcon o )
	{
		if( o == null )
		{
			throw new IllegalArgumentException( "Cannot create a wrapper icon with a null icon." );
		}

		this.p = o;
	}

	@Override
	public int getIconWidth()
	{
		return this.p.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return this.p.getIconHeight();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getMinU()
	{
		return this.p.getMinU();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getMaxU()
	{
		return this.p.getMaxU();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getInterpolatedU( final double d0 )
	{
		if( d0 > 8.0 )
		{
			return this.p.getMaxU();
		}
		return this.p.getMinU();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getMinV()
	{
		return this.p.getMinV();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getMaxV()
	{
		return this.p.getMaxV();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getInterpolatedV( final double d0 )
	{
		if( d0 > 8.0 )
		{
			return this.p.getMaxV();
		}
		return this.p.getMinV();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public String getIconName()
	{
		return this.p.getIconName();
	}
}
