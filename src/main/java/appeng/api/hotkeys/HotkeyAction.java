package appeng.api.hotkeys;

import net.minecraft.world.entity.player.Player;

/**
 * locates and opens Inventories from items
 */
public interface HotkeyAction {

    /**
     * locates and opens an Inventory and returns if it has found anything
     */
    boolean run(Player player);
}
