package appeng.api.implementations.blockentities;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public interface InternalInventory extends Iterable<ItemStack> {

    @Nullable
    static InternalInventory wrapExternal(@Nullable BlockEntity be, @Nonnull Direction side) {
        if (be == null) {
            return null;
        }

        return be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
                .map(PlatformInventoryWrapper::new)
                .orElse(null);
    }

    @Nullable
    static InternalInventory wrapExternal(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Direction side) {
        return wrapExternal(level.getBlockEntity(pos), side);
    }

    static InternalInventory empty() {
        return EmptyInternalInventory.INSTANCE;
    }

    default boolean isEmpty() {
        return !iterator().hasNext();
    }

    default IItemHandler toItemHandler() {
        return new InternalInventoryItemHandler(this);
    }

    default Container toContainer() {
        return new ContainerAdapter(this);
    }

    int size();

    default int getSlotLimit(int slot) {
        return Container.LARGE_MAX_STACK_SIZE;
    }

    ItemStack getStackInSlot(int slotIndex);

    /**
     * Puts the given stack in the given slot and circumvents any potential filters.
     */
    void setItemDirect(int slotIndex, @Nonnull ItemStack stack);

    default boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    default InternalInventory getSubInventory(int fromSlotInclusive, int toSlotExclusive) {
        return new SubInventoryProxy(this, fromSlotInclusive, toSlotExclusive);
    }

    default InternalInventory getSlotInv(int slotIndex) {
        Preconditions.checkArgument(slotIndex >= 0 && slotIndex < size(), "slot out of range");
        return new SubInventoryProxy(this, slotIndex, slotIndex + 1);
    }

    /**
     * @return The redstone signal indicating how full this container is in the [0-15] range.
     */
    default int getRedstoneSignal() {
        var adapter = new ContainerAdapter(this);
        return AbstractContainerMenu.getRedstoneSignalFromContainer(adapter);
    }

    @Nonnull
    @Override
    default Iterator<ItemStack> iterator() {
        return new InternalInventoryIterator(this);
    }

    /**
     * Attempts to insert as much of the given item into this inventory as possible.
     *
     * @param stack The stack to insert. Will not be mutated.
     * @return The overflow, which can be the same object as stack.
     */
    default ItemStack addItems(ItemStack stack) {
        return addItems(stack, false);
    }

    default ItemStack simulateAdd(ItemStack stack) {
        return addItems(stack, true);
    }

    /**
     * Attempts to insert as much of the given item into this inventory as possible.
     *
     * @param stack The stack to insert. Will not be mutated.
     * @return The overflow, which can be the same object as stack.
     */
    @Nonnull
    default ItemStack addItems(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack left = stack.copy();

        for (int slot = 0; slot < size(); slot++) {
            ItemStack is = getStackInSlot(slot);

            if (ItemStack.isSameItemSameTags(is, left)) {
                left = insertItem(slot, left, simulate);
            }
            if (left.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        for (int slot = 0; slot < size(); slot++) {
            left = insertItem(slot, left, simulate);
            if (left.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return left;
    }

    @Nonnull
    default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }

        var inSlot = getStackInSlot(slot);
        if (inSlot.isEmpty()) {
            if (!simulate) {
                setItemDirect(slot, stack);
            }
            return ItemStack.EMPTY;
        }

        if (!ItemStack.isSameItemSameTags(inSlot, stack)) {
            return stack;
        }

        int freeSpace = inSlot.getMaxStackSize() - inSlot.getCount();
        if (freeSpace <= 0) {
            return stack;
        }

        if (!simulate) {
            inSlot.grow(Math.min(stack.getCount(), freeSpace));
        }

        if (freeSpace >= stack.getCount()) {
            return ItemStack.EMPTY;
        } else {
            var r = stack.copy();
            r.shrink(freeSpace);
            return r;
        }
    }

    @Nonnull
    default ItemStack extractItem(int slot, int amount, boolean simulate) {
        var item = getStackInSlot(slot);
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (amount >= item.getCount()) {
            if (!simulate) {
                setItemDirect(slot, ItemStack.EMPTY);
                return item;
            } else {
                return item.copy();
            }
        } else {
            var result = item.copy();
            result.setCount(amount);

            if (!simulate) {
                var reduced = item.copy();
                reduced.shrink(amount);
                setItemDirect(slot, reduced);
            }
            return result;
        }
    }
}
