package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.misc.TileQuartzGrowthAccelerator;

public class RenderBlockQuartzAccelerator extends BaseBlockRender
{

	public RenderBlockQuartzAccelerator() {
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock blk, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		TileEntity te = world.getTileEntity( x, y, z );
		if ( te instanceof TileQuartzGrowthAccelerator )
		{
			if ( ((TileQuartzGrowthAccelerator) te).hasPower )
			{
				IIcon top_Bottom = ExtraBlockTextures.BlockQuartzGrowthAcceleratorOn.getIcon();
				IIcon side = ExtraBlockTextures.BlockQuartzGrowthAcceleratorSideOn.getIcon();
				blk.getRendererInstance().setTemporaryRenderIcons( top_Bottom, top_Bottom, side, side, side, side );
			}
		}

		boolean out = super.renderInWorld( blk, world, x, y, z, renderer );
		blk.getRendererInstance().setTemporaryRenderIcon( null );

		return out;
	}
}
