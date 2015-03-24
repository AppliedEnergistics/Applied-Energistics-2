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


import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;


/**
 * Used to determine if a tile is marked as movable, a block will be considered movable, if...
 *
 * 1. The Tile or its super classes have been white listed with whiteListTileEntity.
 *
 * 2. The Tile has been register with the IMC ( which basically calls whiteListTileEntity. )
 *
 * 3. The Tile implements IMovableTile 4. A IMovableHandler is register that returns canHandle = true for the Tile
 * Entity Class
 *
 * IMC Example: FMLInterModComms.sendMessage( "appliedenergistics2", "movabletile", "appeng.common.AppEngTile" );
 *
 * The movement process is as follows,
 *
 * 1. IMovableTile.prepareToMove() or TileEntity.invalidate() depending on your opt-in method. 2. The tile will be
 * removed from the world. 3. Its world, coordinates will be changed. *** this can be overridden with a IMovableHandler
 * *** 4. It will then be re-added to the world, or a new world. 5. TileEntity.validate() 6. IMovableTile.doneMoving (
 * if you implemented IMovableTile )
 *
 * Please note, this is a 100% white list only feature, I will never opt in any non-vanilla, non-AE blocks. If you do
 * not want to support your tiles being moved, you don't have to do anything.
 *
 * I appreciate anyone that takes the effort to get their tiles to work with this system to create a better use
 * experience.
 *
 * If you need a build of deobf build of AE for testing, do not hesitate to ask.
 */
public interface IMovableRegistry
{

	/**
	 * Black list a block from movement, please only use this to prevent exploits.
	 *
	 * You can also use the IMC, FMLInterModComms.sendMessage( "appliedenergistics2", "whitelist-spatial",
	 * "appeng.common.AppEngTile" );
	 *
	 * @param blk block
	 */
	void blacklistBlock( Block blk );

	/**
	 * White list your tile entity with the registry.
	 *
	 * You can also use the IMC, FMLInterModComms.sendMessage( "appliedenergistics2", "blacklist-block-spatial", new
	 * ItemStack(...) );
	 *
	 * If you tile is handled with IMovableHandler or IMovableTile you do not need to white list it.
	 */
	void whiteListTileEntity( Class<? extends TileEntity> c );

	/**
	 * @param te to be moved tile entity
	 *
	 * @return true if the tile has accepted your request to move it
	 */
	boolean askToMove( TileEntity te );

	/**
	 * tells the tile you are done moving it.
	 *
	 * @param te moved tile entity
	 */
	void doneMoving( TileEntity te );

	/**
	 * add a new handler movable handler.
	 *
	 * @param handler moving handler
	 */
	void addHandler( IMovableHandler handler );

	/**
	 * handlers are used to perform movement, this allows you to override AE's internal version.
	 *
	 * only valid after askToMove(...) = true
	 *
	 * @param te tile entity
	 *
	 * @return moving handler of tile entity
	 */
	IMovableHandler getHandler( TileEntity te );

	/**
	 * @return a copy of the default handler
	 */
	IMovableHandler getDefaultHandler();

	/**
	 * @param blk block
	 *
	 * @return true if this block is blacklisted
	 */
	boolean isBlacklisted( Block blk );
}