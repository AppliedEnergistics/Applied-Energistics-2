package appeng.client.gui.me.search;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import net.minecraft.ChatFormatting;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.core.AEConfig;
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
            var lines = AEKeyRendering.getTooltip(key);

            var tooltipText = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i);

                // Process last line and skip mod name if our heuristic detects it
                if (i > 0 && i >= lines.size() - 1 && !AEConfig.instance().isSearchModNameInTooltips()) {
                    var text = line.getString();
                    boolean hadFormatting = false;
                    if (text.indexOf(ChatFormatting.PREFIX_CODE) != -1) {
                        text = ChatFormatting.stripFormatting(text);
                        hadFormatting = true;
                    } else {
                        hadFormatting = !line.getStyle().isEmpty();
                    }

                    if (!hadFormatting || !Objects.equals(text, Platform.getModName(what.getModId()))) {
                        tooltipText.append('\n').append(text);
                    }
                } else {
                    if (i > 0) {
                        tooltipText.append('\n');
                    }
                    line.visit(text -> {
                        if (text.indexOf(ChatFormatting.PREFIX_CODE) != -1) {
                            text = ChatFormatting.stripFormatting(text);
                        }
                        tooltipText.append(text);
                        return Optional.empty();
                    });
                }
            }

            return tooltipText.toString();
        });
    }
}
