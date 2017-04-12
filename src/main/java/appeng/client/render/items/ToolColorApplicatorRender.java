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

package appeng.client.render.items;


import appeng.api.util.AEColor;
import appeng.client.texture.ExtraItemTextures;
import appeng.items.tools.powered.ToolColorApplicator;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;


public class ToolColorApplicatorRender implements IItemRenderer
{

	@Override
	public boolean handleRenderType( final ItemStack item, final ItemRenderType type )
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper( final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper )
	{
		return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION;
	}

	@Override
	public void renderItem( final ItemRenderType type, final ItemStack item, final Object... data )
	{
		final IIcon par2Icon = item.getIconIndex();

		float f4 = par2Icon.getMinU();
		float f5 = par2Icon.getMaxU();
		float f6 = par2Icon.getMinV();
		float f7 = par2Icon.getMaxV();

		final Tessellator tessellator = Tessellator.instance;

		GL11.glPushMatrix();
		GL11.glPushAttrib( GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT );

		if( type == ItemRenderType.INVENTORY )
		{
			GL11.glColor4f( 1, 1, 1, 1.0F );
			GL11.glScalef( 16F, 16F, 10F );
			GL11.glTranslatef( 0.0F, 1.0F, 0.0F );
			GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
			GL11.glEnable( GL11.GL_ALPHA_TEST );

			tessellator.startDrawingQuads();
			tessellator.setNormal( 0.0F, 1.0F, 0.0F );
			tessellator.addVertexWithUV( 0, 0, 0, f4, f7 );
			tessellator.addVertexWithUV( 1, 0, 0, f5, f7 );
			tessellator.addVertexWithUV( 1, 1, 0, f5, f6 );
			tessellator.addVertexWithUV( 0, 1, 0, f4, f6 );
			tessellator.draw();
		}
		else
		{
			if( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
			{
				GL11.glTranslatef( 0.0F, 0.0F, 0.0F );
			}
			else if( type == ItemRenderType.EQUIPPED )
			{
				GL11.glTranslatef( 0.0F, 0.0F, 0.0F );
			}
			else
			{
				GL11.glTranslatef( -0.5F, -0.3F, 0.01F );
			}
			final float f12 = 0.0625F;
			ItemRenderer.renderItemIn2D( tessellator, f5, f6, f4, f7, par2Icon.getIconWidth(), par2Icon.getIconHeight(), f12 );

			GL11.glDisable( GL11.GL_CULL_FACE );
			GL11.glColor4f( 1, 1, 1, 1.0F );
			GL11.glScalef( -1F, -1F, 1F );
			GL11.glTranslatef( -1.125F, 0.0f, f12 / -2.0f );
			GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
		}

		final IIcon dark = ExtraItemTextures.ToolColorApplicatorTip_Dark.getIcon();
		final IIcon med = ExtraItemTextures.ToolColorApplicatorTip_Medium.getIcon();
		final IIcon light = ExtraItemTextures.ToolColorApplicatorTip_Light.getIcon();

		GL11.glScalef( 1F / 16F, 1F / 16F, 1F );

		if( type != ItemRenderType.INVENTORY )
		{
			GL11.glTranslatef( 2, 0, 0 );
		}

		GL11.glDisable( GL11.GL_LIGHTING );

		final AEColor col = ( (ToolColorApplicator) item.getItem() ).getActiveColor( item );

		if( col != null )
		{
			tessellator.startDrawingQuads();

			f4 = dark.getMinU();
			f5 = dark.getMaxU();
			f6 = dark.getMinV();
			f7 = dark.getMaxV();

			tessellator.setColorOpaque_I( col.blackVariant );
			tessellator.addVertexWithUV( 0, 0, 0, f4, f7 );
			tessellator.addVertexWithUV( 16, 0, 0, f5, f7 );
			tessellator.addVertexWithUV( 16, 16, 0, f5, f6 );
			tessellator.addVertexWithUV( 0, 16, 0, f4, f6 );

			f4 = light.getMinU();
			f5 = light.getMaxU();
			f6 = light.getMinV();
			f7 = light.getMaxV();

			tessellator.setColorOpaque_I( col.whiteVariant );
			tessellator.addVertexWithUV( 0, 0, 0, f4, f7 );
			tessellator.addVertexWithUV( 16, 0, 0, f5, f7 );
			tessellator.addVertexWithUV( 16, 16, 0, f5, f6 );
			tessellator.addVertexWithUV( 0, 16, 0, f4, f6 );

			f4 = med.getMinU();
			f5 = med.getMaxU();
			f6 = med.getMinV();
			f7 = med.getMaxV();

			tessellator.setColorOpaque_I( col.mediumVariant );
			tessellator.addVertexWithUV( 0, 0, 0, f4, f7 );
			tessellator.addVertexWithUV( 16, 0, 0, f5, f7 );
			tessellator.addVertexWithUV( 16, 16, 0, f5, f6 );
			tessellator.addVertexWithUV( 0, 16, 0, f4, f6 );

			tessellator.draw();
		}

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
