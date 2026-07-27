package appeng.util;

import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Event fired when AE2 is looking for ItemStacks in a player inventory. By default, AE2 only looks at the 36 usual
 * slots of the player inventory, use this event to make AE2 consider more stacks. AE2 will check after the event if
 * they contain the item it is searching.
 */
public class SearchInventoryEvent extends PlayerEvent {
    private final List<Stream<InventoryItemAccessor>> streams;

    public SearchInventoryEvent(Player player, List<Stream<InventoryItemAccessor>> streams) {
        super(player);
        this.streams = streams;
    }

    static {
        NeoForge.EVENT_BUS.addListener((SearchInventoryEvent event) -> {
            var player = event.getEntity();
            Stream<InventoryItemAccessor> stream = IntStream.of(0, Inventory.INVENTORY_SIZE)
                    .mapToObj(index -> new InventoryItemAccessor() {
                        @Override
                        public ItemAccess getAccess() {
                            return ItemAccess.forPlayerSlot(player, index);
                        }

                        @Override
                        public ItemMenuHostLocator createLocator() {
                            return MenuLocators.forInventorySlot(index);
                        }
                    });
            event.add(stream);
        });
    }

    public void add(Stream<InventoryItemAccessor> slots) {
        streams.add(slots);
    }

    public static Stream<InventoryItemAccessor> getInventoryAccessors(Player player) {
        List<Stream<InventoryItemAccessor>> items = new ArrayList<>();
        NeoForge.EVENT_BUS.post(new SearchInventoryEvent(player, items));
        return items.stream().flatMap(x -> x);
    }

    public interface InventoryItemAccessor {
        ItemAccess getAccess();

        ItemMenuHostLocator createLocator();
    }
}
