package appeng.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.events.Event;
import appeng.api.events.EventFactory;

public class SearchInventoryEvent {
    /**
     * Event fired when AE2 is looking for ItemStacks in a player inventory. By default, AE2 only looks at the 36 usual
     * slots of the player inventory, use this event to make AE2 consider more stacks. AE2 will check after the event if
     * they contain the item it is searching.
     */
    public static final Event<BiConsumer<List<ItemStack>, Player>> EVENT = EventFactory.createArrayBacked(
            BiConsumer.class,
            (events) -> (items, player) -> {
                for (var event : events) {
                    event.accept(items, player);
                }
            });

    static {
        EVENT.register((stacks, player) -> stacks.addAll(player.getInventory().items));
    }

    public static List<ItemStack> getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        EVENT.invoker().accept(items, player);
        return items;
    }
}
