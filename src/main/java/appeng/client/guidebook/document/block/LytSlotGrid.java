package appeng.client.guidebook.document.block;

import appeng.client.gui.Icon;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.ColorRef;
import appeng.client.guidebook.render.RenderContext;
import net.minecraft.world.item.crafting.Ingredient;

public class LytSlotGrid extends LytBox {
    private final int width;
    private final int height;
    private final LytSlot[] slots;
    private boolean renderEmptySlots = true;

    public LytSlotGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.slots = new LytSlot[width * height];
    }

    public boolean isRenderEmptySlots() {
        return renderEmptySlots;
    }

    public void setRenderEmptySlots(boolean renderEmptySlots) {
        this.renderEmptySlots = renderEmptySlots;
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        // Lay out the slots left-to-right, top-to-bottom
        for (var row = 0; row < height; row++) {
            for (var col = 0; col < width; col++) {
                var index = getSlotIndex(col, row);
                if (index < slots.length) {
                    var slot = slots[index];
                    if (slot != null) {
                        slot.layout(
                                context,
                                x + col * LytSlot.OUTER_SIZE,
                                y + row * LytSlot.OUTER_SIZE,
                                availableWidth
                        );
                    }
                }
            }
        }

        return new LytRect(x, y, LytSlot.OUTER_SIZE * width, LytSlot.OUTER_SIZE * height);
    }

    public void setIngredient(int x, int y, Ingredient ingredient) {
        if (x < 0 || x >= width) {
            throw new IndexOutOfBoundsException("x: " + x);
        }
        if (y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("y: " + y);
        }

        var slotIndex = getSlotIndex(x, y);
        var slot = slots[slotIndex];
        if (slot != null) {
            slot.removeChild(slot);
            slots[slotIndex] = null;
        }

        slot = slots[slotIndex] = new LytSlot(ingredient);
        append(slot);
    }

    @Override
    public void render(RenderContext context) {
        // Render empty slots if requested
        if (renderEmptySlots) {
            for (var y = 0; y < height; y++) {
                for (var x = 0; x < width; x++) {
                    var index = getSlotIndex(x, y);
                    if (index >= slots.length || slots[index] == null) {
                        context.drawIcon(
                                bounds.x() + LytSlot.OUTER_SIZE * x,
                                bounds.y() + LytSlot.OUTER_SIZE * y,
                                Icon.SLOT_BACKGROUND,
                                ColorRef.WHITE
                        );
                    }
                }
            }
        }

        super.render(context);
    }

    private int getSlotIndex(int col, int row) {
        return row * width + col;
    }
}
