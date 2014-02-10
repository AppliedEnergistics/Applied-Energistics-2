package appeng.client.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;

public class AppEngRenderItem extends RenderItem
{

	public IAEItemStack aestack;

	private void renderQuad(Tessellator par1Tessellator, int par2, int par3, int par4, int par5, int par6)
	{
		par1Tessellator.startDrawingQuads();
		par1Tessellator.setColorOpaque_I( par6 );
		par1Tessellator.addVertex( par2 + 0, par3 + 0, 0.0D );
		par1Tessellator.addVertex( par2 + 0, par3 + par5, 0.0D );
		par1Tessellator.addVertex( par2 + par4, par3 + par5, 0.0D );
		par1Tessellator.addVertex( par2 + par4, par3 + 0, 0.0D );
		par1Tessellator.draw();
	}

	@Override
	public void renderItemOverlayIntoGUI(FontRenderer par1FontRenderer, TextureManager par2RenderEngine, ItemStack par3ItemStack, int par4, int par5)
	{
		this.renderItemOverlayIntoGUI( par1FontRenderer, par2RenderEngine, par3ItemStack, par4, par5, (String) null );
	}

	@Override
	public void renderItemOverlayIntoGUI(FontRenderer par1FontRenderer, TextureManager par2RenderEngine, ItemStack is, int par4, int par5, String par6Str)
	{
		if ( is != null )
		{
			float ScaleFactor = AEConfig.instance.useTerminalUseLargeFont() ? 0.85f : 0.5f;
			float RScaleFactor = 1.0f / ScaleFactor;
			int offset = AEConfig.instance.useTerminalUseLargeFont() ? 0 : -1;

			if ( is.isItemDamaged() )
			{
				int k = (int) Math.round( 13.0D - is.getItemDamageForDisplay() * 13.0D / is.getMaxDamage() );
				int l = (int) Math.round( 255.0D - is.getItemDamageForDisplay() * 255.0D / is.getMaxDamage() );
				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL11.GL_DEPTH_TEST );
				GL11.glDisable( GL11.GL_TEXTURE_2D );
				GL11.glDisable( GL11.GL_BLEND );
				Tessellator tessellator = Tessellator.instance;
				int i1 = 255 - l << 16 | l << 8;
				int j1 = (255 - l) / 4 << 16 | 16128;
				this.renderQuad( tessellator, par4 + 2, par5 + 13, 13, 2, 0 );
				this.renderQuad( tessellator, par4 + 2, par5 + 13, 12, 1, j1 );
				this.renderQuad( tessellator, par4 + 2, par5 + 13, k, 1, i1 );
				GL11.glEnable( GL11.GL_TEXTURE_2D );
				GL11.glEnable( GL11.GL_LIGHTING );
				GL11.glEnable( GL11.GL_DEPTH_TEST );
				GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
			}

			if ( is.stackSize == 0 )
			{
				String var6 = AEConfig.instance.useTerminalUseLargeFont() ? "+" : "Craft";
				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL11.GL_DEPTH_TEST );
				GL11.glPushMatrix();
				GL11.glScaled( ScaleFactor, ScaleFactor, ScaleFactor );
				int X = (int) (((float) par4 + offset + 16.0f - par1FontRenderer.getStringWidth( var6 ) * ScaleFactor) * RScaleFactor);
				int Y = (int) (((float) par5 + offset + 16.0f - 7.0f * ScaleFactor) * RScaleFactor);
				par1FontRenderer.drawStringWithShadow( var6, X, Y, 16777215 );
				GL11.glPopMatrix();
				GL11.glEnable( GL11.GL_LIGHTING );
				GL11.glEnable( GL11.GL_DEPTH_TEST );
			}

			long amount = aestack != null ? aestack.getStackSize() : is.stackSize;
			if ( amount > 999999999999L )
				amount = 999999999999L;

			if ( amount != 0 )
			{
				String var6 = "" + Math.abs( amount );

				if ( AEConfig.instance.useTerminalUseLargeFont() )
				{
					if ( amount > 999999999 )
					{
						var6 = "" + (int) Math.floor( amount / 1000000000.0 ) + "B";
					}
					else if ( amount > 99999999 )
					{
						var6 = "." + (int) Math.floor( amount / 100000000.0 ) + "B";
					}
					else if ( amount > 999999 )
					{
						var6 = "" + (int) Math.floor( amount / 1000000.0 ) + "M";
					}
					else if ( amount > 99999 )
					{
						var6 = "." + (int) Math.floor( amount / 100000.0 ) + "M";
					}
					else if ( amount > 999 )
					{
						var6 = "" + (int) Math.floor( amount / 1000.0 ) + "K";
					}
				}
				else
				{
					if ( amount > 999999999 )
					{
						var6 = "" + (int) Math.floor( amount / 1000000000.0 ) + "B";
					}
					else if ( amount > 999999999 )
					{
						var6 = "" + (int) Math.floor( amount / 1000000000.0 ) + "B";
					}
					else if ( amount > 999999 )
					{
						var6 = "" + (int) Math.floor( amount / 1000000.0 ) + "M";
					}
					else if ( amount > 9999 )
					{
						var6 = "" + (int) Math.floor( amount / 1000.0 ) + "K";
					}
				}

				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL11.GL_DEPTH_TEST );
				GL11.glPushMatrix();
				GL11.glScaled( ScaleFactor, ScaleFactor, ScaleFactor );
				int X = (int) (((float) par4 + offset + 16.0f - par1FontRenderer.getStringWidth( var6 ) * ScaleFactor) * RScaleFactor);
				int Y = (int) (((float) par5 + offset + 16.0f - 7.0f * ScaleFactor) * RScaleFactor);
				par1FontRenderer.drawStringWithShadow( var6, X, Y, 16777215 );
				GL11.glPopMatrix();
				GL11.glEnable( GL11.GL_LIGHTING );
				GL11.glEnable( GL11.GL_DEPTH_TEST );
			}
		}
	}
}
