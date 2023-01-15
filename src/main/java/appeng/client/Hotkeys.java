package appeng.client;

import java.util.HashMap;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;

public class Hotkeys {

    private static final HashMap<String, Hotkey> HOTKEYS = new HashMap<>();

    private static boolean finalized;

    private static Hotkey createHotkey(String id) {
        if (finalized) {
            throw new IllegalStateException("Hotkey registration already finalized!");
        }
        return new Hotkey(id, new KeyMapping("key.ae2." + id, GLFW.GLFW_KEY_UNKNOWN, "key.ae2.category"));
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

    public static void registerHotkey(String id) {
        registerHotkey(createHotkey(id));
    }

    public static void checkHotkeys() {
        HOTKEYS.forEach((name, hotkey) -> hotkey.check());
    }
}
