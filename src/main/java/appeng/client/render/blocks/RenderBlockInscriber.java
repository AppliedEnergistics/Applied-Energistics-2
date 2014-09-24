package appeng.client.render.blocks;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.block.misc.BlockInscriber;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.AELog;
import appeng.recipes.handlers.Inscribe.InscriberRecipe;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;

public class RenderBlockInscriber extends BaseBlockRender
{

	public RenderBlockInscriber() {
		super( true, 30 );
	}

	@Override
	public void renderInventory(AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		Tessellator tess = Tessellator.instance;

		renderer.renderAllFaces = true;
		setInvRenderBounds( renderer, 6, 1, 0, 10, 15, 2 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		// sides...
		setInvRenderBounds( renderer, 3, 1, 0, 13, 15, 3 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		setInvRenderBounds( renderer, 0, 1, 0, 3, 15, 16 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		setInvRenderBounds( renderer, 13, 1, 0, 16, 15, 16 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		setInvRenderBounds( renderer, 1, 0, 1, 15, 2, 15 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		setInvRenderBounds( renderer, 1, 14, 1, 15, 16, 15 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockInscriberInside.getIcon() );

		// press
		setInvRenderBounds( renderer, 3, 2, 3, 13, 3, 13 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		setInvRenderBounds( renderer, 3, 13, 3, 13, 15, 13 );
		renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( null );

		renderer.renderAllFaces = false;
		// blk.getRendererInstance().setTemporaryRenderIcon( null );

	}

	@Override
	public boolean renderInWorld(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		preRenderInWorld( block, world, x, y, z, renderer );

		BlockInscriber blk = (BlockInscriber) block;

		IOrientable te = getOrientable( block, world, x, y, z );

		ForgeDirection fdy = te.getUp();
		ForgeDirection fdz = te.getForward();
		ForgeDirection fdx = Platform.crossProduct( fdz, fdy ).getOpposite();

		renderer.renderAllFaces = true;

		// sides...
		renderBlockBounds( renderer, 3, 1, 0, 13, 15, 3, fdx, fdy, fdz );
		boolean out = renderer.renderStandardBlock( blk, x, y, z );

		renderBlockBounds( renderer, 0, 1, 0, 3, 15, 16, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		renderBlockBounds( renderer, 13, 1, 0, 16, 15, 16, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		// top bottom..
		renderBlockBounds( renderer, 1, 0, 1, 15, 4, 15, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		renderBlockBounds( renderer, 1, 12, 1, 15, 16, 15, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		blk.getRendererInstance().setTemporaryRenderIcon( null );

		renderer.renderAllFaces = false;
		blk.getRendererInstance().setTemporaryRenderIcon( null );

		postRenderInWorld( renderer );
		return out;
	}

	@Override
	public void renderTile(AEBaseBlock block, AEBaseTile tile, Tessellator tess, double x, double y, double z, float f, RenderBlocks renderer)
	{
		TileInscriber inv = (TileInscriber) tile;

		GL11.glPushMatrix();
		applyTESRRotation( x, y, z, tile.getForward(), tile.getUp() );

		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		GL11.glDisable( GL11.GL_LIGHTING );
		GL11.glDisable( GL12.GL_RESCALE_NORMAL );

		Minecraft mc = Minecraft.getMinecraft();
		mc.renderEngine.bindTexture( TextureMap.locationBlocksTexture );

		int light = tile.getWorldObj().getLightBrightnessForSkyBlocks( tile.xCoord, tile.yCoord, tile.zCoord, 0 );
		int br = light;// << 20 | light << 4;
		int var11 = br % 65536;
		int var12 = br / 65536;
		OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11, var12 );

		float TwoPx = 2.0f / 16.0f;
		float middle = 0.5f;

		float press = 0.2f;
		float base = 0.4f;

		long lprogress = 0;
		if ( inv.smash )
		{
			long currentTime = System.currentTimeMillis();
			lprogress = currentTime - inv.clientStart;
			if ( lprogress > 800 )
				inv.smash = false;
		}

		float rprogress = (float) (lprogress % 800) / 400.0f;
		float progress = rprogress;

		if ( progress > 1.0f )
			progress = 1.0f - (progress - 1.0f);
		press -= progress / 5.0f;

		IIcon ic = ExtraBlockTextures.BlockInscriberInside.getIcon();
		tess.startDrawingQuads();

		middle += 0.02f;
		tess.addVertexWithUV( TwoPx, middle + press, TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 2 ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle + press, TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 2 ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle + press, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 13 ) );
		tess.addVertexWithUV( TwoPx, middle + press, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 13 ) );

		tess.addVertexWithUV( TwoPx, middle + press, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle + press, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle + base, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 - 16 * (press - base) ) );
		tess.addVertexWithUV( TwoPx, middle + base, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 - 16 * (press - base) ) );

		middle -= 2.0f * 0.02f;
		tess.addVertexWithUV( 1.0 - TwoPx, middle - press, TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 2 ) );
		tess.addVertexWithUV( TwoPx, middle - press, TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 2 ) );
		tess.addVertexWithUV( TwoPx, middle - press, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 13 ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle - press, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 13 ) );

		tess.addVertexWithUV( 1.0 - TwoPx, middle - press, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 ) );
		tess.addVertexWithUV( TwoPx, middle - press, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 ) );
		tess.addVertexWithUV( TwoPx, middle - base, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 - 16 * (press - base) ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle + -base, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 - 16 * (press - base) ) );

