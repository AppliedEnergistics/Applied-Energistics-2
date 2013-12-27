package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;

public class RenderTinyTNT extends BaseBlockRender
{

	public RenderTinyTNT() {
		super( false, 0 );
	}

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack is, RenderBlocks renderer)
	{
		renderer.setRenderBounds( 0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f );
		super.renderInventory( block, is, renderer );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock imb, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		renderer.renderAllFaces = true;
		renderer.setRenderBounds( 0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f );
		boolean out = super.renderInWorld( imb, world, x, y, z, renderer );
		renderer.renderAllFaces = false;
		return out;
	}

}
