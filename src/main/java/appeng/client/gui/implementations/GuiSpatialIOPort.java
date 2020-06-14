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

package appeng.client.gui.implementations;


import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.CommonButtons;
import appeng.container.implementations.ContainerSpatialIOPort;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;


public class GuiSpatialIOPort extends AEBaseGui<ContainerSpatialIOPort>
{

	public GuiSpatialIOPort(ContainerSpatialIOPort container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.ySize = 199;
	}

	@Override
	public void init()
	{
		super.init();
		this.addButton( CommonButtons.togglePowerUnit( this.guiLeft - 18, this.guiTop + 8 ) );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.font.drawString( GuiText.StoredPower.getLocal() + ": " + Platform.formatPowerLong( this.container.getCurrentPower(), false ), 13, 21,
				4210752 );
		this.font.drawString( GuiText.MaxPower.getLocal() + ": " + Platform.formatPowerLong( this.container.getMaxPower(), false ), 13, 31, 4210752 );
		this.font.drawString( GuiText.RequiredPower.getLocal() + ": " + Platform.formatPowerLong( this.container.getRequiredPower(), false ), 13, 73,
				4210752 );
		this.font.drawString( GuiText.Efficiency.getLocal() + ": " + ( ( (float) this.container.getEfficency() ) / 100 ) + '%', 13, 83, 4210752 );

		this.font.drawString( this.getGuiDisplayName( GuiText.SpatialIOPort.getLocal() ), 8, 6, 4210752 );
		this.font.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96, 4210752 );

		if( this.container.xSize != 0 && this.container.ySize != 0 && this.container.zSize != 0 )
		{
			final String text = GuiText.SCSSize.getLocal() + ": " + this.container.xSize + "x" + this.container.ySize + "x" + this.container.zSize;
			this.font.drawString( text, 13, 93, 4210752 );
		}
		else
		{
			this.font.drawString( GuiText.SCSSize.getLocal() + ": " + GuiText.SCSInvalid.getLocal(), 13, 93, 4210752 );
		}

	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks)
	{
		this.bindTexture( "guis/spatialio.png" );
		GuiUtils.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize, 0 /* FIXME this.zlevel was used */ );
	}
}
