package appeng.client.gui.me.search;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.ChatFormatting;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.core.AEConfig;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.util.Platform;

final class TooltipsSearchPredicate implements Predicate<GridInventoryEntry> {
    private final String tooltip;
    private final RepoSearch repoSearch;

    public TooltipsSearchPredicate(String tooltip, RepoSearch repoSearch) {
        this.tooltip = standardify(tooltip.toLowerCase());
        this.repoSearch = repoSearch;
    }

    @Override
    public boolean test(GridInventoryEntry gridInventoryEntry) {
        AEKey entryInfo = Objects.requireNonNull(gridInventoryEntry.getWhat());
        var tooltipText = getTooltipText(entryInfo);

        if (tooltipText.toLowerCase().contains(tooltip)) {
            return true;
        }

        return false;
    }

    /**
     * Gets the concatenated text of a keys tooltip for search purposes.
     */
    private String getTooltipText(AEKey what) {
        return repoSearch.tooltipCache.computeIfAbsent(what, key -> {
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

            return standardify(tooltipText.toString());
        });
    }

    /**
     * util function to standardify the tool tips string
     */
    private String standardify(String input) {
        return input.toLowerCase().replace(" ", "");
    }
}
