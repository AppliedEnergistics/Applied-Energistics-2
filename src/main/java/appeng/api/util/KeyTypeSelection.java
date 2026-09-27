package appeng.api.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;

/**
 * Helper class to store the selection of key types.
 */
public class KeyTypeSelection {
    private final Listener listener;
    private final Map<AEKeyType, Boolean> keyTypes = new LinkedHashMap<>();

    public KeyTypeSelection(Runnable listener, Predicate<AEKeyType> allowKeyType) {
        this(selection -> listener.run(), allowKeyType);
    }

    public KeyTypeSelection(Listener listener, Predicate<AEKeyType> allowKeyType) {
        this.listener = listener;
        for (var keyType : AEKeyTypes.getAll()) {
            if (allowKeyType.test(keyType)) {
                keyTypes.put(keyType, true);
            }
        }
    }

    public static KeyTypeSelection forStack(ItemStack stack, Predicate<AEKeyType> allowKeyType) {
        var out = new KeyTypeSelection(selection -> {
            stack.set(AEComponents.ENABLED_KEY_TYPES, selection.enabledSet());
        }, allowKeyType);
        var selected = stack.get(AEComponents.ENABLED_KEY_TYPES);
        if (selected != null) {
            out.setEnabledSet(selected);
        }
        return out;
    }

    public void setEnabled(AEKeyType type, boolean enabled) {
        if (!keyTypes.containsKey(type)) {
            throw new IllegalArgumentException("Key type " + type + " is not allowed.");
        }

        // Disabling all key types is not allowed
        if (!enabled && enabledSet().size() <= 1) {
            return;
        }

        keyTypes.put(type, enabled);
        listener.onKeyTypeSelectionChanged(this);
    }

    public boolean isEnabled(AEKeyType type) {
        if (!keyTypes.containsKey(type)) {
            throw new IllegalArgumentException("Key type " + type + " is not allowed.");
        }

        return keyTypes.get(type);
    }

    public Map<AEKeyType, Boolean> enabled() {
        return new LinkedHashMap<>(keyTypes);
    }

    public List<AEKeyType> enabledSet() {
        return keyTypes.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
    }

    public void setEnabledSet(List<AEKeyType> selected) {
        for (var entry : keyTypes.entrySet()) {
            entry.setValue(selected.contains(entry.getKey()));
        }
    }

    public Predicate<AEKeyType> enabledPredicate() {
        return keyType -> keyTypes.getOrDefault(keyType, Boolean.FALSE);
    }

    public void writeToNBT(ValueOutput output) {
        var enabledKeyTypes = output.list("enabledKeyTypes", Identifier.CODEC);
        for (var entry : keyTypes.entrySet()) {
            if (entry.getValue()) {
                enabledKeyTypes.add(entry.getKey().getId());
            }
        }
    }

    public void readFromNBT(ValueInput input) {
        for (var entry : keyTypes.entrySet()) {
            entry.setValue(false);
        }
        var enabledKeyTypes = input.listOrEmpty("enabledKeyTypes", Identifier.CODEC);
        for (var enabledKeyType : enabledKeyTypes) {
            try {
                var keyType = AEKeyTypes.get(enabledKeyType);
                if (keyTypes.containsKey(keyType)) {
                    keyTypes.put(keyType, true);
                }
            } catch (IllegalArgumentException e) {
                // Ignore invalid key types
            }
        }

        // Make sure that one type is always enabled
        if (enabledSet().isEmpty()) {
            for (var entry : keyTypes.entrySet()) {
                entry.setValue(true);
                break;
            }
        }
    }

    @FunctionalInterface
    public interface Listener {
        void onKeyTypeSelectionChanged(KeyTypeSelection keyTypeSelection);
    }
}
