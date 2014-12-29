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

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.config.Settings;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerWireless;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;

public class GuiWireless extends AEBaseGui
{

	GuiImgButton units;

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == this.units )
		{
			AEConfig.instance.nextPowerUnit( backwards );
			this.units.set( AEConfig.instance.selectedPowerUnit() );
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.units = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.POWER_UNITS, AEConfig.instance.selectedPowerUnit() );
		this.buttonList.add( this.units );
	}

	public GuiWireless(InventoryPlayer inventoryPlayer, TileWireless te) {
		super( new ContainerWireless( inventoryPlayer, te ) );
		this.ySize = 166;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		this.bindTexture( "guis/wireless.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.Wireless.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		ContainerWireless cw = (ContainerWireless) this.inventorySlots;

		if ( cw.range > 0 )
		{
			String firstMessage = GuiText.Range.getLocal() + ": " + (cw.range / 10.0) + " m";
			String secondMessage = GuiText.PowerUsageRate.getLocal() + ": " + Platform.formatPowerLong( cw.drain, true );

			int strWidth = Math.max( this.fontRendererObj.getStringWidth( firstMessage ), this.fontRendererObj.getStringWidth( secondMessage ) );
			int cOffset = (this.xSize / 2) - (strWidth / 2);
			this.fontRendererObj.drawString( firstMessage, cOffset, 20, 4210752 );
			this.fontRendererObj.drawString( secondMessage, cOffset, 20 + 12, 4210752 );
		}
	}

}
