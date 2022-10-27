package appeng.tile.inventory;

import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Spliterator;
import java.util.function.Consumer;

public class AppEngInternalOversizedInventory extends AppEngInternalInventory {
    public AppEngInternalOversizedInventory(IAEAppEngInventory inventory, int size, int maxStack, IAEItemFilter filter) {
        super(inventory, size, maxStack, filter);
    }

    public AppEngInternalOversizedInventory(IAEAppEngInventory inventory, int size, int maxStack) {
        super(inventory, size, maxStack);
    }

    public AppEngInternalOversizedInventory(IAEAppEngInventory inventory, int size) {
        super(inventory, size);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (this.filter != null && !this.filter.allowInsert(this, slot, stack)) {
            return stack;
        }

        if (!simulate) {
            this.previousStack = this.getStackInSlot(slot).copy();
        }

        if (stack.isEmpty())
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        int limit = maxStack[slot];

        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (this.filter != null && !this.filter.allowExtract(this, slot, amount)) {
            return ItemStack.EMPTY;
        }

        if (!simulate) {
            this.previousStack = this.getStackInSlot(slot).copy();
        }

        if (limitExtraction)
            return super.extractItem(slot, amount, simulate);
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        if (existing.getCount() <= amount) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
            return existing;
        } else {
            if (!simulate) {
                this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - amount));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, amount);
        }
    }

    @Override
    public void forEach(Consumer<? super ItemStack> consumer) {
        super.forEach(consumer);
    }

    @Override
    public Spliterator<ItemStack> spliterator() {
        return super.spliterator();
    }
}
