
package appeng.capabilities;


import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import appeng.api.storage.ISpatialDimension;


class NullSpatialDimension implements ISpatialDimension
{
	@Override
	public int createNewCellDimension( Vec3i size, int owner )
	{
		return -1;
	}

	@Override
	public void deleteCellDimension( int cellStorageId )
	{
	}

	@Override
	public int getCellDimensionOwner( int cellStorageId )
	{
		return -1;
	}

	@Override
	public BlockPos getCellDimensionOrigin( int cellStorageId )
	{
		return null;
	}

	@Override
	public World getWorld()
	{
		return null;
	}

	@Override
	public boolean isCellDimension( int cellDimID )
	{
		return false;
	}

	@Override
	public Vec3i getCellContentSize( int cellDimId )
	{
		return null;
	}
}
