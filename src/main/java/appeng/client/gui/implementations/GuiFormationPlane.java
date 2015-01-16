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

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerFormationPlane;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.parts.automation.PartFormationPlane;


public class GuiFormationPlane extends GuiUpgradeable
{

	GuiTabButton priority;
	GuiImgButton placeMode;

	public GuiFormationPlane( InventoryPlayer inventoryPlayer, PartFormationPlane te )
	{
		super( new ContainerFormationPlane( inventoryPlayer, te ) );
		this.ySize = 251;
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.FormationPlane.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		if ( this.fuzzyMode != null )
			this.fuzzyMode.set( this.cvb.fzMode );

		if ( this.placeMode != null )
			this.placeMode.set( ( ( ContainerFormationPlane ) this.cvb ).placeMode );
	}

	@Override
	protected void addButtons()
	{
		this.placeMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 28, Settings.PLACE_BLOCK, YesNo.YES );
		this.fuzzyMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 48, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		this.buttonList.add( this.priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender ) );

		this.buttonList.add( this.placeMode );
		this.buttonList.add( this.fuzzyMode );
	}

	@Override
	protected void actionPerformed( GuiButton btn )
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == this.priority )
		{
			NetworkHandler.instance.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_PRIORITY ) );
		}
		else if ( btn == this.placeMode )
		{
			NetworkHandler.instance.sendToServer( new PacketConfigButton( this.placeMode.getSetting(), backwards ) );
		}

	}

	@Override
	protected String getBackground()
	{
		return "guis/storagebus.png";
	}

}
