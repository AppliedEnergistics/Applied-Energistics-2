/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.client.gui.widgets;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import appeng.client.gui.widgets.ITooltip;
import appeng.fluids.util.IAEFluidTank;


@OnlyIn( Dist.CLIENT )
public class GuiFluidTank extends GuiButton implements ITooltip
{
	private final IAEFluidTank tank;
	private final int slot;

	public GuiFluidTank( IAEFluidTank tank, int slot, int id, int x, int y, int w, int h )
	{
		super( id, x, y, w, h, "" );
		this.tank = tank;
		this.slot = slot;
	}

	@Override
	public void drawButton( final Minecraft mc, final int mouseX, final int mouseY, final float partialTicks )
	{
		if( this.visible )
		{
			RenderSystem.disableBlend();
			RenderSystem.disableLighting();

			fill( this.x, this.y, this.x + this.width, this.y + this.height, AEColor.GRAY.blackVariant | 0xFF000000 );

			final IAEFluidStack fluid = this.tank.getFluidInSlot( this.slot );
			if( fluid != null && fluid.getStackSize() > 0 )
			{
				mc.getTextureManager().bindTexture( AtlasTexture.LOCATION_BLOCKS_TEXTURE );

				float red = ( fluid.getFluid().getColor() >> 16 & 255 ) / 255.0F;
				float green = ( fluid.getFluid().getColor() >> 8 & 255 ) / 255.0F;
				float blue = ( fluid.getFluid().getColor() & 255 ) / 255.0F;
				RenderSystem.color4f( red, green, blue );

				TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite( fluid.getFluid().getStill().toString() );
				final int scaledHeight = (int) ( this.height * ( (float) fluid.getStackSize() / this.tank.getTankProperties()[this.slot].getCapacity() ) );

				int iconHeightRemainder = scaledHeight % 16;
				if( iconHeightRemainder > 0 )
				{
					this.drawTexturedModalRect( this.x, this.y + this.height - iconHeightRemainder, sprite, 16, iconHeightRemainder );
				}
				for( int i = 0; i < scaledHeight / 16; i++ )
				{
					this.drawTexturedModalRect( this.x, this.y + this.height - iconHeightRemainder - ( i + 1 ) * 16, sprite, 16, 16 );
				}
			}

		}
	}

	@Override
	public String getMessage()
	{
		final IAEFluidStack fluid = this.tank.getFluidInSlot( this.slot );
		if( fluid != null && fluid.getStackSize() > 0 )
		{
			String desc = fluid.getFluid().getLocalizedName( fluid.getFluidStack() );
			String amountToText = fluid.getStackSize() + "mB";

			return desc + "\n" + amountToText;
		}
		return null;
	}

	@Override
	public int xPos()
	{
		return this.x - 2;
	}

	@Override
	public int yPos()
	{
		return this.y - 2;
	}

	@Override
	public int getWidth()
	{
		return this.width + 4;
	}

	@Override
	public int getHeight()
	{
		return this.height + 4;
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

}
