package appeng.hotkeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import appeng.api.hotkeys.HotkeyAction;
import appeng.api.hotkeys.HotkeyActions;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.items.tools.powered.PortableCellItem;

/**
 * registers {@link HotkeyAction}
 */
public class HotkeyActionsImpl extends HotkeyActions {
    public static final HashMap<String, List<HotkeyAction>> REGISTRY = new HashMap<>();

    static {
        register(
                new InventoryHotkeyAction(AEItems.WIRELESS_TERMINAL.asItem(),
                        (player, i) -> AEItems.WIRELESS_TERMINAL.asItem().openFromInventory(player, i)),
                WIRELESS_TERMINAL);
        register(
                new InventoryHotkeyAction(AEItems.WIRELESS_CRAFTING_TERMINAL.asItem(),
                        (player, i) -> AEItems.WIRELESS_CRAFTING_TERMINAL.asItem().openFromInventory(player, i)),
                WIRELESS_TERMINAL);

        registerPortableCell(AEItems.PORTABLE_ITEM_CELL1K, PORTABLE_ITEM_CELL);
        registerPortableCell(AEItems.PORTABLE_ITEM_CELL4K, PORTABLE_ITEM_CELL);
        registerPortableCell(AEItems.PORTABLE_ITEM_CELL16K, PORTABLE_ITEM_CELL);
        registerPortableCell(AEItems.PORTABLE_ITEM_CELL64K, PORTABLE_ITEM_CELL);
        registerPortableCell(AEItems.PORTABLE_ITEM_CELL256K, PORTABLE_ITEM_CELL);

        registerPortableCell(AEItems.PORTABLE_FLUID_CELL1K, PORTABLE_FLUID_CELL);
        registerPortableCell(AEItems.PORTABLE_FLUID_CELL4K, PORTABLE_FLUID_CELL);
        registerPortableCell(AEItems.PORTABLE_FLUID_CELL16K, PORTABLE_FLUID_CELL);
        registerPortableCell(AEItems.PORTABLE_FLUID_CELL64K, PORTABLE_FLUID_CELL);
        registerPortableCell(AEItems.PORTABLE_FLUID_CELL256K, PORTABLE_FLUID_CELL);
    }

    /**
     * a convenience helper for registering hotkeys for portable cells
     */
    public static void registerPortableCell(ItemDefinition<PortableCellItem> cell, String id) {
        register(new InventoryHotkeyAction(cell.asItem(), cell.asItem()::openFromInventory), id);
    }

    /**
     * see {@link HotkeyActions#register(HotkeyAction, String)}
     */
    public static void register(HotkeyAction hotkeyAction, String id) {
        if (REGISTRY.containsKey(id)) {
            REGISTRY.get(id).add(0, hotkeyAction);
        } else {
            REGISTRY.put(id, new ArrayList<>(List.of(hotkeyAction)));
        }
    }
}
