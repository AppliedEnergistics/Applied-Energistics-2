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


import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;


public class ItemRenderer implements IItemRenderer
{

	public static final ItemRenderer INSTANCE = new ItemRenderer();

	@Override
	public boolean handleRenderType( final ItemStack item, final ItemRenderType type )
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper( final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper )
	{
		return true;
	}

	@Override
	public void renderItem( final ItemRenderType type, final ItemStack item, final Object... data )
	{
		GL11.glPushMatrix();
		GL11.glPushAttrib( GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT );
		GL11.glEnable( GL11.GL_ALPHA_TEST );
		GL11.glEnable( GL11.GL_DEPTH_TEST );
		GL11.glEnable( GL11.GL_BLEND );
		GL11.glBlendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );

		if( type == ItemRenderType.ENTITY )
		{
			GL11.glTranslatef( -0.5f, -0.5f, -0.5f );
		}
		if( type == ItemRenderType.INVENTORY )
		{
			GL11.glTranslatef( 0.0f, -0.1f, 0.0f );
		}

		WorldRender.INSTANCE.renderItemBlock( item, type, data );

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
