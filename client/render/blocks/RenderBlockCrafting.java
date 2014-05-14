package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraTextures;
import appeng.tile.crafting.TileCraftingTile;

public class RenderBlockCrafting extends BaseBlockRender
{

	public RenderBlockCrafting() {
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
		TileCraftingTile ct = blk.getTileEntity( world, x, y, z );
		if ( ct != null && ct.isFormed() )
		{
			renderer.overrideBlockTexture = ExtraTextures.BlockControllerConflict.getIcon();
			boolean out = renderer.renderStandardBlock( blk, x, y, z );
			renderer.overrideBlockTexture = null;

			return out;
		}
		else
		{
			renderer.overrideBlockTexture = blk.getIcon( 0, 0 );
			boolean out = renderer.renderStandardBlock( blk, x, y, z );
			renderer.overrideBlockTexture = null;

			return out;
		}
	}
}
