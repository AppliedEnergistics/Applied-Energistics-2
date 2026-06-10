package appeng.hotkeys;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Stream;

import appeng.util.SearchInventoryEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.ItemLike;

import appeng.api.features.HotkeyAction;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import net.neoforged.neoforge.common.NeoForge;

public record InventoryHotkeyAction(Predicate<ItemInstance> locatable, Opener opener) implements HotkeyAction {

    public InventoryHotkeyAction(ItemLike item, Opener opener) {
        this((stack) -> stack.is(item.asItem()), opener);
    }

    @Override
    public boolean run(Player player) {
        var items = player.getInventory().getNonEquipmentItems();
        for (int i = 0; i < items.size(); i++) {
            if (locatable.test(items.get(i))) {
                if (opener.open(player, MenuLocators.forInventorySlot(i))) {
                    return true;
                }
            }
        }
        // Check via event for item sources like curios
        var streams = new ArrayList<Stream<SearchInventoryEvent.InventoryItemAccessor>>();
        var event = new SearchInventoryEvent(player, streams);
        NeoForge.EVENT_BUS.post(event);
        var it = streams.stream().flatMap(x -> x).iterator();
        while (it.hasNext()) {
            var slot = it.next();
            if (locatable.test(slot.getItem())) {
                if (opener.open(player, slot.createLocator())) {
                    return true;
                }
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface Opener {
        boolean open(Player player, ItemMenuHostLocator locator);
    }
}
