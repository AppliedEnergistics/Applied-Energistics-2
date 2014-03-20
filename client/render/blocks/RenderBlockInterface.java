package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.texture.ExtraTextures;
import appeng.tile.misc.TileInterface;

public class RenderBlockInterface extends BaseBlockRender
{

	public RenderBlockInterface() {
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		TileInterface ti = block.getTileEntity( world, x, y, z );

		BlockRenderInfo info = block.getRendererInstance();

		if ( ti.getForward() != ForgeDirection.UNKNOWN )
		{
			IIcon side = ExtraTextures.BlockInterfaceAlternateArrow.getIcon();
			info.setTemporaryRenderIcons( ExtraTextures.BlockInterfaceAlternate.getIcon(), block.getIcon( 0, 0 ), side, side, side, side );
		}

		boolean fz = super.renderInWorld( block, world, x, y, z, renderer );

		info.setTemporaryRenderIcon( null );

		return fz;
	}
}
