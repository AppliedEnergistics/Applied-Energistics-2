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
import appeng.container.slot.OptionalSlotFakeTypeOnly;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.parts.automation.PartFormationPlane;
import appeng.util.Platform;

public class ContainerFormationPlane extends ContainerUpgradeable
{

	final PartFormationPlane storageBus;

	public ContainerFormationPlane(InventoryPlayer ip, PartFormationPlane te) {
		super( ip, te );
		storageBus = te;
	}

	@Override
	protected int getHeight()
	{
		return 251;
	}

	@Override
	public int availableUpgrades()
	{
		return 5;
	}

	@Override
	protected boolean supportCapacity()
	{
		return true;
	}

	@Override
	public boolean isSlotEnabled(int idx)
	{
		int upgrades = upgradeable.getInstalledUpgrades( Upgrades.CAPACITY );

		return upgrades > idx;
	}

	@Override
	protected void setupConfig()
	{
		int xo = 8;
		int yo = 23 + 6;

		IInventory config = upgradeable.getInventoryByName( "config" );
		for (int y = 0; y < 7; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				if ( y < 2 )
					addSlotToContainer( new SlotFakeTypeOnly( config, y * 9 + x, xo + x * 18, yo + y * 18 ) );
				else
					addSlotToContainer( new OptionalSlotFakeTypeOnly( config, this, y * 9 + x, xo, yo, x, y, y - 2 ) );
			}
		}

		IInventory upgrades = upgradeable.getInventoryByName( "upgrades" );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8 + 0, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 4, 187, 8 + 18 * 4, invPlayer )).setNotDraggable() );
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			this.fzMode = (FuzzyMode) this.upgradeable.getConfigManager().getSetting( Settings.FUZZY_MODE );
		}

		standardDetectAndSendChanges();
	}

}
