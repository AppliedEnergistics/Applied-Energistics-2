package appeng.recipes.handlers;

import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public final class GrinderRecipes {

    private GrinderRecipes() {
    }

    /**
     * Search all available Grinder recipes for a recipe matching the given input or null;
     */
    @Nullable
    public static GrinderRecipe findForInput(World world, ItemStack input) {
        for (IRecipe<IInventory> recipe : world.getRecipeManager().byType(GrinderRecipe.TYPE).values()) {
            GrinderRecipe grinderRecipe = (GrinderRecipe) recipe;
            if (grinderRecipe.getIngredient().test(input) && input.getCount() >= grinderRecipe.getIngredientCount()) {
                return grinderRecipe;
            }
        }
        return null;
    }

    /**
     * Checks if the given item stack is an ingredient in any grinder recipe, disregarding its current size.
     */
    public static boolean isValidIngredient(World world, ItemStack stack) {
        for (IRecipe<IInventory> recipe : world.getRecipeManager().byType(GrinderRecipe.TYPE).values()) {
            GrinderRecipe grinderRecipe = (GrinderRecipe) recipe;
            if (grinderRecipe.getIngredient().test(stack)) {
                return true;
            }
        }
        return false;
    }
}
