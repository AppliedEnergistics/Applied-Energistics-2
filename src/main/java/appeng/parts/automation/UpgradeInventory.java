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

package appeng.parts.automation;


import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;


public abstract class UpgradeInventory extends AppEngInternalInventory implements IAEAppEngInventory
{
	private final IAEAppEngInventory parent;

	private boolean cached = false;
	private int fuzzyUpgrades = 0;
	private int speedUpgrades = 0;
	private int redstoneUpgrades = 0;
	private int capacityUpgrades = 0;
	private int inverterUpgrades = 0;
	private int craftingUpgrades = 0;

	public UpgradeInventory( final IAEAppEngInventory parent, final int s )
	{
		super( null, s );
		this.setTileEntity( this );
		this.parent = parent;
	}

	@Override
	protected boolean eventsEnabled()
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isItemValidForSlot( final int i, final ItemStack itemstack )
	{
		if( itemstack == null )
		{
			return false;
		}
		final Item it = itemstack.getItem();
		if( it instanceof IUpgradeModule )
		{
			final Upgrades u = ( (IUpgradeModule) it ).getType( itemstack );
			if( u != null )
			{
				return this.getInstalledUpgrades( u ) < this.getMaxInstalled( u );
			}
		}
		return false;
	}

	public int getInstalledUpgrades( final Upgrades u )
	{
		if( !this.cached )
		{
			this.updateUpgradeInfo();
		}

		switch( u )
		{
			case CAPACITY:
				return this.capacityUpgrades;
			case FUZZY:
				return this.fuzzyUpgrades;
			case REDSTONE:
				return this.redstoneUpgrades;
			case SPEED:
				return this.speedUpgrades;
			case INVERTER:
				return this.inverterUpgrades;
			case CRAFTING:
				return this.craftingUpgrades;
			default:
				return 0;
		}
	}

	public abstract int getMaxInstalled( Upgrades upgrades );

	private void updateUpgradeInfo()
	{
		this.cached = true;
		this.inverterUpgrades = this.capacityUpgrades = this.redstoneUpgrades = this.speedUpgrades = this.fuzzyUpgrades = this.craftingUpgrades = 0;

		for( final ItemStack is : this )
		{
			if( is == null || is.getItem() == null || !( is.getItem() instanceof IUpgradeModule ) )
			{
				continue;
			}

			final Upgrades myUpgrade = ( (IUpgradeModule) is.getItem() ).getType( is );
			switch( myUpgrade )
			{
				case CAPACITY:
					this.capacityUpgrades++;
					break;
				case FUZZY:
					this.fuzzyUpgrades++;
					break;
				case REDSTONE:
					this.redstoneUpgrades++;
					break;
				case SPEED:
					this.speedUpgrades++;
					break;
				case INVERTER:
					this.inverterUpgrades++;
					break;
				case CRAFTING:
					this.craftingUpgrades++;
					break;
				default:
					break;
			}
		}

		this.capacityUpgrades = Math.min( this.capacityUpgrades, this.getMaxInstalled( Upgrades.CAPACITY ) );
		this.fuzzyUpgrades = Math.min( this.fuzzyUpgrades, this.getMaxInstalled( Upgrades.FUZZY ) );
		this.redstoneUpgrades = Math.min( this.redstoneUpgrades, this.getMaxInstalled( Upgrades.REDSTONE ) );
		this.speedUpgrades = Math.min( this.speedUpgrades, this.getMaxInstalled( Upgrades.SPEED ) );
		this.inverterUpgrades = Math.min( this.inverterUpgrades, this.getMaxInstalled( Upgrades.INVERTER ) );
		this.craftingUpgrades = Math.min( this.craftingUpgrades, this.getMaxInstalled( Upgrades.CRAFTING ) );
	}

	@Override
	public void readFromNBT( final NBTTagCompound target )
	{
		super.readFromNBT( target );
		this.updateUpgradeInfo();
	}

	@Override
	public void saveChanges()
	{
		this.parent.saveChanges();
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{
		this.cached = false;
		if( this.parent != null && Platform.isServer() )
		{
			this.parent.onChangeInventory( inv, slot, mc, removedStack, newStack );
		}
	}
}
