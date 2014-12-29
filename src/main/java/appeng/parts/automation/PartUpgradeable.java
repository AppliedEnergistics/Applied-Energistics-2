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

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Upgrades;
import appeng.api.util.IConfigManager;
import appeng.parts.PartBasicState;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;

public class PartUpgradeable extends PartBasicState implements IAEAppEngInventory, IConfigManagerHost
{

	final IConfigManager settings = new ConfigManager( this );
	private final UpgradeInventory upgrades = new UpgradeInventory( this.is, this, this.getUpgradeSlots() );

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return this.upgrades.getInstalledUpgrades( u );
	}

	@Override
	public boolean canConnectRedstone()
	{
		return this.upgrades.getMaxInstalled( Upgrades.REDSTONE ) > 0;
	}

	protected int getUpgradeSlots()
	{
		return 4;
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched)
	{
		for (ItemStack is : this.upgrades)
			if ( is != null )
				drops.add( is );
	}

	@Override
	public void writeToNBT(net.minecraft.nbt.NBTTagCompound extra)
	{
		super.writeToNBT( extra );
		this.settings.writeToNBT( extra );
		this.upgrades.writeToNBT( extra, "upgrades" );
	}

	@Override
	public void readFromNBT(net.minecraft.nbt.NBTTagCompound extra)
	{
		super.readFromNBT( extra );
		this.settings.readFromNBT( extra );
		this.upgrades.readFromNBT( extra, "upgrades" );
	}

	public PartUpgradeable(Class c, ItemStack is) {
		super( c, is );
		this.upgrades.setMaxStackSize( 1 );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.settings;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "upgrades" ) )
			return this.upgrades;

		return null;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{

	}

	public void upgradesChanged()
	{

	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		if ( inv == this.upgrades )
		{
			this.upgradesChanged();
		}
	}

	public RedstoneMode getRSMode()
	{
		return null;
	}

	protected boolean isSleeping()
	{
		if ( this.getInstalledUpgrades( Upgrades.REDSTONE ) > 0 )
		{
			switch (this.getRSMode())
			{
			case IGNORE:
				return false;

			case HIGH_SIGNAL:
				if ( this.host.hasRedstone( this.side ) )
					return false;

				break;

			case LOW_SIGNAL:
				if ( !this.host.hasRedstone( this.side ) )
					return false;

				break;

			case SIGNAL_PULSE:
			default:
				break;

			}

			return true;
		}

		return false;
	}
}
