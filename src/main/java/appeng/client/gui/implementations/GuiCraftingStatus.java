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

/**
 * 
 */
package appeng.client.gui.implementations;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Mouse;

import appeng.api.AEApi;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartTerminal;

public class GuiCraftingStatus extends GuiCraftingCPU
{

	final ContainerCraftingStatus ccc;
	GuiButton selectCPU;

	GuiTabButton originalGuiBtn;
	GuiBridge OriginalGui;
	ItemStack myIcon = null;

	public GuiCraftingStatus(InventoryPlayer inventoryPlayer, ITerminalHost te) {
		super( new ContainerCraftingStatus( inventoryPlayer, te ) );

		this.ccc = (ContainerCraftingStatus) this.inventorySlots;
		Object target = this.ccc.getTarget();

		if ( target instanceof WirelessTerminalGuiObject )
		{
			this.myIcon = AEApi.instance().items().itemWirelessTerminal.stack( 1 );
			this.OriginalGui = GuiBridge.GUI_WIRELESS_TERM;
		}

		if ( target instanceof PartTerminal )
		{
			this.myIcon = AEApi.instance().parts().partTerminal.stack( 1 );
			this.OriginalGui = GuiBridge.GUI_ME;
		}

		if ( target instanceof PartCraftingTerminal )
		{
			this.myIcon = AEApi.instance().parts().partCraftingTerminal.stack( 1 );
			this.OriginalGui = GuiBridge.GUI_CRAFTING_TERMINAL;
		}

		if ( target instanceof PartPatternTerminal )
		{
			this.myIcon = AEApi.instance().parts().partPatternTerminal.stack( 1 );
			this.OriginalGui = GuiBridge.GUI_PATTERN_TERMINAL;
		}
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		super.actionPerformed( btn );

		boolean backwards = Mouse.isButtonDown( 1 );

		if ( btn == this.selectCPU )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "Terminal.Cpu", backwards ? "Prev" : "Next" ) );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}

		if ( btn == this.originalGuiBtn )
		{
			NetworkHandler.instance.sendToServer( new PacketSwitchGuis( this.OriginalGui ) );
		}
	}

	@Override
	protected String getGuiDisplayName(String in)
	{
		return in; // the cup name is on the button
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.selectCPU = new GuiButton( 0, this.guiLeft + 8, this.guiTop + this.ySize - 25, 150, 20, GuiText.CraftingCPU.getLocal() + ": " + GuiText.NoCraftingCPUs );
		// selectCPU.enabled = false;
		this.buttonList.add( this.selectCPU );

		if ( this.myIcon != null )
		{
			this.buttonList.add( this.originalGuiBtn = new GuiTabButton( this.guiLeft + 213, this.guiTop - 4, this.myIcon, this.myIcon.getDisplayName(), itemRender ) );
			this.originalGuiBtn.hideEdge = 13;
		}
	}

	private void updateCPUButtonText()
	{
		String btnTextText = GuiText.NoCraftingJobs.getLocal();

		if ( this.ccc.selectedCpu >= 0 )// && ccc.selectedCpu < ccc.cpus.size() )
		{
			if ( this.ccc.myName.length() > 0 )
			{
				String name = this.ccc.myName.substring( 0, Math.min( 20, this.ccc.myName.length() ) );
				btnTextText = GuiText.CPUs.getLocal() + ": " + name;
			}
			else
				btnTextText = GuiText.CPUs.getLocal() + ": #" + this.ccc.selectedCpu;
		}

		if ( this.ccc.noCPU )
			btnTextText = GuiText.NoCraftingJobs.getLocal();

		this.selectCPU.displayString = btnTextText;
	}

	@Override
	public void drawScreen(int mouse_x, int mouse_y, float btn)
	{
		this.updateCPUButtonText();
		super.drawScreen( mouse_x, mouse_y, btn );
	}
}
