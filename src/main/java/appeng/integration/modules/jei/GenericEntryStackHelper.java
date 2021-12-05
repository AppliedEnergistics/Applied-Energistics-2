package appeng.integration.modules.jei;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import mezz.jei.api.gui.IRecipeLayout;

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
        // TODO: JEI API
        return Collections.emptyList();
    }

    public static List<GenericStack> ofOutputs(IRecipeLayout recipeLayout) {
        // TODO: JEI API
        return Collections.emptyList();
    }
}
