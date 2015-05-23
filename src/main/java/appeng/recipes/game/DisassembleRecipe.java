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


import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Optional;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;


public final class DisassembleRecipe implements IRecipe
{
	private static final ItemStack MISMATCHED_STACK = null;

	private final Map<IItemDefinition, IItemDefinition> cellMappings;
	private final Map<IItemDefinition, IItemDefinition> nonCellMappings;

	public DisassembleRecipe()
	{
		final IDefinitions definitions = AEApi.instance().definitions();
		final IBlocks blocks = definitions.blocks();
		final IItems items = definitions.items();
		final IMaterials mats = definitions.materials();

		this.cellMappings = new HashMap<IItemDefinition, IItemDefinition>( 4 );
		this.nonCellMappings = new HashMap<IItemDefinition, IItemDefinition>( 5 );

		this.cellMappings.put( items.cell1k(), mats.cell1kPart() );
		this.cellMappings.put( items.cell4k(), mats.cell4kPart() );
		this.cellMappings.put( items.cell16k(), mats.cell16kPart() );
		this.cellMappings.put( items.cell64k(), mats.cell64kPart() );

		this.nonCellMappings.put( items.encodedPattern(), mats.blankPattern() );
		this.nonCellMappings.put( blocks.craftingStorage1k(), mats.cell1kPart() );
		this.nonCellMappings.put( blocks.craftingStorage4k(), mats.cell4kPart() );
		this.nonCellMappings.put( blocks.craftingStorage16k(), mats.cell16kPart() );
		this.nonCellMappings.put( blocks.craftingStorage64k(), mats.cell64kPart() );
	}

	@Override
	public boolean matches( InventoryCrafting inv, World w )
	{
		return this.getOutput( inv ) != null;
	}

	@Nullable
	private ItemStack getOutput( IInventory inventory )
	{
		int itemCount = 0;
		ItemStack output = MISMATCHED_STACK;

		for( int slotIndex = 0; slotIndex < inventory.getSizeInventory(); slotIndex++ )
		{
			ItemStack stackInSlot = inventory.getStackInSlot( slotIndex );
			if( stackInSlot != null )
			{
				// needs a single input in the recipe
				itemCount++;
				if( itemCount > 1 )
				{
					return MISMATCHED_STACK;
				}

				// handle storage cells
				for( ItemStack storageCellStack : this.getCellOutput( stackInSlot ).asSet() )
				{
					// make sure the storage cell stackInSlot empty...
					IMEInventory<IAEItemStack> cellInv = AEApi.instance().registries().cell().getCellInventory( stackInSlot, null, StorageChannel.ITEMS );
					if( cellInv != null )
					{
						IItemList<IAEItemStack> list = cellInv.getAvailableItems( StorageChannel.ITEMS.createList() );
						if( !list.isEmpty() )
						{
							return null;
						}
					}

					output = storageCellStack;
				}

				// handle crafting storage blocks
				for( ItemStack craftingStorageStack : this.getNonCellOutput( stackInSlot ).asSet() )
				{
					output = craftingStorageStack;
				}
			}
		}

		return output;
	}

	@Nonnull
	private Optional<ItemStack> getCellOutput( ItemStack compared )
	{
		for( Map.Entry<IItemDefinition, IItemDefinition> entry : this.cellMappings.entrySet() )
		{
			if( entry.getKey().isSameAs( compared ) )
			{
				return entry.getValue().maybeStack( 1 );
			}
		}

		return Optional.absent();
	}

	@Nonnull
	private Optional<ItemStack> getNonCellOutput( ItemStack compared )
	{
		for( Map.Entry<IItemDefinition, IItemDefinition> entry : this.nonCellMappings.entrySet() )
		{
			if( entry.getKey().isSameAs( compared ) )
			{
				return entry.getValue().maybeStack( 1 );
			}
		}

		return Optional.absent();
	}

	@Nullable
	@Override
	public ItemStack getCraftingResult( InventoryCrafting inv )
	{
		return this.getOutput( inv );
	}

	@Override
	public int getRecipeSize()
	{
		return 1;
	}

	@Nullable
	@Override
	public ItemStack getRecipeOutput() // no default output..
	{
		return null;
	}
}