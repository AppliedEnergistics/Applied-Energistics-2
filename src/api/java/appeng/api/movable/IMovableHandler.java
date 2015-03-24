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

package appeng.api.movable;


import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;


public interface IMovableHandler
{

	/**
	 * if you return true from this, your saying you can handle the class, not
	 * that single entity, you cannot opt out of single entities.
	 *
	 * @param myClass tile entity class
	 * @param tile    tile entity
	 *
	 * @return true if it can handle moving
	 */
	boolean canHandle( Class<? extends TileEntity> myClass, TileEntity tile );

	/**
	 * request that the handler move the the tile from its current location to
	 * the new one. the tile has already been invalidated, and the blocks have
	 * already been fully moved.
	 *
	 * Potential Example:
	 *
	 * <pre>
	 * {@code
	 * Chunk c = world.getChunkFromBlockCoords( x, z ); c.setChunkBlockTileEntity( x
	 * & 0xF, y + y, z & 0xF, tile );
	 *
	 * if ( c.isChunkLoaded ) { world.addTileEntity( tile ); world.markBlockForUpdate( x,
	 * y, z ); }
	 * }
	 * </pre>
	 *
	 * @param tile  to be moved tile
	 * @param world world of tile
	 * @param x     x coord of tile
	 * @param y     y coord of tile
	 * @param z     z coord of tile
	 */
	void moveTile( TileEntity tile, World world, int x, int y, int z );
}