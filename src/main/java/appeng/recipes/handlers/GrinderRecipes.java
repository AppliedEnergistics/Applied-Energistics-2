package appeng.recipes.handlers;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class GrinderRecipes {

    private GrinderRecipes() {
    }

    /**
     * Search all available Grinder recipes for a recipe matching the given input or null;
     */
    @Nullable
    public static GrinderRecipe findForInput(World world, ItemStack input) {
        // FIXME: this is slow, this creates a full copy of the list everytime
        List<GrinderRecipe> grinderRecipes = world.getRecipeManager().getRecipesForType(GrinderRecipe.TYPE);
        for (GrinderRecipe recipe : grinderRecipes) {
            if (recipe.getIngredient().test(input) && input.getCount() >= recipe.getIngredientCount()) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Checks if the given item stack is an ingredient in any grinder recipe, disregarding its current size.
     */
    public static boolean isValidIngredient(World world, ItemStack stack) {
        // FIXME: this is slow, this creates a full copy of the list everytime
        List<GrinderRecipe> grinderRecipes = world.getRecipeManager().getRecipesForType(GrinderRecipe.TYPE);
        for (GrinderRecipe recipe : grinderRecipes) {
            if (recipe.getIngredient().test(stack)) {
                return true;
            }
        }
        return false;
    }
}
