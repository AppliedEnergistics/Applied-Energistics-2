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

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import appeng.api.features.InscriberProcessType;
import appeng.core.AppEng;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class InscriberRecipe implements Recipe<Container> {

    public static final net.minecraft.resources.ResourceLocation TYPE_ID = AppEng.makeId("inscriber");

    public static final RecipeType<InscriberRecipe> TYPE = RecipeType.register(TYPE_ID.toString());

    private final ResourceLocation id;
    private final String group;

    private final net.minecraft.world.item.crafting.Ingredient middleInput;
    private final net.minecraft.world.item.crafting.Ingredient topOptional;
    private final net.minecraft.world.item.crafting.Ingredient bottomOptional;
    private final ItemStack output;
    private final InscriberProcessType processType;

    public InscriberRecipe(ResourceLocation id, String group, net.minecraft.world.item.crafting.Ingredient middleInput, ItemStack output,
                           Ingredient topOptional, net.minecraft.world.item.crafting.Ingredient bottomOptional, InscriberProcessType processType) {
        this.id = id;
        this.group = group;
        this.middleInput = middleInput;
        this.output = output;
        this.topOptional = topOptional;
        this.bottomOptional = bottomOptional;
        this.processType = processType;
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public net.minecraft.world.item.ItemStack assemble(Container inv) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
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
    public NonNullList<net.minecraft.world.item.crafting.Ingredient> getIngredients() {
        NonNullList<net.minecraft.world.item.crafting.Ingredient> nonnulllist = net.minecraft.core.NonNullList.create();
        nonnulllist.add(this.topOptional);
        nonnulllist.add(this.middleInput);
        nonnulllist.add(this.bottomOptional);
        return nonnulllist;
    }

    public Ingredient getMiddleInput() {
        return middleInput;
    }

    public ItemStack getOutput() {
        return output;
    }

    public net.minecraft.world.item.crafting.Ingredient getTopOptional() {
        return topOptional;
    }

    public net.minecraft.world.item.crafting.Ingredient getBottomOptional() {
        return bottomOptional;
    }

    public InscriberProcessType getProcessType() {
        return processType;
    }

    @Override
    public String getGroup() {
        return group;
    }
}
