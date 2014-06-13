package appeng.me.cluster.implementations;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.spatial.TileSpatialPylon;

public class SpatialPylonCalculator extends MBCalculator
{

	TileSpatialPylon tqb;

	public SpatialPylonCalculator(IAEMultiBlock t) {
		super( t );
		tqb = (TileSpatialPylon) t;
	}

	@Override
	public boolean isValidTile(TileEntity te)
	{
		return te instanceof TileSpatialPylon;
	}

	@Override
	public boolean checkMultiblockScale(WorldCoord min, WorldCoord max)
	{
		return (min.x == max.x && min.y == max.y && min.z != max.z) || (min.x == max.x && min.y != max.y && min.z == max.z) || (min.x != max.x && min.y == max.y && min.z == max.z);
	}

	@Override
	public void updateTiles(IAECluster cl, World w, WorldCoord min, WorldCoord max)
	{
		SpatialPylonCluster c = (SpatialPylonCluster) cl;

		for (int x = min.x; x <= max.x; x++)
		{
			for (int y = min.y; y <= max.y; y++)
			{
				for (int z = min.z; z <= max.z; z++)
				{
					TileSpatialPylon te = (TileSpatialPylon) w.getTileEntity( x, y, z );
					te.updateStatus( c );
					c.line.add( (te) );
				}
			}
		}

	}

	@Override
	public IAECluster createCluster(World w, WorldCoord min, WorldCoord max)
	{
		return new SpatialPylonCluster( new DimensionalCoord( w, min.x, min.y, min.z ), new DimensionalCoord( w, max.x, max.y, max.z ) );
	}

	@Override
	public void disconnect()
	{
		tqb.disconnect(true);
	}

	@Override
	public boolean verifyInternalStructure(World w, WorldCoord min, WorldCoord max)
	{

		for (int x = min.x; x <= max.x; x++)
		{
			for (int y = min.y; y <= max.y; y++)
			{
				for (int z = min.z; z <= max.z; z++)
				{
					IAEMultiBlock te = (IAEMultiBlock) w.getTileEntity( x, y, z );

					if ( !te.isValid() )
						return false;

				}
			}
		}

		return true;
	}

}
