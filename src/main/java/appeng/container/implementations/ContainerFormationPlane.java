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


import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.items.IItemHandler;

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

	@GuiSync( 6 )
	public YesNo placeMode;

	public ContainerFormationPlane(ContainerType<?> containerType, int id, final PlayerInventory ip, final PartFormationPlane te )
	{
		super( containerType, id, ip, te );
	}

	@Override
	protected int getHeight()
	{
		return 251;
	}

	@Override
	protected void setupConfig()
	{
		final int xo = 8;
		final int yo = 23 + 6;

		final IItemHandler config = this.getUpgradeable().getInventoryByName( "config" );
		for( int y = 0; y < 7; y++ )
		{
			for( int x = 0; x < 9; x++ )
			{
				if( y < 2 )
				{
					this.addSlot( new SlotFakeTypeOnly( config, y * 9 + x, xo + x * 18, yo + y * 18 ) );
				}
				else
				{
					this.addSlot( new OptionalSlotFakeTypeOnly( config, this, y * 9 + x, xo, yo, x, y, y - 2 ) );
				}
			}
		}

		final IItemHandler upgrades = this.getUpgradeable().getInventoryByName( "upgrades" );
		this.addSlot( ( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.getPlayerInventory() ) )
				.setNotDraggable() );
		this.addSlot(
				( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18, this.getPlayerInventory() ) )
						.setNotDraggable() );
		this.addSlot(
				( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, this.getPlayerInventory() ) )
						.setNotDraggable() );
		this.addSlot(
				( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, this.getPlayerInventory() ) )
						.setNotDraggable() );
		this.addSlot(
				( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 4, 187, 8 + 18 * 4, this.getPlayerInventory() ) )
						.setNotDraggable() );
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
			this.setFuzzyMode( (FuzzyMode) this.getUpgradeable().getConfigManager().getSetting( Settings.FUZZY_MODE ) );
			this.setPlaceMode( (YesNo) this.getUpgradeable().getConfigManager().getSetting( Settings.PLACE_BLOCK ) );
		}

		this.standardDetectAndSendChanges();
	}

	@Override
	public boolean isSlotEnabled( final int idx )
	{
		final int upgrades = this.getUpgradeable().getInstalledUpgrades( Upgrades.CAPACITY );

		return upgrades > idx;
	}

	public YesNo getPlaceMode()
	{
		return this.placeMode;
	}

	private void setPlaceMode( final YesNo placeMode )
	{
		this.placeMode = placeMode;
	}
}
