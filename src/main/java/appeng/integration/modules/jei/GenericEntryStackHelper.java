package appeng.integration.modules.jei;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;

import appeng.api.integrations.rei.IngredientConverter;
import appeng.api.integrations.rei.IngredientConverters;
import appeng.api.stacks.GenericStack;

public final class GenericEntryStackHelper {
    private GenericEntryStackHelper() {
    }

    @Nullable
    public static GenericStack ingredientToStack(EntryStack<?> entryStack) {
        for (var converter : IngredientConverters.getConverters()) {
            var stack = tryConvertToStack(converter, entryStack);
            if (stack != null) {
                return stack;
            }
        }

        return null;
    }

    @Nullable
    private static <T> GenericStack tryConvertToStack(IngredientConverter<T> converter, EntryStack<?> ingredient) {
        if (ingredient.getType() == converter.getIngredientType()) {
            return converter.getStackFromIngredient(ingredient.cast());
        }
        return null;
    }

    public static List<List<GenericStack>> ofInputs(Display display) {
        return display.getInputEntries().stream().map(GenericEntryStackHelper::of).toList();
    }

    public static List<GenericStack> ofOutputs(Display display) {
        return display.getOutputEntries().stream().map(entryIngredient -> entryIngredient.stream()
                .map(GenericEntryStackHelper::ingredientToStack)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private static List<GenericStack> of(EntryIngredient entryIngredient) {
        if (entryIngredient.isEmpty()) {
            return Collections.emptyList();
        }

        return entryIngredient.stream()
                .map(GenericEntryStackHelper::ingredientToStack)
                .filter(Objects::nonNull)
                .toList();
    }
}
