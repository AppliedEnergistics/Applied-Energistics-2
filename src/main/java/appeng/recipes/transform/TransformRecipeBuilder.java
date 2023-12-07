package appeng.recipes.transform;

import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.Collections;

public class TransformRecipeBuilder {

    public static void transform(RecipeOutput consumer, ResourceLocation id, ItemLike output, int count,
                                 TransformCircumstance circumstance, ItemLike... inputs) {
        var ingredients = NonNullList.<Ingredient>createWithCapacity(inputs.length);
        for (var input : inputs) {
            ingredients.add(Ingredient.of(input));
        }
        var recipe = new TransformRecipe(ingredients, toStack(output, count), circumstance);
        consumer.accept(id, recipe, null);
    }

    public static void transform(RecipeOutput consumer, ResourceLocation id, ItemLike output, int count,
                                 TransformCircumstance circumstance, Ingredient... inputs) {
        var ingredients = NonNullList.<Ingredient>createWithCapacity(inputs.length);
        Collections.addAll(ingredients, inputs);
        var recipe = new TransformRecipe(ingredients, toStack(output, count), circumstance);
        consumer.accept(id, recipe, null);
    }

    private static ItemStack toStack(ItemLike item, int count) {
        var stack = item.asItem().getDefaultInstance();
        stack.setCount(count);
        return stack;
    }
}
