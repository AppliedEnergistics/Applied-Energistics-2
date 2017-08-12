/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.core.features.registries.charger;


import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.features.IChargerRegistry;


public class ChargerRegistry implements IChargerRegistry
{

	private final Map<CachedItemStack, Double> chargeRates;

	public ChargerRegistry()
	{
		this.chargeRates = new HashMap<>();
	}

	@Override
	public double getChargeRate( ItemStack itemStack )
	{
		Preconditions.checkNotNull( itemStack );
		Preconditions.checkArgument( !itemStack.isEmpty() );

		return this.chargeRates.getOrDefault( new CachedItemStack( itemStack ), 150d );
	}

	@Override
	public void addChargeRate( ItemStack itemStack, double value )
	{
		Preconditions.checkNotNull( itemStack );
		Preconditions.checkArgument( !itemStack.isEmpty() );
		Preconditions.checkArgument( value > 0d );

		this.chargeRates.put( new CachedItemStack( itemStack ), value );
	}

	@Override
	public void removeChargeRate( @Nonnull ItemStack itemStack )
	{
		Preconditions.checkNotNull( itemStack );
		Preconditions.checkArgument( !itemStack.isEmpty() );

		this.chargeRates.remove( new CachedItemStack( itemStack ) );
	}

	private static final class CachedItemStack
	{

		private final Item item;
		private final int metaData;

		CachedItemStack( ItemStack itemStack )
		{
			this.item = itemStack.getItem();
			this.metaData = itemStack.getMetadata();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( item == null ) ? 0 : item.hashCode() );
			result = prime * result + metaData;
			return result;
		}

		@Override
		public boolean equals( Object obj )
		{
			if( this == obj )
			{
				return true;
			}
			if( obj == null )
			{
				return false;
			}
			if( getClass() != obj.getClass() )
			{
				return false;
			}

			final CachedItemStack other = (CachedItemStack) obj;

			if( item == null && other.item != null )
			{
				return false;
			}
			else if( !item.equals( other.item ) )
			{
				return false;
			}

			if( metaData != other.metaData )
			{
				return false;
			}

			return true;
		}

	}
}
