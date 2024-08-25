package appeng.blockentity.misc;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.ChargerRecipe;

public class ChargerRecipes {

    public static Iterable<RecipeHolder<ChargerRecipe>> getRecipes(Level level) {
        return level.getRecipeManager().byType(AERecipeTypes.CHARGER);
    }

    @Nullable
    public static ChargerRecipe findRecipe(Level level, ItemStack input) {
        for (var recipe : getRecipes(level)) {
            if (recipe.value().ingredient.test(input)) {
                return recipe.value();
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
