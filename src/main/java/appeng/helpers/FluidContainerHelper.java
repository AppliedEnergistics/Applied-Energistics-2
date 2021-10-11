package appeng.helpers;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.data.IAEFluidStack;

public final class FluidContainerHelper {
    private FluidContainerHelper() {
    }

    public static boolean isFluidContainer(ItemStack stack) {
        return getReadOnlyStorage(stack) != null;
    }

    @Nullable
    public static IAEFluidStack getContainedFluid(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        var content = StorageUtil.findExtractableContent(
                getReadOnlyStorage(stack), null);
        if (content != null) {
            return IAEFluidStack.of(content);
        } else {
            return null;
        }
    }

    @Nullable
    public static FluidVariant getContainedFluidVariant(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return StorageUtil.findExtractableResource(getReadOnlyStorage(stack), null);
    }

    public static Storage<FluidVariant> getReadOnlyStorage(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return ContainerItemContext.withInitial(stack).find(FluidStorage.ITEM);
    }
}
