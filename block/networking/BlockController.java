package appeng.block.networking;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockController;
import appeng.core.features.AEFeature;
import appeng.tile.networking.TileController;

public class BlockController extends AEBaseBlock
{

	public BlockController() {
		super( BlockController.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setTileEntiy( TileController.class );
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, int id_junk)
	{
		TileController tc = getTileEntity( w, x, y, z );
		if ( tc != null )
			tc.onNeighborChange( false );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockController.class;
	}

}
