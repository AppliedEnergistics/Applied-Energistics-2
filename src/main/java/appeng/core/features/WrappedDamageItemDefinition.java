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

public class WrappedDamageItemDefinition implements AEItemDefinition
{

	final AEItemDefinition baseItem;
	final int damage;

	public WrappedDamageItemDefinition(AEItemDefinition def, int dmg) {
		this.baseItem = def;
		this.damage = dmg;
	}

	@Override
	public Block block()
	{
		return this.baseItem.block();
	}

	@Override
	public Item item()
	{
		return this.baseItem.item();
	}

	@Override
	public Class<? extends TileEntity> entity()
	{
		return this.baseItem.entity();
	}

	@Override
	public ItemStack stack(int stackSize)
	{
		if ( this.baseItem == null )
			return null;

		return new ItemStack( this.baseItem.block(), stackSize, this.damage );
	}

	@Override
	public boolean sameAsStack(ItemStack comparableItem)
	{
		if ( comparableItem == null )
			return false;

		return comparableItem.getItem() == this.baseItem.item() && comparableItem.getItemDamage() == this.damage;
	}

	@Override
	public boolean sameAsBlock(IBlockAccess world, int x, int y, int z)
	{
		if ( this.block() != null )
			return world.getBlock( x, y, z ) == this.block() && world.getBlockMetadata( x, y, z ) == this.damage;
		return false;
	}

}
