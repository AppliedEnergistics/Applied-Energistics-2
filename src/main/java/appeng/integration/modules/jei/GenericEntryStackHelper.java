package appeng.integration.modules.jei;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.ingredients.IIngredientType;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

public final class GenericEntryStackHelper {

    public static final List<IngredientType<?>> INGREDIENT_TYPES = ImmutableList.of(
            new IngredientType<>(VanillaTypes.ITEM, i -> i.getAllIngredients().stream()
                    .map(GenericStack::fromItemStack).toList()),
            new IngredientType<>(VanillaTypes.FLUID, i -> i.getAllIngredients().stream()
                    .map(GenericStack::fromFluidStack).toList()));

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
        return INGREDIENT_TYPES.stream()
                .flatMap(type -> type.getConverted(recipeLayout, predicate))
                .toList();
    }

    public record IngredientType<T> (IIngredientType<T> type,
            Function<IGuiIngredient<T>, List<GenericStack>> converter) {
        public Stream<List<GenericStack>> getConverted(IRecipeLayout layout, Predicate<IGuiIngredient<?>> predicate) {
            return layout.getIngredientsGroup(type).getGuiIngredients().entrySet()
                    .stream()
                    .filter(e -> predicate.test(e.getValue()))
                    // We use this to have consistent ordering of entries in the processing recipe
                    // since the hash maps order is undefined
                    .sorted(Comparator.comparingInt(Map.Entry::getKey))
                    .map(e -> converter.apply(e.getValue()));
        }

    }
}
