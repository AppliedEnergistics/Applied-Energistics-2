package appeng.util;

import javax.annotation.Nullable;

import appeng.api.storage.AEKeySpace;
import appeng.api.storage.AEKeySpaces;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;

import appeng.api.storage.data.AEFluidKey;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;

// Consider moving to API?
public interface IVariantConversion<V extends TransferVariant<?>> {
    IVariantConversion<ItemVariant> ITEM = new Item();
    IVariantConversion<FluidVariant> FLUID = new Fluid();

    AEKeySpace getKeySpace();

    V getVariant(@Nullable AEKey key);

    @Nullable
    AEKey getKey(V variant);

    default boolean variantMatches(AEKey key, V variant) {
        return getVariant(key).equals(variant);
    }

    long getBaseSlotSize(V variant);

    class Fluid implements IVariantConversion<FluidVariant> {
        @Override
        public AEKeySpace getKeySpace() {
            return AEKeySpace.fluids();
        }

        @Override
        public FluidVariant getVariant(AEKey key) {
            return key instanceof AEFluidKey fluidKey ? fluidKey.toVariant() : FluidVariant.blank();
        }

        @Override
        public AEKey getKey(FluidVariant variant) {
            return AEFluidKey.of(variant);
        }

        @Override
        public long getBaseSlotSize(FluidVariant variant) {
            return 4 * AEFluidKey.AMOUNT_BUCKET;
        }
    }

    class Item implements IVariantConversion<ItemVariant> {
        @Override
        public AEKeySpace getKeySpace() {
            return AEKeySpace.items();
        }

        @Override
        public ItemVariant getVariant(AEKey key) {
            return key instanceof AEItemKey itemKey ? itemKey.toVariant() : ItemVariant.blank();
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
