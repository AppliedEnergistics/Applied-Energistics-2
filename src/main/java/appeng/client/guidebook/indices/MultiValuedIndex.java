package appeng.client.guidebook.indices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.GuidePageChange;
import appeng.client.guidebook.compiler.ParsedGuidePage;

/**
 * A convenient index base-class for indices that map keys to multiple pages.
 */
public class MultiValuedIndex<K, V> implements PageIndex {
    private final Map<K, List<Record<V>>> index = new HashMap<>();

    private final String name;
    private final EntryFunction<K, V> entryFunction;

    public MultiValuedIndex(String name, EntryFunction<K, V> entryFunction) {
        this.name = name;
        this.entryFunction = entryFunction;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<V> get(K key) {
        var entries = index.get(key);
        if (entries != null) {
            return entries.stream().map(Record::value).toList();
        }
        return List.of();
    }

    @Override
    public boolean supportsUpdate() {
        return true;
    }

    @Override
    public void rebuild(List<ParsedGuidePage> pages) {
        index.clear();

        for (var page : pages) {
            addToIndex(page);
        }
    }

    @Override
    public void update(List<ParsedGuidePage> allPages, List<GuidePageChange> changes) {
        // Clean up all index entries associated with changed pages
        var idsToRemove = changes.stream()
                .map(GuidePageChange::pageId)
                .collect(Collectors.toSet());
        var it = index.values().iterator();
        while (it.hasNext()) {
            var entries = it.next();
            entries.removeIf(p -> idsToRemove.contains(p.pageId));
            if (entries.isEmpty()) {
                it.remove();
            }
        }

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
            var entries = index.computeIfAbsent(key, k -> new ArrayList<>());
            entries.add(new Record<>(page.getId(), value));
        }
    }

    @FunctionalInterface
    public interface EntryFunction<K, V> {
        Iterable<Pair<K, V>> getEntry(ParsedGuidePage page);
    }

    private record Record<V> (ResourceLocation pageId, V value) {
    }
}
