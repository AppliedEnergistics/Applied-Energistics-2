package appeng.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class SearchInventoryEvent extends Event {
    private final List<ItemStack> items;
    private final Player player;

    /**
     * Event fired when AE2 is looking for ItemStacks in a player inventory. By default, AE2 only looks at the 36 usual
     * slots of the player inventory, use this event to make AE2 consider more stacks. AE2 will check after the event if
     * they contain the item it is searching.
     */
    public SearchInventoryEvent(List<ItemStack> items, Player player) {
        this.items = items;
        this.player = player;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public Player getPlayer() {
        return player;
    }

    static {
        MinecraftForge.EVENT_BUS.addListener((Consumer<SearchInventoryEvent>) event -> {
            event.getItems().addAll(event.getPlayer().getInventory().items);
        });
    }

    public static List<ItemStack> getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        MinecraftForge.EVENT_BUS.post(new SearchInventoryEvent(items, player));
        return items;
    }
}
