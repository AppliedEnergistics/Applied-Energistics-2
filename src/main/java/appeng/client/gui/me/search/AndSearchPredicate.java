package appeng.client.gui.me.search;

import java.util.List;
import java.util.function.Predicate;

import appeng.menu.me.common.GridInventoryEntry;

final class AndSearchPredicate implements Predicate<GridInventoryEntry> {
    private final List<Predicate<GridInventoryEntry>> terms;

    private AndSearchPredicate(List<Predicate<GridInventoryEntry>> terms) {
        this.terms = terms;
    }

    public static Predicate<GridInventoryEntry> of(List<Predicate<GridInventoryEntry>> predicates) {
        if (predicates.isEmpty()) {
            return t -> true;
        }
        if (predicates.size() == 1) {
            return predicates.get(0);
        }
        return new AndSearchPredicate(predicates);
    }

    @Override
    public boolean test(GridInventoryEntry entry) {
        for (var term : terms) {
            if (!term.test(entry)) {
                return false;
            }
        }

        return true;
    }
}
