package appeng.helpers;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.GenericStack;
import appeng.api.stacks.AEFluidKey;

public final class FluidContainerHelper {
    private FluidContainerHelper() {
    }

    @Nullable
    public static GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        var content = StorageUtil.findExtractableContent(
                getReadOnlyStorage(stack), null);
        if (content != null) {
            return new GenericStack(
                    AEFluidKey.of(content.resource()),
                    content.amount());
        } else {
            return null;
        }
    }

    public static Storage<FluidVariant> getReadOnlyStorage(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return ContainerItemContext.withInitial(stack).find(FluidStorage.ITEM);
    }
}
