
package appeng.api.storage;


import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public interface ISpatialDimension
{
	World getWorld();

	int createNewCellDimension( BlockPos contentSize, int playerId );

	void deleteCellDimension( int cellDimId );

	boolean isCellDimension( int cellDimID );

	int getCellDimensionOwner( int cellDimId );

	BlockPos getCellDimensionOrigin( int cellDimId );

	BlockPos getCellContentSize( int cellDimId );
}
