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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.items.parts.ItemFacade;

public final class CreativeTabFacade extends CreativeTabs
{

	public static CreativeTabFacade instance = null;

	public CreativeTabFacade() {
		super( "appliedenergistics2.facades" );
	}

	@Override
	public Item getTabIconItem()
	{
		return this.getIconItemStack().getItem();
	}

	@Override
	public ItemStack getIconItemStack()
	{
		return ((ItemFacade) AEApi.instance().definitions().items().facade().get().item()).getCreativeTabIcon();
	}

	public static void init()
	{
		instance = new CreativeTabFacade();
	}

}