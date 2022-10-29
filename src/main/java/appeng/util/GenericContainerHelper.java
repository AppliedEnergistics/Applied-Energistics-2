package appeng.util;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.GenericStack;

/**
 * Allows generalized extraction from item-based containers such as buckets or tanks.
 */
public final class GenericContainerHelper {
    private GenericContainerHelper() {
    }

    @Nullable
    public static GenericStack getContainedFluidStack(ItemStack stack) {
        return getContainedStack(stack, FluidStorage.ITEM, IVariantConversion.FLUID);
    }

    @Nullable
    public static <V extends TransferVariant<?>> GenericStack getContainedStack(ItemStack stack,
            ItemApiLookup<Storage<V>, ContainerItemContext> apiLookup,
            IVariantConversion<V> conversion) {
        if (stack.isEmpty()) {
            return null;
        }

        var result = ContainerItemContext.withInitial(stack).find(apiLookup);
        var content = StorageUtil.findExtractableContent(result, null);
        if (content != null) {
            return new GenericStack(
                    conversion.getKey(content.resource()),
                    content.amount());
        } else {
            return null;
        }
    }

}
