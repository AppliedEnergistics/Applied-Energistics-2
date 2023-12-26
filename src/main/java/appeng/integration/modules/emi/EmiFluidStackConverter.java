package appeng.integration.modules.emi;

import appeng.api.integrations.emi.EmiStackConverter;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

class EmiFluidStackConverter implements EmiStackConverter {
    @Override
    public Class<?> getKeyType() {
        return Fluid.class;
    }

    @Override
    public @Nullable EmiStack getIngredientFromStack(GenericStack stack) {
        if (stack.what() instanceof AEFluidKey fluidKey) {
            return EmiStack.of(fluidKey.getFluid(), fluidKey.copyTag(), stack.amount());
        }
        return null;
    }

    @Override
    public @Nullable GenericStack getStackFromIngredient(EmiStack stack) {
        var fluid = stack.getKeyOfType(Fluid.class);
        if (fluid != null && fluid != Fluids.EMPTY) {
            var fluidKey = AEFluidKey.of(fluid, stack.getNbt());
            return new GenericStack(fluidKey, stack.getAmount());
        }
        return null;
    }
}
