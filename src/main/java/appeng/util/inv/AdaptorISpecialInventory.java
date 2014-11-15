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
import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import buildcraft.api.inventory.ISpecialInventory;

public class AdaptorISpecialInventory extends InventoryAdaptor
{

	private final AdaptorIInventory remover;

	private final ISpecialInventory i;
	private final ForgeDirection d;

	public AdaptorISpecialInventory(ISpecialInventory s, ForgeDirection dd) {
		i = s;
		d = dd;

		if ( s instanceof ISidedInventory )
			remover = new AdaptorIInventory( new WrapperMCISidedInventory( (ISidedInventory) s, d ) );
		else
			remover = new AdaptorIInventory( s );
	}

	@Override
	public ItemStack removeSimilarItems(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination)
	{
		return remover.removeSimilarItems( how_many, filter, fuzzyMode, destination );
	}

	@Override
	public ItemStack simulateSimilarRemove(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination)
	{
		return remover.simulateSimilarRemove( how_many, filter, fuzzyMode, destination );
	}

	@Override
	public ItemStack removeItems(int how_many, ItemStack filter, IInventoryDestination destination)
	{
		return remover.removeItems( how_many, filter, destination );
	}

	@Override
	public ItemStack simulateRemove(int how_many, ItemStack filter, IInventoryDestination destination)
	{
		return remover.simulateRemove( how_many, filter, destination );
	}

	@Override
	public ItemStack addItems(ItemStack A)
	{
		if ( A == null )
			return null;
		if ( A.stackSize == 0 )
			return null;

		int used = i.addItem( A, true, d );
		ItemStack out = A.copy();
		out.stackSize -= used;
		if ( out.stackSize > 0 )
			return out;
		return null;
	}

	@Override
	public ItemStack simulateAdd(ItemStack A)
	{
		int used = i.addItem( A, false, d );
		ItemStack out = A.copy();
		out.stackSize -= used;
		if ( out.stackSize > 0 )
			return out;
		return null;
	}

	@Override
	public boolean containsItems()
	{
		if ( i instanceof ISidedInventory )
		{
			ISidedInventory sided = (ISidedInventory) i;
			int slots[] = sided.getAccessibleSlotsFromSide( d.ordinal() );

			if ( slots == null )
				return false;

			for (int slot : slots)
			{
				if ( i.getStackInSlot( slot ) != null )
				{
					return true;
				}
			}

			return false;
		}

		int s = i.getSizeInventory();
		for (int x = 0; x < s; x++)
			if ( i.getStackInSlot( x ) != null )
				return true;
		return false;
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return remover.iterator();
	}

}
