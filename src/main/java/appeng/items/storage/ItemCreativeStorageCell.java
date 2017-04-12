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

package appeng.items.storage;


import appeng.api.config.FuzzyMode;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;


public class ItemCreativeStorageCell extends AEBaseItem implements ICellWorkbenchItem
{

	public ItemCreativeStorageCell()
	{
		this.setFeature( EnumSet.of( AEFeature.StorageCells, AEFeature.Creative ) );
		this.setMaxStackSize( 1 );
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( final ItemStack is )
	{
		return null;
	}

	@Override
	public IInventory getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
		return FuzzyMode.IGNORE_ALL;
	}

	@Override
	public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
	{

	}
}
