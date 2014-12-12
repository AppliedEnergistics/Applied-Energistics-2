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
	 * @param tile tile entity
	 * @return true if it can handle moving
	 */
	boolean canHandle(Class<? extends TileEntity> myClass, TileEntity tile);

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
	 * @param tile to be moved tile
	 * @param world world of tile
	 * @param x x coord of tile
	 * @param y y coord of tile
	 * @param z z coord of tile
	 */
	void moveTile(TileEntity tile, World world, int x, int y, int z);

}