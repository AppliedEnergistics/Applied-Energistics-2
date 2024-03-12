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
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeySlotFilter;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.me.helpers.BaseActionSource;

/**
 * Configuration inventories contain a set of {@link AEKey} references that configure how certain aspects of a machine
 * work.
 * <p/>
 * They can expose an {@link net.minecraft.world.item.ItemStack} based wrapper that can be used as backing for
 * {@link net.minecraft.world.inventory.Slot} in {@link appeng.menu.AEBaseMenu}.
 * <p/>
 * Primarily their role beyond their base class {@link GenericStackInv} is enforcing the configured filter even on
 * returned keys, not just when setting them.
 */
public class ConfigInventory extends GenericStackInv {
    private final boolean allowOverstacking;

    /**
     * An empty config-type inventory.
     */
    public static final ConfigInventory EMPTY_TYPES = ConfigInventory.configTypes(AEKeyTypes.getAll(), 0, null);

    protected ConfigInventory(Set<AEKeyType> supportedKeyTypes, @Nullable AEKeySlotFilter slotFilter,
            Mode mode,
            int size, @Nullable Runnable listener,
            boolean allowOverstacking) {
        super(supportedKeyTypes, listener, mode, size);
        this.allowOverstacking = allowOverstacking;
        setFilter(slotFilter);
    }

    /**
     * When in types mode, the config inventory will ignore all amounts and always return amount 1 for stacks in the
     * inventory.
     */
    public static ConfigInventory configTypes(int size, @Nullable Runnable changeListener) {
        return new ConfigInventory(AEKeyTypes.getAll(), null, Mode.CONFIG_TYPES, size, changeListener, false);
    }

    /**
     * When in types mode, the config inventory will ignore all amounts and always return amount 1 for stacks in the
     * inventory.
     */
    public static ConfigInventory configTypes(Set<AEKeyType> supportedKeyTypes, int size,
            @Nullable Runnable changeListener) {
        return new ConfigInventory(supportedKeyTypes, null, Mode.CONFIG_TYPES, size, changeListener, false);
    }

    /**
     * When in stack mode, the config inventory will respect amounts and drop stacks with amounts of 0 or less.
     */
    public static ConfigInventory configStacks(Set<AEKeyType> supportedKeyTypes, int size,
            @Nullable Runnable changeListener, boolean allowOverstacking) {
        return new ConfigInventory(supportedKeyTypes, null, Mode.CONFIG_STACKS, size, changeListener,
                allowOverstacking);
    }

    /**
     * When in stack mode, the config inventory will respect amounts and drop stacks with amounts of 0 or less.
     */
    public static ConfigInventory storage(int size, @Nullable Runnable changeListener) {
        return new ConfigInventory(AEKeyTypes.getAll(), null, Mode.STORAGE, size, changeListener, false);
    }

    /**
     * When in stack mode, the config inventory will respect amounts and drop stacks with amounts of 0 or less.
     */
    public static ConfigInventory storage(Set<AEKeyType> supportedKeyTypes, int size,
            @Nullable Runnable changeListener) {
        return new ConfigInventory(supportedKeyTypes, null, Mode.STORAGE, size, changeListener, false);
    }

    /**
     * When in stack mode, the config inventory will respect amounts and drop stacks with amounts of 0 or less.
     */
    public static ConfigInventory storage(Set<AEKeyType> supportedKeyTypes, @Nullable AEKeySlotFilter slotFilter,
            int size,
            @Nullable Runnable changeListener) {
        return new ConfigInventory(supportedKeyTypes, slotFilter, Mode.STORAGE, size, changeListener, false);
    }

    @Nullable
    @Override
    public GenericStack getStack(int slot) {
        var stack = super.getStack(slot);
        // Filter and clear stacks not supported by the underlying storage channel
        if (stack != null && !isSupportedType(stack.what())) {
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
        if (!isSupportedType(key)) {
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
        if (stack != null) {
            if (!isSupportedType(stack.what())) {
                return;
            }
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
    public long getMaxAmount(AEKey key) {
        if (allowOverstacking)
            return getCapacity(key.getType());
        return super.getMaxAmount(key);
    }

    @Override
    public ConfigMenuInventory createMenuWrapper() {
        return new ConfigMenuInventory(this);
    }

    public ConfigInventory addFilter(ItemLike item) {
        addFilter(AEItemKey.of(item));
        return this;
    }

    public ConfigInventory addFilter(Fluid fluid) {
        addFilter(AEFluidKey.of(fluid));
        return this;
    }

    public ConfigInventory addFilter(AEKey what) {
        Preconditions.checkState(getMode() == Mode.CONFIG_TYPES);
        insert(what, 1, Actionable.MODULATE, new BaseActionSource());
        return this;
    }
}
