package appeng.helpers;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.stacks.GenericStack;

public final class FluidContainerHelper {
    private FluidContainerHelper() {
    }

    @Nullable
    public static GenericStack getContainedStack(ItemStack stack) {
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
