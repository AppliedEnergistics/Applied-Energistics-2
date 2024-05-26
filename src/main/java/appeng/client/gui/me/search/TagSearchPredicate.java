package appeng.client.gui.me.search;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.minecraft.tags.TagKey;

import appeng.api.stacks.AEKeyType;
import appeng.menu.me.common.GridInventoryEntry;

final class TagSearchPredicate implements Predicate<GridInventoryEntry> {
    private final Pattern searchPattern;
    /**
     * Stores the tag keys we found for each AE key type we encountered.
     */
    private final Map<AEKeyType, List<TagKey<?>>> tagCache = new IdentityHashMap<>();

    public TagSearchPredicate(String searchPattern) {
        this.searchPattern = createPattern(searchPattern);
    }

    private static Pattern createPattern(String searchText) {
        try {
            return Pattern.compile(searchText.toLowerCase(),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException ignored) {
            return Pattern.compile(Pattern.quote(searchText.toLowerCase()),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
    }

    /**
     * Finds all tags for all AE key types that match the given search pattern.
     */
    private List<TagKey<?>> buildTagCache(AEKeyType keyType) {
        return keyType.getTagNames()
                .filter(tagKey -> searchPattern.matcher(tagKey.location().toString()).find())
                .toList();
    }

    @Override
    public boolean test(GridInventoryEntry entry) {
        var what = Objects.requireNonNull(entry.getWhat());
        var tags = tagCache.computeIfAbsent(what.getType(), this::buildTagCache);

        for (var tag : tags) {
            if (what.isTagged(tag)) {
                return true;
            }
        }

        return false;
    }
}
