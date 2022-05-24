package appeng.api.hotkeys;

import appeng.hotkeys.HotkeyActionsImpl;

/**
 * registers {@link HotkeyAction}
 */
public class HotkeyActions {

    public static final String WIRELESS_TERMINAL = "wireless_terminal";
    public static final String PORTABLE_ITEM_CELL = "portable_item_cell";
    public static final String PORTABLE_FLUID_CELL = "portable_fluid_cell";

    /**
     * register a new {@link HotkeyAction} under an id
     * <p/>
     * {@link HotkeyAction}s which are added later will be called first
     * <p/>
     * a Keybinding will be created automatically for every id
     */
    public static void register(HotkeyAction hotkeyAction, String id) {
        HotkeyActionsImpl.register(hotkeyAction, id);
    }
}
