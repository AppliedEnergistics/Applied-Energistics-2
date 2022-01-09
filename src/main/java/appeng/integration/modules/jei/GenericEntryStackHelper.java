package appeng.integration.modules.jei;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

public final class GenericEntryStackHelper {

    public static final List<IngredientType<?>> INGREDIENT_TYPES = ImmutableList.of(
            new IngredientType<>(VanillaEntryTypes.ITEM, e -> GenericStack.fromItemStack(e.castValue())),
            new IngredientType<>(VanillaEntryTypes.FLUID, e -> {
                FluidStack fluidStack = e.castValue();
                var what = AEFluidKey.of(fluidStack.getFluid(), fluidStack.getTag());
                return new GenericStack(what, fluidStack.getAmount());
            }));

    private GenericEntryStackHelper() {
    }

    @Nullable
    public static GenericStack of(EntryStack<?> entryStack) {
        for (var ingredientType : INGREDIENT_TYPES) {
            if (ingredientType.type == entryStack.getType()) {
                return ingredientType.converter.apply(entryStack.cast());
            }
        }
        return null;
    }

    public static List<List<GenericStack>> ofInputs(Display display) {
        return display.getInputEntries().stream().map(GenericEntryStackHelper::of).toList();
    }

    public static List<GenericStack> ofOutputs(Display display) {
        return display.getOutputEntries().stream().map(entryIngredient -> entryIngredient.stream()
                .map(GenericEntryStackHelper::of)
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
                .map(GenericEntryStackHelper::of)
                .filter(Objects::nonNull)
                .toList();
    }

    public record IngredientType<T> (EntryType<T> type,
            Function<EntryStack<T>, GenericStack> converter) {
    }
}
