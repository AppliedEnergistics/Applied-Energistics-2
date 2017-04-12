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


public class TaughtIcon implements IIcon
{

	private final float tightness;

	private final IIcon icon;

	public TaughtIcon( final IIcon icon, final float tightness )
	{
		if( icon == null )
		{
			throw new IllegalArgumentException( "Cannot create a wrapper icon with a null icon." );
		}

		this.icon = icon;
		this.tightness = tightness * 0.4f;
	}

	@Override
	public int getIconWidth()
	{
		return this.icon.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return this.icon.getIconHeight();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getMinU()
	{
		return this.u( 0 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getMaxU()
	{
		return this.u( 16 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getInterpolatedU( final double d0 )
	{
		return this.u( d0 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getMinV()
	{
		return this.v( 0 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getMaxV()
	{
		return this.v( 16 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float getInterpolatedV( final double d0 )
	{
		return this.v( d0 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public String getIconName()
	{
		return this.icon.getIconName();
	}

	private float v( double d )
	{
		if( d < 8 )
		{
			d -= this.tightness;
		}
		if( d > 8 )
		{
			d += this.tightness;
		}
		return this.icon.getInterpolatedV( Math.min( 16.0, Math.max( 0.0, d ) ) );
	}

	private float u( double d )
	{
		if( d < 8 )
		{
			d -= this.tightness;
		}
		if( d > 8 )
		{
			d += this.tightness;
		}
		return this.icon.getInterpolatedU( Math.min( 16.0, Math.max( 0.0, d ) ) );
	}
}
