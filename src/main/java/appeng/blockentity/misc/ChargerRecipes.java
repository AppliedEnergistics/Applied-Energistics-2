package appeng.blockentity.misc;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.ChargerRecipe;

public class ChargerRecipes {

    public static Iterable<RecipeHolder<ChargerRecipe>> getRecipes(ServerLevel level) {
        return level.recipeAccess().recipeMap().byType(AERecipeTypes.CHARGER);
    }

    @Nullable
    public static ChargerRecipe findRecipe(ServerLevel level, ItemStack input) {
        for (var recipe : getRecipes(level)) {
            if (recipe.value().ingredient.test(input)) {
                return recipe.value();
            }
        }

        return null;
    }

    public static boolean allowInsert(ServerLevel level, ItemStack stack) {
        return findRecipe(level, stack) != null;
    }

    public static boolean allowExtract(ServerLevel level, ItemStack stack) {
        return findRecipe(level, stack) == null;
    }

}
