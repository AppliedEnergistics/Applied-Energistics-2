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
import appeng.api.definitions.Blocks;
import appeng.api.definitions.Items;
import appeng.api.definitions.Materials;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class DisassembleRecipe implements IRecipe
{

	private final Materials mats = AEApi.instance().materials();
	private final Items items = AEApi.instance().items();
	private final Blocks blocks = AEApi.instance().blocks();

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

				if ( this.items.itemCell1k.sameAsStack( is ) )
					hasCell = this.mats.materialCell1kPart.stack( 1 );

				if ( this.items.itemCell4k.sameAsStack( is ) )
					hasCell = this.mats.materialCell4kPart.stack( 1 );

				if ( this.items.itemCell16k.sameAsStack( is ) )
					hasCell = this.mats.materialCell16kPart.stack( 1 );

				if ( this.items.itemCell64k.sameAsStack( is ) )
					hasCell = this.mats.materialCell64kPart.stack( 1 );

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

				if ( this.items.itemEncodedPattern.sameAsStack( is ) )
					hasCell = this.mats.materialBlankPattern.stack( 1 );

				if ( this.blocks.blockCraftingStorage1k.sameAsStack( is ) )
					hasCell = this.mats.materialCell1kPart.stack( 1 );

				if ( this.blocks.blockCraftingStorage4k.sameAsStack( is ) )
					hasCell = this.mats.materialCell4kPart.stack( 1 );

				if ( this.blocks.blockCraftingStorage16k.sameAsStack( is ) )
					hasCell = this.mats.materialCell16kPart.stack( 1 );

				if ( this.blocks.blockCraftingStorage64k.sameAsStack( is ) )
					hasCell = this.mats.materialCell64kPart.stack( 1 );

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