package appeng.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

import appeng.api.stacks.GenericStack;

/**
 * Allows generalized extraction from item-based containers such as buckets or tanks.
 */
public final class GenericContainerHelper {
    private GenericContainerHelper() {
    }

    @Nullable
    public static GenericStack getContainedFluidStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return GenericStack.fromFluidStack(FluidUtil.getFirstStackContained(stack));
    }

}
