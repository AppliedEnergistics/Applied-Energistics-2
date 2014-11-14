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

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.input.Mouse;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerStorageBus;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.parts.misc.PartStorageBus;

public class GuiStorageBus extends GuiUpgradeable
{

	GuiImgButton rwMode;
	GuiImgButton storageFilter;
	GuiTabButton priority;
	GuiImgButton partition;
	GuiImgButton clear;

	public GuiStorageBus(InventoryPlayer inventoryPlayer, PartStorageBus te) {
		super( new ContainerStorageBus( inventoryPlayer, te ) );
		this.ySize = 251;
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		fontRendererObj.drawString( getGuiDisplayName( GuiText.StorageBus.getLocal() ), 8, 6, 4210752 );
		fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, ySize - 96 + 3, 4210752 );

		if ( fuzzyMode != null )
			fuzzyMode.set( cvb.fzMode );

		if ( storageFilter != null )
			storageFilter.set( ((ContainerStorageBus) cvb).storageFilter );

		if ( rwMode != null )
			rwMode.set( ((ContainerStorageBus) cvb).rwMode );
	}

	@Override
	protected void addButtons()
	{
		clear = new GuiImgButton( this.guiLeft - 18, guiTop + 8, Settings.ACTIONS, ActionItems.CLOSE );
		partition = new GuiImgButton( this.guiLeft - 18, guiTop + 28, Settings.ACTIONS, ActionItems.WRENCH );
		rwMode = new GuiImgButton( this.guiLeft - 18, guiTop + 48, Settings.ACCESS, AccessRestriction.READ_WRITE );
		storageFilter = new GuiImgButton( this.guiLeft - 18, guiTop + 68, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY );
		fuzzyMode = new GuiImgButton( this.guiLeft - 18, guiTop + 88, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		buttonList.add( priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender ) );

		buttonList.add( storageFilter );
		buttonList.add( fuzzyMode );
		buttonList.add( rwMode );
		buttonList.add( partition );
		buttonList.add( clear );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		try
		{
			if ( btn == partition )
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "StorageBus.Action", "Partition" ) );

			else if ( btn == clear )
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "StorageBus.Action", "Clear" ) );

			else if ( btn == priority )
				NetworkHandler.instance.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_PRIORITY ) );

			else if ( btn == rwMode )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( rwMode.getSetting(), backwards ) );

			else if ( btn == storageFilter )
				NetworkHandler.instance.sendToServer( new PacketConfigButton( storageFilter.getSetting(), backwards ) );

		}
		catch (IOException e)
		{
			AELog.error( e );
		}
	}

	@Override
	protected String getBackground()
	{
		return "guis/storagebus.png";
	}

}
