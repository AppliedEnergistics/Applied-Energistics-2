package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;

import appeng.api.integrations.jei.IngredientTypeConverter;
import appeng.api.stacks.GenericStack;

public final class GenericEntryStackHelper {

    private GenericEntryStackHelper() {
    }

    @Nullable
    private static List<IngredientTypeConverter<?>> converters;

    @Nullable
    public static GenericStack ingredientToStack(Object ingredient) {
        for (var converter : getConverters()) {
            var stack = tryConvertToStack(converter, ingredient);
            if (stack != null) {
                return stack;
            }
        }

        return null;
    }

    @Nullable
    public static Object stackToIngredient(GenericStack stack) {
        for (var converter : getConverters()) {
            var ingredient = converter.getIngredientFromStack(stack);
            if (ingredient != null) {
                return ingredient;
            }
        }

        return null;
    }

    @Nullable
    private static <T> GenericStack tryConvertToStack(IngredientTypeConverter<T> converter, Object ingredient) {
        var ingredientClass = converter.getIngredientType().getIngredientClass();
        if (ingredientClass.isInstance(ingredient)) {
            return converter.getStackFromIngredient(ingredientClass.cast(ingredient));
        }
        return null;
    }

    public static List<List<GenericStack>> ofInputs(IRecipeLayout recipeLayout) {
        return ofRecipeLayout(recipeLayout, IGuiIngredient::isInput);
    }

    public static List<GenericStack> ofOutputs(IRecipeLayout recipeLayout) {
        return ofRecipeLayout(recipeLayout, ingredient -> !ingredient.isInput())
                .stream()
                .flatMap(e -> e.stream().limit(1))
                .toList();
    }

    private static List<List<GenericStack>> ofRecipeLayout(IRecipeLayout recipeLayout,
            Predicate<IGuiIngredient<?>> predicate) {
        return getConverters().stream()
                .flatMap(converter -> getConverted(converter, recipeLayout, predicate))
                .toList();
    }

    private synchronized static List<IngredientTypeConverter<?>> getConverters() {
        if (converters == null) {
            converters = new ArrayList<>();
            for (IngredientTypeConverter<?> converter : ServiceLoader.load(IngredientTypeConverter.class)) {
                converters.add(converter);
            }
        }
        return converters;
    }

    private static <T> Stream<List<GenericStack>> getConverted(IngredientTypeConverter<T> converter,
            IRecipeLayout layout, Predicate<IGuiIngredient<?>> predicate) {
        return layout.getIngredientsGroup(converter.getIngredientType())
                .getGuiIngredients().entrySet()
                .stream()
                .filter(e -> predicate.test(e.getValue()))
                // We use this to have consistent ordering of entries in the processing recipe
                // since the hash maps order is undefined
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(e -> converter.getStacksFromGuiIngredient(e.getValue()));
    }
}