		tess.draw();

		GL11.glPopMatrix();

		int items = 0;
		if ( inv.getStackInSlot( 0 ) != null )
			items++;
		if ( inv.getStackInSlot( 1 ) != null )
			items++;
		if ( inv.getStackInSlot( 2 ) != null )
			items++;

		if ( rprogress > 1.0f || items == 0 )
		{
			ItemStack is = inv.getStackInSlot( 3 );

			if ( is == null )
			{
				InscriberRecipe ir = inv.getTask();
				if ( ir != null )
					is = ir.output.copy();
			}

			renderItem( is, 0.0f, block, tile, tess, x, y, z, f, renderer );
		}
		else
		{
			renderItem( inv.getStackInSlot( 0 ), press, block, tile, tess, x, y, z, f, renderer );
			renderItem( inv.getStackInSlot( 1 ), -press, block, tile, tess, x, y, z, f, renderer );
			renderItem( inv.getStackInSlot( 2 ), 0.0f, block, tile, tess, x, y, z, f, renderer );
		}

	}

	public void renderItem(ItemStack sis, float o, AEBaseBlock block, AEBaseTile tile, Tessellator tess, double x, double y, double z, float f,
			RenderBlocks renderer)
	{
		if ( sis != null )
		{
			sis = sis.copy();
			GL11.glPushMatrix();
			applyTESRRotation( x, y, z, tile.getForward(), tile.getUp() );

			try
			{
				GL11.glTranslatef( 0.5f, 0.5f + o, 0.5f );
				GL11.glScalef( 1.0f / 1.1f, 1.0f / 1.1f, 1.0f / 1.1f );
				GL11.glScalef( 1.0f, 1.0f, 1.0f );

				Block blk = Block.getBlockFromItem( sis.getItem() );
				if ( sis.getItemSpriteNumber() == 0 && block != null && RenderBlocks.renderItemIn3d( blk.getRenderType() ) )
				{
					GL11.glRotatef( 25.0f, 1.0f, 0.0f, 0.0f );
					GL11.glRotatef( 15.0f, 0.0f, 1.0f, 0.0f );
					GL11.glRotatef( 30.0f, 0.0f, 1.0f, 0.0f );
				}

				GL11.glRotatef( 90.0f, 1, 0, 0 );

				int light = tile.getWorldObj().getLightBrightnessForSkyBlocks( tile.xCoord, tile.yCoord, tile.zCoord, 0 );
				int br = light;// << 20 | light << 4;
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
