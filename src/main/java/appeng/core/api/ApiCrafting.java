/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2020, TeamAppliedEnergistics, All rights reserved.
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

package appeng.core.api;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import appeng.api.crafting.ICraftingHelper;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.api.definitions.ApiItems;
import appeng.core.features.ItemDefinition;
import appeng.helpers.CraftingPatternDetails;
import appeng.items.misc.EncodedPatternItem;

public class ApiCrafting implements ICraftingHelper {

    private final ItemDefinition encodedPattern;

    public ApiCrafting() {
        this.encodedPattern = ApiItems.ENCODED_PATTERN;
    }

    @Override
    public boolean isEncodedPattern(@Nullable IAEItemStack item) {
        return item != null && item.getItem() instanceof EncodedPatternItem;
    }

    @Override
    public boolean isEncodedPattern(ItemStack item) {
        return !item.isEmpty() && item.getItem() instanceof EncodedPatternItem;
    }

    @Override
    public ItemStack encodeCraftingPattern(@Nullable ItemStack stack, ICraftingRecipe recipe, ItemStack[] in,
            ItemStack out, boolean allowSubstitutes) {
        if (stack == null) {
            stack = encodedPattern.stack();
        } else {
            Preconditions.checkArgument(isEncodedPattern(stack));
        }

        EncodedPatternItem.encodeCraftingPattern(stack, in, new ItemStack[] { out }, recipe.getId(), allowSubstitutes);
        return stack;
    }

    @Override
    public ItemStack encodeProcessingPattern(@Nullable ItemStack stack, ItemStack[] in, ItemStack[] out) {
        if (stack == null) {
            stack = encodedPattern.stack();
        } else {
            Preconditions.checkArgument(isEncodedPattern(stack));
        }

        EncodedPatternItem.encodeProcessingPattern(stack, in, out);
        return stack;
    }

    @Override
    public ICraftingPatternDetails decodePattern(final ItemStack is, final World world, boolean autoRecovery) {
        if (is == null || world == null) {
            return null;
        }

        EncodedPatternItem patternItem = getPatternItem(is);
        if (patternItem == null || !patternItem.isEncodedPattern(is)) {
            return null;
        }

        // The recipe ids encoded in a pattern can go stale. This code attempts to find
        // the new id
        // based on the stored inputs/outputs if that happens.
        ResourceLocation recipeId = patternItem.getCraftingRecipeId(is);
        if (recipeId != null) {
            IRecipe<?> recipe = world.getRecipeManager().getRecipes(IRecipeType.CRAFTING).get(recipeId);
            if (!(recipe instanceof ICraftingRecipe) && (!autoRecovery || !attemptRecovery(patternItem, is, world))) {
                return null;
            }
        }

        // We use the shared itemstack for an identity lookup.
        IAEItemStack ais = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(is);

        try {
            return new CraftingPatternDetails(ais, world);
        } catch (IllegalStateException e) {
            AELog.warn("Could not decode an invalid pattern %s: %s", is, e);
            return null;
        }
    }

    private boolean attemptRecovery(EncodedPatternItem patternItem, ItemStack itemStack, World world) {

        RecipeManager recipeManager = world.getRecipeManager();

        List<IAEItemStack> ingredients = patternItem.getIngredients(itemStack);
        List<IAEItemStack> products = patternItem.getProducts(itemStack);
        if (ingredients.size() < 9 || products.size() < 1) {
            return false;
        }

        ResourceLocation currentRecipeId = patternItem.getCraftingRecipeId(itemStack);

        // Fill a crafting inventory with the ingredients to find a suitable recipe
        CraftingInventory testInventory = new CraftingInventory(new ContainerNull(), 3, 3);
        for (int x = 0; x < 9; x++) {
            final IAEItemStack ais = ingredients.get(x);
            final ItemStack gs = ais != null ? ais.createItemStack() : ItemStack.EMPTY;
            testInventory.setInventorySlotContents(x, gs);
        }

        ICraftingRecipe potentialRecipe = recipeManager.getRecipe(IRecipeType.CRAFTING, testInventory, world)
                .orElse(null);

        // Check that it matches the expected output
        if (potentialRecipe != null && products.get(0).isSameType(potentialRecipe.getCraftingResult(testInventory))) {
            // Yay we found a match, reencode the pattern
            AELog.debug("Re-Encoding pattern from %s -> %s", currentRecipeId, potentialRecipe.getId());
            ItemStack[] in = ingredients.stream().map(ais -> ais != null ? ais.createItemStack() : ItemStack.EMPTY)
                    .toArray(ItemStack[]::new);
            ItemStack out = products.get(0).createItemStack();
            encodeCraftingPattern(itemStack, potentialRecipe, in, out, patternItem.allowsSubstitution(itemStack));
        }

        AELog.debug("Failed to recover encoded crafting pattern for recipe %s", currentRecipeId);
        return false;
    }

    private static EncodedPatternItem getPatternItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof EncodedPatternItem) {
            return (EncodedPatternItem) itemStack.getItem();
        }
        return null;
    }

}
