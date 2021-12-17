package appeng.util;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeyFilter;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.me.helpers.BaseActionSource;

/**
 * Configuration inventories contain a set of {@link AEKey} references that configure how certain aspects of a machine
 * work. These inventories are never exposed as platform storage (inaccessible to other machines).
 * <p/>
 * They can expose an {@link net.minecraft.world.item.ItemStack} based wrapper that can be used as backing for
 * {@link net.minecraft.world.inventory.Slot} in {@link appeng.menu.AEBaseMenu}.
 * <p/>
 * Primarily their role beyond their base class {@link GenericStackInv} is enforcing the configured filter even on
 * returned keys, not just when setting them.
 */
public class ConfigInventory extends GenericStackInv {
    protected ConfigInventory(@Nullable AEKeyFilter filter, Mode mode, int size, @Nullable Runnable listener) {
        super(listener, mode, size);
        setFilter(filter);
    }

    /**
     * When in types mode, the config inventory will ignore all amounts and always return amount 1 for stacks in the
     * inventory.
     */
    public static ConfigInventory configTypes(int size, @Nullable Runnable changeListener) {
        return new ConfigInventory(null, Mode.CONFIG_TYPES, size, changeListener);
    }

    /**
     * When in types mode, the config inventory will ignore all amounts and always return amount 1 for stacks in the
     * inventory.
     */
    public static ConfigInventory configTypes(@Nullable AEKeyFilter filter, int size,
            @Nullable Runnable changeListener) {
        return new ConfigInventory(filter, Mode.CONFIG_TYPES, size, changeListener);
    }

    /**
     * When in stack mode, the config inventory will respect amounts and drop stacks with amounts of 0 or less.
     */
    public static ConfigInventory configStacks(@Nullable AEKeyFilter filter, int size,
            @Nullable Runnable changeListener) {
        return new ConfigInventory(filter, Mode.CONFIG_STACKS, size, changeListener);
    }

    /**
     * When in stack mode, the config inventory will respect amounts and drop stacks with amounts of 0 or less.
     */
    public static ConfigInventory storage(int size, @Nullable Runnable changeListener) {
        return new ConfigInventory(null, Mode.STORAGE, size, changeListener);
    }

    /**
     * When in stack mode, the config inventory will respect amounts and drop stacks with amounts of 0 or less.
     */
    public static ConfigInventory storage(@Nullable AEKeyFilter filter, int size,
            @Nullable Runnable changeListener) {
        return new ConfigInventory(filter, Mode.STORAGE, size, changeListener);
    }

    @Nullable
    @Override
    public GenericStack getStack(int slot) {
        var stack = super.getStack(slot);
        // Filter and clear stacks not supported by the underlying storage channel
        if (stack != null && !isAllowed(stack.what())) {
            setStack(slot, null);
            stack = null;
        }
        return stack;
    }

    @Nullable
    @Override
    public AEKey getKey(int slot) {
        var key = super.getKey(slot);
        if (key == null) {
            return null;
        }
        // Filter and clear stacks not supported by the underlying storage channel
        if (!isAllowed(key)) {
            setStack(slot, null);
            key = null;
        }
        return key;
    }

    public Set<AEKey> keySet() {
        var result = new LinkedHashSet<AEKey>();
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
        if (stack != null && !isAllowed(stack.what())) {
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
        return new ConfigMenuInventory(this);
    }

    public void addFilter(ItemLike item) {
        Preconditions.checkState(getMode() == Mode.CONFIG_TYPES);
        insert(AEItemKey.of(item), 1, Actionable.MODULATE, new BaseActionSource());
    }

    public void addFilter(Fluid fluid) {
        Preconditions.checkState(getMode() == Mode.CONFIG_TYPES);
        insert(AEFluidKey.of(fluid), 1, Actionable.MODULATE, new BaseActionSource());
    }
}
