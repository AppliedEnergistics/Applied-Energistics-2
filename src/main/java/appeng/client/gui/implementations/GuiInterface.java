/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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
import appeng.api.config.YesNo;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerInterface;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.IInterfaceHost;


public final class GuiInterface extends GuiUpgradeable
{

	GuiTabButton priority;
	GuiImgButton BlockMode;
	GuiToggleButton interfaceMode;

	public GuiInterface( InventoryPlayer inventoryPlayer, IInterfaceHost te )
	{
		super( new ContainerInterface( inventoryPlayer, te ) );
		this.ySize = 211;
	}

	@Override
	protected void addButtons()
	{
		this.priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender );
		this.buttonList.add( this.priority );

		this.BlockMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.BLOCK, YesNo.NO );
		this.buttonList.add( this.BlockMode );

		this.interfaceMode = new GuiToggleButton( this.guiLeft - 18, this.guiTop + 26, 84, 85, GuiText.InterfaceTerminal.getLocal(), GuiText.InterfaceTerminalHint.getLocal() );
		this.buttonList.add( this.interfaceMode );
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		if( this.BlockMode != null )
		{
			this.BlockMode.set( ( (ContainerInterface) this.cvb ).bMode );
		}

		if( this.interfaceMode != null )
		{
			this.interfaceMode.setState( ( (ContainerInterface) this.cvb ).iTermMode == YesNo.YES );
		}

		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.Interface.getLocal() ), 8, 6, 4210752 );

		this.fontRendererObj.drawString( GuiText.Config.getLocal(), 18, 6 + 11 + 7, 4210752 );
		this.fontRendererObj.drawString( GuiText.StoredItems.getLocal(), 18, 6 + 60 + 7, 4210752 );
		this.fontRendererObj.drawString( GuiText.Patterns.getLocal(), 8, 6 + 73 + 7, 4210752 );

		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );
	}

	@Override
	protected String getBackground()
	{
		return "guis/interface.png";
	}

	@Override
	protected final void actionPerformed( GuiButton btn )
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if( btn == this.priority )
		{
			NetworkHandler.instance.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_PRIORITY ) );
		}

		if( btn == this.interfaceMode )
		{
			NetworkHandler.instance.sendToServer( new PacketConfigButton( Settings.INTERFACE_TERMINAL, backwards ) );
		}

		if( btn == this.BlockMode )
		{
			NetworkHandler.instance.sendToServer( new PacketConfigButton( this.BlockMode.getSetting(), backwards ) );
		}
	}
}
