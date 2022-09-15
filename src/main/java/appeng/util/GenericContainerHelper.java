package appeng.util;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

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

        var content = FluidUtil.getFluidContained(stack).orElse(null);
        if (content != null) {
            return GenericStack.fromFluidStack(content);
        } else {
            return null;
        }
    }

}
