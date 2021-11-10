package appeng.util;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import appeng.api.storage.GenericStack;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.helpers.iface.GenericStackInv;

/**
 * Configuration inventories contain a set of {@link appeng.api.storage.data.AEKey} references that configure how
 * certain aspects of a machine work. These inventories are never exposed as platform storage (inaccessible to other
 * machines).
 * <p/>
 * They can expose an {@link net.minecraft.world.item.ItemStack} based wrapper that can be used as backing for
 * {@link net.minecraft.world.inventory.Slot} in {@link appeng.menu.AEBaseMenu}.
 * <p/>
 * AE differentiates between two modes of filter-configuration for machines. Sometimes only the *type* of stack is
 * relevant. In this mode, amounts are ignored. Other times (i.e. interface stocking), the type and amount are relevant.
 */
public class ConfigInventory<T extends AEKey> extends GenericStackInv {
    private final IStorageChannel<T> channel;

    protected ConfigInventory(IStorageChannel<T> channel, Mode mode, int size, @Nullable Runnable listener) {
        super(listener, mode, size);
        this.channel = channel;
    }

    /**
     * When in types mode, the config inventory will ignore all amounts and always return amount 1 for stacks in the
     * inventory.
     */
    public static <T extends AEKey> ConfigInventory<T> configTypes(IStorageChannel<T> channel, int size,
            @Nullable Runnable changeListener) {
        return new ConfigInventory<>(channel, Mode.CONFIG_TYPES, size, changeListener);
    }

    /**
     * When in stack mode, the config inventory will respect amounts and drop stacks with amounts of 0 or less.
     */
    public static <T extends AEKey> ConfigInventory<T> configStacks(IStorageChannel<T> channel, int size,
            @Nullable Runnable changeListener) {
        return new ConfigInventory<>(channel, Mode.CONFIG_STACKS, size, changeListener);
    }

    /**
     * When in stack mode, the config inventory will respect amounts and drop stacks with amounts of 0 or less.
     */
    public static <T extends AEKey> ConfigInventory<T> storage(IStorageChannel<T> channel, int size,
            @Nullable Runnable changeListener) {
        return new ConfigInventory<>(channel, Mode.STORAGE, size, changeListener);
    }

    @Nullable
    @Override
    public GenericStack getStack(int slot) {
        var stack = super.getStack(slot);
        // Filter and clear stacks not supported by the underlying storage channel
        if (stack != null && channel.tryCast(stack.what()) == null) {
            setStack(slot, null);
            stack = null;
        }
        return stack;
    }

    @Nullable
    @Override
    public T getKey(int slot) {
        var key = super.getKey(slot);
        if (key == null) {
            return null;
        }
        // Filter and clear stacks not supported by the underlying storage channel
        var channelKey = channel.tryCast(key);
        if (channelKey == null) {
            setStack(slot, null);
        }
        return channelKey;
    }

    public Set<T> keySet() {
        var result = new LinkedHashSet<T>();
        for (int i = 0; i < stacks.length; i++) {
            var what = getKey(i);
            if (what != null) {
                result.add(what);
            }
        }
        return result;
    }

    @Override
    public void setStack(int slot, @Nullable GenericStack stack) {
        if (stack != null && channel.tryCast(stack.what()) == null) {
            return;
        }
        if (stack != null) {
            boolean typesOnly = mode == Mode.CONFIG_TYPES;
            if (typesOnly && stack.amount() != 0) {
                // force amount to 0 in types-only mode
                stack = new GenericStack(stack.what(), 0);
            } else if (!typesOnly && stack.amount() <= 0) {
                // in stack mode, amounts of 0 or less clear the slot
                stack = null;
            }
        }
        super.setStack(slot, stack);
    }

    @Override
    public ConfigMenuInventory createMenuWrapper() {
        return new ConfigMenuInventory(this, channel);
    }

}
