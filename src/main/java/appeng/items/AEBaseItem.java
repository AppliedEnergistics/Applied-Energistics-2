/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.items;


import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public abstract class AEBaseItem extends Item
{

	public AEBaseItem()
	{
		this.setNoRepair();
	}

	@Override
	public String toString()
	{
		String regName = getRegistryName() != null ? getRegistryName().getResourcePath() : "unregistered";
		return getClass().getSimpleName() + "[" + regName  + "]";
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public final void addInformation( final ItemStack stack, final EntityPlayer player, final List lines, final boolean displayMoreInfo )
	{
		this.addCheckedInformation( stack, player, lines, displayMoreInfo );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public final void getSubItems( final Item sameItem, final CreativeTabs creativeTab, final List itemStacks )
	{
		this.getCheckedSubItems( sameItem, creativeTab, itemStacks );
	}

	@Override
	public boolean isBookEnchantable( final ItemStack itemstack1, final ItemStack itemstack2 )
	{
		return false;
	}

	protected void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		super.addInformation( stack, player, lines, displayMoreInfo );
	}

	protected void getCheckedSubItems( final Item sameItem, final CreativeTabs creativeTab, final List<ItemStack> itemStacks )
	{
		super.getSubItems( sameItem, creativeTab, itemStacks );
	}

}
