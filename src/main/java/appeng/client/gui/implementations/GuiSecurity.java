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

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerSecurity;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;


public class GuiSecurity extends GuiMEMonitorable
{

	GuiToggleButton inject, extract, craft, build, security;

	public GuiSecurity( InventoryPlayer inventoryPlayer, ITerminalHost te )
	{
		super( inventoryPlayer, te, new ContainerSecurity( inventoryPlayer, te ) );
		this.customSortOrder = false;
		this.reservedSpace = 33;

		// increase size so that the slot is over the gui.
		this.xSize += 56;
		this.standardSize = this.xSize;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int top = this.guiTop + this.ySize - 116;
		this.buttonList.add( this.inject = new GuiToggleButton( this.guiLeft + 56, top, 11 * 16, 12 * 16, SecurityPermissions.INJECT.getUnlocalizedName(), SecurityPermissions.INJECT.getUnlocalizedTip() ) );

		this.buttonList.add( this.extract = new GuiToggleButton( this.guiLeft + 56 + 18, top, 11 * 16 + 1, 12 * 16 + 1, SecurityPermissions.EXTRACT.getUnlocalizedName(), SecurityPermissions.EXTRACT.getUnlocalizedTip() ) );

		this.buttonList.add( this.craft = new GuiToggleButton( this.guiLeft + 56 + 18 * 2, top, 11 * 16 + 2, 12 * 16 + 2, SecurityPermissions.CRAFT.getUnlocalizedName(), SecurityPermissions.CRAFT.getUnlocalizedTip() ) );

		this.buttonList.add( this.build = new GuiToggleButton( this.guiLeft + 56 + 18 * 3, top, 11 * 16 + 3, 12 * 16 + 3, SecurityPermissions.BUILD.getUnlocalizedName(), SecurityPermissions.BUILD.getUnlocalizedTip() ) );

		this.buttonList.add( this.security = new GuiToggleButton( this.guiLeft + 56 + 18 * 4, top, 11 * 16 + 4, 12 * 16 + 4, SecurityPermissions.SECURITY.getUnlocalizedName(), SecurityPermissions.SECURITY.getUnlocalizedTip() ) );
	}

	@Override
	protected void actionPerformed( net.minecraft.client.gui.GuiButton btn )
	{
		super.actionPerformed( btn );

		SecurityPermissions toggleSetting = null;

		if( btn == this.inject )
			toggleSetting = SecurityPermissions.INJECT;
		if( btn == this.extract )
			toggleSetting = SecurityPermissions.EXTRACT;
		if( btn == this.craft )
			toggleSetting = SecurityPermissions.CRAFT;
		if( btn == this.build )
			toggleSetting = SecurityPermissions.BUILD;
		if( btn == this.security )
			toggleSetting = SecurityPermissions.SECURITY;

		if( toggleSetting != null )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "TileSecurity.ToggleOption", toggleSetting.name() ) );
			}
			catch( IOException e )
			{
				AELog.error( e );
			}
		}
	}

	@Override
	protected String getBackground()
	{
		ContainerSecurity cs = (ContainerSecurity) this.inventorySlots;

		this.inject.setState( ( cs.security & ( 1 << SecurityPermissions.INJECT.ordinal() ) ) > 0 );
		this.extract.setState( ( cs.security & ( 1 << SecurityPermissions.EXTRACT.ordinal() ) ) > 0 );
		this.craft.setState( ( cs.security & ( 1 << SecurityPermissions.CRAFT.ordinal() ) ) > 0 );
		this.build.setState( ( cs.security & ( 1 << SecurityPermissions.BUILD.ordinal() ) ) > 0 );
		this.security.setState( ( cs.security & ( 1 << SecurityPermissions.SECURITY.ordinal() ) ) > 0 );

		return "guis/security.png";
	}

	@Override
	public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
	{
		super.drawFG( offsetX, offsetY, mouseX, mouseY );
		this.fontRendererObj.drawString( GuiText.SecurityCardEditor.getLocal(), 8, this.ySize - 96 + 1 - this.reservedSpace, 4210752 );
	}

	@Override
	public Enum getSortBy()
	{
		return SortOrder.NAME;
	}
}
