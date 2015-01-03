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


public class DisassembleRecipe implements IRecipe
{

	private final IMaterials mats;
	private final IItems items;
	private final IBlocks blocks;
	private final Map<IItemDefinition, IItemDefinition> cellMappings;
	private final Map<IItemDefinition, IItemDefinition> nonCellMappings;

	public DisassembleRecipe()
	{
		final IDefinitions definitions = AEApi.instance().definitions();

		this.blocks = definitions.blocks();
		this.items = definitions.items();
		this.mats = definitions.materials();
		this.cellMappings = new HashMap<IItemDefinition, IItemDefinition>( 4 );
		this.nonCellMappings = new HashMap<IItemDefinition, IItemDefinition>( 5 );

		this.cellMappings.put( this.items.cell1k(), this.mats.cell1kPart() );
		this.cellMappings.put( this.items.cell4k(), this.mats.cell4kPart() );
		this.cellMappings.put( this.items.cell16k(), this.mats.cell16kPart() );
		this.cellMappings.put( this.items.cell64k(), this.mats.cell64kPart() );

		this.nonCellMappings.put( this.items.encodedPattern(), this.mats.blankPattern() );
		this.nonCellMappings.put( this.blocks.craftingStorage1k(), this.mats.cell1kPart() );
		this.nonCellMappings.put( this.blocks.craftingStorage4k(), this.mats.cell4kPart() );
		this.nonCellMappings.put( this.blocks.craftingStorage16k(), this.mats.cell16kPart() );
		this.nonCellMappings.put( this.blocks.craftingStorage64k(), this.mats.cell64kPart() );
	}

	@Override
	public boolean matches( InventoryCrafting inv, World w )
	{
		return this.getOutput( inv, false ) != null;
	}

	private ItemStack getOutput( InventoryCrafting inv, boolean createFacade )
	{
		ItemStack hasCell = null;

		for ( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			ItemStack is = inv.getStackInSlot( x );
			if ( is != null )
			{
				if ( hasCell != null )
					return null;

				hasCell = this.getCellOutput( is );

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

				hasCell = this.getNonCellOutput( is );

				if ( hasCell == null )
					return null;
			}
		}

		return hasCell;
	}

	private ItemStack getCellOutput( ItemStack compared )
	{
		for ( Map.Entry<IItemDefinition, IItemDefinition> entry : this.cellMappings.entrySet() )
		{
			if ( entry.getKey().isSameAs( compared ) )
			{
				return entry.getValue().maybeStack( 1 ).get();
			}
		}

		return null;
	}

	private ItemStack getNonCellOutput( ItemStack compared )
	{
		for ( Map.Entry<IItemDefinition, IItemDefinition> entry : this.nonCellMappings.entrySet() )
		{
			if ( entry.getKey().isSameAs( compared ) )
			{
				return entry.getValue().maybeStack( 1 ).get();
			}
		}

		return null;
	}

	@Override
	public ItemStack getCraftingResult( InventoryCrafting inv )
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