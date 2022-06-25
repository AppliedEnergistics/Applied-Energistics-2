package appeng.blockentity.misc;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.recipes.handlers.ChargerRecipe;

public class ChargerRecipes {

    public static Iterable<ChargerRecipe> getRecipes(Level level) {
        return level.getRecipeManager().byType(ChargerRecipe.TYPE).values();
    }

    @Nullable
    public static ChargerRecipe findRecipe(Level level, ItemStack input) {
        for (ChargerRecipe recipe : getRecipes(level)) {
            if (recipe.ingredient.test(input)) {
                return recipe;
            }
        }

        return null;
    }

    public static boolean allowInsert(Level level, ItemStack stack) {
        return findRecipe(level, stack) != null;
    }

    public static boolean allowExtract(Level level, ItemStack stack) {
        return findRecipe(level, stack) == null;
    }

}
