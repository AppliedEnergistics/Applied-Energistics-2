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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.api.config.ActionItems;
import appeng.api.config.CopyMode;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerCellWorkbench;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.tile.misc.TileCellWorkbench;
import appeng.util.Platform;

public class GuiCellWorkbench extends GuiUpgradeable
{

	final ContainerCellWorkbench workbench;
	final TileCellWorkbench tcw;

	GuiImgButton clear;
	GuiImgButton partition;
	GuiToggleButton copyMode;

	public GuiCellWorkbench(InventoryPlayer inventoryPlayer, TileCellWorkbench te) {
		super( new ContainerCellWorkbench( inventoryPlayer, te ) );
		this.workbench = (ContainerCellWorkbench) this.inventorySlots;
		this.ySize = 251;
		this.tcw = te;
	}

	@Override
	protected boolean drawUpgrades()
	{
		return this.workbench.availableUpgrades() > 0;
	}

	@Override
	public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY)
	{
		this.handleButtonVisibility();

		this.bindTexture( this.getBackground() );
		this.drawTexturedModalRect( offsetX, offsetY, 0, 0, 211 - 34, this.ySize );
		if ( this.drawUpgrades() )
		{
			if ( this.workbench.availableUpgrades() <= 8 )
			{
				this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + this.workbench.availableUpgrades() * 18 );
				this.drawTexturedModalRect( offsetX + 177, offsetY + (7 + (this.workbench.availableUpgrades()) * 18), 177, 151, 35, 7 );
			}
			else if ( this.workbench.availableUpgrades() <= 16 )
			{
				this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18 );
				this.drawTexturedModalRect( offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7 );

				int dx = this.workbench.availableUpgrades() - 8;
				this.drawTexturedModalRect( offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + dx * 18 );
				if ( dx == 8 )
					this.drawTexturedModalRect( offsetX + 177 + 27, offsetY + (7 + (dx) * 18), 186, 151, 35 - 8, 7 );
				else
					this.drawTexturedModalRect( offsetX + 177 + 27 + 4, offsetY + (7 + (dx) * 18), 186 + 4, 151, 35 - 8, 7 );

			}
			else
			{
				this.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18 );
				this.drawTexturedModalRect( offsetX + 177, offsetY + (7 + (8) * 18), 177, 151, 35, 7 );

				this.drawTexturedModalRect( offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + 8 * 18 );
				this.drawTexturedModalRect( offsetX + 177 + 27, offsetY + (7 + (8) * 18), 186, 151, 35 - 8, 7 );

				int dx = this.workbench.availableUpgrades() - 16;
				this.drawTexturedModalRect( offsetX + 177 + 27 + 18, offsetY, 186, 0, 35 - 8, 7 + dx * 18 );
				if ( dx == 8 )
					this.drawTexturedModalRect( offsetX + 177 + 27 + 18, offsetY + (7 + (dx) * 18), 186, 151, 35 - 8, 7 );
				else
					this.drawTexturedModalRect( offsetX + 177 + 27 + 18 + 4, offsetY + (7 + (dx) * 18), 186 + 4, 151, 35 - 8, 7 );
			}
		}
		if ( this.hasToolbox() )
			this.drawTexturedModalRect( offsetX + 178, offsetY + this.ySize - 90, 178, 161, 68, 68 );
	}

	@Override
	protected void actionPerformed(GuiButton btn)
	{
		try
		{
			if ( btn == this.copyMode )
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "CellWorkbench.Action", "CopyMode" ) );
			}
			else if ( btn == this.partition )
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "CellWorkbench.Action", "Partition" ) );
			}
			else if ( btn == this.clear )
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "CellWorkbench.Action", "Clear" ) );
			}
			else if ( btn == this.fuzzyMode )
			{
				boolean backwards = Mouse.isButtonDown( 1 );

				FuzzyMode fz = (FuzzyMode) this.fuzzyMode.getCurrentValue();
				fz = Platform.rotateEnum( fz, backwards, Settings.FUZZY_MODE.getPossibleValues() );

				NetworkHandler.instance.sendToServer( new PacketValueConfig( "CellWorkbench.Fuzzy", fz.name() ) );
			}
			else
				super.actionPerformed( btn );
		}
		catch (IOException ignored)
		{
		}
	}

	@Override
	protected void addButtons()
	{
		this.clear = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.ACTIONS, ActionItems.CLOSE );
		this.partition = new GuiImgButton( this.guiLeft - 18, this.guiTop + 28, Settings.ACTIONS, ActionItems.WRENCH );
		this.copyMode = new GuiToggleButton( this.guiLeft - 18, this.guiTop + 48, 11 * 16 + 5, 12 * 16 + 5, GuiText.CopyMode.getLocal(), GuiText.CopyModeDesc.getLocal() );
		this.fuzzyMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 68, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		this.buttonList.add( this.fuzzyMode );
		this.buttonList.add( this.partition );
		this.buttonList.add( this.clear );
		this.buttonList.add( this.copyMode );
	}

	@Override
	protected void handleButtonVisibility()
	{
		this.copyMode.setState( this.workbench.copyMode == CopyMode.CLEAR_ON_REMOVE );

		boolean hasFuzzy = false;
		IInventory inv = this.workbench.getCellUpgradeInventory();
		for (int x = 0; x < inv.getSizeInventory(); x++)
		{
			ItemStack is = inv.getStackInSlot( x );
			if ( is != null && is.getItem() instanceof IUpgradeModule )
			{
				if ( ((IUpgradeModule) is.getItem()).getType( is ) == Upgrades.FUZZY )
					hasFuzzy = true;
			}
		}
		this.fuzzyMode.setVisibility( hasFuzzy );
	}

	@Override
	protected String getBackground()
	{
		return "guis/cellworkbench.png";
	}

	@Override
	protected GuiText getName()
	{
		return GuiText.CellWorkbench;
	}
}
