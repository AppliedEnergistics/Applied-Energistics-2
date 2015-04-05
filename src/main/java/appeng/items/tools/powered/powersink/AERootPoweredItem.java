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

package appeng.items.tools.powered.powersink;


import java.text.MessageFormat;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.base.Optional;

import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


public abstract class AERootPoweredItem extends AEBaseItem implements IAEItemPowerStorage
{
	private static final String POWER_NBT_KEY = "internalCurrentPower";
	private final double powerCapacity;

	public AERootPoweredItem( double powerCapacity, Optional<String> subName )
	{
		super( subName );
		this.setMaxDamage( 32 );
		this.hasSubtypes = false;
		this.setFull3D();

		this.powerCapacity = powerCapacity;
	}

	@Override
	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayAdditionalInformation )
	{
		NBTTagCompound tag = stack.getTagCompound();
		double internalCurrentPower = 0;
		double internalMaxPower = this.getAEMaxPower( stack );

		if( tag != null )
		{
			internalCurrentPower = tag.getDouble( "internalCurrentPower" );
		}

		double percent = internalCurrentPower / internalMaxPower;

		lines.add( GuiText.StoredEnergy.getLocal() + ':' + MessageFormat.format( " {0,number,#} ", internalCurrentPower ) + Platform.gui_localize( PowerUnits.AE.unlocalizedName ) + " - " + MessageFormat.format( " {0,number,#.##%} ", percent ) );
	}

	@Override
	public boolean isDamageable()
	{
		return true;
	}

	@Override
	public void getSubItems( Item id, CreativeTabs tab, List list )
	{
		super.getSubItems( id, tab, list );

		ItemStack charged = new ItemStack( this, 1 );
		NBTTagCompound tag = Platform.openNbtData( charged );
		tag.setDouble( "internalCurrentPower", this.getAEMaxPower( charged ) );
		tag.setDouble( "internalMaxPower", this.getAEMaxPower( charged ) );
		list.add( charged );
	}

	@Override
	public boolean isRepairable()
	{
		return false;
	}

	@Override
	public double getDurabilityForDisplay( ItemStack is )
	{
		return 1 - this.getAECurrentPower( is ) / this.getAEMaxPower( is );
	}

	@Override
	public boolean isDamaged( ItemStack stack )
	{
		return true;
	}

	@Override
	public void setDamage( ItemStack stack, int damage )
	{

	}

	private double getInternalBattery( ItemStack is, batteryOperation op, double adjustment )
	{
		NBTTagCompound data = Platform.openNbtData( is );

		double currentStorage = data.getDouble( POWER_NBT_KEY );
		double maxStorage = this.getAEMaxPower( is );

		switch( op )
		{
			case INJECT:
				currentStorage += adjustment;
				if( currentStorage > maxStorage )
				{
					double diff = currentStorage - maxStorage;
					data.setDouble( POWER_NBT_KEY, maxStorage );
					return diff;
				}
				data.setDouble( POWER_NBT_KEY, currentStorage );
				return 0;
			case EXTRACT:
				if( currentStorage > adjustment )
				{
					currentStorage -= adjustment;
					data.setDouble( POWER_NBT_KEY, currentStorage );
					return adjustment;
				}
				data.setDouble( POWER_NBT_KEY, 0 );
				return currentStorage;
			default:
				break;
		}

		return currentStorage;
	}

	/**
	 * inject external
	 */
	double injectExternalPower( PowerUnits input, ItemStack is, double amount, boolean simulate )
	{
		if( simulate )
		{
			int requiredEU = (int) PowerUnits.AE.convertTo( PowerUnits.EU, this.getAEMaxPower( is ) - this.getAECurrentPower( is ) );
			if( amount < requiredEU )
				return 0;
			return amount - requiredEU;
		}
		else
		{
			double powerRemainder = this.injectAEPower( is, PowerUnits.EU.convertTo( PowerUnits.AE, amount ) );
			return PowerUnits.AE.convertTo( PowerUnits.EU, powerRemainder );
		}
	}

	@Override
	public double injectAEPower( ItemStack is, double amt )
	{
		return this.getInternalBattery( is, batteryOperation.INJECT, amt );
	}

	@Override
	public double extractAEPower( ItemStack is, double amt )
	{
		return this.getInternalBattery( is, batteryOperation.EXTRACT, amt );
	}

	@Override
	public double getAEMaxPower( ItemStack is )
	{
		return this.powerCapacity;
	}

	@Override
	public double getAECurrentPower( ItemStack is )
	{
		return this.getInternalBattery( is, batteryOperation.STORAGE, 0 );
	}

	@Override
	public AccessRestriction getPowerFlow( ItemStack is )
	{
		return AccessRestriction.WRITE;
	}

	private enum batteryOperation
	{
		STORAGE, INJECT, EXTRACT
	}
}
