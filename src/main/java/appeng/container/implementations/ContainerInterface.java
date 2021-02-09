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

package appeng.container.implementations;


import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotNormal;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;


public class ContainerInterface extends ContainerUpgradeable
{

	private final DualityInterface myDuality;

	@GuiSync( 3 )
	public YesNo bMode = YesNo.NO;

	@GuiSync( 4 )
	public YesNo iTermMode = YesNo.YES;

	public ContainerInterface( final InventoryPlayer ip, final IInterfaceHost te )
	{
		super( ip, te.getInterfaceDuality().getHost() );

		this.myDuality = te.getInterfaceDuality();

		for( int x = 0; x < DualityInterface.NUMBER_OF_PATTERN_SLOTS; x++ )
		{
			this.addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, this.myDuality
					.getPatterns(), x, 8 + 18 * x, 90 + 7, this.getInventoryPlayer() ).setStackLimit( 1 ) );
		}

		for( int x = 0; x < DualityInterface.NUMBER_OF_CONFIG_SLOTS; x++ )
		{
			this.addSlotToContainer( new SlotFake( this.myDuality.getConfig(), x, 8 + 18 * x, 35 ) );
		}

		for( int x = 0; x < DualityInterface.NUMBER_OF_STORAGE_SLOTS; x++ )
		{
			this.addSlotToContainer( new SlotNormal( this.myDuality.getStorage(), x, 8 + 18 * x, 35 + 18 ) );
		}
	}

	@Override
	protected int getHeight()
	{
		return 211;
	}

	@Override
	protected void setupConfig()
	{
		this.setupUpgrades();
	}

	@Override
	public int availableUpgrades()
	{
		return 1;
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );
		super.detectAndSendChanges();
	}

	@Override
	protected void loadSettingsFromHost( final IConfigManager cm )
	{
		this.setBlockingMode( (YesNo) cm.getSetting( Settings.BLOCK ) );
		this.setInterfaceTerminalMode( (YesNo) cm.getSetting( Settings.INTERFACE_TERMINAL ) );
	}

	public YesNo getBlockingMode()
	{
		return this.bMode;
	}

	private void setBlockingMode( final YesNo bMode )
	{
		this.bMode = bMode;
	}

	public YesNo getInterfaceTerminalMode()
	{
		return this.iTermMode;
	}

	private void setInterfaceTerminalMode( final YesNo iTermMode )
	{
		this.iTermMode = iTermMode;
	}
}
