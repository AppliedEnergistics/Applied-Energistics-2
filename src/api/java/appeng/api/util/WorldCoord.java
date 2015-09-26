/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.util;


import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;


/**
 * Represents a relative coordinate, either relative to another object, or
 * relative to the origin of a dimension.
 */
public class WorldCoord
{

	public int x;
	public int y;
	public int z;

	public WorldCoord( final TileEntity s )
	{
		this( s.xCoord, s.yCoord, s.zCoord );
	}

	public WorldCoord( final int _x, final int _y, final int _z )
	{
		this.x = _x;
		this.y = _y;
		this.z = _z;
	}

	public WorldCoord subtract( final ForgeDirection direction, final int length )
	{
		this.x -= direction.offsetX * length;
		this.y -= direction.offsetY * length;
		this.z -= direction.offsetZ * length;
		return this;
	}

	public WorldCoord add( final int _x, final int _y, final int _z )
	{
		this.x += _x;
		this.y += _y;
		this.z += _z;
		return this;
	}

	public WorldCoord subtract( final int _x, final int _y, final int _z )
	{
		this.x -= _x;
		this.y -= _y;
		this.z -= _z;
		return this;
	}

	public WorldCoord multiple( final int _x, final int _y, final int _z )
	{
		this.x *= _x;
		this.y *= _y;
		this.z *= _z;
		return this;
	}

	public WorldCoord divide( final int _x, final int _y, final int _z )
	{
		this.x /= _x;
		this.y /= _y;
		this.z /= _z;
		return this;
	}

	/**
	 * Will Return NULL if it's at some diagonal!
	 */
	public ForgeDirection directionTo( final WorldCoord loc )
	{
		final int ox = this.x - loc.x;
		final int oy = this.y - loc.y;
		final int oz = this.z - loc.z;

		final int xlen = Math.abs( ox );
		final int ylen = Math.abs( oy );
		final int zlen = Math.abs( oz );

		if( loc.isEqual( this.copy().add( ForgeDirection.EAST, xlen ) ) )
		{
			return ForgeDirection.EAST;
		}

		if( loc.isEqual( this.copy().add( ForgeDirection.WEST, xlen ) ) )
		{
			return ForgeDirection.WEST;
		}

		if( loc.isEqual( this.copy().add( ForgeDirection.NORTH, zlen ) ) )
		{
			return ForgeDirection.NORTH;
		}

		if( loc.isEqual( this.copy().add( ForgeDirection.SOUTH, zlen ) ) )
		{
			return ForgeDirection.SOUTH;
		}

		if( loc.isEqual( this.copy().add( ForgeDirection.UP, ylen ) ) )
		{
			return ForgeDirection.UP;
		}

		if( loc.isEqual( this.copy().add( ForgeDirection.DOWN, ylen ) ) )
		{
			return ForgeDirection.DOWN;
		}

		return null;
	}

	public boolean isEqual( final WorldCoord c )
	{
		return this.x == c.x && this.y == c.y && this.z == c.z;
	}

	public WorldCoord add( final ForgeDirection direction, final int length )
	{
		this.x += direction.offsetX * length;
		this.y += direction.offsetY * length;
		this.z += direction.offsetZ * length;
		return this;
	}

	public WorldCoord copy()
	{
		return new WorldCoord( this.x, this.y, this.z );
	}

	@Override
	public int hashCode()
	{
		return ( this.y << 24 ) ^ this.x ^ this.z;
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof WorldCoord && this.isEqual( (WorldCoord) obj );
	}

	@Override
	public String toString()
	{
		return "x=" + this.x + ", y=" + this.y + ", z=" + this.z;
	}
}
