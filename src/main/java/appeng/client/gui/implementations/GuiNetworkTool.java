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

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerNetworkTool;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;


public class GuiNetworkTool extends AEBaseGui
{

	GuiToggleButton tFacades;

	public GuiNetworkTool( InventoryPlayer inventoryPlayer, INetworkTool te )
	{
		super( new ContainerNetworkTool( inventoryPlayer, te ) );
		this.ySize = 166;
	}

	@Override
	protected void actionPerformed( GuiButton btn )
	{
		super.actionPerformed( btn );

		try
		{
			if( btn == this.tFacades )
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "NetworkTool", "Toggle" ) );
		}
		catch( IOException e )
		{
			AELog.error( e );
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.tFacades = new GuiToggleButton( this.guiLeft - 18, this.guiTop + 8, 23, 22, GuiText.TransparentFacades.getLocal(), GuiText.TransparentFacadesHint.getLocal() );

		this.buttonList.add( this.tFacades );
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		if( this.tFacades != null )
			this.tFacades.setState( ( (ContainerNetworkTool) this.inventorySlots ).facadeMode );

		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.NetworkTool.getLocal() ), 8, 6, 4210752 );
		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );
	}

	@Override
	public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		this.bindTexture( "guis/toolbox.png" );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, this.xSize, this.ySize );
	}
}
