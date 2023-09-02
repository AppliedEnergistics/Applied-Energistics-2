package appeng.client;

import java.util.HashMap;

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class Hotkeys {

    private static final HashMap<String, Hotkey> HOTKEYS = new HashMap<>();

    private static Hotkey createHotkey(String id, int defaultKey) {
        return new Hotkey(id, new KeyMapping("key.ae2." + id, defaultKey, "key.ae2.category"));
    }

    private static void registerHotkey(Hotkey hotkey) {
        HOTKEYS.put(hotkey.name(), hotkey);
        KeyBindingHelper.registerKeyBinding(hotkey.mapping());
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
