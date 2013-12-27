package appeng.client.render.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.tile.AEBaseTile;
import appeng.tile.grindstone.TileCrank;

public class RenderBlockCrank extends BaseBlockRender
{

	public RenderBlockCrank() {
		super( true, 60 );
	}

	@Override
	public void renderInventory(AEBaseBlock blk, ItemStack is, RenderBlocks renderer)
	{
		renderer.renderAllFaces = true;

		renderer.setRenderBounds( 0.5D - 0.05, 0.5D - 0.5, 0.5D - 0.05, 0.5D + 0.05, 0.5D + 0.3, 0.5D + 0.05 );
		super.renderInventory( blk, is, renderer );

		renderer.setRenderBounds( 0.70D - 0.15, 0.75D - 0.05, 0.5D - 0.05, 0.70D + 0.28, 0.75D + 0.05, 0.5D + 0.05 );
		super.renderInventory( blk, is, renderer );

		renderer.renderAllFaces = false;
	}

	@Override
	public boolean renderInWorld(AEBaseBlock imb, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		return true;
	}

	@Override
	public void renderTile(AEBaseBlock blk, AEBaseTile tile, Tessellator tess, double x, double y, double z, float f, RenderBlocks rbinstance)
	{
		TileCrank tc = (TileCrank) tile;
		if ( tc.getUp() == null || tc.getUp() == ForgeDirection.UNKNOWN )
			return;

		Minecraft.getMinecraft().getTextureManager().bindTexture( TextureMap.locationBlocksTexture );
		RenderHelper.disableStandardItemLighting();

		if ( Minecraft.isAmbientOcclusionEnabled() )
			GL11.glShadeModel( GL11.GL_SMOOTH );
		else
			GL11.glShadeModel( GL11.GL_FLAT );

		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );

		applyTESRRotation( x, y, z, tile.getForward(), tile.getUp() );

		GL11.glTranslated( 0.5, 0, 0.5 );
		GL11.glRotatef( tc.visibleRotation, 0, 1, 0 );
		GL11.glTranslated( -0.5, 0, -0.5 );

		tess.setTranslation( -tc.xCoord, -tc.yCoord, -tc.zCoord );
		tess.startDrawingQuads();
		rbinstance.renderAllFaces = true;
		rbinstance.blockAccess = tc.worldObj;

		rbinstance.setRenderBounds( 0.5D - 0.05, 0.5D - 0.5, 0.5D - 0.05, 0.5D + 0.05, 0.5D + 0.1, 0.5D + 0.05 );

		rbinstance.renderStandardBlock( blk, tc.xCoord, tc.yCoord, tc.zCoord );

		rbinstance.setRenderBounds( 0.70D - 0.15, 0.55D - 0.05, 0.5D - 0.05, 0.70D + 0.15, 0.55D + 0.05, 0.5D + 0.05 );

		rbinstance.renderStandardBlock( blk, tc.xCoord, tc.yCoord, tc.zCoord );

		tess.draw();
		tess.setTranslation( 0, 0, 0 );
		RenderHelper.enableStandardItemLighting();
	}

}
