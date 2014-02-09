package appeng.block.spatial;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.tile.spatial.TileSpatialIOPort;

public class BlockSpatialIOPort extends AEBaseBlock
{

	public BlockSpatialIOPort() {
		super( BlockSpatialIOPort.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.SpatialIO ) );
		setTileEntiy( TileSpatialIOPort.class );
	}

	@Override
	public final void onNeighborBlockChange(World w, int x, int y, int z, Block junk)
	{
		TileSpatialIOPort te = getTileEntity( w, x, y, z );
		if ( te != null )
			te.updateRedstoneState();
	}

}
