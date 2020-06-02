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

import appeng.api.implementations.IUpgradeableHost;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

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


public class GuiCellWorkbench extends GuiUpgradeable<ContainerCellWorkbench>
{

	private GuiImgButton clear;
	private GuiImgButton partition;
	private GuiToggleButton copyMode;

	public GuiCellWorkbench(ContainerCellWorkbench container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.ySize = 251;
	}

	@Override
	protected void addButtons()
	{
		this.clear = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.ACTIONS, ActionItems.CLOSE );
		this.partition = new GuiImgButton( this.guiLeft - 18, this.guiTop + 28, Settings.ACTIONS, ActionItems.WRENCH );
		this.copyMode = new GuiToggleButton( this.guiLeft - 18, this.guiTop + 48, 11 * 16 + 5, 12 * 16 + 5, GuiText.CopyMode.getLocal(), GuiText.CopyModeDesc
				.getLocal() );
		this.fuzzyMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 68, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		this.addButton( this.fuzzyMode );
		this.addButton( this.partition );
		this.addButton( this.clear );
		this.addButton( this.copyMode );
	}

	@Override
	public void drawBG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.handleButtonVisibility();

		this.bindTexture( this.getBackground() );
		GuiUtils.drawTexturedModalRect( offsetX, offsetY, 0, 0, 211 - 34, this.ySize, 0 /* FIXME this.zlevel was used */ );
		if( this.drawUpgrades() )
		{
			if( this.container.availableUpgrades() <= 8 )
			{
				GuiUtils.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + this.container.availableUpgrades() * 18, 0 /* FIXME this.zlevel was used */ );
				GuiUtilsGuiUtils.drawTexturedModalRect( offsetX + 177, offsetY + ( 7 + ( this.container.availableUpgrades() ) * 18 ), 177, 151, 35, 7, 0 /* FIXME this.zlevel was used */ );
			}
			else if( this.container.availableUpgrades() <= 16 )
			{
				GuiUtils.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18, 0 /* FIXME this.zlevel was used */ );
				GuiUtils.drawTexturedModalRect( offsetX + 177, offsetY + ( 7 + ( 8 ) * 18 ), 177, 151, 35, 7, 0 /* FIXME this.zlevel was used */ );

				final int dx = this.container.availableUpgrades() - 8;
				GuiUtils.drawTexturedModalRect( offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + dx * 18, 0 /* FIXME this.zlevel was used */ );
				if( dx == 8 )
				{
					GuiUtils.drawTexturedModalRect( offsetX + 177 + 27, offsetY + ( 7 + ( dx ) * 18 ), 186, 151, 35 - 8, 7, 0 /* FIXME this.zlevel was used */ );
				}
				else
				{
					GuiUtils.drawTexturedModalRect( offsetX + 177 + 27 + 4, offsetY + ( 7 + ( dx ) * 18 ), 186 + 4, 151, 35 - 8, 7, 0 /* FIXME this.zlevel was used */ );
				}
			}
			else
			{
				GuiUtils.drawTexturedModalRect( offsetX + 177, offsetY, 177, 0, 35, 7 + 8 * 18, 0 /* FIXME this.zlevel was used */ );
				GuiUtils.drawTexturedModalRect( offsetX + 177, offsetY + ( 7 + ( 8 ) * 18 ), 177, 151, 35, 7, 0 /* FIXME this.zlevel was used */ );

				GuiUtils.drawTexturedModalRect( offsetX + 177 + 27, offsetY, 186, 0, 35 - 8, 7 + 8 * 18, 0 /* FIXME this.zlevel was used */ );
				GuiUtils.drawTexturedModalRect( offsetX + 177 + 27, offsetY + ( 7 + ( 8 ) * 18 ), 186, 151, 35 - 8, 7, 0 /* FIXME this.zlevel was used */ );

				final int dx = this.container.availableUpgrades() - 16;
				GuiUtils.drawTexturedModalRect( offsetX + 177 + 27 + 18, offsetY, 186, 0, 35 - 8, 7 + dx * 18, 0 /* FIXME this.zlevel was used */ );
				if( dx == 8 )
				{
					GuiUtils.drawTexturedModalRect( offsetX + 177 + 27 + 18, offsetY + ( 7 + ( dx ) * 18 ), 186, 151, 35 - 8, 7, 0 /* FIXME this.zlevel was used */ );
				}
				else
				{
					GuiUtils.drawTexturedModalRect( offsetX + 177 + 27 + 18 + 4, offsetY + ( 7 + ( dx ) * 18 ), 186 + 4, 151, 35 - 8, 7, 0 /* FIXME this.zlevel was used */ );
				}
			}
		}
		if( this.hasToolbox() )
		{
			GuiUtils.drawTexturedModalRect( offsetX + 178, offsetY + this.ySize - 90, 178, 161, 68, 68, 0 /* FIXME this.zlevel was used */ );
		}
	}

	@Override
	protected void handleButtonVisibility()
	{
		this.copyMode.setState( this.container.getCopyMode() == CopyMode.CLEAR_ON_REMOVE );

		boolean hasFuzzy = false;
		final IItemHandler inv = this.container.getCellUpgradeInventory();
		for( int x = 0; x < inv.getSlots(); x++ )
		{
			final ItemStack is = inv.getStackInSlot( x );
			if( !is.isEmpty() && is.getItem() instanceof IUpgradeModule )
			{
				if( ( (IUpgradeModule) is.getItem() ).getType( is ) == Upgrades.FUZZY )
				{
					hasFuzzy = true;
				}
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
	protected boolean drawUpgrades()
	{
		return this.container.availableUpgrades() > 0;
	}

	@Override
	protected GuiText getName()
	{
		return GuiText.CellWorkbench;
	}

	@Override
	protected void actionPerformed( final GuiButton btn )
	{
		try
		{
			if( btn == this.copyMode )
			{
				NetworkHandler.instance().sendToServer( new PacketValueConfig( "CellWorkbench.Action", "CopyMode" ) );
			}
			else if( btn == this.partition )
			{
				NetworkHandler.instance().sendToServer( new PacketValueConfig( "CellWorkbench.Action", "Partition" ) );
			}
			else if( btn == this.clear )
			{
				NetworkHandler.instance().sendToServer( new PacketValueConfig( "CellWorkbench.Action", "Clear" ) );
			}
			else if( btn == this.fuzzyMode )
			{
				final boolean backwards = Mouse.isButtonDown( 1 );

				FuzzyMode fz = (FuzzyMode) this.fuzzyMode.getCurrentValue();
				fz = Platform.rotateEnum( fz, backwards, Settings.FUZZY_MODE.getPossibleValues() );

				NetworkHandler.instance().sendToServer( new PacketValueConfig( "CellWorkbench.Fuzzy", fz.name() ) );
			}
			else
			{
				super.actionPerformed( btn );
			}
		}
		catch( final IOException ignored )
		{
		}
	}
}
