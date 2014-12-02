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

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.api.config.Settings;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerSpatialIOPort;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.util.Platform;

public class GuiSpatialIOPort extends AEBaseGui
{

	final ContainerSpatialIOPort container;
	GuiImgButton units;

	public GuiSpatialIOPort(InventoryPlayer inventoryPlayer, TileSpatialIOPort te) {
		super( new ContainerSpatialIOPort( inventoryPlayer, te ) );
		this.ySize = 199;
		container = (ContainerSpatialIOPort) inventorySlots;
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == units )
		{
			AEConfig.instance.nextPowerUnit( backwards );
			units.set( AEConfig.instance.selectedPowerUnit() );
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		units = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.POWER_UNITS, AEConfig.instance.selectedPowerUnit() );
		buttonList.add( units );
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		bindTexture( "guis/spatialio.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, xSize, ySize );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( GuiText.StoredPower.getLocal() + ": " + Platform.formatPowerLong( container.currentPower, false ), 13, 21, 4210752 );
		fontRendererObj.drawString( GuiText.MaxPower.getLocal() + ": " + Platform.formatPowerLong( container.maxPower, false ), 13, 31, 4210752 );
		fontRendererObj.drawString( GuiText.RequiredPower.getLocal() + ": " + Platform.formatPowerLong( container.reqPower, false ), 13, 78, 4210752 );
		fontRendererObj.drawString( GuiText.Efficiency.getLocal() + ": " + (((float) container.eff) / 100) + "%", 13, 88, 4210752 );

		fontRendererObj.drawString( getGuiDisplayName( GuiText.SpatialIOPort.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96, 4210752 );
	}

}
