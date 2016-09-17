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
import net.minecraft.util.math.BlockPos;


/**
 * Represents a relative coordinate, either relative to another object, or
 * relative to the origin of a dimension.
 *
 * TODO: Consider replacing with {@link BlockPos}
 */
public class WorldCoord
{

	public int x;
	public int y;
	public int z;

	public WorldCoord( final TileEntity s )
	{
		this( s.getPos() );
	}

	public WorldCoord( final int _x, final int _y, final int _z )
	{
		this.x = _x;
		this.y = _y;
		this.z = _z;
	}

	public WorldCoord( final BlockPos pos )
	{
		this.x = pos.getX();
		this.y = pos.getY();
		this.z = pos.getZ();
	}

	public WorldCoord subtract( final AEPartLocation direction, final int length )
	{
		this.x -= direction.xOffset * length;
		this.y -= direction.yOffset * length;
		this.z -= direction.zOffset * length;
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
	public AEPartLocation directionTo( final WorldCoord loc )
	{
		final int ox = this.x - loc.x;
		final int oy = this.y - loc.y;
		final int oz = this.z - loc.z;

		final int xlen = Math.abs( ox );
		final int ylen = Math.abs( oy );
		final int zlen = Math.abs( oz );

		if( loc.isEqual( this.copy().add( AEPartLocation.EAST, xlen ) ) )
		{
			return AEPartLocation.EAST;
		}

		if( loc.isEqual( this.copy().add( AEPartLocation.WEST, xlen ) ) )
		{
			return AEPartLocation.WEST;
		}

		if( loc.isEqual( this.copy().add( AEPartLocation.NORTH, zlen ) ) )
		{
			return AEPartLocation.NORTH;
		}

		if( loc.isEqual( this.copy().add( AEPartLocation.SOUTH, zlen ) ) )
		{
			return AEPartLocation.SOUTH;
		}

		if( loc.isEqual( this.copy().add( AEPartLocation.UP, ylen ) ) )
		{
			return AEPartLocation.UP;
		}

		if( loc.isEqual( this.copy().add( AEPartLocation.DOWN, ylen ) ) )
		{
			return AEPartLocation.DOWN;
		}

		return null;
	}

	public boolean isEqual( final WorldCoord c )
	{
		return this.x == c.x && this.y == c.y && this.z == c.z;
	}

	public WorldCoord add( final AEPartLocation direction, final int length )
	{
		this.x += direction.xOffset * length;
		this.y += direction.yOffset * length;
		this.z += direction.zOffset * length;
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

	public BlockPos getPos()
	{
		return new BlockPos( this.x, this.y, this.z );
	}

	@Override
	public String toString()
	{
		return "x=" + this.x + ", y=" + this.y + ", z=" + this.z;
	}
}
