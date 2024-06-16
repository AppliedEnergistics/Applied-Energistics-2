package appeng.client.gui.me.search;

import java.util.Objects;
import java.util.function.Predicate;

import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;

final class NameSearchPredicate implements Predicate<GridInventoryEntry> {
    private final String term;

    public NameSearchPredicate(String term) {
        this.term = term.toLowerCase();
    }

    @Override
    public boolean test(GridInventoryEntry gridInventoryEntry) {
        AEKey entryInfo = Objects.requireNonNull(gridInventoryEntry.getWhat());
        String displayName = entryInfo.getDisplayName().getString();
        return displayName.toLowerCase().contains(term);
    }
}
