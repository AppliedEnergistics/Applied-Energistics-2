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
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

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
	private static final ItemStack MISMATCHED_STACK = ItemStack.EMPTY;

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
	public boolean matches( final InventoryCrafting inv, final World w )
	{
		return !this.getOutput( inv ).isEmpty();
	}

	@Nullable
	private ItemStack getOutput( final IInventory inventory )
	{
		int itemCount = 0;
		ItemStack output = MISMATCHED_STACK;

		for( int slotIndex = 0; slotIndex < inventory.getSizeInventory(); slotIndex++ )
		{
			final ItemStack stackInSlot = inventory.getStackInSlot( slotIndex );
			if( !stackInSlot.isEmpty() )
			{
				// needs a single input in the recipe
				itemCount++;
				if( itemCount > 1 )
				{
					return MISMATCHED_STACK;
				}

				// handle storage cells
				Optional<ItemStack> maybeCellOutput = this.getCellOutput( stackInSlot );
				if( maybeCellOutput.isPresent() )
				{
					ItemStack storageCellStack = maybeCellOutput.get();
					// make sure the storage cell stackInSlot empty...
					final IMEInventory<IAEItemStack> cellInv = AEApi.instance().registries().cell().getCellInventory( stackInSlot, null, StorageChannel.ITEMS );
					if( cellInv != null )
					{
						final IItemList<IAEItemStack> list = cellInv.getAvailableItems( StorageChannel.ITEMS.createList() );
						if( !list.isEmpty() )
						{
							return ItemStack.EMPTY;
						}
					}

					output = storageCellStack;
				}

				// handle crafting storage blocks
				output = getNonCellOutput( stackInSlot ).orElse( output );
			}
		}

		return output;
	}

	@Nonnull
	private Optional<ItemStack> getCellOutput( final ItemStack compared )
	{
		for( final Map.Entry<IItemDefinition, IItemDefinition> entry : this.cellMappings.entrySet() )
		{
			if( entry.getKey().isSameAs( compared ) )
			{
				return entry.getValue().maybeStack( 1 );
			}
		}

		return Optional.empty();
	}

	@Nonnull
	private Optional<ItemStack> getNonCellOutput( final ItemStack compared )
	{
		for( final Map.Entry<IItemDefinition, IItemDefinition> entry : this.nonCellMappings.entrySet() )
		{
			if( entry.getKey().isSameAs( compared ) )
			{
				return entry.getValue().maybeStack( 1 );
			}
		}

		return Optional.empty();
	}

	@Nullable
	@Override
	public ItemStack getCraftingResult( final InventoryCrafting inv )
	{
		return this.getOutput( inv );
	}

	@Override
	public boolean canFit(int i, int i1) {
		return false;
	}

	@Nullable
	@Override
	public ItemStack getRecipeOutput() // no default output..
	{
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems( final InventoryCrafting inv )
	{
		return ForgeHooks.defaultRecipeGetRemainingItems( inv );
	}

	/**
	 * Sets a unique name for this Item. This should be used for uniquely identify the instance of the Item.
	 * This is the valid replacement for the atrocious 'getUnlocalizedName().substring(6)' stuff that everyone does.
	 * Unlocalized names have NOTHING to do with unique identifiers. As demonstrated by vanilla blocks and items.
	 * <p>
	 * The supplied name will be prefixed with the currently active mod's modId.
	 * If the supplied name already has a prefix that is different, it will be used and a warning will be logged.
	 * <p>
	 * If a name already exists, or this Item is already registered in a registry, then an IllegalStateException is thrown.
	 * <p>
	 * Returns 'this' to allow for chaining.
	 *
	 * @param name Unique registry name
	 * @return This instance
	 */
	@Override
	public IRecipe setRegistryName(ResourceLocation name) {
		return null;
	}

	/**
	 * A unique identifier for this entry, if this entry is registered already it will return it's official registry name.
	 * Otherwise it will return the name set in setRegistryName().
	 * If neither are valid null is returned.
	 *
	 * @return Unique identifier or null.
	 */
	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return null;
	}

	@Override
	public Class<IRecipe> getRegistryType() {
		return null;
	}
}