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

	public FlippableIcon( IIcon o )
	{

		if( o == null )
			throw new RuntimeException( "Cannot create a wrapper icon with a null icon." );

		this.original = o;
		this.flip_u = false;
		this.flip_v = false;
	}

	@Override
	public int getIconWidth()
	{
		return this.original.getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return this.original.getIconHeight();
	}

	@Override
	public float getMinU()
	{
		if( this.flip_u )
			return this.original.getMaxU();
		return this.original.getMinU();
	}

	@Override
	public float getMaxU()
	{
		if( this.flip_u )
			return this.original.getMinU();
		return this.original.getMaxU();
	}

	@Override
	public float getInterpolatedU( double px )
	{
		if( this.flip_u )
			return this.original.getInterpolatedU( 16 - px );
		return this.original.getInterpolatedU( px );
	}

	@Override
	public float getMinV()
	{
		if( this.flip_v )
			return this.original.getMaxV();
		return this.original.getMinV();
	}

	@Override
	public float getMaxV()
	{
		if( this.flip_v )
			return this.original.getMinV();
		return this.original.getMaxV();
	}

	@Override
	public float getInterpolatedV( double px )
	{
		if( this.flip_v )
			return this.original.getInterpolatedV( 16 - px );
		return this.original.getInterpolatedV( px );
	}

	@Override
	public String getIconName()
	{
		return this.original.getIconName();
	}

	public IIcon getOriginal()
	{
		return this.original;
	}

	public void setFlip( boolean u, boolean v )
	{
		this.flip_u = u;
		this.flip_v = v;
	}

	public int setFlip( int orientation )
	{
		this.flip_u = ( orientation & 8 ) == 8;
		this.flip_v = ( orientation & 16 ) == 16;
		return orientation & 7;
	}
}
