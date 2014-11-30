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

package appeng.client.gui.widgets;


import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import appeng.client.texture.ExtraBlockTextures;

public class GuiTabButton extends GuiButton implements ITooltip
{

	final RenderItem itemRenderer;

	int myIcon = -1;
	public int hideEdge = 0;

	ItemStack myItem;

	final String Msg;

	public void setVisibility(boolean vis)
	{
		this.visible = vis;
		this.enabled = vis;
	}

	public GuiTabButton(int x, int y, int ico, String Msg, RenderItem ir) {
		super( 0, 0, 16, "" );
		this.xPosition = x;
		this.yPosition = y;
		this.width = 22;
		this.height = 22;
		this.myIcon = ico;
		this.Msg = Msg;
		this.itemRenderer = ir;
	}

	public GuiTabButton(int x, int y, ItemStack ico, String Msg, RenderItem ir) {
		super( 0, 0, 16, "" );
		this.xPosition = x;
		this.yPosition = y;
		this.width = 22;
		this.height = 22;
		this.myItem = ico;
		this.Msg = Msg;
		this.itemRenderer = ir;
	}

	@Override
	public boolean isVisible()
	{
		return this.visible;
	}

	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3)
	{
		if ( this.visible )
		{
			GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
			par1Minecraft.renderEngine.bindTexture( ExtraBlockTextures.GuiTexture( "guis/states.png" ) );
			this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;

			int uv_y = (int) Math.floor( 13 / 16 );
			int uv_x = ( this.hideEdge > 0 ? 11 : 13) - uv_y * 16;

			int offsetX = this.hideEdge > 0 ? 1 : 0;

			this.drawTexturedModalRect( this.xPosition, this.yPosition, uv_x * 16, uv_y * 16, 25, 22 );

			if ( this.myIcon >= 0 )
			{
				uv_y = (int) Math.floor( this.myIcon / 16 );
				uv_x = this.myIcon - uv_y * 16;

				this.drawTexturedModalRect( offsetX + this.xPosition + 3, this.yPosition + 3, uv_x * 16, uv_y * 16, 16, 16 );
			}

			this.mouseDragged( par1Minecraft, par2, par3 );

			if ( this.myItem != null )
			{
				this.zLevel = 100.0F;
				this.itemRenderer.zLevel = 100.0F;

				GL11.glEnable( GL11.GL_LIGHTING );
				GL11.glEnable( GL12.GL_RESCALE_NORMAL );
				RenderHelper.enableGUIStandardItemLighting();
				FontRenderer fontrenderer = par1Minecraft.fontRenderer;
				this.itemRenderer.renderItemAndEffectIntoGUI( fontrenderer, par1Minecraft.renderEngine, this.myItem, offsetX + this.xPosition + 3, this.yPosition + 3 );
				GL11.glDisable( GL11.GL_LIGHTING );

				this.itemRenderer.zLevel = 0.0F;
				this.zLevel = 0.0F;
			}
		}
	}

	@Override
	public String getMsg()
	{
		return this.Msg;
	}

	@Override
	public int xPos()
	{
		return this.xPosition;
	}

	@Override
	public int yPos()
	{
		return this.yPosition;
	}

	@Override
	public int getWidth()
	{
		return 22;
	}

	@Override
	public int getHeight()
	{
		return 22;
	}

}
