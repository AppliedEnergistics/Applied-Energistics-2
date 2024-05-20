package appeng.integration.modules.emi;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import appeng.api.integrations.emi.EmiStackConverters;
import appeng.api.stacks.GenericStack;

public final class EmiStackHelper {
    private EmiStackHelper() {
    }

    @Nullable
    public static GenericStack toGenericStack(EmiStack emiStack) {
        for (var converter : EmiStackConverters.getConverters()) {
            var stack = converter.toGenericStack(emiStack);
            if (stack != null) {
                return stack;
            }
        }

        return null;
    }

    @Nullable
    public static EmiStack toEmiStack(GenericStack stack) {
        for (var converter : EmiStackConverters.getConverters()) {
            var emiStack = converter.toEmiStack(stack);
            if (emiStack != null) {
                return emiStack;
            }
        }

        return null;
    }

    public static List<List<GenericStack>> ofInputs(EmiRecipe emiRecipe) {
        return emiRecipe.getInputs().stream().map(EmiStackHelper::of).toList();
    }

    public static List<GenericStack> ofOutputs(EmiRecipe emiRecipe) {
        return emiRecipe.getOutputs().stream()
                .map(EmiStackHelper::toGenericStack)
                .filter(Objects::nonNull)
                .toList();
    }

    private static List<GenericStack> of(EmiIngredient emiIngredient) {
        if (emiIngredient.isEmpty()) {
            return Collections.emptyList();
        }

        return emiIngredient.getEmiStacks()
                .stream()
                .map(EmiStackHelper::toGenericStack)
                .filter(Objects::nonNull)
                .map(x -> new GenericStack(x.what(), emiIngredient.getAmount()))
                .toList();
    }
}
