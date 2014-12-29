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
import appeng.api.AEApi;
import appeng.api.util.AEItemDefinition;

public final class CreativeTab extends CreativeTabs
{

	public static CreativeTab instance = null;

	public CreativeTab() {
		super( "appliedenergistics2" );
	}

	@Override
	public Item getTabIconItem()
	{
		return this.getIconItemStack().getItem();
	}

	@Override
	public ItemStack getIconItemStack()
	{
		return this.findFirst( AEApi.instance().blocks().blockController, AEApi.instance().blocks().blockChest, AEApi.instance().blocks().blockCellWorkbench, AEApi
				.instance().blocks().blockFluix, AEApi.instance().items().itemCell1k, AEApi.instance().items().itemNetworkTool,
				AEApi.instance().materials().materialFluixCrystal, AEApi.instance().materials().materialCertusQuartzCrystal );
	}

	private ItemStack findFirst(AEItemDefinition... choices)
	{
		for (AEItemDefinition a : choices)
		{
			ItemStack is = a.stack( 1 );
			if ( is != null )
				return is;
		}

		return new ItemStack( Blocks.chest );
	}

	public static void init()
	{
		instance = new CreativeTab();
	}

}