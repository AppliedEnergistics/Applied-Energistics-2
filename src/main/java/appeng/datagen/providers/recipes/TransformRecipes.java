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

package appeng.datagen.providers.recipes;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.TransformRecipeSerializer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TransformRecipes extends AE2RecipeProvider {
    public TransformRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {

        // Fluix crystals
        transform(Set.of(Items.REDSTONE, Items.QUARTZ), AEItems.FLUIX_CRYSTAL.asItem(), 2).save(consumer, "fluix_crystals");

        // Recycle dust back into crystals
        transform(Set.of(AEItems.CERTUS_QUARTZ_DUST.asItem()), AEItems.CERTUS_QUARTZ_CRYSTAL.asItem(), 2).save(consumer, "certus_quartz_crystals");
        transform(Set.of(AEItems.FLUIX_DUST.asItem()), AEItems.FLUIX_CRYSTAL.asItem(), 1).save(consumer, "fluix_crystal");
        // Restore budding quartz
        transform(Set.of(AEBlocks.QUARTZ_BLOCK.asItem()),
        AEBlocks.DAMAGED_BUDDING_QUARTZ.asItem(), 1).save(consumer, "damaged_budding_quartz");
        transform(Set.of(AEBlocks.DAMAGED_BUDDING_QUARTZ.asItem()),
        AEBlocks.CHIPPED_BUDDING_QUARTZ.asItem(), 1).save(consumer, "chipped_budding_quartz");
        transform(Set.of(AEBlocks.CHIPPED_BUDDING_QUARTZ.asItem()),
        AEBlocks.FLAWED_BUDDING_QUARTZ.asItem(), 1).save(consumer, "flawed_budding_quartz");
    }

    private TransformRecipeBuilder transform(Set<Item> inputs, Item output, int count) {
        List<Ingredient> ingredients = new ArrayList<>(3);

        ingredients.add(Ingredient.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem()));
        inputs.forEach(item -> ingredients.add(Ingredient.of(item)));

        return new TransformRecipeBuilder(ingredients, output, count);
    }

    public record TransformRecipeBuilder(List<Ingredient> ingredients, Item output, int count) {

        public void save(Consumer<FinishedRecipe> consumer, String name) {
            consumer.accept(new Result(name));
        }

        class Result implements FinishedRecipe {
            private final String name;

            public Result(String name) {
                this.name = name;
            }

            @Override
            public void serializeRecipeData(JsonObject json) {
                json.add("result", toJson(new ItemStack(output, count)));

                JsonArray inputs = new JsonArray();
                ingredients.forEach(ingredient -> inputs.add(ingredient.toJson()));
                json.add("ingredients", inputs);
            }

            @Override
            public ResourceLocation getId() {
                return AppEng.makeId("transform/" + name);
            }

            @Override
            public RecipeSerializer<?> getType() {
                return TransformRecipeSerializer.INSTANCE;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        }
    }

}
