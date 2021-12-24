package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

public final class GenericEntryStackHelper {

    private GenericEntryStackHelper() {
    }

    @Nullable
    public static GenericStack of(EntryStack<?> entryStack) {

        if (entryStack.getType() == VanillaEntryTypes.ITEM) {
            return GenericStack.fromItemStack(entryStack.castValue());
        } else if (entryStack.getType() == VanillaEntryTypes.FLUID) {
            FluidStack fluidStack = entryStack.castValue();
            return new GenericStack(AEFluidKey.of(fluidStack.getFluid(), fluidStack.getTag()), fluidStack.getAmount());
        } else {
            return null;
        }
    }

    /**
     * Given a list of ingredients, take the first of each that is convertible to a generic stack, and return a list of
     * them.
     */
    public static List<GenericStack> of(List<EntryIngredient> ingredients) {
        var result = new ArrayList<GenericStack>(ingredients.size());
        for (var ingredient : ingredients) {
            for (var entryStack : ingredient) {
                // We use the first convertible stack of each ingredient
                var stack = of(entryStack);
                if (stack != null) {
                    result.add(stack);
                    break;
                }
            }
        }
        return result;
    }

}
