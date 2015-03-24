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
import net.minecraft.world.World;


/**
 * Represents a location in the Minecraft Universe
 */
public class DimensionalCoord extends WorldCoord
{

	private final World w;
	private final int dimId;

	public DimensionalCoord( DimensionalCoord s )
	{
		super( s.x, s.y, s.z );
		this.w = s.w;
		this.dimId = s.dimId;
	}

	public DimensionalCoord( TileEntity s )
	{
		super( s );
		this.w = s.getWorldObj();
		this.dimId = this.w.provider.dimensionId;
	}

	public DimensionalCoord( World _w, int _x, int _y, int _z )
	{
		super( _x, _y, _z );
		this.w = _w;
		this.dimId = _w.provider.dimensionId;
	}

	@Override
	public DimensionalCoord copy()
	{
		return new DimensionalCoord( this );
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() ^ this.dimId;
	}

	@Override
	public boolean equals( Object obj )
	{
		return obj instanceof DimensionalCoord && this.isEqual( (DimensionalCoord) obj );
	}

	public boolean isEqual( DimensionalCoord c )
	{
		return this.x == c.x && this.y == c.y && this.z == c.z && c.w == this.w;
	}

	@Override
	public String toString()
	{
		return "dimension=" + this.dimId + ", " + super.toString();
	}

	public boolean isInWorld( World world )
	{
		return this.w == world;
	}

	public World getWorld()
	{
		return this.w;
	}
}
