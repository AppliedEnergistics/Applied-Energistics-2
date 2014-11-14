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

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import appeng.api.implementations.items.IBiometricCard;
import appeng.api.util.AEColor;
import appeng.client.texture.ExtraItemTextures;

import com.mojang.authlib.GameProfile;

public class ToolBiometricCardRender implements IItemRenderer
{

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		IIcon par2Icon = item.getIconIndex();

		float f4 = par2Icon.getMinU();
		float f5 = par2Icon.getMaxU();
		float f6 = par2Icon.getMinV();
		float f7 = par2Icon.getMaxV();
		float f12 = 0.0625F;

		Tessellator tessellator = Tessellator.instance;
		GL11.glPushMatrix();
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

		if ( type == ItemRenderType.INVENTORY )
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
			ItemRenderer.renderItemIn2D( tessellator, f5, f6, f4, f7, par2Icon.getIconWidth(), par2Icon.getIconHeight(), f12 );

			GL11.glDisable( GL11.GL_CULL_FACE );
			GL11.glColor4f( 1, 1, 1, 1.0F );
			GL11.glScalef( 1F, 1.1F, 1F );
			GL11.glTranslatef( 0.0F, 1.07F, f12 / -2.0f );
			GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
		}

		float u = ExtraItemTextures.White.getIcon().getInterpolatedU( 8.1 );
		float v = ExtraItemTextures.White.getIcon().getInterpolatedV( 8.1 );

		String username = "";
		if ( item.getItem() instanceof IBiometricCard )
		{
			GameProfile gp = (( IBiometricCard) item.getItem() ).getProfile(item);
			if ( gp != null )
				username = gp.getName();
		}
		int hash = username.length() > 0 ? username.hashCode() : 0;

		GL11.glScalef( 1F / 16F, 1F / 16F, 1F );
		GL11.glTranslatef( 4, 6, 0 );
		GL11.glDisable( GL11.GL_LIGHTING );

		tessellator.startDrawingQuads();
		float z = 0;

		AEColor col = AEColor.values()[Math.abs( 3 + hash ) % AEColor.values().length];
		if ( hash == 0 )
			col = AEColor.Black;

		for (int x = 0; x < 8; x++)// 8
		{
			for (int y = 0; y < 6; y++)// 6
			{
				boolean isLit = false;
				float scale = 0.3f / 255.0f;

				if ( x == 0 || y == 0 || x == 7 || y == 5 )
					isLit = false;
				else
					isLit = (hash & (1 << x)) != 0 || (hash & (1 << y)) != 0;

				if ( isLit )
					tessellator.setColorOpaque_I( col.mediumVariant );
				else
					tessellator.setColorOpaque_F( ((col.blackVariant >> 16) & 0xff) * scale, ((col.blackVariant >> 8) & 0xff) * scale,
							(col.blackVariant & 0xff) * scale );

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
