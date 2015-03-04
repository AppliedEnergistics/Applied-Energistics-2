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


import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.google.common.base.Optional;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.api.definitions.Items;
import appeng.api.definitions.Materials;
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
		final IDefinitions definitions = AEApi.instance().definitions();
		final IBlocks blocks = definitions.blocks();
		final IItems items = definitions.items();
		final IMaterials materials = definitions.materials();

		return this.findFirst( blocks.controller(), blocks.chest(), blocks.cellWorkbench(), blocks.fluix(), items.cell1k(), items.networkTool(), materials.fluixCrystal(), materials.certusQuartzCrystal() );
	}

	private ItemStack findFirst( Optional<AEItemDefinition>... choices )
	{
		for ( Optional<AEItemDefinition> maybe : choices )
		{
			for ( AEItemDefinition definition : maybe.asSet() )
			{
				final ItemStack stack = definition.stack( 1 );
				if ( stack != null )
				{
					return stack;
				}
			}
		}

		return new ItemStack( Blocks.chest );
	}
}