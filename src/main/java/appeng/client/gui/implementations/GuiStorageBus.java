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

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

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

	private GuiImgButton rwMode;
	private GuiImgButton storageFilter;
	private GuiTabButton priority;
	private GuiImgButton partition;
	private GuiImgButton clear;

	public GuiStorageBus( final InventoryPlayer inventoryPlayer, final PartStorageBus te )
	{
		super( new ContainerStorageBus( inventoryPlayer, te ) );
		this.ySize = 251;
	}

	@Override
	protected void addButtons()
	{
		this.clear = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.ACTIONS, ActionItems.CLOSE );
		this.partition = new GuiImgButton( this.guiLeft - 18, this.guiTop + 28, Settings.ACTIONS, ActionItems.WRENCH );
		this.rwMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 48, Settings.ACCESS, AccessRestriction.READ_WRITE );
		this.storageFilter = new GuiImgButton( this.guiLeft - 18, this.guiTop + 68, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY );
		this.fuzzyMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 88, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		this.buttonList.add( this.priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender ) );

		this.buttonList.add( this.storageFilter );
		this.buttonList.add( this.fuzzyMode );
		this.buttonList.add( this.rwMode );
		this.buttonList.add( this.partition );
		this.buttonList.add( this.clear );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.StorageBus.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		if( this.fuzzyMode != null )
		{
			this.fuzzyMode.set( this.cvb.getFuzzyMode() );
		}

		if( this.storageFilter != null )
		{
			this.storageFilter.set( ( (ContainerStorageBus) this.cvb ).getStorageFilter() );
		}

		if( this.rwMode != null )
		{
			this.rwMode.set( ( (ContainerStorageBus) this.cvb ).getReadWriteMode() );
		}
	}

	@Override
	protected String getBackground()
	{
		return "guis/storagebus.png";
	}

	@Override
	protected void actionPerformed( final GuiButton btn )
	{
		super.actionPerformed( btn );

		final boolean backwards = Mouse.isButtonDown( 1 );

		try
		{
			if( btn == this.partition )
			{
				NetworkHandler.INSTANCE.sendToServer( new PacketValueConfig( "StorageBus.Action", "Partition" ) );
			}
			else if( btn == this.clear )
			{
				NetworkHandler.INSTANCE.sendToServer( new PacketValueConfig( "StorageBus.Action", "Clear" ) );
			}
			else if( btn == this.priority )
			{
				NetworkHandler.INSTANCE.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_PRIORITY ) );
			}
			else if( btn == this.rwMode )
			{
				NetworkHandler.INSTANCE.sendToServer( new PacketConfigButton( this.rwMode.getSetting(), backwards ) );
			}
			else if( btn == this.storageFilter )
			{
				NetworkHandler.INSTANCE.sendToServer( new PacketConfigButton( this.storageFilter.getSetting(), backwards ) );
			}
		}
		catch( final IOException e )
		{
			AELog.error( e );
		}
	}
}
