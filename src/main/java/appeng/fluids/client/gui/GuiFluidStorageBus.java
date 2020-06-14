/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.client.gui;


import appeng.client.gui.widgets.GuiActionButton;
import appeng.client.gui.widgets.GuiServerSettingToggleButton;
import appeng.container.implementations.ContainerPriority;
import net.minecraft.entity.player.PlayerInventory;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiSettingToggleButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.client.gui.widgets.GuiOptionalFluidSlot;
import appeng.fluids.container.ContainerFluidStorageBus;
import appeng.fluids.util.IAEFluidTank;
import net.minecraft.util.text.ITextComponent;


/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class GuiFluidStorageBus extends GuiUpgradeable<ContainerFluidStorageBus>
{
	private GuiSettingToggleButton<AccessRestriction> rwMode;
	private GuiSettingToggleButton<StorageFilter> storageFilter;

	public GuiFluidStorageBus(ContainerFluidStorageBus container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		this.ySize = 251;
	}

	@Override
	public void init()
	{
		super.init();

		final int xo = 8;
		final int yo = 23 + 6;

		final IAEFluidTank config = this.container.getFluidConfigInventory();

		for( int y = 0; y < 7; y++ )
		{
			for( int x = 0; x < 9; x++ )
			{
				final int idx = y * 9 + x;
				if( y < 2 )
				{
					this.guiSlots.add( new GuiFluidSlot( config, idx, idx, xo + x * 18, yo + y * 18 ) );
				}
				else
				{
					this.guiSlots.add( new GuiOptionalFluidSlot( config, container, idx, idx, y - 2, xo, yo, x, y ) );
				}
			}
		}
	}

	@Override
	protected void addButtons()
	{
		addButton( new GuiActionButton( this.guiLeft - 18, this.guiTop + 8, ActionItems.CLOSE, btn -> clear() ) );
		addButton( new GuiActionButton( this.guiLeft - 18, this.guiTop + 28, ActionItems.WRENCH, btn -> partition() ) );
		this.rwMode = new GuiServerSettingToggleButton<>( this.guiLeft - 18, this.guiTop + 48, Settings.ACCESS, AccessRestriction.READ_WRITE );
		this.storageFilter = new GuiServerSettingToggleButton<>( this.guiLeft - 18, this.guiTop + 68, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY );
		this.fuzzyMode = new GuiServerSettingToggleButton<>( this.guiLeft - 18, this.guiTop + 88, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );

		addButton( this.addButton( new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRenderer, btn -> openPriorityGui() ) ) );

		this.addButton( this.storageFilter );
		this.addButton( this.fuzzyMode );
		this.addButton( this.rwMode );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		this.font.drawString( this.getGuiDisplayName( this.getName().getLocal() ), 8, 6, 4210752 );
		this.font.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

		if( this.fuzzyMode != null )
		{
			this.fuzzyMode.set( this.cvb.getFuzzyMode() );
		}

		if( this.storageFilter != null )
		{
			this.storageFilter.set( ( (ContainerFluidStorageBus) this.cvb ).getStorageFilter() );
		}

		if( this.rwMode != null )
		{
			this.rwMode.set( ( (ContainerFluidStorageBus) this.cvb ).getReadWriteMode() );
		}
	}

	@Override
	protected String getBackground()
	{
		return "guis/storagebus.png";
	}

	private void partition() {
		NetworkHandler.instance().sendToServer( new PacketValueConfig( "StorageBus.Action", "Partition" ) );
	}

	private void clear() {
		NetworkHandler.instance().sendToServer( new PacketValueConfig( "StorageBus.Action", "Clear" ) );
	}

	private void openPriorityGui() {
		NetworkHandler.instance().sendToServer( new PacketSwitchGuis( ContainerPriority.TYPE ) );
	}

	@Override
	protected GuiText getName()
	{
		return GuiText.StorageBusFluids;
	}
}
