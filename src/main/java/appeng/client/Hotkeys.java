package appeng.client;

import java.util.HashMap;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

import appeng.hotkeys.LocatingServicesImpl;

public class Hotkeys {

    private static final HashMap<String, Hotkey> HOTKEYS = new HashMap<>();

    static {
        LocatingServicesImpl.REGISTRY.forEach((id, locatingServices) -> registerHotkey(createHotkey(id)));
    }

    private static Hotkey createHotkey(String id) {
        return new Hotkey(id, new KeyMapping("key.ae2." + id, GLFW.GLFW_KEY_UNKNOWN, "key.ae2.category"));
    }

    private static void registerHotkey(Hotkey hotkey) {
        HOTKEYS.put(hotkey.name(), hotkey);
        KeyBindingHelper.registerKeyBinding(hotkey.mapping());
    }

    public static void checkHotkeys() {
        HOTKEYS.forEach((name, hotkey) -> hotkey.check());
    }
}
