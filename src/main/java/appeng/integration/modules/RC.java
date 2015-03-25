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

package appeng.integration.modules;


import net.minecraft.item.ItemStack;

import mods.railcraft.api.crafting.IRockCrusherRecipe;
import mods.railcraft.api.crafting.RailcraftCraftingManager;

import appeng.integration.BaseModule;
import appeng.integration.abstraction.IRC;


public class RC extends BaseModule implements IRC
{

	public static RC instance;

	public RC()
	{
		this.testClassExistence( RailcraftCraftingManager.class );
		this.testClassExistence( IRockCrusherRecipe.class );
	}

	@Override
	public void rockCrusher( ItemStack input, ItemStack output )
	{
		IRockCrusherRecipe re = RailcraftCraftingManager.rockCrusher.createNewRecipe( input, true, true );
		re.addOutput( output, 1.0f );
	}

	@Override
	public void init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void postInit()
	{

	}
}
