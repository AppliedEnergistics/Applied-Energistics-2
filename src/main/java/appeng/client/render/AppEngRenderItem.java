/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render;


import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.util.ISlimReadableNumberConverter;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class AppEngRenderItem extends RenderItem
{
	private static final ISlimReadableNumberConverter SLIM_CONVERTER = ReadableNumberConverter.INSTANCE;
	private static final IWideReadableNumberConverter WIDE_CONVERTER = ReadableNumberConverter.INSTANCE;

	private IAEItemStack aeStack = null;

	@Override
	public void renderItemOverlayIntoGUI( final FontRenderer fontRenderer, final TextureManager textureManager, final ItemStack is, final int par4, final int par5, final String par6Str )
	{
		if( is != null )
		{
			final float scaleFactor = AEConfig.instance.useTerminalUseLargeFont() ? 0.85f : 0.5f;
			final float inverseScaleFactor = 1.0f / scaleFactor;
			final int offset = AEConfig.instance.useTerminalUseLargeFont() ? 0 : -1;

			final boolean unicodeFlag = fontRenderer.getUnicodeFlag();
			fontRenderer.setUnicodeFlag( false );

			if( is.getItem().showDurabilityBar( is ) )
			{
				final double health = is.getItem().getDurabilityForDisplay( is );
				final int j1 = (int) Math.round( 13.0D - health * 13.0D );
				final int k = (int) Math.round( 255.0D - health * 255.0D );

				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL11.GL_DEPTH_TEST );
				GL11.glDisable( GL11.GL_TEXTURE_2D );
				GL11.glDisable( GL11.GL_ALPHA_TEST );
				GL11.glDisable( GL11.GL_BLEND );

				final Tessellator tessellator = Tessellator.instance;
				final int l = 255 - k << 16 | k << 8;
				final int i1 = ( 255 - k ) / 4 << 16 | 16128;

				this.renderQuad( tessellator, par4 + 2, par5 + 13, 13, 2, 0 );
				this.renderQuad( tessellator, par4 + 2, par5 + 13, 12, 1, i1 );
				this.renderQuad( tessellator, par4 + 2, par5 + 13, j1, 1, l );

				GL11.glEnable( GL11.GL_ALPHA_TEST );
				GL11.glEnable( GL11.GL_TEXTURE_2D );
				GL11.glEnable( GL11.GL_LIGHTING );
				GL11.glEnable( GL11.GL_DEPTH_TEST );
				GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
			}

			if( is.stackSize == 0 )
			{
				final String craftLabelText = AEConfig.instance.useTerminalUseLargeFont() ? GuiText.LargeFontCraft.getLocal() : GuiText.SmallFontCraft.getLocal();

				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL11.GL_DEPTH_TEST );
				GL11.glPushMatrix();
				GL11.glScaled( scaleFactor, scaleFactor, scaleFactor );

				final int X = (int) ( ( (float) par4 + offset + 16.0f - fontRenderer.getStringWidth( craftLabelText ) * scaleFactor ) * inverseScaleFactor );
				final int Y = (int) ( ( (float) par5 + offset + 16.0f - 7.0f * scaleFactor ) * inverseScaleFactor );
				fontRenderer.drawStringWithShadow( craftLabelText, X, Y, 16777215 );

				GL11.glPopMatrix();
				GL11.glEnable( GL11.GL_LIGHTING );
				GL11.glEnable( GL11.GL_DEPTH_TEST );
			}

			final long amount = this.aeStack != null ? this.aeStack.getStackSize() : is.stackSize;

			if( amount != 0 )
			{
				final String stackSize = this.getToBeRenderedStackSize( amount );

				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL11.GL_DEPTH_TEST );
				GL11.glPushMatrix();
				GL11.glScaled( scaleFactor, scaleFactor, scaleFactor );

				final int X = (int) ( ( (float) par4 + offset + 16.0f - fontRenderer.getStringWidth( stackSize ) * scaleFactor ) * inverseScaleFactor );
				final int Y = (int) ( ( (float) par5 + offset + 16.0f - 7.0f * scaleFactor ) * inverseScaleFactor );
				fontRenderer.drawStringWithShadow( stackSize, X, Y, 16777215 );

				GL11.glPopMatrix();
				GL11.glEnable( GL11.GL_LIGHTING );
				GL11.glEnable( GL11.GL_DEPTH_TEST );
			}

			fontRenderer.setUnicodeFlag( unicodeFlag );
		}
	}

	private void renderQuad( final Tessellator par1Tessellator, final int par2, final int par3, final int par4, final int par5, final int par6 )
	{
		par1Tessellator.startDrawingQuads();
		par1Tessellator.setColorOpaque_I( par6 );
		par1Tessellator.addVertex( par2, par3, 0.0D );
		par1Tessellator.addVertex( par2, par3 + par5, 0.0D );
		par1Tessellator.addVertex( par2 + par4, par3 + par5, 0.0D );
		par1Tessellator.addVertex( par2 + par4, par3, 0.0D );
		par1Tessellator.draw();
	}

	private String getToBeRenderedStackSize( final long originalSize )
	{
		if( AEConfig.instance.useTerminalUseLargeFont() )
		{
			return SLIM_CONVERTER.toSlimReadableForm( originalSize );
		}
		else
		{
			return WIDE_CONVERTER.toWideReadableForm( originalSize );
		}
	}

	public IAEItemStack getAeStack()
	{
		return this.aeStack;
	}

	public void setAeStack( @Nonnull final IAEItemStack aeStack )
	{
		this.aeStack = aeStack;
	}
}
