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

package appeng.core;


import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.api.util.AEItemDefinition;


public final class CreativeTab extends CreativeTabs
{

	public static CreativeTab instance = null;

	public CreativeTab()
	{
		super( "appliedenergistics2" );
	}

	public static void init()
	{
		instance = new CreativeTab();
	}

	@Override
	public Item getTabIconItem()
	{
		return this.getIconItemStack().getItem();
	}

	@Override
	public ItemStack getIconItemStack()
	{
		final IAppEngApi api = AEApi.instance();
		final IBlocks blocks = api.definitions().blocks();
		final IItems items = api.definitions().items();
		final IMaterials materials = api.definitions().materials();

		// ArrayLists do not like generics, so we use a linked list instead.
		final List<Optional<AEItemDefinition>> choices = Lists.newLinkedList();
		choices.add( blocks.controller() );
		choices.add( blocks.chest() );
		choices.add( blocks.cellWorkbench() );
		choices.add( blocks.fluix() );
		choices.add( items.cell1k() );
		choices.add( items.networkTool() );
		choices.add( materials.fluixCrystal() );
		choices.add( materials.certusQuartzCrystal() );

		return this.findFirst( choices );
	}

	private ItemStack findFirst( List<Optional<AEItemDefinition>> choices )
	{
		for ( Optional<AEItemDefinition> choice : choices )
		{
			if ( choice.isPresent() )
			{
				ItemStack is = choice.get().stack( 1 );
				if ( is != null )
				{
					return is;
				}
			}
		}

		return new ItemStack( Blocks.chest );
	}
}