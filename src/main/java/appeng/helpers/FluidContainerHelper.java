package appeng.helpers;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.data.IAEFluidStack;
import appeng.util.fluid.AEFluidStack;

public final class FluidContainerHelper {

    private FluidContainerHelper() {
    }

    public static boolean isFluidContainer(ItemStack stack) {
        return getReadOnlyStorage(stack) != null;
    }

    @Nullable
    public static IAEFluidStack getContainedFluid(ItemStack stack) {
        var storage = getReadOnlyStorage(stack);
        if (storage == null) {
            return null;
        }

        // Gather all the extractable fluid of the first fluid variant we can actually extract
        try (var tx = Transaction.openOuter()) {
            FluidVariant resultFluid = null;
            long resultAmount = 0L;
            for (StorageView<FluidVariant> storedView : storage.iterable(tx)) {
                var resource = storedView.getResource();
                if (!resource.isBlank() && (resultFluid == null || resultFluid == resource)) {
                    // Using Integer.MAX_VALUE to guard against overflow here
                    var canExtract = storage.extract(resource, Integer.MAX_VALUE, tx);
                    if (canExtract > 0) {
                        resultFluid = resource;
                        resultAmount += canExtract;
                    }
                }
            }

            if (resultFluid == null || resultAmount == 0) {
                return AEFluidStack.of(resultFluid, resultAmount);
            } else {
                return null;
            }
        }
    }

    @Nullable
    public static FluidVariant getContainedFluidVariant(ItemStack stack) {
        var storage = getReadOnlyStorage(stack);
        if (storage == null) {
            return null;
        }

        // Return the first fluid variant that is extractable
        try (var tx = Transaction.openOuter()) {
            for (StorageView<FluidVariant> storedView : storage.iterable(tx)) {
                var resource = storedView.getResource();
                if (!resource.isBlank()) {
                    // Using Integer.MAX_VALUE to guard against overflow here
                    var canExtract = storage.extract(resource, Integer.MAX_VALUE, tx);
                    if (canExtract > 0) {
                        return resource;
                    }
                }
            }
        }

        return null;
    }

    public static Storage<FluidVariant> getReadOnlyStorage(ItemStack stack) {
        return ContainerItemContext.ofSingleSlot(
                new SingleStackStorage() {
                    private ItemStack buffer = stack;

                    @Override
                    protected ItemStack getStack() {
                        return buffer;
                    }

                    @Override
                    protected void setStack(ItemStack stack) {
                        this.buffer = stack;
                    }
                }).find(FluidStorage.ITEM);
    }
}
