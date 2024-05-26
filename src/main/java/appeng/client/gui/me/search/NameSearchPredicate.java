package appeng.client.gui.me.search;

import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;

import java.util.Objects;
import java.util.function.Predicate;

final class NameSearchPredicate implements Predicate<GridInventoryEntry> {
    private final String name;

    public NameSearchPredicate(String name) {
        this.name = name.toLowerCase();
    }
    @Override
    public boolean test(GridInventoryEntry gridInventoryEntry) {
        AEKey entryInfo = Objects.requireNonNull(gridInventoryEntry.getWhat());
        String displayName = entryInfo.getDisplayName().getString();
        return displayName.toLowerCase().contains(name);
    }
}