package appeng.client.gui.me.search;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import appeng.menu.me.common.GridInventoryEntry;

final class SearchPredicates {

    static Predicate<GridInventoryEntry> fromString(String searchString) {
        List<Predicate<GridInventoryEntry>> gridPredictions;

        String[] orParts = searchString.split("\\|");

        if (orParts.length == 1) {
            gridPredictions = getPredicates(searchString);
        } else {
            List<Predicate<GridInventoryEntry>> orPartFilters = new LinkedList<>();

            for (String orPart : orParts) {
                orPartFilters.add(AndSearchPredicate.of(getPredicates(orPart)));
            }

            gridPredictions = new LinkedList<>();
            gridPredictions.add(OrSearchPredicate.of(orPartFilters));
        }

        return AndSearchPredicate.of(gridPredictions);
    }

    private static List<Predicate<GridInventoryEntry>> getPredicates(String query) {
        List<Predicate<GridInventoryEntry>> predicateFilters = new LinkedList<>();

        for (String part : query.toLowerCase().trim().split(" ")) {
            if (part.startsWith("@")) {
                predicateFilters.add(new ModSearchPredicate(part.substring(1)));
            } else if (part.startsWith("#")) {
                predicateFilters.add(new TooltipsSearchPredicate(part.substring(1)));
            } else if (part.startsWith("$")) {
                predicateFilters.add(new TagSearchPredicate(part.substring(1)));
            } else if (part.startsWith("*")) {
                predicateFilters.add(new ItemIdSearchPredicate(part.substring(1)));
            } else {
                predicateFilters.add(new NameSearchPredicate(part));
            }
        }

        return predicateFilters;
    }
}
