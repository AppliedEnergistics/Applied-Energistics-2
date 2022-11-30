package appeng.client.guidebook.indices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.GuidePageChange;
import appeng.client.guidebook.compiler.ParsedGuidePage;

/**
 * Maintains an index for any given page using a mapping function for keys and values of the index.
 */
public class UniqueIndex<K, V> implements PageIndex {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueIndex.class);

    private final Map<K, Record<V>> index = new HashMap<>();

    private final String name;
    private final EntryFunction<K, V> entryFunction;

    // We need to track this to fully rebuild on incremental changes if we had duplicates
    private boolean hadDuplicates;

    public UniqueIndex(String name, EntryFunction<K, V> entryFunction) {
        this.name = name;
        this.entryFunction = entryFunction;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    public V get(K key) {
        var entry = index.get(key);
        if (entry != null) {
            return entry.value();
        }
        return null;
    }

    @Override
    public boolean supportsUpdate() {
        return true;
    }

    @Override
    public void rebuild(List<ParsedGuidePage> pages) {
        index.clear();
        hadDuplicates = false;

        for (var page : pages) {
            addToIndex(page);
        }
    }

    @Override
    public void update(List<ParsedGuidePage> allPages, List<GuidePageChange> changes) {
        if (hadDuplicates) {
            rebuild(allPages);
            return;
        }

        // Clean up all index entries associated with changed pages
        var idsToRemove = changes.stream()
                .map(GuidePageChange::pageId)
                .collect(Collectors.toSet());
        index.values().removeIf(p -> idsToRemove.contains(p.pageId));

        // Then re-add new or changed pages
        for (var change : changes) {
            var newPage = change.newPage();
            if (newPage != null) {
                addToIndex(newPage);
            }
        }
    }

    private void addToIndex(ParsedGuidePage page) {
        for (var entry : entryFunction.getEntry(page)) {
            var key = entry.getKey();
            var value = entry.getValue();
            var previousPage = index.put(key, new Record<>(page.getId(), value));
            if (previousPage != null) {
                LOGGER.warn("Key conflict in index {}: {} is used by pages {} and {}",
                        name, key, page, previousPage);
                hadDuplicates = true;
            }
        }
    }

    @FunctionalInterface
    public interface EntryFunction<K, V> {
        Iterable<Pair<K, V>> getEntry(ParsedGuidePage page);
    }

    private record Record<V> (ResourceLocation pageId, V value) {
    }
}
