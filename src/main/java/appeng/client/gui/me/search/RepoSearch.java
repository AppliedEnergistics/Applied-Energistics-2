package appeng.client.gui.me.search;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import appeng.menu.me.common.GridInventoryEntry;
import appeng.util.Platform;

public class RepoSearch {

    private String searchString = "";

    // Cached information
    private final Long2BooleanMap cache = new Long2BooleanOpenHashMap();
    private Predicate<GridInventoryEntry> search = (e) -> true;

    private final Long2ObjectMap<List<Component>> tooltipCache = new Long2ObjectOpenHashMap<>();

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
        // first cache the result, when got, it cannot be null
        tooltipCache.computeIfAbsent(entry.getSerial(), s -> Platform.getTooltip(entry.getWhat()));
        return cache.computeIfAbsent(entry.getSerial(), s -> search.test(entry));
    }

    public List<Component> getTooltip(GridInventoryEntry entry) {
        return Objects.requireNonNull(tooltipCache.get(entry.getSerial()));
    }
}
