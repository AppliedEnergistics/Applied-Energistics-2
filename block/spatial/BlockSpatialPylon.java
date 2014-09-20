package appeng.block.spatial;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderSpatialPylon;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.spatial.TileSpatialPylon;

public class BlockSpatialPylon extends AEBaseBlock
{

	public BlockSpatialPylon() {
		super( BlockSpatialPylon.class, AEGlassMaterial.instance );
		setfeature( EnumSet.of( AEFeature.SpatialIO ) );
		setTileEntity( TileSpatialPylon.class );
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block junk)
	{
		TileSpatialPylon tsp = getTileEntity( w, x, y, z );
		if ( tsp != null )
			tsp.onNeighborBlockChange();
	}

	@Override
	public int getLightValue(IBlockAccess w, int x, int y, int z)
	{
		TileSpatialPylon tsp = getTileEntity( w, x, y, z );
		if ( tsp != null )
			return tsp.getLightValue();
		return super.getLightValue( w, x, y, z );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderSpatialPylon.class;
	}

}
