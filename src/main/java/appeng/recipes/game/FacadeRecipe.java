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
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IDefinitions;
import appeng.items.parts.ItemFacade;


public final class FacadeRecipe implements IRecipe
{
	private final IComparableDefinition anchor;
	private final ItemFacade facade;

	public FacadeRecipe( ItemFacade facade )
	{
		this.facade = facade;
		final IDefinitions definitions = AEApi.instance().definitions();

		this.anchor = definitions.parts().cableAnchor();
	}

	@Override
	public boolean matches( final InventoryCrafting inv, final World w )
	{
		return !this.getOutput( inv, false ).isEmpty();
	}

	@Nullable
	private ItemStack getOutput( final IInventory inv, final boolean createFacade )
	{
		if( inv.getStackInSlot( 0 ).isEmpty() && inv.getStackInSlot( 2 ).isEmpty() && inv.getStackInSlot( 6 ).isEmpty() && inv.getStackInSlot( 8 ).isEmpty() )
		{
			if( this.anchor.isSameAs( inv.getStackInSlot( 1 ) ) && this.anchor.isSameAs( inv.getStackInSlot( 3 ) ) && this.anchor.isSameAs( inv.getStackInSlot( 5 ) ) && this.anchor.isSameAs( inv.getStackInSlot( 7 ) ) )
			{
				final ItemStack facades = facade.createFacadeForItem( inv.getStackInSlot( 4 ), !createFacade );
				if( !facades.isEmpty() && createFacade )
				{
					facades.setCount( 4 );
				}
				return facades;
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getCraftingResult( final InventoryCrafting inv )
	{
		return this.getOutput( inv, true );
	}

	@Override
	public boolean canFit(int i, int i1) {
		return false;
	}

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