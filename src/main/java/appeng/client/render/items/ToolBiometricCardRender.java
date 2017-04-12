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


import appeng.api.implementations.items.IBiometricCard;
import appeng.api.util.AEColor;
import appeng.client.texture.ExtraItemTextures;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;


public class ToolBiometricCardRender implements IItemRenderer
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

		final float f4 = par2Icon.getMinU();
		final float f5 = par2Icon.getMaxU();
		final float f6 = par2Icon.getMinV();
		final float f7 = par2Icon.getMaxV();

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
			GL11.glTranslatef( -0.5F, -0.3F, 0.01F );
			final float f12 = 0.0625F;
			ItemRenderer.renderItemIn2D( tessellator, f5, f6, f4, f7, par2Icon.getIconWidth(), par2Icon.getIconHeight(), f12 );

			GL11.glDisable( GL11.GL_CULL_FACE );
			GL11.glColor4f( 1, 1, 1, 1.0F );
			GL11.glScalef( 1F, 1.1F, 1F );
			GL11.glTranslatef( 0.0F, 1.07F, f12 / -2.0f );
			GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
		}

		final float u = ExtraItemTextures.White.getIcon().getInterpolatedU( 8.1 );
		final float v = ExtraItemTextures.White.getIcon().getInterpolatedV( 8.1 );

		String username = "";

		if( item.getItem() instanceof IBiometricCard )
		{
			final GameProfile gp = ( (IBiometricCard) item.getItem() ).getProfile( item );
			if( gp != null )
			{
				username = gp.getName();
			}
		}
		final int hash = username.length() > 0 ? username.hashCode() : 0;

		GL11.glScalef( 1F / 16F, 1F / 16F, 1F );
		GL11.glTranslatef( 4, 6, 0 );
		GL11.glDisable( GL11.GL_LIGHTING );

		tessellator.startDrawingQuads();

		AEColor col = AEColor.values()[Math.abs( 3 + hash ) % AEColor.values().length];
		if( hash == 0 )
		{
			col = AEColor.Black;
		}

		final float z = 0;

		for( int x = 0; x < 8; x++ )// 8
		{
			for( int y = 0; y < 6; y++ )// 6
			{
				boolean isLit = false;

				if( x == 0 || y == 0 || x == 7 || y == 5 )
				{
					isLit = false;
				}
				else
				{
					isLit = ( hash & ( 1 << x ) ) != 0 || ( hash & ( 1 << y ) ) != 0;
				}

				if( isLit )
				{
					tessellator.setColorOpaque_I( col.mediumVariant );
				}
				else
				{
					final float scale = 0.3f / 255.0f;
					tessellator.setColorOpaque_F( ( ( col.blackVariant >> 16 ) & 0xff ) * scale, ( ( col.blackVariant >> 8 ) & 0xff ) * scale, ( col.blackVariant & 0xff ) * scale );
				}

				tessellator.addVertexWithUV( x, y, z, u, v );
				tessellator.addVertexWithUV( x + 1, y, z, u, v );
				tessellator.addVertexWithUV( x + 1, y + 1, z, u, v );
				tessellator.addVertexWithUV( x, y + 1, z, u, v );
			}
		}

		tessellator.draw();

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
