package appeng.client.gui.me.search;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import appeng.core.AEConfig;
import appeng.menu.me.common.GridInventoryEntry;

final class SearchPredicates {

    /*
     * Creates a predicate for provided search string.
     */
    static Predicate<GridInventoryEntry> fromString(String searchString, RepoSearch repoSearch) {
        List<Predicate<GridInventoryEntry>> gridPredictions;

        String[] orParts = searchString.split("\\|");

        if (orParts.length == 1) {
            gridPredictions = getPredicates(orParts[0], repoSearch);
        } else {
            List<Predicate<GridInventoryEntry>> orPartFilters = new LinkedList<>();

            for (String orPart : orParts) {
                orPartFilters.add(AndSearchPredicate.of(getPredicates(orPart, repoSearch)));
            }

            gridPredictions = new LinkedList<>();
            gridPredictions.add(OrSearchPredicate.of(orPartFilters));
        }

        return AndSearchPredicate.of(gridPredictions);
    }

    /*
     * Created as a helper function for {@code fromString()}. This is designed to handle between the | (or operations)
     * to and the searched together delimited by " " Each space in {@code query} treated as a separate 'and' operation.
     */
    private static List<Predicate<GridInventoryEntry>> getPredicates(String query, RepoSearch repoSearch) {
        List<Predicate<GridInventoryEntry>> predicateFilters = new LinkedList<>();

        for (String part : query.toLowerCase().trim().split(" ")) {
            if (part.startsWith("@")) {
                predicateFilters.add(new ModSearchPredicate(part.substring(1)));
            } else if (part.startsWith("#")) {
                predicateFilters.add(new TooltipsSearchPredicate(part.substring(1), repoSearch));
            } else if (part.startsWith("$")) {
                predicateFilters.add(new TagSearchPredicate(part.substring(1)));
            } else if (part.startsWith("*")) {
                predicateFilters.add(new ItemIdSearchPredicate(part.substring(1)));
            } else {
                if (AEConfig.instance().isSearchTooltips()) {
                    // The tooltip includes the display name too
                    predicateFilters.add(new TooltipsSearchPredicate(part, repoSearch));
                } else {
                    predicateFilters.add(new NameSearchPredicate(part));
                }
            }
        }

        return predicateFilters;
    }
}
