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

package appeng.recipes.handlers;

import java.util.Map;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import appeng.core.AppEng;

public class InscriberRecipe implements Recipe<Container> {
    public static final ResourceLocation TYPE_ID = AppEng.makeId("inscriber");

    public static final RecipeType<InscriberRecipe> TYPE = RecipeType.register(TYPE_ID.toString());

    private final ResourceLocation id;

    private final Ingredient middleInput;
    private final Ingredient topOptional;
    private final Ingredient bottomOptional;
    private final ItemStack output;
    private final InscriberProcessType processType;

    public InscriberRecipe(ResourceLocation id, Ingredient middleInput, ItemStack output,
            Ingredient topOptional, Ingredient bottomOptional, InscriberProcessType processType) {
        this.id = id;
        this.middleInput = middleInput;
        this.output = output;
        this.topOptional = topOptional;
        this.bottomOptional = bottomOptional;
        this.processType = processType;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv) {
        ItemStack result = this.output.copy();
        ItemStack input = inv.getItem(2);

        CompoundTag inputTag = input.getTag();
        CompoundTag resultTag = result.getTag();

        var resultEnchants = EnchantmentHelper.deserializeEnchantments(result.getEnchantmentTags());
        resultTag.remove("Enchantments");

        if (resultTag != null) {
            result.setTag(inputTag);
            for (String key : resultTag.getAllKeys()) {
                if (!result.getTag().contains(key)) {
                    result.addTagElement(key, resultTag.get(key));
                }
            }
        }
        if (resultEnchants != null) {
            for (Map.Entry<Enchantment, Integer> enchantment : resultEnchants.entrySet()) {
                if (EnchantmentHelper.getEnchantments(result).get(enchantment.getKey()) == null) {
                    result.enchant(enchantment.getKey(), enchantment.getValue());
                }
            }
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return this.output;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return InscriberRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> nonnulllist = NonNullList.create();
        nonnulllist.add(this.topOptional);
        nonnulllist.add(this.middleInput);
        nonnulllist.add(this.bottomOptional);
        return nonnulllist;
    }

    public Ingredient getMiddleInput() {
        return this.middleInput;
    }

    public Ingredient getTopOptional() {
        return this.topOptional;
    }

    public Ingredient getBottomOptional() {
        return this.bottomOptional;
    }

    public InscriberProcessType getProcessType() {
        return this.processType;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
