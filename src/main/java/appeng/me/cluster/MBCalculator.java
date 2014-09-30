package appeng.me.cluster;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.util.Platform;

public abstract class MBCalculator
{

	final private IAEMultiBlock target;

	public MBCalculator(IAEMultiBlock t) {
		target = t;
	}

	/**
	 * check if the tile entities are correct for the structure.
	 * 
	 * @param te to be checked tile entity
	 * @return true if tile entity is valid for structure
	 */
	public abstract boolean isValidTile(TileEntity te);

	/**
	 * construct the correct cluster, usually very simple.
	 * 
	 * @param w world
	 * @param min min world coord
	 * @param max max world coord
	 * @return created cluster
	 */
	public abstract IAECluster createCluster(World w, WorldCoord min, WorldCoord max);

	/**
	 * configure the multi-block tiles, most of the important stuff is in here.
	 * 
	 * @param c updated cluster
	 * @param w in world
	 * @param min min world coord
	 * @param max max world coord
	 */
	public abstract void updateTiles(IAECluster c, World w, WorldCoord min, WorldCoord max);

	/**
	 * disassembles the multi-block.
	 */
	public abstract void disconnect();

	/**
	 * verify if the structure is the correct dimensions, or size
	 * 
	 * @param min min world coord
	 * @param max max world coord
	 * @return true if structure has correct dimensions or size
	 */
	public abstract boolean checkMultiblockScale(WorldCoord min, WorldCoord max);

	public boolean isValidTileAt(World w, int x, int y, int z)
	{
		return isValidTile( w.getTileEntity( x, y, z ) );
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
					IAECluster cluster = target.getCluster();
					if ( cluster == null )
					{
						updateTiles( c, w, min, max );

						updateGrid = true;
					}
					else
						c = cluster;

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

	public boolean verifyUnownedRegionInner(World w, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, ForgeDirection side)
	{
		switch (side)
		{
		case WEST:
			minX -= 1;
			maxX = minX;
			break;
		case EAST:
			maxX += 1;
			minX = maxX;
			break;
		case DOWN:
			minY -= 1;
			maxY = minY;
			break;
		case NORTH:
			maxZ += 1;
			minZ = maxZ;
			break;
		case SOUTH:
			minZ -= 1;
			maxZ = minZ;
			break;
		case UP:
			maxY += 1;
			minY = maxY;
			break;
		case UNKNOWN:
			return false;
		}

		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					TileEntity te = w.getTileEntity( x, y, z );
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
