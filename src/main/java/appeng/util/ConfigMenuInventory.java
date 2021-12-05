package appeng.util;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;

/**
 * Wraps this configuration inventory as an {@link net.minecraft.world.item.ItemStack} based inventory for use in a
 * menu. It will automatically convert appropriately from {@link net.minecraft.world.item.ItemStack}s set by the player
 * to the internal key-based representation with the help of a matching {@link AEKeyType}.
 */
public class ConfigMenuInventory implements InternalInventory {
    private final GenericStackInv inv;

    public ConfigMenuInventory(GenericStackInv inv) {
        this.inv = Objects.requireNonNull(inv);
    }

    public GenericStackInv getDelegate() {
        return inv;
    }

    @Override
    public int size() {
        return inv.size();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true; // Clearing filters is always allowed
        }

        return convertToSuitableStack(stack) != null;
    }

    @Override
    public int getSlotLimit(int slot) {
        return (int) Math.min(Integer.MAX_VALUE, inv.getCapacity(AEKeyType.items()));
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        GenericStack stack = inv.getStack(slotIndex);

        // Special case for item channel to maximize the support with other mods and allowing pick up if the
        // underlying inventory / slot does.
        if (stack != null && stack.what() instanceof AEItemKey itemKey) {
            // For type only inventories, force amount = 1 since this will hide amount rendering
            // Otherwise, only convert to the real stack if it fits in the max stack size
            if (inv.getMode() == ConfigInventory.Mode.CONFIG_TYPES) {
                return itemKey.toStack();
            } else if (stack.amount() > 0 && stack.amount() <= itemKey.getItem().getMaxStackSize()) {
                return itemKey.toStack((int) stack.amount());
            }
        }

        return GenericStack.wrapInItemStack(stack);
    }

    @Override
    public void setItemDirect(int slotIndex, @NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            inv.setStack(slotIndex, null);
        } else {
            var converted = convertToSuitableStack(stack);
            if (converted != null) {
                inv.setStack(slotIndex, converted);
            }
        }
    }

    @Nullable
    public GenericStack convertToSuitableStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        // Item Stacks that contain a wrapped GenericStack will automatically be unwrapped
        var unwrapped = GenericStack.unwrapItemStack(stack);
        if (unwrapped != null) {
            if (unwrapped.what() instanceof AEItemKey itemKey) {
                // Let the standard logic handle wrapped items
                stack = itemKey.toStack();
            } else {
                // In all other cases the channel must match
                if (inv.isAllowed(unwrapped.what())) {
                    return unwrapped;
                } else {
                    return null;
                }
            }
        }

        // Try items last
        var what = AEItemKey.of(stack);
        if (inv.isAllowed(what)) {
            return new GenericStack(what, stack.getCount());
        }

        return null;
    }
}
