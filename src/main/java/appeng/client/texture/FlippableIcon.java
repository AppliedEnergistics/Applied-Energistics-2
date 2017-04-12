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

import javax.annotation.Nonnull;


public class FlippableIcon implements IIcon
{

	private IIcon original;
	private boolean flip_u;
	private boolean flip_v;

	public FlippableIcon( final IIcon o )
	{
		if( o == null )
		{
			throw new IllegalArgumentException( "Cannot create a wrapper icon with a null icon." );
		}

		this.setOriginal( o );
		this.setFlipU( false );
		this.setFlipV( false );
	}

	@Override
	public int getIconWidth()
	{
		return this.getOriginal().getIconWidth();
	}

	@Override
	public int getIconHeight()
	{
		return this.getOriginal().getIconHeight();
	}

	@Override
	public float getMinU()
	{
		if( this.isFlipU() )
		{
			return this.getOriginal().getMaxU();
		}
		return this.getOriginal().getMinU();
	}

	@Override
	public float getMaxU()
	{
		if( this.isFlipU() )
		{
			return this.getOriginal().getMinU();
		}
		return this.getOriginal().getMaxU();
	}

	@Override
	public float getInterpolatedU( final double px )
	{
		if( this.isFlipU() )
		{
			return this.getOriginal().getInterpolatedU( 16 - px );
		}
		return this.getOriginal().getInterpolatedU( px );
	}

	@Override
	public float getMinV()
	{
		if( this.isFlipV() )
		{
			return this.getOriginal().getMaxV();
		}
		return this.getOriginal().getMinV();
	}

	@Override
	public float getMaxV()
	{
		if( this.isFlipV() )
		{
			return this.getOriginal().getMinV();
		}
		return this.getOriginal().getMaxV();
	}

	@Override
	public float getInterpolatedV( final double px )
	{
		if( this.isFlipV() )
		{
			return this.getOriginal().getInterpolatedV( 16 - px );
		}
		return this.getOriginal().getInterpolatedV( px );
	}

	@Override
	public String getIconName()
	{
		return this.getOriginal().getIconName();
	}

	public IIcon getOriginal()
	{
		return this.original;
	}

	public void setFlip( final boolean u, final boolean v )
	{
		this.setFlipU( u );
		this.setFlipV( v );
	}

	public int setFlip( final int orientation )
	{
		this.setFlipU( ( orientation & 8 ) == 8 );
		this.setFlipV( ( orientation & 16 ) == 16 );
		return orientation & 7;
	}

	boolean isFlipU()
	{
		return this.flip_u;
	}

	void setFlipU( final boolean flipU )
	{
		this.flip_u = flipU;
	}

	boolean isFlipV()
	{
		return this.flip_v;
	}

	void setFlipV( final boolean flipV )
	{
		this.flip_v = flipV;
	}

	public void setOriginal( @Nonnull final IIcon original )
	{
		this.original = original;
	}
}
