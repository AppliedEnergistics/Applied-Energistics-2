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


import appeng.api.AEApi;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IDefinitions;
import appeng.items.parts.ItemFacade;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;


public final class FacadeRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final IComparableDefinition anchor;
    private final ItemFacade facade;

    public FacadeRecipe(ItemFacade facade) {
        this.facade = facade;
        final IDefinitions definitions = AEApi.instance().definitions();

        this.anchor = definitions.parts().cableAnchor();
    }

    @Override
    public boolean matches(final InventoryCrafting inv, final World w) {
        return !this.getOutput(inv, false).isEmpty();
    }

    @Nullable
    private ItemStack getOutput(final IInventory inv, final boolean createFacade) {
        if (inv.getStackInSlot(0).isEmpty() && inv.getStackInSlot(2).isEmpty() && inv.getStackInSlot(6).isEmpty() && inv.getStackInSlot(8).isEmpty()) {
            if (this.anchor.isSameAs(inv.getStackInSlot(1)) && this.anchor.isSameAs(inv.getStackInSlot(3)) && this.anchor
                    .isSameAs(inv.getStackInSlot(5)) && this.anchor.isSameAs(inv.getStackInSlot(7))) {
                final ItemStack facades = this.facade.createFacadeForItem(inv.getStackInSlot(4), !createFacade);
                if (!facades.isEmpty() && createFacade) {
                    facades.setCount(4);
                }
                return facades;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getCraftingResult(final InventoryCrafting inv) {
        return this.getOutput(inv, true);
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
    public NonNullList<ItemStack> getRemainingItems(final InventoryCrafting inv) {
        return ForgeHooks.defaultRecipeGetRemainingItems(inv);
    }
}