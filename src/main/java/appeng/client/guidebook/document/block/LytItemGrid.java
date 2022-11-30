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
    private final List<LytSlot> slots = new ArrayList<>();

    public LytItemGrid() {
        setPadding(5);
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        var cols = Math.max(1, availableWidth / LytSlot.OUTER_SIZE);
        var rows = (slots.size() + cols - 1) / cols;

        for (int i = 0; i < slots.size(); i++) {
            var slotX = i % cols;
            var slotY = i / cols;
            slots.get(i).layout(
                    context,
                    x + slotX * LytSlot.OUTER_SIZE,
                    y + slotY * LytSlot.OUTER_SIZE,
                    availableWidth
            );
        }

        return new LytRect(
                x,
                y,
                cols * LytSlot.OUTER_SIZE,
                rows * LytSlot.OUTER_SIZE
        );
    }

    public void addItem(Item item) {
        var slot = new LytSlot(item.getDefaultInstance());
        slots.add(slot);
        append(slot);
    }
}
