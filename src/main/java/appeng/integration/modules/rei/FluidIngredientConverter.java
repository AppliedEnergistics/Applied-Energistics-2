package appeng.integration.modules.rei;

import org.jetbrains.annotations.Nullable;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;

import appeng.api.integrations.rei.IngredientConverter;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

public class FluidIngredientConverter implements IngredientConverter<FluidStack> {
    @Override
    public EntryType<FluidStack> getIngredientType() {
        return VanillaEntryTypes.FLUID;
    }

    @Nullable
    @Override
    public EntryStack<FluidStack> getIngredientFromStack(GenericStack stack) {
        if (stack.what() instanceof AEFluidKey fluidKey) {
            return EntryStack.of(getIngredientType(), FluidStack.create(
                    fluidKey.getFluid(),
                    Math.max(1, stack.amount()),
                    fluidKey.copyTag()));
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public GenericStack getStackFromIngredient(EntryStack<FluidStack> ingredient) {
        if (ingredient.getType() == getIngredientType()) {
            FluidStack fluidStack = ingredient.castValue();
            return new GenericStack(
                    AEFluidKey.of(fluidStack.getFluid(), fluidStack.getTag()),
                    fluidStack.getAmount());
        }
        return null;
    }
}
