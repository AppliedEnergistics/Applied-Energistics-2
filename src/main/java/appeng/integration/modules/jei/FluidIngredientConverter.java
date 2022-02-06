package appeng.integration.modules.jei;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;

import appeng.api.integrations.jei.IngredientConverter;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

public class FluidIngredientConverter implements IngredientConverter<FluidStack> {
    @Override
    public IIngredientType<FluidStack> getIngredientType() {
        return VanillaTypes.FLUID;
    }

    @Nullable
    @Override
    public FluidStack getIngredientFromStack(GenericStack stack) {
        if (stack.what() instanceof AEFluidKey fluidKey) {
            return fluidKey.toStack(Math.max(1, Ints.saturatedCast(stack.amount())));
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public GenericStack getStackFromIngredient(FluidStack ingredient) {
        return GenericStack.fromFluidStack(ingredient);
    }
}
