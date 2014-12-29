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

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class UpgradeInventory extends AppEngInternalInventory implements IAEAppEngInventory
{

	private final Object itemOrBlock;
	private final IAEAppEngInventory parent;

	private boolean cached = false;
	private int FuzzyUpgrades = 0;
	private int SpeedUpgrades = 0;
	private int RedstoneUpgrades = 0;
	private int CapacityUpgrades = 0;
	private int InverterUpgrades = 0;
	private int CraftingUpgrades = 0;

	public UpgradeInventory(Object itemOrBlock, IAEAppEngInventory _te, int s) {
		super( null, s );
		this.te = this;
		this.parent = _te;
		this.itemOrBlock = itemOrBlock;
	}

	@Override
	protected boolean eventsEnabled()
	{
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if ( itemstack == null )
			return false;
		Item it = itemstack.getItem();
		if ( it instanceof IUpgradeModule )
		{
			Upgrades u = ((IUpgradeModule) it).getType( itemstack );
			if ( u != null )
			{
				return this.getInstalledUpgrades( u ) < this.getMaxInstalled( u );
			}
		}
		return false;
	}

	public int getMaxInstalled(Upgrades u)
	{
		Integer max = null;
		for (ItemStack is : u.getSupported().keySet())
		{
			if ( is.getItem() == this.itemOrBlock )
			{
				max = u.getSupported().get( is );
				break;
			}
			else if ( is.getItem() instanceof ItemBlock && Block.getBlockFromItem( is.getItem() ) == this.itemOrBlock )
			{
				max = u.getSupported().get( is );
				break;
			}
			else if ( this.itemOrBlock instanceof ItemStack && Platform.isSameItem( (ItemStack) this.itemOrBlock, is ) )
			{
				max = u.getSupported().get( is );
				break;
			}
		}

		if ( max == null )
			return 0;
		return max;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	private void updateUpgradeInfo()
	{
		this.cached = true;
		this.InverterUpgrades = this.CapacityUpgrades = this.RedstoneUpgrades = this.SpeedUpgrades = this.FuzzyUpgrades = this.CraftingUpgrades = 0;

		for (ItemStack is : this)
		{
			if ( is == null || is.getItem() == null || !(is.getItem() instanceof IUpgradeModule) )
				continue;

			Upgrades myUpgrade = ((IUpgradeModule) is.getItem()).getType( is );
			switch (myUpgrade)
			{
			case CAPACITY:
				this.CapacityUpgrades++;
				break;
			case FUZZY:
				this.FuzzyUpgrades++;
				break;
			case REDSTONE:
				this.RedstoneUpgrades++;
				break;
			case SPEED:
				this.SpeedUpgrades++;
				break;
			case INVERTER:
				this.InverterUpgrades++;
				break;
			case CRAFTING:
				this.CraftingUpgrades++;
				break;
			default:
				break;
			}
		}

		this.CapacityUpgrades = Math.min( this.CapacityUpgrades, this.getMaxInstalled( Upgrades.CAPACITY ) );
		this.FuzzyUpgrades = Math.min( this.FuzzyUpgrades, this.getMaxInstalled( Upgrades.FUZZY ) );
		this.RedstoneUpgrades = Math.min( this.RedstoneUpgrades, this.getMaxInstalled( Upgrades.REDSTONE ) );
		this.SpeedUpgrades = Math.min( this.SpeedUpgrades, this.getMaxInstalled( Upgrades.SPEED ) );
		this.InverterUpgrades = Math.min( this.InverterUpgrades, this.getMaxInstalled( Upgrades.INVERTER ) );
		this.CraftingUpgrades = Math.min( this.CraftingUpgrades, this.getMaxInstalled( Upgrades.CRAFTING ) );
	}

	public int getInstalledUpgrades(Upgrades u)
	{
		if ( !this.cached )
			this.updateUpgradeInfo();

		switch (u)
		{
		case CAPACITY:
			return this.CapacityUpgrades;
		case FUZZY:
			return this.FuzzyUpgrades;
		case REDSTONE:
			return this.RedstoneUpgrades;
		case SPEED:
			return this.SpeedUpgrades;
		case INVERTER:
			return this.InverterUpgrades;
		case CRAFTING:
			return this.CraftingUpgrades;
		default:
			return 0;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound target)
	{
		super.readFromNBT( target );
		this.updateUpgradeInfo();
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		this.cached = false;
		if ( this.parent != null && Platform.isServer() )
			this.parent.onChangeInventory( inv, slot, mc, removedStack, newStack );
	}

	@Override
	public void saveChanges()
	{
		this.parent.saveChanges();
	}

}
