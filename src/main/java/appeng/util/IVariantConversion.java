package appeng.util;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;

import appeng.api.storage.FluidStorageChannel;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ItemStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.AEFluidKey;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;

// Consider moving to API?
public interface IVariantConversion<V extends TransferVariant<?>, T extends AEKey> {
    IVariantConversion<ItemVariant, AEItemKey> ITEM = new Item();
    IVariantConversion<FluidVariant, AEFluidKey> FLUID = new Fluid();

    IStorageChannel<T> getChannel();

    V getVariant(@Nullable T key);

    @Nullable
    T getKey(V variant);

    default boolean variantMatches(T key, V variant) {
        return getVariant(key).equals(variant);
    }

    long getBaseSlotSize(V variant);

    class Fluid implements IVariantConversion<FluidVariant, AEFluidKey> {
        @Override
        public FluidStorageChannel getChannel() {
            return StorageChannels.fluids();
        }

        @Override
        public FluidVariant getVariant(AEFluidKey key) {
            return key == null ? FluidVariant.blank() : key.toVariant();
        }

        @Override
        public AEFluidKey getKey(FluidVariant variant) {
            return AEFluidKey.of(variant);
        }

        @Override
        public long getBaseSlotSize(FluidVariant variant) {
            return 4 * AEFluidKey.AMOUNT_BUCKET;
        }
    }

    class Item implements IVariantConversion<ItemVariant, AEItemKey> {
        @Override
        public ItemStorageChannel getChannel() {
            return StorageChannels.items();
        }

        @Override
        public ItemVariant getVariant(AEItemKey key) {
            return key == null ? ItemVariant.blank() : key.toVariant();
        }

        @org.jetbrains.annotations.Nullable
        @Override
        public AEItemKey getKey(ItemVariant variant) {
            return AEItemKey.of(variant);
        }

        @Override
        public long getBaseSlotSize(ItemVariant variant) {
            return Math.min(64, variant.getItem().getMaxStackSize());
        }
    }
}
