package appeng.util;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
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
    public static List<Optional<Ingredient>> ensure3by3CraftingMatrix(Recipe<?> recipe) {
        var ingredients = getIngredients(recipe);
        var expandedIngredients = NonNullList.<Optional<Ingredient>>withSize(9, Optional.empty());

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

    public static List<Optional<Ingredient>> getIngredients(Recipe<?> recipe) {
        // Special handling for upgrade recipes since those do not override getIngredients
        if (recipe instanceof SmithingRecipe trimRecipe) {
            return List.of(
                trimRecipe.templateIngredient(),
                Optional.of(trimRecipe.baseIngredient()),
                trimRecipe.additionIngredient()
            );
        }

        var placementInfo = recipe.placementInfo();
        if (!placementInfo.isImpossibleToPlace()) {
            var slotsToIngredient = placementInfo.slotsToIngredientIndex();
            var ingredients = NonNullList.<Optional<Ingredient>>withSize(slotsToIngredient.size(), Optional.empty());
            for (int i = 0; i < slotsToIngredient.size(); i++) {
                ingredients.set(i, Optional.of(placementInfo.ingredients().get(i)));
            }
            return ingredients;
        }

        // TODO 1.21.4 hack around with displays?
        return NonNullList.create();
    }
}
