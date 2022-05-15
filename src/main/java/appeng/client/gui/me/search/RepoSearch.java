package appeng.client.gui.me.search;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;

import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.util.Platform;

public class RepoSearch {

    private String searchString = "";

    // Cached information
    private final Long2BooleanMap cache = new Long2BooleanOpenHashMap();
    private Predicate<GridInventoryEntry> search = (e) -> true;

    private final Map<AEKey, String> tooltipCache = new WeakHashMap<>();

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

    /**
     * Gets the concatenated text of a keys tooltip for search purposes.
     */
    public String getTooltipText(AEKey what) {
        return tooltipCache.computeIfAbsent(what, key -> {
            return Platform.getTooltip(key).stream()
                    .map(Component::getString)
                    .collect(Collectors.joining("\n"));
        });
    }
}
