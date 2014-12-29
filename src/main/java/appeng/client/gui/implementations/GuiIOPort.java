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

import appeng.api.AEApi;
import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.container.implementations.ContainerIOPort;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.tile.storage.TileIOPort;

public class GuiIOPort extends GuiUpgradeable
{

	GuiImgButton fullMode;
	GuiImgButton operationMode;

	public GuiIOPort(InventoryPlayer inventoryPlayer, TileIOPort te) {
		super( new ContainerIOPort( inventoryPlayer, te ) );
		this.ySize = 166;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		super.drawBG( offsetX, offsetY, mouseX, mouseY );
		this.drawItem( offsetX + 66 - 8, offsetY + 17, AEApi.instance().items().itemCell1k.stack( 1 ) );
		this.drawItem( offsetX + 94 + 8, offsetY + 17, AEApi.instance().blocks().blockDrive.stack( 1 ) );
	}

	@Override
	public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.IOPort.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		if ( this.redstoneMode != null )
			this.redstoneMode.set( this.cvb.rsMode );

		if ( this.operationMode != null )
			this.operationMode.set( ((ContainerIOPort) this.cvb).opMode );

		if ( this.fullMode != null )
			this.fullMode.set( ((ContainerIOPort) this.cvb).fMode );
	}

	@Override
	protected void addButtons()
	{
		this.redstoneMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 28, Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE );
		this.fullMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.FULLNESS_MODE, FullnessMode.EMPTY );
		this.operationMode = new GuiImgButton( this.guiLeft + 80, this.guiTop + 17, Settings.OPERATION_MODE, OperationMode.EMPTY );

		this.buttonList.add( this.operationMode );
		this.buttonList.add( this.redstoneMode );
		this.buttonList.add( this.fullMode );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == this.fullMode )
			NetworkHandler.instance.sendToServer( new PacketConfigButton( this.fullMode.getSetting(), backwards ) );

		if ( btn == this.operationMode )
			NetworkHandler.instance.sendToServer( new PacketConfigButton( this.operationMode.getSetting(), backwards ) );
	}

	@Override
	protected String getBackground()
	{
		return "guis/ioport.png";
	}

}
