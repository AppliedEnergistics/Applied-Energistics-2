package appeng.client.render.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;

public class RenderBlockEnergyCube extends BaseBlockRender
{

	public RenderBlockEnergyCube() {
		super( false, 20 );
	}

	@Override
	public void renderInventory(AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		IAEItemPowerStorage myItem = (IAEItemPowerStorage) is.getItem();
		double internalCurrentPower = myItem.getAECurrentPower( is );
		double internalMaxPower = myItem.getAEMaxPower( is );

		int meta = (int) (8.0 * (internalCurrentPower / internalMaxPower));

		if ( meta > 7 )
			meta = 7;
		if ( meta < 0 )
			meta = 0;

		renderer.setOverrideBlockTexture( blk.getIcon( 0, meta ) );
		super.renderInventory( blk, is, renderer, type, obj );
		renderer.setOverrideBlockTexture( null );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock blk, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		int meta = world.getBlockMetadata( x, y, z );

		renderer.overrideBlockTexture = blk.getIcon( 0, meta );
		boolean out = renderer.renderStandardBlock( blk, x, y, z );
		renderer.overrideBlockTexture = null;

		return out;
	}
}
