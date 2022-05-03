package appeng.client.gui.me.search;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import appeng.api.config.YesNo;
import appeng.core.AEConfig;
import appeng.menu.me.common.GridInventoryEntry;

final class SearchPredicates {

    static Predicate<GridInventoryEntry> fromString(String searchString, RepoSearch repoSearch) {
        if (searchString.startsWith("@")) {
            return createModIdPredicate(searchString.substring(1));
        } else if (searchString.startsWith("*")) {
            return createIdPredicate(searchString.substring(1));
        } else if (searchString.startsWith("#")) {
            var pattern = createPattern(searchString.substring(1));
            return new TagPredicate(pattern);
        } else {
            var pattern = createPattern(searchString);

            var result = createNamePredicate(pattern);
            if (AEConfig.instance().getSearchTooltips() != YesNo.NO) {
                result = result.or(createTooltipPredicate(pattern, repoSearch));
            }

            return result;
        }
    }

    private static Predicate<GridInventoryEntry> createModIdPredicate(String searchText) {
        var searchPattern = createPattern(searchText);
        return entry -> {
            var what = Objects.requireNonNull(entry.getWhat());
            return searchPattern.matcher(what.getModId()).find();
        };
    }

    private static Predicate<GridInventoryEntry> createIdPredicate(String searchText) {
        var searchPattern = createPattern(searchText);
        return entry -> {
            var what = Objects.requireNonNull(entry.getWhat());
            return searchPattern.matcher(what.getId().toString()).find();
        };
    }

    private static Predicate<GridInventoryEntry> createNamePredicate(Pattern searchPattern) {
        return entry -> {
            var what = Objects.requireNonNull(entry.getWhat());
            String displayName = what.getDisplayName().getString();
            return searchPattern.matcher(displayName).find();
        };
    }

    private static Predicate<GridInventoryEntry> createTooltipPredicate(Pattern searchPattern, RepoSearch repoSearch) {
        return entry -> {
            var tooltip = repoSearch.getTooltip(entry);

            for (var line : tooltip) {
                if (searchPattern.matcher(line.getString()).find()) {
                    return true;
                }
            }

            return false;
        };
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

}
