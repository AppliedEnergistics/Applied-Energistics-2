/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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


import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import appeng.api.util.AEColor;
import appeng.client.texture.ExtraItemTextures;
import appeng.items.misc.ItemPaintBall;


public final class PaintBallRender implements IItemRenderer
{

	@Override
	public final boolean handleRenderType( ItemStack item, ItemRenderType type )
	{
		return true;
	}

	@Override
	public final boolean shouldUseRenderHelper( ItemRenderType type, ItemStack item, ItemRendererHelper helper )
	{
		return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION;
	}

	@Override
	public final void renderItem( ItemRenderType type, ItemStack item, Object... data )
	{
		IIcon par2Icon = item.getIconIndex();
		if( item.getItemDamage() >= 20 )
		{
			par2Icon = ExtraItemTextures.ItemPaintBallShimmer.getIcon();
		}

		float f4 = par2Icon.getMinU();
		float f5 = par2Icon.getMaxU();
		float f6 = par2Icon.getMinV();
		float f7 = par2Icon.getMaxV();
		float f12 = 0.0625F;

		ItemPaintBall ipb = (ItemPaintBall) item.getItem();

		Tessellator tessellator = Tessellator.instance;
		GL11.glPushMatrix();
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

		AEColor col = ipb.getColor( item );

		int colorValue = item.getItemDamage() >= 20 ? col.mediumVariant : col.mediumVariant;
		int r = ( colorValue >> 16 ) & 0xff;
		int g = ( colorValue >> 8 ) & 0xff;
		int b = ( colorValue ) & 0xff;

		int full = (int) ( 255 * 0.3 );
		float fail = 0.7f;

		if( item.getItemDamage() >= 20 )
		{
			GL11.glColor4ub( (byte) ( full + r * fail ), (byte) ( full + g * fail ), (byte) ( full + b * fail ), (byte) 255 );
		}
		else
		{
			GL11.glColor4ub( (byte) r, (byte) g, (byte) b, (byte) 255 );
		}

		if( type == ItemRenderType.INVENTORY )
		{
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
			else
			{
				GL11.glTranslatef( -0.5F, -0.3F, 0.01F );
			}
			ItemRenderer.renderItemIn2D( tessellator, f5, f6, f4, f7, par2Icon.getIconWidth(), par2Icon.getIconHeight(), f12 );

			GL11.glDisable( GL11.GL_CULL_FACE );
			GL11.glColor4f( 1, 1, 1, 1.0F );
			GL11.glScalef( 1F, 1.1F, 1F );
			GL11.glTranslatef( 0.0F, 1.07F, f12 / -2.0f );
			GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
		}

		GL11.glColor4f( 1, 1, 1, 1.0F );

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
