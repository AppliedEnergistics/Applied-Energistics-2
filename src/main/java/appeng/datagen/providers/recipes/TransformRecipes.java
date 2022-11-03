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

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeSerializer;

public class TransformRecipes extends AE2RecipeProvider {
    public TransformRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {
        TransformCircumstance water = TransformCircumstance.fluid(FluidTags.WATER);
        // Fluix crystals
        transform(AEItems.FLUIX_CRYSTAL.asItem(), 2, water,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, Items.REDSTONE, Items.QUARTZ)
                        .save(consumer, "fluix_crystals");
        // Recycle dust back into crystals
        transform(AEItems.CERTUS_QUARTZ_CRYSTAL.asItem(), 2, water,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEItems.CERTUS_QUARTZ_DUST)
                        .save(consumer, "certus_quartz_crystals");
        transform(AEItems.FLUIX_CRYSTAL.asItem(), 1, water,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEItems.FLUIX_DUST)
                        .save(consumer, "fluix_crystal");
        // Restore budding quartz
        transform(AEBlocks.DAMAGED_BUDDING_QUARTZ.asItem(), 1, water,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEBlocks.QUARTZ_BLOCK)
                        .save(consumer, "damaged_budding_quartz");
        transform(AEBlocks.CHIPPED_BUDDING_QUARTZ.asItem(), 1, water,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEBlocks.DAMAGED_BUDDING_QUARTZ)
                        .save(consumer, "chipped_budding_quartz");
        transform(AEBlocks.FLAWED_BUDDING_QUARTZ.asItem(), 1, water,
                AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEBlocks.CHIPPED_BUDDING_QUARTZ)
                        .save(consumer, "flawed_budding_quartz");
        // Entangled Singularities
        transform(AEItems.QUANTUM_ENTANGLED_SINGULARITY.asItem(), 2, TransformCircumstance.EXPLOSION,
                Ingredient.of(AEItems.SINGULARITY), Ingredient.of(ConventionTags.ENDER_PEARL_DUST))
                        .save(consumer, "entangled_singularity");
        transform(AEItems.QUANTUM_ENTANGLED_SINGULARITY.asItem(), 2, TransformCircumstance.EXPLOSION,
                Ingredient.of(AEItems.SINGULARITY), Ingredient.of(ConventionTags.ENDER_PEARL))
                        .save(consumer, "entangled_singularity_from_pearl");

    }

    public static TransformRecipeBuilder transform(Item output, int count, TransformCircumstance circumstance,
            ItemLike... inputs) {
        return new TransformRecipeBuilder(Stream.of(inputs).map(Ingredient::of).toList(), output, count, circumstance);
    }

    public static TransformRecipeBuilder transform(Item output, int count, TransformCircumstance circumstance,
            Ingredient... inputs) {
        return new TransformRecipeBuilder(List.of(inputs), output, count, circumstance);
    }

    public record TransformRecipeBuilder(List<Ingredient> ingredients, Item output, int count,
            TransformCircumstance circumstance) {

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
                json.add("circumstance", circumstance.toJson());
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
