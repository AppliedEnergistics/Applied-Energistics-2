package appeng.client.render.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.tile.AEBaseTile;

public class RenderStorageMonitor extends BaseBlockRender
{

	public RenderStorageMonitor() {
		super( true, 30 );
	}

	@Override
	public void renderTile(AEBaseBlock blk, AEBaseTile tile, Tessellator tess, double x, double y, double z, float f,
			RenderBlocks rinstance)
	{
		IPartStorageMonitor monitor = (IPartStorageMonitor) tile;
		IAEItemStack is = ((IAEItemStack) monitor.getDisplayed());

		if ( is != null && monitor.isPowered() )
		{
			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

			// applyTESRRotation( x, y, z, monitor.getForward(), monitor.getUp()
			// );

			GL11.glPushMatrix();
			try
			{
				ItemStack sis = is.getItemStack();
				sis.stackSize = 1;

				GL11.glTranslatef( 0.0f, -0.05f, -0.25f );
				GL11.glScalef( 1.0f / 1.5f, 1.0f / 1.5f, 1.0f / 1.5f );
				GL11.glScalef( 1.0f, -1.0f, 0.005f );

				int k = sis.itemID;
				Block block = (k < Block.blocksList.length ? Block.blocksList[k] : null);
				if ( sis.getItemSpriteNumber() == 0 && block != null
						&& RenderBlocks.renderItemIn3d( Block.blocksList[k].getRenderType() ) )
				{
					GL11.glRotatef( 25.0f, 1.0f, 0.0f, 0.0f );
					GL11.glRotatef( 15.0f, 0.0f, 1.0f, 0.0f );
					GL11.glRotatef( 30.0f, 0.0f, 1.0f, 0.0f );
				}
				int br = 16 << 20 | 16 << 4;
				int var11 = br % 65536;
				int var12 = br / 65536;
				OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11 * 0.8F, var12 * 0.8F );

				GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL12.GL_RESCALE_NORMAL );
				tess.setColorOpaque_F( 1.0f, 1.0f, 1.0f );

				doRenderItem( sis, tile );
			}
			catch (Exception err)
			{
				err.printStackTrace();
			}

			GL11.glPopMatrix();

			GL11.glTranslatef( 0.0f, 0.14f, -0.24f );
			GL11.glScalef( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );

			long qty = is.getStackSize();
			if ( qty > 999999999999L )
				qty = 999999999999L;

			String msg = Long.toString( qty );
			if ( qty > 1000000000 )
				msg = Long.toString( qty / 1000000000 ) + "B";
			else if ( qty > 1000000 )
				msg = Long.toString( qty / 1000000 ) + "M";
			else if ( qty > 9999 )
				msg = Long.toString( qty / 1000 ) + "K";

			int width = fr.getStringWidth( msg );
			GL11.glTranslatef( -0.5f * width, 0.0f, -1.0f );
			fr.drawString( msg, 0, 0, 0 );
		}
	}

}
