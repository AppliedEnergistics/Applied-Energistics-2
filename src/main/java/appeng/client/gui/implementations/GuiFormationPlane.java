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


import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.widgets.GuiServerSettingToggleButton;
import appeng.client.gui.widgets.GuiSettingToggleButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerFormationPlane;
import appeng.container.implementations.ContainerPriority;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;


public class GuiFormationPlane extends GuiUpgradeable<ContainerFormationPlane>
{

	private GuiSettingToggleButton<YesNo> placeMode;

	public GuiFormationPlane(ContainerFormationPlane container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.ySize = 251;
	}

	@Override
	protected void addButtons()
	{
		this.placeMode = new GuiServerSettingToggleButton<>( this.guiLeft - 18, this.guiTop + 28, Settings.PLACE_BLOCK, YesNo.YES );
		this.fuzzyMode = new GuiServerSettingToggleButton<>( this.guiLeft - 18, this.guiTop + 48, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		this.addButton( new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRenderer, btn -> openPriorityGui() ) );

		this.addButton( this.placeMode );
		this.addButton( this.fuzzyMode );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.font.drawString( this.getGuiDisplayName( GuiText.FormationPlane.getLocal() ), 8, 6, 4210752 );
		this.font.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		if( this.fuzzyMode != null )
		{
			this.fuzzyMode.set( this.cvb.getFuzzyMode() );
		}

		if( this.placeMode != null )
		{
			this.placeMode.set( ( (ContainerFormationPlane) this.cvb ).getPlaceMode() );
		}
	}

	@Override
	protected String getBackground()
	{
		return "guis/storagebus.png";
	}

	private void openPriorityGui() {
		NetworkHandler.instance().sendToServer( new PacketSwitchGuis( ContainerPriority.TYPE ) );
	}

}
