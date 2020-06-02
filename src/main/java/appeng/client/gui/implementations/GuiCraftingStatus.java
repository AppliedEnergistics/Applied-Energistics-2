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

import appeng.container.implementations.ContainerCraftingCPU;
import appeng.core.Api;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
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

	private GuiButton selectCPU;

	private GuiTabButton originalGuiBtn;
	private GuiBridge originalGui;
	private ItemStack myIcon = ItemStack.EMPTY;

	public GuiCraftingStatus(ContainerCraftingCPU container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);

		final Object target = this.container.getTarget();
		final IDefinitions definitions = Api.INSTANCE.definitions();
		final IParts parts = definitions.parts();

		if( target instanceof WirelessTerminalGuiObject )
		{
			this.myIcon = definitions.items().wirelessTerminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );

			this.originalGui = GuiBridge.GUI_WIRELESS_TERM;
		}

		if( target instanceof PartTerminal )
		{
			this.myIcon = parts.terminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );

			this.originalGui = GuiBridge.GUI_ME;
		}

		if( target instanceof PartCraftingTerminal )
		{
			this.myIcon = parts.craftingTerminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );

			this.originalGui = GuiBridge.GUI_CRAFTING_TERMINAL;
		}

		if( target instanceof PartPatternTerminal )
		{
			this.myIcon = parts.patternTerminal().maybeStack( 1 ).orElse( ItemStack.EMPTY );

			this.originalGui = GuiBridge.GUI_PATTERN_TERMINAL;
		}
	}

	@Override
	protected void actionPerformed( final GuiButton btn ) throws IOException
	{
		super.actionPerformed( btn );

		final boolean backwards = Mouse.isButtonDown( 1 );

		if( btn == this.selectCPU )
		{
			try
			{
				NetworkHandler.instance().sendToServer( new PacketValueConfig( "Terminal.Cpu", backwards ? "Prev" : "Next" ) );
			}
			catch( final IOException e )
			{
				AELog.debug( e );
			}
		}

		if( btn == this.originalGuiBtn )
		{
			NetworkHandler.instance().sendToServer( new PacketSwitchGuis( this.originalGui ) );
		}
	}

	@Override
	public void init()
	{
		super.init();

		this.selectCPU = new GuiButton( 0, this.guiLeft + 8, this.guiTop + this.ySize - 25, 150, 20, GuiText.CraftingCPU
				.getLocal() + ": " + GuiText.NoCraftingCPUs );
		// selectCPU.enabled = false;
		this.addButton( this.selectCPU );

		if( !this.myIcon.isEmpty() )
		{
			this.addButton(
					this.originalGuiBtn = new GuiTabButton( this.guiLeft + 213, this.guiTop - 4, this.myIcon, this.myIcon.getDisplayName(), this.itemRender ) );
			this.originalGuiBtn.setHideEdge( 13 );
		}
	}

	@Override
	public void render(final int mouseX, final int mouseY, final float btn )
	{
		this.updateCPUButtonText();
		super.render( mouseX, mouseY, btn );
	}

	private void updateCPUButtonText()
	{
		String btnTextText = GuiText.NoCraftingJobs.getLocal();

		if( this.container.selectedCpu >= 0 )// && status.selectedCpu < status.cpus.size() )
		{
			if( this.container.myName.length() > 0 )
			{
				final String name = this.container.myName.substring( 0, Math.min( 20, this.container.myName.length() ) );
				btnTextText = GuiText.CPUs.getLocal() + ": " + name;
			}
			else
			{
				btnTextText = GuiText.CPUs.getLocal() + ": #" + this.container.selectedCpu;
			}
		}

		if( this.container.noCPU )
		{
			btnTextText = GuiText.NoCraftingJobs.getLocal();
		}

		this.selectCPU.displayString = btnTextText;
	}

	@Override
	protected String getGuiDisplayName( final String in )
	{
		return in; // the cup name is on the button
	}
}
