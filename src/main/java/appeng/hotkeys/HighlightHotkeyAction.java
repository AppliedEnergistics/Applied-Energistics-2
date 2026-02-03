package appeng.hotkeys;

import net.minecraft.world.entity.player.Player;

import appeng.api.features.HotkeyAction;

public class HighlightHotkeyAction implements HotkeyAction {
    @Override
    public boolean run(Player player) {
        // NO-OP, everything is handled
        return true;
    }
}
