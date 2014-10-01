package appeng.client.render.blocks;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.block.misc.BlockCharger;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.AELog;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;

public class RenderBlockCharger extends BaseBlockRender
{

	public RenderBlockCharger() {
		super( true, 30 );
	}

	@Override
	public void renderInventory(AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		Tessellator tess = Tessellator.instance;

		renderer.renderAllFaces = true;
		setInvRenderBounds( renderer, 6, 1, 0, 10, 15, 2 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcons( ExtraBlockTextures.BlockChargerInside.getIcon(), null, null, null, null, null );

		setInvRenderBounds( renderer, 2, 0, 2, 14, 3, 14 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		setInvRenderBounds( renderer, 3, 3, 3, 13, 4, 13 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( null );

		blk.getRendererInstance().setTemporaryRenderIcons( null, ExtraBlockTextures.BlockChargerInside.getIcon(), null, null, null, null );

		setInvRenderBounds( renderer, 2, 13, 2, 14, 16, 14 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		setInvRenderBounds( renderer, 3, 12, 3, 13, 13, 13 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		renderer.renderAllFaces = false;
		blk.getRendererInstance().setTemporaryRenderIcon( null );

	}

	@Override
	public boolean renderInWorld(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		preRenderInWorld( block, world, x, y, z, renderer );

		BlockCharger blk = (BlockCharger) block;

		IOrientable te = getOrientable( block, world, x, y, z );

		ForgeDirection fdy = te.getUp();
		ForgeDirection fdz = te.getForward();
		ForgeDirection fdx = Platform.crossProduct( fdz, fdy ).getOpposite();

		renderer.renderAllFaces = true;
		renderBlockBounds( renderer, 6, 1, 0, 10, 15, 2, fdx, fdy, fdz );
		boolean out = renderer.renderStandardBlock( blk, x, y, z );

		blk.getRendererInstance().setTemporaryRenderIcons( ExtraBlockTextures.BlockChargerInside.getIcon(), null, null, null, null, null );

		renderBlockBounds( renderer, 2, 0, 2, 14, 3, 14, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		renderBlockBounds( renderer, 3, 3, 3, 13, 4, 13, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		blk.getRendererInstance().setTemporaryRenderIcon( null );

		blk.getRendererInstance().setTemporaryRenderIcons( null, ExtraBlockTextures.BlockChargerInside.getIcon(), null, null, null, null );

		renderBlockBounds( renderer, 2, 13, 2, 14, 16, 14, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		renderBlockBounds( renderer, 3, 12, 3, 13, 13, 13, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		renderer.renderAllFaces = false;
		blk.getRendererInstance().setTemporaryRenderIcon( null );

		postRenderInWorld( renderer );
		return out;
	}

	@Override
	public void renderTile(AEBaseBlock block, AEBaseTile tile, Tessellator tess, double x, double y, double z, float f, RenderBlocks renderer)
	{
		ItemStack sis = null;
		if ( tile instanceof IInventory )
			sis = ((IInventory) tile).getStackInSlot( 0 );

		if ( sis != null )
		{
			GL11.glPushMatrix();
			applyTESRRotation( x, y, z, tile.getForward(), tile.getUp() );

			try
			{
				GL11.glTranslatef( 0.5f, 0.45f, 0.5f );
				GL11.glScalef( 1.0f / 1.1f, 1.0f / 1.1f, 1.0f / 1.1f );
				GL11.glScalef( 1.0f, 1.0f, 1.0f );

				Block blk = Block.getBlockFromItem( sis.getItem() );
				if ( sis.getItemSpriteNumber() == 0 && block != null && RenderBlocks.renderItemIn3d( blk.getRenderType() ) )
				{
					GL11.glRotatef( 25.0f, 1.0f, 0.0f, 0.0f );
					GL11.glRotatef( 15.0f, 0.0f, 1.0f, 0.0f );
					GL11.glRotatef( 30.0f, 0.0f, 1.0f, 0.0f );
				}

				int br = tile.getWorldObj().getLightBrightnessForSkyBlocks( tile.xCoord, tile.yCoord, tile.zCoord, 0 );// << 20 | light << 4;
				int var11 = br % 65536;
				int var12 = br / 65536;
				OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11, var12 );

				GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL12.GL_RESCALE_NORMAL );
				tess.setColorOpaque_F( 1.0f, 1.0f, 1.0f );

				doRenderItem( sis, tile );
			}
			catch (Exception err)
			{
				AELog.error( err );
			}

			GL11.glPopMatrix();
		}
	}

}
