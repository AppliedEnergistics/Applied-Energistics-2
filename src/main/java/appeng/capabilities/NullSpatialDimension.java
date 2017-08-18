
package appeng.capabilities;


import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.storage.ISpatialDimension;


class NullSpatialDimension implements ISpatialDimension
{
	@Override
	public int createNewCellStorage( EntityPlayer owner )
	{
		return -1;
	}

	@Override
	public void deleteCellStorage( int cellStorageId )
	{
	}

	@Override
	public UUID getCellStorageOwner( int cellStorageId )
	{
		return null;
	}

	@Override
	public BlockPos getCellStorageOffset( int cellStorageId )
	{
		return null;
	}

	@Override
	public World getWorld()
	{
		return null;
	}
}
