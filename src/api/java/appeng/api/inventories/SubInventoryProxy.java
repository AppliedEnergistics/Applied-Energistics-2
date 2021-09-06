package appeng.api.inventories;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;

/**
 * Exposes a subset of an {@link InternalInventory}.
 */
final class SubInventoryProxy extends BaseInternalInventory {
    private final InternalInventory delegate;
    private final int fromSlot;
    private final int toSlot;

    public SubInventoryProxy(InternalInventory delegate, int fromSlotInclusive, int toSlotExclusive) {
        Preconditions.checkArgument(fromSlotInclusive <= toSlotExclusive, "fromSlotInclusive <= toSlotExclusive");
        Preconditions.checkArgument(fromSlotInclusive >= 0, "fromSlotInclusive >= 0");
        Preconditions.checkArgument(toSlotExclusive <= delegate.size(), "toSlotExclusive <= size()");
        this.delegate = delegate;
        this.fromSlot = fromSlotInclusive;
        this.toSlot = toSlotExclusive;
    }

    @Override
    public int size() {
        return toSlot - fromSlot;
    }

    private int translateSlot(int slotIndex) {
        Preconditions.checkArgument(slotIndex >= 0, "slotIndex >= 0");
        Preconditions.checkArgument(slotIndex < size(), "slotIndex < size()");
        return slotIndex + fromSlot;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return delegate.getStackInSlot(translateSlot(slotIndex));
    }

    @Override
    public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
        delegate.setItemDirect(translateSlot(slotIndex), stack);
    }

    @Override
    public InternalInventory getSubInventory(int fromSlotInclusive, int toSlotExclusive) {
        Preconditions.checkArgument(toSlotExclusive >= 0, "toSlotExclusive >= 0");
        Preconditions.checkArgument(toSlotExclusive <= size(), "toSlotExclusive <= size()");
        return delegate.getSubInventory(translateSlot(fromSlotInclusive), toSlotExclusive + this.fromSlot);
    }

    @Override
    public InternalInventory getSlotInv(int slotIndex) {
        return delegate.getSlotInv(translateSlot(slotIndex));
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return delegate.insertItem(translateSlot(slot), stack, simulate);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return delegate.extractItem(translateSlot(slot), amount, simulate);
    }

}
