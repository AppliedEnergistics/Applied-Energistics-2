package appeng.client.gui.me.search;

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
    public final Map<AEKey, String> tooltipCache = new WeakHashMap<>();

    public RepoSearch() {
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        if (!searchString.equals(this.searchString)) {
            this.search = SearchPredicates.fromString(searchString, this);
            this.searchString = searchString;
            this.cache.clear();
        }
    }

    public boolean matches(GridInventoryEntry entry) {
        return cache.computeIfAbsent(entry.getSerial(), s -> search.test(entry));
    }
}
