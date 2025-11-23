package appeng.recipes.transform;

import java.util.Collections;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class TransformRecipeBuilder {

    public static void transform(RecipeOutput consumer, Identifier id, ItemLike output, int count,
            TransformCircumstance circumstance, ItemLike... inputs) {
        var ingredients = NonNullList.<Ingredient>createWithCapacity(inputs.length);
        for (var input : inputs) {
            ingredients.add(Ingredient.of(input));
        }
        var recipe = new TransformRecipe(ingredients, toStack(output, count), circumstance);
        consumer.accept(ResourceKey.create(Registries.RECIPE, id), recipe, null);
    }

    public static void transform(RecipeOutput consumer, Identifier id, ItemLike output, int count,
            TransformCircumstance circumstance, Ingredient... inputs) {
        var ingredients = NonNullList.<Ingredient>createWithCapacity(inputs.length);
        Collections.addAll(ingredients, inputs);
        var recipe = new TransformRecipe(ingredients, toStack(output, count), circumstance);
        consumer.accept(ResourceKey.create(Registries.RECIPE, id), recipe, null);
    }

    private static ItemStack toStack(ItemLike item, int count) {
        var stack = item.asItem().getDefaultInstance();
        stack.setCount(count);
        return stack;
    }
}
