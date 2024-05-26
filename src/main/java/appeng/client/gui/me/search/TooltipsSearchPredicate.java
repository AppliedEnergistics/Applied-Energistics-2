package appeng.client.gui.me.search;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.network.chat.Component;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;

final class TooltipsSearchPredicate implements Predicate<GridInventoryEntry> {
    private final String tooltip;

    public TooltipsSearchPredicate(String tooltip) {
        this.tooltip = tooltip.toLowerCase();
    }

    @Override
    public boolean test(GridInventoryEntry gridInventoryEntry) {
        AEKey entryInfo = Objects.requireNonNull(gridInventoryEntry.getWhat());
        List<Component> stackTooltip = AEKeyRendering.getTooltip(entryInfo);

        for (int i = 1; i < stackTooltip.size(); ++i) {
            if (stackTooltip.get(i).getString().toLowerCase().contains(tooltip.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
