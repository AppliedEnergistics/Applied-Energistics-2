package appeng.integration.modules.jei;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;

import appeng.api.integrations.jei.IngredientConverter;
import appeng.api.integrations.jei.IngredientConverters;
import appeng.api.stacks.GenericStack;

public final class GenericEntryStackHelper {
    private GenericEntryStackHelper() {
    }

    @Nullable
    public static GenericStack ingredientToStack(Object ingredient) {
        for (var converter : IngredientConverters.getConverters()) {
            var stack = tryConvertToStack(converter, ingredient);
            if (stack != null) {
                return stack;
            }
        }

        return null;
    }

    @Nullable
    public static <T> GenericStack ingredientToStack(IIngredientType<T> type, T ingredient) {
        var converter = IngredientConverters.getConverter(type);
        if (converter != null) {
            return converter.getStackFromIngredient(ingredient);
        }
        return null;
    }

    @Nullable
    public static <T> GenericStack ingredientToStack(ITypedIngredient<T> ingredient) {
        return ingredientToStack(ingredient.getType(), ingredient.getIngredient());
    }

    @Nullable
    public static ITypedIngredient<?> stackToIngredient(IIngredientManager manager, GenericStack stack) {
        for (var converter : IngredientConverters.getConverters()) {
            var ingredient = makeTypedIngredient(manager, converter, stack);
            if (ingredient != null) {
                return ingredient;
            }
        }

        return null;
    }

    @Nullable
    private static <T> ITypedIngredient<T> makeTypedIngredient(IIngredientManager manager,
            IngredientConverter<T> converter, GenericStack stack) {
        var ingredient = converter.getIngredientFromStack(stack);
        if (ingredient != null) {
            return manager.createTypedIngredient(converter.getIngredientType(), ingredient).orElse(null);
        } else {
            return null;
        }
    }

    public static List<List<GenericStack>> ofInputs(IRecipeSlotsView recipeLayout) {
        return recipeLayout.getSlotViews(RecipeIngredientRole.INPUT)
                .stream()
                .map(GenericEntryStackHelper::ofSlot)
                .toList();
    }

    public static List<GenericStack> ofOutputs(IRecipeSlotsView recipeLayout) {
        return recipeLayout.getSlotViews(RecipeIngredientRole.OUTPUT)
                .stream()
                .flatMap(slot -> ofSlot(slot).stream().limit(1))
                .toList();
    }

    private static List<GenericStack> ofSlot(IRecipeSlotView slot) {
        return slot.getAllIngredients()
                .map(GenericEntryStackHelper::ingredientToStack)
                .filter(Objects::nonNull)
                .toList();
    }

    @Nullable
    private static <T> GenericStack tryConvertToStack(IngredientConverter<T> converter, Object ingredient) {
        var ingredientClass = converter.getIngredientType().getIngredientClass();
        if (ingredientClass.isInstance(ingredient)) {
            return converter.getStackFromIngredient(ingredientClass.cast(ingredient));
        }
        return null;
    }

    @Nullable
    private static <T, U> GenericStack tryConvertTypedToStack(IngredientConverter<T> converter,
            IIngredientType<U> ingredientType, U ingredient) {
        if (converter.getIngredientType().equals(ingredientType)) {
            return converter.getIngredientType()
                    .castIngredient(ingredient)
                    .map(converter::getStackFromIngredient)
                    .orElse(null);
        } else {
            return null;
        }
    }
}
