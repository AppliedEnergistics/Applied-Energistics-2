package appeng.crafting;

import java.util.Collection;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.core.AppEng;

/**
 * Utility class for accessing recipes.
 * <p>
 * It abstracts the difference between access to recipes on the client- and server-side.
 */
public final class RecipeAccess {
    private RecipeAccess() {
    }

    /**
     * @see net.minecraft.world.item.crafting.RecipeMap#byKey(ResourceKey)
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Recipe<?>> RecipeHolder<T> byKey(Level level, RecipeType<T> type,
            ResourceKey<Recipe<?>> id) {
        var recipeMap = AppEng.instance().getRecipeMapForType(level, type);

        var holder = recipeMap.byKey(id);
        if (holder != null && holder.value().getType() == type) {
            return (RecipeHolder<T>) holder;
        }
        return null;
    }

    /**
     * @see net.minecraft.world.item.crafting.RecipeMap#byType(RecipeType)
     */
    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(Level level,
            RecipeType<T> type) {
        var recipeMap = AppEng.instance().getRecipeMapForType(level, type);
        return recipeMap.byType(type);
    }

    /**
     * @see net.minecraft.world.item.crafting.RecipeMap#getRecipesFor(RecipeType, RecipeInput, Level)
     */
    public static <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getRecipesFor(Level level,
            RecipeType<T> type, I input) {
        var recipeMap = AppEng.instance().getRecipeMapForType(level, type);
        return recipeMap.getRecipesFor(type, input, level);
    }

    /**
     * @see net.minecraft.world.item.crafting.RecipeManager#getRecipeFor(RecipeType, RecipeInput, Level)
     */
    @Nullable
    public static <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getRecipeFor(Level level,
            RecipeType<T> type, I input) {
        var recipeMap = AppEng.instance().getRecipeMapForType(level, type);
        return recipeMap.getRecipesFor(type, input, level).findFirst().orElse(null);
    }

    /**
     * @see net.minecraft.world.item.crafting.RecipeManager#getRecipeFor(RecipeType, RecipeInput, Level, ResourceKey)
     */
    @Nullable
    public static <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getRecipeFor(Level level,
            RecipeType<T> type, I input, ResourceKey<Recipe<?>> previousRecipeKey) {
        var previousRecipe = byKey(level, type, previousRecipeKey);
        return getRecipeFor(level, type, input, previousRecipe);
    }

    /**
     * @see net.minecraft.world.item.crafting.RecipeManager#getRecipeFor(RecipeType, RecipeInput, Level, RecipeHolder)
     */
    @Nullable
    public static <I extends RecipeInput, T extends Recipe<I>> RecipeHolder<T> getRecipeFor(Level level,
            RecipeType<T> type, I input, @Nullable RecipeHolder<T> previousRecipe) {
        if (previousRecipe != null && previousRecipe.value().matches(input, level)) {
            return previousRecipe;
        }

        return getRecipeFor(level, type, input);
    }
}
