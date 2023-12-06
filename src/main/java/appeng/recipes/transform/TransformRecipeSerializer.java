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

package appeng.recipes.transform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipeCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class TransformRecipeSerializer implements RecipeSerializer<TransformRecipe> {

    public static final TransformRecipeSerializer INSTANCE = new TransformRecipeSerializer();

    private static final Codec<TransformRecipe> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
                Ingredient.CODEC_NONEMPTY
                        .listOf()
                        .fieldOf("ingredients")
                        .flatXmap(ingredients -> {
                            return DataResult
                                    .success(NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new)));
                        }, DataResult::success)
                        .forGetter(r -> r.ingredients),
                CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter(r -> r.output),
                ExtraCodecs
                        .strictOptionalField(TransformCircumstance.CODEC, "circumstance",
                                TransformCircumstance.fluid(FluidTags.WATER))
                        .forGetter(t -> t.circumstance))
                .apply(builder, TransformRecipe::new);
    });

    private TransformRecipeSerializer() {
    }

    @Override
    public Codec<TransformRecipe> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public TransformRecipe fromNetwork(FriendlyByteBuf buffer) {
        ItemStack output = buffer.readItem();

        int size = buffer.readByte();
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (int i = 0; i < size; i++) {
            ingredients.add(Ingredient.fromNetwork(buffer));
        }
        TransformCircumstance circumstance = TransformCircumstance.fromNetwork(buffer);

        return new TransformRecipe(ingredients, output, circumstance);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, TransformRecipe recipe) {
        buffer.writeItem(recipe.output);
        buffer.writeByte(recipe.ingredients.size());
        recipe.ingredients.forEach(ingredient -> ingredient.toNetwork(buffer));
        recipe.circumstance.toNetwork(buffer);
    }

}
