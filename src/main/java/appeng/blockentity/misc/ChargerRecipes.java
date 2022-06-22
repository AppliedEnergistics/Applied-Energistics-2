package appeng.blockentity.misc;

import java.util.List;

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
        System.out.println(List.of(getRecipes(level)).size());
        for (ChargerRecipe recipe : getRecipes(level)) {
            if (recipe.ingredient.test(input)) {
                return recipe;
            }
        }

        return null;
    }

    public static boolean allowInsert(Level level, ItemStack input) {
        for (ChargerRecipe recipe : getRecipes(level)) {
            if (recipe.ingredient.test(input)) {
                return true;
            }
        }

        return false;
    }

    public static boolean allowExtract(Level level, ItemStack output) {
        for (ChargerRecipe recipe : getRecipes(level)) {
            if (recipe.ingredient.test(output)) {
                return false;
            }
        }

        return true;
    }

}
