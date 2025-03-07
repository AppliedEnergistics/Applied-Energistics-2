package appeng.recipes.handlers;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ChargerRecipeBuilder {

    public static void charge(RecipeOutput consumer, ResourceLocation id, ItemLike input, ItemLike output) {
        consumer.accept(ResourceKey.create(Registries.RECIPE, id), new ChargerRecipe(Ingredient.of(input), output.asItem().getDefaultInstance()), null);
    }

    public static void charge(RecipeOutput consumer, ResourceLocation id, HolderSet<Item> input,
            ItemLike output) {
        consumer.accept(ResourceKey.create(Registries.RECIPE, id), new ChargerRecipe(Ingredient.of(input), output.asItem().getDefaultInstance()), null);
    }

    public static void charge(RecipeOutput consumer, ResourceLocation id, Ingredient input,
            ItemLike output) {
        consumer.accept(ResourceKey.create(Registries.RECIPE, id), new ChargerRecipe(input, output.asItem().getDefaultInstance()), null);
    }
}
