package appeng.client.gui.me.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;

import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;

public class RepoSearch {

    private String searchString = "";

    // Cached information
    private final Long2BooleanMap cache = new Long2BooleanOpenHashMap();
    private Predicate<GridInventoryEntry> search = (e) -> true;
    final Map<AEKey, String> tooltipCache = new WeakHashMap<>();

    public RepoSearch() {
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        if (!searchString.equals(this.searchString)) {
            this.search = fromString(searchString);
            this.searchString = searchString;
            this.cache.clear();
        }
    }

    public boolean matches(GridInventoryEntry entry) {
        return cache.computeIfAbsent(entry.getSerial(), s -> search.test(entry));
    }

    /*
     * Creates a predicate for provided search string.
     */
    private Predicate<GridInventoryEntry> fromString(String searchString) {
        var orParts = searchString.split("\\|");

        if (orParts.length == 1) {
            return AndSearchPredicate.of(getPredicates(orParts[0]));
        } else {
            var orPartFilters = new ArrayList<Predicate<GridInventoryEntry>>(orParts.length);

            for (String orPart : orParts) {
                orPartFilters.add(AndSearchPredicate.of(getPredicates(orPart)));
            }

            return OrSearchPredicate.of(orPartFilters);
        }
    }

    /*
     * Created as a helper function for {@code fromString()}. This is designed to handle between the | (or operations)
     * to and the searched together delimited by " " Each space in {@code query} treated as a separate 'and' operation.
     */
    private List<Predicate<GridInventoryEntry>> getPredicates(String query) {
        var terms = query.toLowerCase().trim().split("\\s+");
        var predicateFilters = new ArrayList<Predicate<GridInventoryEntry>>(terms.length);

        for (String part : terms) {
            if (part.startsWith("@")) {
                predicateFilters.add(new ModSearchPredicate(part.substring(1)));
            } else if (part.startsWith("#")) {
                predicateFilters.add(new TooltipsSearchPredicate(part.substring(1), tooltipCache));
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
