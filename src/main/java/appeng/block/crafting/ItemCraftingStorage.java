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

package appeng.block.crafting;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.block.AEBaseItemBlock;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

public class ItemCraftingStorage extends AEBaseItemBlock
{

	public ItemCraftingStorage(Block id) {
		super( id );
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return AEConfig.instance.isFeatureEnabled( AEFeature.enableDisassemblyCrafting );
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack)
	{
		return AEApi.instance().definitions().blocks().craftingUnit().get().stack( 1 );
	}

}
