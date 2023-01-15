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

import com.mojang.serialization.Codec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class InscriberRecipeSerializer implements RecipeSerializer<InscriberRecipe> {

    public static final InscriberRecipeSerializer INSTANCE = new InscriberRecipeSerializer();

    private InscriberRecipeSerializer() {
    }

    @Override
    public Codec<InscriberRecipe> codec() {
        return InscriberRecipe.CODEC;
    }

    @Nullable
    @Override
    public InscriberRecipe fromNetwork(FriendlyByteBuf buffer) {
        Ingredient middle = Ingredient.fromNetwork(buffer);
        ItemStack result = buffer.readItem();
        Ingredient top = Ingredient.fromNetwork(buffer);
        Ingredient bottom = Ingredient.fromNetwork(buffer);
        InscriberProcessType mode = buffer.readEnum(InscriberProcessType.class);

        return new InscriberRecipe(middle, result, top, bottom, mode);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, InscriberRecipe recipe) {
        recipe.getMiddleInput().toNetwork(buffer);
        buffer.writeItem(recipe.getResultItem());
        recipe.getTopOptional().toNetwork(buffer);
        recipe.getBottomOptional().toNetwork(buffer);
        buffer.writeEnum(recipe.getProcessType());
    }

}
