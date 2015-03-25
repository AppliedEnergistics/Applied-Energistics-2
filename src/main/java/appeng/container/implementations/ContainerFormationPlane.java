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
import net.minecraft.inventory.IInventory;

import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.OptionalSlotFakeTypeOnly;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.parts.automation.PartFormationPlane;
import appeng.util.Platform;


public class ContainerFormationPlane extends ContainerUpgradeable
{

	final PartFormationPlane storageBus;

	@GuiSync( 6 )
	public YesNo placeMode;

	public ContainerFormationPlane( InventoryPlayer ip, PartFormationPlane te )
	{
		super( ip, te );
		this.storageBus = te;
	}

	@Override
	protected int getHeight()
	{
		return 251;
	}

	@Override
	protected void setupConfig()
	{
		int xo = 8;
		int yo = 23 + 6;

		IInventory config = this.upgradeable.getInventoryByName( "config" );
		for( int y = 0; y < 7; y++ )
		{
			for( int x = 0; x < 9; x++ )
			{
				if( y < 2 )
					this.addSlotToContainer( new SlotFakeTypeOnly( config, y * 9 + x, xo + x * 18, yo + y * 18 ) );
				else
					this.addSlotToContainer( new OptionalSlotFakeTypeOnly( config, this, y * 9 + x, xo, yo, x, y, y - 2 ) );
			}
		}

		IInventory upgrades = this.upgradeable.getInventoryByName( "upgrades" );
		this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.invPlayer ) ).setNotDraggable() );
		this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18, this.invPlayer ) ).setNotDraggable() );
		this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, this.invPlayer ) ).setNotDraggable() );
		this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, this.invPlayer ) ).setNotDraggable() );
		this.addSlotToContainer( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 4, 187, 8 + 18 * 4, this.invPlayer ) ).setNotDraggable() );
	}

	@Override
	protected boolean supportCapacity()
	{
		return true;
	}

	@Override
	public int availableUpgrades()
	{
		return 5;
	}

	@Override
	public void detectAndSendChanges()
	{
		this.verifyPermissions( SecurityPermissions.BUILD, false );

		if( Platform.isServer() )
		{
			this.fzMode = (FuzzyMode) this.upgradeable.getConfigManager().getSetting( Settings.FUZZY_MODE );
			this.placeMode = (YesNo) this.upgradeable.getConfigManager().getSetting( Settings.PLACE_BLOCK );
		}

		this.standardDetectAndSendChanges();
	}

	@Override
	public boolean isSlotEnabled( int idx )
	{
		int upgrades = this.upgradeable.getInstalledUpgrades( Upgrades.CAPACITY );

		return upgrades > idx;
	}
}
