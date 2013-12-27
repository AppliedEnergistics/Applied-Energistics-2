package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;

public class RenderNull extends BaseBlockRender
{

	public RenderNull() {
		super( false, 20 );
	}

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack is, RenderBlocks renderer)
	{

	}

	@Override
	public boolean renderInWorld(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		return true;
	}

}
