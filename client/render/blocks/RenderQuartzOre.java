package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.block.AEBaseBlock;
import appeng.block.solids.OreQuartz;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;

public class RenderQuartzOre extends BaseBlockRender
{

	public RenderQuartzOre() {
		super( false, 20 );
	}

	@Override
	public void renderInventory(AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		super.renderInventory( blk, is, renderer, type, obj );
		blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.OreQuartzStone.getIcon() );
		super.renderInventory( blk, is, renderer, type, obj );
		blk.getRendererInstance().setTemporaryRenderIcon( null );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		OreQuartz blk = (OreQuartz) block;
		blk.enhanceBrightness = true;
		super.renderInWorld( block, world, x, y, z, renderer );
		blk.enhanceBrightness = false;

		blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.OreQuartzStone.getIcon() );
		boolean out = super.renderInWorld( block, world, x, y, z, renderer );
		blk.getRendererInstance().setTemporaryRenderIcon( null );

		return out;
	}
}
