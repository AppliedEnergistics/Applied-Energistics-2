package appeng.me.cluster;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.util.Platform;

public abstract class MBCalculator
{

	private IAEMultiBlock target;

	public MBCalculator(IAEMultiBlock t) {
		target = t;
	}

	/**
	 * check if the tile entities are correct for the structure.
	 * 
	 * @param te
	 * @return
	 */
	public abstract boolean isValidTile(TileEntity te);

	/**
	 * construct the correct cluster, usually very simple.
	 * 
	 * @param w
	 * @param min
	 * @param max
	 * @return
	 */
	public abstract IAECluster createCluster(World w, WorldCoord min, WorldCoord max);

	/**
	 * configure the mutli-block tiles, most of the important stuff is in here.
	 * 
	 * @param c
	 * @param w
	 * @param min
	 * @param max
	 */
	public abstract void updateTiles(IAECluster c, World w, WorldCoord min, WorldCoord max);

	/**
	 * disassembles the multi-block.
	 */
	public abstract void disconnect();

	/**
	 * verify if the structure is the correct dimentions, or size
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public abstract boolean checkMultiblockScale(WorldCoord min, WorldCoord max);

	public boolean isValidTileAt(World w, int x, int y, int z)
	{
		return isValidTile( w.getBlockTileEntity( x, y, z ) );
	}

	public void calculateMultiblock(World worldObj, WorldCoord loc)
	{
		if ( Platform.isClient() )
			return;

		try
		{
			WorldCoord min = loc.copy();
			WorldCoord max = loc.copy();

			World w = worldObj;

			// find size of MB structure...
			while (isValidTileAt( w, min.x - 1, min.y, min.z ))
				min.x--;
			while (isValidTileAt( w, min.x, min.y - 1, min.z ))
				min.y--;
			while (isValidTileAt( w, min.x, min.y, min.z - 1 ))
				min.z--;
			while (isValidTileAt( w, max.x + 1, max.y, max.z ))
				max.x++;
			while (isValidTileAt( w, max.x, max.y + 1, max.z ))
				max.y++;
			while (isValidTileAt( w, max.x, max.y, max.z + 1 ))
				max.z++;

			if ( checkMultiblockScale( min, max ) )
			{
				if ( verifyUnownedRegion( w, min, max ) )
				{
					IAECluster c = createCluster( w, min, max );

					try
					{
						if ( !verifyInternalStructure( worldObj, min, max ) )
						{
							disconnect();
							return;
						}
					}
					catch (Exception err)
					{
						disconnect();
						return;
					}

					boolean updateGrid = false;
					IAECluster clust = target.getCluster();
					if ( clust == null )
					{
						updateTiles( c, w, min, max );

						updateGrid = true;
					}
					else
						c = clust;

					c.updateStatus( updateGrid );
					return;
				}
			}
		}
		catch (Throwable err)
		{
			AELog.error( err );
		}

		disconnect();
	}

	public abstract boolean verifyInternalStructure(World worldObj, WorldCoord min, WorldCoord max);

	public boolean verifyUnownedRegionInner(World w, int minx, int miny, int minz, int maxx, int maxy, int maxz, ForgeDirection side)
	{
		switch (side)
		{
		case WEST:
			minx -= 1;
			maxx = minx;
			break;
		case EAST:
			maxx += 1;
			minx = maxx;
			break;
		case DOWN:
			miny -= 1;
			maxy = miny;
			break;
		case NORTH:
			maxz += 1;
			minz = maxz;
			break;
		case SOUTH:
			minz -= 1;
			maxz = minz;
			break;
		case UP:
			maxy += 1;
			miny = maxy;
			break;
		case UNKNOWN:
			return false;
		}

		for (int x = minx; x <= maxx; x++)
		{
			for (int y = miny; y <= maxy; y++)
			{
				for (int z = minz; z <= maxz; z++)
				{
					TileEntity te = w.getBlockTileEntity( x, y, z );
					if ( isValidTile( te ) )
						return true;
				}
			}
		}

		return false;
	}

	public boolean verifyUnownedRegion(World w, WorldCoord min, WorldCoord max)
	{
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			if ( verifyUnownedRegionInner( w, min.x, min.y, min.z, max.x, max.y, max.z, side ) )
				return false;

		return true;
	}

}
