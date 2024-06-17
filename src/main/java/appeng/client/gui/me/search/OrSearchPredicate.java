package appeng.client.gui.me.search;

import java.util.List;
import java.util.function.Predicate;

import appeng.menu.me.common.GridInventoryEntry;

final class OrSearchPredicate implements Predicate<GridInventoryEntry> {
    private final List<Predicate<GridInventoryEntry>> terms;

    private OrSearchPredicate(List<Predicate<GridInventoryEntry>> terms) {
        this.terms = terms;
    }

    public static Predicate<GridInventoryEntry> of(List<Predicate<GridInventoryEntry>> filters) {
        if (filters.isEmpty()) {
            return t -> false;
        }
        if (filters.size() == 1) {
            return filters.get(0);
        }
        return new OrSearchPredicate(filters);
    }

    @Override
    public boolean test(GridInventoryEntry entry) {
        for (var term : terms) {
            if (term.test(entry)) {
                return true;
            }
        }

        return false;
    }
}
