package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderer;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;

public class RendererCableBus extends BaseBlockRender
{

	public RendererCableBus() {
		super( true, 30 );
	}

	@Override
	public void renderInventory(AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type)
	{
		// nothin.
	}

	@Override
	public boolean renderInWorld(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		AEBaseTile t = block.getTileEntity( world, x, y, z );

		if ( t instanceof TileCableBus )
		{
			BusRenderer.instance.renderer.renderAllFaces = true;
			BusRenderer.instance.renderer.blockAccess = renderer.blockAccess;
			((TileCableBus) t).cb.renderStatic( x, y, z );
			BusRenderer.instance.renderer.renderAllFaces = false;
		}

		return true;
	}

	@Override
	public void renderTile(AEBaseBlock block, AEBaseTile t, Tessellator tess, double x, double y, double z, float f, RenderBlocks renderer)
	{
		if ( t instanceof TileCableBus )
		{
			((TileCableBus) t).cb.renderDynamic( x, y, z );
		}
	}

}
