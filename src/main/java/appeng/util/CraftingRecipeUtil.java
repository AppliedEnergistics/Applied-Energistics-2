package appeng.util;

import com.google.common.base.Preconditions;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

public final class CraftingRecipeUtil {
    private CraftingRecipeUtil() {
    }

    /**
     * Expand any recipe to a 3x3 matrix.
     * <p>
     * Will throw an {@link IllegalArgumentException} in case it has more than 9 or a shaped recipe is either wider or
     * higher than 3. ingredients.
     */
    public static NonNullList<Ingredient> ensure3by3CraftingMatrix(Recipe<?> recipe) {
        var ingredients = getIngredients(recipe);
        var expandedIngredients = NonNullList.withSize(9, Ingredient.EMPTY);

        Preconditions.checkArgument(ingredients.size() <= 9);

        // shaped recipes can be smaller than 3x3, expand to 3x3 to match the crafting
        // matrix
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            var width = shapedRecipe.getWidth();
            var height = shapedRecipe.getHeight();
            Preconditions.checkArgument(width <= 3 && height <= 3);

            for (var h = 0; h < height; h++) {
                for (var w = 0; w < width; w++) {
                    var source = w + h * width;
                    var target = w + h * 3;
                    var i = ingredients.get(source);
                    expandedIngredients.set(target, i);
                }
            }
        }
        // Anything else should be a flat list
        else {
            for (var i = 0; i < ingredients.size(); i++) {
                expandedIngredients.set(i, ingredients.get(i));
            }
        }

        return expandedIngredients;
    }

    private static NonNullList<Ingredient> getIngredients(Recipe<?> recipe) {
        // Special handling for upgrade recipes since those do not override getIngredients
        // TODO: is this used anywhere?
        if (recipe instanceof SmithingTrimRecipe trimRecipe) {
            var ingredients = NonNullList.withSize(3, Ingredient.EMPTY);
            ingredients.set(0, trimRecipe.template);
            ingredients.set(1, trimRecipe.base);
            ingredients.set(2, trimRecipe.addition);
            return ingredients;
        }

        if (recipe instanceof SmithingTransformRecipe transformRecipe) {
            var ingredients = NonNullList.withSize(3, Ingredient.EMPTY);
            ingredients.set(0, transformRecipe.template);
            ingredients.set(1, transformRecipe.base);
            ingredients.set(2, transformRecipe.addition);
            return ingredients;
        }

        return recipe.getIngredients();
    }
}
