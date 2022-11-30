package appeng.client.guidebook.document.block;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows items in a grid-like fashion, i.e. to show-case variants.
 */
public class LytItemGrid extends LytBox {
    private static final int PADDING = 5;

    private final List<LytSlot> slots = new ArrayList<>();

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        var cols = Math.max(1, (availableWidth - 2 * PADDING) / LytSlot.OUTER_SIZE);
        var rows = (slots.size() + cols - 1) / cols;

        for (int i = 0; i < slots.size(); i++) {
            var slotX = i % cols;
            var slotY = i / cols;
            slots.get(i).layout(
                    context,
                    x + PADDING + slotX * LytSlot.OUTER_SIZE,
                    y + PADDING + slotY * LytSlot.OUTER_SIZE,
                    availableWidth
            );
        }

        return new LytRect(
                x,
                y,
                2 * PADDING + cols * LytSlot.OUTER_SIZE,
                2 * PADDING + rows * LytSlot.OUTER_SIZE
        );
    }

    public void addItem(Item item) {
        var slot = new LytSlot(item.getDefaultInstance());
        slots.add(slot);
        append(slot);
    }
}
