package appeng.util;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.item.AEItemStack;

// Consider moving to API?
public interface IVariantConversion<V extends TransferVariant<?>, T extends IAEStack> {
    IVariantConversion<ItemVariant, IAEItemStack> ITEM = new Item();
    IVariantConversion<FluidVariant, IAEFluidStack> FLUID = new Fluid();

    IStorageChannel<T> getChannel();

    V getVariant(T stack);

    default boolean variantMatches(T stack, V variant) {
        return getVariant(stack).equals(variant);
    }

    T createStack(V variant, long amount);

    long getBaseSlotSize(V variant);

    class Fluid implements IVariantConversion<FluidVariant, IAEFluidStack> {
        @Override
        public IStorageChannel<IAEFluidStack> getChannel() {
            return StorageChannels.fluids();
        }

        @Override
        public FluidVariant getVariant(IAEFluidStack stack) {
            return stack.getFluid();
        }

        @Override
        public IAEFluidStack createStack(FluidVariant variant, long amount) {
            return IAEFluidStack.of(variant, amount);
        }

        @Override
        public long getBaseSlotSize(FluidVariant variant) {
            return 4 * FluidConstants.BUCKET;
        }
    }

    class Item implements IVariantConversion<ItemVariant, IAEItemStack> {
        @Override
        public IStorageChannel<IAEItemStack> getChannel() {
            return StorageChannels.items();
        }

        @Override
        public ItemVariant getVariant(IAEItemStack stack) {
            return stack.getVariant();
        }

        @Override
        public IAEItemStack createStack(ItemVariant variant, long amount) {
            return AEItemStack.of(variant, amount);
        }

        @Override
        public long getBaseSlotSize(ItemVariant variant) {
            return Math.min(64, variant.getItem().getMaxStackSize());
        }
    }
}
