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
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerFormationPlane;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.parts.automation.PartFormationPlane;

public class GuiFormationPlane extends GuiUpgradeable
{

	GuiTabButton priority;

	public GuiFormationPlane(InventoryPlayer inventoryPlayer, PartFormationPlane te) {
		super( new ContainerFormationPlane( inventoryPlayer, te ) );
		this.ySize = 251;
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.FormationPlane.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		if ( fuzzyMode != null )
			fuzzyMode.set( cvb.fzMode );
	}

	@Override
	protected void addButtons()
	{
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		buttonList.add( priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender ) );

		buttonList.add( fuzzyMode );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		if ( btn == priority )
		{
			NetworkHandler.instance.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_PRIORITY ) );
		}
	}

	@Override
	protected String getBackground()
	{
		return "guis/storagebus.png";
	}

}
