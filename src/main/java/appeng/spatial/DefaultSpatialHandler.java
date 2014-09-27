package appeng.spatial;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import appeng.api.movable.IMovableHandler;

public class DefaultSpatialHandler implements IMovableHandler
{

	@Override
	public void moveTile(TileEntity te, World w, int x, int y, int z)
	{

		te.setWorldObj( w );
		te.xCoord = x;
		te.yCoord = y;
		te.zCoord = z;

		Chunk c = w.getChunkFromBlockCoords( x, z );
		c.func_150812_a( x & 0xF, y, z & 0xF, te );
		// c.setChunkBlockTileEntity( x & 0xF, y, z & 0xF, te );

		if ( c.isChunkLoaded )
		{
			w.addTileEntity( te );
			w.markBlockForUpdate( x, y, z );
		}
	}

	/**
	 * never called for the default.
	 * 
	 * @param tile tile entity
	 * @return true
	 */
	@Override
	public boolean canHandle(Class<? extends TileEntity> myClass, TileEntity tile)
	{
		return true;
	}

}
