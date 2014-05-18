package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import appeng.block.AEBaseBlock;
import appeng.tile.AEBaseTile;

public class RenderBlockCraftingMonitor extends RenderBlockCrafting
{

	public RenderBlockCraftingMonitor() {
		super( true, 20 );
	}

	@Override
	public void renderTile(AEBaseBlock block, AEBaseTile tile, Tessellator tess, double x, double y, double z, float f, RenderBlocks renderer)
	{

	}

}
