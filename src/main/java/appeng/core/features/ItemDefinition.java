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

package appeng.core.features;


import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import appeng.api.util.AEItemDefinition;
import appeng.util.Platform;


public class ItemDefinition implements AEItemDefinition
{
	private final Item item;
	private final boolean enabled;

	public ItemDefinition( Item item, boolean enabled )
	{
		this.item = item;
		this.enabled = enabled;
	}

	@Override
	public Block block()
	{
		return null;
	}

	@Override
	public Item item()
	{
		return this.item;
	}

	@Override
	public Class<? extends TileEntity> entity()
	{
		return null;
	}

	@Override
	public ItemStack stack( int stackSize )
	{
		if ( this.enabled )
		{
			return new ItemStack( this.item );
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean sameAsStack( ItemStack comparableItem )
	{
		return this.enabled && Platform.isSameItemType( comparableItem, this.stack( 1 ) );
	}

	@Override
	public boolean sameAsBlock( IBlockAccess world, int x, int y, int z )
	{
		return false;
	}
}
