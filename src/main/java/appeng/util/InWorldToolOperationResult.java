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

package appeng.util;


import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class InWorldToolOperationResult
{

	private final ItemStack BlockItem;
	private final List<ItemStack> Drops;

	public InWorldToolOperationResult()
	{
		this.BlockItem = null;
		this.Drops = null;
	}

	public InWorldToolOperationResult( final ItemStack block, final List<ItemStack> drops )
	{
		this.BlockItem = block;
		this.Drops = drops;
	}

	public InWorldToolOperationResult( final ItemStack block )
	{
		this.BlockItem = block;
		this.Drops = null;
	}

	public static InWorldToolOperationResult getBlockOperationResult( final ItemStack[] items )
	{
		final List<ItemStack> temp = new ArrayList<ItemStack>();
		ItemStack b = null;

		for( final ItemStack l : items )
		{
			if( b == null )
			{
				final Block bl = Block.getBlockFromItem( l.getItem() );

				if( bl != null && !( bl instanceof BlockAir ) )
				{
					b = l;
					continue;
				}
			}

			temp.add( l );
		}

		return new InWorldToolOperationResult( b, temp );
	}

	public ItemStack getBlockItem()
	{
		return this.BlockItem;
	}

	public List<ItemStack> getDrops()
	{
		return this.Drops;
	}
}
