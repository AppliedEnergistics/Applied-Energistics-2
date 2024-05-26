package appeng.client.gui.me.search;

import java.util.Objects;
import java.util.function.Predicate;

import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;

final class ItemIdSearchPredicate implements Predicate<GridInventoryEntry> {
    private final String inputItemID;

    public ItemIdSearchPredicate(String inputItemID) {
        this.inputItemID = inputItemID.toLowerCase();
    }

    @Override
    public boolean test(GridInventoryEntry gridInventoryEntry) {
        AEKey entryInfo = Objects.requireNonNull(gridInventoryEntry.getWhat());
        String ItemID = entryInfo.getId().toString();
        return ItemID.toLowerCase().contains(inputItemID);
    }
}
