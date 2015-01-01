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

package appeng.util.inv;

import java.util.Iterator;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.inventory.ISpecialInventory;

import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;

public class AdaptorISpecialInventory extends InventoryAdaptor
{

	private final AdaptorIInventory remover;

	private final ISpecialInventory i;
	private final ForgeDirection d;

	public AdaptorISpecialInventory(ISpecialInventory s, ForgeDirection dd) {
		this.i = s;
		this.d = dd;

		if ( s instanceof ISidedInventory )
			this.remover = new AdaptorIInventory( new WrapperMCISidedInventory( (ISidedInventory) s, this.d ) );
		else
			this.remover = new AdaptorIInventory( s );
	}

	@Override
	public ItemStack removeSimilarItems(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination)
	{
		return this.remover.removeSimilarItems( how_many, filter, fuzzyMode, destination );
	}

	@Override
	public ItemStack simulateSimilarRemove(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination)
	{
		return this.remover.simulateSimilarRemove( how_many, filter, fuzzyMode, destination );
	}

	@Override
	public ItemStack removeItems(int how_many, ItemStack filter, IInventoryDestination destination)
	{
		return this.remover.removeItems( how_many, filter, destination );
	}

	@Override
	public ItemStack simulateRemove(int how_many, ItemStack filter, IInventoryDestination destination)
	{
		return this.remover.simulateRemove( how_many, filter, destination );
	}

	@Override
	public ItemStack addItems(ItemStack A)
	{
		if ( A == null )
			return null;
		if ( A.stackSize == 0 )
			return null;

		int used = this.i.addItem( A, true, this.d );
		ItemStack out = A.copy();
		out.stackSize -= used;
		if ( out.stackSize > 0 )
			return out;
		return null;
	}

	@Override
	public ItemStack simulateAdd(ItemStack A)
	{
		int used = this.i.addItem( A, false, this.d );
		ItemStack out = A.copy();
		out.stackSize -= used;
		if ( out.stackSize > 0 )
			return out;
		return null;
	}

	@Override
	public boolean containsItems()
	{
		if ( this.i instanceof ISidedInventory )
		{
			ISidedInventory sided = (ISidedInventory) this.i;
			int[] slots = sided.getAccessibleSlotsFromSide( this.d.ordinal() );

			if ( slots == null )
				return false;

			for (int slot : slots)
			{
				if ( this.i.getStackInSlot( slot ) != null )
				{
					return true;
				}
			}

			return false;
		}

		int s = this.i.getSizeInventory();
		for (int x = 0; x < s; x++)
			if ( this.i.getStackInSlot( x ) != null )
				return true;
		return false;
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return this.remover.iterator();
	}

}
