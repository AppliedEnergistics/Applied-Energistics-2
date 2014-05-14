package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;

public class RenderBlockAssembler extends BaseBlockRender
{

	public RenderBlockAssembler() {
		super( false, 20 );
	}

	@Override
	public void renderInventory(AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		renderer.setOverrideBlockTexture( blk.getIcon( 0, 0 ) );
		super.renderInventory( blk, is, renderer, type, obj );
		renderer.setOverrideBlockTexture( null );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock blk, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		renderer.overrideBlockTexture = blk.getIcon( 0, 0 );
		boolean out = renderer.renderStandardBlock( blk, x, y, z );
		renderer.overrideBlockTexture = null;

		return out;
	}
}
