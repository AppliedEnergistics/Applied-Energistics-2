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

import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IDefinitions;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.items.parts.FacadeItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public final class FacadeRecipe extends SpecialCraftingRecipe {
    public static SpecialRecipeSerializer<FacadeRecipe> SERIALIZER = null;

    private final IComparableDefinition anchor;
    private final FacadeItem facade;

    public FacadeRecipe(Identifier id, FacadeItem facade) {
        super(id);
        this.facade = facade;
        final IDefinitions definitions = Api.instance().definitions();

        this.anchor = definitions.parts().cableAnchor();
    }

    @Override
    public boolean matches(@Nonnull final CraftingInventory inv, @Nonnull final World w) {
        return !this.getOutput(inv, false).isEmpty();
    }

    @Nonnull
    private ItemStack getOutput(final Inventory inv, final boolean createFacade) {
        if (inv.getStack(0).isEmpty() && inv.getStack(2).isEmpty() && inv.getStack(6).isEmpty()
                && inv.getStack(8).isEmpty()) {
            if (this.anchor.isSameAs(inv.getStack(1)) && this.anchor.isSameAs(inv.getStack(3))
                    && this.anchor.isSameAs(inv.getStack(5)) && this.anchor.isSameAs(inv.getStack(7))) {
                final ItemStack facades = this.facade.createFacadeForItem(inv.getStack(4), !createFacade);
                if (!facades.isEmpty() && createFacade) {
                    facades.setCount(4);
                }
                return facades;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack craft(@Nonnull final CraftingInventory inv) {
        return this.getOutput(inv, true);
    }

    @Override
    public boolean fits(int i, int i1) {
        return false;
    }

    @Nonnull
    @Override
    public RecipeSerializer<FacadeRecipe> getSerializer() {
        return getSerializer(facade);
    }

    public static RecipeSerializer<FacadeRecipe> getSerializer(FacadeItem facade) {
        if (SERIALIZER == null) {
            SERIALIZER = new SpecialRecipeSerializer<>(id -> new FacadeRecipe(id, facade));
        }
        return SERIALIZER;
    }

}