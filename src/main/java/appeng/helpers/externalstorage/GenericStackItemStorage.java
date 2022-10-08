package appeng.helpers.externalstorage;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.Platform;

/**
 * Exposes a {@link GenericInternalInventory} as the platforms external item storage interface.
 */
public class GenericStackItemStorage implements IItemHandler {
    private final GenericInternalInventory inv;

    public GenericStackItemStorage(GenericInternalInventory inv) {
        this.inv = inv;
    }

    @Override
    public int getSlots() {
        return inv.size();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (inv.getKey(slot) instanceof AEItemKey what) {
            var amount = Ints.saturatedCast(inv.getAmount(slot));
            return what.toStack(amount);
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        var what = AEItemKey.of(stack);
        if (what == null) {
            return stack;
        }

        var inserted = (int) inv.insert(slot, what, stack.getCount(), Actionable.ofSimulate(simulate));

        return Platform.copyStackWithSize(stack, stack.getCount() - inserted);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!(inv.getKey(slot) instanceof AEItemKey what)) {
            return ItemStack.EMPTY;
        }

        var extracted = (int) inv.extract(slot, what, amount, Actionable.ofSimulate(simulate));

        return what.toStack(extracted);
    }

    @Override
    public int getSlotLimit(int slot) {
        return Ints.saturatedCast(inv.getCapacity(AEKeyType.items()));
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        var what = AEItemKey.of(stack);
        return what == null || inv.isAllowed(what);
    }
}
