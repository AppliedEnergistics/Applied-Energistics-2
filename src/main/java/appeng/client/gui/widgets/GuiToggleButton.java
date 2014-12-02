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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import appeng.client.texture.ExtraBlockTextures;

public class GuiToggleButton extends GuiButton implements ITooltip
{

	final int iconIdxOn;
	final int iconIdxOff;

	final String Name;
	final String Hint;

	boolean on;

	public void setState(boolean isOn)
	{
		on = isOn;
	}

	public void setVisibility(boolean vis)
	{
		visible = vis;
		enabled = vis;
	}

	public GuiToggleButton(int x, int y, int on, int off, String Name, String Hint) {
		super( 0, 0, 16, "" );
		iconIdxOn = on;
		iconIdxOff = off;
		this.Name = Name;
		this.Hint = Hint;
		xPosition = x;
		yPosition = y;
		width = 16;
		height = 16;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3)
	{
		if ( this.visible )
		{
			int iconIndex = getIconIndex();

			GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
			par1Minecraft.renderEngine.bindTexture( ExtraBlockTextures.GuiTexture( "guis/states.png" ) );
			this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;

			int uv_y = (int) Math.floor( iconIndex / 16 );
			int uv_x = iconIndex - uv_y * 16;

			this.drawTexturedModalRect( this.xPosition, this.yPosition, 256 - 16, 256 - 16, 16, 16 );
			this.drawTexturedModalRect( this.xPosition, this.yPosition, uv_x * 16, uv_y * 16, 16, 16 );
			this.mouseDragged( par1Minecraft, par2, par3 );
		}
	}

	private int getIconIndex()
	{
		return on ? iconIdxOn : iconIdxOff;
	}

	@Override
	public String getMsg()
	{
		String DisplayName = Name;
		String DisplayValue = Hint;

		if ( DisplayName != null )
		{
			String Name = StatCollector.translateToLocal( DisplayName );
			String Value = StatCollector.translateToLocal( DisplayValue );

			if ( Name == null || Name.equals( "" ) )
				Name = DisplayName;
			if ( Value == null || Value.equals( "" ) )
				Value = DisplayValue;

			Value = Value.replace( "\\n", "\n" );
			StringBuilder sb = new StringBuilder( Value );

			int i = sb.lastIndexOf( "\n" );
			if ( i <= 0 )
				i = 0;
			while (i + 30 < sb.length() && (i = sb.lastIndexOf( " ", i + 30 )) != -1)
			{
				sb.replace( i, i + 1, "\n" );
			}

			return Name + "\n" + sb.toString();
		}
		return null;
	}

	@Override
	public int xPos()
	{
		return xPosition;
	}

	@Override
	public int yPos()
	{
		return yPosition;
	}

	@Override
	public int getWidth()
	{
		return 16;
	}

	@Override
	public int getHeight()
	{
		return 16;
	}

}
