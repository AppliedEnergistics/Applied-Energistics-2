/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.integration.modules.crafttweaker;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IItemStack;

import appeng.integration.abstraction.ICraftTweaker;


public class CTModule implements ICraftTweaker
{
	static final List<IAction> ADDITIONS = new ArrayList<>();
	static final List<IAction> REMOVALS = new ArrayList<>();

	@Override
	public void preInit()
	{
		CraftTweakerAPI.registerClass( GrinderRecipes.class );
		CraftTweakerAPI.registerClass( InscriberRecipes.class );
	}

	@Override
	public void postInit()
	{
		ADDITIONS.forEach( CraftTweakerAPI::apply );
		REMOVALS.forEach( CraftTweakerAPI::apply );
	}

	public static ItemStack toStack( IItemStack iStack )
	{
		if( iStack == null )
		{
			return ItemStack.EMPTY;
		}
		else
		{
			return (ItemStack) iStack.getInternal();
		}
	}
}
