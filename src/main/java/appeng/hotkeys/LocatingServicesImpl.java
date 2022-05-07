package appeng.hotkeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import appeng.api.hotkeys.LocatingService;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.items.tools.powered.PortableCellItem;

/**
 * registers {@link LocatingService}
 */
public class LocatingServicesImpl {
    public static final HashMap<String, List<LocatingService>> REGISTRY = new HashMap<>();

    static {
        register(
                new InventoryLocatingService(AEItems.WIRELESS_TERMINAL.asItem(),
                        (player, i) -> AEItems.WIRELESS_TERMINAL.asItem().openFromInventory(player, i)),
                "wireless_terminal");
        register(
                new InventoryLocatingService(AEItems.WIRELESS_CRAFTING_TERMINAL.asItem(),
                        (player, i) -> AEItems.WIRELESS_CRAFTING_TERMINAL.asItem().openFromInventory(player, i)),
                "wireless_crafting_terminal");

        registerPortableCell(AEItems.PORTABLE_ITEM_CELL64K, "portable_item_cell");
        registerPortableCell(AEItems.PORTABLE_ITEM_CELL16K, "portable_item_cell");
        registerPortableCell(AEItems.PORTABLE_ITEM_CELL4K, "portable_item_cell");
        registerPortableCell(AEItems.PORTABLE_ITEM_CELL1K, "portable_item_cell");

        registerPortableCell(AEItems.PORTABLE_FLUID_CELL64K, "portable_fluid_cell");
        registerPortableCell(AEItems.PORTABLE_FLUID_CELL16K, "portable_fluid_cell");
        registerPortableCell(AEItems.PORTABLE_FLUID_CELL4K, "portable_fluid_cell");
        registerPortableCell(AEItems.PORTABLE_FLUID_CELL1K, "portable_fluid_cell");
    }

    /**
     * a convenience helper for registering hotkeys for portable cells
     */
    public static void registerPortableCell(ItemDefinition<PortableCellItem> cell, String id) {
        register(new InventoryLocatingService(cell.asItem(), cell.asItem()::openFromInventory), id);
    }

    /**
     * register a new {@link LocatingService} under an id
     * <p/>
     * a Keybinding will be created automatically for every id
     */
    public static void register(LocatingService locatingService, String id) {
        if (REGISTRY.containsKey(id)) {
            REGISTRY.get(id).add(locatingService);
        } else {
            REGISTRY.put(id, new ArrayList<>(List.of(locatingService)));
        }
    }
}
