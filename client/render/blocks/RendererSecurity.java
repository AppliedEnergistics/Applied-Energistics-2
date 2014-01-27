package appeng.client.render.blocks;

import java.util.EnumSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraTextures;
import appeng.tile.misc.TileSecurity;

public class RendererSecurity extends BaseBlockRender
{

	public RendererSecurity() {
		super( false, 0 );
	}

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack is, RenderBlocks renderer)
	{
		renderer.overrideBlockTexture = ExtraTextures.getMissing();
		this.renderInvBlock( EnumSet.of( ForgeDirection.SOUTH ), block, Tessellator.instance, 0x000000, renderer );

		renderer.overrideBlockTexture = ExtraTextures.MEChest.getIcon();
		this.renderInvBlock( EnumSet.of( ForgeDirection.UP ), block, Tessellator.instance, 0xffffff, renderer );

		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock imb, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		TileSecurity sp = imb.getTileEntity( world, x, y, z );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		ForgeDirection up = sp.getUp();

		preRenderInWorld( imb, world, x, y, z, renderer );

		boolean result = renderer.renderStandardBlock( imb, x, y, z );

		int b = world.getLightBrightnessForSkyBlocks( x + up.offsetX, y + up.offsetY, z + up.offsetZ, 0 );
		if ( sp.isActive() )
		{
			b = 15 << 20 | 15 << 4;
		}

		Tessellator.instance.setBrightness( b );
		Tessellator.instance.setColorOpaque_I( 0xffffff );
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		Icon ico = sp.isActive() ? ExtraTextures.BlockMESecurityOn.getIcon() : ExtraTextures.MEChest.getIcon();
		renderFace( x, y, z, imb, ico, renderer, up );

		renderer.overrideBlockTexture = null;
		postRenderInWorld( renderer );

		return result;
	}
}
