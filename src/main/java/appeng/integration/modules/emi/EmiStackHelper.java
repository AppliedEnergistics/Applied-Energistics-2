package appeng.integration.modules.emi;

import appeng.api.integrations.emi.EmiStackConverter;
import appeng.api.integrations.emi.EmiStackConverters;
import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class EmiStackHelper {
    private EmiStackHelper() {
    }

    @Nullable
    public static GenericStack ingredientToStack(EmiStack emiStack) {
        for (var converter : EmiStackConverters.getConverters()) {
            var stack = tryConvertToStack(converter, emiStack);
            if (stack != null) {
                return stack;
            }
        }

        return null;
    }

    @Nullable
    private static GenericStack tryConvertToStack(EmiStackConverter converter, EmiStack emiStack) {
        return converter.getStackFromIngredient(emiStack);
    }

    public static List<List<GenericStack>> ofInputs(EmiRecipe emiRecipe) {
        return emiRecipe.getInputs().stream().map(EmiStackHelper::of).toList();
    }

    public static List<GenericStack> ofOutputs(EmiRecipe emiRecipe) {
        return emiRecipe.getOutputs().stream()
                .map(EmiStackHelper::ingredientToStack)
                .filter(Objects::nonNull)
                .toList();
    }

    private static List<GenericStack> of(EmiIngredient emiIngredient) {
        if (emiIngredient.isEmpty()) {
            return Collections.emptyList();
        }

        return emiIngredient.getEmiStacks()
                .stream()
                .map(EmiStackHelper::ingredientToStack)
                .filter(Objects::nonNull)
                .toList();
    }
}
