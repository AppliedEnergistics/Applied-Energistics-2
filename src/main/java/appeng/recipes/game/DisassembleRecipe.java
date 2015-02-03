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

package appeng.recipes.game;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class DisassembleRecipe implements IRecipe
{

	private final IMaterials mats = AEApi.instance().definitions().materials();
	private final IItems items = AEApi.instance().definitions().items();
	private final IBlocks blocks = AEApi.instance().definitions().blocks();

	private ItemStack getOutput(InventoryCrafting inv, boolean createFacade)
	{
		ItemStack hasCell = null;

		for (int x = 0; x < inv.getSizeInventory(); x++)
		{
			ItemStack is = inv.getStackInSlot( x );
			if ( is != null )
			{
				if ( hasCell != null )
					return null;

				if ( this.items.cell1k().get().sameAsStack( is ) )
					hasCell = this.mats.cell16kPart().get().stack( 1 );

				if ( this.items.cell4k().get().sameAsStack( is ) )
					hasCell = this.mats.cell4kPart().get().stack( 1 );

				if ( this.items.cell16k().get().sameAsStack( is ) )
					hasCell = this.mats.cell16kPart().get().stack( 1 );

				if ( this.items.cell64k().get().sameAsStack( is ) )
					hasCell = this.mats.cell64kPart().get().stack( 1 );

				// make sure the storage cell is empty...
				if ( hasCell != null )
				{
					IMEInventory<IAEItemStack> cellInv = AEApi.instance().registries().cell().getCellInventory( is, null, StorageChannel.ITEMS );
					if ( cellInv != null )
					{
						IItemList<IAEItemStack> list = cellInv.getAvailableItems( StorageChannel.ITEMS.createList() );
						if ( !list.isEmpty() )
							return null;
					}
				}

				if ( this.items.encodedPattern().get().sameAsStack( is ) )
					hasCell = this.mats.blankPattern().get().stack( 1 );

				if ( this.blocks.craftingStorage1k().get().sameAsStack( is ) )
					hasCell = this.mats.cell1kPart().get().stack( 1 );

				if ( this.blocks.craftingStorage4k().get().sameAsStack( is ) )
					hasCell = this.mats.cell4kPart().get().stack( 1 );

				if ( this.blocks.craftingStorage16k().get().sameAsStack( is ) )
					hasCell = this.mats.cell16kPart().get().stack( 1 );

				if ( this.blocks.craftingStorage64k().get().sameAsStack( is ) )
					hasCell = this.mats.cell64kPart().get().stack( 1 );

				if ( hasCell == null )
					return null;
			}
		}

		return hasCell;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World w)
	{
		return this.getOutput( inv, false ) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		return this.getOutput( inv, true );
	}

	@Override
	public int getRecipeSize()
	{
		return 1;
	}

	@Override
	public ItemStack getRecipeOutput() // no default output..
	{
		return null;
	}

}