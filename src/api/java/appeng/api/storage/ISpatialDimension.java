package appeng.api.storage;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISpatialDimension
{
	World getWorld();
	
	int createNewCellStorage( EntityPlayer owner );
	void deleteCellStorage( int cellStorageId );
	
	UUID getCellStorageOwner ( int cellStorageId );
	BlockPos getCellStorageOffset( int cellStorageId );	
}
