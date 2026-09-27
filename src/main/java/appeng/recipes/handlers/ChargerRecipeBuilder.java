package appeng.recipes.handlers;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ChargerRecipeBuilder {

    public static void charge(RecipeOutput consumer, Identifier id, ItemLike input, ItemLike output) {
        consumer.accept(ResourceKey.create(Registries.RECIPE, id),
                new ChargerRecipe(Ingredient.of(input), new ItemStackTemplate(output.asItem())), null);
    }

    public static void charge(RecipeOutput consumer, Identifier id, HolderSet<Item> input,
            ItemLike output) {
        consumer.accept(ResourceKey.create(Registries.RECIPE, id),
                new ChargerRecipe(Ingredient.of(input), new ItemStackTemplate(output.asItem())), null);
    }

    public static void charge(RecipeOutput consumer, Identifier id, Ingredient input,
            ItemLike output) {
        consumer.accept(ResourceKey.create(Registries.RECIPE, id),
                new ChargerRecipe(input, new ItemStackTemplate(output.asItem())), null);
    }
}
