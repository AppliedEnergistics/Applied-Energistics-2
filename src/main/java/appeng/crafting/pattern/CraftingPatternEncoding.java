/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.crafting.pattern;

import java.util.Arrays;
import java.util.Objects;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.AELog;
import appeng.menu.AutoCraftingMenu;

/**
 * Helper functions to work with patterns, mostly related to (de)serialization.
 */
class CraftingPatternEncoding {
    private static final String NBT_INPUTS = "in";
    private static final String NBT_OUTPUTS = "out";
    private static final String NBT_SUBSITUTE = "substitute";
    private static final String NBT_SUBSITUTE_FLUIDS = "substituteFluids";
    private static final String NBT_RECIPE_ID = "recipe";

    public static GenericStack[] getCraftingInputs(CompoundTag nbt) {
        Objects.requireNonNull(nbt, "Pattern must have a tag.");

        ListTag tag = nbt.getList(NBT_INPUTS, Tag.TAG_COMPOUND);
        Preconditions.checkArgument(tag.size() <= 9, "Cannot use more than 9 ingredients");

        var result = new GenericStack[tag.size()];
        for (int x = 0; x < tag.size(); ++x) {
            var ingredientTag = tag.getCompound(x);
            if (!ingredientTag.isEmpty()) {
                result[x] = GenericStack.fromItemStack(ItemStack.of(ingredientTag));
            }
        }
        return result;

    }

    public static boolean canSubstitute(CompoundTag nbt) {
        Objects.requireNonNull(nbt, "Pattern must have a tag.");

        return nbt.getBoolean(NBT_SUBSITUTE);
    }

    public static boolean canSubstituteFluids(CompoundTag nbt) {
        Objects.requireNonNull(nbt, "Pattern must have a tag.");

        return nbt.getBoolean(NBT_SUBSITUTE_FLUIDS);
    }

    public static ResourceLocation getRecipeId(CompoundTag nbt) {
        Objects.requireNonNull(nbt, "Pattern must have a tag.");

        return new ResourceLocation(nbt.getString(NBT_RECIPE_ID));
    }

    public static ItemStack getCraftingResult(CompoundTag nbt) {
        Objects.requireNonNull(nbt, "Pattern must have a tag.");

        return ItemStack.of(nbt.getCompound(NBT_OUTPUTS));
    }

    public static void encode(CompoundTag tag, RecipeHolder<CraftingRecipe> recipe,
            ItemStack[] sparseInputs,
            ItemStack output, boolean allowSubstitution, boolean allowFluidSubstitution) {
        tag.put(NBT_INPUTS, encodeItemStackList(sparseInputs));
        tag.putBoolean(NBT_SUBSITUTE, allowSubstitution);
        tag.putBoolean(NBT_SUBSITUTE_FLUIDS, allowFluidSubstitution);
        tag.put(NBT_OUTPUTS, output.save(new CompoundTag()));
        tag.putString(NBT_RECIPE_ID, recipe.id().toString());
    }

    private static ListTag encodeItemStackList(ItemStack[] stacks) {
        ListTag tag = new ListTag();
        boolean foundStack = false;
        for (var stack : stacks) {
            if (stack.isEmpty()) {
                tag.add(new CompoundTag());
            } else {
                tag.add(stack.save(new CompoundTag()));
                foundStack = true;
            }
        }
        Preconditions.checkArgument(foundStack, "List passed to pattern must contain at least one stack.");
        return tag;
    }

    public static boolean recover(CompoundTag tag, Level level) {
        RecipeManager recipeManager = level.getRecipeManager();

        var ingredients = getCraftingInputs(tag);
        var product = getCraftingResult(tag);
        if (product.isEmpty()) {
            return false;
        }

        ResourceLocation currentRecipeId = getRecipeId(tag);

        // Fill a crafting inventory with the ingredients to find a suitable recipe
        CraftingContainer testInventory = new TransientCraftingContainer(new AutoCraftingMenu(), 3, 3);
        for (int x = 0; x < 9; x++) {
            var ais = x < ingredients.length ? ingredients[x] : null;
            if (ais != null && ais.what() instanceof AEItemKey itemKey) {
                testInventory.setItem(x, itemKey.toStack());
            }
        }

        var potentialRecipe = recipeManager.getRecipeFor(RecipeType.CRAFTING, testInventory, level)
                .orElse(null);

        // Check that it matches the expected output
        if (potentialRecipe != null && ItemStack.isSameItemSameTags(product,
                potentialRecipe.value().assemble(testInventory, level.registryAccess()))) {
            // Yay we found a match, reencode the pattern
            AELog.debug("Re-Encoding pattern from %s -> %s", currentRecipeId, potentialRecipe.id());
            ItemStack[] in = Arrays.stream(ingredients)
                    .map(stack -> stack.what() instanceof AEItemKey itemKey ? itemKey.toStack() : ItemStack.EMPTY)
                    .toArray(ItemStack[]::new);
            encode(tag, potentialRecipe, in, product,
                    canSubstitute(tag), canSubstituteFluids(tag));
            return true;
        }

        AELog.debug("Failed to recover encoded crafting pattern for recipe %s", currentRecipeId);
        return false;
    }
}
