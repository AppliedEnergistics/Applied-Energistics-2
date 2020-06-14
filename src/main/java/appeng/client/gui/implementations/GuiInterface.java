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


import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.widgets.GuiServerSettingToggleButton;
import appeng.client.gui.widgets.GuiSettingToggleButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerInterface;
import appeng.container.implementations.ContainerPriority;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;


public class GuiInterface extends GuiUpgradeable<ContainerInterface>
{

	private GuiSettingToggleButton<YesNo> blockMode;
	private GuiToggleButton interfaceMode;

	public GuiInterface(ContainerInterface container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.ySize = 211;
	}

	@Override
	protected void addButtons()
	{
		this.addButton( new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRenderer, btn -> openPriorityGui() ) );

		this.blockMode = new GuiServerSettingToggleButton<>( this.guiLeft - 18, this.guiTop + 8, Settings.BLOCK, YesNo.NO );
		this.addButton( this.blockMode);

		this.interfaceMode = new GuiToggleButton( this.guiLeft - 18, this.guiTop + 26, 84, 85, GuiText.InterfaceTerminal
				.getLocal(), GuiText.InterfaceTerminalHint.getLocal(), btn -> selectNextInterfaceMode() );
		this.addButton( this.interfaceMode );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		if( this.blockMode != null )
		{
			this.blockMode.set( ( (ContainerInterface) this.cvb ).getBlockingMode() );
		}

		if( this.interfaceMode != null )
		{
			this.interfaceMode.setState( ( (ContainerInterface) this.cvb ).getInterfaceTerminalMode() == YesNo.YES );
		}

		this.font.drawString( this.getGuiDisplayName( GuiText.Interface.getLocal() ), 8, 6, 4210752 );

		this.font.drawString( GuiText.Config.getLocal(), 8, 6 + 11 + 7, 4210752 );
		this.font.drawString( GuiText.StoredItems.getLocal(), 8, 6 + 60 + 7, 4210752 );
		this.font.drawString( GuiText.Patterns.getLocal(), 8, 6 + 73 + 7, 4210752 );

		this.font.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );
	}

	@Override
	protected String getBackground()
	{
		return "guis/interface.png";
	}

	private void openPriorityGui() {
		NetworkHandler.instance().sendToServer( new PacketSwitchGuis( ContainerPriority.TYPE ) );
	}

	private void selectNextInterfaceMode() {
		final boolean backwards = getMinecraft().mouseHelper.isRightDown();
		NetworkHandler.instance().sendToServer( new PacketConfigButton( Settings.INTERFACE_TERMINAL, backwards ) );
	}

}
