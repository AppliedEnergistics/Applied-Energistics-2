package appeng.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WirelessTerminalEvent {
    public static final Event<BiConsumer<List<ItemStack>, Player>> EVENT = EventFactory.createArrayBacked(
            BiConsumer.class,
            (events) -> (items, player) -> {
                for (var event : events) {
                    event.accept(items, player);
                }
            });

    public static void init() {
        EVENT.register((stacks, player) -> stacks.addAll(player.getInventory().items));
    }

    public static List<ItemStack> getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        EVENT.invoker().accept(items, player);
        return items;
    }
}
