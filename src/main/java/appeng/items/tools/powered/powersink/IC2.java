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


import appeng.api.config.PowerUnits;
import appeng.integration.IntegrationType;
import appeng.transformer.annotations.Integration.Interface;
import appeng.transformer.annotations.Integration.InterfaceList;
import appeng.transformer.annotations.Integration.Method;
import com.google.common.base.Optional;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


@InterfaceList( value = { @Interface( iface = "ic2.api.item.ISpecialElectricItem", iname = IntegrationType.IC2 ), @Interface( iface = "ic2.api.item.IElectricItemManager", iname = IntegrationType.IC2 ) } )
public abstract class IC2 extends AERootPoweredItem implements IElectricItemManager, ISpecialElectricItem
{
	public IC2( final double powerCapacity, final Optional<String> subName )
	{
		super( powerCapacity, subName );
	}

	@Override
	public double charge( final ItemStack is, final double amount, final int tier, final boolean ignoreTransferLimit, final boolean simulate )
	{
		double addedAmt = amount;
		final double limit = this.getTransferLimit( is );

		if( !ignoreTransferLimit && amount > limit )
		{
			addedAmt = limit;
		}

		return addedAmt - ( (int) this.injectExternalPower( PowerUnits.EU, is, addedAmt, simulate ) );
	}

	@Override
	public double discharge( final ItemStack itemStack, final double amount, final int tier, final boolean ignoreTransferLimit, final boolean externally, final boolean simulate )
	{
		return 0;
	}

	@Override
	public double getCharge( final ItemStack is )
	{
		return (int) PowerUnits.AE.convertTo( PowerUnits.EU, this.getAECurrentPower( is ) );
	}

	@Override
	public boolean canUse( final ItemStack is, final double amount )
	{
		return this.getCharge( is ) > amount;
	}

	@Override
	public boolean use( final ItemStack is, final double amount, final EntityLivingBase entity )
	{
		if( this.canUse( is, amount ) )
		{
			// use the power..
			this.extractAEPower( is, PowerUnits.EU.convertTo( PowerUnits.AE, amount ) );
			return true;
		}
		return false;
	}

	@Override
	public void chargeFromArmor( final ItemStack itemStack, final EntityLivingBase entity )
	{
		// wtf?
	}

	@Override
	public String getToolTip( final ItemStack itemStack )
	{
		return null;
	}

	@Override
	public boolean canProvideEnergy( final ItemStack itemStack )
	{
		return false;
	}

	@Override
	public Item getChargedItem( final ItemStack itemStack )
	{
		return itemStack.getItem();
	}

	@Override
	public Item getEmptyItem( final ItemStack itemStack )
	{
		return itemStack.getItem();
	}

	@Override
	public double getMaxCharge( final ItemStack itemStack )
	{
		return PowerUnits.AE.convertTo( PowerUnits.EU, this.getAEMaxPower( itemStack ) );
	}

	@Override
	public int getTier( final ItemStack itemStack )
	{
		return 1;
	}

	@Override
	public double getTransferLimit( final ItemStack itemStack )
	{
		return Math.max( 32, this.getMaxCharge( itemStack ) / 200 );
	}

	@Override
	@Method( iname = IntegrationType.IC2 )
	public IElectricItemManager getManager( final ItemStack itemStack )
	{
		return this;
	}
}
