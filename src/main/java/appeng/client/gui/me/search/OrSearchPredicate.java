package appeng.client.gui.me.search;

import java.util.List;
import java.util.function.Predicate;

import appeng.menu.me.common.GridInventoryEntry;

final class OrSearchPredicate implements Predicate<GridInventoryEntry> {
    private final List<Predicate<GridInventoryEntry>> orPartFilters;

    private OrSearchPredicate(List<Predicate<GridInventoryEntry>> orPartFilters) {
        this.orPartFilters = orPartFilters;
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
    public boolean test(GridInventoryEntry gridStack) {
        for (Predicate<GridInventoryEntry> part : orPartFilters) {
            if (part.test(gridStack)) {
                return true;
            }
        }

        return false;
    }
}
