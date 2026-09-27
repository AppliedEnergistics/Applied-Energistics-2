package appeng.helpers;

import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

// Consider moving to API?
public interface ResourceConversion<V extends Resource> {
    ResourceConversion<ItemResource> ITEM = new Item();
    ResourceConversion<FluidResource> FLUID = new Fluid();

    AEKeyType getKeyType();

    /**
     * Convert key to variant. If the key is null or of the wrong type, return a blank variant. Keys are not always of
     * this type, make sure to always check.
     */
    V getVariant(@Nullable AEKey key);

    @Nullable
    AEKey getKey(V variant);

    default boolean variantMatches(AEKey key, V variant) {
        return getVariant(key).equals(variant);
    }

    long getBaseSlotSize(V variant);

    class Fluid implements ResourceConversion<FluidResource> {
        @Override
        public AEKeyType getKeyType() {
            return AEKeyType.fluids();
        }

        @Override
        public FluidResource getVariant(AEKey key) {
            return key instanceof AEFluidKey fluidKey ? fluidKey.toResource() : FluidResource.EMPTY;
        }

        @Override
        public AEKey getKey(FluidResource variant) {
            return AEFluidKey.of(variant);
        }

        @Override
        public long getBaseSlotSize(FluidResource variant) {
            return 4 * AEFluidKey.AMOUNT_BUCKET;
        }
    }

    class Item implements ResourceConversion<ItemResource> {
        @Override
        public AEKeyType getKeyType() {
            return AEKeyType.items();
        }

        @Override
        public ItemResource getVariant(AEKey key) {
            return key instanceof AEItemKey itemKey ? itemKey.toResource() : ItemResource.EMPTY;
        }

        @Nullable
        @Override
        public AEItemKey getKey(ItemResource variant) {
            return AEItemKey.of(variant);
        }

        @Override
        public long getBaseSlotSize(ItemResource variant) {
            var stack = variant.toStack();
            return Math.min(64, stack.getMaxStackSize());
        }
    }
}
