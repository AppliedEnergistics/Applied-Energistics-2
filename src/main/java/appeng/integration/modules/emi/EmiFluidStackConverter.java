package appeng.integration.modules.emi;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import dev.emi.emi.api.FabricEmiStack;
import dev.emi.emi.api.stack.EmiStack;

import appeng.api.integrations.emi.EmiStackConverter;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

class EmiFluidStackConverter implements EmiStackConverter {
    @Override
    public Class<?> getKeyType() {
        return Fluid.class;
    }

    @Override
    public @Nullable EmiStack toEmiStack(GenericStack stack) {
        if (stack.what() instanceof AEFluidKey fluidKey) {
            return FabricEmiStack.of(fluidKey.toVariant(), stack.amount());
        }
        return null;
    }

    @Override
    public @Nullable GenericStack toGenericStack(EmiStack stack) {
        var fluid = stack.getKeyOfType(Fluid.class);
        if (fluid != null && fluid != Fluids.EMPTY) {
            var fluidStack = FluidVariant.of(fluid.defaultFluidState().getType(), stack.getNbt());
            var fluidKey = AEFluidKey.of(fluidStack);
            return new GenericStack(fluidKey, stack.getAmount());
        }
        return null;
    }
}
