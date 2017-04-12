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


import appeng.items.misc.ItemEncodedPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;


public class ItemEncodedPatternRenderer implements IItemRenderer
{

	private final RenderItem ri = new RenderItem();
	private boolean recursive = false;

	@Override
	public boolean handleRenderType( final ItemStack item, final ItemRenderType type )
	{
		final boolean isShiftHeld = Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT );

		if( !this.recursive && type == IItemRenderer.ItemRenderType.INVENTORY && isShiftHeld )
		{
			final ItemEncodedPattern iep = (ItemEncodedPattern) item.getItem();

			if( iep.getOutput( item ) != null )
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean shouldUseRenderHelper( final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper )
	{
		return false;
	}

	@Override
	public void renderItem( final ItemRenderType type, final ItemStack item, final Object... data )
	{
		this.recursive = true;

		final ItemEncodedPattern iep = (ItemEncodedPattern) item.getItem();
		final ItemStack is = iep.getOutput( item );
		final Minecraft mc = Minecraft.getMinecraft();

		GL11.glPushAttrib( GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT );
		RenderHelper.enableGUIStandardItemLighting();
		this.ri.renderItemAndEffectIntoGUI( mc.fontRenderer, mc.getTextureManager(), is, 0, 0 );
		RenderHelper.disableStandardItemLighting();
		GL11.glPopAttrib();

		this.recursive = false;
	}
}
