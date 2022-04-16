package appeng.hotkeys;

import net.minecraft.world.entity.player.Player;

/**
 * locates and opens Inventories from items
 */
public interface LocatingService {

    /**
     * locates and opens an Inventory and returns if it has found anything
     */
    boolean findAndOpen(Player player);
}
