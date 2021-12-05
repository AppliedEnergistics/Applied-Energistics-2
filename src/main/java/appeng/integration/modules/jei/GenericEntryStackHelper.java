package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

public final class GenericEntryStackHelper {

    private GenericEntryStackHelper() {
    }

    @Nullable
    public static GenericStack of(Object ingredient) {

        if (ingredient instanceof ItemStack itemStack) {
            return GenericStack.fromItemStack(itemStack);
        } else if (ingredient instanceof FluidStack fluidStack) {
            return new GenericStack(AEFluidKey.of(fluidStack.getFluid(), fluidStack.getTag()), fluidStack.getAmount());
        } else {
            return null;
        }
    }

    public static List<GenericStack> ofInputs(IRecipeLayout recipeLayout) {
        return ofRecipeLayout(recipeLayout, true);
    }

    public static List<GenericStack> ofOutputs(IRecipeLayout recipeLayout) {
        return ofRecipeLayout(recipeLayout, false);

    }

    private static List<GenericStack> ofRecipeLayout(IRecipeLayout recipeLayout, boolean input) {
        var result = Streams.concat(
                convertGuiIngredients(recipeLayout.getItemStacks(), GenericStack::fromItemStack, input),
                convertGuiIngredients(recipeLayout.getFluidStacks(), GenericStack::fromFluidStack, input))
                // We're doing this assuming that lower number input and output slots should correlate with
                // the encoded pattern slots. Especially output slot 0 == primary output.
                .sorted(Comparator.comparingInt(Pair::getKey))
                .map(Pair::getValue)
                .toList();
        return new ArrayList<>(result);
    }

    private static <T> Stream<Pair<Integer, GenericStack>> convertGuiIngredients(IGuiIngredientGroup<T> group,
            Function<T, GenericStack> converter,
            boolean input) {
        return group.getGuiIngredients().entrySet().stream()
                .filter(e -> e.getValue().isInput() == input)
                .map(e -> {
                    var displayed = e.getValue().getDisplayedIngredient();
                    if (displayed != null) {
                        return Pair.of(e.getKey(), converter.apply(displayed));
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }
}
