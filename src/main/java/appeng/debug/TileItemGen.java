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

package appeng.debug;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import appeng.tile.AEBaseTile;


public class TileItemGen extends AEBaseTile implements IInventory
{

	private static final Queue<ItemStack> POSSIBLE_ITEMS = new LinkedList<ItemStack>();

	public TileItemGen()
	{
		if( POSSIBLE_ITEMS.isEmpty() )
		{
			for( Object obj : Item.itemRegistry )
			{
				Item mi = (Item) obj;
				if( mi != null )
				{
					if( mi.isDamageable() )
					{
						for( int dmg = 0; dmg < mi.getMaxDamage(); dmg++ )
						{
							POSSIBLE_ITEMS.add( new ItemStack( mi, 1, dmg ) );
						}
					}
					else
					{
						List<ItemStack> list = new ArrayList<ItemStack>();
						mi.getSubItems( mi, mi.getCreativeTab(), list );
						POSSIBLE_ITEMS.addAll( list );
					}
				}
			}
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot( int i )
	{
		return this.getRandomItem();
	}

	private ItemStack getRandomItem()
	{
		return POSSIBLE_ITEMS.peek();
	}

	@Override
	public ItemStack decrStackSize( int i, int j )
	{
		ItemStack a = POSSIBLE_ITEMS.poll();
		ItemStack out = a.copy();
		POSSIBLE_ITEMS.add( a );
		return out;
	}

	@Override
	public ItemStack getStackInSlotOnClosing( int i )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( int i, ItemStack itemstack )
	{
		ItemStack a = POSSIBLE_ITEMS.poll();
		POSSIBLE_ITEMS.add( a );
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isUseableByPlayer( EntityPlayer entityplayer )
	{
		return false;
	}

	@Override
	public void openInventory(
			EntityPlayer player )
	{
		
	}
	
	@Override
	public void closeInventory(
			EntityPlayer player )
	{
		
	}
	
	@Override
	public boolean isItemValidForSlot( int i, ItemStack itemstack )
	{
		return false;
	}

	@Override
	public IChatComponent getDisplayName()
	{
		return null;
	}

	@Override
	public int getField(
			int id )
	{
		return 0;
	}

	@Override
	public void setField(
			int id,
			int value )
	{
		
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		
	}
}
