package appeng.integration.modules.jei;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.IIngredientType;

import appeng.api.integrations.jei.IngredientConverter;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

public class FluidIngredientConverter implements IngredientConverter<IJeiFluidIngredient> {
    @Override
    public IIngredientType<IJeiFluidIngredient> getIngredientType() {
        return FabricTypes.FLUID_STACK;
    }

    @Nullable
    @Override
    public IJeiFluidIngredient getIngredientFromStack(GenericStack stack) {
        if (stack.what() instanceof AEFluidKey fluidKey) {
            return new Ingredient(fluidKey, Math.max(1, stack.amount()));
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public GenericStack getStackFromIngredient(IJeiFluidIngredient ingredient) {
        var key = AEFluidKey.of(ingredient.getFluid(), ingredient.getTag().orElse(null));
        return new GenericStack(key, ingredient.getAmount());
    }

    record Ingredient(AEFluidKey key, long amount) implements IJeiFluidIngredient {
        @Override
        public Fluid getFluid() {
            return key.getFluid();
        }

        @Override
        public long getAmount() {
            return amount;
        }

        @Override
        public Optional<CompoundTag> getTag() {
            return Optional.ofNullable(key.getTag());
        }
    }
}
