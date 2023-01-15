package appeng.client;

import java.util.HashMap;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;

import net.minecraft.client.KeyMapping;

public class Hotkeys {

    private static final HashMap<String, Hotkey> HOTKEYS = new HashMap<>();

    private static boolean finalized;

    private static Hotkey createHotkey(String id, int defaultKey) {
        if (finalized) {
            throw new IllegalStateException("Hotkey registration already finalized!");
        }
        return new Hotkey(id, new KeyMapping("key.ae2." + id, defaultKey, "key.ae2.category"));
    }

    private static void registerHotkey(Hotkey hotkey) {
        HOTKEYS.put(hotkey.name(), hotkey);
    }

    public static void finalizeRegistration(Consumer<KeyMapping> register) {
        for (var value : HOTKEYS.values()) {
            register.accept(value.mapping());
        }
        finalized = true;
    }

    public static void registerHotkey(String id, int defaultKey) {
        registerHotkey(createHotkey(id, defaultKey));
    }

    public static Hotkey getHotkey(String id) {
        Preconditions.checkArgument(HOTKEYS.containsKey(id));
        return HOTKEYS.get(id);
    }

    public static void checkHotkeys() {
        HOTKEYS.forEach((name, hotkey) -> hotkey.check());
    }
}
