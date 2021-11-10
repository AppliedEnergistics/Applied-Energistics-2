package appeng.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;
import appeng.api.storage.FluidStorageChannel;
import appeng.api.storage.GenericStack;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEFluidKey;
import appeng.api.storage.data.AEItemKey;
import appeng.helpers.FluidContainerHelper;
import appeng.helpers.iface.GenericStackInv;

/**
 * Wraps this configuration inventory as an {@link net.minecraft.world.item.ItemStack} based inventory for use in a
 * menu. It will automatically convert appropriately from {@link net.minecraft.world.item.ItemStack}s set by the player
 * to the internal key-based representation with the help of a matching {@link appeng.api.storage.IStorageChannel}.
 */
public class ConfigMenuInventory implements InternalInventory {
    /**
     * Can be null to allow all channels. Used to automatically convert fluid containers to fluid for example.
     */
    @Nullable
    private IStorageChannel<?> channel;

    private final GenericStackInv inv;

    public ConfigMenuInventory(GenericStackInv inv, @Nullable IStorageChannel<?> channel) {
        this.inv = inv;
        this.channel = channel;
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
        return (int) Math.min(Integer.MAX_VALUE, inv.getCapacity());
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

        var unwrapped = GenericStack.unwrapItemStack(stack);
        if (unwrapped != null) {
            if (unwrapped.what() instanceof AEItemKey itemKey) {
                // Let the standard logic handle wrapped items
                stack = itemKey.toStack();
            } else {
                // In all other cases the channel must match
                if (channel == null || unwrapped.what().getChannel() == channel) {
                    return unwrapped;
                } else {
                    return null;
                }
            }
        }

        // Give the storage channel a chance to modify the player's cursor item into a suitable filter/config item
        if (channel instanceof FluidStorageChannel) {
            // Automatic conversion of the player's cursor is only allowed in configuration inventories, because
            // this would otherwise void the container item
            if (inv.getMode() != ConfigInventory.Mode.STORAGE) {
                var containedStack = FluidContainerHelper.getContainedStack(stack);
                if (AEFluidKey.is(containedStack)) {
                    return containedStack;
                }
            }

            return null;
        } else {
            return new GenericStack(AEItemKey.of(stack), stack.getCount());
        }
    }

    @Nullable
    public IStorageChannel<?> getChannel() {
        return channel;
    }

    public void setChannel(@Nullable IStorageChannel<?> channel) {
        this.channel = channel;
    }
}
