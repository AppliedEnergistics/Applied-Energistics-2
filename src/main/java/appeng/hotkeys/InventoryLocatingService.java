package appeng.hotkeys;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.hotkeys.LocatingService;

public class InventoryLocatingService implements LocatingService {

    private final Locatable locatable;
    private final Opener opener;

    public InventoryLocatingService(Locatable locatable, Opener opener) {
        this.locatable = locatable;
        this.opener = opener;
    }

    public InventoryLocatingService(Item item, Opener opener) {
        this((stack) -> stack.getItem() == item, opener);
    }

    @Override
    public boolean findAndOpen(Player player) {
        var items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++) {
            if (locatable.matchItem(items.get(i))) {
                opener.open(player, i);
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface Locatable {
        boolean matchItem(ItemStack stack);
    }

    @FunctionalInterface
    public interface Opener {
        boolean open(Player player, int inventorySlot);
    }
}
